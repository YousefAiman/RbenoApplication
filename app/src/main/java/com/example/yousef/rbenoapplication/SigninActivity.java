package com.example.yousef.rbenoapplication;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SigninActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    CollectionReference userRef;
    CallbackManager callbackManager;
    TwitterLoginButton twitterLoginButton;
    ProgressDialog dialog;
    CheckBox rememberBox;
    EditText emailed, passwordEd;
    Button loginButton;
    SignInButton googleSigninBtn;
    ImageView googleImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_signin);

        emailed = findViewById(R.id.emailEd);
        if (getIntent().hasExtra("email")) {
            emailed.setText(getIntent().getStringExtra("email"));
        }
        passwordEd = findViewById(R.id.passwordEd);
        loginButton = findViewById(R.id.signintoAccountBtn);
        googleSigninBtn = findViewById(R.id.googleSigninBtn);
        googleImage = findViewById(R.id.googleImage);

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseFirestore.getInstance().collection("users");

        twitterLoginButton = findViewById(R.id.twitterLoginButton);
        ImageView twitterImage = findViewById(R.id.twitterImage);

        twitterImage.setOnClickListener(view -> {
            if (WifiUtil.checkWifiConnection(this)) {
                firebaseAuthWithTwitter();
            }
        });


        googleImage.setOnClickListener(v -> {
            if (WifiUtil.checkWifiConnection(this)) {
                googleSignIn();
            }
        });

        TextView forgotpasswordTv = findViewById(R.id.forgotpasswordTv);
        setSpan(forgotpasswordTv);
        forgotpasswordTv.setOnClickListener(v ->
                startActivity(new Intent(
                        SigninActivity.this, PasswordResetActivity.class)));

        TextView registerTv = findViewById(R.id.registerTv);
        setSpan(registerTv);

        TextView registerTv2 = findViewById(R.id.registerTv2);
        setSpan(registerTv2);

        findViewById(R.id.registerLinear).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            finish();
        });
        user = auth.getCurrentUser();

        rememberBox = findViewById(R.id.rememberBox);
        rememberBox.setChecked(true);

        loginButton.setOnClickListener(v -> {

            final String email = emailed.getText().toString().trim();
            String password = passwordEd.getText().toString().trim();
            if (!password.isEmpty() && !email.isEmpty()) {
                if (WifiUtil.checkWifiConnection(this)) {
                    dialog = ProgressDialog.show(SigninActivity.this, "جاري تسجيل الدخول",
                            "الرجاء الإنتظار!", true);
                    loginButton.setClickable(false);
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.isComplete()) {
                            if (auth.getCurrentUser().isEmailVerified()) {

                                updateUserInfoAndStartActivity(userRef.document(auth.getCurrentUser().getUid())
                                );

//                userRef.whereEqualTo("email", email).get().addOnCompleteListener(task14 -> {
//
//                  final DocumentReference userRef = task14.getResult()
//                          .getDocuments().get(0).getReference();
//
//                  FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s ->
//                          userRef.update("token", s));
//
//                  final SharedPreferences sharedPreferences
//                          = getSharedPreferences("rbeno", Context.MODE_PRIVATE);
//
//                  String countryCode = null;
//                  String cityName = null;
//
//                  if(sharedPreferences.contains("countryCode")){
//                    countryCode = sharedPreferences.getString("countryCode",null);
//                  }
//
//                  if(sharedPreferences.contains("cityName")){
//                    cityName = sharedPreferences.getString("cityName",null);
//                  }
//
//                  userRef.update("remembered", rememberBox.isChecked(),
//                          "countryCode",countryCode,
//                          "cityName",cityName)
//                          .addOnSuccessListener(aVoid -> {
//
//                    startMessagingService();
//                    dialog.dismiss();
//
//                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
//                    finish();
//                  });
//                });
                            } else {
                                auth.signOut();
                                dialog.dismiss();
                                Toast.makeText(SigninActivity.this,
                                        "الرجاء تفعيل الحساب عن طريق الضغط على الرابط في ايميلك!",
                                        Toast.LENGTH_SHORT).show();
                                loginButton.setClickable(true);
                            }
                        }
                    }).addOnFailureListener(e -> {
                        dialog.dismiss();
                        loginButton.setClickable(true);
                        Toast.makeText(SigninActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                Toast.makeText(SigninActivity.this,
                        "الرجاء تعبئة البريد و كلمة السر!", Toast.LENGTH_SHORT).show();
            }
        });

        LoginButton facebookBtn = findViewById(R.id.facebookBtn);
        facebookBtn.setOnClickListener(view -> {
            FacebookSdk.fullyInitialize();
            callbackManager = CallbackManager.Factory.create();
            facebookBtn.setReadPermissions("email", "public_profile");
            facebookBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    dialog = ProgressDialog.show(SigninActivity.this,
                            "جاري التسجيل باستخدام حساب فيسبوك",
                            "الرجاء الإنتظار!", true);
                    Log.d("ttt", "facebook:onSuccess:" + loginResult);
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.d("ttt", "facebook:onCancel");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d("ttt", "facebook:onError", error);
                }
            });
//      facebookBtn.performClick();
        });
        findViewById(R.id.facebookImage).setOnClickListener(v -> {
            if (WifiUtil.checkWifiConnection(this)) {
                facebookBtn.performClick();
            }
        });
    }

    void startMessagingService() {

        startService(new Intent(SigninActivity.this, MyFirebaseMessaging.class));

        getApplicationContext().getPackageManager().setComponentEnabledSetting(
                new ComponentName(this.getApplicationContext(), MyFirebaseMessaging.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            task.addOnSuccessListener(googleSignInAccount -> {
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    dialog.dismiss();

                    Log.w("ttt", "Google sign in failed" + e.getMessage());
                }
            }).addOnFailureListener(e -> dialog.dismiss());

        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

//  @Override
//  public void onBackPressed() {
//    moveTaskToBack(true);
//  }

    public void googleSignIn() {

        dialog = ProgressDialog.show(SigninActivity.this, "جاري التسجيل باستخدام حساب جوجل",
                "الرجاء الإنتظار!", true);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        Intent googleIntent = GoogleSignIn.getClient(this, gso).getSignInIntent();
        startActivityForResult(googleIntent, 0);
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        FacebookSdk.setAutoInitEnabled(true);
        new GraphRequest();
        GraphRequest graphRequest = GraphRequest.newMeRequest(token, (object, response) -> {
            AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
            auth.signInWithCredential(credential).addOnSuccessListener(authResult -> {

                final FirebaseUser facebookUser = auth.getCurrentUser();
                userRef.document(facebookUser.getUid()).get().addOnCompleteListener(task12 -> {
                    if (!task12.getResult().exists()) {
                        String email = "";
                        if (object.has("email")) {
                            try {
                                email = object.getString("email");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        addUserToFirestore(email, facebookUser.getDisplayName(),
                                facebookUser.getPhotoUrl().toString(),
                                facebookUser.getUid());
                    } else {
                        updateUserInfoAndStartActivity(userRef.document(facebookUser.getUid()));
                    }
                });
            }).addOnFailureListener(e -> Toast.makeText(SigninActivity.this,
                    "لقد فشلت عملية تسجيل الدخول:"
                            + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();

    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        Log.d("ttt", "firebaseAuthWithGoogle:" + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    Log.d("ttt", "signInWithCredential:success");

                    if (authResult.getAdditionalUserInfo().isNewUser()) {
                        addUserToFirestore(account.getEmail(), account.getDisplayName(),
                                account.getPhotoUrl().toString(),
                                auth.getCurrentUser().getUid());
                    } else {

                        updateUserInfoAndStartActivity(userRef.document(auth.getCurrentUser().getUid()));

//                userRef.whereEqualTo("email", account.getEmail())
//                        .get().addOnSuccessListener(snapshots -> {
//
//                          final DocumentReference userRef = snapshots.getDocuments().get(0).getReference();
//
//                          FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
//                            Log.d("ttt", "token: " + s);
//                            userRef.update("token", s);
//                          });
//
//                  final SharedPreferences sharedPreferences
//                          = getSharedPreferences("rbeno", Context.MODE_PRIVATE);
//
//                  String countryCode = null;
//                  String cityName = null;
//
//                  if(sharedPreferences.contains("countryCode")){
//                    countryCode = sharedPreferences.getString("countryCode",null);
//                  }
//
//                  if(sharedPreferences.contains("cityName")){
//                    cityName = sharedPreferences.getString("cityName",null);
//                  }
//
//
//                          userRef.update("remembered", rememberBox.isChecked(),
//                                  "country", GlobalVariables.getCountry(),
//                                  "currency", GlobalVariables.getCurrency(),
//                                  "countryCode", GlobalVariables.getCountryCode())
//                                  .addOnSuccessListener(aVoid -> {
//
//                              dialog.dismiss();
//                            startMessagingService();
////                                    //registerNotificationClickReceiver();
////                             NotificationClickReceiver.registerReceiver(this);
//                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
//                            finish();
//                          }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                              dialog.dismiss();
//                            }
//                          });

//                        }).addOnFailureListener(new OnFailureListener() {
//                  @Override
//                  public void onFailure(@NonNull Exception e) {
//                    dialog.dismiss();
//                  }
//                });


                    }

                }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(SigninActivity.this, "لقد فشلت عملية التسجيل عن طريق حساب gmail!"
                    , Toast.LENGTH_SHORT).show();
        });
    }

    public void firebaseAuthWithTwitter() {

        dialog = ProgressDialog.show(SigninActivity.this,
                "جاري التسجيل باستخدام حساب تويتر",
                "الرجاء الإنتظار!", true);

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(
                        "7a1GjxWZ2Hiad99jkByjuiq1E",
                        "FKCOIBBm49mKvPFWRuYfD1gfuT4p5aJovAdAvEJ96Bvfx61s86"))
                .debug(true)
                .build();

        Twitter.initialize(config);

        final OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
        final Task<AuthResult> pendingResultTask = auth.getPendingAuthResult();

        if (pendingResultTask != null) {
            Log.d("ttt", "user already exists");
            pendingResultTask.addOnSuccessListener(authResult -> {

                final FirebaseUser twitterAuthUser = auth.getCurrentUser();

                if (authResult.getAdditionalUserInfo().isNewUser()) {

                    Log.d("ttt", "new user already signing in");

                    addUserToFirestore((String) authResult.getAdditionalUserInfo()
                                    .getProfile().get("email"),
                            twitterAuthUser.getDisplayName(),
                            twitterAuthUser.getPhotoUrl().toString().replace(
                                    "_normal",
                                    ""),
                            twitterAuthUser.getUid());

                } else {
                    Log.d("ttt", "old user already signing in");

                    updateUserInfoAndStartActivity(userRef.document(twitterAuthUser.getUid())
                    );

                }
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Log.d("ttt", "failed twitter sign in: " + e.toString());
            });

//      pendingResultTask.addOnSuccessListener(authResult ->
//
//      userRef.whereEqualTo("email",
//              authResult.getAdditionalUserInfo().getProfile().get("email"))
//              .get().addOnSuccessListener(snapshots ->{
//                Log.d("ttt", "already signed in");
//                final DocumentReference userRef = snapshots.getDocuments().get(0).getReference();
//
//                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s ->
//                        userRef.update("token",s));
//
//                userRef.update("remembered", rememberBox.isChecked(),
//                        "country",GlobalVariables.getCountry(),
//                        "currency",GlobalVariables.getCurrency(),
//                        "countryCode",GlobalVariables.getCountryCode())
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                  @Override
//                  public void onSuccess(Void aVoid) {
//                    Toast.makeText(SigninActivity.this, "تم تسجيل الدخول!",
//                            Toast.LENGTH_SHORT).show();
//                    startMessagingService();
//                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
//                    finish();
//                  }
//                });
//              }))
//              .addOnFailureListener(e -> {
//        Log.d("ttt", e.getMessage());
//      });

        } else {
            auth.startActivityForSignInWithProvider(SigninActivity.this, provider.build())
                    .addOnSuccessListener(authResult -> {


                        if (authResult.getAdditionalUserInfo().isNewUser()) {

                            Log.d("ttt", "new user signed in");
                            final FirebaseUser twitterAuthUser = auth.getCurrentUser();

                            addUserToFirestore((String) authResult.getAdditionalUserInfo()
                                            .getProfile().get("email"),
                                    twitterAuthUser.getDisplayName(),
                                    twitterAuthUser.getPhotoUrl().toString().replace(
                                            "_normal",
                                            ""),
                                    twitterAuthUser.getUid());

                        } else {

                            updateUserInfoAndStartActivity(userRef.document(authResult.getUser().getUid())
                            );

                        }

                    }).addOnFailureListener(e -> {
                dialog.dismiss();

                Toast.makeText(this, " لقد فشل التسجيل عن طريق تويتر!"
                        , Toast.LENGTH_SHORT).show();
            });
        }
    }


    void addUserToFirestore(String email, String username, String photoUrl, String userId) {

        final Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", email);
        userInfo.put("username", username);
        userInfo.put("userId", userId);
        userInfo.put("staticusername", "@" + username.toLowerCase().trim());
        userInfo.put("favpromosids", new ArrayList<>());
        userInfo.put("status", true);
        userInfo.put("remembered", true);

        final SharedPreferences sharedPreferences
                = getSharedPreferences("rbeno", Context.MODE_PRIVATE);

        String cityName = null;

        if (sharedPreferences.contains("cityName")) {
            cityName = sharedPreferences.getString("cityName", null);
        }

        userInfo.put("countryCode", GlobalVariables.getInstance().getCountryCode());
        userInfo.put("cityName", cityName);
        userInfo.put("reports", new ArrayList<>());
        userInfo.put("usersBlocked", new ArrayList<>());
        if (photoUrl != null) {
            userInfo.put("imageurl", photoUrl);
        } else {
            userInfo.put("imageurl", "");
        }

        userRef.document(userId).set(userInfo).addOnSuccessListener(task1 -> {
            dialog.dismiss();
            startMessagingService();

            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s ->
                    userRef.document(userId).update("token", s));

            startActivity(new Intent(SigninActivity.this, HomeActivity.class));
            finish();
        }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(SigninActivity.this, "لقد فشلت عملية تسجيل الدخول:" +
                    e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            auth.signOut();
        });

    }

    void setSpan(TextView textView) {
        SpannableString s = new SpannableString(textView.getText());
        s.setSpan(new UnderlineSpan(), 0, s.length(), Spanned.SPAN_MARK_POINT);
        textView.setText(s);
    }

//  void registerNotificationClickReceiver(){
//    final IntentFilter intentFilter = new IntentFilter();
//    intentFilter.addAction("com.example.yousef.rbenoapplication.notificationClick");
//    registerReceiver(new NotificationClickReceiver(), intentFilter);
//  }


    private void updateUserInfoAndStartActivity(DocumentReference userRef) {

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s ->
                userRef.update("token", s));

        final SharedPreferences sharedPreferences
                = getSharedPreferences("rbeno", Context.MODE_PRIVATE);

        String countryCode = null;
        String cityName = null;

        if (sharedPreferences.contains("countryCode")) {
            countryCode = sharedPreferences.getString("countryCode", null);
        }

        if (sharedPreferences.contains("cityName")) {
            cityName = sharedPreferences.getString("cityName", null);
        }

        Log.d("ttt", "countryCode: " + countryCode + " ,cityName: " + cityName);

        userRef.update("remembered", rememberBox.isChecked(),
                "countryCode", countryCode,
                "cityName", cityName)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ttt", "update success going to home acitivity");
                    dialog.dismiss();
                    startMessagingService();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                });
    }
}
