package com.example.letsdiscusstodo.model;

import com.google.firebase.database.IgnoreExtraProperties;


@IgnoreExtraProperties
public class Comment {

    public String uid;
    public String author;
    public String text;

    public Comment() {
        // Default constructor
    }

    public Comment(String uid, String author, String text) {
        this.uid = uid;
        this.author = author;
        this.text = text;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

