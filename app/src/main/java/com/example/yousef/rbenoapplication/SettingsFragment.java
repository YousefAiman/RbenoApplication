package com.example.yousef.rbenoapplication;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;


public class SettingsFragment extends Fragment {

    public SettingsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ((TextView) view.findViewById(R.id.toolbarTitleTv)).setText("الاعدادات");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.settingsProfileLl).setOnClickListener(v -> {
            ((HomeActivity) getActivity()).openAccountSettingFragment();
        });

        view.findViewById(R.id.settingsPasswordLl).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), PasswordActivity.class));
        });

        view.findViewById(R.id.settingsSignoutLl).setOnClickListener(v -> {
            if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();
            }
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "تم تسجيل الخروج!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getContext(), WelcomeActivity.class));
        });

    }
}
