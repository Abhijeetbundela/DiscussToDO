package com.example.letsdiscusstodo.model;

import android.net.Uri;

public class UserInformation {

    private String userId;
    private String userName;
    private String about;
    private String profileUri;
    private String userEmail;

    public UserInformation(){}



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
