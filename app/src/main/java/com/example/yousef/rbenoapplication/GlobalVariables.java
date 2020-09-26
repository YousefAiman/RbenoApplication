package com.example.yousef.rbenoapplication;

import android.app.Application;

import java.util.List;

public class GlobalVariables extends Application {
    private static GlobalVariables globalSingleton;
    private static boolean wifiIsOn;
    private static String country;
    private static String currency;
    private static List<String> blockedUsers;
    private static int videoViewedCount;
    private static int windowHeight;
    private static int windowWidth;
    private static float density;
    private static String profileImageUrl;

    public static GlobalVariables getInstance() {
        return globalSingleton;
    }

    public static String getCountry() {
        return country;
    }

    public static void setCountry(String country) {
        GlobalVariables.country = country;
    }

    public static String getCurrency() {
        return currency;
    }

    public static void setCurrency(String currency) {
        GlobalVariables.currency = currency;
    }

    public static boolean isWifiIsOn() {
        return wifiIsOn;
    }

    public static void setWifiIsOn(boolean wifiIsOn) {
        GlobalVariables.wifiIsOn = wifiIsOn;
    }

    public static int getVideoViewedCount() {
        return videoViewedCount;
    }

    public static void setVideoViewedCount(int videoViewedCount) {
        GlobalVariables.videoViewedCount = videoViewedCount;
    }

    public static int getWindowHeight() {
        return windowHeight;
    }

    public static void setWindowHeight(int windowHeight) {
        GlobalVariables.windowHeight = windowHeight;
    }

    public static int getWindowWidth() {
        return windowWidth;
    }

    public static void setWindowWidth(int windowWidth) {
        GlobalVariables.windowWidth = windowWidth;
    }

    public static float getDensity() {
        return density;
    }

    public static void setDensity(float density) {
        GlobalVariables.density = density;
    }

    public static String getProfileImageUrl() {
        return profileImageUrl;
    }

    public static void setProfileImageUrl(String profileImageUrl) {
        GlobalVariables.profileImageUrl = profileImageUrl;
    }

    public static List<String> getBlockedUsers() {
        return blockedUsers;
    }

    public static void setBlockedUsers(List<String> blockedUsers) {
        GlobalVariables.blockedUsers = blockedUsers;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        globalSingleton = this;
    }
}
