package com.example.yousef.rbenoapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        ((Toolbar) findViewById(R.id.passwordToolbar)).setNavigationOnClickListener(view -> finish());

        final EditText currentPasswordEd = findViewById(R.id.currentPasswordEd);
        final EditText newPasswordEd = findViewById(R.id.newPasswordEd);
        final EditText confirmPasswordEd = findViewById(R.id.confirmPasswordEd);

        findViewById(R.id.changePasswordBtn).setOnClickListener(v -> {

            String currentPassword = currentPasswordEd.getText().toString();
            String newPassword = newPasswordEd.getText().toString();
            String confirmPassword = confirmPasswordEd.getText().toString();

            if (!currentPassword.equals("") && !newPassword.equals("") && !confirmPassword.equals("")) {
                if (newPassword.equals(confirmPassword)) {
                    if (WifiUtil.checkWifiConnection(this)) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), currentPassword)).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(PasswordActivity.this, "لقد تم تحديث كلمة السر!", Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }
                                });
                            }
                        }).addOnFailureListener(e -> Toast.makeText(PasswordActivity.this, "كلمة السر غير صحيحة!", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(PasswordActivity.this, "كلمة السر غير متطابقة!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PasswordActivity.this, "الرجاء تعبئة الحقول!", Toast.LENGTH_SHORT).show();
            }
        });


    }
}
