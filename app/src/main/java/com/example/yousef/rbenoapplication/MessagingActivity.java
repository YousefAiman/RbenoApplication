package com.example.yousef.rbenoapplication;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;

public class MessagingActivity extends AppCompatActivity {
    static final int PICK_IMAGE = 1;

    EditText messageEd;
    Button attachButtotn;
    TextView noMessagesTv;
    String messagingUserId;
    String currentUserId;
    String messagingUsername;

    CollectionReference userRef;
    MessageAdapter messageAdapter;
    RecyclerView messageRv;
    CollectionReference chatRef;
    CollectionReference promoRef;
    long intendedPromoId;
    ArrayList<String> receivedMessages;
    String documentid;
    String currentUserName;
    FrameLayout messagingFrameLayout;
    Promotion promotion;
    APIService apiService;
    DocumentReference dr;
    List<Bundle> bundles;
    DocumentSnapshot currentDs;
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    Dialog dialog;
    ImageView recordBtn;
    ImageView cameraBtn;
    ImageView imageBtn;
    MediaRecorder mediaRecorder;
    String fileName;

    long latestAudioMessageStartTime;
    long latestAudioMessageEndTime;
    boolean animationHasPlayed = false;
    View cameraView;
    View imageView;
    View recordView;
    LinearLayoutManager llm;
    ListenerRegistration listener;
//    int recyclerOriginalSize;
String temporaryImageMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        findViewById(R.id.promoBackIv).setOnClickListener(v->onBackPressed());
        setSupportActionBar(findViewById(R.id.promotiontoolbar));

        sharedPreferences = getSharedPreferences("rbeno", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("messagingscreen", true).apply();
        if (sharedPreferences.contains("latestNotificationID")) {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(sharedPreferences.getInt("latestNotificationID", 0));
            sharedPreferences.edit().remove("latestNotificationID").apply();
        }

        messagingFrameLayout = findViewById(R.id.messagingFrameLayout);
        messageRv = findViewById(R.id.messagesRv);
        llm = new LinearLayoutManager(getApplicationContext());
        llm.setStackFromEnd(true);

        llm.setOrientation(RecyclerView.VERTICAL);
        messageRv.setLayoutManager(llm);

        chatRef = FirebaseFirestore.getInstance().collection("chats");
        userRef = FirebaseFirestore.getInstance().collection("users");
        promoRef = FirebaseFirestore.getInstance().collection("promotions");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        messagingUserId = getIntent().getStringExtra("promouserid");
        intendedPromoId = getIntent().getLongExtra("intendedpromoid", 0);

        cameraView = findViewById(R.id.cameraView);
        imageView = findViewById(R.id.imageView);
        recordView = findViewById(R.id.recordView);

        messageEd = findViewById(R.id.messageEd);

        attachButtotn = findViewById(R.id.attachBtn);
        recordBtn = findViewById(R.id.recordBtn);
        cameraBtn = findViewById(R.id.cameraBtn);
        imageBtn = findViewById(R.id.imageBtn);
        noMessagesTv = findViewById(R.id.nomessageTv);

        userRef.whereEqualTo("userId",messagingUserId).limit(1).get().addOnSuccessListener(task -> {
            DocumentSnapshot messagingDs = task.getDocuments().get(0);
            messagingUsername = messagingDs.getString("username");
            ((TextView)findViewById(R.id.messagingUserNameTv)).setText(messagingUsername);
            dr = userRef.document(messagingDs.getId());
            readMessages(currentUserId, messagingUserId);
        });


        userRef.whereEqualTo("userId", currentUserId).limit(1).get().addOnSuccessListener(task -> {
            currentDs = task.getDocuments().get(0);
            currentUserName = currentDs.getString("username");
//            String currentUserImage = currentDs.getString("imageurl");
//            if (currentUserImage != null && !currentUserImage.isEmpty()) {
//                ImageView userImageIndicator = findViewById(R.id.userImageIndicator);
//                Picasso.get().load(currentUserImage).fit().into(userImageIndicator);
//            }
        });
        if (getIntent().hasExtra("promodocumentid")) {
            promoRef.document(getIntent().getStringExtra("promodocumentid")).get().addOnSuccessListener(task -> {
                promotion = task.toObject(Promotion.class);
                ((TextView)findViewById(R.id.intededPromoId)).setText("#" + intendedPromoId);
                String promoType = promotion.getPromoType();
                if (promoType.equals("image")) {
                    Picasso.get().load(promotion.getPromoimages().get(0)).fit().into(((ImageView)findViewById(R.id.messagingPromoImage)));
                } else if (promoType.equals("video")) {
                    Picasso.get().load(promotion.getVideoThumbnail()).fit().into(((ImageView)findViewById(R.id.messagingPromoImage)));
                }
                setPromoViewClicker();
            });
        }else{
            promoRef.whereEqualTo("promoid", intendedPromoId).limit(1).get().addOnSuccessListener(task -> {
                if (!task.isEmpty()) {
                    promotion = task.getDocuments().get(0).toObject(Promotion.class);
                    ((TextView)findViewById(R.id.intededPromoId)).setText("#" + intendedPromoId);
                    String promoType = promotion.getPromoType();
                    if (promoType.equals("image")) {
                        Picasso.get().load(promotion.getPromoimages().get(0)).fit().into(((ImageView)findViewById(R.id.messagingPromoImage)));
                    } else if (promoType.equals("video")) {
                        Picasso.get().load(promotion.getVideoThumbnail()).fit().into(((ImageView)findViewById(R.id.messagingPromoImage)));
                    }
                    setPromoViewClicker();
                }
            });
        }


        int Measuredwidth = GlobalVariables.getWindowWidth();

        double percentage15 = Measuredwidth / 8.2;
        double percentage25 = Measuredwidth / 4.7;
        double percentage35 = Measuredwidth / 3.4;

        float imageOriginalPosition = imageBtn.getTranslationY();
        TranslateAnimation startAnimateEdit = new TranslateAnimation(0, (float) Measuredwidth / 8, 0, 0);
        startAnimateEdit.setDuration(250);
        startAnimateEdit.setFillAfter(true);

        TranslateAnimation endAnimateEdit = new TranslateAnimation((float) Measuredwidth / 8, 0, 0, 0);
        endAnimateEdit.setDuration(250);
        endAnimateEdit.setFillAfter(true);

        cameraView.setClickable(false);
        imageView.setClickable(false);
        recordView.setClickable(false);
        animationHasPlayed = false;

        imageView.setOnClickListener(v -> {
            Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent1.setAction(Intent.ACTION_PICK);
            startActivityForResult(intent1, PICK_IMAGE);
        });
        recordBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startRecording();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                stopRecording();
            }
            return true;
        });

        cameraView.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, PICK_IMAGE);
            }
        });
        messageEd.setOnClickListener(v -> {
//              messageRv.scrollToPosition(receivedMessages.size()-1);
            if (animationHasPlayed) {
                animationHasPlayed = false;
                cameraView.setClickable(false);
                imageView.setClickable(false);
                recordView.setClickable(false);
                cameraBtn.animate().translationX(imageOriginalPosition).setDuration(250).start();
                imageBtn.animate().translationX(imageOriginalPosition).setDuration(250).start();
                recordBtn.animate().translationX(imageOriginalPosition).setDuration(250).start();
                messageEd.startAnimation(endAnimateEdit);
            }
        });
        messageEd.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                messageEd.performClick();
            }
        });


        attachButtotn.setOnClickListener(v -> {
            if (animationHasPlayed) {
                animationHasPlayed = false;
                messageEd.startAnimation(endAnimateEdit);
                cameraView.setClickable(false);
                imageView.setClickable(false);
                recordView.setClickable(false);
                cameraBtn.animate().translationX(imageOriginalPosition).setDuration(250).start();
                imageBtn.animate().translationX(imageOriginalPosition).setDuration(250).start();
                recordBtn.animate().translationX(imageOriginalPosition).setDuration(250).start();

            } else {
                animationHasPlayed = true;

                cameraView.setClickable(true);
                imageView.setClickable(true);
                recordView.setClickable(true);

                messageEd.startAnimation(startAnimateEdit);
                cameraBtn.animate().translationX((float) percentage15).setDuration(250).start();
                imageBtn.animate().translationX((float) percentage25).setDuration(250).start();
                recordBtn.animate().translationX((float) percentage35).setDuration(250).start();

            }
        });



        findViewById(R.id.sendMessageBtn).setOnClickListener(v -> {
            String message = messageEd.getText().toString().trim();
            if (!message.equals("")) {
                findViewById(R.id.sendMessageBtn).setClickable(false);
                sendMessage(currentUserId, messagingUserId, message);
                sendNotification(message);
                CollectionReference notifRef = FirebaseFirestore.getInstance().collection("notifications");
                notifRef.whereEqualTo("receiverId", messagingUserId).whereEqualTo("senderId", currentUserId)
                        .whereEqualTo("promoId", intendedPromoId).whereEqualTo("type", "message").get().addOnCompleteListener(task -> {
                    if (task.getResult().isEmpty()) {
                        Notification notification = new Notification();
                        notification.setPromoId(intendedPromoId);
                        notification.setSenderId(currentUserId);
                        notification.setReceiverId(messagingUserId);
                        notification.setType("message");
                        notification.setTimeCreated(System.currentTimeMillis() / 1000);
                        notification.setSeen(false);
                        notifRef.add(notification);
                    }
                });
            } else {
                Toast.makeText(MessagingActivity.this, "لا يمكنك ارسال رسالة فارغة! ", Toast.LENGTH_SHORT).show();
            }
            messageEd.setText("");
        });
    }


    private void sendMessage(final String sender, final String receiver, final String message) {
        chatRef.document(sender + "_" + receiver + "_" + intendedPromoId).get().addOnCompleteListener(task -> {
            if (task.getResult().exists()) {
                chatRef.document(sender + "_" + receiver + "_" + intendedPromoId).update("messages", FieldValue.arrayUnion(message + "--" + System.currentTimeMillis() + "--" + 1)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(message.equals("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/images")){
                            temporaryImageMessage = message + "--" + System.currentTimeMillis() + "--" + 1;
                            Log.d("imageMessage","message1: "+temporaryImageMessage);
                        }

                        findViewById(R.id.sendMessageBtn).setClickable(true);
                    }
                }).addOnFailureListener(e ->{
                    findViewById(R.id.sendMessageBtn).setClickable(true);
                    Toast.makeText(MessagingActivity.this,"حصل خطأ أثناء إرسال الرسالة", Toast.LENGTH_SHORT).show();
                });
            } else {
                chatRef.document(receiver + "_" + sender + "_" + intendedPromoId).get().addOnCompleteListener(task1 -> {
                    if (task1.getResult().exists()) {

                        chatRef.document(receiver + "_" + sender + "_" + intendedPromoId).update("messages", FieldValue.arrayUnion(message + "--" + System.currentTimeMillis() + "--" + 2)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if(message.equals("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/images")){
                                    temporaryImageMessage = message + "--" + System.currentTimeMillis() + "--" + 2;
                                    Log.d("imageMessage","message2: "+temporaryImageMessage);
                                }
                                findViewById(R.id.sendMessageBtn).setClickable(true);
                            }
                        }).addOnFailureListener(e ->{
                            findViewById(R.id.sendMessageBtn).setClickable(true);
                            Toast.makeText(MessagingActivity.this,"حصل خطأ أثناء إرسال الرسالة", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        ArrayList<String> sentmessages = new ArrayList<>();
                        sentmessages.add(message + "--" + System.currentTimeMillis() + "--" + 1);
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", sender);
                        hashMap.put("receiver", receiver);
                        hashMap.put("messages", sentmessages);
                        hashMap.put("timsent", System.currentTimeMillis());
                        hashMap.put("intendedpromoid", intendedPromoId);
                        hashMap.put(sender + ":LastSeenMessage", 0);
                        hashMap.put(receiver + ":LastSeenMessage", 0);
                        chatRef.document(sender + "_" + receiver + "_" + intendedPromoId).set(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (receivedMessages.isEmpty()) {
                                    findViewById(R.id.sendMessageBtn).setClickable(true);
                                    readMessages(sender, receiver);
                                }
                            }
                        }).addOnFailureListener(e -> {findViewById(R.id.sendMessageBtn).setClickable(true);
                            Toast.makeText(MessagingActivity.this,"حصل خطأ أثناء إرسال الرسالة", Toast.LENGTH_SHORT).show();});
                    }
                });
            }

        });
    }



    private void readMessages(final String myid, final String userid) {
        receivedMessages = new ArrayList<>();
        chatRef.document(myid + "_" + userid + "_" + intendedPromoId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    receivedMessages = (ArrayList<String>) task.getResult().get("messages");

                    documentid = myid + "_" + userid + "_" + intendedPromoId;
                    messageAdapter = new MessageAdapter(receivedMessages, documentid, MessagingActivity.this,myid);
                    messageAdapter.setHasStableIds(true);
                    messageRv.setAdapter(messageAdapter);
                    noMessagesTv.setVisibility(View.GONE);
                    messageRv.scrollToPosition(receivedMessages.size() - 1);

                    listener = chatRef.document(documentid).addSnapshotListener((documentSnapshot, e) -> {
                        if (documentSnapshot.exists()) {
                            List<String> newMessage = (List<String>) documentSnapshot.get("messages");
                            if (newMessage.size() > receivedMessages.size()) {
                                receivedMessages.add(newMessage.get(newMessage.size() - 1));
                                messageAdapter.notifyItemInserted(receivedMessages.size());
                                messageRv.scrollToPosition(receivedMessages.size() - 1);
                            } else if (newMessage.size() < receivedMessages.size()) {
                                List<String> receivedMessagesCopy = new ArrayList<>(receivedMessages);
                                receivedMessagesCopy.removeAll(newMessage);
                                String removedMessage = receivedMessagesCopy.get(0);
                                int removedMessageIndex = receivedMessages.indexOf(removedMessage);
                                receivedMessages.remove(removedMessage);
                                messageAdapter.notifyItemRemoved(removedMessageIndex);
//                                if(messageRv.getHeight() < recyclerOriginalSize){
//                                    llm.setStackFromEnd(true);
//                                }else{
//                                    llm.setStackFromEnd(false);
//                                }
                                if (removedMessage.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com")) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(removedMessage.split("--")[0]).delete();
                                }
                            }
                        } else {
                            Toast.makeText(MessagingActivity.this, "لقد تم حذف المحادثة!", Toast.LENGTH_SHORT).show();
                            if (dialog != null) {
                                dialog.dismiss();
                                progressDialog.dismiss();
                            }
                            finish();
                        }
                    });
                } else {
                    chatRef.document(userid + "_" + myid + "_" + intendedPromoId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.getResult().exists()) {
                                documentid = userid + "_" + myid + "_" + intendedPromoId;
                                receivedMessages = (ArrayList<String>) task.getResult().get("messages");
                                messageAdapter = new MessageAdapter(receivedMessages, documentid, MessagingActivity.this,userid);
                                messageAdapter.setHasStableIds(true);
                                messageRv.setAdapter(messageAdapter);
                                noMessagesTv.setVisibility(View.GONE);
                                messageRv.scrollToPosition(receivedMessages.size() - 1);

                                listener = chatRef.document(documentid).addSnapshotListener((documentSnapshot, e) -> {

                                    if (documentSnapshot.exists()) {

                                        List<String> newMessage = (List<String>) documentSnapshot.get("messages");
                                        if (newMessage.size() > receivedMessages.size()) {
                                            receivedMessages.add(newMessage.get(newMessage.size() - 1));
                                            messageAdapter.notifyItemInserted(receivedMessages.size());
                                            messageRv.scrollToPosition(receivedMessages.size() - 1);

                                        } else if (newMessage.size() < receivedMessages.size()) {
                                            List<String> receivedMessagesCopy = new ArrayList<>(receivedMessages);
                                            receivedMessagesCopy.removeAll(newMessage);
                                            String removedMessage = receivedMessagesCopy.get(0);
                                            int removedMessageIndex = receivedMessages.indexOf(removedMessage);
                                            receivedMessages.remove(removedMessage);
                                            messageAdapter.notifyItemRemoved(removedMessageIndex);
//                                            if (messageRv.getHeight() > recyclerOriginalSize) {
//                                                llm.setStackFromEnd(true);
//                                            } else {
//                                                llm.setStackFromEnd(false);
//                                            }
                                            if (removedMessage.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com")) {
                                                FirebaseStorage.getInstance().getReferenceFromUrl(removedMessage.split("--")[0]).delete();
                                            }
                                        }
                                    } else {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                            progressDialog.dismiss();
                                        }
                                        finish();
                                    }
                                });
                            }
                        }
                    });

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
//        if (imageDialog != null && imageDialog.isShowing()) {
//            imageDialog.dismiss();
//        } else {
        FragmentManager fragmentManager = getSupportFragmentManager();
            if (messagingFrameLayout.getVisibility() == View.VISIBLE) {


                if(fragmentManager.getBackStackEntryCount() - 1 == -1){
                    finish();
                    return;
                }

                String fragmentName = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
                if (fragmentName.equals("promoFragment")) {
                    if (bundles.size() >= 2) {
                        Log.d("fragment", "removed promo fragment");
                        PromotionInfoFragment promoFragment = new PromotionInfoFragment();
                        promoFragment.setArguments(bundles.get(bundles.size() - 2));
                        bundles.remove(bundles.size() - 2);
                        fragmentManager.beginTransaction().replace(R.id.messagingFrameLayout, promoFragment, "promoFragment").addToBackStack("promoFragment").commit();
                    } else if (bundles.size() == 1) {
                        Log.d("fragment", "removed last promo fragment");
                        bundles.clear();
                        fragmentManager.popBackStack(fragmentName, 1);
                    }
                } else {
                    Log.d("fragment", "removed non bottom fragment");
                    fragmentManager.popBackStack(fragmentName, 1);
                }
//
//                if (bundles.size() > 1) {
//                    PromotionInfoFragment promoFragment = new PromotionInfoFragment();
//                    promoFragment.setArguments(bundles.get(bundles.size() - 2));
//                    bundles.remove(bundles.size() - 2);
//                    getSupportFragmentManager().beginTransaction().replace(R.id.homeFragmentContainer, promoFragment).commit();
//                } else if (bundles.size() == 1) {
//                    bundles.remove(bundles.size() - 1);
//                    getSupportFragmentManager().popBackStack();
//                } else {
//                    finish();
//                }
            } else {
                finish();
            }
//        }
    }

    private void sendNotification(String messageContent) {

        if(apiService==null) apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);


        Data data = new Data(currentUserId, R.mipmap.ic_launcher, messageContent, "رسالة جديد من: " + currentUserName, currentUserId, currentDs.getString("imageurl"), currentUserName);
        dr.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.contains("token")) {
                Sender sender = new Sender(data, documentSnapshot.getString("token"));
                apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, retrofit2.Response<MyResponse> response) {
                    }
                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                    }
                });
            }
        });
//        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            findViewById(R.id.sendMessageBtn).setClickable(false);
            ProgressDialog imageProgress = new ProgressDialog(MessagingActivity.this);
            imageProgress.setTitle("جاري إرسال الصورة!");
            imageProgress.setCancelable(false);
            imageProgress.show();


//                int positionAdded = receivedMessages.size();
//                temporaryImageMessage = "https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/images" + "--" + System.currentTimeMillis() + "--" + 1;
//            sendMessage(currentUserId, messagingUserId,"https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/images");
//                receivedMessages.add(temporaryImageMessage);
//                messageAdapter.notifyItemInserted(positionAdded);
//                messageRv.scrollToPosition(receivedMessages.size() - 1);
            final StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());
            findViewById(R.id.sendMessageBtn).setClickable(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ref.putFile(data.getData()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("imageMessage","message delete: "+temporaryImageMessage);

//                            chatRef.document(documentid).update("messages", FieldValue.arrayRemove(temporaryImageMessage));
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageProgress.dismiss();
//                                    receivedMessages.remove(temporaryImageMessage);
//                                    messageRv.post(()->messageAdapter.notifyItemRemoved(positionAdded));
                                    sendMessage(currentUserId, messagingUserId, uri.toString());
                                    sendNotification("قام بإرسال صورة");
                                }
                            }).addOnFailureListener(e -> {
                                imageProgress.dismiss();
                                Toast.makeText(MessagingActivity.this, "فشلت محاولة ارسال الصورة!", Toast.LENGTH_SHORT).show();
                                Log.d("ttt",e.toString());
                            });;
                        }
                    }).addOnFailureListener(e -> {
                        imageProgress.dismiss();
                        Toast.makeText(MessagingActivity.this, "فشلت محاولة ارسال الصورة!", Toast.LENGTH_SHORT).show();
                        Log.d("ttt",e.toString());
                    });
                }
            }).start();
//            new Thread(() -> ref.putFile(data.getData()).addOnSuccessListener(taskSnapshot -> {
//                ref.getDownloadUrl().addOnSuccessListener(uri -> {
//                    receivedMessages.remove(temporaryImageMessage);
//                    messageRv.post(()->messageAdapter.notifyItemRemoved(positionAdded));
//                    sendMessage(currentUserId, messagingUserId, uri.toString());
//                    sendNotification("قام بإرسال صورة");
//                }
//            }).addOnFailureListener(e -> {
//                Toast.makeText(MessagingActivity.this, "فشلت محاولة ارسال الصورة!", Toast.LENGTH_SHORT).show();
//                Log.d("ttt",e.toString());
//            })).addOnFailureListener(e -> {
//                Toast.makeText(MessagingActivity.this, "فشلت محاولة ارسال الصورة!", Toast.LENGTH_SHORT).show();
//                Log.d("ttt",e.toString());
//            })).start();

        }
    }


    public void replacePromoFragment(Fragment fragment) {
        if(bundles==null)bundles = new ArrayList<>();
        bundles.add(fragment.getArguments());
        getSupportFragmentManager().beginTransaction().replace(R.id.messagingFrameLayout, fragment, "promoFragment").addToBackStack("promoFragment").commit();
    }

    public void replaceNonPromoFragment(Fragment fragment) {
        messagingFrameLayout.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.messagingFrameLayout, fragment, "nonPromo").addToBackStack("fragment").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.messaging_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.messagingViewProfile:
                Bundle bundle = new Bundle();
                UserFragment fragment = new UserFragment();
                bundle.putString("promouserid", messagingUserId);
                fragment.setArguments(bundle);
                replaceNonPromoFragment(fragment);
                break;
            case R.id.messagingDeleteMessages:

                deleteMessages();
                break;
            case R.id.messagingBlockUser:
                ArrayList<String> usersBlocked = (ArrayList<String>) currentDs.get("usersBlocked");
                if (usersBlocked != null) {
                    if (!usersBlocked.contains(messagingUserId)) {
                        blockUser();
                    } else {
                        Toast.makeText(this, "لقد تم حظر هذا المستخدم من قبل!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    blockUser();
                }
                break;
            case R.id.report_user:
                userRef.document(currentDs.getId()).get().addOnSuccessListener(documentSnapshot -> {
                    long currentTimeInMillies = System.currentTimeMillis() / 1000;
                    ArrayList<String> reports = (ArrayList<String>) documentSnapshot.get("reports");
                    if (reports != null) {
                        for (String report : reports) {
                            if (report.split("-")[0].equals(currentUserId)) {
                                Toast.makeText(this,"لقد قمت بالإبلاغ عن هذا المستخدم من قبل!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        userRef.document(currentDs.getId()).update("reports", FieldValue.arrayUnion(currentUserId + "-" + currentTimeInMillies)).addOnSuccessListener(aVoid -> {
                            reports.add(currentUserId + "-" + currentTimeInMillies);
                            if (reports.size() >= 10) {
                                if (((currentTimeInMillies - Long.parseLong(reports.get(reports.size() - 10).split("-")[1]))) < 86400000) {
                                    userRef.document(currentDs.getId()).update("userBanned", true);
                                    super.onBackPressed();
                                }
                            }
                            Toast.makeText(this, "لقد تم الإبلاغ عن هذا المستخدم!", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        userRef.document(currentDs.getId()).update("reports", FieldValue.arrayUnion(currentUserId + "-" + currentTimeInMillies)).addOnSuccessListener(aVoid -> Toast.makeText(this, "لقد تم الإبلاغ عن هذا المستخدم!", Toast.LENGTH_SHORT).show());
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteMessages() {

        Dialog dialog = new Dialog(MessagingActivity.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.delete_promo_alert_layout);
        ((TextView)dialog.findViewById(R.id.textView45)).setText("هل تريد حذف المحادثة؟");
        dialog.findViewById(R.id.delete_close).setOnClickListener(v2 -> dialog.dismiss());
        dialog.findViewById(R.id.delete_confirm).setOnClickListener(v -> {
            progressDialog = ProgressDialog.show(MessagingActivity.this, "جاري حذف المحادثة!",
                    "الرجاء الإنتظار!", true);
//            progressDialog.show();
            chatRef.document(documentid).delete()
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(MessagingActivity.this, "لقد فشل حذف المحادثة!", Toast.LENGTH_SHORT).show();
                    });
        });
        dialog.show();
    }

    private void startRecording() {


        if (ActivityCompat.checkSelfPermission(MessagingActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "بدء التسجيل الصوتي...", Toast.LENGTH_SHORT).show();

            if(fileName == null || fileName.isEmpty()){
                fileName = getExternalFilesDir("/").getAbsolutePath() + "/NewRecording.3gp";
            }
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(fileName);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaRecorder.start();
            latestAudioMessageStartTime = System.currentTimeMillis();
        } else {
            ActivityCompat.requestPermissions(MessagingActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

    }

    private void stopRecording() {
        if(mediaRecorder!=null){
            latestAudioMessageEndTime = System.currentTimeMillis();
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            uploadRecording();
            Toast.makeText(this, "توقف التسجيل!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadRecording() {
        findViewById(R.id.sendMessageBtn).setClickable(false);

        ProgressDialog recordingProgress = new ProgressDialog(MessagingActivity.this);
        recordingProgress.setTitle("جاري إرسال التسجيل الصوتي!");
        recordingProgress.show();

        StorageReference recordingPath = FirebaseStorage.getInstance().getReference().child("recordings/" + UUID.randomUUID().toString());
        recordingPath.putFile(Uri.fromFile(new File(fileName))).addOnSuccessListener(taskSnapshot -> recordingPath.getDownloadUrl().addOnSuccessListener(uri -> {
            sendMessage(currentUserId, messagingUserId, uri.toString() + "`" + new SimpleDateFormat("mm:ss", Locale.getDefault()).format(latestAudioMessageEndTime - latestAudioMessageStartTime));
            recordingProgress.dismiss();
        })).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                recordingProgress.dismiss();
                Toast.makeText(MessagingActivity.this, "لقد فشل إرسال التسجيل الصوتي!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    void blockUser() {
        userRef.document(currentDs.getId()).update("usersBlocked", FieldValue.arrayUnion(messagingUserId)).addOnSuccessListener(aVoid -> {
            GlobalVariables.getBlockedUsers().add(messagingUserId);
            Toast.makeText(this, "لقد تم حظر هذا المستخدم!", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        });
    }


    public void showImageFullScreen(String imageUrl) {
        Dialog imageDialog = new Dialog(MessagingActivity.this);
        imageDialog.setCanceledOnTouchOutside(true);
        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imageDialog.setContentView(R.layout.full_screen_layout);
        imageDialog.show();
        Picasso.get().load(imageUrl).into((ImageView) imageDialog.findViewById(R.id.fillScreenTv));
    }


    public void setPromoViewClicker() {
        findViewById(R.id.promoView).setOnClickListener(v -> {
            messagingFrameLayout.setVisibility(View.VISIBLE);
            Bundle bundle = new Bundle();
            PromotionInfoFragment fragment = new PromotionInfoFragment();
            bundle.putSerializable("promo", promotion);
            fragment.setArguments(bundle);
            replacePromoFragment(fragment);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.remove();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (documentid != null) {
            chatRef.document(documentid).update(currentUserId + ":LastSeenMessage", receivedMessages.size());
        }
        sharedPreferences.edit().putBoolean("messagingscreen", false).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences.edit().putBoolean("messagingscreen", true).apply();
    }
}

