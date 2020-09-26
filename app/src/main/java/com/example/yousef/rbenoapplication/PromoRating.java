package com.example.yousef.rbenoapplication;

import androidx.annotation.Keep;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

@Keep
public class PromoRating implements Serializable {

    @PropertyName("userid")
    public String userid;
    @PropertyName("rating")
    public float rating;

    PromoRating() {
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

}
