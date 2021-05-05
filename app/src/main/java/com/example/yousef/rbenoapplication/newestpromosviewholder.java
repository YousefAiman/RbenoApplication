package com.example.yousef.rbenoapplication;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

public class newestpromosviewholder extends RecyclerView.ViewHolder {
    private final ImageView promoimage;
    private final TextView titleTv, priceTv, categoryTv, viewsTv, dateTv, myPromoDescTv;
    private final Picasso picasso = Picasso.get();
    private final Locale locale = Locale.getDefault();
    ImageView myPromoHeartIv;
    TextView favTv;

    newestpromosviewholder(@NonNull View itemView) {
        super(itemView);
        promoimage = itemView.findViewById(R.id.myPromoImage);
        titleTv = itemView.findViewById(R.id.myPromoTitleTv);
        favTv = itemView.findViewById(R.id.myPromopublishFavTv);
        priceTv = itemView.findViewById(R.id.myPromoPriceTv);
        categoryTv = itemView.findViewById(R.id.newestPromoCategoryTv);
        viewsTv = itemView.findViewById(R.id.myPromoViewsTv);
        dateTv = itemView.findViewById(R.id.newestPromoDateTv);
        myPromoHeartIv = itemView.findViewById(R.id.myPromoHeartIv);
        myPromoDescTv = itemView.findViewById(R.id.myPromoDescTv);
    }

//  private static Activity unwrap(Context context) {
//    while (!(context instanceof Activity) && context instanceof ContextWrapper) {
//      context = ((ContextWrapper) context).getBaseContext();
//    }
//    return (Activity) context;
//  }
//

    public void bind(final Promotion promo) {

//    myPromoHeartIv.setOnClickListener(v-> itemListener.heartViewClickListener(getAdapterPosition()));

        if (GlobalVariables.getFavPromosIds().contains(promo.getPromoid())) {
            myPromoHeartIv.setImageResource(R.drawable.heart_icon);
        } else {
            myPromoHeartIv.setImageResource(R.drawable.heart_grey_outlined);
        }

        switch (promo.getPromoType()) {

            case "image":
                picasso.load(promo.getPromoimages().get(0))
                        .centerCrop().fit().into(promoimage);
                itemView.findViewById(R.id.myPromoArrow).setVisibility(View.GONE);
                break;

            case "video":
                picasso.load(promo.getVideoThumbnail())
                        .centerCrop().fit().into(promoimage);

                itemView.findViewById(R.id.myPromoArrow).setVisibility(View.VISIBLE);
                break;

            case "text":
                itemView.findViewById(R.id.myPromoArrow).setVisibility(View.GONE);
                promoimage.setBackgroundResource(R.color.red);

        }
//    if (promo.getPromoimages() != null && !promo.getPromoimages().isEmpty()) {
//      picasso.load(promo.getPromoimages().get(0))
//              .centerCrop().fit().into(promoimage);
////      promoimage.setOnClickListener(v -> ((HomeActivity) unwrap(v.getContext())).showImageFullScreen(promo.getPromoimages().get(0)));
//    } else if (promo.getVideoThumbnail() != null && !promo.getVideoThumbnail().isEmpty()) {
//      picasso.load(promo.getVideoThumbnail())
//              .centerCrop().fit().into(promoimage);
////      promoimage.setOnClickListener(v -> ((HomeActivity) unwrap(v.getContext())).showImageFullScreen(promo.getVideoThumbnail()));
//      itemView.findViewById(R.id.myPromoArrow).setVisibility(View.VISIBLE);
//    } else {
//      promoimage.setBackgroundResource(R.color.red);
//    }
        dateTv.setText(TimeConvertor.getTimeAgo(promo.getPublishtime()));
        priceTv.setText(String.format(locale, "%,d", (long) promo.getPrice()) + " " + promo.getCurrency());
        titleTv.setText(promo.getTitle());
        categoryTv.setText(promo.getType());
        favTv.setText(promo.getFavcount() + "");
        viewsTv.setText(promo.getViewcount() + "");
    }

    public void bindTextPromo(final Promotion promo) {

        promoimage.setBackgroundResource(R.color.red);
        if (GlobalVariables.getFavPromosIds().contains(promo.getPromoid())) {
            myPromoHeartIv.setImageResource(R.drawable.heart_icon);
        } else {
            myPromoHeartIv.setImageResource(R.drawable.heart_grey_outlined);
        }
        myPromoDescTv.setText(promo.getDescription());
        dateTv.setText(TimeConvertor.getTimeAgo(promo.getPublishtime()));

        priceTv.setText(String.format(locale, "%,d", (long) promo.getPrice()) + " " + promo.getCurrency());
        titleTv.setText(promo.getTitle());
        categoryTv.setText(promo.getType());
        favTv.setText(promo.getFavcount() + "");
        viewsTv.setText(promo.getViewcount() + "");
    }


    public void makePromoDeleted() {
        if (promoimage.getDrawable() != ResourcesCompat.getDrawable(itemView.getResources()
                , R.drawable.ic_delete_grey, null)) {
            promoimage.setImageResource(R.drawable.ic_delete_grey);
            promoimage.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.white));
            if (myPromoDescTv != null) {
                myPromoDescTv.setVisibility(View.GONE);
            }
        }
    }


}

