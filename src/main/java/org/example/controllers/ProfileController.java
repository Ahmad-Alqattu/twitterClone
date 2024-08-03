package org.example.controllers;

import org.example.dao.TweetDAO;
import org.example.dao.UserDAO;
import org.example.models.Tweet;
import org.example.models.User;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

// src/main/java/com/twitterclone/controllers/ProfileController.java
public class ProfileController {
    private final UserDAO userDAO;
    private final TweetDAO tweetDAO;

    @Inject
    public ProfileController(UserDAO userDAO, TweetDAO tweetDAO) {
        this.userDAO = userDAO;
        this.tweetDAO = tweetDAO;
    }

    public void registerRoutes(Javalin app) {

        app.get("/user/{username}", this::showProfile);
    }

    private void showProfile(Context ctx) {
        String username = ctx.pathParam("username");
        User user = userDAO.findByUsername(username);
        if (user == null) {
            ctx.status(404);
            return;
        }

        List<Tweet> tweets = tweetDAO.getTweetsForUser(user.getId(), 20, 0);
        ctx.render("profile.peb", Map.of("user", user, "tweets", tweets));
    }
}