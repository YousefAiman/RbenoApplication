package com.example.yousef.rbenoapplication;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Iterables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagingRealTimeActivity extends AppCompatActivity
        implements MessageTextMapAdapter.DeleteMessageListener, View.OnLayoutChangeListener {

    private final static int DOCUMENT_MESSAGE_LIMIT = 15;
    private final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final CollectionReference
            userRef = FirebaseFirestore.getInstance().collection("users"),
            promoRef = FirebaseFirestore.getInstance().collection("promotions"),
            notifRef = FirebaseFirestore.getInstance().collection("notifications");

    private EditText messageEd;
    private String messagingUserId, currentUserName;
    private RecyclerView messageRv;
    private FrameLayout messagingFrameLayout;
    private Promotion promotion;
    private DocumentSnapshot currentDs;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private long intendedPromoId;
    private TextView messagingUserNameTv;
    private ImageView messagingUserIv;
    private Data data;
    private NotificationManager notificationManager;
    private ImageView sendMessageBtn;

    private MessageTextMapAdapter messageTextMapAdapter;
    private ArrayList<MessageMap> messageMaps;
    private boolean isLoadingMessages;
    private OnScrollListener currentScrollListener;
    private DocumentReference currentMessagingUserRef;
    private ProgressBar messagesProgressBar;

    private DatabaseReference messagingChildRef;
    private String firstKey, lastKey;

    private Map<DatabaseReference, ChildEventListener> childEventListeners;
    private Map<DatabaseReference, ValueEventListener> valueEventListeners;

    private PromotionDeleteReceiver promotionDeleteReceiver;

    private int lastListYScroll;
    private boolean isFitchingMoreMessages;

    public MessagingRealTimeActivity() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ttt", "requestCode: " + requestCode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        setupDeletionReceiver();

        final Toolbar messagesToolBar = findViewById(R.id.messagesToolBar);
        messagesToolBar.setNavigationOnClickListener(v -> finish());
        messagesToolBar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        sendMessageBtn = findViewById(R.id.sendMessageBtn);
        sendMessageBtn.setClickable(false);
        messagesProgressBar = findViewById(R.id.messagesProgressBar);
        messagingFrameLayout = findViewById(R.id.messagingFrameLayout);
        messageRv = findViewById(R.id.messagesRv);
        messagingUserNameTv = findViewById(R.id.messagingUserNameTv);
        messagingUserIv = findViewById(R.id.messagingUserIv);
        messageEd = findViewById(R.id.messageEd);


        sharedPreferences = getSharedPreferences("rbeno", Context.MODE_PRIVATE);

        final Intent intent = getIntent();

        if (intent.hasExtra("messagingBundle")) {

            final Bundle messagingBundle = intent.getBundleExtra("messagingBundle");

            messagingUserId = messagingBundle.getString("promouserid");
            intendedPromoId = messagingBundle.getLong("intendedpromoid");

            if (Build.VERSION.SDK_INT < 26) {
                BadgeUtil.decrementBadgeNum(this);
            }

        } else {

            messagingUserId = intent.getStringExtra("promouserid");
            intendedPromoId = intent.getLongExtra("intendedpromoid", 0);

        }

        readMessagesNew();

        sharedPreferences.edit()
//          .putBoolean("messagingscreen", true)
                .putString("currentMessagingUserId", messagingUserId)
                .putLong("currentMessagingPromoId", intendedPromoId).apply();

        if (GlobalVariables.getMessagesNotificationMap() != null) {

//        final String notificationIdentifier = "رسالة جديد من: " +
//                messagingUserId + " بخصوص اعلان #" + intendedPromoId;

            final String notificationIdentifier = messagingUserId + "message" + intendedPromoId;

            if (GlobalVariables.getMessagesNotificationMap().containsKey(notificationIdentifier)) {
                Log.d("ttt", "removing: " + notificationIdentifier);

                if (notificationManager == null)
                    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

//        BadgeUtil.decrementBadgeNum(this);

                notificationManager.cancel(GlobalVariables.getMessagesNotificationMap()
                        .get(notificationIdentifier));

//        sharedPreferences.edit().remove(notificationIdentifier).apply();

                GlobalVariables.getMessagesNotificationMap().remove(notificationIdentifier);
            }
        }


        final LinearLayoutManager llm = new LinearLayoutManager(MessagingRealTimeActivity.this,
                RecyclerView.VERTICAL, false) {
            @Override
            public void onItemsAdded(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
                Log.d("ttt", "onItemsAdded");
                Log.d("ttt", "positionStart: " + positionStart + " itemCount" + itemCount);
//        scrollToPosition(messageMaps.size()-);
//        new Thread(new Runnable() {
//          @Override
//          public void run() {
//
////            if(messageMaps.get(messageMaps.size()-1).getSender()){
////
////            }
//
//            messageRv.post(() -> scrollToPosition(messageMaps.size() - 1));
//
//          }
//        });

                messageRv.post(() -> scrollToPosition(messageMaps.size() - 1));
//        if(itemCount > 1){
//         if(messageMaps.size() - itemCount == 0){
//
//           messageRv.post(new Runnable() {
//             @Override
//             public void run() {
//               scrollToPosition(itemCount-1);
//             }
//           });
//        Log.d("ttt","messageMaps.size(): "+messageMaps.size()+" itemCount: "+itemCount);
////           Log.d("ttt","messageRv.getHeight(): "+messageRv.getHeight());
////           Log.d("ttt","messageRv.getMeasuredHeight(): "+messageRv.getMeasuredHeight());
////           Log.d("ttt","messageRv.getLayoutManager().getHeight(): "+
////                   Objects.requireNonNull(messageRv.getLayoutManager()).getHeight());
////           Log.d("ttt"," messageRv.getLayoutParams().height: "+
////                   messageRv.getLayoutParams().height);
//         }
//        }else{
//          messageRv.post(new Runnable() {
//            @Override
//            public void run() {
//              scrollToPosition(messageMaps.size()-1);
//            }
//          });
//
////          smoothScrollToPosition(recyclerView, null, messageMaps.size()-1);
//        }

//        if (positionStart == 0) {
//
//          scrollToPosition(itemCount);
//        } else {
//          smoothScrollToPosition(recyclerView, null, positionStart + itemCount);
//        }
//        super.onItemsAdded(recyclerView, positionStart, itemCount);
            }
//      int itemHeight = 0;
//      @Override
//      public void onItemsAdded(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
//        Log.d("ttt","item height: "+itemHeight);
//        Log.d("ttt","items height: "+itemHeight * itemCount);
//        Log.d("ttt","all height: "+getHeight());
//
//        setStackFromEnd(itemHeight * itemCount > getHeight());
//        super.onItemsAdded(recyclerView, positionStart, itemCount);
//      }
//      @Override
//      public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
//        itemHeight = lp.height;
//        return super.checkLayoutParams(lp);
//      }
        };

//    llm.setStackFromEnd(true);
        messageRv.setLayoutManager(llm);
        messageRv.addOnLayoutChangeListener(this);

        userRef.whereEqualTo("userId", messagingUserId).get().addOnSuccessListener(task -> {
            final DocumentSnapshot ds = task.getDocuments().get(0);
            currentMessagingUserRef = ds.getReference();
            messagingUserNameTv.setText(ds.getString("username"));

            Picasso.get().load(ds.getString("imageUrl")).fit().into(messagingUserIv);
        });

        messagingUserNameTv.setOnClickListener(v -> showProfile());
        messagingUserIv.setOnClickListener(v -> showProfile());

        userRef.whereEqualTo("userId", currentUserId).get().addOnSuccessListener(task -> {

            currentDs = task.getDocuments().get(0);

            currentDs.getReference().update("ActivelyMessaging",
                    intendedPromoId + "-" + messagingUserId);


            currentUserName = currentDs.getString("username");
        });

        final TextView messagingPromoTitleTv = findViewById(R.id.messagingPromoTitleTv);
        messagingPromoTitleTv.setText("بخصوص الاعلان #" + intendedPromoId);

        if (intent.hasExtra("promodocumentid")) {

            promoRef.document(intent.getStringExtra("promodocumentid")).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            addPromoFromSnapshot(snapshot);
                        } else {
                            setDeletedPromoClicker();
                        }
                    });

        } else {

            promoRef.whereEqualTo("promoid", intendedPromoId)
                    .get().addOnSuccessListener(task -> {
                if (!task.isEmpty()) {
                    addPromoFromSnapshot(task.getDocuments().get(0));
                } else {
                    setDeletedPromoClicker();
                }
            });
        }


    }

    void addPromoFromSnapshot(DocumentSnapshot documentSnapshot) {

        final ImageView messagingPromoIv = findViewById(R.id.messagingPromoIv);
        final TextView messagingPromoIdTv = findViewById(R.id.messagingPromoIdTv);


        promotion = documentSnapshot.toObject(Promotion.class);
        messagingPromoIdTv.setText(promotion.getTitle());
        if (promotion.getPromoType().equals(Promotion.IMAGE_TYPE)) {
            Picasso.get().load(promotion.getPromoimages().get(0)).fit().into(messagingPromoIv);
        } else if (promotion.getPromoType().equals(Promotion.VIDEO_TYPE)) {
            Picasso.get().load(promotion.getVideoThumbnail()).fit().into(messagingPromoIv);
        }
        setPromoViewClicker();
    }


    void sendCloudNotification(String message) {
        Log.d("ttt", "sending cloud notificaiton");
        if (data == null && promotion != null) {
            data = new Data(
                    currentUserId,
                    message,
                    "رسالة جديد من: " + currentUserName + " بخصوص اعلان #" + promotion.getPromoid(),
                    currentDs.getString("imageurl"),
                    currentUserName,
                    "message",
                    promotion.getPromoid()
            );

        } else if (data != null) {
            data.setBody(message);
        }

        CloudMessagingNotificationsSender.sendNotification(messagingUserId, data);

    }

    void readMessagesNew() {

        messageMaps = new ArrayList<>();

//    messageRv.addOnLayoutChangeListener((view, i, i1, i2, bottom, i4, i5, i6, oldBottom) -> {
//      if (bottom < oldBottom) {
//        messageRv.post(() -> messageRv.scrollToPosition(messageMaps.size()-1));
//      }
//    });

        final DatabaseReference messagingRef =
                FirebaseDatabase.getInstance().getReference().child("Messages");

        valueEventListeners = new HashMap<>();

        ValueEventListener valueEventListener;
        messagingRef.addListenerForSingleValueEvent(valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String adapterId;
                if (snapshot.hasChild(currentUserId + "-" + messagingUserId + "-" + intendedPromoId)) {

                    messagingChildRef =
                            messagingRef.child(currentUserId + "-" + messagingUserId + "-" + intendedPromoId);

                    addUserDeleteEventListener();

                    Log.d("realTimeActivity", "child: " +
                            messagingChildRef.getKey());


                    getInitialMessages();
                    adapterId = currentUserId;

//          messagingChildRef.addChildEventListener(new ChildListener());
                } else if (snapshot.hasChild(messagingUserId + "-" + currentUserId + "-" + intendedPromoId)) {

                    messagingChildRef =
                            messagingRef.child(messagingUserId + "-" + currentUserId + "-" + intendedPromoId);


                    addUserDeleteEventListener();

                    Log.d("realTimeActivity", "child: " +
                            messagingChildRef.getKey());

                    getInitialMessages();
                    adapterId = messagingUserId;
                } else {

                    messagingChildRef =
                            messagingRef.child(currentUserId + "-" + messagingUserId + "-" + intendedPromoId);

                    Log.d("realTimeActivity", "child: " +
                            messagingChildRef.getKey());

                    sendMessageBtn.setOnClickListener(new FirstMessageClickListener());
                    adapterId = currentUserId;
//          MessagingReference messagingReference = new MessagingReference(
//                  currentUserId,
//                  messagingUserId,
//                  intendedPromoId,
//                  false,
//                  false
//          );

//          messagingRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//          });
                }

                messageTextMapAdapter = new MessageTextMapAdapter(
                        messageMaps,
                        MessagingRealTimeActivity.this,
//                messagingChildRef.getKey(),
                        adapterId,
                        MessagingRealTimeActivity.this);

                Log.d("messageDeleted", "messagingChildRef.getKey(): " + messagingChildRef.getKey());

                messageRv.setAdapter(messageTextMapAdapter);

                sendMessageBtn.setClickable(true);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("realTimeActivity", "single event listener: " + error.getMessage());

            }
        });

        valueEventListeners.put(messagingRef, valueEventListener);

    }

    public void addFragmentToHomeContainer(Fragment fragment) {

        messagingFrameLayout.setVisibility(View.VISIBLE);

        final FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.messagingFrameLayout, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            fragmentTransaction.hide(getSupportFragmentManager().getFragments()
                    .get(getSupportFragmentManager().getFragments().size() - 1));
        }

        fragmentTransaction.commit();
    }

    void showProfile() {
        Bundle bundle = new Bundle();
        UserFragment fragment = new UserFragment();
        bundle.putString("promouserid", messagingUserId);
        fragment.setArguments(bundle);
        addFragmentToHomeContainer(fragment);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() != R.id.moreOptions && WifiUtil.checkWifiConnection(this)) {
            if (item.getItemId() == R.id.messagingViewProfile) {
                showProfile();
            } else if (item.getItemId() == R.id.messagingDeleteMessages) {

                if (messagingChildRef != null) {
                    deleteMessages();
                } else {
                    finish();
//        Toast.makeText(this,
//               "لا يمكنك حذف محادثة فارغة!", Toast.LENGTH_SHORT).show();
                }
//        if(documentid!=null){
//          deleteMessages();
//        }else{
//          Toast.makeText(this,
//                  "لا يمكنك حذف محادثة فارغة!", Toast.LENGTH_SHORT).show();
//        }

            } else if (item.getItemId() == R.id.messagingBlockUser) {
//        ArrayList<String> usersBlocked = (ArrayList<String>) currentDs.get("usersBlocked");
                if (GlobalVariables.getBlockedUsers() != null) {
                    if (!GlobalVariables.getBlockedUsers().contains(messagingUserId)) {
                        blockUser();
                    } else {
                        Toast.makeText(this, "لقد تم حظر هذا المستخدم من قبل!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    blockUser();
                }
            } else if (item.getItemId() == R.id.report_user) {
                userRef.document(currentDs.getId()).get().addOnSuccessListener(documentSnapshot -> {
                    long currentTimeInMillies = System.currentTimeMillis() / 1000;
                    ArrayList<String> reports = (ArrayList<String>) documentSnapshot.get("reports");
                    if (reports != null) {
                        for (String report : reports) {
                            if (report.split("-")[0].equals(currentUserId)) {
                                Toast.makeText(this, "لقد قمت بالإبلاغ عن هذا المستخدم من قبل!",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        userRef.document(currentDs.getId()).update("reports",
                                FieldValue.arrayUnion(currentUserId + "-" + currentTimeInMillies))
                                .addOnSuccessListener(v -> {
                                    reports.add(currentUserId + "-" + currentTimeInMillies);
                                    if (reports.size() >= 10) {
                                        if (((currentTimeInMillies - Long.parseLong(reports.get(reports.size() - 10)
                                                .split("-")[1]))) < 86400000) {
                                            userRef.document(currentDs.getId()).update("userBanned", true);
                                            super.onBackPressed();
                                        }
                                    }
                                    Toast.makeText(this, "لقد تم الإبلاغ عن هذا المستخدم!",
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        userRef.document(currentDs.getId()).update("reports",
                                FieldValue.arrayUnion(currentUserId + "-" + currentTimeInMillies))
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "لقد تم الإبلاغ عن هذا المستخدم!"
                                                , Toast.LENGTH_SHORT).show());
                    }
                });
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteMessages() {

        Dialog dialog = new Dialog(MessagingRealTimeActivity.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.delete_promo_alert_layout);
        ((TextView) dialog.findViewById(R.id.textView45)).setText("هل تريد حذف المحادثة؟");
        dialog.findViewById(R.id.delete_close).setOnClickListener(v2 -> dialog.dismiss());
        dialog.findViewById(R.id.delete_confirm).setOnClickListener(v -> {

            dialog.dismiss();

            progressDialog = ProgressDialog.show(MessagingRealTimeActivity.this,
                    "جاري حذف المحادثة!",
                    "الرجاء الإنتظار!", true);


            messagingChildRef.child("isDeletedFor:" + currentUserId)
                    .setValue(true)
                    .addOnSuccessListener(aVoid -> {

                        progressDialog.dismiss();
                        finish();

                    });


//            progressDialog.show();

//      final String identifier =
//              documentid.split("_")[0].equals(currentUserId)?"sender":"receiver";
//
//              Log.d("ttt","identifier: "+identifier);
//
//      chatRef.document(documentid).update("isDeletedFor_"+identifier,true)
//              .addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                  progressDialog.dismiss();
//                  finish();
//                }
//              }).addOnFailureListener(new OnFailureListener() {
//        @Override
//        public void onFailure(@NonNull Exception e) {
//
//          progressDialog.dismiss();
//                    Toast.makeText(MessagingRealTimeActivity.this,
//                  "لقد فشل حذف المحادثة!", Toast.LENGTH_SHORT).show();
//        }
//      });

//      chatRef.document(documentid).delete().addOnFailureListener(new OnFailureListener() {
//        @Override
//        public void onFailure(@NonNull Exception e) {
//          progressDialog.dismiss();
//          Toast.makeText(MessagingActivity.this,
//                  "لقد فشل حذف المحادثة!", Toast.LENGTH_SHORT).show();
//        }
//      });
//
//      final CollectionReference messagesRef =
//              chatRef.document(documentid).collection("Messages");
//
//      messagesRef.get().addOnSuccessListener(queryDocumentSnapshots2 -> {
//        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots2) {
//          snapshot.getReference().delete();
//        }
//      }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//        @Override
//        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//          progressDialog.dismiss();
//          finish();
//        }
//      });
//      chatRef.document(documentid).delete()
//              .addOnFailureListener(e -> {
//                progressDialog.dismiss();
//                Toast.makeText(MessagingActivity.this, "لقد فشل حذف المحادثة!", Toast.LENGTH_SHORT).show();
//              });
        });
        dialog.show();
    }

    void blockUser() {
        userRef.document(currentDs.getId()).update("usersBlocked",
                FieldValue.arrayUnion(messagingUserId)).addOnSuccessListener(aVoid -> {
            GlobalVariables.getBlockedUsers().add(messagingUserId);
            Toast.makeText(this, "لقد تم حظر هذا المستخدم!", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        });
    }

    public void setPromoViewClicker() {
        findViewById(R.id.promoLayout).setOnClickListener(v -> {
            messagingFrameLayout.setVisibility(View.VISIBLE);
            addFragmentToHomeContainer(new PromotionInfoFragment(promotion));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences.edit()
                .remove("isPaused")
                .remove("currentMessagingUserId")
                .remove("currentMessagingPromoId").apply();

        if (childEventListeners != null && !childEventListeners.isEmpty()) {
            for (DatabaseReference reference : childEventListeners.keySet()) {
                reference.removeEventListener(childEventListeners.get(reference));
            }
        }
        if (valueEventListeners != null && !valueEventListeners.isEmpty()) {
            for (DatabaseReference reference : valueEventListeners.keySet()) {
                reference.removeEventListener(valueEventListeners.get(reference));
            }
        }

        if (promotionDeleteReceiver != null) {
            unregisterReceiver(promotionDeleteReceiver);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (currentDs != null)
            currentDs.getReference().update("ActivelyMessaging", null);

        if (messagingChildRef != null) {

            notifRef
                    .whereEqualTo("receiverId", currentUserId)
                    .whereEqualTo("promoId", intendedPromoId)
                    .whereEqualTo("type", "message").get().addOnSuccessListener(snaps -> {
                if (!snaps.isEmpty()) {
                    snaps.getDocuments().get(0).getReference().delete();
                }
            });

            Log.d("saveMessages", "update last seen: " +
                    lastKey);

            Log.d("savedMessages", "lastKey: " + lastKey);

            if (lastKey != null && !lastKey.isEmpty()) {
                messagingChildRef.child(currentUserId + ":LastSeenMessage")
                        .setValue(Integer.parseInt(lastKey) + 1);
            }

        }

        sharedPreferences.edit().putBoolean("isPaused", true).apply();

//    sharedPreferences.edit()
//            .remove("currentMessagingUserId")
//            .remove("currentMessagingPromoId").apply();

//    sharedPreferences.edit().putBoolean("messagingscreen", false).apply();
    }

    @Override
    public void onLayoutChange(View view, int i, int i1, int i2, int bottom, int i4,
                               int i5, int i6, int oldBottom) {
        Log.d("ttt", "onLayoutChange: " + "bottom: " + bottom + " | oldBottom: " + oldBottom);
        if (oldBottom != 0) {

//      Log.d("ttt","messageRv.getScrollY(): "+lastListYScroll);


            Log.d("ttt", oldBottom + " - " + bottom);
            if (oldBottom > bottom) {
                messageRv.post(new Runnable() {
                    @Override
                    public void run() {
                        messageRv.scrollToPosition(messageMaps.size() - 1);
                    }
                });
            }


            Log.d("ttt", "vertical offset: " + messageRv.computeVerticalScrollOffset());

//            if (oldBottom > bottom) {
//                messageRv.scrollBy(0, oldBottom - bottom);
//            } else {
//                Log.d("ttt", "updated 1");
//                messageRv.scrollBy(0, Math.abs(oldBottom - bottom));
//            }

        }
    }

    class OnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            Log.d("ttt", "dx: " + dx);
            Log.d("ttt", "dy: " + dy);
            lastListYScroll = dy;

            if (isFitchingMoreMessages && !messageRv.canScrollVertically(-1)) {
                Log.d("ttt", "at top man");
                if (!isLoadingMessages) {
                    isLoadingMessages = true;
                    messagesProgressBar.setVisibility(View.VISIBLE);
                    Log.d("realTimeActivity", "geeting more messages");
                    getMoreTopMessages();
//          getMoreMessagesAndAddListener();
//            getNextTopMessageDocument();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (messagingUserId != null) {
            sharedPreferences.edit()
                    .putString("currentMessagingUserId", messagingUserId)
                    .putLong("currentMessagingPromoId", intendedPromoId).apply();
        }

//    sharedPreferences.edit().putBoolean("messagingscreen", true).apply();
    }

    void getMoreTopMessages() {

        Log.d("realTimeActivity", "String.valueOf(Integer.parseInt(firstKey)) " +
                (Integer.parseInt(firstKey) - 1));

        messagingChildRef
                .child("messages")
                .orderByKey()
                .limitToLast(DOCUMENT_MESSAGE_LIMIT)
                .endAt(String.valueOf(Integer.parseInt(firstKey) - 1))
                .get().addOnSuccessListener(snapshot -> {
            //        int index = 0;
            final List<MessageMap> newMessages = new ArrayList<>();

            for (DataSnapshot child : snapshot.getChildren()) {

                //                  final MessageMap messageMap = child.getValue(MessageMap.class);
                //                  messageMap.setId(child.getKey());
                newMessages.add(child.getValue(MessageMap.class));

//                MessageMap messageMap= new MessageMap(
//                        snapshot.child("content").getValue(String.class),
//                        snapshot.child("deleted").getValue(Boolean.class),
//                        snapshot.child("sender").getValue(Integer.class),
//                        snapshot.child("time").getValue(Long.class)
//                        );

                //          messageMaps.add(index,child.getValue(MessageMap.class));
                //          index++;
            }

            messageMaps.addAll(0, newMessages);
            //        messageTextMapAdapter.notifyItemRangeInserted(0,
            //                (int) snapshot.getChildrenCount());

            //        if(index == 0){
            //          firstKey = child.getKey();
            //        }
            //
            firstKey = String.valueOf(Integer.parseInt(lastKey) - messageMaps.size());
            Log.d("realTimeActivity", "from previous first: " +
                    (Integer.parseInt(firstKey) - 1));

            //        firstKey = Iterables.get(snapshot.getChildren(),0).getKey();


        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Log.d("realTimeActivity", "to new first: " +
                        Integer.parseInt(firstKey));


                Log.d("realTimeActivity", "messageMaps size: " + messageMaps.size());
                messageTextMapAdapter.notifyItemRangeInserted(0,
                        (int) task.getResult().getChildrenCount());


                messagesProgressBar.setVisibility(View.INVISIBLE);

                if (task.getResult().getChildrenCount() < DOCUMENT_MESSAGE_LIMIT) {
                    Log.d("realTimeActivity", "snapshot.getChildrenCount(): "
                            + task.getResult().getChildrenCount());
                    messageRv.removeOnScrollListener(currentScrollListener);
                }

                isLoadingMessages = false;
            }
        });

//    messagingChildRef
//            .child("messages")
//            .orderByKey()
//            .endAt(firstKey)
//            .limitToLast(DOCUMENT_MESSAGE_LIMIT)
//            .addListenerForSingleValueEvent(new ValueEventListener() {
//              @Override
//              public void onDataChange(@NonNull DataSnapshot snapshot) {
//
////                final List<MessageMap> newMessages = new ArrayList<>();
//
////                final GenericTypeIndicator<List<MessageMap>> t
////                        = new GenericTypeIndicator<List<MessageMap>>(){};
////
////                messageMaps.addAll(0,snapshot.getValue(t));
//
//                int index = 0;
//                for(DataSnapshot child:snapshot.getChildren()){
////                  if(index == 0){
////                    firstKey = child.getKey();
////                  }
////                  final MessageMap messageMap = child.getValue(MessageMap.class);
////                  messageMap.setId(child.getKey());
//
//                  messageMaps.add(index,child.getValue(MessageMap.class));
//                  index++;
//                }
//
//                firstKey = String.valueOf(Integer.parseInt(lastKey) - messageMaps.size());
////                firstKey = snapshot.getChildren().iterator().next().getKey();
////                messageMaps.addAll(0,newMessages);
//
//                messageTextMapAdapter.notifyItemRangeInserted(0,
//                        (int) snapshot.getChildrenCount());
//
//                isLoadingMessages = false;
//                messagesProgressBar.setVisibility(View.INVISIBLE);
//
//                if(snapshot.getChildrenCount() < DOCUMENT_MESSAGE_LIMIT){
//                  Log.d("realTimeActivity","snapshot.getChildrenCount(): "+snapshot.getChildrenCount());
//                  messageRv.removeOnScrollListener(currentScrollListener);
//                }
//
//              }
//
//              @Override
//              public void onCancelled(@NonNull DatabaseError error) {
//                Log.d("realTimeActivity","error: "+error.getMessage());
//              }
//            });

    }

//  class ChildListener implements ChildEventListener{
//
//      @Override
//      public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//        lastChildId = snapshot.getKey();
////        messageMaps.add(snapshot.getValue(MessageMap.class));
//        final MessageMap messageMap = snapshot.getValue(MessageMap.class);
//        messageMap.setId(snapshot.getKey());
//        messageMaps.add(messageMap);
//
//        messageTextMapAdapter.notifyItemInserted(messageMaps.size());
//
//        Log.d("realTimeActivity","new onChildAdded: "+snapshot.getKey());
//      }
//
//      @Override
//      public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
////        int index = messageMaps.indexOf(snapshot.getValue(MessageMap.class));
////        messageMaps.get(index).setDeleted(true);
//
//        Log.d("realTimeActivity","child event: onChildChanged: "+snapshot.getKey());
//      }
//
//      @Override
//      public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//        Log.d("realTimeActivity","onChildRemoved: "+snapshot.getKey());
//      }
//
//      @Override
//      public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//        Log.d("realTimeActivity","onChildMoved: "+snapshot.getKey());
//      }
//
//      @Override
//      public void onCancelled(@NonNull DatabaseError error) {
//        Log.d("realTimeActivity","onCancelled: "+error.getMessage());
//      }
//    }
//    void sendMessage(String message){
//
//    }
//    void setMessageClickListener(){
//      Log.d("ttt","clicekd");
//      sendMessageBtn.setOnClickListener(v->{
//        final String message = messageEd.getText().toString().trim();
//        if (!message.equals("")) {
//          if (WifiUtil.checkWifiConnection(this)) {
//            messageEd.setText("");
//            sendMessageBtn.setClickable(false);
//            sendMessage(message);
//          }
//        } else {
//
//          Toast.makeText(this,
//                  "لا يمكنك ارسال رسالة فارغة! ", Toast.LENGTH_SHORT).show();
//        }
//      });
//
//    }

    class FirstMessageClickListener implements View.OnClickListener {

//      int identifier = messagingChildRef.getKey().split("-")[0].equals(currentUserId)?1:2;

        @Override
        public void onClick(View view) {

            final String message = messageEd.getText().toString().trim();
            if (!message.equals("")) {
                if (WifiUtil.checkWifiConnection(view.getContext())) {
                    messageEd.setText("");
                    sendMessageBtn.setClickable(false);

                    final Map<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", currentUserId);
                    hashMap.put("receiver", messagingUserId);
                    hashMap.put("intendedpromoid", intendedPromoId);
                    hashMap.put(currentUserId + ":LastSeenMessage", 0);
                    hashMap.put(messagingUserId + ":LastSeenMessage", 0);
                    hashMap.put("isDeletedFor:" + currentUserId, false);
                    hashMap.put("isDeletedFor:" + messagingUserId, false);
//            hashMap.put("lastDeleted",null);

                    final MessageMap messageMap =
                            new MessageMap(message, System.currentTimeMillis() / 1000, 1);

                    final HashMap<String, MessageMap> messages = new HashMap<>();
                    messages.put("0", messageMap);
                    hashMap.put("messages", messages);
//            hashMap.put("messages",new HashMap<String,HashMap<String, Object>>(1)
//            .put("0",new HashMap<String,Object>().put("0",messageMap)));

                    messagingChildRef.setValue(hashMap).addOnSuccessListener(v -> {

                        messageMaps.add(messageMap);
                        messageTextMapAdapter.notifyDataSetChanged();
                        firstKey = "0";
                        lastKey = "0";

                        addUserDeleteEventListener();
                        addListenerForNewMessages();
                        addDeleteFieldListener();
                        checkUserActivityAndSendNotifications(messageMap.getContent());

                        sendMessageBtn.setOnClickListener(new MessageSenderClickListener());
                        sendMessageBtn.setClickable(true);


//                messagingChildRef.child("messages")
//                        .child("0").setValue(messageMap)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                          @Override
//                          public void onSuccess(Void aVoid) {
//
//                            messageMaps.add(messageMap);
//                            messageTextMapAdapter.notifyDataSetChanged();
//                            firstKey = "0";
//                            lastKey = "0";
//
//                            addListenerForNewMessages();
//                            addDeleteFieldListener();
//                            checkUserActivityAndSendNotifications(messageMap.getContent());
//
//                            sendMessageBtn.setClickable(true);
//                            sendMessageBtn.setOnClickListener(new MessageSenderClickListener());
//                          }
//                        }).addOnFailureListener(new OnFailureListener() {
//                  @Override
//                  public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(MessagingRealTimeActivity.this,
//                            "failed", Toast.LENGTH_SHORT).show();
//                    sendMessageBtn.setClickable(true);
//                  }
//                });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(MessagingRealTimeActivity.this, "failed", Toast.LENGTH_SHORT).show();
                        sendMessageBtn.setClickable(true);
                    });


                }
            } else {
                Toast.makeText(view.getContext(),
                        "لا يمكنك ارسال رسالة فارغة! ", Toast.LENGTH_SHORT).show();
            }


        }
    }

    class MessageSenderClickListener implements View.OnClickListener {

        final int identifier
                = messagingChildRef.getKey().split("-")[0].equals(currentUserId) ? 1 : 2;

        @Override
        public void onClick(View view) {

            final String message = messageEd.getText().toString().trim();
            if (!message.equals("")) {
                if (WifiUtil.checkWifiConnection(view.getContext())) {
                    messageEd.setText("");
                    sendMessageBtn.setClickable(false);

                    MessageMap messageMap = new MessageMap(
                            message,
                            System.currentTimeMillis() / 1000,
                            identifier
                    );

                    messagingChildRef.child("messages")
                            .child(String.valueOf(Integer.parseInt(lastKey) + 1))
                            .setValue(messageMap).addOnSuccessListener(aVoid -> {

                        checkUserActivityAndSendNotifications(messageMap.getContent());
                        sendMessageBtn.setClickable(true);

                    }).addOnFailureListener(e -> {

                        Toast.makeText(MessagingRealTimeActivity.this,
                                "failed", Toast.LENGTH_SHORT).show();
                        sendMessageBtn.setClickable(true);

                    });

                }
            } else {
                Toast.makeText(view.getContext(),
                        "لا يمكنك ارسال رسالة فارغة! ", Toast.LENGTH_SHORT).show();
            }


        }
    }

    void getInitialMessages() {

        messagingChildRef
                .child("messages")
                .orderByKey()
//              .limitToLast(DOCUMENT_MESSAGE_LIMIT)
                .get().addOnSuccessListener(snapshot -> {

            Log.d("realTimeActivity", "children count: " + snapshot.getChildrenCount());

            for (DataSnapshot child : snapshot.getChildren()) {
                //            if(firstKey == null){
                //              firstKey = child.getKey();
                //              Log.d("realTimeMessaging","firstKey from initial: "+firstKey);
                //            }
                messageMaps.add(child.getValue(MessageMap.class));
            }

            firstKey = Iterables.get(snapshot.getChildren(), 0).getKey();

            lastKey = Iterables.getLast(snapshot.getChildren()).getKey();


            Log.d("realTimeActivity", "from first: " + firstKey + " to last: " + lastKey);

        }).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                messageTextMapAdapter.notifyDataSetChanged();

                messageRv.post(() -> messageRv.scrollToPosition(messageMaps.size() - 1));


                messagesProgressBar.setVisibility(View.INVISIBLE);


                messagingChildRef.child(currentUserId + ":LastSeenMessage")
                        .setValue(Integer.parseInt(lastKey) + 1);

                Log.d("realTimeActivity", "size from initial: " + messageMaps.size());

                messageRv.addOnScrollListener(currentScrollListener = new OnScrollListener());


                if (Integer.parseInt(lastKey) + 1 > DOCUMENT_MESSAGE_LIMIT) {
                    Log.d("realTimeActivity", "snapshot.getChildrenCount(): " +
                            Integer.parseInt(lastKey) + 1);
//          Log.d("");
                    isFitchingMoreMessages = true;
//          messageRv.addOnScrollListener(currentScrollListener = new OnScrollListener());
                }

                sendMessageBtn.setOnClickListener(new MessageSenderClickListener());
                addListenerForNewMessages();
                addDeleteFieldListener();
            }
        });


//      Query query = messagingChildRef
//              .child("messages")
//              .orderByKey()
//              .limitToLast(DOCUMENT_MESSAGE_LIMIT);
//
//      query.addListenerForSingleValueEvent(new ValueEventListener() {
//        @Override
//        public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                for(DataSnapshot child:snapshot.getChildren()){
//                  if(firstKey == null){
//                    firstKey = child.getKey();
//                    Log.d("realTimeMessaging","firstKey from initial: "+firstKey);
//                  }
////                        final MessageMap messageMap = child.getValue(MessageMap.class);
////                        messageMap.setId(child.getKey());
//                        messageMaps.add(child.getValue(MessageMap.class));
//               }
//
////               final GenericTypeIndicator<List<MessageMap>> t
////                          = new GenericTypeIndicator<List<MessageMap>>(){};
////
////                  messageMaps.addAll(snapshot.getValue(t));
//
//             messageTextMapAdapter.notifyDataSetChanged();
//
//             messagesProgressBar.setVisibility(View.INVISIBLE);
//
////             firstKey = Iterables.getFirst(snapshot.getChildren(),null).getKey();
////              firstKey = snapshot.getChildren().iterator().next().getKey();
//              lastKey = Iterables.getLast(snapshot.getChildren()).getKey();
//
//          messagingChildRef.child(currentUserId+":LastSeenMessage")
//                  .setValue(Integer.parseInt(lastKey)+1);
//
//             if(Integer.parseInt(lastKey) + 1 > DOCUMENT_MESSAGE_LIMIT){
//
//               Log.d("realTimeActivity","snapshot.getChildrenCount(): "+
//                       Integer.parseInt(lastKey) + 1);
//
//                  messageRv.addOnScrollListener(currentScrollListener = new OnScrollListener());
//             }
//
//
//          sendMessageBtn.setOnClickListener(new MessageSenderClickListener());
//
//          addListenerForNewMessages();
//          addDeleteFieldListener();
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError error) {
//
//        }
//      });

    }

    void addDeleteFieldListener() {

        ValueEventListener valueEventListener;
        messagingChildRef
                .child("lastDeleted")
                .addValueEventListener(valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("realTimeActivity", "onDataChange");
                        if (snapshot.exists()) {

                            MessageMap messageMap = snapshot.getValue(MessageMap.class);
//
//
//                    long indexRemoved = Long.parseLong(snapshot.getValue(String.class));
////                    messageMaps.indexOf(messageMap) =
////                    indexRemoved - String.valueOf(Integer.parseInt(firstKey);
//                    Log.d("realTimeActivity","1: "+
//                            (indexRemoved - Integer.parseInt(firstKey)));
//
//                    Log.d("realTimeActivity","2: "+
//                            (Integer.parseInt(lastKey) - Integer.parseInt(firstKey)));
//
//                    Log.d("realTimeActivity","3: "+
//                            (indexRemoved - (Integer.parseInt(lastKey) - Integer.parseInt(firstKey))));
//
//                    if(indexRemoved > Integer.parseInt(firstKey)  + messageMaps.size()){
//
//                    }else{
//
//                    }
//
//                    Integer.parseInt(lastKey) -  Integer.parseInt(firstKey)

//                    final MessageMap messageMap = snapshot.getValue(MessageMap.class);
                            for (int i = 0; i < messageMaps.size(); i++) {
                                if (messageMaps.get(i).getContent().equals(messageMap.getContent())
                                        && messageMaps.get(i).getTime() == messageMap.getTime()) {
                                    if (!messageMaps.get(i).getDeleted()) {
                                        messageMaps.get(i).setDeleted(true);
                                        messageTextMapAdapter.notifyItemChanged(i);
                                    }
                                    break;
                                }
                            }
//                    Log.d("realTimeMessaging","chaned deleted: "+
//                            snapshot.getValue(MessageMap.class).getContent());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        valueEventListeners.put(messagingChildRef.child("lastDeleted"), valueEventListener);


//      messagingChildRef
//              .child("lastDeleted").addChildEventListener(new ChildEventListener() {
//        @Override
//        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//          Log.d("realTimeActivity","onChildAdded");
//        }
//
//        @Override
//        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//          Log.d("realTimeActivity","onChildChanged");
//        }
//
//        @Override
//        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//        }
//
//        @Override
//        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError error) {
//
//        }
//      });
    }

    void addListenerForNewMessages() {

        childEventListeners = new HashMap<>();

        ChildEventListener childEventListener;

        final Query query =
                messagingChildRef
                        .child("messages")
                        .orderByKey()
                        .startAt(String.valueOf(Integer.parseInt(lastKey) + 1));

        query.addChildEventListener(childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {
//                  addMessageToEnd(snapshot);

                lastKey = snapshot.getKey();
                messageMaps.add(snapshot.getValue(MessageMap.class));
                messageTextMapAdapter.notifyItemInserted(messageMaps.size());
                scrollToBottom();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot,
                                       @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        childEventListeners.put(query.getRef(), childEventListener);
    }

    @Override
    public void deleteMessage(MessageMap messageMap, DialogInterface dialog) {

        final String id = getMessageDataSnapshotId(messageMap);

        messagingChildRef.child("messages").child(id).child("deleted").setValue(true).
                addOnSuccessListener(v -> messagingChildRef.child("lastDeleted").setValue(messageMap)
                        .addOnSuccessListener(vo -> dialog.dismiss()).addOnFailureListener(e ->
                                dialog.dismiss())).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(MessagingRealTimeActivity.this,
                    "لقد فشل حذف الرسالة", Toast.LENGTH_SHORT).show();

            Log.d("ttt", "failed: " + e.getMessage());
        });
    }

    String getMessageDataSnapshotId(MessageMap messageMap) {
        return String.valueOf(Integer.parseInt(firstKey) + messageMaps.indexOf(messageMap));
    }

    void checkUserActivityAndSendNotifications(String message) {

        if (currentMessagingUserRef == null) {
            Log.d("ttt", "currentMessagingUserRef==null");
            return;
        }

        currentMessagingUserRef
                .get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.contains("ActivelyMessaging")) {
                final String messaging = documentSnapshot.getString("ActivelyMessaging");
                if (messaging == null || !messaging.equals(intendedPromoId + "-" + currentUserId)) {
                    Log.d("ttt", "sendBothNotifs");
                    sendBothNotifs(message);
                }
            } else {
                Log.d("ttt", "sendBothNotifs");
                sendBothNotifs(message);
            }
        }).addOnFailureListener(e -> Log.d("ttt", "currentMessagingUserRef e: " + e.getMessage()));


    }

    void sendBothNotifs(String message) {
        FirestoreNotificationSender.sendFirestoreNotification(intendedPromoId,
                messagingUserId, "message");
        sendCloudNotification(message);
    }

    void setupDeletionReceiver() {

        promotionDeleteReceiver =
                new PromotionDeleteReceiver() {
                    @Override

                    public void onReceive(Context context, Intent intent) {
                        final long promoId = intent.getLongExtra("promoId", 0);
                        if (promoId == promotion.getPromoid()) {
                            setDeletedPromoClicker();
                        }
                    }
                };

        registerReceiver(promotionDeleteReceiver,
                new IntentFilter(BuildConfig.APPLICATION_ID + ".promoDelete"));

    }


    void setDeletedPromoClicker() {
        findViewById(R.id.promoLayout).setOnClickListener(v ->
                Toast.makeText(MessagingRealTimeActivity.this,
                        "لقد تم ازالة او ايقاف هذا الإعلان!", Toast.LENGTH_SHORT).show());
    }

    void addUserDeleteEventListener() {

        final DatabaseReference deleteRef = messagingChildRef.child("isDeletedFor:" + messagingUserId);

        ValueEventListener valueEventListener;
        deleteRef.addValueEventListener(valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("realTimeActivity", "onDataChange: " +
                        snapshot.getValue(Boolean.class));

                if (snapshot.getValue(Boolean.class)) {

                    Log.d("realTimeActivity", "messaging user deleted this messages");


                    Toast.makeText(MessagingRealTimeActivity.this,
                            "لا يمكنك المراسلة على هذه المحادثة!",
                            Toast.LENGTH_SHORT).show();

                    findViewById(R.id.messagingLayout).setVisibility(View.GONE);


                    messageTextMapAdapter.disableLongClick();

                    deleteRef.removeEventListener(this);
                    valueEventListeners.remove(deleteRef);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        valueEventListeners.put(deleteRef, valueEventListener);

    }

    private void scrollToBottom() {
        messageRv.post(() -> messageRv.scrollToPosition(messageMaps.size() - 1));
    }

    public void removeAndReplaceFragment(Fragment fragment) {

        getSupportFragmentManager().popBackStack();
        removeAndReplaceFragment(fragment);

    }

}

