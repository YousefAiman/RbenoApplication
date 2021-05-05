package com.example.yousef.rbenoapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

  private static final int
          REQUEST_CHECK_SETTINGS = 100,
          REQUEST_LOCATION_PERMISSION = 10;

  private LocationRequester locationRequester;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (WifiUtil.isConnectedToInternet(this)) {

      if (getIntent().hasExtra("messagingBundle")) {
        startMessagingActivity();
      } else {
        startHomeActivity();
      }

    } else {

      startConnectionActivity();

    }

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
                .whereEqualTo("userId", user.getUid())
                .get().addOnSuccessListener(snapshots -> {
          final DocumentSnapshot documentSnapshot = snapshots.getDocuments().get(0);
          if (documentSnapshot.contains("remembered")
                  && documentSnapshot.getBoolean("remembered")) {

//            startService(new Intent(MainActivity.this,MyFirebaseMessaging.class));

            getApplicationContext().getPackageManager().setComponentEnabledSetting(
                    new ComponentName(this.getApplicationContext(), MyFirebaseMessaging.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

//            FirebaseMessaging.getInstance().subscribeToTopic(user.getUid());
//            updateToken(FirebaseInstanceId.getInstance()., documentSnapshot.getId());

            locationRequester = new LocationRequester(MainActivity.this, this,
                    HomeActivity.class);
            locationRequester.geCountryFromLocation();

//            startActivity(new Intent(MainActivity.this, HomeActivity.class));
//            finish();
          } else {
            new Handler().postDelayed(() -> {
              startActivity(new Intent(MainActivity.this, SigninActivity.class)
                      .putExtra("email", user.getEmail()));
              finish();
            }, 500);
          }
        }).addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            finish();
          }
        });
      } else {

        final SharedPreferences sharedPreferences = getSharedPreferences("rbeno", MODE_PRIVATE);
        if (sharedPreferences.contains("countryCode")) {

          GlobalVariables.getInstance().setCountryCode(sharedPreferences.getString("countryCode", ""));

          new Handler().postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
          }, 500);

        } else {
          new Handler().postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            finish();
          }, 500);
        }

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

        final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

          Log.d("ttt", "requesting location persmission");

          requestPermissions(permissions, REQUEST_LOCATION_PERMISSION);

        } else {
          intilizeLocationRequester();
        }

      }
    }

  }

  void intilizeLocationRequester() {
    Log.d("ttt", "location requester");
    locationRequester = new LocationRequester(MainActivity.this, this);
    locationRequester.geCountryFromLocation();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {

    if (requestCode == REQUEST_LOCATION_PERMISSION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        intilizeLocationRequester();
      } else {
        Toast.makeText(this,
                "هذا التطبيق يحتاج الى الوصول الى موقعك بهدف اظهار اعلانات من دولتك",
                Toast.LENGTH_LONG).show();
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
        locationRequester.getLastKnownLocation();

      }
    } else if (resultCode == ConnectionActivity.CONNECTION_RESULT) {

      if (getIntent().hasExtra("messagingBundle")) {
        startMessagingActivity();
      } else {
        startHomeActivity();
      }

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
