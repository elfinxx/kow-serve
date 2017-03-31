package org.kow.controller;

import org.kow.domain.Position;
import org.kow.domain.Tier;
import org.kow.domain.User;
import org.kow.service.GoogleSheetUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    GoogleSheetUserServiceImpl googleSheetUserServiceImpl;

    @RequestMapping("/users")
    public List<User> getAllUser() {
        List<User> users = googleSheetUserServiceImpl.getUsers();

        if (users == null) {
            return getDummyUsers();
        } else {
            return users;
        }
    }

    @RequestMapping("/d_users")
    public List<User> getDiscordUsers() {
        List<User> users = googleSheetUserServiceImpl.getDiscordUsers();

        if (users == null) {
            return getDummyUsers();
        } else {
            return users;
        }
    }

    @RequestMapping("/user/{battleTag}")
    public User getUser(@PathVariable("battleTag") String battleTag) {
        return googleSheetUserServiceImpl.getUser(battleTag);
    }

    private List<User> getDummyUsers() {
        List<User> users = new ArrayList<>();

        List<String> most = new ArrayList<>();
        most.add("아나");
        most.add("라인하르트");

        User aUser = new User("바트#31102",
                5000,
                most,
                Tier.MASTER,
                Position.SUPPORT, "Kakao", "aaa");
        users.add(aUser);

        return users;
    }
}
