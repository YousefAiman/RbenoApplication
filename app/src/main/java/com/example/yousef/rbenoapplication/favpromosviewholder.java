package com.example.yousef.rbenoapplication;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class favpromosviewholder extends RecyclerView.ViewHolder {

    ImageView promoimage;
    TextView titleTv;
    TextView ownerTv;
    TextView favTv;
    TextView priceTv;
    TextView categoryTv;
    TextView viewsTv;
    // ImageView promomenuimage;
    TextView promoidTv;
    ImageView removeImageView;
    TextView currencyTv;

    favpromosviewholder(@NonNull View itemView) {
        super(itemView);
        promoimage = itemView.findViewById(R.id.myPromoImage);
        titleTv = itemView.findViewById(R.id.myPromoTitleTv);
        ownerTv = itemView.findViewById(R.id.newestPromoSubmitterTv);
        favTv = itemView.findViewById(R.id.myPromopublishFavTv);
        priceTv = itemView.findViewById(R.id.myPromoPriceTv);
        categoryTv = itemView.findViewById(R.id.newestPromoCategoryTv);
        viewsTv = itemView.findViewById(R.id.myPromoViewsTv);
        // promomenuimage =itemView.findViewById(R.id.promoMenuImage);
        promoidTv = itemView.findViewById(R.id.promoIdTv);
        removeImageView = itemView.findViewById(R.id.removeImageView);
        currencyTv = itemView.findViewById(R.id.myPromoCurrencyTv);

    }
}
