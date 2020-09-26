package com.example.yousef.rbenoapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class WifiReceiver extends BroadcastReceiver {
    public boolean wifiIsOn;

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert conMan != null;
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Log.d("ttt", "wifi online");
            GlobalVariables.setWifiIsOn(true);
        } else {
            GlobalVariables.setWifiIsOn(false);
            Log.d("ttt", "wifi offline");
        }
    }
}
