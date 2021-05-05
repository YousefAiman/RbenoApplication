package com.example.yousef.rbenoapplication;


import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;


public class SettingsFragment extends Fragment implements View.OnClickListener {

  public SettingsFragment() {
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_settings, container, false);
    ((Toolbar) view.findViewById(R.id.settingsToolbar)).setNavigationOnClickListener(v ->
            getActivity().onBackPressed());

    final LinearLayout linearLayout = view.findViewById(R.id.settingsPasswordLl);

    if (!FirebaseAuth.getInstance().getCurrentUser().getProviderData().get(1).getProviderId()
            .equals("firebase")) {
      linearLayout.setVisibility(View.GONE);
    } else {
      linearLayout.setOnClickListener(this);
    }

    linearLayout.setOnClickListener(this);


    view.findViewById(R.id.settingsSignoutLl).setOnClickListener(this);
    view.findViewById(R.id.settingsProfileLl).setOnClickListener(this);

    return view;
  }


  @Override
  public void onClick(View view) {
    if (WifiUtil.checkWifiConnection(getContext())) {

      if (view.getId() == R.id.settingsPasswordLl) {
        startActivity(new Intent(getContext(), PasswordActivity.class));
      } else if (view.getId() == R.id.settingsProfileLl) {

        if (WifiUtil.checkWifiConnection(getContext())) {
          ((HomeActivity) getActivity())
                  .addFragmentToHomeContainer(AccountSettingsFragment.newInstance());
        }

        ((HomeActivity) getActivity()).addFragmentToHomeContainer
                (AccountSettingsFragment.newInstance());
      } else if (view.getId() == R.id.settingsSignoutLl) {
        signOut();
      }
    }
  }

  void signOut() {
    if (AccessToken.getCurrentAccessToken() != null) {
      LoginManager.getInstance().logOut();
    }
    FirebaseAuth.getInstance().signOut();

    getContext().getPackageManager().setComponentEnabledSetting(
            new ComponentName(getContext(), MyFirebaseMessaging.class),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP);

    Toast.makeText(getContext(), "تم تسجيل الخروج!", Toast.LENGTH_SHORT).show();
    startActivity(new Intent(getContext(), WelcomeActivity.class));
    getActivity().finish();
  }
}
