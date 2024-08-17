package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class Tweet {
    public String imageBase64;
    private int id;
    private int userId;
    private String content;
    private LocalDateTime createdAt;
    private User user;
    private int likeCount;
    private int retweetCount;
    private byte[] ImageData;
    private Boolean likedByMe;
    private Boolean retweetedByMe;
    private List<Comment> Comments;
    private String retweetedByUser;



    public Tweet() {

    }


}


// Update JdbiTweetDAO to implement the retweet method