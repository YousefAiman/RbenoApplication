package com.example.yousef.rbenoapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class VideoVerticalViewPagerAdapter extends FragmentStateAdapter {

     ArrayList<Promotion> videoPromotionsItems;

    public VideoVerticalViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Promotion> videoPromotionsItems) {
        super(fragmentActivity);
        this.videoPromotionsItems = videoPromotionsItems;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
       VideoPageVerticalFragment fragment = new VideoPageVerticalFragment();
        Bundle bundle = new Bundle();
        bundle.putString("videoUrl",videoPromotionsItems.get(position).getVideoUrl());
        bundle.putString("videoTitle",videoPromotionsItems.get(position).getTitle());
        bundle.putString("videoDesc",videoPromotionsItems.get(position).getDescription());
        fragment.setArguments(bundle);
        return fragment;
    }
    @Override
    public int getItemCount() {
        return videoPromotionsItems.size();
    }




}
