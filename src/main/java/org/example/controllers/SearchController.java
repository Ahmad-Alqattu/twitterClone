package org.example.controllers;

import org.example.dao.UserDAO;
import org.example.models.User;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

// src/main/java/com/twitterclone/controllers/SearchController.java
public class SearchController {
    private final UserDAO userDAO;

    @Inject
    public SearchController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void registerRoutes(Javalin app) {
        app.get("/search", this::searchUsers);
    }

    private void searchUsers(Context ctx) {
        String query = ctx.queryParam("q");
        List<User> users = userDAO.searchUsers(query, 10);
        ctx.render("partials/user-list.pebble", Map.of("users", users));
    }
}