package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessagingUserAdapter extends RecyclerView.Adapter<MessagingUserAdapter.ViewHolder> {
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private Context context;
    private List<UserMessage> chattingUsers;

    MessagingUserAdapter(Context context, List<UserMessage> chattingUsers) {
        this.context = context;
        this.chattingUsers = chattingUsers;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chattinguseritem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final UserMessage userMessage = chattingUsers.get(position);
//        if (position == chattingUsers.size() - 1) {
        long messageUnread = userMessage.getMessagesCount() - userMessage.getLastMessageRead();
        if (messageUnread == 0) {
            holder.unreadMessagesCountTv.setVisibility(View.INVISIBLE);
        } else {
            holder.unreadMessagesCountTv.setVisibility(View.VISIBLE);
            holder.unreadMessagesCountTv.setText(messageUnread + "");
        }
//        }

        holder.chattingUserNameTv.setText(userMessage.getChattingUsername());
        holder.chattinglatestMessageTv.setText(userMessage.getChattingLatestMessage());
        String timeAgo = TimeConvertor.getTimeAgo(userMessage.getChattingLatestMessageTime());
        holder.chattingMessageTimeStampTv.setText(timeAgo);
        holder.chattingPromotionIdTv.setText(userMessage.getChattingPromoId() + "");
        if(userMessage.getChattingUserImage()!=null && !userMessage.getChattingUserImage().isEmpty()){
            Picasso.get().load(userMessage.getChattingUserImage()).fit().into(holder.chattingUserImageView);
        }

//        Glide.with(context).load(userMessage.getChattingUserImage()).into(holder.chattingUserImageView);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), MessagingActivity.class);
            intent.putExtra("promouserid", userMessage.getMessagingUserId());
            intent.putExtra("currentuserid", currentUserId);
            intent.putExtra("intendedpromoid", userMessage.getChattingPromoId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chattingUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView chattingUserImageView;
        private TextView chattingUserNameTv;
        private TextView chattingPromotionIdTv;
        private TextView chattingMessageTimeStampTv;
        private TextView chattinglatestMessageTv;
        private TextView unreadMessagesCountTv;

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
