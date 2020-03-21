package com.example.letsdiscusstodo.model;

import com.google.firebase.database.IgnoreExtraProperties;


@IgnoreExtraProperties
public class User {

    public String username;
    public String email;

    public User() {
        // Default constructor
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

}

