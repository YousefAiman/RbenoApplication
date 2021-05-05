package com.example.yousef.rbenoapplication;

import java.util.ArrayList;

public class UserInfo {

    private String email;
    private String username;
    private String userId;
    private String staticusername;
    private String imageurl;
    private String token;
    private ArrayList<Long> favpromosids;
    private ArrayList<String> reports;
    private ArrayList<String> searchHistory;
    private ArrayList<String> usersBlocked;
    private boolean status;
    private boolean remembered;
    private String city;
    private String countryCode;
    private String phonenum;


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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStaticusername() {
        return staticusername;
    }

    public void setStaticusername(String staticusername) {
        this.staticusername = staticusername;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ArrayList<Long> getFavpromosids() {
        return favpromosids;
    }

    public void setFavpromosids(ArrayList<Long> favpromosids) {
        this.favpromosids = favpromosids;
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isRemembered() {
        return remembered;
    }

    public void setRemembered(boolean remembered) {
        this.remembered = remembered;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhonenum() {
        return phonenum;
    }

    public void setPhonenum(String phonenum) {
        this.phonenum = phonenum;
    }

    public ArrayList<String> getSearchHistory() {
        return searchHistory;
    }

    public void setSearchHistory(ArrayList<String> searchHistory) {
        this.searchHistory = searchHistory;
    }
}
