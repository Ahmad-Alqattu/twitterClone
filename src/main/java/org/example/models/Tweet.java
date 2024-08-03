package org.example.models;

// src/main/java/com/twitterclone/models/Tweet.java
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Tweet {
    private int id;
    private int userId;
    private String content;
    private LocalDateTime createdAt;
    private User user; // Associated user object
    private String mediaUrl;
    private int likeCount;
    private int retweetCount;


}


// Update JdbiTweetDAO to implement the retweet method