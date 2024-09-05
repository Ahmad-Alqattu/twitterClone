package org.example.services;

import org.example.dao.UserDAO;
import org.example.models.Tweet;
import org.example.models.User;
import com.google.inject.Inject;
import io.javalin.http.UploadedFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class UserService {
    private final UserDAO userDAO;

    @Inject
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public boolean isFollowing(int currentUserId, int userId) {
        return userDAO.isFollowing(currentUserId, userId);
    }

    public String getProfilePic(int userId) {
        return encodeToBase64(userDAO.getProfilePicData(userId));
    }

    public byte[] getProfilePicData(int userId) {
        return userDAO.getProfilePicData(userId);
    }

    public byte[] getWallpaperPicData(int userId) {
        return userDAO.getWallpaperPicData(userId);
    }

    public void followUser(int followerId, int followedId) {
        userDAO.followUser(followerId, followedId);
    }

    public void unfollowUser(int followerId, int followedId) {
        userDAO.unfollowUser(followerId, followedId);
    }

    public void updateUserProfile(int userId, String bio, byte[] profilePic, byte[] wallpaperPic) {
        User user = userDAO.getUserById((long) userId);

        if (bio != null) {
            user.setBio(bio);
        }
        if (profilePic != null) {
            user.setProfilePicData(profilePic);
        }
        if (wallpaperPic != null) {
            user.setWallpaperPicData(wallpaperPic);
        }

        userDAO.updateUserProfile(user);
    }


    public User SignUp(User user) {
      return   userDAO.create(user);
    }


    public List<Tweet> getUserTweets(int userId, int offset, int limit) {
        return userDAO.getUserTweets(userId, offset, limit);
    }

    public User getUserById(Long userId) {
        return userDAO.getUserById(userId);
    }

    public List<User> searchUsers(String query) {
        return userDAO.searchUsers(query);
    }





    private String encodeToBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
}
