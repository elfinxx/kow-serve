package org.kow.service;

import org.kow.domain.User;

import java.util.List;

public interface UserService {
    List<User> getUsers();
    User getUser(String battleTag);
    User addUser(User aUser);
    User removeUser(String battleTag);
    User updateUser(User aUser);
}