package org.example.controllers;

import io.javalin.http.UploadedFile;
import org.example.models.Tweet;
import org.example.models.User;
import org.example.services.TweetService;
import org.example.services.UserService;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.services.UtilService;

import java.util.List;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public class ProfileController {
    private final UserService userService;
    private final UtilService UtilService;

    @Inject
    public ProfileController(UserService userService, UtilService utilService) {
        this.userService = userService;
        this.UtilService = utilService;
    }

    public void registerRoutes(Javalin app) {
        app.get("/profile/{username}", this::showProfile);
        app.post("/profile/update", this::updateProfile);
        app.post("/profile/{username}/follow", this::followUser);
        app.post("/profile/{username}/unfollow", this::unfollowUser);
        app.get("/profile/{id}/load-more", this::loadTweets);
        app.get("/profile/{id}/profile-pic", this::getProfilePic);
        app.get("/profile/{id}/wallpaper-pic", this::getWallpaperPic);
    }

    private void loadTweets(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int offset = ctx.queryParamAsClass("offset", Integer.class).getOrDefault(0);
        int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(10);
        List<Tweet> tweets = userService.getUserTweets(id, offset, limit);
        Integer currentUserId = ctx.sessionAttribute("userId");

        ctx.render("templates/partials/tweet-list.peb", model(
                "tweets", tweets,
                "hasMoreTweets", tweets.size() == limit,
                "nextOffset", offset + limit,
                "forProfile", true,
                "currentUser", currentUserId,
                "userId",id

        ));
    }

    private void showProfile(Context ctx) {
        String username = ctx.pathParam("username");
        User user = userService.findByUsername(username);
        Integer currentUserId = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.status(404).result("User not found");
            return;
        }

        Map<String, Object> model = (currentUserId == null)
                ? Map.of("user", user)
                : Map.of("user", user, "isFollowing", userService.isFollowing(currentUserId, user.getId()), "currentUser", currentUserId);
        ctx.render("templates/profile.peb", model);
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
        String bio = ctx.formParam("bio");
        byte[] profilePic = (ctx.uploadedFile("profilePic")!=null && ctx.uploadedFile("profilePic").size() > 0 ) ? UtilService.readUploadedFile(ctx.uploadedFile("profilePic")) : null;
        byte[] wallpaperPic = ( ctx.uploadedFile("wallpaperPic")!=null && ctx.uploadedFile("wallpaperPic").size() > 0 ) ? UtilService.readUploadedFile(ctx.uploadedFile("wallpaperPic")) : null;

        userService.updateUserProfile(currentUserId, bio, profilePic, wallpaperPic);
        ctx.redirect("/profile/" + userService.getUserById(Long.valueOf(currentUserId)).getUsername());
    }

    private void getProfilePic(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("id"));
        byte[] imageData = userService.getProfilePicData(userId);
        if (imageData != null) {
            ctx.result(imageData).contentType("image/jpeg");
        } else {
            ctx.status(404).result("Profile picture not found");
        }
    }

    private void getWallpaperPic(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("id"));
        byte[] imageData = userService.getWallpaperPicData(userId);
        if (imageData != null) {
            ctx.result(imageData).contentType("image/jpeg");
        } else {
            ctx.status(404).result("Wallpaper picture not found");
        }
    }
}
