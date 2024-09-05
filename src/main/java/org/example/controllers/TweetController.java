package org.example.controllers;

import io.javalin.http.UploadedFile;
import org.example.models.Comment;
import org.example.models.Tweet;
import org.example.models.User;
import org.example.services.TweetService;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.services.UtilService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public class TweetController {
    private final TweetService tweetService;
    private final UtilService utilService;

    @Inject
    public TweetController(TweetService tweetService,UtilService utilService) {
        this.tweetService = tweetService;
        this.utilService = utilService;
    }

    private void createTweet(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.status(401).result("Not authenticated");
            return;
        }

        String content = ctx.formParam("content");
        byte[] tweetImage = ctx.uploadedFile("image") != null ? utilService.readUploadedFile(ctx.uploadedFile("image")) : null;

        tweetService.createTweet(userId, content, tweetImage);
        ctx.redirect("/feed");
    }

    public void registerRoutes(Javalin app) {
        app.post("/tweet/{id}/like", this::toggleLike);
        app.get("/tweet/{id}/likers", this::getLikers);
        app.post("/tweet/{id}/retweet", this::toggleRetweet);
        app.post("/tweet/{id}/comment", this::addComment);
        app.get("/tweet/{id}/edit", this::editTweetForm);
        app.post("/tweet/{id}/update", this::updateTweet);
        app.post("/tweet/create", this::createTweet);
        app.get("/tweet/{id}/content", this::getTweetContent);
        app.get("/tweet/{id}/confirm-delete", this::confirmDelete);
        app.delete("/tweet/{id}/delete", this::deleteTweet);
        app.get("/tweet/{id}/image", this::getTweetImage);  // New endpoint for images
    }

    private void getTweetContent(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        Tweet tweet = tweetService.getTweetById(tweetId, ctx.sessionAttribute("userId"));

        if (tweet != null) {
            ctx.render("templates/partials/tweet-content.peb", Map.of("tweet", tweet));
        } else {
            ctx.status(404).result("Tweet not found");
        }
    }

    private void editTweetForm(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        Tweet tweet = tweetService.getTweetById(tweetId, ctx.sessionAttribute("userId"));

        if (tweet != null) {
            ctx.render("templates/partials/edit_tweet_form.peb", Map.of("tweet", tweet));
        } else {
            ctx.status(404).result("Tweet not found");
        }
    }

    private void updateTweet(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        String content = ctx.formParam("content");
        UploadedFile imageFile = ctx.uploadedFile("image");


        Tweet tweet = tweetService.getTweetById(tweetId, ctx.sessionAttribute("userId"));

        if (tweet != null) {
            tweet.setContent(content);

            if (imageFile != null && imageFile.size() > 0) {
                byte[] imageData = utilService.readUploadedFile(imageFile);
                tweet.setImageData(imageData);
            }

            if (tweetService.updateTweet(tweet)) {
                ctx.render("templates/partials/tweet-content.peb", Map.of("tweet", tweet));
            } else {
                ctx.status(500).result("Failed to update tweet");
            }
        } else {
            ctx.status(404).result("Tweet not found");
        }
    }

    private void confirmDelete(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        ctx.render("templates/partials/delete_confirmation.peb", Map.of("tweetId", tweetId));
    }

    private void deleteTweet(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        boolean success = tweetService.deleteTweet(tweetId);
        if (success) {
            ctx.status(200).result("");
        } else {
            ctx.status(404).result("Tweet not found");
        }
    }

    private void toggleLike(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        int userId = ctx.sessionAttribute("userId");
        tweetService.likeTweet(userId, tweetId);
        Tweet tweet = tweetService.getTweetById(tweetId, userId);
        ctx.render("templates/partials/like_button.peb", Map.of("tweet", tweet));
    }

    private void getLikers(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        List<User> likers = tweetService.getLikers(tweetId);
        ctx.render("templates/partials/reactors.peb", Map.of("reactors", likers));
    }

    private void toggleRetweet(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        int userId = ctx.sessionAttribute("userId");
        tweetService.retweet(userId, tweetId);
        Tweet tweet = tweetService.getTweetById(tweetId, userId);
        ctx.render("templates/partials/retweet_button.peb", Map.of("tweet", tweet));
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
        ctx.render("templates/partials/comments.peb", Map.of("comment", comment));
    }

    private void getTweetImage(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        byte[] imageData = tweetService.getTweetImageData(tweetId);
        if (imageData != null) {
            ctx.contentType("image/jpeg");
            ctx.result(imageData);
        } else {
            ctx.status(404).result("Image not found");
        }
    }
}
