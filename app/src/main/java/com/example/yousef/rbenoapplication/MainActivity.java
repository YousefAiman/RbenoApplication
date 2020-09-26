package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    CollectionReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//
//        if (activeNetwork != null && activeNetwork.isConnected()){
//            startHomeActivity();
//        }else{
//            startConnectionActivity();
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    Toast.makeText(MainActivity.this, "online", Toast.LENGTH_SHORT).show();
                    startHomeActivity();
                    Log.d("ttt", "wifi online");
                    cm.unregisterNetworkCallback(this);
                    cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            super.onAvailable(network);
                            Log.d("ttt", "wifi online");
                            GlobalVariables.setWifiIsOn(true);
                        }

                        @Override
                        public void onLost(@NonNull Network network) {
                            super.onLost(network);
                            GlobalVariables.setWifiIsOn(false);
                            Log.d("ttt", "wifi offline");
                        }
                    });

                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Toast.makeText(MainActivity.this, "offline", Toast.LENGTH_SHORT).show();
                    startConnectionActivity();
                    cm.unregisterNetworkCallback(this);
                }
            });

        } else {

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                registerReceiver(new WifiReceiver(), intentFilter);
                startHomeActivity();
            } else {
                startConnectionActivity();
            }
        }
    }
//    public boolean isLoggedIn() {
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        return accessToken != null && !accessToken.isExpired();
//    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void updateToken(String token, String documentId) {
        userRef.document(documentId).update("token", token).addOnFailureListener(e -> Log.d("ttt", e.getLocalizedMessage())).addOnSuccessListener(aVoid -> Log.d("ttt", "token updated"));
    }

    void startConnectionActivity() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), ConnectionActivity.class));
            finish();
        }, 500);
    }

    void startHomeActivity() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseFirestore.getInstance().collection("users");
        if (user != null) {
            userRef.whereEqualTo("userId", user.getUid()).limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                if (documentSnapshot.getBoolean("remembered")) {
//                  FirebaseMessaging.getInstance().setAutoInitEnabled(true);
                    FirebaseMessaging.getInstance().subscribeToTopic(user.getUid());
                    updateToken(FirebaseInstanceId.getInstance().getToken(), documentSnapshot.getId());
                    new Handler().postDelayed(() -> {
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        finish();
                    }, 0);
                } else {
//                      updateToken(FirebaseInstanceId.getInstance().getToken(),documentSnapshot.getId());
                    new Handler().postDelayed(() -> {
                        startActivity(new Intent(getApplicationContext(), SigninActivity.class).putExtra("email", user.getEmail()));
                        finish();
                    }, 500);
                }
            }).addOnFailureListener(e -> {
                startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                Log.d("ttt", e.getMessage());
            });
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("rbeno", Context.MODE_PRIVATE);
            Editor editor = getSharedPreferences("rbeno", MODE_PRIVATE).edit();
            if (!sharedPreferences.getBoolean("hasVisited", false)) {
                editor.putBoolean("hasVisited", true).apply();
//                    editor.putString("area", address).apply();
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(getApplicationContext(), SliderActivity.class));
                    finish();
                }, 500);
            } else {
//                      FirebaseMessaging.getInstance().setAutoInitEnabled(true);
//                    editor.putString("area", address).apply();
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                    finish();
                }, 500);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
