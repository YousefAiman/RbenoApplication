package com.example.yousef.rbenoapplication;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.Button;
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

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
  FirebaseAuth auth;
  FirebaseFirestore firestore;
  CollectionReference userRefrence;
  ProgressDialog dialog;
  SignInButton googleSigninBtn;
  CallbackManager callbackManager;
  FirebaseUser user;
  Button signUpButton;
  ImageView googleImage, facebookImage, twitterImage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    auth = FirebaseAuth.getInstance();
    user = auth.getCurrentUser();
    firestore = FirebaseFirestore.getInstance();
    userRefrence = firestore.collection("users");


    googleSigninBtn = findViewById(R.id.googleSigninBtn);
    googleImage = findViewById(R.id.googleImage);

    TextView registerTv = findViewById(R.id.registerTv);
    setSpan(registerTv);

    TextView registerTv2 = findViewById(R.id.registerTv2);
    setSpan(registerTv2);

    twitterImage = findViewById(R.id.twitterImage);
    facebookImage = findViewById(R.id.facebookImage);

    findViewById(R.id.registerLinear).setOnClickListener(v -> {
      startActivity(new Intent(getApplicationContext(), SigninActivity.class));
      finish();
    });

    final EditText emailEd = findViewById(R.id.emailRegisterEd);
    final EditText passwordEd = findViewById(R.id.passwordRegisterEd);
    final EditText passwordEd2 = findViewById(R.id.passwordRegisterEd2);
    final EditText usernameEd = findViewById(R.id.usernameRegisterEd);
    signUpButton = findViewById(R.id.registerBtn);


    signUpButton.setOnClickListener(v -> {
      final String email = emailEd.getText().toString().trim();
      final String password = passwordEd.getText().toString().trim();
      final String password2 = passwordEd2.getText().toString().trim();
      final String username = usernameEd.getText().toString().trim();

      if (!password.equals("") &&
              !password.isEmpty() &&
              password.equals(password2) &&
              !email.equals("") &&
              !email.isEmpty() &&
              !username.equals("") &&
              !username.isEmpty()) {

        if (WifiUtil.checkWifiConnection(this)) {
          dialog = ProgressDialog.show(RegisterActivity.this, "جاري إنشاء الحساب",
                  "الرجاء الإنتظار!", true);
          auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {

            addUserToFirestore(email, username, "",
                    auth.getCurrentUser().getUid());

          }).addOnFailureListener(e -> {
            Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(),
                    Toast.LENGTH_SHORT).show();
            dialog.dismiss();
          });
        }

      } else if (!password.equals(password2)) {
        Toast.makeText(RegisterActivity.this, "كلمة السر غير متطابقة!",
                Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(RegisterActivity.this, "الرجاء تعبئة البريد و كلمة السر!",
                Toast.LENGTH_SHORT).show();
      }
    });


    googleImage.setOnClickListener(view -> {

      if (WifiUtil.checkWifiConnection(this)) {
        googleSignIn();

      }
    });

    twitterImage.setOnClickListener(v -> {
      if (WifiUtil.checkWifiConnection(this)) {
        firebaseAuthWithTwitter();
      }
    });

    final LoginButton facebookBtn = findViewById(R.id.facebookBtn);
    facebookBtn.setOnClickListener(view -> {


      FacebookSdk.fullyInitialize();
      callbackManager = CallbackManager.Factory.create();
      facebookBtn.setReadPermissions("email", "public_profile");
      facebookBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
          dialog = ProgressDialog.show(RegisterActivity.this,
                  "جاري التسجيل باستخدام حساب فيسبوك"
                  , "الرجاء الإنتظار!", true);
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
    });

    facebookImage.setOnClickListener(v -> {
      if (WifiUtil.checkWifiConnection(this)) {
        facebookBtn.performClick();
      }
    });
  }

//  @Override
//  public void onBackPressed() {
//    moveTaskToBack(true);
//  }

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
                updateUserInfoAndStartActivity(userRefrence.document(auth.getCurrentUser().getUid()));
              }

            }).addOnFailureListener(e -> {
      dialog.dismiss();
      Toast.makeText(RegisterActivity.this,
              "لقد فشلت عملية التسجيل عن طريق حساب gmail!"
              , Toast.LENGTH_SHORT).show();
    });
  }


  public void googleSignIn() {

    dialog = ProgressDialog.show(RegisterActivity.this, "جاري التسجيل باستخدام حساب جوجل",
            "الرجاء الإنتظار!", true);
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder
            (GoogleSignInOptions.DEFAULT_SIGN_IN)
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
        userRefrence.document(facebookUser.getUid()).get().addOnCompleteListener(task12 -> {
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
            updateUserInfoAndStartActivity(userRefrence.document(facebookUser.getUid()));
          }
        });
      }).addOnFailureListener(e -> Toast.makeText(RegisterActivity.this,
              "لقد فشلت عملية تسجيل الدخول:"
                      + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    });

    Bundle parameters = new Bundle();
    parameters.putString("fields", "email");
    graphRequest.setParameters(parameters);
    graphRequest.executeAsync();

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 0) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        GoogleSignInAccount account = task.getResult(ApiException.class);
        firebaseAuthWithGoogle(account);
      } catch (ApiException e) {
        dialog.dismiss();
        Log.w("ttt", "Google sign in failed" + e.getMessage());
      }
    } else {
      callbackManager.onActivityResult(requestCode, resultCode, data);
    }
  }

  public void firebaseAuthWithTwitter() {

    dialog = ProgressDialog.show(RegisterActivity.this,
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

          updateUserInfoAndStartActivity(userRefrence.document(twitterAuthUser.getUid()));

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
      auth.startActivityForSignInWithProvider(RegisterActivity.this, provider.build())
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

                  updateUserInfoAndStartActivity(userRefrence.document(authResult.getUser().getUid())
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

    userRefrence.document(userId).set(userInfo).addOnSuccessListener(task1 -> {
      dialog.dismiss();
      startMessagingService();

      FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s ->
              userRefrence.document(userId).update("token", s));

      startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
      finish();
    }).addOnFailureListener(error -> {
      dialog.dismiss();
      Toast.makeText(RegisterActivity.this, "لقد فشلت عملية تسجيل الدخول:" +
              error, Toast.LENGTH_SHORT).show();
      auth.signOut();
    });

  }


  void startMessagingService() {

    startService(new Intent(RegisterActivity.this, MyFirebaseMessaging.class));

    getApplicationContext().getPackageManager().setComponentEnabledSetting(
            new ComponentName(this.getApplicationContext(), MyFirebaseMessaging.class),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);

  }

  void setSpan(TextView textView) {
    SpannableString s = new SpannableString(textView.getText());
    s.setSpan(new UnderlineSpan(), 0, s.length(), 0);
    textView.setText(s);
  }

  private void updateUserInfoAndStartActivity(DocumentReference userRef) {

    FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s ->
            userRef.update("token", s));

    final SharedPreferences sharedPreferences
            = getSharedPreferences("rbeno", Context.MODE_PRIVATE);

    String cityName = null;

    if (sharedPreferences.contains("cityName")) {
      cityName = sharedPreferences.getString("cityName", null);
    }

    userRef.update("countryCode", GlobalVariables.getInstance().getCountryCode(), "cityName", cityName)
            .addOnSuccessListener(aVoid -> {

              dialog.dismiss();
              startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
              finish();
            });
  }

}
