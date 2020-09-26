package com.example.yousef.rbenoapplication;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class VideoPageVerticalFragment extends Fragment {

    PlayerView videoPlayerView;
    SimpleExoPlayer newExoPlayer;
    public VideoPageVerticalFragment() {}

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_view_pager_item, container, false);
        videoPlayerView = view.findViewById(R.id.videoPlayerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView)view.findViewById(R.id.videoTitleTv)).setText(getArguments().getString("videoTitle"));
        ((TextView)view.findViewById(R.id.videoDescTv)).setText(getArguments().getString("videoDesc"));
    }

    @Override
    public void onResume() {
        super.onResume();
        newExoPlayer = new SimpleExoPlayer.Builder(getContext()).build();
        newExoPlayer.prepare(new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "RbenoApp")))
                .createMediaSource(Uri.parse(getArguments().getString("videoUrl"))));
        newExoPlayer.setPlayWhenReady(true);
        videoPlayerView.setPlayer(newExoPlayer);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(newExoPlayer!=null) {
            videoPlayerView.setPlayer(null);
            newExoPlayer.release();
            newExoPlayer = null;
        }
    }
}