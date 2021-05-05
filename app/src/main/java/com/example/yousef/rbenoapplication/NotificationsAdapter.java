package com.example.yousef.rbenoapplication;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.notificationViewHolder> {

    final ArrayList<Notification> notifications;

    NotificationsAdapter(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public notificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item_layout,
                parent, false);
        return new notificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final notificationViewHolder holder, final int position) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }


    static class notificationViewHolder extends RecyclerView.ViewHolder {

        private final ImageView notificationImage, notificationStatusImage;
        private final TextView notificationTimeTv, notificationNameTv, notificationPromoTv, notificationDescTv;
        private final CollectionReference notifRef = FirebaseFirestore.getInstance()
                .collection("notifications"),
                userRef = FirebaseFirestore.getInstance().collection("users"),
                promosRef = FirebaseFirestore.getInstance()
                        .collection("promotions");

        notificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationImage = itemView.findViewById(R.id.notificationImage);
            notificationNameTv = itemView.findViewById(R.id.notificationNameTv);
            notificationPromoTv = itemView.findViewById(R.id.notificationPromoTv);
            notificationDescTv = itemView.findViewById(R.id.notificationDescTv);
            notificationTimeTv = itemView.findViewById(R.id.notificationTimeTv);
            notificationStatusImage = itemView.findViewById(R.id.notificationStatusImage);
        }


        void bind(Notification n) {

            notificationPromoTv.setText(String.valueOf(n.getPromoId()));
            notificationTimeTv.setText(TimeConvertor.getTimeAgo(n.getTimeCreated()));
            String descContent = null;
            switch (n.getType()) {
                case "favourite":
                    descContent = "قام بإضافة الإعلان الخاص بك إلي المفضلة";
                    break;
                case "message":
                    descContent = "قام بإرسالة رسالة";
                    break;
                case "rating":
                    descContent = "قام بإضافة تقييم جديد";
                    break;
            }

            notificationDescTv.setText(descContent);

            userRef.whereEqualTo("userId", n.getSenderId()).get().addOnSuccessListener(snapshots -> {
                if (!snapshots.isEmpty()) {
                    final DocumentSnapshot userSnap = snapshots.getDocuments().get(0);

                    final String imageUrl = userSnap.getString("imageurl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).fit().into(notificationImage);
                    }
                    notificationNameTv.setText(userSnap.getString("username"));


                    if (userSnap.getBoolean("status")) {
                        notificationStatusImage.setImageResource(R.drawable.green_circle);
                    } else {
                        notificationStatusImage.setImageResource(R.drawable.red_circle);
                    }

                }

                itemView.setOnClickListener(v -> {
                    if (WifiUtil.checkWifiConnection(itemView.getContext())) {
                        if (n.getType().equals("rating") || n.getType().equals("favourite")) {

                            promosRef
                                    .whereEqualTo("promoid", n.getPromoId()).get()
                                    .addOnSuccessListener(snapshots1 -> {
                                        ((HomeActivity) itemView.getContext()).addFragmentToHomeContainer(
                                                new PromotionInfoFragment(snapshots1.getDocuments().get(0)
                                                        .toObject(Promotion.class)));
                                    });

                        } else {

                            itemView.getContext().startActivity(new Intent(itemView.getContext(), MessagingRealTimeActivity.class)
                                    .putExtra("promouserid", n.getSenderId())
                                    .putExtra("intendedpromoid", n.getPromoId()));
                        }
                        deleteNotification(n);
                    }
                });
            });


        }

        void deleteNotification(Notification n) {
            Log.d("ttt", "deleting notif");

            notifRef.whereEqualTo("receiverId", n.getReceiverId())
                    .whereEqualTo("senderId", n.getSenderId())
                    .whereEqualTo("promoId", n.getPromoId())
                    .whereEqualTo("type", n.getType()).get()
                    .addOnSuccessListener(snapshots -> {
                        Log.d("ttt", "deleted notif man");
                        snapshots.getDocuments().get(0).getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    if (!n.getType().equals("message") && Build.VERSION.SDK_INT < 26) {
                                        BadgeUtil.decrementBadgeNum(itemView.getContext());
                                    }
                                });
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("ttt", "failed to delete: " + e.getMessage());
                }
            });
        }

    }

}
