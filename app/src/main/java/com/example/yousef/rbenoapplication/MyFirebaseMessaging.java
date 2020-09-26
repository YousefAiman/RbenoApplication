package com.example.yousef.rbenoapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    NotificationManager notificationManager;
    SharedPreferences sharedPreferences;
    CollectionReference usersRef = FirebaseFirestore.getInstance().collection("users");
    String currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if (currentUid != null) {
            usersRef.whereEqualTo("userId", currentUid).limit(1).get().addOnSuccessListener(snapshots -> usersRef.document(snapshots.getDocuments().get(0).getId()).update("token", s));
        }
        Log.d("ttt", "new token: " + s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("ttt", "message received");
//      if (firebaseUser != null) {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences("rbeno", Context.MODE_PRIVATE);
        }
        try {
            if (sharedPreferences.contains("messagingscreen")) {
                if (!sharedPreferences.getBoolean("messagingscreen", true)) {
                    sendNotification(remoteMessage);
                }
            } else {
                sendNotification(remoteMessage);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
//        }
    }

    public void sendNotification(RemoteMessage remoteMessage) throws ExecutionException, InterruptedException {

        createChannel();
        Map<String, String> data = remoteMessage.getData();
        int j = Integer.parseInt(data.get("user").replaceAll("[\\D]", "").substring(0, 4));
        Intent newIntent = new Intent(this, MainActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, newIntent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel1")
                .setSmallIcon(Integer.parseInt(data.get("icon")))
                .setContentTitle(data.get("title"))
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText(data.get("body"))
                .setAutoCancel(true);
        if (data.containsKey("imageUrl")) {
            builder.setLargeIcon(Glide.with(this)
                    .asBitmap()
                    .load(data.get("imageUrl"))
                    .submit().get());
            int i = 0;
            if (j > 0) {
                i = j;
            }
            notificationManager.notify(i, builder.build());
            sharedPreferences.edit().putInt("latestNotificationID", i).apply();
        } else {
            int i = 0;
            if (j > 0) {
                i = j;
            }
            notificationManager.notify(i, builder.build());
            sharedPreferences.edit().putInt("latestNotificationID", i).apply();
        }

    }

    public void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            if (notificationManager.getNotificationChannel("channel1") == null) {
                NotificationChannel channel = new NotificationChannel("channel1", "notifChannel", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("notifications");
                channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
