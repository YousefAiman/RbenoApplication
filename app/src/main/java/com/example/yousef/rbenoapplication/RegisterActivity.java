package com.example.yousef.rbenoapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    String country, currency;
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

        country = GlobalVariables.getCountry();
        currency = GlobalVariables.getCurrency();

        googleSigninBtn = findViewById(R.id.googleSigninBtn);
        googleImage = findViewById(R.id.googleImage);
        TextView signinTv = findViewById(R.id.signinTv);
        String string = getString(R.string.haveAnAccount);
        SpannableString registerContent = new SpannableString(string);
        registerContent.setSpan(new UnderlineSpan(), 0, registerContent.length(), 0);
        signinTv.setText(registerContent);
        twitterImage = findViewById(R.id.twitterImage);
        facebookImage = findViewById(R.id.facebookLogin);

        final EditText emailEd = findViewById(R.id.emailRegisterEd);
        final EditText passwordEd = findViewById(R.id.passwordRegisterEd);
        final EditText passwordEd2 = findViewById(R.id.passwordRegisterEd2);
        final EditText usernameEd = findViewById(R.id.usernameRegisterEd);
        signUpButton = findViewById(R.id.registerTv2);

        signinTv.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SigninActivity.class));
            finish();
        });

        signUpButton.setOnClickListener(v -> {
            String email = emailEd.getText().toString().trim();
            String password = passwordEd.getText().toString().trim();
            String password2 = passwordEd2.getText().toString().trim();
            String username = usernameEd.getText().toString().trim();

            if (!password.equals("") && !password.isEmpty() && password.equals(password2) && !email.equals("") && !email.isEmpty() && !username.equals("") && !username.isEmpty()) {
                dialog = ProgressDialog.show(RegisterActivity.this, "جاري إنشاء الحساب",
                        "الرجاء الإنتظار!", true);
                auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                    addUserToFirestore(email, username,
                            "",
                            auth.getCurrentUser().getUid(), true);
                }).addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            } else if (!password.equals(password2)) {
                Toast.makeText(RegisterActivity.this, "كلمة السر غير متطابقة!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "الرجاء تعبئة البريد و كلمة السر!", Toast.LENGTH_SHORT).show();
            }
        });


        googleImage.setOnClickListener(view -> googleSignIn());

        twitterImage.setOnClickListener(v -> loginWithTwitter());

        final LoginButton facebookLoginBtn = findViewById(R.id.facebookImage);
        facebookImage.setOnClickListener(view -> {
            FacebookSdk.fullyInitialize();
            callbackManager = CallbackManager.Factory.create();
            facebookLoginBtn.setReadPermissions("email", "public_profile");
            facebookLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    dialog = ProgressDialog.show(RegisterActivity.this, "جاري التسجيل باستخدام حساب فيسبوك", "الرجاء الإنتظار!", true);
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
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("ttt", "signInWithCredential:success");
                        addUserToFirestore(account.getEmail(), account.getDisplayName(),
                                account.getPhotoUrl().toString(),
                                auth.getCurrentUser().getUid(), false);
                    } else {
                        dialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "لقد فشلت عملية التسجيل عن طريق حساب فيسبوك!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void googleSignIn() {

        dialog = ProgressDialog.show(RegisterActivity.this, "جاري التسجيل باستخدام حساب جوجل",
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
            auth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {

                    final FirebaseUser facebookUser = auth.getCurrentUser();
                    userRefrence.whereEqualTo("userId", facebookUser.getUid()).get().addOnCompleteListener(task12 -> {
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
                                    facebookUser.getUid(), false);
                        } else {
                            dialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "signing in ", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            finish();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegisterActivity.this, "لقد فشلت عملية تسجيل الدخول:" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
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

    void loginWithTwitter() {
        final OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
        provider.addCustomParameter("lang", Locale.getDefault().getLanguage());

        Task<AuthResult> pendingResultTask = auth.getPendingAuthResult();
        if (pendingResultTask != null) {
            pendingResultTask.addOnSuccessListener(authResult -> {
                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                finish();
            }).addOnFailureListener(e -> Log.d("ttt", "Error in Twitter Login" + e.getMessage()));
        } else {
            auth.startActivityForSignInWithProvider(RegisterActivity.this, provider.build()).addOnSuccessListener(authResult -> {
                Log.d("ttt", "USER SIGNED IN WITH TWITTER");
                Toast.makeText(this, "signied in twitter", Toast.LENGTH_SHORT).show();
                FirebaseUser twitterAuthUser = auth.getCurrentUser();
                addUserToFirestore((String) authResult.getAdditionalUserInfo().getProfile().get("email"),
                        twitterAuthUser.getDisplayName(),
                        twitterAuthUser.getPhotoUrl().toString(),
                        twitterAuthUser.getUid(), false);
            }).addOnFailureListener(e -> Log.d("ttt", "Twitter Signin Failed" + e.getMessage() + "   -   " + e.getCause()));
        }
    }

    void addUserToFirestore(String email, String username, String photoUrl, String userId, boolean isNewUser) {

        userRefrence.whereEqualTo("userId", userId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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

                    userRefrence.add(userInfo).addOnSuccessListener(task1 -> {
                        if (isNewUser) {
                            auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(aVoid -> {
                                auth.signOut();
                                dialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), SigninActivity.class));
                                Toast.makeText(RegisterActivity.this, "تم إنشاء الحساب! قم بتفعيل الحساب من خلال الضغط على الرابط في بريدك", Toast.LENGTH_LONG).show();
                                finish();
                            }).addOnFailureListener(e -> {
                                auth.signOut();
                                dialog.dismiss();
                                Log.d("ttt", e.getLocalizedMessage());
                            });
                        } else {
                            dialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "لقد نجحت عملية التسجيل عن طريق حساب فيسبوك!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "لقد فشلت عملية تسجيل الدخول:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        auth.signOut();
                    });
                } else {
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    finish();
                }
            }
        });
    }
}
