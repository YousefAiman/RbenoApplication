package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

public class MyPromotionsAdapter extends RecyclerView.Adapter<mypromotionsviewholder> {

    ArrayList<Promotion> mypromotions;
    Context context;
    Locale locale = Locale.getDefault();
    int height = GlobalVariables.getWindowHeight() / 3;

    MyPromotionsAdapter(ArrayList<Promotion> mypromotions, Context context) {
        this.mypromotions = mypromotions;
        this.context = context;
    }


    @NonNull
    @Override
    public mypromotionsviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new mypromotionsviewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.mypromo_item_design, parent, false));
    }

    @Override
    public long getItemId(int position) {
        return mypromotions.get(position).hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull mypromotionsviewholder holder, int position) {
        holder.itemView.getLayoutParams().height = height;
        final Promotion p = mypromotions.get(position);

        holder.titleTv.setText(p.getTitle());
        holder.viewsTv.setText(p.getViewcount() + "");
        holder.currencyTv.setText(p.getCurrency());
        holder.priceTv.setText(String.format(locale, "%,d", ((long) p.getPrice())));
        holder.publishtimeTv.setText(TimeConvertor.getTimeAgo(p.getPublishtime()));

        switch (holder.getItemViewType()) {
            case 1:
                holder.descTv.setVisibility(View.GONE);
                Picasso.get().load(p.getVideoThumbnail()).fit().centerCrop().into(holder.promoimage);
                break;
            case 2:
                holder.descTv.setVisibility(View.VISIBLE);
                holder.descTv.setText(p.getDescription());
                holder.promoimage.setImageResource(R.drawable.red_staggered_background);
                switch (p.getType()) {
                    case "أثاث":
                        DrawableCompat.setTint(
                                holder.promoimage.getDrawable(),
                                ContextCompat.getColor(context, R.color.greenFurniture));
                        break;
                    case "موبيلات":
                        DrawableCompat.setTint(
                                holder.promoimage.getDrawable(),
                                ContextCompat.getColor(context, R.color.blueMobile));
                        break;
                    case "سيارات":
                        DrawableCompat.setTint(
                                holder.promoimage.getDrawable(),
                                ContextCompat.getColor(context, R.color.red));
                        break;
                    case "كمبيوتر و لاب توب":
                    case "اليكترونيات":
                        DrawableCompat.setTint(
                                holder.promoimage.getDrawable(),
                                ContextCompat.getColor(context, R.color.pcBlack));
                        break;
                    case "عقارات":
                        DrawableCompat.setTint(
                                holder.promoimage.getDrawable(),
                                ContextCompat.getColor(context, R.color.houseOrange));
                        break;
                }
                break;
            case 3:
                holder.descTv.setVisibility(View.GONE);
                Picasso.get().load(p.getPromoimages().get(0)).fit().centerCrop().into(holder.promoimage);
                break;
        }
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            PromotionInfoFragment fragment = new PromotionInfoFragment();
            bundle.putSerializable("promo", p);
            fragment.setArguments(bundle);
            ((HomeActivity) context).replacePromoFragment(fragment);
        });

    }

    @Override
    public int getItemViewType(int position) {

        switch (mypromotions.get(position).getPromoType()) {
            case "text":
                return 2;
            case "image":
                return 3;
            case "video":
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public int getItemCount() {
        return mypromotions.size();
    }
}
