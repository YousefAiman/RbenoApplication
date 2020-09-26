package com.example.yousef.rbenoapplication;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class notificationViewHolder extends RecyclerView.ViewHolder {

    ImageView notificationImage;
    TextView notificationNameTv;
    TextView notificationPromoTv;
    TextView notificationDescTv;
    TextView notificationTimeTv;
    ImageView notificationStatusImage;

    notificationViewHolder(@NonNull View itemView) {
        super(itemView);
        notificationImage = itemView.findViewById(R.id.notificationImage);
        notificationNameTv = itemView.findViewById(R.id.notificationNameTv);
        notificationPromoTv = itemView.findViewById(R.id.notificationPromoTv);
        notificationDescTv = itemView.findViewById(R.id.notificationDescTv);
        notificationTimeTv = itemView.findViewById(R.id.notificationTimeTv);
        notificationStatusImage = itemView.findViewById(R.id.notificationStatusImage);
    }

}