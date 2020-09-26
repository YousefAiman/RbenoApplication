package com.example.yousef.rbenoapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
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
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class SigninActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;
    CollectionReference userRef;
    CallbackManager callbackManager;
    //LoginButton facebookLoginButton;
    TwitterLoginButton twitterLoginButton;
    ProgressDialog dialog;
    CheckBox rememberBox;
    String currentDocumentId;
    EditText emailed;
    EditText passwordEd;
    Button loginButton;
    SignInButton googleSigninBtn;
    ImageView googleImage;
    GoogleSignInClient googleSignInClient;
    String country, currency;
//            , provider;
//    double latitude, longitude;
//    FusedLocationProviderClient mFusedLocationClient;
//    Location location1;
//    LocationRequest locationRequest;
//    LocationCallback mLocationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_signin);

        emailed = findViewById(R.id.emailEd);
        passwordEd = findViewById(R.id.passwordEd);
        loginButton = findViewById(R.id.signintoAccountBtn);
        googleSigninBtn = findViewById(R.id.googleSigninBtn);
        googleImage = findViewById(R.id.googleImage);

        country = GlobalVariables.getCountry();
        currency = GlobalVariables.getCurrency();

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseFirestore.getInstance().collection("users");

        twitterLoginButton = findViewById(R.id.twitterLoginButton);
        ImageView twitterImage = findViewById(R.id.twitterImage);

        twitterImage.setOnClickListener(view -> {
            firebaseAuthWithTwitter();
        });


        googleImage.setOnClickListener(v -> googleSignIn());

        TextView forgotpasswordTv = findViewById(R.id.forgotpasswordTv);
        SpannableString forgotContent = new SpannableString(getString(R.string.forgotPassword));
        forgotContent.setSpan(new UnderlineSpan(), 0, forgotContent.length(), 0);
        forgotpasswordTv.setText(forgotContent);
        forgotpasswordTv.setOnClickListener(v -> startActivity(new Intent(SigninActivity.this, PasswordResetActivity.class)));

        TextView registerTv = findViewById(R.id.registerTv);
        SpannableString registerContent = new SpannableString(getString(R.string.dontHaveAnAccount));
        registerContent.setSpan(new UnderlineSpan(), 0, registerContent.length(), 0);
        registerTv.setText(registerContent);
        registerTv.setOnClickListener(v -> {
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
                dialog = ProgressDialog.show(SigninActivity.this, "جاري تسجيل الدخول",
                        "الرجاء الإنتظار!", true);
                loginButton.setClickable(false);
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.isComplete()) {
                        if (auth.getCurrentUser().isEmailVerified()) {
                            userRef.whereEqualTo("email", email).get().addOnCompleteListener(task14 -> {
                                currentDocumentId = Objects.requireNonNull(task14.getResult()).getDocuments().get(0).getId();
                                userRef.document(currentDocumentId).update("remembered", rememberBox.isChecked()).addOnSuccessListener(aVoid -> {
                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                    finish();
                                    Toast.makeText(SigninActivity.this, "تم تسجيل الدخول!", Toast.LENGTH_SHORT).show();
                                });
                            });
                        } else {
                            auth.signOut();
                            dialog.dismiss();
                            Toast.makeText(SigninActivity.this, "الرجاء تفعيل الحساب عن طريق الضغط على الرابط في ايميلك!", Toast.LENGTH_SHORT).show();
                            loginButton.setClickable(true);
                        }
                    }
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    loginButton.setClickable(true);
                    Toast.makeText(SigninActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(SigninActivity.this, "الرجاء تعبئة البريد و كلمة السر!", Toast.LENGTH_SHORT).show();
            }
        });


        ImageView facebookImage = findViewById(R.id.facebookLogin);
        final LoginButton facebookLoginBtn = findViewById(R.id.facebookImage);
        facebookImage.setOnClickListener(view -> {
            FacebookSdk.fullyInitialize();
            callbackManager = CallbackManager.Factory.create();
            facebookLoginBtn.setReadPermissions("email", "public_profile");
            facebookLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    dialog = ProgressDialog.show(SigninActivity.this, "جاري التسجيل باستخدام حساب فيسبوك", "الرجاء الإنتظار!", true);
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
            facebookLoginBtn.performClick();
        });

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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void googleSignIn() {

        dialog = ProgressDialog.show(SigninActivity.this, "جاري التسجيل باستخدام حساب جوجل",
                "الرجاء الإنتظار!", true);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id))
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
                userRef.whereEqualTo("userId", facebookUser.getUid()).get().addOnCompleteListener(task12 -> {
                    if (task12.getResult().getDocuments().isEmpty()) {
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
                        dialog.dismiss();
                        Toast.makeText(SigninActivity.this, "signing in ", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SigninActivity.this, HomeActivity.class));
                        finish();
                    }
                });
            }).addOnFailureListener(e -> Toast.makeText(SigninActivity.this, "لقد فشلت عملية تسجيل الدخول:" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
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
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("ttt", "signInWithCredential:success");
                        addUserToFirestore(account.getEmail(), account.getDisplayName(),
                                account.getPhotoUrl().toString(),
                                auth.getCurrentUser().getUid());
                    } else {
                        dialog.dismiss();
                        Toast.makeText(SigninActivity.this, "لقد فشلت عملية التسجيل عن طريق حساب فيسبوك!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void firebaseAuthWithTwitter() {

        dialog = ProgressDialog.show(SigninActivity.this, "جاري التسجيل باستخدام حساب تويتر",
                "الرجاء الإنتظار!", true);

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("7a1GjxWZ2Hiad99jkByjuiq1E", "FKCOIBBm49mKvPFWRuYfD1gfuT4p5aJovAdAvEJ96Bvfx61s86"))
                .debug(true)
                .build();
        Twitter.initialize(config);

        final OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
        Task<AuthResult> pendingResultTask = auth.getPendingAuthResult();
        if (pendingResultTask != null) {
            pendingResultTask.addOnSuccessListener(authResult -> {
                startActivity(new Intent(SigninActivity.this, HomeActivity.class));
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(SigninActivity.this, "user not signed", Toast.LENGTH_SHORT).show();
                Log.d("ttt", e.getMessage());
            });
        } else {
            auth.startActivityForSignInWithProvider(SigninActivity.this, provider.build()).addOnSuccessListener(authResult -> {
                Log.d("ttt", "USER SIGNED IN WITH TWITTER");
                Toast.makeText(SigninActivity.this, "user signed in with twitter", Toast.LENGTH_SHORT).show();
                final FirebaseUser twitterAuthUser = auth.getCurrentUser();
                addUserToFirestore((String) authResult.getAdditionalUserInfo().getProfile().get("email"),
                        twitterAuthUser.getDisplayName(),
                        twitterAuthUser.getPhotoUrl().toString(),
                        twitterAuthUser.getUid());
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(SigninActivity.this, "user twitter failed to sign in: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    void addUserToFirestore(String email, String username, String photoUrl, String userId) {

        userRef.whereEqualTo("userId", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("email", email);
                userInfo.put("username", username);
                userInfo.put("userId", userId);
                userInfo.put("staticusername", "@" + username.toLowerCase().trim());
                userInfo.put("favpromosids", new ArrayList<>());
                userInfo.put("status", true);
                userInfo.put("remembered", true);
                userInfo.put("country", country);
                userInfo.put("currency", currency);
                userInfo.put("reports", new ArrayList<>());
                userInfo.put("usersBlocked", new ArrayList<>());
                if (photoUrl != null) {
                    userInfo.put("imageurl", photoUrl);
                } else {
                    userInfo.put("imageurl", "");
                }

                userRef.add(userInfo).addOnSuccessListener(task1 -> {
                    dialog.dismiss();
                    Toast.makeText(SigninActivity.this, "لقد نجحت عملية التسجيل!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SigninActivity.this, HomeActivity.class));
                    finish();
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(SigninActivity.this, "لقد فشلت عملية تسجيل الدخول:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    auth.signOut();
                });
            } else {
                startActivity(new Intent(SigninActivity.this, HomeActivity.class));
                finish();
            }
        });
    }

}
