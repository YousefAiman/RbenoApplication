package com.example.yousef.rbenoapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import java.util.Objects;

public class VideoFullScreenFragment extends DialogFragment {


    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = () -> {

        int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        Activity activity = getActivity();
        if (activity != null
                && activity.getWindow() != null) {
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        }

    };


    private String videoUrl;
    private Uri videoUri;
    //video
    private PlayerView playerView;
    private SimpleExoPlayer exoPlayer;
    private boolean mVisible;
    private final Runnable mHideRunnable = this::hide;

    private ImageView videoCloseIv;

    public VideoFullScreenFragment(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public VideoFullScreenFragment(Uri videoUri) {
        this.videoUri = videoUri;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_full_screen, container, false);

        playerView = view.findViewById(R.id.playerView);
        videoCloseIv = view.findViewById(R.id.videoCloseIv);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVisible = true;

        videoCloseIv.setOnClickListener(v -> dismiss());
        playerView.setOnClickListener(v -> toggle());

        Objects.requireNonNull(playerView.getVideoSurfaceView()).setOnClickListener(v -> {

            if (playerView.isControllerVisible()) {
                playerView.hideController();
            } else {
                playerView.showController();
            }

            toggle();
            Log.d("ttt", "mVisible: " + mVisible);

        });

        if (videoUrl != null) {
            playVideoFromUrl();


        } else if (videoUri != null) {

            Log.d("ttt", "videoUri !=null");
            playVideoFromUri();
        }

    }


    private void playVideoFromUrl() {

        final DefaultTrackSelector trackSelector = new DefaultTrackSelector(getContext());

        trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd());

        exoPlayer = new SimpleExoPlayer.Builder(getContext())
                .setTrackSelector(trackSelector).build();

        final MediaSource mediaSource =
                new ProgressiveMediaSource.Factory(new VideoDataSourceFactory(getContext()))
                        .createMediaSource(MediaItem.fromUri(videoUrl));

        exoPlayer.setPlayWhenReady(true);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();


        playerView.setPlayer(exoPlayer);

    }

    private void playVideoFromUri() {

        exoPlayer = new SimpleExoPlayer.Builder(getContext(), new DefaultRenderersFactory(getContext())).build();

        DataSpec dataSpec = new DataSpec(videoUri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            Log.d("ttt", "failed to open file data source: " + e.getMessage());
            e.printStackTrace();
        }

        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(),
                        "simpleExoPlayer"));


        MediaSource firstSource = null;
        try {


//      File file = new File(new URI(videoUri.getPath()));

            firstSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(new FileUtils(requireContext()).getPath(videoUri)));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (firstSource != null) {

                exoPlayer.setMediaSource(firstSource, true);
                exoPlayer.prepare();
                exoPlayer.setPlayWhenReady(true);

            }
        }


    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {

            mVisible = true;
            if (getActivity() != null && getActivity().getWindow() != null) {

                getActivity().getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );

                delayedHide(3000);
            }


        }
    }

    private void hide() {
        mVisible = false;
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);

    }


    private void delayedHide(int delayMillis) {

        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);

            getActivity().setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        show();

        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            getActivity().setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        }

        delayedHide(100);

        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (exoPlayer != null) {
            playerView.setPlayer(null);
            playerView = null;
            exoPlayer.release();
            exoPlayer = null;

            if (!VideoCache.isNull()) {
                Log.d("exoPlayerPlayback", "video cache is not null");
                VideoDataSourceFactory.clearVideoCache(getContext());
            } else {
                Log.d("exoPlayerPlayback", "video cache is null");
            }

        }
    }

}