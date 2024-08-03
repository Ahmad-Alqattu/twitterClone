package org.example.dao;

import org.example.models.User;
import com.google.inject.Inject;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

// src/main/java/com/twitterclone/dao/JdbiUserDAO.java
public class JdbiUserDAO implements UserDAO {
    private final Jdbi jdbi;

    @Inject
    public JdbiUserDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public User findByUsername(String username) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM users WHERE username = :username")
                .bind("username", username)
                .mapToBean(User.class)
                .findOne()
                .orElse(null)
        );
    }

    @Override
    public User create(User user) {
        int id = jdbi.withHandle(handle ->
            handle.createUpdate("INSERT INTO users (username, email, password_hash) VALUES (:username, :email, :passwordHash)")
                .bindBean(user)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Integer.class)
                .one()
        );
        user.setId(id);
        return user;
    }

    @Override
    public List<User> searchUsers(String query, int limit) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM users WHERE username LIKE :query OR email LIKE :query LIMIT :limit")
                .bind("query", "%" + query + "%")
                .bind("limit", limit)
                .mapToBean(User.class)
                .list()
        );
    }
    @Override
    public User getUserById(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM users WHERE id = :id")
                        .bind("id", id)
                        .mapToBean(User.class)
                        .one());
    }
}