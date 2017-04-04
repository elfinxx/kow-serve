package org.kow.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.jsoup.HttpStatusException;
import org.kow.domain.Position;
import org.kow.domain.Tier;
import org.kow.domain.User;
import org.kow.util.POWScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.kow.util.GoogleSheetHelper.getSheetsService;

@Service
public class GoogleSheetUserServiceImpl implements UserService {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Sheets service;
    private String spreadsheetId = "1D63gLoHAt2l_yaW-F47pkjsqq-82JlGKZ5wXolEj-fI";

    public GoogleSheetUserServiceImpl() throws IOException {
        service = getSheetsService();
    }

    @Override
    @Cacheable("users")
    public List<User> getUsers() {
        String range = "users!A2:G";
        List<User> users = new ArrayList<>();
        for (List<Object> userData : getValuesBy(range)) {
            users.add(generateUserFromRawData(userData));
        }
        return users;
    }

    @Override
    @Cacheable("jjals")
    public List<String> getJjals() {
        String range = "jjal!A2:A";
        List<String> jjals = new ArrayList<>();
        for (List<Object> rawData : getValuesBy(range)) {
            jjals.add((String) rawData.get(0));
        }
        return jjals;
    }

    @Override
    public String addJjal(String jjal) {
        String range = "jjal!A2:A";
        List<List<Object>> values = getValuesBy(range);

        logger.info(jjal);

        List<Object> addValue = new ArrayList<>();
        addValue.add(jjal);
        values.add(addValue);

        ValueRange valueRange = new ValueRange();
        valueRange.setValues(values);
        try {
            service.spreadsheets().values()
                    .update(spreadsheetId, "jjal!A2:A", valueRange)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jjal;
    }

    @Override
    public List<User> getDiscordUsers() {
        List<User> allUsers = getUsers();
        List<String> bts = getDiscordMembersBts();
        List<User> users = new ArrayList<>();

        for (User user : allUsers) {
            for (String bt : bts) {
                if (user.getBattleTag().equals(bt)) {
                    users.add(user);
                    System.out.println(bt);
                    break;
                }
            }
        }

        return users;
    }

    @Override
    public User getUser(String battleTag) {
        battleTag = battleTag.replace('-', '#');
        List<User> users = getUsers();

        for (User aUser : users) {
            if (aUser.getBattleTag().equals(battleTag)) {
                return aUser;
            }
        }
        return null;
    }

    @Override
    public User addUser(String battleTag) {
        return null;
    }

    @Override
    public User removeUser(String battleTag) {
        return null;
    }

    @Override
    public User updateUser(User aUser) {
        return null;
    }

    @Override
    @CacheEvict("users")
    @Scheduled(fixedRate = 1000 * 60 * 60)
    public List<User> updateUsers() {
        String range = "bt!A2:C";
        List<List<Object>> values = getValuesBy(range);
        List<List<Object>> writeValues = new ArrayList<>();
        List<User> updatedUsers = new ArrayList<>();
        for (List<Object> userData : values) {
            logger.info("Update " + userData.get(0));
            try {
                User aUser = POWScraper.getUser(((String) userData.get(0)));
                aUser.setOverlogId((String) userData.get(1));
                aUser.setGroup((String) userData.get(2));
                updatedUsers.add(aUser);
                writeValues.add(writeRawDataFromUser(aUser));
            } catch (HttpStatusException e) {
                logger.info("404 존재하지 않는 페이지, 유져");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ValueRange valueRange = new ValueRange();
        valueRange.setValues(writeValues);
        try {
            service.spreadsheets().values()
                    .update(spreadsheetId, "users!A2:G", valueRange)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return updatedUsers;
    }

    private List<List<Object>> getValuesBy(String range) {
        try {
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            return response.getValues();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private User generateUserFromRawData(List<Object> rawData) {
        User user = new User();

        user.setBattleTag((String) rawData.get(0));
        user.setCompRank(Integer.parseInt((String) rawData.get(1)));
        user.setTier(Tier.valueOf(((String) rawData.get(2)).toUpperCase()));
        user.setPosition(Position.valueOf(((String) rawData.get(3)).toUpperCase()));
        String[] mostHeroes = ((String) rawData.get(4)).split("\\|");
        user.setMost(new ArrayList<>(Arrays.asList(mostHeroes)));
        user.setGroup((String) rawData.get(5));
        if (rawData.size() > 6) {
            user.setOverlogId((String) rawData.get(6));
        }
        return user;
    }

    private List<Object> writeRawDataFromUser(User user) {
        List<Object> rawData = new ArrayList<>();
        rawData.add(user.getBattleTag());
        rawData.add(user.getCompRank());
        rawData.add(user.getTier().toString());
        rawData.add(user.getPosition().toString());

        String heroes = "";
        for (String hero : user.getMost()) {
            heroes = heroes.concat(hero).concat("|");
        }
        rawData.add(heroes);
        rawData.add(user.getGroup());
        rawData.add(user.getOverlogId());

        return rawData;
    }

    private List<String> getDiscordMembersBts() {
        String range = "bt!A2:D";
        List<List<Object>> values = getValuesBy(range);
        List<String> bts = new ArrayList<>();
        for (List<Object> userData : values) {
            if (userData.size() >= 4) {
                bts.add((String) userData.get(0));
            }
        }

        return bts;
    }

    @Scheduled(fixedRate = 1000 * 60 * 60)
    public List<User> updateSSUsers() {
        String range = "team_samsung_bt!A2:C";
        List<List<Object>> values = getValuesBy(range);
        List<List<Object>> writeValues = new ArrayList<>();
        List<User> updatedUsers = new ArrayList<>();
        for (List<Object> userData : values) {
            logger.info("Update " + userData.get(0));
            try {
                User aUser = POWScraper.getUser(((String) userData.get(0)));
                aUser.setOverlogId((String) userData.get(1));
                aUser.setGroup((String) userData.get(2));
                updatedUsers.add(aUser);
                writeValues.add(writeRawDataFromUser(aUser));
            } catch (HttpStatusException e) {
                logger.info("404 존재하지 않는 페이지, 유져");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ValueRange valueRange = new ValueRange();
        valueRange.setValues(writeValues);
        try {
            service.spreadsheets().values()
                    .update(spreadsheetId, "team_samsung_users!A2:G", valueRange)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return updatedUsers;
    }
}
