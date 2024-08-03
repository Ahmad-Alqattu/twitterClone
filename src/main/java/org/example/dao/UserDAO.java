package org.example.dao;

import org.example.models.User;

import java.util.List;

// src/main/java/com/twitterclone/dao/UserDAO.java
public interface UserDAO {
    User findByUsername(String username);
    User create(User user);
    List<User> searchUsers(String query, int limit);
    User getUserById(Long id);
}