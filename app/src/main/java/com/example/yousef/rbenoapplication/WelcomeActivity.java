package com.example.yousef.rbenoapplication;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    private String country;
    private String currency;
    private int wasClicked = 0;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        init();

        progressDialog = new ProgressDialog(WelcomeActivity.this);
        progressDialog.setTitle("جاري الحصول على الموقع");

        Button tosigninBtn = findViewById(R.id.signinBtn);
        tosigninBtn.setOnClickListener(v -> {
            if (country != null) {
                startActivity(1);
                finish();
            } else {
                progressDialog.show();
                wasClicked = 1;
                requestPermissions();
            }
        });

        Button toRegisterBtn = findViewById(R.id.signupBtn);
        toRegisterBtn.setOnClickListener(v -> {
            if (country != null) {
                startActivity(2);
                finish();
            } else {
                progressDialog.show();
                wasClicked = 2;
                requestPermissions();
            }
        });

        Button guestBtn = findViewById(R.id.guestBtn);
        guestBtn.setOnClickListener(v -> {
            if (country != null) {
                startGuestActivity();
                finish();
            } else {
                progressDialog.show();
                wasClicked = 3;
                requestPermissions();
            }
        });

    }

    public void stopLocationUpdates() {
        // Removing location updates

        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback);
    }

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                if (mCurrentLocation != null) {
                    Log.d("ttt", "mCurrentLocation is not null");
                    Geocoder geocoder = new Geocoder(WelcomeActivity.this, new Locale("ar"));
                    try {
                        List<Address> addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                        if (!addresses.isEmpty()) {
                            Address a = addresses.get(0);
                            country = a.getCountryName();
                            Currency currentCurrency = Currency.getInstance(new Locale("ar", a.getCountryCode()));
                            currency = currentCurrency.getSymbol(new Locale("ar", a.getCountryCode()));
                            GlobalVariables.setCountry(country);
                            if (currency.contains(".")) {
                                if (currency.substring(currency.length() - 2, currency.length() - 1).equals(".")) {
                                    currency = currency.substring(0, currency.length() - 2);
                                }
                            }
                            GlobalVariables.setCurrency(currency);
                            stopLocationUpdates();
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            switch (wasClicked) {
                                case 1:
                                    startActivity(1);
                                    break;
                                case 2:
                                    startActivity(2);
                                    break;
                                case 3:
                                    startGuestActivity();
                                    break;
                            }
                        } else {
                            fetchFromApi(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                        }
                    } catch (IOException e) {
                        stopLocationUpdates();
                        Log.d("ttt", e.getLocalizedMessage());
                    }

//                    Toast.makeText(getApplicationContext(), "Lat: " + mCurrentLocation.getLatitude()
//                            + ", Lng: " + mCurrentLocation.getLongitude(), Toast.LENGTH_LONG).show();
                }
//                else {
//                    Toast.makeText(getApplicationContext(), "Last known location is not available!", Toast.LENGTH_SHORT).show();
//                }
//                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        requestPermissions();
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mSettingsClient
                    .checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener(this, locationSettingsResponse ->
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, Looper.myLooper()))
                    .addOnFailureListener(this, e -> {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(WelcomeActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.d("welcome", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Toast.makeText(WelcomeActivity.this, "لقد حصلت مشكلة اثناء محاولة الحصول على الموقع الرجاء التاكد من الاعدادات!", Toast.LENGTH_LONG).show();
                        }
                    });
        }

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("welcome", "User chose not to make required location settings changes.");
                mRequestingLocationUpdates = false;
            }
        }
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates && checkPermissions() && mSettingsClient != null) {
            startLocationUpdates();
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRequestingLocationUpdates) {
            stopLocationUpdates();
        }
    }

    public void fetchFromApi(double latitude, double longitude) {
        String url = "https://api.opencagedata.com/geocode/v1/json?key=078648c6ff684a8e851e63cbb1c8f6d8&q=" + latitude + "+" + longitude + "&pretty=1&no_annotations=1";
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, response -> {
            try {
                JSONObject Status = response.getJSONObject("status");
                if (Status.getString("message").equalsIgnoreCase("ok")) {
                    JSONArray Results = response.getJSONArray("results");
                    JSONObject zero = Results.getJSONObject(0);
                    JSONObject address = zero.getJSONObject("components");
                    country = address.getString("country");
                    String countryCode = address.getString("country_code");
                    Currency currentCurrency = Currency.getInstance(new Locale("ar", countryCode));
                    currency = currentCurrency.getSymbol(new Locale("ar", countryCode));
                    if (currency.contains(".")) {
                        if (currency.substring(currency.length() - 2, currency.length() - 1).equals(".")) {
                            currency = currency.substring(0, currency.length() - 2);
                        }
                    }
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    switch (wasClicked) {
                        case 1:
                            startActivity(1);
                            break;
                        case 2:
                            startActivity(2);
                            break;
                        case 3:
                            startGuestActivity();
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        queue.add(jsonObjectRequest);
        queue.start();
    }

    void requestPermissions() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(WelcomeActivity.this, "يجب الموافقة على صلاحية الوصول للموقع للمتابعة في التسجيل!", Toast.LENGTH_SHORT).show();
                        if (response.isPermanentlyDenied()) {
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    void startActivity(int path) {
        if (path == 1) {
            startActivity(new Intent(WelcomeActivity.this, SigninActivity.class).putExtra("country", country).putExtra("currency", currency));
        } else {
            startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class).putExtra("country", country).putExtra("currency", currency));
        }
        finish();
    }

    void startGuestActivity() {
        startActivity(new Intent(WelcomeActivity.this, HomeActivity.class).putExtra("country", country).putExtra("currency", currency).putExtra("guest", true));
        finish();
    }
}
