package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class Tweet {
    private int id;
    private int userId;
    private String content;
    private LocalDateTime createdAt;
    private User user;
    private int likeCount;
    private byte[] ImageData;
    private int retweetCount;
    private Boolean hasImage;
    private Boolean likedByMe;
    private Boolean retweetedByMe;
    private List<Comment> Comments;
    private String retweetedByUser ;



    public Tweet() {

    }


}


