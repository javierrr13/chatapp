package com.chat.shared;

import java.io.Serializable;

public class UserProfileModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String fullname;
    private String bio;
    private String profilePicture;
    private String createdAt;

    public UserProfileModel(int id, int userId, String fullname, String bio, String profilePicture, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.fullname = fullname;
        this.bio = bio;
        this.profilePicture = profilePicture;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getFullname() {
        return fullname;
    }

    public String getBio() {
        return bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    @Override
    public String toString() {
        return "UserProfileModel{" +
               "id=" + id +
               ", userId=" + userId +
               ", fullname='" + fullname + '\'' +
               ", bio='" + bio + '\'' +
               ", profilePicture='" + profilePicture + '\'' +
               ", createdAt='" + createdAt + '\'' +
               '}';
    }
}
