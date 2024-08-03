package org.example.models;

import lombok.Data;

// src/main/java/com/twitterclone/models/User.java
@Data
public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String profilePhotoUrl;


}