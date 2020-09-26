package com.example.yousef.rbenoapplication;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class FullScreenVideoFragment extends DialogFragment {

    PlayerView playerView;
    SimpleExoPlayer newExoPlayer;
    private ImageView fullScreenIcon;
    private ImageView exo_previous;
    private ImageView exo_nextvideo;
    private String lastMediaSource;
    private ArrayList<String> videos;
    private ImageButton exo_rew;
    private ImageButton exo_ffwd;

    public FullScreenVideoFragment() {
    }

    static FullScreenVideoFragment newInstance() {
        return new FullScreenVideoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_screen_video, container, false);
        playerView = (PlayerView) view.getRootView();
        fullScreenIcon = playerView.findViewById(R.id.exoplayer_fullscreen_icon);
        exo_previous = playerView.findViewById(R.id.exo_previous);
        exo_nextvideo = playerView.findViewById(R.id.exo_nextvideo);
        exo_rew = playerView.findViewById(R.id.exo_rew);
        exo_ffwd = playerView.findViewById(R.id.exo_ffwd);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        DefaultDataSourceFactory newFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "RbenoApp"));
        newExoPlayer = new SimpleExoPlayer.Builder(getContext()).build();
        lastMediaSource = bundle.getString("videoUrl");
        newExoPlayer.prepare(new ProgressiveMediaSource.Factory(newFactory).createMediaSource(Uri.parse(lastMediaSource)));

        exo_rew.setOnClickListener(v -> {
            if (newExoPlayer.getCurrentPosition() < 3000) {
                newExoPlayer.seekTo(0);
            } else {
                newExoPlayer.seekTo(newExoPlayer.getCurrentPosition() - 3000);
            }
        });

        exo_ffwd.setOnClickListener(v -> {
            if (newExoPlayer.getCurrentPosition() < newExoPlayer.getDuration() - 3000) {
                newExoPlayer.seekTo(newExoPlayer.getCurrentPosition() + 3000);
            } else {
                newExoPlayer.seekTo(newExoPlayer.getDuration());
            }
        });

        if (bundle.containsKey("allVideos")) {
            videos = bundle.getStringArrayList("allVideos");
            int newIndex = videos.indexOf(lastMediaSource);
            Log.d("ttt", videos.indexOf(lastMediaSource) + " is the index");
            if (newIndex == 0) {
                exo_previous.setVisibility(View.INVISIBLE);
            } else if (newIndex == videos.size() - 1) {
                exo_nextvideo.setVisibility(View.INVISIBLE);
            }

            exo_previous.setOnClickListener(v -> {
                int nextIndex = videos.indexOf(lastMediaSource);
                lastMediaSource = videos.get(nextIndex - 1);
                newExoPlayer.prepare(new ProgressiveMediaSource.Factory(newFactory).createMediaSource(Uri.parse(
                        lastMediaSource
                )));
                if (nextIndex - 1 == 0) {
                    exo_previous.setVisibility(View.INVISIBLE);
                }
                exo_nextvideo.setVisibility(View.VISIBLE);

            });
            exo_nextvideo.setOnClickListener(v -> {

                int nextIndex = videos.indexOf(lastMediaSource);
                lastMediaSource = videos.get(nextIndex + 1);
                newExoPlayer.prepare(new ProgressiveMediaSource.Factory(newFactory).createMediaSource(Uri.parse(
                        lastMediaSource
                )));


                if (nextIndex + 1 == videos.size() - 1) {
                    exo_nextvideo.setVisibility(View.INVISIBLE);
                }

                exo_previous.setVisibility(View.VISIBLE);


//                if(nextIndex+1 == videos.size()-1){
//                    exo_nextvideo.setVisibility(View.INVISIBLE);
//                    exo_previous.setVisibility(View.VISIBLE);
//                }else{
//                        exo_previous.setVisibility(View.VISIBLE);
//                    newExoPlayer.prepare(new ProgressiveMediaSource.Factory(newFactory).createMediaSource(Uri.parse(
//                            videos.get(nextIndex+1)
//                    )));
////                    ((NewestPromosFragment)getParentFragment())
////                            .videoViewPager.setCurrentItem(nextIndex+1);
//                    lastMediaSource = videos.get(nextIndex+1);
//                }
            });
        } else {
            exo_previous.setVisibility(View.GONE);
            exo_nextvideo.setVisibility(View.GONE);
        }

        newExoPlayer.setPlayWhenReady(true);
        playerView.setPlayer(newExoPlayer);
        newExoPlayer.seekTo(bundle.getLong("videoPosition"));
        fullScreenIcon.setOnClickListener(v -> {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            dismiss();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        if (newExoPlayer != null) {
//            playerView.setPlayer(null);
//            newExoPlayer.release();
//            newExoPlayer = null;
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (newExoPlayer != null) {
            newExoPlayer.setPlayWhenReady(false);
        }
    }
}
