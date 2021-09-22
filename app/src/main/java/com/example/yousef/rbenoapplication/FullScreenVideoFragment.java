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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FullScreenVideoFragment extends Fragment implements View.OnClickListener {

//  private static final int SHARE_CODE = 10,FAV = 1,UN_FAV = 0;

    static final CollectionReference promosRef =
            FirebaseFirestore.getInstance().collection("promotions"),
            usersRef = FirebaseFirestore.getInstance().collection("users");

    final Context context;
    private final FirebaseUser currentUser =
            FirebaseAuth.getInstance().getCurrentUser();
    public PlayerView videoPlayerView;
    public SimpleExoPlayer newExoPlayer;
    public long lastPosition;
    private ImageView videoUserImageIv, videoShareIv, videoFavsIv, videoPromoPlayIv;
    private TextView videoUsernameTv, videoTitleTv, videoPriceTv, videoCategoryTv, videoViewsTv,
            videoFavsTv;

    private final Promotion promotion;
    //  private Bitmap sharedBitmap;
    private File file;
    private DocumentReference currentPromoRef, currentUserRef;


    FullScreenVideoFragment(Context context, Promotion promotion, long lastPosition) {
        this.context = context;
        this.promotion = promotion;
        this.lastPosition = lastPosition;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("fullScreenVideo", "onCreate");
        if (!promotion.getUid().equals(Objects.requireNonNull(currentUser).getUid()))
            promosRef.whereEqualTo("promoid", promotion.getPromoid())
                    .get().addOnSuccessListener(snapshots ->
                    snapshots.getDocuments().get(0).getReference()
                            .update("viewcount", FieldValue.increment(1)));


        if (GlobalVariables.getVideoViewedCount() != 0
                && GlobalVariables.getVideoViewedCount() % 2 == 0) {
            InterstitialAdUtil.showAd(context);
        }

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("fullScreenVideo", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_video_view_pager_item, container,
                false);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        TextView videoShowTv = view.findViewById(R.id.videoShowTv);
        videoShowTv.setVisibility(View.GONE);
        videoPlayerView = view.findViewById(R.id.videoPlayerView);
        videoUserImageIv = view.findViewById(R.id.videoUserImageIv);
        videoUsernameTv = view.findViewById(R.id.videoUsernameTv);
        videoTitleTv = view.findViewById(R.id.videoTitleTv);
        videoPriceTv = view.findViewById(R.id.videoPriceTv);
        videoCategoryTv = view.findViewById(R.id.videoCategoryTv);
        videoViewsTv = view.findViewById(R.id.videoViewsTv);
        videoFavsTv = view.findViewById(R.id.videoFavsTv);
        videoShareIv = view.findViewById(R.id.videoShareIv);
        videoPromoPlayIv = view.findViewById(R.id.videoPromoPlayIv);
        videoFavsIv = view.findViewById(R.id.videoFavsIv);
        final ImageView videoCloseIv = view.findViewById(R.id.videoCloseIv);
        videoCloseIv.setVisibility(View.VISIBLE);
        videoCloseIv.setOnClickListener(v -> getActivity().onBackPressed());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("fullScreenVideo", "onViewCreated");


        videoTitleTv.setText(promotion.getTitle());
        videoPriceTv.setText(String.format(Locale.getDefault(),
                "%,d", (long) promotion.getPrice()) + " " + promotion.getCurrency());
        videoCategoryTv.setText(promotion.getType());

        videoViewsTv.setText(String.valueOf(promotion.getViewcount() + 1));
        videoFavsTv.setText(String.valueOf(promotion.getFavcount()));

        videoCategoryTv.setOnClickListener(this);

        initializeAndPlayVideo();

        if (GlobalVariables.getFavPromosIds().contains(promotion.getPromoid())) {
            videoFavsIv.setImageResource(R.drawable.heart_icon);
        }

        usersRef.whereEqualTo("userId", promotion.getUid())
                .get().addOnSuccessListener(snapshots -> {

            final DocumentSnapshot documentSnapshot = snapshots.getDocuments().get(0);

            Picasso.get().load(documentSnapshot.getString("imageurl"))
                    .fit()
                    .into(videoUserImageIv);

            videoUsernameTv.setText(documentSnapshot.getString("username"));

            videoShareIv.setOnClickListener(this);

            if (currentUser.isAnonymous()) {
                videoFavsIv.setOnClickListener(view1 -> SigninUtil.getInstance(getContext(),
                        getActivity()).show());

                videoUserImageIv.setOnClickListener(view12 -> showProfile());
                videoUsernameTv.setOnClickListener(view1 -> showProfile());

            } else {


                promosRef.whereEqualTo("promoid", promotion.getPromoid()).get()
                        .addOnSuccessListener(snaps -> {
                            currentPromoRef = snaps.getDocuments().get(0).getReference();
                            currentPromoRef.update("viewcount", FieldValue.increment(1));
                        });

                if (((List<String>) documentSnapshot.get("usersBlocked")).contains(currentUser.getUid())) {

                    videoFavsIv.setOnClickListener(v -> Toast.makeText(getContext(),
                            "عذرا, لا يمكنك إضافة هذا الإعلان إلى المفضلة!",
                            Toast.LENGTH_SHORT).show());
                    videoUserImageIv.setOnClickListener(v -> Toast.makeText(getContext(),
                            "عذرا, لا يمكنك زيارة صفحة هذا المستخدم!",
                            Toast.LENGTH_SHORT).show());

                    videoUsernameTv.setOnClickListener(v -> Toast.makeText(getContext(),
                            "عذرا, لا يمكنك زيارة صفحة هذا المستخدم!",
                            Toast.LENGTH_SHORT).show());

                } else {

                    videoFavsIv.setOnClickListener(this);
                    videoUserImageIv.setOnClickListener(this);
                    videoUsernameTv.setOnClickListener(this);

                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();

        pauseVideoIfPlaying(false);

        Log.d("fullScreenVideo", "onPause");
    }


    @Override
    public void onResume() {
        super.onResume();

        getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        playVideoIfPaused(false);

        Log.d("fullScreenVideo", "onResume");
    }


    void showProfile() {

        pauseVideoIfPlaying(false);

        final UserFragment fragment = new UserFragment();

        if (!promotion.getUid().equals(currentUser.getUid())) {
            Bundle bundle = new Bundle();
            bundle.putString("promouserid", promotion.getUid());
            fragment.setArguments(bundle);
        }

        ((HomeActivity) getActivity()).addFragmentToHomeContainer(fragment);

        addObserver(fragment);

    }

    void addObserver(Fragment fragment) {
        fragment.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    playVideoIfPaused(true);
                    fragment.getLifecycle().removeObserver(this);
                } else if (event == Lifecycle.Event.ON_START) {
                    pauseVideoIfPlaying(true);
                }
            }
        });
    }

    void pauseVideoIfPlaying(boolean hidePlayer) {
        if (newExoPlayer != null && newExoPlayer.getPlayWhenReady()) {
            if (hidePlayer) {
                videoPlayerView.setVisibility(View.INVISIBLE);
            }
            lastPosition = newExoPlayer.getCurrentPosition();
            videoPromoPlayIv.setVisibility(View.VISIBLE);
            newExoPlayer.setPlayWhenReady(false);
        }
    }

    public void playVideoIfPaused(boolean showPlayer) {
        if (newExoPlayer != null && !newExoPlayer.getPlayWhenReady()) {
            if (showPlayer) {
                videoPlayerView.setVisibility(View.VISIBLE);
            }
            videoPromoPlayIv.setVisibility(View.INVISIBLE);
            newExoPlayer.setPlayWhenReady(true);
        }
    }

    void initializeAndPlayVideo() {

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(getContext());

        trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd());

        newExoPlayer = new SimpleExoPlayer.Builder(getContext())
                .setTrackSelector(trackSelector).build();

        final MediaSource mediaSource =
                new ProgressiveMediaSource.Factory(new VideoDataSourceFactory(getContext()))
                        .createMediaSource(MediaItem.fromUri(promotion.getVideoUrl()));

        newExoPlayer.setMediaSource(mediaSource);
        newExoPlayer.prepare();
        newExoPlayer.setPlayWhenReady(true);

        if (lastPosition != 0) {
            newExoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY && lastPosition != 0) {
                        newExoPlayer.seekTo(lastPosition);
                    }
                    newExoPlayer.removeListener(this);
                }
            });
        }

        increaseVideoViewCount();

        videoPlayerView.getVideoSurfaceView().setOnClickListener(view -> {

            Log.d("ttt", "player clicked");
            if (newExoPlayer != null) {
                if (newExoPlayer.getPlaybackState() == SimpleExoPlayer.STATE_ENDED) {

                    Log.d("videoPager", "videos state ended");
//          newExoPlayer.seekTo(0);
                    lastPosition = 0;
                    newExoPlayer.seekTo(lastPosition);
                    newExoPlayer.setPlayWhenReady(true);
                    videoPromoPlayIv.setVisibility(View.INVISIBLE);
                    videoPromoPlayIv.setImageResource(R.drawable.play_arrow_white);

                    increaseVideoViewCount();

                    if (GlobalVariables.getVideoViewedCount() != 0 &&
                            GlobalVariables.getVideoViewedCount() % 2 == 0) {
                        InterstitialAdUtil.showAd(getContext());
                    }

                } else if (newExoPlayer.getPlayWhenReady()) {
                    pauseVideoIfPlaying(false);
                } else {
                    videoPromoPlayIv.setVisibility(View.INVISIBLE);
                    newExoPlayer.setPlayWhenReady(true);
                }
            }
        });

        newExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

//        Log.d("videoPager","playWhenReady: "+playWhenReady);

                if (playbackState == SimpleExoPlayer.STATE_ENDED) {
                    videoPromoPlayIv.setImageResource(R.drawable.replay_white);
                    videoPromoPlayIv.setVisibility(View.VISIBLE);
                }
            }
        });

        videoPlayerView.setPlayer(newExoPlayer);

    }

    void increaseVideoViewCount() {
        GlobalVariables.setVideoViewedCount(GlobalVariables.getVideoViewedCount() + 1);
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        if (WifiUtil.checkWifiConnection(getContext())) {
            if (id == R.id.videoFavsIv) {

                videoFavsIv.setClickable(false);

                Promotion.favOrUnFavPromo(context, promotion, currentUser.getUid()
                        , videoFavsIv, videoFavsTv, false);

//          changePromoFav(GlobalVariables.getFavPromosIds().contains(promotion.getPromoid())
//                  ? UN_FAV : FAV);

            } else if (id == R.id.videoUserImageIv || id == R.id.videoUsernameTv) {
                showProfile();
            } else if (id == R.id.videoShareIv) {
                file = Promotion.sharePromo(promotion, getContext(), file, videoShareIv);
//        sharePromo();
            } else if (id == R.id.videoCategoryTv) {
                showCategory();
            }
        }

    }

    void showCategory() {

        final AllPromosFragment promosFragment = new AllPromosFragment();
        final Bundle b = new Bundle();
        b.putString("category", promotion.getType());
        promosFragment.setArguments(b);
        ((HomeActivity) getActivity()).addFragmentToHomeContainer(promosFragment);

        addObserver(promosFragment);

    }


}
