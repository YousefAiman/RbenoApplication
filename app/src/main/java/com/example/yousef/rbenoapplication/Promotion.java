package com.example.yousef.rbenoapplication;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Promotion implements Serializable {
    @PropertyName("type")
    public String type;
    @PropertyName("title")
    public String title;
    //    @PropertyName("city")
//    public String city;
    @PropertyName("description")
    public String description;
    @PropertyName("negotiable")
    public boolean negotiable;
    @PropertyName("price")
    public double price;
    @PropertyName("promoid")
    public long promoid;
    @PropertyName("publishtime")
    public long publishtime;
    @PropertyName("rating")
    public double rating;
    @PropertyName("uid")
    public String uid;
    @PropertyName("promoimages")
    public ArrayList<String> promoimages;
    @PropertyName("userName")
    public String userName;
    @PropertyName("favcount")
    public long favcount;
    @PropertyName("viewcount")
    public long viewcount;
    @PropertyName("videoUrl")
    public String videoUrl;
    @PropertyName("reports")
    public ArrayList<String> reports;
    @PropertyName("videoThumbnail")
    public String videoThumbnail;
    @PropertyName("country")
    public String country;
    @PropertyName("keyWords")
    public List<String> keyWords;
    @PropertyName("currencyCode")
    public String currencyCode;
    @PropertyName("currency")
    public String currency;
    @PropertyName("promoType")
    public String promoType;
    @PropertyName("isBanned")
    public boolean isBanned;

    public Promotion() {

    }

    //textPromo
    public Promotion(String type, String title, String description, boolean negotiable, double price, long promoid, long publishtime, double rating, String uid, String userName, long favcount, long viewcount, ArrayList<String> reports, String country, List<String> keyWords, String currencyCode, String currency, String promoType, boolean isBanned) {
        this.type = type;
        this.title = title;
//        this.city = city;
        this.description = description;
        this.negotiable = negotiable;
        this.price = price;
        this.promoid = promoid;
        this.publishtime = publishtime;
        this.rating = rating;
        this.uid = uid;
        this.userName = userName;
        this.favcount = favcount;
        this.viewcount = viewcount;
        this.reports = reports;
        this.country = country;
        this.keyWords = keyWords;
        this.currencyCode = currencyCode;
        this.currency = currency;
        this.promoType = promoType;
        this.isBanned = isBanned;
    }

    //videoPromo
    public Promotion(String type, String title, String description, boolean negotiable, double price, long promoid, long publishtime, double rating, String uid, String userName, long favcount, long viewcount, String videoUrl, ArrayList<String> reports, String videoThumbnail, String country, List<String> keyWords, String currencyCode, String currency, String promoType, boolean isBanned) {
        this.type = type;
        this.title = title;
//        this.city = city;
        this.description = description;
        this.negotiable = negotiable;
        this.price = price;
        this.promoid = promoid;
        this.publishtime = publishtime;
        this.rating = rating;
        this.uid = uid;
        this.userName = userName;
        this.favcount = favcount;
        this.viewcount = viewcount;
        this.videoUrl = videoUrl;
        this.reports = reports;
        this.videoThumbnail = videoThumbnail;
        this.country = country;
        this.keyWords = keyWords;
        this.currencyCode = currencyCode;
        this.currency = currency;
        this.promoType = promoType;
        this.isBanned = isBanned;
    }

    //imagePromo
    public Promotion(String type, String title, String description, boolean negotiable, double price, long promoid, long publishtime, double rating, String uid, ArrayList<String> promoimages, String userName, long favcount, long viewcount, ArrayList<String> reports, String country, List<String> keyWords, String currencyCode, String currency, String promoType, boolean isBanned) {
        this.type = type;
        this.title = title;
//        this.city = city;
        this.description = description;
        this.negotiable = negotiable;
        this.price = price;
        this.promoid = promoid;
        this.publishtime = publishtime;
        this.rating = rating;
        this.uid = uid;
        this.promoimages = promoimages;
        this.userName = userName;
        this.favcount = favcount;
        this.viewcount = viewcount;
        this.reports = reports;
        this.country = country;
        this.keyWords = keyWords;
        this.currencyCode = currencyCode;
        this.currency = currency;
        this.promoType = promoType;
        this.isBanned = isBanned;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public String getCity() {
//        return city;
//    }
//
//    public void setCity(String city) {
//        this.city = city;
//    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public boolean isNegotiable() {
        return negotiable;
    }

    public void setNegotiable(boolean negotiable) {
        this.negotiable = negotiable;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getPromoid() {
        return promoid;
    }

    public void setPromoid(long promoid) {
        this.promoid = promoid;
    }

    public long getPublishtime() {
        return publishtime;
    }

    public void setPublishtime(long publishtime) {
        this.publishtime = publishtime;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public ArrayList<String> getPromoimages() {
        return promoimages;
    }

    public void setPromoimages(ArrayList<String> imagesArray) {
        this.promoimages = imagesArray;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getFavcount() {
        return favcount;
    }

    public void setFavcount(long favcount) {
        this.favcount = favcount;
    }

    public long getViewcount() {
        return viewcount;
    }

    public void setViewcount(long viewcount) {
        this.viewcount = viewcount;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public ArrayList<String> getReports() {
        return reports;
    }

    public void setReports(ArrayList<String> reports) {
        this.reports = reports;
    }

    public String getVideoThumbnail() {
        return videoThumbnail;
    }

    public void setVideoThumbnail(String videoThumbnail) {
        this.videoThumbnail = videoThumbnail;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


    public List<String> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(List<String> keyWords) {
        this.keyWords = keyWords;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPromoType() {
        return promoType;
    }

    public void setPromoType(String promoType) {
        this.promoType = promoType;
    }

    public boolean getIsBanned() {
        return isBanned;
    }

    public void setIsBanned(boolean isBanned) {
        this.isBanned = isBanned;
    }
}
