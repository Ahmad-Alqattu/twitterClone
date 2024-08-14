package org.example.dao;

import org.example.models.Comment;
import org.example.models.Tweet;
import org.example.models.User;
import com.google.inject.Inject;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

// src/main/java/com/twitterclone/dao/JdbiUserDAO.java
public class JdbiUserDAO implements UserDAO {
    private final Jdbi jdbi;

    @Inject
    public JdbiUserDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public boolean isFollowing(int followerId, int followedId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM followers WHERE follower_id = :followerId AND followed_id = :followedId")
                        .bind("followerId", followerId)
                        .bind("followedId", followedId)
                        .mapTo(Boolean.class)
                        .one()
        );
    }
    @Override
    public void followUser(int followerId, int followedId) {
        jdbi.useHandle(handle ->
                handle.createUpdate("INSERT INTO followers (follower_id, followed_id) VALUES (:followerId, :followedId)")
                        .bind("followerId", followerId)
                        .bind("followedId", followedId)
                        .execute()
        );
    }
    @Override
    public void unfollowUser(int followerId, int followedId) {
        jdbi.useHandle(handle ->
                handle.createUpdate("DELETE FROM followers WHERE follower_id = :followerId AND followed_id = :followedId")
                        .bind("followerId", followerId)
                        .bind("followedId", followedId)
                        .execute()
        );
    }

        @Override
    public User findByUsername(String username) {
        return jdbi.withHandle(handle -> handle.createQuery(
                        "SELECT u.id, u.username, u.bio, u.email, u.password_hash, u.profile_pic_data, u.wallpaper_pic_data, u.created_at, " +
                                "(SELECT COUNT(*) FROM followers WHERE followed_id = u.id) AS followersCount, " +
                                "(SELECT COUNT(*) FROM followers WHERE follower_id = u.id) AS followingCount " +
                                "FROM users u WHERE username = :username")
                .bind("username", username)
                .map((rs, ctx) -> {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setProfilePicData(rs.getBytes("profile_pic_data"));
                    user.setWallpaperPicData(rs.getBytes("wallpaper_pic_data"));
                    user.setCreatedAt((rs.getTimestamp("created_at")));
                    user.setBio(rs.getString("bio"));
                    user.setFollowersCount(rs.getInt("followersCount"));
                    user.setFollowingCount(rs.getInt("followingCount"));
                    return user;
                })
                .findOne()
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }

    @Override
    public User create(User user) {
        int id = jdbi.withHandle(handle ->
            handle.createUpdate("INSERT INTO users (username, email, password_hash) VALUES (:username, :email, :passwordHash)")
                .bindBean(user)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Integer.class)
                .one()
        );
        user.setId(id);
        return user;
    }

    @Override
    public List<User> searchUsers(String query, int limit) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM users WHERE username LIKE :query OR email LIKE :query LIMIT :limit")
                .bind("query", "%" + query + "%")
                .bind("limit", limit)
                .mapToBean(User.class)
                .list()
        );
    }
    @Override
    public User getUserById(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM users WHERE id = :id")
                        .bind("id", id)
                        .mapToBean(User.class)
                        .one());
    }



    @Override
    public void updateUserProfile(User user) {
        jdbi.useHandle(handle ->
                handle.createUpdate("UPDATE users SET bio = :bio, profile_pic_data = :profilePicData, wallpaper_pic_data = :wallpaperPicData WHERE id = :id")
                        .bind("bio", user.getBio())
                        .bind("profilePicData", user.getProfilePicData())
                        .bind("wallpaperPicData", user.getWallpaperPicData())
                        .bind("id", user.getId())
                        .execute()
        );
    }

    @Override
    public List<Tweet> getUserTweets(int userId) {
        return jdbi.withHandle(handle -> {
            List<Tweet> tweets = handle.createQuery(
                            "SELECT t.id, t.user_id, t.content, t.image_data, t.created_at, " +
                                    "u.username, u.profile_pic_data, " +
                                    "COUNT(DISTINCT l.id) AS like_count, " +
                                    "COUNT(DISTINCT r.id) AS retweet_count, " +
                                    "EXISTS (SELECT 1 FROM likes WHERE tweet_id = t.id AND user_id = :userId) AS liked_by_me, " +
                                    "EXISTS (SELECT 1 FROM retweets WHERE tweet_id = t.id AND user_id = :userId) AS retweeted_by_me, " +
                                    "COALESCE((SELECT username FROM users WHERE id = (SELECT user_id FROM retweets WHERE tweet_id = t.id AND user_id = :userId LIMIT 1)), NULL) AS retweeted_by_user " +
                                    "FROM tweets t " +
                                    "JOIN users u ON t.user_id = u.id " +
                                    "LEFT JOIN likes l ON t.id = l.tweet_id " +
                                    "LEFT JOIN retweets r ON t.id = r.tweet_id " +
                                    "WHERE t.user_id = :userId OR t.id IN (SELECT tweet_id FROM retweets WHERE user_id = :userId) " +
                                    "GROUP BY t.id, t.user_id, t.content, t.image_data, t.created_at, u.username, u.profile_pic_data " +
                                    "ORDER BY t.created_at DESC")
                    .bind("userId", userId)
                    .map((rs, ctx) -> {
                        Tweet tweet = new Tweet();
                        tweet.setId(rs.getInt("id"));
                        tweet.setUserId(rs.getInt("user_id"));
                        tweet.setContent(rs.getString("content"));
                        tweet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        tweet.setImageData(rs.getBytes("image_data"));
                        tweet.setLikeCount(rs.getInt("like_count"));
                        tweet.setRetweetCount(rs.getInt("retweet_count"));
                        tweet.setLikedByMe(rs.getBoolean("liked_by_me"));
                        tweet.setRetweetedByMe(rs.getBoolean("retweeted_by_me"));
                        tweet.setRetweetedByUser(rs.getString("retweeted_by_user"));

                        User user = new User();
                        user.setId(rs.getInt("user_id"));
                        user.setUsername(rs.getString("username"));
                        user.setProfilePicData(rs.getBytes("profile_pic_data"));
                        tweet.setUser(user);

                        return tweet;
                    })
                    .list();

            for (Tweet tweet : tweets) {
                List<Comment> comments = handle.createQuery(
                                "SELECT c.*, u.username, u.profile_pic_data " +
                                        "FROM comments c " +
                                        "JOIN users u ON c.user_id = u.id " +
                                        "WHERE c.tweet_id = :tweetId " +
                                        "ORDER BY c.created_at ASC")
                        .bind("tweetId", tweet.getId())
                        .map((rs, ctx) -> {
                            Comment comment = new Comment();
                            comment.setId(rs.getInt("id"));
                            comment.setContent(rs.getString("content"));
                            comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                            User user = new User();
                            user.setId(rs.getInt("user_id"));
                            user.setUsername(rs.getString("username"));
                            user.setProfilePicData(rs.getBytes("profile_pic_data"));
                            comment.setUser(user);

                            return comment;
                        })
                        .list();

                tweet.setComments(comments);
            }

            return tweets;
        });
    }

}