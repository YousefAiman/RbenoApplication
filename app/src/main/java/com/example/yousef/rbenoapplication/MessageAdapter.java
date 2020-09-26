package com.example.yousef.rbenoapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private static final int MSG_TYPE_RIGHT_IMAGE = 2;
    private static final int MSG_TYPE_LEFT_IMAGE = 3;

    private static final int MSG_TYPE_LEFT_AUDIO = 4;
    private static final int MSG_TYPE_RIGHT_AUDIO = 5;

    static CollectionReference chatsRef = FirebaseFirestore.getInstance().collection("chats");

    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private ArrayList<String> messages;
    private Context context;
    private String documentid;
    static boolean iamSender = false;
    MessageAdapter(ArrayList<String> messages, String documentid, Context context,String senderUid) {
        this.messages = messages;
        this.context = context;
        this.documentid = documentid;
        if(senderUid.equals(currentUserId)){
            iamSender = true;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_chat_item, parent, false));
        } else if (viewType == MSG_TYPE_LEFT) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.received_chat_item, parent, false));
        } else if (viewType == MSG_TYPE_RIGHT_IMAGE) {
            return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_chat_item_image, parent, false));
        } else if (viewType == MSG_TYPE_LEFT_IMAGE) {
            return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.received_chat_item_image, parent, false));
        } else if (viewType == MSG_TYPE_LEFT_AUDIO) {
            return new AudioViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.received_chat_item_audio, parent, false));
        } else {
            return new AudioViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_chat_item_audio, parent, false));
        }
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).hashCode();
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        String message = messages.get(position);

        switch (holder.getItemViewType()) {
            case 0:
            case 1:
                ((ViewHolder) holder).addMessage(message, context, documentid);
                break;
            case 2:
            case 3:
                ((ImageViewHolder) holder).addImageMessage(message, context, documentid, currentUserId);
                break;
            case 4:
            case 5:
                ((AudioViewHolder) holder).addAudioLength(message, context, documentid, position);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {

        String message = messages.get(position);
//        String content = message.split("--")[0];
        int msgType;
        if(iamSender){
            msgType = getMessageType(message,message.split("--")[0],"1");
//            if (message.split("--")[2].equals("1")) {
//                if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/images")) {
//                    return MSG_TYPE_RIGHT_IMAGE;
//                } else if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/recordings")) {
//                    return MSG_TYPE_RIGHT_AUDIO;
//                } else {
//                    return MSG_TYPE_RIGHT;
//                }
//            } else {
//                if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/images")) {
//                    return MSG_TYPE_LEFT_IMAGE;
//                } else if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/recordings")) {
//                    return MSG_TYPE_LEFT_AUDIO;
//                } else {
//                    return MSG_TYPE_LEFT;
//                }
//            }
        }else{
            msgType = getMessageType(message,message.split("--")[0],"2");
//            if (message.split("--")[2].equals("2")) {
//                if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/images")) {
//                    return MSG_TYPE_RIGHT_IMAGE;
//                } else if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/recordings")) {
//                    return MSG_TYPE_RIGHT_AUDIO;
//                } else {
//                    return MSG_TYPE_RIGHT;
//                }
//            } else {
//                if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/images")) {
//                    return MSG_TYPE_LEFT_IMAGE;
//                } else if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/recordings")) {
//                    return MSG_TYPE_LEFT_AUDIO;
//                } else {
//                    return MSG_TYPE_LEFT;
//                }
//            }
        }
        return msgType;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        DateFormat todayYearMonthDayFormat = new SimpleDateFormat("yyyy MMMM dd", Locale.getDefault());
        Date date = new Date();
        DateFormat hourMinuteFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        DateFormat withoutYearFormat = new SimpleDateFormat("h:mm a MMMM dd", Locale.getDefault());
        DateFormat formatter = new SimpleDateFormat("h:mm a yyyy MMMM dd", Locale.getDefault());
        TextView messageTv;
//        ImageView messageImageTv;
//        ProgressBar loadImageProgressbar;
        TextView messageTimeTv;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.messageTv);
//            profileImage = itemView.findViewById(R.id.message_profile_image);
//            messageImageTv = itemView.findViewById(R.id.messageImageTv);
//            loadImageProgressbar = itemView.findViewById(R.id.loadImageProgressbar);
            messageTimeTv = itemView.findViewById(R.id.messageTimeTv);
        }

        void addMessage(String message, Context context, String documentid) {

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if(iamSender){
                        if (message.split("--")[2].equals("1")) {
                            showMessageDeleteDialog(message,context,documentid);
                        }
                    }else{
                        if (message.split("--")[2].equals("2")) {
                            showMessageDeleteDialog(message,context,documentid);
                        }
                    }
//                    if (message.split("--")[2].equals(uid)) {
//                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
//                        alert.setTitle("هل تريد حذف الرسالة؟");
//                        alert.setPositiveButton("حذف", (dialog, which) -> {
//                            chatsRef.document(documentid).update("messages", FieldValue.arrayRemove(message))
//                                    .addOnSuccessListener(aVoid -> dialog.dismiss()).addOnFailureListener(e -> {
//                                Toast.makeText(context, "لقد فشل حذف الرسالة", Toast.LENGTH_SHORT).show();
//                                dialog.dismiss();
//                            });
//                        });
//                        alert.setNegativeButton("إلغاء", (dialog, which) -> dialog.dismiss());
//                        alert.create().show();
//                        return true;
//                    }
                    return true;
                }
            });

            long timeInMillies = Long.parseLong(message.split("--")[1]);
            messageTv.setText(message.split("--")[0]);
            DateFormat todayYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

            if (todayYearMonthDayFormat.format(date).equals(todayYearMonthDayFormat.format(timeInMillies))) {
                messageTimeTv.setText(hourMinuteFormat.format(timeInMillies));
            } else if (todayYearFormat.format(date).equals(todayYearFormat.format(timeInMillies))) {
                messageTimeTv.setText(withoutYearFormat.format(timeInMillies));
            } else {
                messageTimeTv.setText(formatter.format(timeInMillies));
            }

        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        DateFormat todayYearMonthDayFormat = new SimpleDateFormat("yyyy MMMM dd", Locale.getDefault());
        Date date = new Date();
        DateFormat hourMinuteFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        DateFormat withoutYearFormat = new SimpleDateFormat("h:mm a MMMM dd", Locale.getDefault());
        DateFormat todayYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        DateFormat formatter = new SimpleDateFormat("h:mm a yyyy MMMM dd", Locale.getDefault());
        ImageView messageImageTv;
        ProgressBar loadImageProgressbar;
        TextView messageTimeTv;
        Picasso picasso = Picasso.get();


        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageImageTv = itemView.findViewById(R.id.messageImageTv);
            loadImageProgressbar = itemView.findViewById(R.id.loadImageProgressbar);
            messageTimeTv = itemView.findViewById(R.id.messageTimeTv);
        }

        void addImageMessage(String message, Context context, String documentid, String uid) {
            long timeInMillies = Long.parseLong(message.split("--")[1]);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                messageImageTv.setClipToOutline(true);
            } else {
                messageImageTv.setPadding(10, 10, 10, 10);
            }
            if(!message.split("--")[0].equals("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/images")){
                picasso.load(message.split("--")[0]).fit().centerCrop().into(messageImageTv, new Callback() {
                    @Override
                    public void onSuccess() {
                        loadImageProgressbar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                        loadImageProgressbar.setVisibility(View.INVISIBLE);
                    }
                });
            }else{
                loadImageProgressbar.setVisibility(View.VISIBLE);
            }


            messageImageTv.setOnLongClickListener(v -> {
                if(iamSender){
                    if (message.split("--")[2].equals("1")) {
                        showMessageDeleteDialog(message,context,documentid);
                    }
                }else{
                    if (message.split("--")[2].equals("2")) {
                        showMessageDeleteDialog(message,context,documentid);
                    }
                }

//                    if (message.split("--")[2].equals(uid)) {
//                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
//                        alert.setTitle("هل تريد حذف الرسالة؟");
//                        alert.setPositiveButton("حذف", (dialog, which) -> {
//                            chatsRef.document(documentid).update("messages", FieldValue.arrayRemove(message))
//                                    .addOnSuccessListener(aVoid -> dialog.dismiss()).addOnFailureListener(e -> {
//                                Toast.makeText(context, "لقد فشل حذف الرسالة", Toast.LENGTH_SHORT).show();
//                                dialog.dismiss();
//                            });
//                        });
//                        alert.setNegativeButton("إلغاء", (dialog, which) -> dialog.dismiss());
//                        alert.create().show();
//                        return true;
//                    }
                return true;
            });

            messageImageTv.setOnClickListener(v -> ((MessagingActivity) context).showImageFullScreen(message.split("--")[0]));

            if (todayYearMonthDayFormat.format(date).equals(todayYearMonthDayFormat.format(timeInMillies))) {
                messageTimeTv.setText(hourMinuteFormat.format(timeInMillies));
            } else if (todayYearFormat.format(date).equals(todayYearFormat.format(timeInMillies))) {
                messageTimeTv.setText(withoutYearFormat.format(timeInMillies));
            } else {
                messageTimeTv.setText(formatter.format(timeInMillies));
            }

        }
    }

    public static class AudioViewHolder extends RecyclerView.ViewHolder {

        final Handler handler = new Handler();
        DateFormat todayYearMonthDayFormat = new SimpleDateFormat("yyyy MMMM dd", Locale.getDefault());
        Date date = new Date();
        DateFormat hourMinuteFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        DateFormat withoutYearFormat = new SimpleDateFormat("h:mm a MMMM dd", Locale.getDefault());
        DateFormat formatter = new SimpleDateFormat("h:mm a yyyy MMMM dd", Locale.getDefault());
        DateFormat todayYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        ImageView messagePlayBtn;
        TextView messageAudioLengthTv;
        ProgressBar messageAudioProgressBar;
        ProgressBar messageAudioBackProgressBar;
        TextView messageTimeTv;
        boolean isPlaying;
        boolean hasDataSource = false;
        boolean isPaused = false;
        Runnable progressRunnable;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Runnable runnable;
        MediaPlayer mediaPlayer = new MediaPlayer();
        DateFormat secondMinuteFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        int lastClickedPlayer = -1;

        AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            messagePlayBtn = itemView.findViewById(R.id.messagePlayBtn);
            messageAudioLengthTv = itemView.findViewById(R.id.messageAudioLengthTv);
            messageAudioProgressBar = itemView.findViewById(R.id.messageAudioProgressBar);
            messageAudioBackProgressBar = itemView.findViewById(R.id.messageAudioBackProgressBar);
            messageTimeTv = itemView.findViewById(R.id.messageTimeTv);
        }

        void addAudioLength(String message, Context context, String documentid,int position) {

            long timeInMillies = Long.parseLong(message.split("--")[1]);

            if (todayYearMonthDayFormat.format(date).equals(todayYearMonthDayFormat.format(timeInMillies))) {
                messageTimeTv.setText(hourMinuteFormat.format(timeInMillies));
            } else if (todayYearFormat.format(date).equals(todayYearFormat.format(timeInMillies))) {
                messageTimeTv.setText(withoutYearFormat.format(timeInMillies));
            } else {
                messageTimeTv.setText(formatter.format(timeInMillies));
            }
            isPlaying = false;
            String[] messageSplit = message.split("--");
            String content = messageSplit[0];

            //long time = Long.parseLong(content.split("`")[1]);
            String duration = content.split("`")[1];
            messageAudioLengthTv.setText(duration);

            hasDataSource = false;
            isPaused = false;


            itemView.setOnLongClickListener(v -> {
                if(iamSender){
                    if (message.split("--")[2].equals("1")) {
                        showMessageDeleteDialog(message,context,documentid);
                    }
                }else{
                    if (message.split("--")[2].equals("2")) {
                        showMessageDeleteDialog(message,context,documentid);
                    }
                }
                return true;
            });


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(lastClickedPlayer!=-1 && lastClickedPlayer!=position){
                        mediaPlayer.stop();
                    }
                    lastClickedPlayer = position;
                    if (!mediaPlayer.isPlaying()) {
                        if (isPaused) {
                            messagePlayBtn.setVisibility(View.VISIBLE);
                            if(iamSender){
                                if (messageSplit[2].equals("1")) {
                                    messagePlayBtn.setImageResource(R.drawable.ic_pause_white);
                                } else {
                                    messagePlayBtn.setImageResource(R.drawable.ic_pause_grey);
                                }
                            }else{
                                if (messageSplit[2].equals("2")) {
                                    messagePlayBtn.setImageResource(R.drawable.ic_pause_white);
                                } else {
                                    messagePlayBtn.setImageResource(R.drawable.ic_pause_grey);
                                }
                            }

                            mediaPlayer.start();
                            isPaused = false;
                        } else {
                            mediaPlayer.stop();
                            mediaPlayer.reset();


                            messagePlayBtn.setVisibility(View.GONE);
                            messageAudioProgressBar.setVisibility(View.VISIBLE);
                            messageAudioProgressBar.setEnabled(true);
                            if (!hasDataSource) {
                                try {
                                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    mediaPlayer.setDataSource(content.split("`")[0]);
                                    mediaPlayer.prepareAsync();
                                    mediaPlayer.setOnPreparedListener(mp -> {
                                        hasDataSource = true;
                                        messageAudioProgressBar.setVisibility(View.GONE);
                                        messageAudioProgressBar.setEnabled(false);
                                        if(iamSender){
                                            if (messageSplit[2].equals("1")) {
                                                messagePlayBtn.setImageResource(R.drawable.ic_pause_white);
                                            } else {
                                                messagePlayBtn.setImageResource(R.drawable.ic_pause_grey);
                                            }
                                        }else{
                                            if (messageSplit[2].equals("2")) {
                                                messagePlayBtn.setImageResource(R.drawable.ic_pause_white);
                                            } else {
                                                messagePlayBtn.setImageResource(R.drawable.ic_pause_grey);
                                            }
                                        }

                                        messagePlayBtn.setVisibility(View.VISIBLE);
                                        mediaPlayer.start();
                                        int delay =  mediaPlayer.getDuration()/100;
                                        progressRunnable = () -> {
                                            messageAudioBackProgressBar.setProgress((int)((float)mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration()*100));
//                                            Log.d("audioPercent","percent: "+(int)((float)mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration()*100));
                                            messageAudioBackProgressBar.postDelayed(progressRunnable,delay);
                                        };
                                        messageAudioBackProgressBar.post(progressRunnable);

                                        runnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                messageAudioLengthTv.setText(secondMinuteFormat.format((long)mediaPlayer.getCurrentPosition()));
                                                messageAudioLengthTv.postDelayed(this, 1000);
                                            }
                                        };
                                        messageAudioLengthTv.post(runnable);
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                mediaPlayer.stop();
                                mediaPlayer.reset();
                                try {
                                    mediaPlayer.setDataSource(content.split("`")[0]);
                                    mediaPlayer.prepareAsync();
                                    mediaPlayer.setOnPreparedListener(mp -> {
                                        mediaPlayer.start();

                                        messageAudioProgressBar.setVisibility(View.GONE);
                                        messagePlayBtn.setVisibility(View.VISIBLE);

                                        int delay =  mediaPlayer.getDuration()/100;
                                        progressRunnable = () -> {

                                            messageAudioBackProgressBar.setProgress((int)((float)mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration()*100));
                                            messageAudioBackProgressBar.postDelayed(progressRunnable, delay);
                                        };
                                        messageAudioBackProgressBar.post(progressRunnable);

                                        runnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                messageAudioLengthTv.setText(secondMinuteFormat.format((long)mediaPlayer.getCurrentPosition()));
                                                messageAudioLengthTv.postDelayed(this, 1000);
                                            }
                                        };
                                        messageAudioLengthTv.post(runnable);

                                        if(iamSender){
                                            if (messageSplit[2].equals("1")) {
                                                messagePlayBtn.setImageResource(R.drawable.ic_pause_white);
                                            } else {
                                                messagePlayBtn.setImageResource(R.drawable.ic_pause_grey);
                                            }
                                        }else{
                                            if (messageSplit[2].equals("2")) {
                                                messagePlayBtn.setImageResource(R.drawable.ic_pause_white);
                                            } else {
                                                messagePlayBtn.setImageResource(R.drawable.ic_pause_grey);
                                            }
                                        }

                                    });

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    } else {
                        isPaused = true;
                        messagePlayBtn.setVisibility(View.VISIBLE);
                        if(iamSender){
                            if (messageSplit[2].equals("1")) {
                                messagePlayBtn.setImageResource(R.drawable.ic_play_arrow_white);
                            } else {
                                messagePlayBtn.setImageResource(R.drawable.ic_play_arrow_grey);
                            }
                        }else{
                            if (messageSplit[2].equals("2")) {
                                messagePlayBtn.setImageResource(R.drawable.ic_play_arrow_white);
                            } else {
                                messagePlayBtn.setImageResource(R.drawable.ic_play_arrow_grey);
                            }
                        }
                        mediaPlayer.pause();
                    }
                    mediaPlayer.setOnCompletionListener(mp -> {
                        if(iamSender){
                            if (messageSplit[2].equals("1")) {
                                messagePlayBtn.setImageResource(R.drawable.ic_play_arrow_white);
                            } else {
                                messagePlayBtn.setImageResource(R.drawable.ic_play_arrow_grey);
                            }
                        }else{
                            if (messageSplit[2].equals("2")) {
                                messagePlayBtn.setImageResource(R.drawable.ic_play_arrow_white);
                            } else {
                                messagePlayBtn.setImageResource(R.drawable.ic_play_arrow_grey);
                            }
                        }

                        messageAudioLengthTv.setText(duration);
                        isPaused = false;
//                        handler.removeCallbacks(runnable);
//                        handler.removeCallbacks(progressRunnable);
                        messageAudioLengthTv.removeCallbacks(runnable);
                        messageAudioBackProgressBar.removeCallbacks(progressRunnable);
                        messageAudioBackProgressBar.setProgress(0);
                    });
                }
            });
        }
    }
    int getMessageType(String message,String content,String sender){
        if (message.split("--")[2].equals(sender)) {
            if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/images")) {
                return MSG_TYPE_RIGHT_IMAGE;
            } else if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/recordings")) {
                return MSG_TYPE_RIGHT_AUDIO;
            } else {
                return MSG_TYPE_RIGHT;
            }
        } else {
            if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/images")) {
                return MSG_TYPE_LEFT_IMAGE;
            } else if (content.contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com/o/recordings")) {
                return MSG_TYPE_LEFT_AUDIO;
            } else {
                return MSG_TYPE_LEFT;
            }
        }
    }
    static void showMessageDeleteDialog(String message,Context context,String documentid){
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("هل تريد حذف الرسالة؟");
        alert.setPositiveButton("حذف", (dialog, which) -> {
            chatsRef.document(documentid).update("messages", FieldValue.arrayRemove(message))
                    .addOnSuccessListener(aVoid -> dialog.dismiss()).addOnFailureListener(e -> {
                Toast.makeText(context, "لقد فشل حذف الرسالة", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        alert.setNegativeButton("إلغاء", (dialog, which) -> dialog.dismiss());
        alert.create().show();
    }


}
