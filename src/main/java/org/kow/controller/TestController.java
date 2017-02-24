package org.kow.controller;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.kow.domain.User;
import org.kow.service.UserService;
import org.kow.util.POWScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static org.kow.util.GoogleSheetHelper.getSheetsService;

@RestController
@RequestMapping("/api")
public class TestController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @RequestMapping(value = "/test")
    public String test() throws IOException {
        // Build a new authorized API client service.
        Sheets service = getSheetsService();

        String spreadsheetId = "1D63gLoHAt2l_yaW-F47pkjsqq-82JlGKZ5wXolEj-fI";
        String range = "test!A2:E";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        logger.info(values.toString());
        return "HELL";
    }

    @RequestMapping(value = "/test2")
    public User test2() throws IOException {
        User user = POWScraper.getUser("바트-31102");

        return user;
    }

    @RequestMapping(value = "/update")
    public List<User> updateUsers() throws IOException {
        List<User> updatedUsers= userService.updateUsers();
        return updatedUsers;
    }
}
