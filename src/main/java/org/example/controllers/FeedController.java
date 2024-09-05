package org.example.controllers;

import org.example.models.Tweet;
import org.example.models.User;
import org.example.services.TweetService;
import org.example.services.UserService;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public class FeedController {
    private final TweetService tweetService;
    private final UserService userService;
    private static final int TWEETS_PER_PAGE = 5;

    @Inject
    public FeedController(TweetService tweetService, UserService userService) {
        this.tweetService = tweetService;
        this.userService = userService;
    }

    public void registerRoutes(Javalin app) {
        app.get("/feed", this::renderFeed);
        app.get("/", this::renderFeed);
        app.get("/feed/load-more", this::loadMoreTweets);
    }

    private void renderFeed(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            if (!ctx.path().equalsIgnoreCase("/")) {
                ctx.redirect("/");
            }
            ctx.render("templates/landing.peb");
            return;
        }


        User user = userService.getUserById(Long.valueOf(userId));
        ctx.render("templates/feed.peb", model(
//                "hasMoreTweets", initialTweets.size() == TWEETS_PER_PAGE,
                "nextOffset", 0,
                "user", user,
                "currentUser", userId
        ));
    }

    private void loadMoreTweets(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        Integer   offset = ctx.queryParamAsClass("offset", Integer.class).getOrDefault(0);
        int nextOffset= (offset + TWEETS_PER_PAGE);
        List<Tweet> moreTweets = tweetService.getTimelineForUser(userId, TWEETS_PER_PAGE, offset);
        ctx.render("templates/partials/tweet-list.peb", model(
                "tweets", moreTweets,
                "hasMoreTweets", moreTweets.size() == TWEETS_PER_PAGE,
                "nextOffset",nextOffset,
                "currentUser",userId
        ));
    }
}
