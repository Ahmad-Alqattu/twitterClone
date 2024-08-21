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

//    public List<Tweet> getTimelineForUser(int userId, int limit, int offset) {
//        List<Tweet> tweets = getTimelineForUser(userId, limit, offset);
//         tweets.forEach(this::processTweet);
//        return tweets;
//    }

    public List<Tweet> getTimelineForUser(int userId, int limit, int offset) {
        List<Tweet> originalTweets = tweetDAO.getTweetsForUser(userId, limit, offset);
        List<Tweet> retweets = tweetDAO.getRetweetsForUser(userId, limit, offset);

        // Combine the two lists
        List<Tweet> combinedTweets = new ArrayList<>();
        combinedTweets.addAll(originalTweets);
        combinedTweets.addAll(retweets);

        // Sort combined tweets by creation date
        combinedTweets.sort(Comparator.comparing(Tweet::getCreatedAt).reversed());

        // Limit to final size after combining (if necessary)
        if (combinedTweets.size() > limit) {

            combinedTweets = combinedTweets.subList(0, limit);
            combinedTweets.forEach(this::processTweet);

            return combinedTweets;

        } else {
            combinedTweets.forEach(this::processTweet);
            return combinedTweets;
        }

    }

    public Tweet getTweetById(int tweetId, int userId) {
        Tweet tweet = tweetDAO.getTweetById(tweetId, userId);

        return tweet;
    }

    public Tweet processTweet(Tweet tweet) {
        if (tweet.getUser().getProfilePicData() != null) {
            String profilePicBase64 = Base64.getEncoder().encodeToString(tweet.getUser().getProfilePicData());
            tweet.getUser().setProfilePicBase64(profilePicBase64);
        }
        if (tweet.getImageData() != null) {
            String tweetBase64 = Base64.getEncoder().encodeToString(tweet.getImageData());
            tweet.setImageBase64(tweetBase64);
        }
        tweet.getComments().forEach(this::processComment);
        return tweet;
    }

    public Comment processComment(Comment comment) {
        if (comment.getUser().getProfilePicData() != null) {
            String commentProfilePicBase64 = Base64.getEncoder().encodeToString(comment.getUser().getProfilePicData());
            comment.getUser().setProfilePicBase64(commentProfilePicBase64);
        }
        return comment;
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
        Comment c = tweetDAO.addComment(tweetId, userId, content);
        processComment(c);
        return c;
    }

    public List<User> getLikers(int tweetId) {
        return tweetDAO.likers(tweetId);
    }

    public List<User> getRetweeters(int tweetId) {
        return tweetDAO.retweeters(tweetId);
    }
}
