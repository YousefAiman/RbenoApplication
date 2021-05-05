package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;

import java.util.ArrayList;

public class VideoVerticalViewPagerAdapter extends FragmentStateAdapter {

  private final ArrayList<Promotion> videoPromotionsItems;
  private final Context context;
  private final Fragment fragment;

  public VideoVerticalViewPagerAdapter(@NonNull Fragment fragment,
                                       ArrayList<Promotion> videoPromotionsItems) {
    super(fragment);
    this.fragment = fragment;
    context = fragment.getContext();
    this.videoPromotionsItems = videoPromotionsItems;


  }


  @NonNull
  @Override
  public Fragment createFragment(int position) {
    Log.d("videoPager", "created vertical fragment at: " + position);
    return new VideoPageVerticalFragment(context,
            videoPromotionsItems.get(position));
  }

  @Override
  public int getItemCount() {
    return videoPromotionsItems.size();
  }


  @Override
  public void onViewDetachedFromWindow(@NonNull FragmentViewHolder holder) {
    super.onViewDetachedFromWindow(holder);


    VideoPageVerticalFragment detachedFragment =
            (VideoPageVerticalFragment) fragment.getChildFragmentManager()
                    .findFragmentByTag("f" + holder.getAdapterPosition());

    detachedFragment.releasePlayer();


  }
}
