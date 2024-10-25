package com.example.trackpocket.Model;

import android.net.Uri;

public class UserInformation {

    private String userId;
    private String name;
    private String phoneNumber;
    private String createdAt;
    private String updatedAt;

    public UserInformation() {
    }

    // Constructor with parameters
    public UserInformation(String userId, String name, String phoneNumber,
                           String createdAt, String updatedAt) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setUserId(String uid) {
        this.userId = uid;
    }


    public String getUserId() {
        return userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
