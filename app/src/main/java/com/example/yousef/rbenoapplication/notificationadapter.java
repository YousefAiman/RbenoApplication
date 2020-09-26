package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class notificationadapter extends RecyclerView.Adapter<notificationViewHolder> {


    private ArrayList<Notification> notifications;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String descContent;
    private CollectionReference notifRef = firestore.collection("notifications");
    private CollectionReference userRef = firestore.collection("users");
    private Picasso picasso = Picasso.get();
    private Context context;

    notificationadapter(ArrayList<Notification> notifications, Context context) {
        this.notifications = notifications;
        this.context = context;
    }

    @NonNull
    @Override
    public notificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.newnotificationitemdesign, parent, false);
        return new notificationViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final notificationViewHolder holder, final int position) {

        final Notification n = notifications.get(position);
        holder.notificationPromoTv.setText(n.getPromoId() + "");
        holder.notificationTimeTv.setText(TimeConvertor.getTimeAgo(n.getTimeCreated()));
        new Thread(() -> userRef.whereEqualTo("userId", n.getSenderId()).limit(1).get().addOnSuccessListener(snapshots -> {

            DocumentSnapshot userSnap = snapshots.getDocuments().get(0);
            String imageUrl =userSnap.getString("imageurl");

            holder.notificationImage.post(() -> {
                if(imageUrl!=null && !imageUrl.isEmpty()){
                    picasso.load(imageUrl).fit().into(holder.notificationImage);
                }
            });
            String username = userSnap.getString("username");
            holder.notificationNameTv.post(()->holder.notificationNameTv.setText(username));

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
            holder.notificationDescTv.post(()->holder.notificationDescTv.setText(descContent));
            boolean status = userSnap.getBoolean("status");
            holder.notificationStatusImage.post(() -> {
                if (status) {
                    holder.notificationStatusImage.setImageResource(R.drawable.green_circle);
                } else {
                    holder.notificationStatusImage.setImageResource(R.drawable.red_circle);
                }
            });
            holder.itemView.setOnClickListener(v -> {
                if (n.getType().equals("rating") || n.getType().equals("favourite")) {
                    Bundle bundle = new Bundle();
                    PromotionInfoFragment frag = new PromotionInfoFragment();
                    FirebaseFirestore.getInstance()
                            .collection("promotions")
                            .whereEqualTo("promoid", n.getPromoId()).limit(1).get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot snapshots1) {
                                    bundle.putSerializable("promo", snapshots1.getDocuments().get(0).toObject(Promotion.class));
                                    frag.setArguments(bundle);
                                    deleteNotification(position);
                                    ((HomeActivity) context).replacePromoFragment(frag);
                                }
                            });

                } else {
                    Intent intent = new Intent(holder.itemView.getContext(), MessagingActivity.class);
                    intent.putExtra("promouserid", n.getSenderId());
                    intent.putExtra("currentuserid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    intent.putExtra("intendedpromoid", n.getPromoId());
                    deleteNotification(position);
                    context.startActivity(intent);
                }
            });
        })).start();

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    void deleteNotification(int position) {
        Notification deletedNotification = notifications.get(position);
        notifRef.whereEqualTo("receiverId", deletedNotification.getReceiverId()).whereEqualTo("senderId", deletedNotification.getSenderId())
                .whereEqualTo("promoId", deletedNotification.getPromoId()).whereEqualTo("type", deletedNotification.getType()).limit(1).get().addOnSuccessListener(snapshots ->
                notifRef.document(snapshots.getDocuments().get(0).getId()).delete());

    }
}
