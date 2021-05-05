package com.example.yousef.rbenoapplication;

import android.app.Application;
import android.net.ConnectivityManager.NetworkCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GlobalVariables extends Application {

    private static GlobalVariables globalSingleton;
    private static boolean wifiIsOn;
    private static List<String> blockedUsers = new ArrayList<>();
    private static List<Long> favPromosIds = new ArrayList<>();
    private static List<String> previousSentNotifications = new ArrayList<>();
    private String countryCode;
    private static int videoViewedCount;
    private static boolean appIsRunning;
    private static String currentToken;
    private static Map<String, Integer> messagesNotificationMap;
    private static WifiReceiver currentWifiReceiver;
    private static NetworkCallback registeredNetworkCallback;


    public static GlobalVariables getInstance() {
        return globalSingleton;
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

    public static List<String> getBlockedUsers() {
        return blockedUsers;
    }

    public static void setBlockedUsers(List<String> blockedUsers) {
        GlobalVariables.blockedUsers = blockedUsers;
    }

    public static List<Long> getFavPromosIds() {
        return favPromosIds;
    }

    public static void setFavPromosIds(List<Long> favPromosIds) {
        GlobalVariables.favPromosIds = favPromosIds;
    }

    public static List<String> getPreviousSentNotifications() {
        return previousSentNotifications;
    }

    public static void setPreviousSentNotifications(List<String> previousSentNotifications) {
        GlobalVariables.previousSentNotifications = previousSentNotifications;
    }

    public static Map<String, Integer> getMessagesNotificationMap() {
        return messagesNotificationMap;
    }

    public static void setMessagesNotificationMap(Map<String, Integer> messagesNotificationMap) {
        GlobalVariables.messagesNotificationMap = messagesNotificationMap;
    }

    public static String getCurrentToken() {
        return currentToken;
    }

    public static void setCurrentToken(String currentToken) {
        GlobalVariables.currentToken = currentToken;
    }

    public static boolean isAppIsRunning() {
        return appIsRunning;
    }

    public static void setAppIsRunning(boolean appIsRunning) {
        GlobalVariables.appIsRunning = appIsRunning;
    }

    public static NetworkCallback getRegisteredNetworkCallback() {
        return registeredNetworkCallback;
    }

    public static void setRegisteredNetworkCallback(NetworkCallback registeredNetworkCallback) {
        GlobalVariables.registeredNetworkCallback = registeredNetworkCallback;
    }

    public static WifiReceiver getCurrentWifiReceiver() {
        return currentWifiReceiver;
    }

    public static void setCurrentWifiReceiver(WifiReceiver currentWifiReceiver) {
        GlobalVariables.currentWifiReceiver = currentWifiReceiver;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        globalSingleton = this;
    }

}
