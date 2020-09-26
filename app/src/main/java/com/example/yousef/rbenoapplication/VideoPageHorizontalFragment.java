package com.example.yousef.rbenoapplication;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class VideoPageHorizontalFragment extends Fragment {
    PlayerView videoPlayerView;
    SimpleExoPlayer newExoPlayer;
    ConstraintLayout playLayout;
    ImageView videoThumbnail;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_video_page_horizontal, container, false);
        TextView titleTv = view.findViewById(R.id.videoPromoTitleTv);
        videoPlayerView = view.findViewById(R.id.videoPlayerView);
        playLayout = view.findViewById(R.id.playVideoLayout);
        Promotion p = (Promotion) getArguments().getSerializable("promo");
        videoThumbnail = (ImageView) view.findViewById(R.id.videoThumbnail);
        Picasso.get().load(p.getVideoThumbnail()).fit().into(videoThumbnail);
        titleTv.setText(p.getTitle());

        playLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(((NewestPromosFragment)getParentFragment()).checkVideoIsDeleted(p))return;

                playLayout.setVisibility(View.INVISIBLE);
                videoThumbnail.setVisibility(View.GONE);
                newExoPlayer = new SimpleExoPlayer.Builder(getContext()).build();
                newExoPlayer.prepare(new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "RbenoApp")))
                        .createMediaSource(Uri.parse(p.getVideoUrl())));
                newExoPlayer.setPlayWhenReady(true);
                videoPlayerView.setPlayer(newExoPlayer);
//                ((NewestPromosFragment)getParentFragment()).playerLayoutClick();
                videoPlayerView.setOnClickListener(v2->{
                    if(((NewestPromosFragment)getParentFragment()).checkVideoIsDeleted(p))return;

                    newExoPlayer.setPlayWhenReady(false);
                    assert getParentFragment() != null;
                    ((NewestPromosFragment)getParentFragment()).startVerticalVideoFragment(p);
                });
            }
        });
        titleTv.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            PromotionInfoFragment frag = new PromotionInfoFragment();
            bundle.putSerializable("promo", p);
            frag.setArguments(bundle);
            ((HomeActivity) getActivity()).replacePromoFragment(frag);
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

         if (newExoPlayer!=null) {

             videoPlayerView.setPlayer(null);
             newExoPlayer.release();
             newExoPlayer = null;

                    if (playLayout.getVisibility() == View.INVISIBLE) {
                        playLayout.setVisibility(View.VISIBLE);
                    }
                    if (videoThumbnail.getVisibility() == View.GONE) {
                        videoThumbnail.setVisibility(View.VISIBLE);
                    }
       }
    }

}