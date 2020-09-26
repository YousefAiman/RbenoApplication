package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ConnectionActivity extends AppCompatActivity {
    boolean isOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    isOnline = true;
                }

                @Override
                public void onLost(@NonNull Network network) {
                    isOnline = false;
                }
            });
        }
        Button button = findViewById(R.id.retryBtn);
        button.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (isOnline) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(ConnectionActivity.this, "الرجاء التحقق من الاتصال بالانترنت!", Toast.LENGTH_SHORT).show();
                }
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                    registerReceiver(new WifiReceiver(), intentFilter);

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(ConnectionActivity.this, "الرجاء التحقق من الاتصال بالانترنت!", Toast.LENGTH_SHORT).show();
                }
//            WifiReceiver.wifiIsOn;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//                cm.addDefaultNetworkActiveListener(() -> {
//                    if(cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()){
////                            startHomeActivity();
////                            finish();
//                        Log.d("ttt","wifi online");
//                    }else{
////                            finish();
////                            startConnectionActivity();
//                        Log.d("ttt","wifi offline");
//                    }
//                    cm.removeDefaultNetworkActiveListener((ConnectivityManager.OnNetworkActiveListener) this);
//                });
//            }
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
