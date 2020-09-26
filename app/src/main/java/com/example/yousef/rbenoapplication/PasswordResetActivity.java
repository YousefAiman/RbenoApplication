package com.example.yousef.rbenoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        ((TextView) findViewById(R.id.toolbarTitleTv)).setText("إعاد تعيين  كلمة المرور");

        EditText emailEd = findViewById(R.id.emailEd);
        findViewById(R.id.resetPasswordBtn).setOnClickListener(v -> {
            if (!emailEd.getText().toString().isEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailEd.getText().toString()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PasswordResetActivity.this, "اضغط على الرابط الذي تم ارساله الى بريدك لإعادة تعيين كلمة المرور!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(PasswordResetActivity.this, SigninActivity.class));
                    }
                }).addOnFailureListener(e -> Toast.makeText(PasswordResetActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(PasswordResetActivity.this, "أدخل ايميلك في الحقل!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
