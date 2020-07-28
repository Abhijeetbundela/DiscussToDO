package com.example.letsdiscusstodo.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;


@IgnoreExtraProperties
public class Post {

    public String uid;
    public String author;
    public String title;
    public String date;
    public String body;

    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();
    public int postlikeCount = 0;
    public Map<String, Boolean> postlike = new HashMap<>();


    public Post() {
        // Default constructor
    }

    public int getPostlikeCount() {
        return postlikeCount;
    }

    public void setPostlikeCount(int postlikeCount) {
        this.postlikeCount = postlikeCount;
    }

    public Map<String, Boolean> getPostlike() {
        return postlike;
    }

    public void setPostlike(Map<String, Boolean> postlike) {
        this.postlike = postlike;
    }

    public Post(String uid, String author, String title, String body, int starCount, Map<String, Boolean> stars, String date , int postlikeCount, Map<String, Boolean> postlike) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.starCount = starCount;
        this.stars = stars;
        this.date = date;
        this.postlike = postlike;
        this.postlikeCount = postlikeCount;

    }

    public Post(String uid, String author, String title, String body, String date) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.date = date;

    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("date", date);
        result.put("postlikecout", postlikeCount);
        result.put("postlike", postlike);

        return result;
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

    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    public Map<String, Boolean> getStars() {
        return stars;
    }

    public void setStars(Map<String, Boolean> stars) {
        this.stars = stars;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

