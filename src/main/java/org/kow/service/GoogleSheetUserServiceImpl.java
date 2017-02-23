package org.kow.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.kow.domain.Position;
import org.kow.domain.Tier;
import org.kow.domain.User;
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

    public GoogleSheetUserServiceImpl() throws IOException {
        service = getSheetsService();
    }

    @Override
    public List<User> getUsers() {
        String spreadsheetId = "1D63gLoHAt2l_yaW-F47pkjsqq-82JlGKZ5wXolEj-fI";
        String range = "users!A2:E";
        List<User> users = new ArrayList<>();
        try {
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            logger.info(values.toString());

            for (List<Object> userData : values) {
                users.add(generateUserFromRawData(userData));
            }
        } catch (IOException e) {
            e.printStackTrace();
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
    public User addUser(User aUser) {
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
}
