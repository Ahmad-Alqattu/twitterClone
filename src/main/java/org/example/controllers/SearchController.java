package org.example.controllers;

import org.example.models.User;
import org.example.services.UserService;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

public class SearchController {
    private final UserService userService;

    @Inject
    public SearchController(UserService userService) {
        this.userService = userService;
    }

    public void registerRoutes(Javalin app) {
        app.get("/search", this::searchUsers);
    }

    private void searchUsers(Context ctx) {
        String query = ctx.queryParam("search");
        List<User> users = userService.searchUsers(query);

        if (users.isEmpty()) {
            ctx.html("<div class='dropdown-item'>No users found</div>");
        } else {
            ctx.render("templates/partials/search_results.peb", Map.of("users", users));
        }
    }
}
