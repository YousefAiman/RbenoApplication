package com.example.yousef.rbenoapplication;

import java.io.Serializable;

public class BlockedUser implements Serializable {

    private String username;
    private String imageurl;
    private String userId;
    private boolean status;

    public BlockedUser(String username, String imageurl, String userId, boolean status) {
        this.username = username;
        this.imageurl = imageurl;
        this.userId = userId;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
