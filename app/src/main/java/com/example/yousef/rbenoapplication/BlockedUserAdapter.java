package com.example.yousef.rbenoapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BlockedUserAdapter extends
        RecyclerView.Adapter<BlockedUserAdapter.BlockedUsersViewHolder> {

    private final List<BlockedUser> blockedUsers;
    private final BlockedClickListener blockedClickListener;
    private String currentUid;

    BlockedUserAdapter(List<BlockedUser> blockedUsers, BlockedClickListener blockedClickListener) {
        this.blockedUsers = blockedUsers;
        this.blockedClickListener = blockedClickListener;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public BlockedUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item_layout, parent, false);

        return new BlockedUsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BlockedUsersViewHolder holder, final int position) {
        holder.bind(blockedUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return blockedUsers.size();
    }


    public interface BlockedClickListener {
        void onBlockedClickListener(String id);
    }

    class BlockedUsersViewHolder extends RecyclerView.ViewHolder {

        private final ImageView profileIv, statusIV, removeIv;
        private final TextView usernameTv;
        private final CollectionReference userRef =
                FirebaseFirestore.getInstance().collection("users");


        BlockedUsersViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            statusIV = itemView.findViewById(R.id.statusIV);
            removeIv = itemView.findViewById(R.id.removeIv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
        }


        void bind(BlockedUser user) {
            if (user.getImageurl() != null && !user.getImageurl().isEmpty()) {
                Picasso.get().load(user.getImageurl()).fit().into(profileIv);
            }
            usernameTv.setText(user.getUsername());

            if (user.isStatus()) {
                statusIV.setImageResource(R.drawable.green_circle);
            } else {
                statusIV.setImageResource(R.drawable.red_circle);
            }

            itemView.setOnClickListener(v -> blockedClickListener.onBlockedClickListener(user.getUserId()));


            removeIv.setOnClickListener(view -> {

                if (currentUid == null)
                    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                userRef.whereEqualTo("userId", currentUid)
                        .get().addOnSuccessListener(snapshots ->
                        snapshots.getDocuments().get(0).getReference().update(
                                "usersBlocked",
                                FieldValue.arrayRemove(user.getUserId()))
                                .addOnSuccessListener(v -> {

                                    GlobalVariables.getBlockedUsers().remove(user.getUserId());

                                    blockedUsers.remove(user);
                                    notifyItemRemoved(getAdapterPosition());
                                    Toast.makeText(itemView.getContext(),
                                            "تم الغاء حظر المستخدم!", Toast.LENGTH_SHORT).show();
                                }));

            });
        }
    }


}
