package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Objects;

public class VideoPagerFragment extends Fragment {

  private ViewPager2 videoViewPager;
  private VideoVerticalViewPagerAdapter adapter;
  private ArrayList<Promotion> promotions;
  private PromotionDeleteReceiver promotionDeleteReceiver;

  public VideoPagerFragment() {
  }

  public VideoPagerFragment(ArrayList<Promotion> promotions) {
    this.promotions = promotions;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    adapter = new VideoVerticalViewPagerAdapter(this, promotions);


  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_video_pager, container, false);

    setupDeletionReceiver();

    ((HomeActivity) Objects.requireNonNull(getActivity()))
            .lockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

    view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

    videoViewPager = view.findViewById(R.id.videoViewPager);
    final ImageView videoCloseIv = view.findViewById(R.id.videoCloseIv);

    videoCloseIv.setOnClickListener(v -> getActivity().onBackPressed());

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    videoViewPager.setAdapter(adapter);

    videoViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
      @Override
      public void onPageSelected(int position) {
        super.onPageSelected(position);

        VideoPageVerticalFragment fragment = (VideoPageVerticalFragment) getChildFragmentManager()
                .findFragmentByTag("f" + position);

        fragment.initializeAndPlayVideo();

      }
    });

  }


  @Override
  public void onDetach() {
    super.onDetach();
    Log.d("videoPager", "video pager fragment onDetach");
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d("videoPager", "video pager fragment onResume");

//    final VideoPageVerticalFragment frag = getCurrentFragment();
//    if(frag!=null){
//      frag.isResumedFromAd = true;
//    }

  }


  @Override
  public void onPause() {
    super.onPause();

    Log.d("videoPager", "video pager fragment onPause");

//    final VideoPageVerticalFragment myFragment =
//            (VideoPageVerticalFragment) getActivity().getSupportFragmentManager()
//                    .findFragmentByTag("f" + videoViewPager.getCurrentItem());
//
//    if(myFragment!=null){
//      Log.d("videoPager","fragment exists: "+myFragment.getTag());
//    }else{
//      Log.d("videoPager","fragment doesn't exist: "+"f" +
//              videoViewPager.getCurrentItem());
//    }
//
////    onDestroy();
//
//    myFragment.videoPlayerView.setPlayer(null);
//    if (myFragment.newExoPlayer != null) {
//      myFragment.newExoPlayer.release();
//      myFragment.newExoPlayer = null;
//    }

  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    Log.d("videoPager", "video pager fragment onHiddenChanged");

//    VideoPageVerticalFragment fragment = getCurrentFragment();
//
//    if(fragment!=null){
//    if (!hidden) {
//
//      Log.d("videoPager", "video pager fragment is hidden true");
//      fragment.playVideoIfPaused(true);
////      if(fragment.newExoPlayer!=null){
////
////
////
////        if (!fragment.newExoPlayer.getPlayWhenReady()) {
////
////
////          fragment.videoPlayerView.setVisibility(View.VISIBLE);
////          fragment.newExoPlayer.setPlayWhenReady(true);
////        }
//////        fragment.lastPosition = fragment.newExoPlayer.getCurrentPosition();
////      }
//
////    }
////    else {
////
////      Log.d("videoPager", "video pager fragment is hidden false");
////
////      fragment.videoPlayerView.setVisibility(View.VISIBLE);
////
////      if (fragment.newExoPlayer != null) {
////        fragment.newExoPlayer.setPlayWhenReady(true);
////      }
//      }
//    }
  }

//  VideoPageVerticalFragment getCurrentFragment(){
//
//    return (VideoPageVerticalFragment) getChildFragmentManager()
//            .findFragmentByTag("f" + videoViewPager.getCurrentItem());
//
//  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d("videoPager", "onDestroy the parent");


//    final VideoPageVerticalFragment myFragment =
//            (VideoPageVerticalFragment) getActivity().getSupportFragmentManager()
//                    .findFragmentByTag("f" + videoViewPager.getCurrentItem());
//
//    if(myFragment!=null){
//      Log.d("videoPager","fragment exists: "+myFragment.getTag());
//    }else{
//      Log.d("videoPager","fragment doesn't exist: "+"f"+
//              videoViewPager.getCurrentItem());
//    }
//
//
//    myFragment.videoPlayerView.setPlayer(null);
//    if (myFragment.newExoPlayer != null) {
//      myFragment.newExoPlayer.release();
//      myFragment.newExoPlayer = null;
//    }
//    if(videoViewPager!=null){
//      videoViewPager.setAdapter(null);
//    }


  }

//  @Override
//  public void onDetach() {
//    super.onDetach();
//    Log.d("videoPager","onDetach the parent");
//
//
//
//  }
//
//  @Override
//  public void onDismiss(@NonNull DialogInterface dialog) {
//    super.onDismiss(dialog);
//
//    Log.d("videoPager","onDismiss the parent");
//    getActivity().getWindow().clearFlags(View.SYSTEM_UI_FLAG_VISIBLE);
//
//    final VideoPageVerticalFragment myFragment =
//            (VideoPageVerticalFragment) getActivity().getSupportFragmentManager()
//                    .findFragmentByTag("f" + videoViewPager.getCurrentItem());
//
//    myFragment.videoPlayerView.setPlayer(null);
//    if (myFragment.newExoPlayer != null) {
//      myFragment.newExoPlayer.release();
//      myFragment.newExoPlayer = null;
//    }
//    videoViewPager.setAdapter(null);
//  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    Log.d("videoPager", "onDestroyView parent fragment");

    ((HomeActivity) Objects.requireNonNull(getActivity()))
            .lockDrawer(DrawerLayout.LOCK_MODE_UNLOCKED);

//    final VideoPageVerticalFragment myFragment =
//            (VideoPageVerticalFragment) getActivity().getSupportFragmentManager()
//                    .findFragmentByTag("f" + videoViewPager.getCurrentItem());
//
//    if(myFragment!=null){
//      Log.d("videoPager","fragment exists: "+myFragment.getTag());
//    }else{
//      Log.d("videoPager","fragment doesn't exist: "+"f"+
//              videoViewPager.getCurrentItem());
//    }
//
//    myFragment.videoPlayerView.setPlayer(null);
//    if (myFragment.newExoPlayer != null) {
//      myFragment.newExoPlayer.release();
//      myFragment.newExoPlayer = null;
//    }

    if (promotionDeleteReceiver != null)
      getContext().unregisterReceiver(promotionDeleteReceiver);

  }

  void setupDeletionReceiver() {

    promotionDeleteReceiver =
            new PromotionDeleteReceiver() {
              @Override

              public void onReceive(Context context, Intent intent) {
                checkAndDeletePromoFromList(promotions,
                        intent.getLongExtra("promoId", 0));
              }
            };


    getContext().registerReceiver(promotionDeleteReceiver,
            new IntentFilter(BuildConfig.APPLICATION_ID + ".promoDelete"));


  }

  void checkAndDeletePromoFromList(ArrayList<Promotion> promos, long id) {

    if (promos != null && !promos.isEmpty()) {
      for (Promotion promo : promos) {
        if (promo.getPromoid() == id) {
          final int index = promos.indexOf(promo);
          promos.remove(promo);
          adapter.notifyItemRemoved(index);
          break;
        }
      }
    }

  }


}