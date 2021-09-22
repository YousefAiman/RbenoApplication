package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class VideoPageVerticalFragment extends Fragment implements View.OnClickListener {

    private static final int SHARE_CODE = 10, FAV = 1, UN_FAV = 0;

    static final CollectionReference promosRef =
            FirebaseFirestore.getInstance().collection("promotions"),
            usersRef = FirebaseFirestore.getInstance().collection("users");

    final Context context;
    private final FirebaseUser currentUser =
            FirebaseAuth.getInstance().getCurrentUser();
    public PlayerView videoPlayerView;
    public SimpleExoPlayer newExoPlayer;

    private ImageView videoUserImageIv, videoShareIv, videoFavsIv, videoPromoPlayIv;
    private TextView videoUsernameTv;
    private TextView videoTitleTv;
    private TextView videoPriceTv;
    private TextView videoCategoryTv;
    private TextView videoViewsTv;
    private TextView videoFavsTv;
    private final Promotion promotion;
    //  private Bitmap sharedBitmap;
    private File file;
//  private boolean isDialogFragment;
//  private boolean isSharingPromo,isResumedFromAd,wasStopped;

    private DocumentReference currentPromoRef, currentUserRef;

    public VideoPageVerticalFragment(Context context, Promotion promotion) {
        this.context = context;
        this.promotion = promotion;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_video_view_pager_item,
                container, false);

        TextView videoShowTv = view.findViewById(R.id.videoShowTv);

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


        final SpannableString s = new SpannableString(videoShowTv.getText());
        s.setSpan(new UnderlineSpan(), 0, s.length(), 0);
        videoShowTv.setText(s);

        videoShowTv.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoTitleTv.setText(promotion.getTitle());
        videoPriceTv.setText(String.format(Locale.getDefault(),
                "%,d", (long) promotion.getPrice()) + " " + promotion.getCurrency());
        videoCategoryTv.setText(promotion.getType());

        videoViewsTv.setText(String.valueOf(promotion.getViewcount() + 1));
        videoFavsTv.setText(String.valueOf(promotion.getFavcount()));

        videoCategoryTv.setOnClickListener(this);

        usersRef.whereEqualTo("userId", promotion.getUid())
                .get().addOnSuccessListener(snapshots -> {

            final DocumentSnapshot documentSnapshot = snapshots.getDocuments().get(0);

            Picasso.get().load(documentSnapshot.getString("imageurl"))
                    .fit()
                    .into(videoUserImageIv);

            videoUsernameTv.setText(documentSnapshot.getString("username"));

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
                            "عذرا, لا يمكنك زيارة صفحة هذا المستخدم!", Toast.LENGTH_SHORT).show());

                    videoUsernameTv.setOnClickListener(v -> Toast.makeText(getContext(),
                            "عذرا, لا يمكنك زيارة صفحة هذا المستخدم!", Toast.LENGTH_SHORT).show());

                } else {

                    videoFavsIv.setOnClickListener(VideoPageVerticalFragment.this);
                    videoUserImageIv.setOnClickListener(VideoPageVerticalFragment.this);
                    videoUsernameTv.setOnClickListener(VideoPageVerticalFragment.this);
                    videoShareIv.setOnClickListener(VideoPageVerticalFragment.this);

                }


            }
        });


    }


    void pauseVideoIfPlaying(boolean hidePlayer) {


        if (newExoPlayer != null) {

            if (hidePlayer) {
                videoPlayerView.setVisibility(View.INVISIBLE);
            }

            if (newExoPlayer.getPlayWhenReady()) {
                videoPromoPlayIv.setVisibility(View.VISIBLE);
                newExoPlayer.setPlayWhenReady(false);
            }

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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        Log.d("videoPager", "hidden changed for vertical fragment: " + hidden);

//    if(hidden){
//
//      pauseVideoIfPlaying(true);
//
////      videoPlayerView.setVisibility(View.INVISIBLE);
////      if(newExoPlayer!=null){
////        if (newExoPlayer.getPlayWhenReady()) {
////          newExoPlayer.setPlayWhenReady(false);
////        }
////        lastPosition = newExoPlayer.getCurrentPosition();
////      }
//    }else{
//
//      playVideoIfPaused(true);
////
////      videoPlayerView.setVisibility(View.VISIBLE);
////
////      if(newExoPlayer!=null){
////        newExoPlayer.setPlayWhenReady(true);
////      }
//    }


    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("videoPager", "onStart vertical fragment");

//    newExoPlayer = new SimpleExoPlayer.Builder(getContext())
//            .build();
//    newExoPlayer.prepare(new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(getContext(),
//            Util.getUserAgent(getContext(), "RbenoApp")))
//            .createMediaSource(Uri.parse(promotion.getVideoUrl())));
//    newExoPlayer.setPlayWhenReady(true);


    }

    public void initializeAndPlayVideo() {
        final DefaultTrackSelector trackSelector = new DefaultTrackSelector(getContext());

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

        increaseVideoViewCount();

        videoPlayerView.getVideoSurfaceView().setOnClickListener(view -> {

            if (newExoPlayer != null) {
                if (newExoPlayer.getPlaybackState() == SimpleExoPlayer.STATE_ENDED) {

                    newExoPlayer.seekTo(0);
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
                    playVideoIfPaused(false);
                }

            }

//      Log.d("ttt", "player clicked");
//      if(newExoPlayer!=null) {
//        if (newExoPlayer.getPlaybackState() == SimpleExoPlayer.STATE_ENDED) {
//
//          Log.d("videoPager","videos state ended");
////          newExoPlayer.seekTo(0);
//          newExoPlayer.seekTo(0);
//          newExoPlayer.setPlayWhenReady(true);
//
////          newExoPlayer.addListener(new Player.EventListener() {
////            @Override
////            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
////              if (playbackState == Player.STATE_READY) {
////                newExoPlayer.seekTo(0);
////                newExoPlayer.removeListener(this);
////              }
////            }
////          });
//
//          videoPromoPlayIv.setVisibility(View.INVISIBLE);
//          videoPromoPlayIv.setImageResource(R.drawable.play_arrow_white);
//          increaseVideoViewCount();
//
//          if (GlobalVariables.getVideoViewedCount() != 0 &&
//                  GlobalVariables.getVideoViewedCount() % 5 == 0) {
//
////            ((HomeActivity) getActivity()).showRewardVideo();
//            InterstitialAdUtil.showAd(getContext());
//          }
//
//        }
//        else if (newExoPlayer.getPlayWhenReady()) {
//          pauseVideoIfPlaying(false);
//        } else {
//          videoPromoPlayIv.setVisibility(View.INVISIBLE);
//          newExoPlayer.setPlayWhenReady(true);
//        }
//      }
        });

        newExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == SimpleExoPlayer.STATE_ENDED) {
                    videoPromoPlayIv.setImageResource(R.drawable.replay_white);
                    videoPromoPlayIv.setVisibility(View.VISIBLE);
                }
            }

        });

        videoPlayerView.setPlayer(newExoPlayer);

    }


    @Override
    public void onResume() {
        super.onResume();

        playVideoIfPaused(false);

//    if(!wasStopped) {
//      if (getParentFragment() != null) {
//        Log.d("videoPager", "parent is not null");
//        if (!getParentFragment().isHidden()) {
//          Log.d("videoPager", "parent is hidden: " + false);
//          if (newExoPlayer == null) {
//            videoPromoPlayIv.setVisibility(View.INVISIBLE);
//            Log.d("videoPager", "newExoPlayer is null: " + true);
//            initializeAndPlayVideo();
//
//          } else {
//            Log.d("videoPager", "newExoPlayer is null: " + false);
//            newExoPlayer.setPlayWhenReady(true);
//            videoPromoPlayIv.setVisibility(View.INVISIBLE);
//          }
//
//        } else {
//          Log.d("videoPager", "parent is hidden: " + true);
//          if (newExoPlayer == null) {
//            Log.d("videoPager", "newExoPlayer is null: " + true);
//            videoPromoPlayIv.setVisibility(View.INVISIBLE);
//            initializeAndPlayVideo();
//          }
//        }
//
//      } else {
//        Log.d("videoPager", "parent is null");
//        if (newExoPlayer == null) {
//          Log.d("videoPager", "newExoPlayer is null: " + true);
////        videoPromoPlayIv.setVisibility(View.INVISIBLE);
//          initializeAndPlayVideo();
//        } else {
//          playVideoIfPaused(true);
//        }
//      }
//    }else{
//      wasStopped = false;
//    }

        Log.d("videoPager", "resuming vertical fragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("videoPager", "onPause vertical fragment");


        pauseVideoIfPlaying(false);

//    if(getParentFragment() != null){
//      if (InterstitialAdUtil.isIsAdShowing() || isSharingPromo) {
//
//        if (isSharingPromo) {
//          Log.d("videoPager", "isSharingPromo so just pausing");
//          isSharingPromo = false;
//        } else {
//          Log.d("videoPager", "ad is showing so just pausing");
//        }
//
//        pauseVideoIfPlaying(false);
//
//
//      } else {
//        if (newExoPlayer != null) {
//          Log.d("videoPager", "ad is not showing so releasing");
//
//          videoPlayerView.setPlayer(null);
//          newExoPlayer.release();
//          newExoPlayer = null;
//        }
//      }
//    }else{
//
//      pauseVideoIfPlaying(false);
//
//    }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("videoPager", "onDestroy vertical fragment");
        if (newExoPlayer != null) {
            videoPlayerView.setPlayer(null);
            newExoPlayer.release();
            newExoPlayer = null;
        }
    }

    public void releasePlayer() {

        if (newExoPlayer != null) {

            videoPromoPlayIv.setVisibility(View.INVISIBLE);
            videoPromoPlayIv.setImageResource(R.drawable.play_arrow_white);
            videoPlayerView.setPlayer(null);
            newExoPlayer.release();
            newExoPlayer = null;

        }
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
//    fragment.show(getChildFragmentManager(), "UserFragment");
    }

//  void sharePromo() {
//
//    Log.d("ttt", "started sharing");
//    final Intent shareIntent = new Intent();
//    shareIntent.setAction(Intent.ACTION_SEND);
//    shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
//    shareIntent.putExtra(Intent.EXTRA_TEXT, promotion.getTitle()
//            + " - " + promotion.getPrice() + " " +
//            promotion.getCurrency() + "\n" + promotion.getDescription());
//    shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//    shareIntent.setType("image/*");
//
//    if (sharedBitmap == null) {
//      final ProgressDialog progressDialog = new ProgressDialog(getContext());
//      progressDialog.setCancelable(false);
//      progressDialog.show();
//
//      Glide.with(getContext()).asBitmap()
//              .load(promotion.getVideoThumbnail())
//              .into(new CustomTarget<Bitmap>() {
//                @Override
//                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                  progressDialog.dismiss();
//                  sharedBitmap = resource;
//                  createShareFileAndStartIntent(shareIntent);
//                }
//
//                @Override
//                public void onLoadFailed(@Nullable Drawable errorDrawable) {
//                  progressDialog.dismiss();
//                  super.onLoadFailed(errorDrawable);
//                }
//
//                @Override
//                public void onLoadCleared(@Nullable Drawable placeholder) {
//
//                }
//              });
//    } else {
//      if (file == null) {
//        Log.d("ttt", "file is null so crating a new one");
//        createShareFileAndStartIntent(shareIntent);
//      } else {
//        Log.d("ttt", "file not null so using it");
//        try {
//          if (Build.VERSION.SDK_INT >= 24) {
//            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context,
//                    BuildConfig.APPLICATION_ID + ".provider", file));
//          } else {
//            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//          }
//        } finally {
//          startActivityForResult(Intent.createChooser(shareIntent, "choose one"),SHARE_CODE);
//        }
//      }
//    }
//  }
//
//  void createShareFileAndStartIntent(Intent shareIntent) {
//    try {
//      Log.d("ttt", "creating file for sharing");
////    final File file2 = File.createTempFile(System.currentTimeMillis()+"",".jpg");
//      file = new File(getContext().getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
//      final FileOutputStream out = new FileOutputStream(file);
//      sharedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//      out.close();
//
//      if (Build.VERSION.SDK_INT >= 24) {
//        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context,
//                BuildConfig.APPLICATION_ID + ".provider", file));
//      } else {
//        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//      }
//
//
//    } catch (Exception IOException) {
//      IOException.printStackTrace();
//    } finally {
//      Log.d("ttt", "starting sharing activity");
//      startActivity(Intent.createChooser(shareIntent, "choose one"));
//    }
//  }

    void showCategory() {

        final AllPromosFragment promosFragment = new AllPromosFragment();
        final Bundle b = new Bundle();
        b.putString("category", promotion.getType());
        promosFragment.setArguments(b);
        ((HomeActivity) getActivity()).addFragmentToHomeContainer(promosFragment);

        addObserver(promosFragment);
    }

    void addObserver(Fragment fragment) {
        fragment.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
//          if()
                    videoPlayerView.setVisibility(View.VISIBLE);
//          playVideoIfPaused(true);
                    fragment.getLifecycle().removeObserver(this);
                } else if (event == Lifecycle.Event.ON_START) {
                    pauseVideoIfPlaying(true);
                }
            }
        });
    }

    void increaseVideoViewCount() {
        GlobalVariables.setVideoViewedCount(GlobalVariables.getVideoViewedCount() + 1);
    }

    void changePromoFav(int type) {


        currentPromoRef.update("favcount", FieldValue.increment(type == UN_FAV ? -1 : 1));

        if (currentUserRef != null) {
            updateFavForUser(type, currentPromoRef);
        } else {
            usersRef.whereEqualTo("userId", currentUser.getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot snapshots) {
                    currentUserRef = snapshots.getDocuments().get(0).getReference();
                    updateFavForUser(type, currentUserRef);
                }
            });
        }


    }

    void updateFavForUser(int type, DocumentReference documentReference) {

        documentReference.update("favpromosids",
                type == UN_FAV ?
                        FieldValue.arrayRemove(promotion.getPromoid()) :
                        FieldValue.arrayUnion(promotion.getPromoid())
        )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                if(type == UN_FAV){
//                  GlobalVariables.getFavPromosIds().remove(promotion.getPromoid());
//                }else{
//                  GlobalVariables.getFavPromosIds().add(promotion.getPromoid());
//                }

                        if (getContext() != null) {

                            if (type == UN_FAV) {
                                videoFavsIv.setImageResource(R.drawable.heart_grey_outlined);

                                videoFavsTv.setText(String.valueOf(
                                        Integer.parseInt(videoFavsTv.getText().toString()) - 1));

                                Toast.makeText(getContext(), "تمت الازالة من المفضلة!",
                                        Toast.LENGTH_SHORT).show();

                            } else {

                                videoFavsIv.setImageResource(R.drawable.heart_icon);

                                videoFavsTv.setText(String.valueOf(
                                        Integer.parseInt(videoFavsTv.getText().toString()) + 1));

                                Toast.makeText(getContext(), "تمت الإضافة من المفضلة!",
                                        Toast.LENGTH_SHORT).show();

                            }

                            videoFavsIv.setClickable(true);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                videoFavsIv.setClickable(true);

                Toast.makeText(getContext(),
                        type == UN_FAV ?
                                "لقد فشلت الازالة من" :
                                "لقد فشلت الإضافة إلى" +
                                        "المفضلة ! حاول مرة اخرى",
                        Toast.LENGTH_SHORT).show();

            }
        });
    }

    void showPromo() {

        final Fragment fragment = new PromotionInfoFragment(promotion);

        ((HomeActivity) getActivity()).addFragmentToHomeContainer(fragment);

        addObserver(fragment);

    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        if (WifiUtil.checkWifiConnection(getContext())) {
            if (id == R.id.videoFavsIv) {
                videoFavsIv.setClickable(false);
                changePromoFav(GlobalVariables.getFavPromosIds().contains(promotion.getPromoid())
                        ? UN_FAV : FAV);

            } else if (id == R.id.videoUserImageIv || id == R.id.videoUsernameTv) {
                showProfile();
            } else if (id == R.id.videoShareIv) {
                file = Promotion.sharePromo(promotion, getContext(), file, videoShareIv);
            } else if (id == R.id.videoCategoryTv) {
                showCategory();
            } else if (id == R.id.videoShowTv) {
                showPromo();
            }
        }
    }


}