package com.example.yousef.rbenoapplication;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class FirestoreNotificationSender {
    private static final CollectionReference notificationRef = FirebaseFirestore.getInstance().collection("notifications");
    //  private static String documentId = null;
    private static final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    static void sendFirestoreNotification(long promoId, String promoUid, String type) {

//    final long time = System.currentTimeMillis() / 1000;
        final String notificationPath = promoId + "_" + currentUserId
                + "_" + promoUid + "_" + type;

//    notificationRef.document(notificationPath)
//            .get().continueWith(new Continuation<DocumentSnapshot, Object>() {
//      @Override
//      public Object then(@NonNull Task<DocumentSnapshot> task) throws Exception {
//        if(!task.getResult().exists()){
//          final HashMap<String, Object> notification = new HashMap<>();
//          notification.put("promoId",promoId);
//          notification.put("senderId",currentUserId);
//          notification.put("receiverId",promoUid);
//          notification.put("type",type);
//          notification.put("timeCreated",time);
//
//          notificationRef.document(notificationPath).set(notification);
//        }else{
//          notificationRef.document(task.getResult().getId())
//                  .delete();
//        }
//        return null;
//      }
//    });

        notificationRef.document(notificationPath)
                .get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {

                Log.d("ttt", "adding notification firestore");
                final HashMap<String, Object> notification = new HashMap<>();
                notification.put("promoId", promoId);
                notification.put("senderId", currentUserId);
                notification.put("receiverId", promoUid);
                notification.put("type", type);
                notification.put("timeCreated", System.currentTimeMillis() / 1000);
                notification.put("sent", false);

                notificationRef.document(notificationPath).set(notification);
            } else {

                if (type.equals("message")) {
                    documentSnapshot.getReference().update("timeCreated", System.currentTimeMillis() / 1000);
                } else {
                    Log.d("ttt", "deleting notification firestore");
                    documentSnapshot.getReference().delete();
                }

            }
        });
//      notificationRef.whereEqualTo("promoId", promoId).
//              whereEqualTo("receiverId", promoUid)
//              .whereEqualTo("senderId",currentUserId)
//              .whereEqualTo("type", type).get().continueWith(task -> {
//                if (task.getResult().isEmpty()) {
//
//
//                  final HashMap<String, Object> notification = new HashMap<>();
//                  notification.put("promoId",promoId);
//                  notification.put("senderId",currentUserId);
//                  notification.put("receiverId",promoUid);
//                  notification.put("type",type);
//                  notification.put("timeCreated",time);
//
//                  notificationRef.document().set(notification);
//
//                        .continueWith(task1 -> {
//                          documentId = task1.getResult().get().getResult().getId();
//                          return documentId;
//                        });
//                }
//
//                return null;
//              });

    }

    static void deleteFirestoreNotification(long promoId, String promoUid, String type) {
        notificationRef.document(promoId + "_" + currentUserId
                + "_" + promoUid + "_" + type).delete();
    }
}
