package org.example.controllers;

import org.example.dao.TweetDAO;
import org.example.models.Tweet;
import org.example.models.User;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

// src/main/java/com/twitterclone/controllers/FeedController.java
public class FeedController {
    private final TweetDAO tweetDAO;

    @Inject
    public FeedController(TweetDAO tweetDAO) {
        this.tweetDAO = tweetDAO;
    }

    public void registerRoutes(Javalin app) {
        app.get("/feed", this::showFeed);
        app.get("/feed/load-more-tweets", this::loadMoreTweets);
    }

    private void showFeed(Context ctx) {
        User user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        List<Tweet> tweets = tweetDAO.getTimelineForUser(user.getId(), 20, 0);
        ctx.render("./templates/tweets.peb", Map.of("tweets", tweets));
    }

    private void loadMoreTweets(Context ctx) {
        User user = ctx.sessionAttribute("user");
        int offset;
        try {
            String offsetParam = ctx.queryParam("offset");
            offset = offsetParam != null ? Integer.parseInt(offsetParam) : 0;
        } catch (NumberFormatException e) {
            // Log the error
            offset = 0; // Use a default value
        }
        List<Tweet> tweets = tweetDAO.getTimelineForUser(user.getId(), 20, offset);
        ctx.render("templates/tweets.peb", Map.of("tweets", tweets));
    }
}