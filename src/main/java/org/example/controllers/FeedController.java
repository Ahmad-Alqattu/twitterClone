package org.example.controllers;

import org.example.dao.TweetDAO;
import org.example.models.Tweet;
import org.example.models.User;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;


public class FeedController {
    private final TweetDAO tweetDAO;
    private static final int TWEETS_PER_PAGE = 5;

    @Inject
    public FeedController(TweetDAO tweetDAO) {
        this.tweetDAO = tweetDAO;
    }

    public void registerRoutes(Javalin app) {
        app.get("/feed", this::renderFeed);
        app.get("/", this::renderFeed);
  app.get("/feed/load-more", this::loadMoreTweets);
    }

    private void renderFeed(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }
        List<Tweet> initialTweets = tweetDAO.getTimelineForUser(userId, TWEETS_PER_PAGE, 0);
        System.out.println(initialTweets.size());
        ctx.render("templates/feed.peb", model(
                "tweets", initialTweets,
                "hasMoreTweets", initialTweets.size() == TWEETS_PER_PAGE
        ));
    }

    private void loadMoreTweets(Context ctx) {
        int userId = ctx.sessionAttribute("userId");
        int offset = ctx.queryParamAsClass("offset", Integer.class).getOrDefault(0);

        List<Tweet> moreTweets = tweetDAO.getTimelineForUser(userId, TWEETS_PER_PAGE, offset);

        ctx.render("templates/partials/tweet-list.peb", model(
                "tweets", moreTweets,
                "hasMoreTweets", moreTweets.size() == TWEETS_PER_PAGE,
                "nextOffset", offset + TWEETS_PER_PAGE
        ));
    }
}