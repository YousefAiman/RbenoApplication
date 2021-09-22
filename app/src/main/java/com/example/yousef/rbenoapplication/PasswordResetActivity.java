package com.example.yousef.rbenoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        final AdView adView = findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

        final Toolbar toolbar = findViewById(R.id.passwordResetToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        final EditText emailEd = findViewById(R.id.emailEd);
        findViewById(R.id.resetPasswordBtn).setOnClickListener(v -> {
            if (!emailEd.getText().toString().isEmpty()) {
                if (WifiUtil.checkWifiConnection(this)) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(emailEd.getText().toString()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PasswordResetActivity.this, "اضغط على الرابط الذي تم ارساله الى بريدك لإعادة تعيين كلمة المرور!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(PasswordResetActivity.this, SigninActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(e -> Toast.makeText(PasswordResetActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(PasswordResetActivity.this, "أدخل ايميلك في الحقل!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
