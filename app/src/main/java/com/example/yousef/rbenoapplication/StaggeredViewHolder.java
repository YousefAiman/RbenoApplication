package com.example.yousef.rbenoapplication;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.Locale;

public class StaggeredViewHolder extends RecyclerView.ViewHolder {
    private ImageView promoimage;
    private TextView titleTv;
    private TextView innerTitleTv;
    private TextView priceTv;
    private TextView viewsTv;
    private TextView publishtimeTv;
    private RequestOptions requestOptions = new RequestOptions();
    private TextView currencyTv;
    private int newMeasuredwidth;
    private ImageView videoPlayIv;
    StaggeredViewHolder(@NonNull View itemView) {
        super(itemView);
        this.promoimage = itemView.findViewById(R.id.myPromoImage);
        this.titleTv = itemView.findViewById(R.id.titleTv);
        this.innerTitleTv = itemView.findViewById(R.id.myPromoTitleTv);
        this.priceTv = itemView.findViewById(R.id.myPromoPriceTv);
        this.viewsTv = itemView.findViewById(R.id.myPromoViewsTv);
        this.publishtimeTv = itemView.findViewById(R.id.myPromopublishTimeTv);
        this.currencyTv = itemView.findViewById(R.id.currencyTv);
        this.videoPlayIv  = itemView.findViewById(R.id.videoPlayIv);
        newMeasuredwidth = GlobalVariables.getWindowWidth();

    }

    private static Activity unwrap(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        return (Activity) context;
    }

    public void bind(final Promotion p, int i, Context context) {

//        if (i % 2 == 0) {
//            itemView.setPadding(0, 0, 5, 0);
//        }

        switch (p.getPromoType()) {
            case "image":
                innerTitleTv.setText(p.getTitle());
                innerTitleTv.setEllipsize(TextUtils.TruncateAt.END);
                Glide.with(itemView.getContext()).load(p.getPromoimages().get(0)).apply(requestOptions.override(newMeasuredwidth / 2, Target.SIZE_ORIGINAL)).into(promoimage);
                titleTv.setVisibility(View.GONE);
                innerTitleTv.setVisibility(View.VISIBLE);
                promoimage.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                break;
            case "video":
                innerTitleTv.setText(p.getTitle());
                innerTitleTv.setVisibility(View.VISIBLE);
                innerTitleTv.setEllipsize(TextUtils.TruncateAt.END);
                Glide.with(itemView.getContext()).load(p.getVideoThumbnail()).apply(requestOptions.override(newMeasuredwidth / 2, Target.SIZE_ORIGINAL)).into(promoimage);
                promoimage.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                titleTv.setVisibility(View.GONE);
                videoPlayIv.setVisibility(View.VISIBLE);
                break;
            case "text":
                innerTitleTv.setVisibility(View.GONE);
                titleTv.setVisibility(View.VISIBLE);
                titleTv.setText(p.getTitle());
                promoimage.setImageResource(R.drawable.red_staggered_background);
                promoimage.getLayoutParams().height = newMeasuredwidth / 3;
                switch (p.getType()) {
                    case "أثاث":
                        DrawableCompat.setTint(
                                promoimage.getDrawable(),
                                ContextCompat.getColor(context, R.color.greenFurniture));
                        break;
                    case "موبيلات":
                        DrawableCompat.setTint(
                                promoimage.getDrawable(),
                                ContextCompat.getColor(context, R.color.blueMobile));
                        break;
                    case "سيارات":
                        DrawableCompat.setTint(
                                promoimage.getDrawable(),
                                ContextCompat.getColor(context, R.color.red));
                        break;
                    case "كمبيوتر و لاب توب":
                    case "اليكترونيات":
                        DrawableCompat.setTint(
                                promoimage.getDrawable(),
                                ContextCompat.getColor(context, R.color.pcBlack));
                        break;
                    case "عقارات":
                        DrawableCompat.setTint(
                                promoimage.getDrawable(),
                                ContextCompat.getColor(context, R.color.houseOrange));
                        break;
                }
                break;
        }
        publishtimeTv.setText(TimeConvertor.getTimeAgo(p.getPublishtime()));
        viewsTv.setText(p.getViewcount() + "");
        priceTv.setText(String.format(Locale.getDefault(), "%,d", ((long) p.getPrice())));
        currencyTv.setText(p.getCurrency());

        itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            PromotionInfoFragment fragment = new PromotionInfoFragment();
            bundle.putSerializable("promo", p);
            fragment.setArguments(bundle);
            if (context instanceof HomeActivity) {
                ((HomeActivity) context).replacePromoFragment(fragment);
            } else if (context instanceof MessagingActivity) {
                ((MessagingActivity) context).replacePromoFragment(fragment);
            } else {
                ((HomeActivity) unwrap(v.getContext())).replacePromoFragment(fragment);
            }
        });
    }
}