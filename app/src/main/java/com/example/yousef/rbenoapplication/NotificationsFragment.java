package com.example.yousef.rbenoapplication;


import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NotificationsFragment extends Fragment {

    private final ArrayList<Notification> notifications = new ArrayList<>();
    private RecyclerView notificationsRv;
    private NotificationsAdapter adapter;
    private ImageView notifIcon;
    private TextView notifTv;
    private ListenerRegistration listener;
    private NotificationManager notificationManager;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        notificationsRv = view.findViewById(R.id.notificationsRv);
        notifIcon = view.findViewById(R.id.notifIcon);
        notifTv = view.findViewById(R.id.notifTv);

        final AdView adView = view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });
        Toolbar toolbar = view.findViewById(R.id.notificationsToolBar);
        toolbar.setNavigationOnClickListener(view1 -> ((HomeActivity) getActivity()).showDrawer());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final CollectionReference notificationRef = FirebaseFirestore.getInstance().collection("notifications");
        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new NotificationsAdapter(notifications);
//    adapter.setHasStableIds(true);
        notificationsRv.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(adapter));
        itemTouchHelper.attachToRecyclerView(notificationsRv);


        listener = notificationRef.whereEqualTo("receiverId", currentUserId)
                .orderBy("timeCreated", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();
                    for (DocumentChange dc : documentChanges) {

                        final QueryDocumentSnapshot document = dc.getDocument();

                        switch (dc.getType()) {
                            case ADDED:
                                Notification n = document.toObject(Notification.class);

                                if (notifications.size() == 0) {
                                    notifications.add(n);
                                    adapter.notifyItemInserted(0);
                                } else {
                                    if (n.getTimeCreated() > notifications.get(0).getTimeCreated()) {
                                        notifications.add(0, n);
                                        adapter.notifyItemInserted(0);
                                    } else {
                                        notifications.add(n);
                                        adapter.notifyItemInserted(notifications.size() - 1);
                                    }
                                }
                                break;

                            case REMOVED:

                                for (Notification notification : notifications) {
                                    if (document.getLong("promoId") == notification.getPromoId()
                                            && document.getString("receiverId").equals(notification.getReceiverId())
                                            && document.getString("senderId").equals(notification.getSenderId())
                                            && document.getString("type").equals(notification.getType())) {

                                        final int index = notifications.indexOf(notification);
                                        notifications.remove(index);
                                        adapter.notifyItemRemoved(index);

                                        final String notificationIdentifier = notification.getSenderId()
                                                + notification.getType() + notification.getPromoId();

                                        if (GlobalVariables.getMessagesNotificationMap() != null) {
                                            if (GlobalVariables.getMessagesNotificationMap().containsKey(notificationIdentifier)) {


                                                if (notificationManager == null)
                                                    notificationManager = (NotificationManager)
                                                            getContext().getSystemService(Context.NOTIFICATION_SERVICE);


                                                notificationManager.cancel(GlobalVariables
                                                        .getMessagesNotificationMap().get(notificationIdentifier));


                                                GlobalVariables.getMessagesNotificationMap().remove(notificationIdentifier);

                                            }
                                        }

                                        break;
                                    }
                                }

                                break;

                            case MODIFIED:

                                for (Notification notification : notifications) {
                                    if (document.getLong("promoId") == notification.getPromoId()
                                            && document.getString("receiverId").equals(notification.getReceiverId())
                                            && document.getString("senderId").equals(notification.getSenderId())
                                            && document.getString("type").equals(notification.getType())) {


                                        final int index = notifications.indexOf(notification);

                                        notification.setTimeCreated(document.getLong("timeCreated"));
                                        adapter.notifyItemChanged(index);

                                        if (index != 0) {
                                            Collections.swap(notifications, index, 0);
                                            adapter.notifyItemMoved(index, 0);
                                        }
//                        else {
//                          adapter.notifyItemChanged(index);
//                        }

                                        break;
                                    }
                                }
                                break;
                        }
                    }
                    if (notifications.isEmpty()) {
                        notifIcon.setVisibility(View.VISIBLE);
                        notifTv.setVisibility(View.VISIBLE);
                    } else {
                        notifIcon.setVisibility(View.INVISIBLE);
                        notifTv.setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.remove();
        }
    }
}
