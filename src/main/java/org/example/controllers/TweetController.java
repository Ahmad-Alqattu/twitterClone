package org.example.controllers;

import io.javalin.http.UploadedFile;
import org.example.models.Comment;
import org.example.models.Tweet;
import org.example.models.User;
import org.example.services.TweetService;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.services.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TweetController {
    private final TweetService tweetService;

    @Inject
    public TweetController(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    public void registerRoutes(Javalin app) {
        app.post("/tweet/{id}/like", this::toggleLike);
        app.get("/tweet/{id}/likers", this::getLikers);
        app.post("/tweet/{id}/retweet", this::toggleRetweet);
        app.get("/tweet/{id}/retweeters", this::getRetweeters);
        app.post("/tweet/{id}/comment", this::addComment);
        app.post("/tweet/create", this::createTweet);
        app.post("/tweet/{id}/delete", this::deleteTweet);
    }

    private void toggleLike(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        int userId = ctx.sessionAttribute("userId");
        tweetService.likeTweet(userId, tweetId);
        Tweet tweet = tweetService.getTweetById(tweetId, userId);
        ctx.render("./templates/partials/like_button.peb", Map.of("tweet", tweet));
    }

    private void deleteTweet(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        boolean success = tweetService.deleteTweet(tweetId);

        if (success) {
            ctx.status(204);  // No Content response if successful
        } else {
            ctx.status(404).result("Tweet not found");
        }
    }

    private void getLikers(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        List<User> likers = tweetService.getLikers(tweetId);
        ctx.render("./templates/partials/reactors.peb", Map.of("reactors", likers));
    }

    private void toggleRetweet(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        int userId = ctx.sessionAttribute("userId");
        tweetService.retweet(userId, tweetId);
        Tweet tweet = tweetService.getTweetById(tweetId, userId);
        ctx.render("./templates/partials/retweet_button.peb", Map.of("tweet", tweet));
    }

    private void addComment(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        String content = ctx.formParam("content");
        Integer userId = ctx.sessionAttribute("userId");

        if (userId == null) {
            ctx.status(401).result("User not authenticated");
            return;
        }

        Comment comment = tweetService.addComment(tweetId, userId, content);
        ctx.render("./templates/partials/comments.peb", Map.of("comment", comment));
    }



    private void getRetweeters(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        List<User> retweeters = tweetService.getRetweeters(tweetId);
        ctx.render("templates/partials/reactors.peb", Map.of("reactors", retweeters));
    }

    private void createTweet(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.status(401).result("Not authenticated");
            return;
        }

        String content = ctx.formParam("content");
        byte[] tweetImage = ctx.uploadedFile("image") != null ? readUploadedFile(ctx.uploadedFile("image")) : null;

        tweetService.createTweet(userId, content, tweetImage);
        ctx.redirect("/feed");
    }

    private static byte[] readUploadedFile(UploadedFile uploadedFile) {
        try {
            return uploadedFile.content().readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read file", e);
        }
    }
}
