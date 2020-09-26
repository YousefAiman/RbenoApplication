package com.example.yousef.rbenoapplication;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class newestpromotextviewholder extends RecyclerView.ViewHolder {

    ImageView promomenuimage;
    private TextView titleTv;
    private TextView ownerTv;
    private TextView favTv;
    private TextView priceTv;
    private TextView categoryTv;
    private TextView viewsTv;
    private TextView dateTv;
    private TextView descTv;
    private TextView currencyTv;
    private Locale locale = Locale.getDefault();

    newestpromotextviewholder(@NonNull View itemView) {
        super(itemView);
        this.titleTv = itemView.findViewById(R.id.myPromoTitleTextTv);
        this.ownerTv = itemView.findViewById(R.id.newestPromoSubmitterTextTv);
        this.favTv = itemView.findViewById(R.id.myPromopublishFavTextTv);
        this.priceTv = itemView.findViewById(R.id.myPromoPriceTextTv);
        this.categoryTv = itemView.findViewById(R.id.newestPromoCategoryTextTv);
        this.viewsTv = itemView.findViewById(R.id.myPromoViewsTextTv);
        this.promomenuimage = itemView.findViewById(R.id.promoMenuTextImage);
        this.dateTv = itemView.findViewById(R.id.newestPromoDateTextTv);
        this.descTv = itemView.findViewById(R.id.newestPromoDescTextTv);
        this.currencyTv = itemView.findViewById(R.id.myPromoCurrencyTextTv);
    }

    public void bind(Promotion promo) {
        dateTv.setText(TimeConvertor.getTimeAgo(promo.getPublishtime()));
        priceTv.setText(String.format(locale, "%,d", ((long) promo.getPrice())));
        titleTv.setText(promo.getTitle());
        descTv.setText(promo.getDescription());
        currencyTv.setText(promo.getCurrency());
        categoryTv.setText(promo.getType());
        ownerTv.setText(promo.getUserName());
        favTv.setText(promo.getFavcount() + "");
        viewsTv.setText(promo.getViewcount() + "");
        currencyTv.setText(promo.getCurrency());
    }
}
