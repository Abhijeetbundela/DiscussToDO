package com.example.letsdiscusstodo.model;

import java.util.ArrayList;

public class UserInformation {

    private String userId,verified;
    private String userName;
    private String about;
    private String profileUri;
    private String userEmail;
    private String user_thumb_image;
    private String registrationToken;

    public UserInformation() {
    }



    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getUser_thumb_image() {
        return user_thumb_image;
    }

    public void setUser_thumb_image(String user_thumb_image) {
        this.user_thumb_image = user_thumb_image;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public UserInformation(String userId, String userName, String about, String profileUri, String userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.about = about;
        this.profileUri = profileUri;
        this.userEmail = userEmail;

    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(String profileUri) {
        this.profileUri = profileUri;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
