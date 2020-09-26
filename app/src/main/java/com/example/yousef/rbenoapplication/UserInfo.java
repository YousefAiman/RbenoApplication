package com.example.yousef.rbenoapplication;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;

public class UserInfo implements Serializable {
    @PropertyName("email")
    public String email;
    @PropertyName("username")
    public String username;
    @PropertyName("imageurl")
    public String imageurl;
    @PropertyName("userId")
    public String userId;
    @PropertyName("staticusername")
    public String staticusername;
    @PropertyName("phonenum")
    public String phonenum;
    @PropertyName("favpromosids")
    public ArrayList<Integer> favpromosids;
    @PropertyName("status")
    public boolean status;
    @PropertyName("remembered")
    public boolean remembered;
    @PropertyName("country")
    public String country;
    //    @PropertyName("city")
//    public String city;
    @PropertyName("currency")
    public String currency;
    @PropertyName("reports")
    public ArrayList<String> reports;
    @PropertyName("usersBlocked")
    public ArrayList<String> usersBlocked;


    public UserInfo() {

    }

    public UserInfo(String email, String username, String imageurl, String userId, String staticusername, ArrayList<Integer> favpromosids,
                    boolean remembered, boolean status, String country, String currency, ArrayList<String> reports, ArrayList<String> usersBlocked) {
        this.email = email;
        this.username = username;
        this.imageurl = imageurl;
        this.userId = userId;
        this.staticusername = staticusername;
        this.favpromosids = favpromosids;
        this.remembered = remembered;
        this.status = status;
        this.country = country;
        this.currency = currency;
        this.reports = reports;
        this.usersBlocked = usersBlocked;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getStaticusername() {
        return staticusername;
    }

    public void setStaticusername(String staticusername) {
        this.staticusername = staticusername;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhonenum() {
        return phonenum;
    }

    public void setPhonenum(String phonenum) {
        this.phonenum = phonenum;
    }

    public ArrayList<Integer> getFavpromosids() {
        return favpromosids;
    }

    public void setFavpromosids(ArrayList<Integer> favpromosids) {
        this.favpromosids = favpromosids;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public boolean getRemembered() {
        return remembered;
    }

    public void setRemembered(boolean remembered) {
        this.remembered = remembered;
    }

    public ArrayList<String> getReports() {
        return reports;
    }

    public void setReports(ArrayList<String> reports) {
        this.reports = reports;
    }

    public ArrayList<String> getUsersBlocked() {
        return usersBlocked;
    }

    public void setUsersBlocked(ArrayList<String> usersBlocked) {
        this.usersBlocked = usersBlocked;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

//    public String getCity() {
//        return city;
//    }
//
//    public void setCity(String city) {
//        this.city = city;
//    }
}
