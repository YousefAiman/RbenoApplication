package com.example.yousef.rbenoapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {

    private static final int REQUEST_CHECK_SETTINGS = 100,
            REQUEST_LOCATION_PERMISSION = 10;

    private LocationRequester locationRequester;
    int lastClicked = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        final Button signinBtn = findViewById(R.id.signinBtn);
        final Button signupBtn = findViewById(R.id.signupBtn);
        final Button guestBtn = findViewById(R.id.guestBtn);

        if (GlobalVariables.getInstance().getCountryCode() != null) {

            signinBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, SigninActivity.class));
                finish();
            });

            signupBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
            });

            guestBtn.setOnClickListener(v -> {
                guestBtn.setClickable(false);
                FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        startActivity(new Intent(WelcomeActivity.this, HomeActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        guestBtn.setClickable(true);
                        Toast.makeText(WelcomeActivity.this, "فشلت محاولة الدخول الفوري!" +
                                " الرجاء المحاولة مرة اخرى", Toast.LENGTH_SHORT).show();
                    }
                });

            });
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

                Log.d("ttt", "requesting location persmission");
                requestPermissions(permissions, REQUEST_LOCATION_PERMISSION);
            }
            findViewById(R.id.signinBtn).setOnClickListener(v ->
                    intilizeLocationRequester(lastClicked = 1));

            findViewById(R.id.signupBtn).setOnClickListener(v ->
                    intilizeLocationRequester(lastClicked = 2));

            findViewById(R.id.guestBtn).setOnClickListener(v ->
                    intilizeLocationRequester(lastClicked = 0));
        }


    }

    void intilizeLocationRequester(int wasCLicked) {
        Log.d("ttt", "location requester");
        locationRequester = new LocationRequester(WelcomeActivity.this,
                this, wasCLicked);
        locationRequester.getLastKnownLocation();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (lastClicked != -1) {
                    intilizeLocationRequester(lastClicked);
                }
            } else {
                Toast.makeText(this,
                        "هذا التطبيق يحتاج الى الوصول الى موقعك بهدف اظهار اعلانات من دولتك",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

//    void startActivity(int path) {
//
//        startActivity(new Intent(this,
//                path == 1 ? SigninActivity.class : RegisterActivity.class));
//
//        finish();
//    }
//  @Override
//  public void onBackPressed() {
//    moveTaskToBack(true);
//  }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_CANCELED) {
                locationRequester.mRequestingLocationUpdates = false;
            } else {
                locationRequester.getLastKnownLocation();
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
