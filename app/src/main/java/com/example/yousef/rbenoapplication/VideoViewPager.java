package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class VideoViewPager extends PagerAdapter {

    private Context context;
    private ArrayList<Promotion> videoPromotionsItems;
    private Picasso picasso = Picasso.get();
//    List<View> mList = new ArrayList<>();

    VideoViewPager(Context context, ArrayList<Promotion> videoPromotionsItems) {
        this.context = context;
        this.videoPromotionsItems = videoPromotionsItems;
    }

    @Override
    public int getCount() {
        return videoPromotionsItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

//    public void addView(View view, int index) {
//        mList.add(index, view);
//        notifyDataSetChanged();
//    }
//
//    public void removeView(int index) {
//        notifyDataSetChanged();
//        mList.remove(index);
//        notifyDataSetChanged();
//    }
    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.promo_video_pager_layout,null, true);
        Promotion p = videoPromotionsItems.get(position);
        TextView titleTv = view.findViewById(R.id.videoPromoTitleTv);
        picasso.load(p.getVideoThumbnail()).fit().into((ImageView) view.findViewById(R.id.videoThumbnail));
        titleTv.setText(p.getTitle());
//        view.findViewById(R.id.playVideoLayout).setOnClickListener(v -> {
//            if(videoPromotionsItems.indexOf(p) == -1){
//                removeView(position);
////                notifyDataSetChanged();
//                Toast.makeText(context, "لقد تم حذف هذا الإعلان!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        });
        titleTv.setOnClickListener(view1 -> {
//            view.findViewById(R.id.playVideoLayout).performClick();

            Bundle bundle = new Bundle();
            PromotionInfoFragment frag = new PromotionInfoFragment();
            bundle.putSerializable("promo", p);
            frag.setArguments(bundle);
            ((HomeActivity) context).replacePromoFragment(frag);
        });
//        mList.add(view);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

//    public void removePage(ViewGroup pager, int position) {
//        destroyItem(pager, position, null);
//        videoPromotionsItems.remove(position);
//        notifyDataSetChanged();
//    }
//
//    @Override
//    public int getItemPosition(Object object){
//        if (videoPromotionsItems.contains(object)) {
//            return videoPromotionsItems.indexOf(object);
//        } else {
//            return POSITION_NONE;
//        }
//    }
}
