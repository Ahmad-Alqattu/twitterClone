package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private boolean hasProfilePic;
    private String bio;
    private boolean hasWallpaper;
    private Date createdAt;
    private int followersCount;
    private int followingCount;

    private byte[] wallpaperPicData;
    private byte[] profilePicData;

    public User() {

    }
}