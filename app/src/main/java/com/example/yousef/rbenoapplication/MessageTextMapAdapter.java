package com.example.yousef.rbenoapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageTextMapAdapter extends RecyclerView.Adapter<MessageTextMapAdapter.ViewHolder> {

//  private final static int DOCUMENT_MESSAGE_LIMIT = 10;

    private final DateFormat
            hourMinuteFormat = new SimpleDateFormat("h:mm a", Locale.getDefault()),
            withoutYearFormat = new SimpleDateFormat("h:mm a MMM dd", Locale.getDefault()),
            formatter = new SimpleDateFormat("h:mm a yyyy MMM dd", Locale.getDefault()),
            todayYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault()),
            todayYearMonthDayFormat = new SimpleDateFormat("yyyy MMM dd", Locale.getDefault());

    private static final Date date = new Date();

    private static final int
            MSG_TYPE_LEFT = 0,
            MSG_TYPE_RIGHT = 1;

    private boolean longCLickEnabled = true;
    //  static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
//          .child("Messages");
    private final boolean iamSender;
    private final ArrayList<MessageMap> messages;
    private final Context context;
    private final DeleteMessageListener deleteMessageListener;

    public interface DeleteMessageListener {
        void deleteMessage(MessageMap messageMap, DialogInterface dialog);
    }

    void disableLongClick() {
        longCLickEnabled = false;
    }

    MessageTextMapAdapter(ArrayList<MessageMap> messages,
                          Context context,
                          String senderUid,
                          DeleteMessageListener deleteMessageListener) {
        this.messages = messages;
        this.context = context;
        this.deleteMessageListener = deleteMessageListener;
        iamSender = senderUid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }


    @NonNull
    @Override
    public MessageTextMapAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sent_chat_item, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.received_chat_item, parent, false));
        }
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull MessageTextMapAdapter.ViewHolder holder, int position) {
        holder.addMessage(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {

//    final MessageMap message = messages.get(position);

//    if(position!=-1 && message!=null) {
        if (iamSender) {
            return getMessageType(messages.get(position).getSender(), 1);
        } else {
            return getMessageType(messages.get(position).getSender(), 2);
        }
//    }

//    return -1;
    }

    int getMessageType(int identifier, int sender) {
        if (identifier == sender) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener,
            View.OnClickListener {

        private final TextView messageTv, messageTimeTv;
        private boolean timeIsVisible;
//    private final ScheduledThreadPoolExecutor background
//            = new ScheduledThreadPoolExecutor(5);

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            messageTv = itemView.findViewById(R.id.messageTv);
            messageTimeTv = itemView.findViewById(R.id.messageTimeTv);

        }

        void addMessage(MessageMap message) {

            if (message == null)
                return;

            if (!message.getDeleted()) {
//        final WeakReference<TextView> textViewRef = new WeakReference<>(messageTimeTv);
//
//        final PrecomputedTextCompat.Params params =  TextViewCompat.getTextMetricsParams(messageTv);
//
//        background.execute(() -> {
//
//          final PrecomputedTextCompat preText =
//                  PrecomputedTextCompat.create((CharSequence) message.get("M"),params);
//          final TextView tv = textViewRef.get();
//
//          tv.post(() -> tv.setText(preText));
//        });
//        background.submit(() -> {
//
//
//
//        });
//        TextViewCompat.setPrecomputedText(messageTimeTv,
//                PrecomputedTextCompat.create((String)message.get("M"),params));
                messageTv.setText(message.getContent());
            } else {
                messageTv.setText("تم حذف هذه الرسالة");
            }

            itemView.setOnClickListener(this);

            if (!message.getDeleted()) {
                itemView.setOnLongClickListener(this);
            }

            if (timeIsVisible) {
                messageTimeTv.setText(getTimeFormatted(messages.get(getAdapterPosition()).getTime()));
                messageTimeTv.setVisibility(View.VISIBLE);
            } else {
                messageTimeTv.setVisibility(View.GONE);
            }

//      final Matcher m = pattern.matcher(message);
//      if(m.find()){
//
//        makeMessageALink(message.substring(m.start(),m.end()));
//        Log.d("ttt",message.substring(m.start(),m.end()));
//        Log.d("ttt","matcher found a link");
//      }
//      while (matcher.find()) {
//        Log.d("ttt",matcher.start()+" : "+matcher.end());
////        makeMessageALink();
//      }

//      for(String messageSplit:message.split(" "))
//      if(message.contains(URLUtil.isValidUrl(message)))
        }
//
//    void makeMessageALink(String link) {
//      messageTv.setMovementMethod(LinkMovementMethod.getInstance());
//      messageTv.setLinkTextColor(Color.BLUE);

        @Override
        public boolean onLongClick(View view) {

            final MessageMap message = messages.get(getAdapterPosition());

            if (iamSender) {
                if (message.getSender() == 1) {
                    showMessageDeleteDialog(message);
                }
            } else {
                if (message.getSender() == 2) {
                    showMessageDeleteDialog(message);
                }
            }
            return longCLickEnabled;
        }

        @Override
        public void onClick(View view) {


            if (messageTimeTv.getVisibility() == View.GONE) {
                messageTimeTv.setText(getTimeFormatted(messages.get(getAdapterPosition()).getTime()));
                messageTimeTv.setVisibility(View.VISIBLE);
                timeIsVisible = true;
            } else {
                messageTimeTv.setVisibility(View.GONE);
                timeIsVisible = false;
            }

        }

    }

    void showMessageDeleteDialog(MessageMap messageMap) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("هل تريد حذف الرسالة؟");
        alert.setPositiveButton("حذف", (dialog, which) -> {
            deleteMessageListener.deleteMessage(messageMap, dialog);
        });
        alert.setNegativeButton("إلغاء", (dialog, which) -> dialog.dismiss());
        alert.create().show();
    }

    private String getTimeFormatted(long time) {

        if (time < 1000000000000L) {
            time *= 1000;
        }
        if (todayYearMonthDayFormat.format(date)
                .equals(todayYearMonthDayFormat.format(time))) {
            return hourMinuteFormat.format(time);

        } else if (todayYearFormat.format(date).equals(todayYearFormat.format(time))) {
            return withoutYearFormat.format(time);
        } else {
            return formatter.format(time);
        }
    }

}
