package com.example.yousef.rbenoapplication;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class favpromotextviewholder extends RecyclerView.ViewHolder {

    TextView titleTv;
    TextView ownerTv;
    TextView favTv;
    TextView priceTv;
    TextView categoryTv;
    TextView viewsTv;
    TextView descTv;
    TextView promoidTv;
    ImageView removeImageView;
    TextView currencyTv;

    favpromotextviewholder(@NonNull View itemView) {
        super(itemView);
        titleTv = itemView.findViewById(R.id.myPromoTitleTv);
        ownerTv = itemView.findViewById(R.id.newestPromoSubmitterTextTv);
        favTv = itemView.findViewById(R.id.myPromoPublishFavTextTv);
        priceTv = itemView.findViewById(R.id.myPromoPriceTv);
        categoryTv = itemView.findViewById(R.id.newestPromoCategoryTv);
        viewsTv = itemView.findViewById(R.id.myPromoViewsTextTv);
        descTv = itemView.findViewById(R.id.newestPromoDescTv);
        promoidTv = itemView.findViewById(R.id.promoIdTv);
        removeImageView = itemView.findViewById(R.id.removeImageView);
        currencyTv = itemView.findViewById(R.id.myPromoCurrencyTextTv);

    }
}
