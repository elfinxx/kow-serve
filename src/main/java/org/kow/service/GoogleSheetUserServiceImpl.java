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
    public List<User> getUsers() {
        String range = "users!A2:E";
        List<User> users = new ArrayList<>();
        for (List<Object> userData : getValuesBy(range)) {
            users.add(generateUserFromRawData(userData));
        }
        return users;
    }

    @Override
    public User getUser(String battleTag) {
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
    public List<User> updateUsers() {
        String range = "bt!A:A";
        List<List<Object>> values = getValuesBy(range);
        List<List<Object>> writeValues = new ArrayList<>();
        List<User> updatedUsers = new ArrayList<>();
        for (List<Object> userData : values) {
            logger.info("Update " + (String) userData.get(0));
            try {
                User aUser = POWScraper.getUser(((String) userData.get(0)));
                updatedUsers.add(aUser);
//                System.out.println(writeRawDataFromUser(aUser));
                writeValues.add(writeRawDataFromUser(aUser));
            } catch (HttpStatusException e) {
              logger.info("404 존재하지 않는 페이지, 유져");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logger.info(writeValues.toString());
        ValueRange valueRange = new ValueRange();
        valueRange.setValues(writeValues);
        try {
            service.spreadsheets().values()
                    .update(spreadsheetId, "update_users!A1:E", valueRange)
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
        String[] mostHeroes = ((String) rawData.get(4)).split("\\s");
        user.setMost(new ArrayList<>(Arrays.asList(mostHeroes)));

        return user;
    }

    private List<Object> writeRawDataFromUser(User user) {
        List<Object> rawData = new ArrayList<>();
        rawData.add(user.getBattleTag());
        rawData.add(user.getCompRank());
        rawData.add(user.getTier().toString());
        rawData.add(user.getPosition().toString());

        String heroes = "";
        for(String hero : user.getMost()) {
            heroes = heroes.concat(hero).concat("|");
        }
        rawData.add(heroes);

        return rawData;
    }
}
