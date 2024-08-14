package org.example.controllers;

import io.javalin.http.UploadedFile;
import org.example.dao.TweetDAO;
import org.example.dao.UserDAO;
import org.example.models.Tweet;
import org.example.models.User;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLOutput;
import java.text.DateFormat;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public class ProfileController {
    private final UserDAO userDAO;
    private final TweetDAO tweetDAO;

    @Inject
    public ProfileController(UserDAO userDAO, TweetDAO tweetDAO) {
        this.userDAO = userDAO;
        this.tweetDAO = tweetDAO;
    }

    public void registerRoutes(Javalin app) {
        app.get("/profile/{username}", this::showProfile);
        app.post("/profile/update", this::updateProfile);
        app.post("/profile/{username}/follow", this::followUser);
        app.post("/profile/{username}/unfollow", this::unfollowUser);
    }

    private void showProfile(Context ctx) {
        String username = ctx.pathParam("username");
        User user = userDAO.findByUsername(username);
        Integer currentUserId = ctx.sessionAttribute("userId");

        if (user == null) {
            ctx.status(404).result("User not found");
            return;
        }

        List<Tweet> tweets = userDAO.getUserTweets(user.getId());

        // Convert the profile picture and wallpaper to Base64 strings for rendering in HTML
        String profilePicBase64 = user.getProfilePicData() != null ? Base64.getEncoder().encodeToString(user.getProfilePicData()) : null;
        String wallpaperPicBase64 = user.getWallpaperPicData() != null ? Base64.getEncoder().encodeToString(user.getWallpaperPicData()) : null;


        if (currentUserId == null){

            Map<String, Object> model = Map.of(
                    "user", user,
                    "tweets", tweets,
                    "profilePicBase64", profilePicBase64,
                    "wallpaperPicBase64", wallpaperPicBase64
            );
            ctx.render("./templates/profile.peb", model);
        } else {
            Boolean isFollowing = userDAO.isFollowing(currentUserId, user.getId());
            Map<String, Object> model = Map.of(
                    "user", user,
                    "tweets", tweets,
                    "profilePicBase64", profilePicBase64,
                    "wallpaperPicBase64", wallpaperPicBase64,
                    "isFollowing", isFollowing,"currentUser", currentUserId);

            ctx.render("./templates/profile.peb",model);
        }

    }

    private void followUser(Context ctx) {
        Integer currentUserId = ctx.sessionAttribute("userId");
        String username = ctx.pathParam("username");

        User userToFollow = userDAO.findByUsername(username);
        if (userToFollow == null || currentUserId == null) {
            ctx.status(404).result("User not found or not authenticated");
            return;
        }

        userDAO.followUser(currentUserId, userToFollow.getId());
        ctx.redirect("/profile/" + username);
    }
    private void unfollowUser(Context ctx) {
        Integer currentUserId = ctx.sessionAttribute("userId");
        String username = ctx.pathParam("username");

        User userToFollow = userDAO.findByUsername(username);
        if (userToFollow == null || currentUserId == null) {
            ctx.status(404).result("User not found or not authenticated");
            return;
        }

        userDAO.unfollowUser(currentUserId, userToFollow.getId());
        ctx.redirect("/profile/" + username);
    }
    private void updateProfile(Context ctx) {
        Integer currentUserId = ctx.sessionAttribute("userId");
        if (currentUserId == null) {
            ctx.status(401).result("Not authenticated");
            return;
        }

        User currentUser = userDAO.getUserById(Long.valueOf(currentUserId));

        // Form parameters
        String bio = ctx.formParam("bio");
        UploadedFile profilePic = ctx.uploadedFile("profilePic");
        UploadedFile wallpaperPic = ctx.uploadedFile("wallpaperPic");

        // Update bio if present
        if (bio != null) {
            currentUser.setBio(bio);
        }

        // Update profile picture if present
        if (profilePic != null && profilePic.size() > 0) {
            byte[] profilePicData = readUploadedFile(profilePic);
            currentUser.setProfilePicData(profilePicData);  // Assuming you rename the field to store BYTEA data
        }

        // Update wallpaper if present
        if (wallpaperPic != null && wallpaperPic.size() > 0) {
            byte[] wallpaperPicData = readUploadedFile(wallpaperPic);
            currentUser.setWallpaperPicData(wallpaperPicData);  // Assuming you rename the field to store BYTEA data
        }

        // Update the user in the database
        userDAO.updateUserProfile(currentUser);

        // Redirect back to the profile page
        ctx.redirect("/profile/" + currentUser.getUsername());
    }

    // Utility method to read the uploaded file as a byte array
    private byte[] readUploadedFile(UploadedFile uploadedFile) {
        try {
            return uploadedFile.content().readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read file", e);
        }
    }




}
