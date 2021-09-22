package com.example.yousef.rbenoapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int
            REQUEST_CHECK_SETTINGS = 100,
            REQUEST_LOCATION_PERMISSION_TO_SLIDER = 11,
            REQUEST_LOCATION_PERMISSION_TO_HOME = 12;

    private LocationRequester locationRequester;

    private static final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MobileAds.initialize(this, initializationStatus -> directUserToAppropriateActivity());

        FirebaseFirestore.getInstance().collection("users")
                .get().addOnSuccessListener(snapshots -> {

            for (DocumentSnapshot snapshot : snapshots) {
                final String username = snapshot.getString("username");

                final String lowerCaseTrimmedUsername = username.toLowerCase().trim().replaceAll("\\s", "");
                snapshot.getReference().update("staticusername", "@" + lowerCaseTrimmedUsername,
                        "usernameForSearch", lowerCaseTrimmedUsername);

            }

        });

//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//      final NetworkCapabilities capabilities= cm.getNetworkCapabilities(cm.getActiveNetwork());
//
//      if (capabilities != null &&
//              (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
//              || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
//              || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) {
//
//        Log.d("ttt","has one of te mentioned transposrtrs");
//
//          WifiUtil.registerNetworkCallback(this,0,cm);
//
//          if(getIntent().hasExtra("messagingBundle")){
//            startMessagingActivity();
//          }else{
//            startHomeActivity();
//          }
//
//      }else{
//        startConnectionActivity();
//        Log.d("ttt","capabilities  null");
//      }
//
//
//    } else {
//
//      final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//
//      if (activeNetwork != null && activeNetwork.isConnected()) {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//          WifiUtil.registerNetworkCallback(this,0,cm);
//        }else{
//          WifiUtil.registerReceiver(this);
//        }
//
//        if(getIntent().hasExtra("messagingBundle")){
//          startMessagingActivity();
//        }else{
//          startHomeActivity();
//        }
//
//      } else {
//
//        startConnectionActivity();
//
//      }
//
//    }
    }

    private void directUserToAppropriateActivity() {

        if (WifiUtil.isConnectedToInternet(this)) {

            final SharedPreferences sharedPreferences =
                    getSharedPreferences("rbeno", Context.MODE_PRIVATE);

            if (!sharedPreferences.contains("notFirstTime")) {

                Log.d("ttt", "doesn't contain first time");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

                    Log.d("ttt", "requesting location persmission");

                    requestPermissions(permissions, REQUEST_LOCATION_PERMISSION_TO_SLIDER);

                } else {


                    new Handler().postDelayed(() -> {

                        locationRequester = new LocationRequester(
                                MainActivity.this,
                                MainActivity.this,
                                SliderActivity.class);

                        locationRequester.geCountryFromLocation();
                    }, 1000);

                }


            } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                if (getIntent().hasExtra("messagingBundle")) {
                    startMessagingActivity();
                } else {
                    startHomeActivity();
                }


            } else {

                Log.d("ttt", "contains first time");


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

                    Log.d("ttt", "requesting location persmission");

                    requestPermissions(permissions, REQUEST_LOCATION_PERMISSION_TO_HOME);

                } else {

                    new Handler().postDelayed(() -> FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(authResult -> intilizeLocationRequester(HomeActivity.class)).addOnFailureListener(e -> {

                        Toast.makeText(MainActivity.this,
                                "حصلت مشكلة اثناء محاولة الدخول الى التطبيق!", Toast.LENGTH_SHORT).show();
                        finish();

                    }), 500);
                }

            }
        } else {

            startConnectionActivity();

        }

    }

//    public boolean isLoggedIn() {
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        return accessToken != null && !accessToken.isExpired();
//    }
//  private void updateToken(String token, String documentId) {
//    userRef.document(documentId).update("token", token);
//  }


    private void startMessagingActivity() {
        new Handler().postDelayed(() -> {

            startActivity(new Intent(MainActivity.this, MessagingRealTimeActivity.class)
                    .putExtra("messagingBundle",
                            getIntent().getBundleExtra("messagingBundle")));

            finish();
        }, 500);
    }

    private void startConnectionActivity() {
        new Handler().postDelayed(() -> {
            startActivityForResult(new Intent(MainActivity.this, ConnectionActivity.class),
                    ConnectionActivity.CONNECTION_RESULT);
        }, 800);
    }

    private void startHomeActivity() {

        GlobalVariables.setAppIsRunning(true);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (!user.isAnonymous()) {

                startService(new Intent(this, MyFirebaseMessaging.class));

                FirebaseFirestore.getInstance().collection("users")
                        .document(user.getUid())
                        .get().addOnSuccessListener(snapshot -> {


                    if (snapshot.contains("remembered")
                            && snapshot.getBoolean("remembered")) {

                        GlobalVariables.setBlockedUsers((List<String>) snapshot.get("usersBlocked"));
                        GlobalVariables.setCurrentToken(snapshot.getString("token"));

//            startService(new Intent(MainActivity.this,MyFirebaseMessaging.class));

                        getApplicationContext().getPackageManager().setComponentEnabledSetting(
                                new ComponentName(this.getApplicationContext(), MyFirebaseMessaging.class),
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);

//            FirebaseMessaging.getInstance().subscribeToTopic(user.getUid());
//            updateToken(FirebaseInstanceId.getInstance()., documentSnapshot.getId());

                        intilizeLocationRequester(HomeActivity.class);

//            startActivity(new Intent(MainActivity.this, HomeActivity.class));
//            finish();
                    } else {
                        new Handler().postDelayed(() -> {
                            startActivity(new Intent(MainActivity.this, SigninActivity.class)
                                    .putExtra("email", user.getEmail()));
                            finish();
                        }, 500);
                    }
                }).addOnFailureListener(e -> {

                    FirebaseAuth.getInstance().signOut();

                    startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                    finish();
                });
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(permissions, REQUEST_LOCATION_PERMISSION_TO_HOME);

                } else {
                    intilizeLocationRequester(HomeActivity.class);
                }

//                final SharedPreferences sharedPreferences = getSharedPreferences("rbeno", MODE_PRIVATE);
//                if (sharedPreferences.contains("countryCode")) {
//
//                    GlobalVariables.getInstance().setCountryCode(sharedPreferences.getString("countryCode", ""));
//
//                    new Handler().postDelayed(() -> {
//                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
//                        finish();
//                    }, 500);
//
//                } else {
//
//
//
//                    new Handler().postDelayed(() -> {
//                        startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
//                        finish();
//                    }, 500);
//                }

            }
        } else {


            final SharedPreferences sharedPreferences = getSharedPreferences("rbeno", MODE_PRIVATE);
            if (sharedPreferences.contains("countryCode")) {

                GlobalVariables.getInstance().setCountryCode(sharedPreferences.getString("countryCode", ""));

                FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(authResult -> {
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                });
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

                    Log.d("ttt", "requesting location persmission");

                    requestPermissions(permissions, REQUEST_LOCATION_PERMISSION_TO_HOME);

                } else {
                    intilizeLocationRequester(HomeActivity.class);
                }

            }
        }

    }

    void intilizeLocationRequester(Class<?> targetActivity) {
        Log.d("ttt", "location requester");
        locationRequester = new LocationRequester(MainActivity.this, this, targetActivity);
        locationRequester.geCountryFromLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION_PERMISSION_TO_HOME ||
                requestCode == REQUEST_LOCATION_PERMISSION_TO_SLIDER) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                switch (requestCode) {

                    case REQUEST_LOCATION_PERMISSION_TO_HOME:
                        intilizeLocationRequester(HomeActivity.class);
                        break;

                    case REQUEST_LOCATION_PERMISSION_TO_SLIDER:
                        intilizeLocationRequester(SliderActivity.class);
                        break;

                }

            } else {
                Toast.makeText(this,
                        "هذا التطبيق يحتاج الى الوصول الى موقعك بهدف اظهار اعلانات من دولتك",
                        Toast.LENGTH_LONG).show();

                new Handler().postDelayed(() -> finish(), 1500);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_CANCELED) {
//        locationRequester.mRequestingLocationUpdates = false;
                Log.d("ttt", "RESULT_CANCELED");

            } else {

//        LocationRequester.getNewCountry(this);

                Log.d("ttt", "result ok man");
//                locationRequester.getLastKnownLocation();
                directUserToAppropriateActivity();
            }
        } else if (resultCode == ConnectionActivity.CONNECTION_RESULT) {

            directUserToAppropriateActivity();

//            if (getIntent().hasExtra("messagingBundle")) {
//                startMessagingActivity();
//            } else {
//                startHomeActivity();
//            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationRequester != null) {
            locationRequester.resumeLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationRequester != null) {
            locationRequester.stopLocationUpdates();
        }
    }

}
