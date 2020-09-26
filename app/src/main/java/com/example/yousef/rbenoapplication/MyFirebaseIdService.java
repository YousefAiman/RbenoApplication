package com.example.yousef.rbenoapplication;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseIdService extends FirebaseMessagingService {
    CollectionReference usersRef = FirebaseFirestore.getInstance().collection("users");
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUid = firebaseUser.getUid();

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        usersRef.whereEqualTo("userId", currentUid).limit(1).get().addOnSuccessListener(snapshots -> usersRef.document(snapshots.getDocuments().get(0).getId()).update("token", s));
        Log.d("ttt", "new token: " + s);
    }
}