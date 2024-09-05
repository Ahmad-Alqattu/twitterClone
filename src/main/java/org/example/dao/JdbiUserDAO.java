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
    public byte[] getProfilePicData(int userId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT profile_pic_data FROM users WHERE id = :userId")
                        .bind("userId", userId)
                        .mapTo(byte[].class)
                        .findOnly()
        );
    }

    @Override
    public byte[] getWallpaperPicData(int userId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT wallpaper_pic_data FROM users WHERE id = :userId")
                        .bind("userId", userId)
                        .mapTo(byte[].class)
                        .findOnly()
        );
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
                .orElseThrow(() ->  new IllegalArgumentException("User not found")));
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
    public List<User> searchUsers(String query) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT id, username, email, bio,password_hash, created_at," +
                                "                                        profile_pic_data IS NOT NULL AS has_profile_pic FROM users WHERE username ILIKE :query OR email ILIKE :query")
                        .bind("query", "%" + query + "%")
                        .map((rs, ctx) -> {
                            User user = new User();
                            user.setId(rs.getInt("id"));
                            user.setUsername(rs.getString("username"));
                            user.setEmail(rs.getString("email"));
                            user.setPasswordHash(rs.getString("password_hash"));
                            user.setHasProfilePic(rs.getBoolean("has_profile_pic"));
                            user.setCreatedAt((rs.getTimestamp("created_at")));
                            user.setBio(rs.getString("bio"));

                            return user;
                        })
                            .list()
        );
    }

    @Override
    public User getUserById(Long userId) {
        return jdbi.withHandle(handle ->
                handle.createQuery(
                                "SELECT id, username, email, bio,password_hash, created_at," +
                                        "profile_pic_data ,profile_pic_data IS NOT NULL AS has_profile_pic, " +  // Boolean to check if profile picture exists
                                        "wallpaper_pic_data,wallpaper_pic_data IS NOT NULL AS has_wallpaper " +   // Boolean to check if wallpaper exists
                                        "FROM users WHERE id = :userId")
                        .bind("userId", userId)
                        .map((rs, ctx) -> {
                            User user = new User();
                            user.setId(rs.getInt("id"));
                            user.setUsername(rs.getString("username"));
                            user.setEmail(rs.getString("email"));
                            user.setProfilePicData(rs.getBytes("profile_pic_data"));
                            user.setWallpaperPicData(rs.getBytes("wallpaper_pic_data"));
                            user.setBio(rs.getString("bio"));
                            user.setPasswordHash(rs.getString("password_hash"));
                            user.setCreatedAt(rs.getTimestamp("created_at"));
                            user.setHasProfilePic(rs.getBoolean("has_profile_pic"));  // Set the boolean field
                            user.setHasWallpaper(rs.getBoolean("has_wallpaper"));    // Set the boolean field
                            return user;
                        })
                        .findOnly()
        );
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
    public List<Tweet> getUserTweets(int userId, int offset, int limit) {
        return jdbi.withHandle(handle -> {
            List<Tweet> tweets = handle.createQuery(
                            "SELECT t.id, t.user_id, t.content, t.created_at, " +
                                    "u.username, " +
                                    "t.image_data IS NOT NULL AS has_image, " +  // Boolean to check if image exists
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
                                    "GROUP BY t.id, t.user_id, t.content, t.created_at, u.username " +
                                    "ORDER BY t.created_at DESC, t.id DESC LIMIT :limit OFFSET :offset")
                    .bind("userId", userId)
                    .bind("offset", offset)
                    .bind("limit", limit)
                    .map((rs, ctx) -> {
                        Tweet tweet = new Tweet();
                        tweet.setId(rs.getInt("id"));
                        tweet.setUserId(rs.getInt("user_id"));
                        tweet.setContent(rs.getString("content"));
                        tweet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        tweet.setHasImage(rs.getBoolean("has_image"));  // Set the boolean field
                        tweet.setLikeCount(rs.getInt("like_count"));
                        tweet.setRetweetCount(rs.getInt("retweet_count"));
                        tweet.setLikedByMe(rs.getBoolean("liked_by_me"));
                        tweet.setRetweetedByMe(rs.getBoolean("retweeted_by_me"));
                        tweet.setRetweetedByUser(rs.getString("retweeted_by_user"));

                        User user = new User();
                        user.setId(rs.getInt("user_id"));
                        user.setUsername(rs.getString("username"));
                        tweet.setUser(user);

                        return tweet;
                    })
                    .list();


            for (Tweet tweet : tweets) {
                List<Comment> comments = handle.createQuery(
                                "SELECT c.*, u.username, u.profile_pic_data " +
                                        "IS NOT NULL AS has_profile_pic " +
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
                            user.setHasProfilePic(rs.getBoolean("has_profile_pic"));
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