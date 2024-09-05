package org.example.services;

import com.google.inject.Inject;
import org.example.dao.TweetDAO;
import org.example.models.Comment;
import org.example.models.Tweet;
import org.example.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

public class TweetService {
    private final TweetDAO tweetDAO;

    @Inject
    public TweetService(TweetDAO tweetDAO) {
        this.tweetDAO = tweetDAO;
    }

    public List<Tweet> getTimelineForUser(int userId, int limit, int offset) {
        List<Tweet> originalTweets = tweetDAO.getTweetsForUser(userId, limit, offset);
        List<Tweet> retweets = tweetDAO.getRetweetsForUser(userId, limit, offset);

        List<Tweet> combinedTweets = new ArrayList<>();
        combinedTweets.addAll(originalTweets);
        combinedTweets.addAll(retweets);

        combinedTweets.sort(Comparator.comparing(Tweet::getCreatedAt).reversed());

        if (combinedTweets.size() > limit) {
            combinedTweets = combinedTweets.subList(0, limit);
        }

        return combinedTweets;
    }

    public Tweet getTweetById(int tweetId, int userId) {
        return tweetDAO.getTweetById(tweetId, userId);
    }

    public byte[] getTweetImageData(int tweetId) {
        return tweetDAO.getTweetImagData(tweetId);
    }

    public Tweet createTweet(int userId, String content, byte[] imageData) {
        Tweet tweet = new Tweet();
        tweet.setUserId(userId);
        tweet.setContent(content);
        tweet.setCreatedAt(LocalDateTime.now());
        if (imageData != null) {
            tweet.setImageData(imageData);
        }
        tweetDAO.create(tweet);
        return tweet;
    }

    public boolean deleteTweet(int tweetId) {
        return tweetDAO.deleteTweet(tweetId);
    }

    public void likeTweet(int userId, int tweetId) {
        if (tweetDAO.isLikedByMe(tweetId, userId)) {
            tweetDAO.unlike(userId, tweetId);
        } else {
            tweetDAO.like(userId, tweetId);
        }
    }

    public void retweet(int userId, int tweetId) {
        if (tweetDAO.isRetweetedByMe(tweetId, userId)) {
            tweetDAO.unretweet(userId, tweetId);
        } else {
            tweetDAO.retweet(userId, tweetId);
        }
    }

    public Comment addComment(int tweetId, int userId, String content) {
        return tweetDAO.addComment(tweetId, userId, content);
    }

    public List<User> getLikers(int tweetId) {
        return tweetDAO.likers(tweetId);
    }

    public boolean updateTweet(Tweet tweet) {
        return tweetDAO.updateTweet(tweet);
    }
}
