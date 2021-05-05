package com.example.yousef.rbenoapplication;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CloudMessagingNotificationsSender {

    private static final CollectionReference usersRef =
            FirebaseFirestore.getInstance().collection("users");
    //  private static final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final APIService apiService =
            Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

    static void sendNotification(String userId, Data data) {

        if (data != null) {

            Log.d("ttt", "sending to userid: " + userId);
            usersRef.whereEqualTo("userId", userId).get().addOnSuccessListener(documents -> {

                final String token = documents.getDocuments().get(0).getString("token");

                if (GlobalVariables.getCurrentToken().equals(token))
                    return;

                Log.d("ttt", "sending to token: " + token);
                Sender sender = new Sender(data, token);
                //apiService.sendNotification(sender);

                apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MyResponse> call,
                                           @NonNull Response<MyResponse> response) {

                        if (!data.getType().equals("message")) {
                            GlobalVariables.getPreviousSentNotifications().add(data.getUser()
                                    + data.getType() + data.getPromoId());
                        }

                        Log.d("ttt", "notification send: " + response.message());
                    }

                    @Override
                    public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                        Log.d("ttt", "notification send error: " + t.getMessage());
                    }
                });
            });

        }

    }

}
