package org.example.services;

import org.example.dao.UserDAO;
import org.example.models.Comment;
import org.example.models.Tweet;
import org.example.models.User;
import com.google.inject.Inject;

import java.util.Base64;
import java.util.List;

public class UserService {
    private final UserDAO userDAO;

    @Inject
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User findByUsername(String username) {
        return  processUser(userDAO.findByUsername(username));
    }

    public boolean isFollowing(int currentUserId, int userId) {
        return userDAO.isFollowing(currentUserId, userId);
    }

    public void followUser(int followerId, int followedId) {
        userDAO.followUser(followerId, followedId);
    }

    public void unfollowUser(int followerId, int followedId) {
        userDAO.unfollowUser(followerId, followedId);
    }

    public void updateUserProfile(User user) {
        userDAO.updateUserProfile(user);
    }
    public List<Tweet> getUserTweets(int userId){
        return userDAO.getUserTweets(userId);
    }
    public User getUserById(Long userId) {
        User user = userDAO.getUserById(userId);
        if (user.getProfilePicData() != null) {
            user.setProfilePicBase64(Base64.getEncoder().encodeToString(user.getProfilePicData()));
        }
        if (user.getWallpaperPicData() != null) {
            user.setWallpaperPicBase64(Base64.getEncoder().encodeToString(user.getWallpaperPicData()));
        }
        return user;
    }

    public List<User> searchUsers(String query) {
        List<User> users = userDAO.searchUsers(query);
        users.forEach(user -> {
            if (user.getProfilePicData() != null) {
                user.setProfilePicBase64(Base64.getEncoder().encodeToString(user.getProfilePicData()));
            }
        });
        return users;
    }
    public User processUser(User user) {
        if (user.getProfilePicData() != null) {
            String commentProfilePicBase64 = Base64.getEncoder().encodeToString(user.getProfilePicData());
            user.setProfilePicBase64(commentProfilePicBase64);
        }
        if (user.getWallpaperPicData() != null) {
            String WallpaperPicData = Base64.getEncoder().encodeToString(user.getWallpaperPicData());
            user.setWallpaperPicBase64(WallpaperPicData);
        }
        return user;
    }

}
