package org.example.dao;

import org.example.models.Comment;
import org.example.models.Tweet;
import org.example.models.User;

import java.util.List;

public interface TweetDAO {
    List<Tweet> getTimelineForUser(int userId, int limit, int offset);

    Comment addComment(int tweetId, int userId, String content);

    Tweet create(Tweet tweet);
    boolean retweet(int userId, int originalTweetId);
    // Add more methods as needed
    boolean like(int userId, int tweetId);
    boolean unlike(int userId, int tweetId);

    Tweet getTweetById(int tweetId, int userId);

    List<Tweet> searchTweets(String query, int limit);
    List<User> likers(int tweetId) ;
    List<User> retweeters(int tweetId);


    boolean isLikedByMe(int tweetId, int userId);

    boolean isRetweetedByMe(int tweetId,  int userId);

    boolean unretweet(int userId,int tweetId);
}