package org.example.dao;

import com.google.inject.Inject;
import org.example.models.Comment;
import org.example.models.Tweet;
import org.example.models.User;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import java.util.List;

public class JdbiTweetDAO implements TweetDAO {
    private final Jdbi jdbi;

    @Inject
    public JdbiTweetDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }


    @Override
    public List<Tweet> getTimelineForUser(int userId, int limit, int offset) {
        return jdbi.withHandle(handle -> {
            // Fetch both original tweets and retweets in a single query
            List<Tweet> tweets = handle.createQuery(
                            "SELECT t.id, t.user_id, t.content, t.image_data, t.created_at, " +
                                    "u.username, u.profile_pic_data, " +
                                    "COUNT(DISTINCT l.id) AS like_count, " +
                                    "COUNT(DISTINCT r.id) AS retweet_count, " +
                                    "EXISTS (SELECT 1 FROM likes WHERE tweet_id = t.id AND user_id = :userId) AS liked_by_me, " +
                                    "EXISTS (SELECT 1 FROM retweets WHERE tweet_id = t.id AND user_id = :userId) AS retweeted_by_me, " +
                                    "(SELECT ru.username FROM users ru JOIN retweets r2 ON r2.user_id = ru.id WHERE r2.tweet_id = t.id LIMIT 1) AS retweeted_by_user " +
                                    "FROM tweets t " +
                                    "JOIN users u ON t.user_id = u.id " +
                                    "LEFT JOIN likes l ON t.id = l.tweet_id " +
                                    "LEFT JOIN retweets r ON t.id = r.tweet_id " +
                                    "WHERE t.user_id = :userId OR t.user_id IN (SELECT followed_id FROM followers WHERE follower_id = :userId) " +
                                    "OR t.id IN (SELECT tweet_id FROM retweets WHERE user_id = :userId) " +
                                    "GROUP BY t.id, t.user_id, t.content, t.image_data, t.created_at, u.username, u.profile_pic_data " +
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

            // Fetch comments for each tweet
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




    @Override
    public Comment addComment(int tweetId, int userId, String content) {
        return jdbi.withHandle(handle -> {
            int id = handle.createUpdate("INSERT INTO comments (tweet_id, user_id, content, created_at) VALUES (:tweetId, :userId, :content, :createdAt)")
                    .bind("tweetId", tweetId)
                    .bind("userId", userId)
                    .bind("content", content)
                    .bind("createdAt", LocalDateTime.now())
                    .executeAndReturnGeneratedKeys("id")
                    .mapTo(Integer.class)
                    .one();

            return handle.createQuery("SELECT c.*, u.username, u.profile_pic_data FROM comments c JOIN users u ON c.user_id = u.id WHERE c.id = :id")
                    .bind("id", id)
                    .map((rs, ctx) -> {
                        User user = new User();
                        user.setId(rs.getInt("user_id"));
                        user.setUsername(rs.getString("username"));
                        user.setProfilePicData(rs.getBytes("profile_pic_data"));
                        Comment comment = new Comment(
                                rs.getInt("id"),
                                rs.getInt("tweet_id"),
                                rs.getInt("user_id"),
                                rs.getString("content"),
                                rs.getTimestamp("created_at").toLocalDateTime(),user
                        );
                        return comment;
                    })
                    .findOne()
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve created comment"));
        });
    }
    @Override
    public Tweet create(Tweet tweet) {
        // Insert the tweet into the database and get the generated ID
        Integer newId = jdbi.withHandle(handle ->
                handle.createUpdate("INSERT INTO tweets (user_id, content, image_data, created_at) VALUES (:userId, :content, :imagedata, :createdAt)")
                        .bind("userId", tweet.getUserId())
                        .bind("content", tweet.getContent())
                        .bind("imagedata", tweet.getImageData())
                        .bind("createdAt", Timestamp.valueOf(tweet.getCreatedAt()))
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(Integer.class)
                        .findOnly()
        );

        // Set the new ID to the tweet and return it
        tweet.setId(newId);
        return tweet;
    }


    @Override
    public boolean retweet(int userId, int originalTweetId) {
        int rowsAffected = jdbi.withHandle(handle -> handle.createUpdate(
                        "INSERT INTO retweets (user_id, tweet_id, created_at) VALUES (:userId, :tweetId, :createdAt)")
                .bind("userId", userId)
                .bind("tweetId", originalTweetId)
                .bind("createdAt", Timestamp.valueOf(LocalDateTime.now()))
                .execute());

        return rowsAffected > 0;
    }



    @Override
    public boolean like(int userId, int tweetId) {
        try {
            jdbi.useTransaction(handle -> {
                handle.createUpdate("INSERT INTO likes (user_id, tweet_id) VALUES (:userId, :tweetId)").bind("userId", userId).bind("tweetId", tweetId).execute();

            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean unlike(int userId, int tweetId) {
        try {
            jdbi.useTransaction(handle -> {
                handle.createUpdate("DELETE FROM likes WHERE user_id = :userId AND tweet_id = :tweetId").bind("userId", userId).bind("tweetId", tweetId).execute();

            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<User> likers(int tweetId) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT u.id, u.username, u.email, u.profile_pic_data " + "FROM users u " + "JOIN likes l ON u.id = l.user_id " + "WHERE l.tweet_id = :tweetId "

        ).bind("tweetId", tweetId).map((rs, ctx) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setProfilePicData(rs.getBytes("profile_pic_data"));
            return user;
        }).list());
    }

    @Override
    public List<User> retweeters(int tweetId) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT u.id, u.username, u.email, u.profile_pic_data " + "FROM users u " + "JOIN retweets r ON u.id = r.user_id " + "WHERE r.tweet_id = :tweetId "

        ).bind("tweetId", tweetId).map((rs, ctx) -> {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setProfilePicData(rs.getBytes("profile_pic_data"));
                    return user;
                }

        ).list());
    }

    @Override
    public Tweet getTweetById(int tweetId, int userId) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT t.id, t.content, t.created_at, t.image_data, " + "u.id AS user_id, u.username, u.profile_pic_data, u.email, " + "(SELECT COUNT(*) FROM likes WHERE tweet_id = t.id) AS like_count, " + "(SELECT COUNT(*) FROM retweets WHERE tweet_id = t.id) AS retweet_count, " + "EXISTS (SELECT 1 FROM likes WHERE tweet_id = t.id AND user_id = :userId) AS liked_by_me, " + "EXISTS (SELECT 1 FROM retweets WHERE tweet_id = t.id AND user_id = :userId) AS retweeted_by_me " + "FROM tweets t, users u " + // Added space after "users"
                        "WHERE t.user_id = u.id AND t.id = :tweetId" // Added space before "AND"
                ).bind("tweetId", tweetId).bind("userId", userId)
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
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setProfilePicData(rs.getBytes("profile_pic_data"));

                    tweet.setUser(user);
                    return tweet;
                }).findFirst().orElse(null));
    }

    @Override
    public List<Tweet> searchTweets(String query, int limit) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT t.*, u.username, u.profile_photo_data FROM tweets t " + "JOIN users u ON t.user_id = u.id " + "WHERE t.content LIKE :query " + "ORDER BY t.created_at DESC LIMIT :limit").bind("query", "%" + query + "%").bind("limit", limit).map((rs, ctx) -> {
            Tweet tweet = new Tweet();
            tweet.setId(rs.getInt("id"));
            tweet.setUserId(rs.getInt("user_id"));
            tweet.setContent(rs.getString("content"));
            tweet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            tweet.setImageData(rs.getBytes("image_data"));
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setProfilePicData(rs.getBytes("profile_pic_data"));

            tweet.setUser(user);
            return tweet;
        }).list());
    }

    @Override
    @SqlQuery("SELECT EXISTS (SELECT 1 FROM likes WHERE tweet_id = :tweetId AND user_id = :userId) AS liked_by_me")
    public boolean isLikedByMe(@Bind("tweetId") int tweetId, @Bind("userId") int userId) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT EXISTS (SELECT 1 FROM likes WHERE tweet_id = :tweetId AND user_id = :userId)").bind("tweetId", tweetId).bind("userId", userId).mapTo(Boolean.class).one());
    }

    @Override
    @SqlQuery("SELECT EXISTS (SELECT 1 FROM retweets WHERE tweet_id = :tweetId AND user_id = :userId) AS retweeted_by_me")
    public boolean isRetweetedByMe(@Bind("tweetId") int tweetId, @Bind("userId") int userId) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT EXISTS (SELECT 1 FROM retweets WHERE tweet_id = :tweetId AND user_id = :userId)").bind("tweetId", tweetId).bind("userId", userId).mapTo(Boolean.class).one());
    }

    @Override
    @SqlUpdate("DELETE FROM retweets WHERE user_id = :userId AND tweet_id = :tweetId")
    public boolean unretweet(@Bind("userId") int userId, @Bind("tweetId") int tweetId) {
        return jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM retweets WHERE user_id = :userId AND tweet_id = :tweetId").bind("userId", userId).bind("tweetId", tweetId).execute()) > 0;
    }

    @Override
    @SqlUpdate("DELETE FROM tweets WHERE id = :tweetId")
    public boolean deleteTweet( @Bind("tweetId") int tweetId) {
        return jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM tweets WHERE id = :tweetId").bind("tweetId", tweetId).execute()) > 0;
    }
}