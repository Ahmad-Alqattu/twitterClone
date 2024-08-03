package org.example.controllers;

import org.example.dao.TweetDAO;
import org.example.models.Tweet;
import org.example.models.User;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.time.LocalDateTime;
import java.util.Map;

// src/main/java/com/twitterclone/controllers/TweetController.java
public class TweetController {
    private final TweetDAO tweetDAO;

    @Inject
    public TweetController(TweetDAO tweetDAO) {
        this.tweetDAO = tweetDAO;
    }

    public void registerRoutes(Javalin app) {
        app.post("/tweets", this::createTweet);
    }

    private void createTweet(Context ctx) {
        User user = ctx.sessionAttribute("user");
        String content = ctx.formParam("content");

        Tweet tweet = new Tweet();
        tweet.setUserId(user.getId());
        tweet.setContent(content);
        tweet.setCreatedAt(LocalDateTime.now());

        Tweet createdTweet = tweetDAO.create(tweet);
        ctx.render("templates/tweets.pebble", Map.of("tweet", createdTweet));
    }
}