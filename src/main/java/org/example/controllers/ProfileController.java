package org.example.controllers;

import io.javalin.http.UploadedFile;
import org.example.models.Tweet;
import org.example.models.User;
import org.example.services.TweetService;
import org.example.services.UserService;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProfileController {
    private final UserService userService;
    private final TweetService tweetService;

    @Inject
    public ProfileController(UserService userService, TweetService tweetService) {
        this.userService = userService;
        this.tweetService = tweetService;
    }

    public void registerRoutes(Javalin app) {
        app.get("/profile/{username}", this::showProfile);
        app.post("/profile/update", this::updateProfile);
        app.post("/profile/{username}/follow", this::followUser);
        app.post("/profile/{username}/unfollow", this::unfollowUser);
    }

    private void showProfile(Context ctx) {
        String username = ctx.pathParam("username");
        User user = userService.findByUsername(username);
        Integer currentUserId = ctx.sessionAttribute("userId");

        if (user == null) {
            ctx.status(404).result("User not found");
            return;
        }

        List<Tweet> tweets = userService.getUserTweets(user.getId());
        tweets.forEach(tweetService::processTweet);

        Map<String, Object> model;
        if (currentUserId == null) {
            model = Map.of("user", user, "tweets", tweets);
        } else {
            Boolean isFollowing = userService.isFollowing(currentUserId, user.getId());
            model = Map.of("user", user, "tweets", tweets, "isFollowing", isFollowing, "currentUser", currentUserId);
        }
        ctx.render("./templates/profile.peb", model);
    }

    private void followUser(Context ctx) {
        Integer currentUserId = ctx.sessionAttribute("userId");
        String username = ctx.pathParam("username");

        User userToFollow = userService.findByUsername(username);
        if (userToFollow == null || currentUserId == null) {
            ctx.status(404).result("User not found or not authenticated");
            return;
        }

        userService.followUser(currentUserId, userToFollow.getId());
        ctx.redirect("/profile/" + username);
    }

    private void unfollowUser(Context ctx) {
        Integer currentUserId = ctx.sessionAttribute("userId");
        String username = ctx.pathParam("username");

        User userToFollow = userService.findByUsername(username);
        if (userToFollow == null || currentUserId == null) {
            ctx.status(404).result("User not found or not authenticated");
            return;
        }

        userService.unfollowUser(currentUserId, userToFollow.getId());
        ctx.redirect("/profile/" + username);
    }

    private void updateProfile(Context ctx) {
        Integer currentUserId = ctx.sessionAttribute("userId");
        if (currentUserId == null) {
            ctx.status(401).result("Not authenticated");
            return;
        }

        User currentUser = userService.getUserById(Long.valueOf(currentUserId));
        String bio = ctx.formParam("bio");
        byte[] profilePic = ctx.uploadedFile("profilePic") != null ? readUploadedFile(ctx.uploadedFile("profilePic")) : null;
        byte[] wallpaperPic = ctx.uploadedFile("wallpaperPic") != null ? readUploadedFile(ctx.uploadedFile("wallpaperPic")) : null;

        if (bio != null) {
            currentUser.setBio(bio);
        }

        if (profilePic != null) {
            currentUser.setProfilePicData(profilePic);
        }


        if (wallpaperPic != null) {
            currentUser.setWallpaperPicData(wallpaperPic);
        }

        userService.updateUserProfile(currentUser);
        ctx.redirect("/profile/" + currentUser.getUsername());
    }

    private byte[] readUploadedFile(UploadedFile uploadedFile) {
        try {
            return uploadedFile.content().readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read file", e);
        }
    }
}
