package com.example.yousef.rbenoapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = NetworkUtil.getConnectivityStatusString(context);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                GlobalVariables.setWifiIsOn(false);
//                context.startActivity(new Intent(context, ConnectionActivity.class));
            } else if (status == NetworkUtil.NETWORK_STATUS_WIFI) {
                GlobalVariables.setWifiIsOn(true);
            }
        }
    }
}
