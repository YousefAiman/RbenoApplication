package com.example.yousef.rbenoapplication;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

public class newestpromosviewholder extends RecyclerView.ViewHolder {

    ImageView promomenuimage;
    private ImageView promoimage;
    private TextView titleTv;
    private TextView ownerTv;
    private TextView favTv;
    private TextView priceTv;
    private TextView categoryTv;
    private TextView viewsTv;
    private TextView dateTv;
    private Picasso picasso = Picasso.get();
    private TextView currencyTv;
    private Locale locale = Locale.getDefault();

    newestpromosviewholder(@NonNull View itemView) {
        super(itemView);
        promoimage = itemView.findViewById(R.id.myPromoImage);
        titleTv = itemView.findViewById(R.id.myPromoTitleTv);
        ownerTv = itemView.findViewById(R.id.newestPromoSubmitterTv);
        favTv = itemView.findViewById(R.id.myPromopublishFavTv);
        priceTv = itemView.findViewById(R.id.myPromoPriceTv);
        categoryTv = itemView.findViewById(R.id.newestPromoCategoryTv);
        viewsTv = itemView.findViewById(R.id.myPromoViewsTv);
        promomenuimage = itemView.findViewById(R.id.promoMenuImage);
        dateTv = itemView.findViewById(R.id.newestPromoDateTv);
        currencyTv = itemView.findViewById(R.id.myPromoCurrencyTv);

    }

    private static Activity unwrap(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        return (Activity) context;
    }


    public void bind(Promotion promo) {

        if (promo.getPromoimages() != null && !promo.getPromoimages().isEmpty()) {
            picasso.load(promo.getPromoimages().get(0))
                    .centerCrop().fit().into(promoimage);
            promoimage.setOnClickListener(v -> ((HomeActivity) unwrap(v.getContext())).showImageFullScreen(promo.getPromoimages().get(0)));
        } else {
            picasso.load(promo.getVideoThumbnail())
                    .centerCrop().fit().into(promoimage);
            promoimage.setOnClickListener(v -> ((HomeActivity) unwrap(v.getContext())).showImageFullScreen(promo.getVideoThumbnail()));
        }
        currencyTv.setText(promo.getCurrency());
        dateTv.setText(TimeConvertor.getTimeAgo(promo.getPublishtime()));
        priceTv.setText(String.format(locale, "%,d", (long) promo.getPrice()));
        titleTv.setText(promo.getTitle());
        categoryTv.setText(promo.getType());
        ownerTv.setText(promo.getUserName());
        favTv.setText(promo.getFavcount() + "");
        viewsTv.setText(promo.getViewcount() + "");
    }

}

