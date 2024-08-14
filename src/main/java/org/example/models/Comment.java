package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Comment {
    private int id;
    private int tweetId;
    private int userId;
    private String content;
    private LocalDateTime createdAt;
    private User user;

    public Comment() {

    }
}