package org.kow.service;

import org.kow.domain.User;

import java.util.List;

public interface UserService {
    List<User> getUsers();
    List<User> getDiscordUsers();
    User getUser(String battleTag);
    User addUser(String battleTag);
    User removeUser(String battleTag);
    User updateUser(User aUser);

    List<User> updateUsers();
}
