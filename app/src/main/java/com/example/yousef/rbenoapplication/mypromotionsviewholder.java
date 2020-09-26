package com.example.yousef.rbenoapplication;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class mypromotionsviewholder extends RecyclerView.ViewHolder {
    ImageView promoimage;
    TextView titleTv;
    TextView priceTv;
    TextView viewsTv;
    TextView publishtimeTv;
    TextView currencyTv;
    TextView descTv;

    mypromotionsviewholder(@NonNull View itemView) {
        super(itemView);
        promoimage = itemView.findViewById(R.id.myPromoImage);
        titleTv = itemView.findViewById(R.id.myPromoTitleTv);
        priceTv = itemView.findViewById(R.id.myPromoPriceTv);
        viewsTv = itemView.findViewById(R.id.myPromoViewsTv);
        publishtimeTv = itemView.findViewById(R.id.myPromopublishTimeTv);
        currencyTv = itemView.findViewById(R.id.myPromoCurrencyTv);
        descTv = itemView.findViewById(R.id.myPromoDesc);
    }
}
