package com.example.yousef.rbenoapplication;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.google.firebase.auth.FirebaseAuth;

import javax.annotation.Nullable;

public class SigninUtil {

//  private static Dialog dialog;
//
//  private SigninUtil(){}

    public static Dialog getInstance(Context context, @Nullable Activity activity) {

//    if(dialog == null) {

        Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.signin_alert_layout);
        dialog.findViewById(R.id.alert_close).setOnClickListener(v -> dialog.cancel());
        dialog.findViewById(R.id.alert_signin).setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            dialog.dismiss();
            context.startActivity(new Intent(context, SigninActivity.class));

            if (activity != null) {
                activity.finish();
            }

        });

//    }

        return (dialog);
    }

}
