package com.example.yousef.rbenoapplication;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

public class VideoViewPagerAdapter extends FragmentStatePagerAdapter {
    ArrayList<Promotion> videoPromotionsItems;
    public VideoViewPagerAdapter(@NonNull FragmentManager fm, int behavior, ArrayList<Promotion> videoPromotionsItems) {
        super(fm, behavior);
        this.videoPromotionsItems = videoPromotionsItems;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment f = new VideoPageHorizontalFragment();
        Bundle b = new Bundle();
        b.putSerializable("promo",videoPromotionsItems.get(position));
        f.setArguments(b);

        return f;
    }

    @Override
    public int getCount() {
        return videoPromotionsItems.size();
    }

    
    @Override
    public int getItemPosition(@NonNull Object object) {
        int index = videoPromotionsItems.indexOf (object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    //    @Override
//    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        if (position >= getCount()) {
//            FragmentManager manager = ((Fragment) object).getFragmentManager();
//            FragmentTransaction trans = manager.beginTransaction();
//            trans.remove((Fragment) object);
//            trans.commit();
//        }
//    }
}
