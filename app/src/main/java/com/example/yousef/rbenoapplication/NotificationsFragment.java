package com.example.yousef.rbenoapplication;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment implements onBackPressed {

    private ArrayList<Notification> notifications;
    private RecyclerView notificationsRv;
    private notificationadapter adapter;
    private ImageView notifIcon;
    private TextView notifTv;
    private View notificationCloseImg;
    private CollectionReference notificationRef;
    private ListenerRegistration listener;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        notificationsRv = view.findViewById(R.id.notificationsRv);
        notifIcon = view.findViewById(R.id.notifIcon);
        notifTv = view.findViewById(R.id.notifTv);
        notificationCloseImg = view.findViewById(R.id.notificationCloseImg);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        notifications = new ArrayList<>();
        notificationRef = firestore.collection("notifications");
        String currentUserId = auth.getCurrentUser().getUid();
        Query notificationQuery = notificationRef.whereEqualTo("receiverId", currentUserId);

        adapter = new notificationadapter(notifications, getContext());
        adapter.setHasStableIds(true);
        notificationsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationsRv.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(adapter));
        itemTouchHelper.attachToRecyclerView(notificationsRv);


//        LinearLayoutManager llm = new LinearLayoutManager(getContext());
//        llm.setOrientation(RecyclerView.VERTICAL);
//        Notification notification2 = new Notification();
//        notification2.setPromoId(14);
//        notification2.setReceiverId("PoaO6ls6IHSmuikEc0JszSzaMDN2");
//        notification2.setSenderId("tQEK2q1ZiXhUq2ytOy4wicmoPVA2");
//        notification2.setSeen(false);
//        notification2.setTimeCreated(1592911244);
//        notification2.setType("message");
//        notifications.add(notification2);
//        notifications.add(notification2);
//        notifications.add(notification2);
//        notifications.add(notification2);
//        adapter.notifyDataSetChanged();

        listener = notificationQuery.whereEqualTo("receiverId", currentUserId).addSnapshotListener((queryDocumentSnapshots, e) -> {
            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();
            for (int j = 0; j < documentChanges.size(); j++) {
                DocumentChange dc = documentChanges.get(j);
                QueryDocumentSnapshot document = dc.getDocument();
                if (dc.getType() == DocumentChange.Type.ADDED) {
                    notifications.add(dc.getDocument().toObject(Notification.class));
                    adapter.notifyItemInserted(notifications.size());
                } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                    for (int i = 0; i < notifications.size(); i++) {
                        Notification currentNotification = notifications.get(i);
                        if (document.getLong("promoId") == currentNotification.getPromoId()
                                && document.getString("receiverId").equals(currentNotification.getReceiverId())
                                && document.getString("senderId").equals(currentNotification.getSenderId())
                                && document.getString("type").equals(currentNotification.getType())) {
                            notifications.remove(currentNotification);
                            adapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }

                if (j == documentChanges.size() - 1) {
                    if (notifications.isEmpty()) {
                        notificationCloseImg.setVisibility(View.INVISIBLE);
                        notifIcon.setVisibility(View.VISIBLE);
                        notifTv.setVisibility(View.VISIBLE);
                    } else {
                        notificationCloseImg.setVisibility(View.VISIBLE);
                        notifIcon.setVisibility(View.INVISIBLE);
                        notifTv.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        notificationCloseImg.setOnClickListener(v -> {
            notificationCloseImg.setVisibility(View.INVISIBLE);
            notifIcon.setVisibility(View.INVISIBLE);
            notifTv.setVisibility(View.INVISIBLE);
            for (Notification n : notifications) {
                notificationRef.whereEqualTo("receiverId", n.getReceiverId()).whereEqualTo("senderId", n.getSenderId())
                        .whereEqualTo("promoId", n.getPromoId()).whereEqualTo("type", n.getType())
                        .limit(1).get().addOnSuccessListener(snapshots -> {
                    notificationRef.document(snapshots.getDocuments().get(0).getId()).delete();
                });
            }
        });

    }

    @Override
    public void onBackPressed() {
//        ((HomeActivity) getContext()).unClickableFrameLayout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            Toast.makeText(getContext(), "REMOVED NOTIFICATION LISTENER", Toast.LENGTH_SHORT).show();
            listener.remove();
        }
    }
}
