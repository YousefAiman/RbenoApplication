package com.example.yousef.rbenoapplication;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class VideoPagerFragment extends DialogFragment {
    ViewPager2 videoViewPager;
    VideoVerticalViewPagerAdapter adapter;
    public VideoPagerFragment() {}

    static VideoPagerFragment newInstance() {
        return new VideoPagerFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_pager, container, false);
        videoViewPager = view.findViewById(R.id.videoViewPager);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
            ArrayList<Promotion> videoPromotions = (ArrayList<Promotion>) getArguments().getSerializable("videoPromotions");
            adapter = new VideoVerticalViewPagerAdapter(getActivity(),videoPromotions);
            videoViewPager.setAdapter(adapter);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        VideoPageVerticalFragment myFragment = (VideoPageVerticalFragment) getActivity().getSupportFragmentManager().findFragmentByTag("f" + videoViewPager.getCurrentItem());
        myFragment.videoPlayerView.setPlayer(null);
        if(myFragment.newExoPlayer!=null) {
            myFragment.newExoPlayer.release();
            myFragment.newExoPlayer = null;
        }
    }
}