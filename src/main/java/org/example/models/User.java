package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Optional;

// src/main/java/com/twitterclone/models/User.java
@Data
@AllArgsConstructor
public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private byte[] profilePicData;
    private String bio;
    private byte[] wallpaperPicData;
    private Date createdAt;
    private int followersCount;
    private int followingCount;


    public User() {

    }
}