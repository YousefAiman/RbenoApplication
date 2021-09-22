package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MessagingUserAdapter extends RecyclerView.Adapter<MessagingUserAdapter.ViewHolder> {
    //  private final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final Context context;
    private final List<UserMessage> chattingUsers;

    MessagingUserAdapter(Context context, List<UserMessage> chattingUsers) {
        this.context = context;
        this.chattingUsers = chattingUsers;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_message_item_layout, parent,
                false);
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final UserMessage userMessage = chattingUsers.get(position);
        final long messageUnread = userMessage.getMessagesCount() - userMessage.getLastMessageRead();
        if (messageUnread == 0) {
            holder.unreadMessagesCountTv.setVisibility(View.INVISIBLE);
            holder.chattinglatestMessageTv.setTextColor(ContextCompat.getColor(context,
                    R.color.message_color_light));
        } else {

            holder.unreadMessagesCountTv.setVisibility(View.VISIBLE);
            holder.unreadMessagesCountTv.setText(messageUnread + "");
            holder.chattinglatestMessageTv.setTextColor(ContextCompat.getColor(context,
                    R.color.message_color_dark));

        }

        holder.chattingUserNameTv.setText(userMessage.getChattingUsername());

        if (!userMessage.getChattingLatestMessageMap().getDeleted()) {
            holder.chattinglatestMessageTv.setText
                    (userMessage.getChattingLatestMessageMap().getContent());
        } else {
            holder.chattinglatestMessageTv.setText("لقد تم حذف هذه الرسالة");
        }


        holder.chattingMessageTimeStampTv.setText(
                TimeConvertor.getTimeAgo(userMessage.getChattingLatestMessageMap().getTime()));


//    holder.chattinglatestMessageTv.setText(userMessage.getChattingLatestMessage());
//    holder.chattingMessageTimeStampTv.setText(TimeConvertor.getTimeAgo(userMessage.getChattingLatestMessageTime()));
        holder.chattingPromotionIdTv.setText(userMessage.getChattingPromoId() + "");
        if (userMessage.getChattingUserImage() != null && !userMessage.getChattingUserImage().isEmpty()) {
            Picasso.get().load(userMessage.getChattingUserImage()).fit().into(holder.chattingUserImageView);
        }

        holder.itemView.setOnClickListener(v -> {
            if (WifiUtil.checkWifiConnection(context)) {

                context.startActivity(new Intent(context, MessagingRealTimeActivity.class)
                        .putExtra("promouserid", userMessage.getMessagingUserId())
                        .putExtra("intendedpromoid", userMessage.getChattingPromoId()));

            }
        });
    }

    @Override
    public int getItemCount() {
        return chattingUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView chattingUserImageView;
        private final TextView chattingUserNameTv;
        private final TextView chattingPromotionIdTv;
        private final TextView chattingMessageTimeStampTv;
        private final TextView chattinglatestMessageTv;
        private final TextView unreadMessagesCountTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chattingUserImageView = itemView.findViewById(R.id.ChatterImageView);
            chattingUserNameTv = itemView.findViewById(R.id.ChatterUserNameTv);
            chattingPromotionIdTv = itemView.findViewById(R.id.ChattingPromotionIdTv);
            chattingMessageTimeStampTv = itemView.findViewById(R.id.LastChattingMessageTimeTv);
            chattinglatestMessageTv = itemView.findViewById(R.id.LatestMessageTv);
            unreadMessagesCountTv = itemView.findViewById(R.id.unreadMessagesCountTv);

        }


    }

}
