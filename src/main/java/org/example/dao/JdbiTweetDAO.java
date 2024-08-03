package org.example.dao;

import org.example.models.Tweet;
import com.google.inject.Inject;
import org.example.models.User;
import org.jdbi.v3.core.Jdbi;

import java.time.LocalDateTime;
import java.util.List;

// src/main/java/com/twitterclone/dao/JdbiTweetDAO.java
public class JdbiTweetDAO implements TweetDAO {
    private final Jdbi jdbi;

    @Inject
    public JdbiTweetDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public List<Tweet> getTimelineForUser(int userId, int limit, int offset) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT t.*, u.username, u.profile_photo_url, " +
                                "COUNT(DISTINCT l.id) AS like_count, " +
                                "COUNT(DISTINCT r.id) AS retweet_count " +
                                "FROM tweets t " +
                                "JOIN followers f ON t.user_id = f.followee_id " +
                                "JOIN users u ON t.user_id = u.id " +
                                "LEFT JOIN likes l ON t.id = l.tweet_id " +
                                "LEFT JOIN retweets r ON t.id = r.tweet_id " +
                                "WHERE f.follower_id = :userId " +
                                "GROUP BY t.id, u.id " +
                                "ORDER BY t.created_at DESC LIMIT :limit OFFSET :offset")
                        .bind("userId", userId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .map((rs, ctx) -> {
                            Tweet tweet = new Tweet();
                            tweet.setId(rs.getInt("id"));
                            tweet.setUserId(rs.getInt("user_id"));
                            tweet.setContent(rs.getString("content"));
                            tweet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                            tweet.setMediaUrl(rs.getString("media_url"));
                            tweet.setLikeCount(rs.getInt("like_count"));
                            tweet.setRetweetCount(rs.getInt("retweet_count"));

                            User user = new User();
                            user.setId(rs.getInt("user_id"));
                            user.setUsername(rs.getString("username"));
                            user.setProfilePhotoUrl(rs.getString("profile_photo_url"));

                            tweet.setUser(user);
                            return tweet;
                        })
                        .list()
        );
    }


    @Override
    public Tweet create(Tweet tweet) {
        int id = jdbi.withHandle(handle ->
            handle.createUpdate("INSERT INTO tweets (user_id, content, created_at, retweet_id) VALUES (:userId, :content, :createdAt, :retweetId)")
                .bindBean(tweet)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Integer.class)
                .one()
        );
        tweet.setId(id);
        return tweet;
    }

    @Override
    public List<Tweet> getTweetsForUser(int userId, int limit, int offset) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM tweets WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
                .bind("userId", userId)
                .bind("limit", limit)
                .bind("offset", offset)
                .mapToBean(Tweet.class)
                .list()
        );
    }

    @Override
    public Tweet retweet(int userId, int originalTweetId) {
        Tweet originalTweet = jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM tweets WHERE id = :id")
                .bind("id", originalTweetId)
                .mapToBean(Tweet.class)
                .findOne()
                .orElseThrow(() -> new IllegalArgumentException("Tweet not found"))
        );

        Tweet retweet = new Tweet();
        retweet.setUserId(userId);
        retweet.setContent(originalTweet.getContent());
        retweet.setCreatedAt(LocalDateTime.now());

        return create(retweet);
    }

    @Override
    public void like(int userId, int tweetId) {
        jdbi.useTransaction(handle -> {
            handle.createUpdate("INSERT INTO likes (user_id, tweet_id) VALUES (:userId, :tweetId)")
                    .bind("userId", userId)
                    .bind("tweetId", tweetId)
                    .execute();
            handle.createUpdate("UPDATE tweets SET like_count = like_count + 1 WHERE id = :tweetId")
                    .bind("tweetId", tweetId)
                    .execute();
        });
    }

    @Override
    public void unlike(int userId, int tweetId) {
        jdbi.useTransaction(handle -> {
            handle.createUpdate("DELETE FROM likes WHERE user_id = :userId AND tweet_id = :tweetId")
                    .bind("userId", userId)
                    .bind("tweetId", tweetId)
                    .execute();
            handle.createUpdate("UPDATE tweets SET like_count = like_count - 1 WHERE id = :tweetId")
                    .bind("tweetId", tweetId)
                    .execute();
        });
    }

    @Override
    public List<Tweet> searchTweets(String query, int limit) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT t.*, u.username, u.profile_photo_url FROM tweets t " +
                                "JOIN users u ON t.user_id = u.id " +
                                "WHERE t.content LIKE :query " +
                                "ORDER BY t.created_at DESC LIMIT :limit")
                        .bind("query", "%" + query + "%")
                        .bind("limit", limit)
                        .map((rs, ctx) -> {
                            Tweet tweet = new Tweet();
                            tweet.setId(rs.getInt("id"));
                            tweet.setUserId(rs.getInt("user_id"));
                            tweet.setContent(rs.getString("content"));
                            tweet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                            tweet.setMediaUrl(rs.getString("media_url"));

                            User user = new User();
                            user.setId(rs.getInt("user_id"));
                            user.setUsername(rs.getString("username"));
                            user.setProfilePhotoUrl(rs.getString("profile_photo_url"));

                            tweet.setUser(user);
                            return tweet;
                        })
                        .list()
        );
    }


}