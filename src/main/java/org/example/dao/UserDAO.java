package org.example.dao;

import org.example.models.Tweet;
import org.example.models.User;

import java.util.List;

public interface UserDAO {
    boolean isFollowing(int followerId, int followedId);

    void followUser(int followerId, int followedId);

    void unfollowUser(int followerId, int followedId);

    User findByUsername(String username);
    User create(User user);
    List<User> searchUsers(String query);
    User getUserById(Long id);
    void updateUserProfile(User user);
    List<Tweet> getUserTweets(int userId);
}