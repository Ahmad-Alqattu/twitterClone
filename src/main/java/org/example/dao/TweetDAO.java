package org.example.dao;

import org.example.models.Tweet;

import java.util.List;

public interface TweetDAO {
    List<Tweet> getTimelineForUser(int userId, int limit, int offset);
    Tweet create(Tweet tweet);
    List<Tweet> getTweetsForUser(int userId, int limit, int offset);
    Tweet retweet(int userId, int originalTweetId);
    // Add more methods as needed
    void like(int userId, int tweetId);
    void unlike(int userId, int tweetId);
    List<Tweet> searchTweets(String query, int limit);
}