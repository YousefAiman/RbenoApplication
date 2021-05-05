package com.example.yousef.rbenoapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class ShutdownService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.d("exoPlayerPlayback", "onTaskRemoved");

        GlobalVariables.setAppIsRunning(false);

//    BadgeUtil.clearBadge(this);

        if (!VideoCache.isNull()) {
            Log.d("exoPlayerPlayback", "video cache is not null");
            VideoDataSourceFactory.clearVideoCache(this);

        } else {
            Log.d("exoPlayerPlayback", "video cache is null");
        }

        getSharedPreferences("rbeno", MODE_PRIVATE).edit()
                .remove("isPaused")
                .remove("currentMessagingUserId")
                .remove("currentMessagingPromoId").apply();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                GlobalVariables.getRegisteredNetworkCallback() != null) {
            ((ConnectivityManager) getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .unregisterNetworkCallback(GlobalVariables.getRegisteredNetworkCallback());

            GlobalVariables.setRegisteredNetworkCallback(null);

        } else if (GlobalVariables.getCurrentWifiReceiver() != null) {

            unregisterReceiver(GlobalVariables.getCurrentWifiReceiver());
            GlobalVariables.setCurrentWifiReceiver(null);
        }

        this.stopSelf();
    }
}
