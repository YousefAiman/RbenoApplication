package com.example.yousef.rbenoapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class LocationRequester {

  private static final int REQUEST_CHECK_SETTINGS = 100;
  private final Context context;
  private final Activity activity;
  Boolean mRequestingLocationUpdates;
  private FusedLocationProviderClient fusedLocationClient;
  private LocationRequest locationRequest;
  private LocationCallback locationCallback;
  private int activityBtnClicked;
  private ProgressDialog progressDialog;
  private int retries = 0;
  private Class<?> destinationActivity;

  public LocationRequester(Context context, Activity activity) {
    this.activity = activity;
    this.context = context;
  }

  public LocationRequester(Context context, Activity activity, Class<?> destinationActivity) {
    this.activity = activity;
    this.context = context;
    this.destinationActivity = destinationActivity;

//    progressDialog = new ProgressDialog(context);
//    progressDialog.setMessage("جاري التحميل...");
//    progressDialog.setCancelable(false);
//    progressDialog.show();

  }

  public LocationRequester(Context context, Activity activity, int activityBtnClicked) {
    this.activity = activity;
    this.context = context;
    this.activityBtnClicked = activityBtnClicked;

    progressDialog = new ProgressDialog(context);
    progressDialog.setCancelable(false);
    progressDialog.show();
  }

  @SuppressLint("MissingPermission")
  void geCountryFromLocation() {

    Log.d("ttt", "getting last known location");

    if (fusedLocationClient != null && locationCallback != null) {

      fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
              Looper.myLooper());

      return;
    }

    locationRequest = LocationRequest.create().
            setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            .setInterval(10000).setFastestInterval(5000);

    final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest);

    LocationServices.getSettingsClient(activity)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener(locationSettingsResponse -> {
              Log.d("ttt", "location is enabled");

              getLastKnownLocation();

            }).addOnFailureListener(e -> {
      if (e instanceof ResolvableApiException) {
        Log.d("ttt", "location is not enabled");
        try {
          final ResolvableApiException resolvable = (ResolvableApiException) e;
          resolvable.startResolutionForResult(activity,
                  REQUEST_CHECK_SETTINGS);

        } catch (IntentSender.SendIntentException sendEx) {
          // Ignore the error.
        }
      }
    });

  }

  @SuppressLint("MissingPermission")
  void getLastKnownLocation() {

    Log.d("ttt", "getting last known location");
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

    fusedLocationClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
              @Override
              public void onSuccess(Location location) {
                if (location == null) {

                  Log.d("ttt", "last location is null");

                  mRequestingLocationUpdates = true;

                  fusedLocationClient.requestLocationUpdates(locationRequest,
                          locationCallback = addLocationCallback(),
                          Looper.getMainLooper());

                } else {

                  getCountryInfoFromLocation(location);

                  Log.d("ttt", "last known location: " + location);

                }

              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback = addLocationCallback(),
                Looper.getMainLooper());

        Log.d("ttt", "last known failed: " + e.getMessage());
      }
    });

  }

  @SuppressLint("MissingPermission")
  void resumeLocationUpdates() {

    if (mRequestingLocationUpdates != null && !mRequestingLocationUpdates
            && locationCallback != null && fusedLocationClient != null) {
      mRequestingLocationUpdates = true;

      fusedLocationClient.requestLocationUpdates(locationRequest,
              locationCallback = addLocationCallback(),
              Looper.getMainLooper());
    }

  }

  void stopLocationUpdates() {
    Log.d("ttt", "stopping location updates");
    if (mRequestingLocationUpdates != null && mRequestingLocationUpdates) {
      if (locationCallback != null && fusedLocationClient != null) {
        mRequestingLocationUpdates = false;
        dismissProgressDialog();
        fusedLocationClient.removeLocationUpdates(locationCallback);
      }
    }
  }


  void dismissProgressDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  LocationCallback addLocationCallback() {

    return new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {

        Log.d("ttt", "onLocationResult");

        if (locationResult == null) {
          Log.d("ttt", "onLocationResult locationResult is null");
          return;
        }

        for (Location location : locationResult.getLocations()) {
          Log.d("ttt", "location result is not null");

          if (location != null) {

            stopLocationUpdates();
            getCountryInfoFromLocation(location);

            break;
          }
        }

      }

    };

  }

  public static void main(String[] args) {

    Currency currency = Currency.getInstance(Locale.getDefault());
    System.out.println("currency Code: " + currency.getCurrencyCode());
    System.out.println("currency getDisplayName: " + currency.getDisplayName());
    System.out.println("currency getSymbol: " + currency.getSymbol());

  }


  void getCountryInfoFromLocation(Location location) {
    final Geocoder geocoder = new Geocoder(context, new Locale("ar"));

    try {
      final List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
              location.getLongitude(), 1);

      if (!addresses.isEmpty() && addresses.get(0).getCountryName() != null) {

        final Address a = addresses.get(0);

        Log.d("ttt", "address to string: " + a.toString());

        final String cityName = a.getLocality();

        final String country = a.getCountryName();
        final String countryCode = a.getCountryCode();

        String currency = Currency.getInstance(new Locale("en", countryCode))
                .getCurrencyCode();

        if (currency.contains(".")) {
          if (currency.substring(currency.length() - 2, currency.length() - 1)
                  .equals(".")) {
            currency = currency.substring(0, currency.length() - 2);
          }
        }

        Log.d("ttt", "currency: " + currency);
        updateFirebaseAndSharedPreferences(countryCode.toUpperCase(), cityName, country, currency);

        Log.d("ttt", "from geocoder: " + country + countryCode.toLowerCase() + currency);

        if (destinationActivity != null) {

          context.startActivity(new Intent(context, destinationActivity));
          activity.finish();

        } else {
          startTargetActivity();
        }


      } else {
        fetchFromApi(location.getLatitude(), location.getLongitude());
      }
    } catch (IOException e) {
      stopLocationUpdates();
      fetchFromApi(location.getLatitude(), location.getLongitude());
      Log.d("ttt", "geocoder error:" + e.getLocalizedMessage());
    }

  }
//  void init() {
//
//    mRequestingLocationUpdates = true;
//
//    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
//
//
//    mLocationRequest = new LocationRequest();
//    mLocationRequest.setInterval(10000);
//    mLocationRequest.setFastestInterval(5000);
//    mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//
//    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//    builder.addLocationRequest(mLocationRequest);
//
//    Task<LocationSettingsResponse> task =
//            LocationServices.getSettingsClient(activity)
//                    .checkLocationSettings(builder.build());
//
//    task.addOnSuccessListener(locationSettingsResponse -> {
//      Log.d("ttt", "location is enabled");
//      startLocationUpdates();
//
//    }).addOnFailureListener(e -> {
//      if (e instanceof ResolvableApiException) {
//        Log.d("ttt", "location is not enabled");
//        try {
//          ResolvableApiException resolvable = (ResolvableApiException) e;
//          resolvable.startResolutionForResult(activity,
//                  REQUEST_CHECK_SETTINGS);
//
//        } catch (IntentSender.SendIntentException sendEx) {
//          // Ignore the error.
//        }
//      }
//    });
//
//
////    requestPermissions();
//  }

  void startTargetActivity() {

    if (activityBtnClicked == 0) {

      FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(authResult -> {
        context.startActivity(new Intent(context, HomeActivity.class));
        activity.finish();
      });

    } else {

      dismissProgressDialog();

      startActivity(activityBtnClicked);
    }
  }

  private void fetchFromApi(double latitude, double longitude) {

    final String url =
            "https://api.opencagedata.com/geocode/v1/json?key=078648c6ff684a8e851e63cbb1c8f6d8&q="
                    + latitude + "+" + longitude + "&pretty=1&no_annotations=1";

    final RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, response -> {
      try {
        if (response.getJSONObject("status").getString("message")
                .equalsIgnoreCase("ok")) {

          final JSONObject address = response.getJSONArray("results")
                  .getJSONObject(0).getJSONObject("components");

          Log.d("ttt", "address: " + address.toString());
          final String country = address.getString("country");
          final String countryCode = address.getString("country_code");

          String cityName = null;
          if (address.has("city")) {
            cityName = address.getString("city");
          } else if (address.has("region")) {
            cityName = address.getString("region");
          }

          Log.d("ttt", "country:+ " + country);
          Log.d("ttt", "code:+ " + countryCode);

          String currency = Currency.getInstance(new Locale("en", countryCode))
                  .getCurrencyCode();

          if (currency.contains(".")) {
            if (currency.substring(currency.length() - 2, currency.length() - 1).equals(".")) {
              currency = currency.substring(0, currency.length() - 2);
            }
          }

          Log.d("ttt", "currency: " + currency);
          updateFirebaseAndSharedPreferences(countryCode.toUpperCase(), cityName, country, currency);

          Log.d("ttt", "from api: " + country + countryCode + currency);

          if (destinationActivity != null) {

            context.startActivity(new Intent(context, destinationActivity));
            activity.finish();

          } else {
            startTargetActivity();
          }

        } else {
          Log.d("ttt", "error here man 3: " +
                  response.getJSONObject("status").getString("message"));
        }
      } catch (JSONException e) {
        Log.d("ttt", "error here man 1: " + e.getMessage());
        e.printStackTrace();
      }
    }, error -> {

      if (retries < 3) {
        retries++;

        fetchFromApi(latitude, longitude);
      } else {
        Toast.makeText(context, "حصلت مشكلة! حاول اعادة تشغيل التطبيق"
                , Toast.LENGTH_SHORT).show();
        ((Activity) context).finish();
      }

      Log.d("ttt", "error here man 2: " + error.getMessage());
    });
    queue.add(jsonObjectRequest);
    queue.start();
  }


  void startActivity(int path) {

    context.startActivity(new Intent(context,
            path == 1 ? SigninActivity.class : RegisterActivity.class));

    activity.finish();
  }


  private void updateFirebaseAndSharedPreferences(String countryCode, String cityName,
                                                  String country, String currency) {
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    GlobalVariables.getInstance().setCountryCode(countryCode);

    if (user != null) {
      FirebaseFirestore.getInstance().collection("users").
              document(user.getUid())
              .update("countryCode", countryCode,
                      "cityName", cityName)
              .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                  Log.d("ttt", "updating success");
                }
              }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          Log.d("ttt", "updating onFailure: " + e.getMessage());
        }
      });
    }

    activity.getSharedPreferences("rbeno", Context.MODE_PRIVATE).edit()
            .putString("country", country)
            .putString("currency", currency)
            .putString("countryCode", countryCode)
            .putString("cityName", cityName).apply();
  }

}

