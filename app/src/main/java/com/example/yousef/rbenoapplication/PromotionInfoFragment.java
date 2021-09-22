package com.example.yousef.rbenoapplication;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import hyogeun.github.com.colorratingbarlib.ColorRatingBar;

public class PromotionInfoFragment extends Fragment implements View.OnClickListener {

    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final Promotion p;
    private ViewPager promotionsPager;
    private LinearLayout promotionDotsSlider;
    private String viewedDocumentID;
    private ColorRatingBar ratingBar;
    private ImageView favImage, promoUserIv, promoUserIv2, shareImageIv, onlineStatusIv;

    private TextView promotionViews, promotionTitle, promotionPrice, promotionPublish,
            promotionId, promotionDescTv, promotionFavsTv, userNameTv, promotionCategoryTv,
            relatedTv, promotionCountryTv, promotionCurrencyTv;

    private DocumentSnapshot currentDs;
    private PlayerView promoVideoPlayer;
    private SimpleExoPlayer exoPlayer;
    private RecyclerView relatedRv;
    private ConstraintLayout messagingLayout;
    private long lastPosition;
    private File file;
    private Toolbar toolbar;
    private final CollectionReference
            promotionRef = FirebaseFirestore.getInstance().collection("promotions"),
            usersRef = FirebaseFirestore.getInstance().collection("users");

    private boolean currentUserIsBlocked;

    private List<String> currentlyBlocked;

    private PromotionDeleteReceiver promotionDeleteReceiver;

    private ArrayList<Promotion> relatedPromos;

    private String userPhoneNumber;

    public PromotionInfoFragment(Promotion p) {
        this.p = p;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_promotion_info_new, container, false);

        //Initialize delete broadcast
        setupDeletionReceiver();

        //Initializing views
        favImage = view.findViewById(R.id.favImage);
        ratingBar = view.findViewById(R.id.ratingBar);

        promotionDescTv = view.findViewById(R.id.promotionDescTv);
        promotionViews = view.findViewById(R.id.promotionViewsTv);
        promotionTitle = view.findViewById(R.id.promotion_title);
        promotionPrice = view.findViewById(R.id.promotion_price);
        promotionPublish = view.findViewById(R.id.promotionPublishtimeTv);
        promotionCategoryTv = view.findViewById(R.id.promotionCategoryTv);
        promotionId = view.findViewById(R.id.promotionIdTv);
        userNameTv = view.findViewById(R.id.userNameTv);
        promoUserIv = view.findViewById(R.id.promoUserIv);
        promoUserIv2 = view.findViewById(R.id.promoUserIv2);
        promotionFavsTv = view.findViewById(R.id.promotionFavsTv);
        relatedRv = view.findViewById(R.id.relatedRv);
        messagingLayout = view.findViewById(R.id.messagingLayout);
        relatedTv = view.findViewById(R.id.relatedTv);
        promotionCountryTv = view.findViewById(R.id.promotionCountryTv);
        shareImageIv = view.findViewById(R.id.shareImageIv);
        toolbar = view.findViewById(R.id.toolbar);
        onlineStatusIv = view.findViewById(R.id.onlineStatusIv);
        promotionCurrencyTv = view.findViewById(R.id.promotionCurrencyTv);
        final AdView adView = view.findViewById(R.id.adView);

        //Back navigation
        toolbar.setNavigationOnClickListener(v -> Objects.requireNonNull(getActivity()).onBackPressed());

        ((TextView) view.findViewById(R.id.countryToolbarTv)).setText(
                CountryUtil.getCountryName(GlobalVariables.getInstance().getCountryCode()));


        //Loading AdView
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });

        //Inflating menu
        if (p.getUid().equals(Objects.requireNonNull(currentUser).getUid())) {
            inflateOwnerUserMenu();
        } else {
            inflatePromoMenu();
        }

        //Creating video or image pager depending on promo type
        checkPromoTypeAndCreateVideoOrImagePager(view);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //filing promo data from promo object
        fillPromoData();

        if (Objects.requireNonNull(currentUser).isAnonymous()) {

            //current user is anon
            messagingLayout.setOnClickListener(v -> showSigninDialog());
            favImage.setOnClickListener(v -> showSigninDialog());

            promoUserIv2.setOnClickListener(v -> showProfile());
            userNameTv.setOnClickListener(v -> showProfile());

            shareImageIv.setOnClickListener(this);

            getUserInfo();

        } else {

            //Already favoured
            if (GlobalVariables.getFavPromosIds().contains(p.getPromoid())) {
                favImage.setImageResource(R.drawable.heart_icon);
            }


            if (currentUser.getUid().equals(p.getUid())) {

                //Current user is same as publisher
                messagingLayout.setVisibility(View.GONE);

                initFavAndProfileClickers();

                getUserInfo();

            } else {

                //current user is not same as publisher
                usersRef.whereEqualTo("userId", p.getUid()).addSnapshotListener((value, error) -> {

                    if (value == null)
                        return;

                    for (DocumentChange dc : value.getDocumentChanges()) {

                        currentDs = dc.getDocument();
                        if (dc.getType() == DocumentChange.Type.ADDED) {

                            final String imageUrl = currentDs.getString("imageUrl");
                            userNameTv.setText(currentDs.getString("username"));
                            userPhoneNumber = currentDs.getString("phonenum");

                            if (!currentDs.getBoolean("status")) {
                                onlineStatusIv.setImageResource(R.drawable.red_circle);
                            }

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get().load(imageUrl).fit().into(promoUserIv);
                                Picasso.get().load(imageUrl).fit().into(promoUserIv2);
                            }

                            currentlyBlocked = (List<String>) currentDs.get("usersBlocked");

                            if (currentlyBlocked != null) {
                                currentUserIsBlocked = currentlyBlocked.contains(currentUser.getUid());
                            }
//            promotionCountryTv
                            initFavAndProfileClickers();

                        } else {

                            currentlyBlocked = (List<String>) currentDs.get("usersBlocked");

                            if (currentlyBlocked != null) {
                                currentUserIsBlocked = currentlyBlocked.contains(currentUser.getUid());
                            }

                        }

                    }
                });
            }

            messagingLayout.setOnClickListener(this);
        }

        getRelatedPromotions();


    }

    void startMessagingActivity() {
        Intent messagingIntent = new Intent(getContext(), MessagingRealTimeActivity.class);
        messagingIntent.putExtra("promouserid", p.getUid());
        messagingIntent.putExtra("promoid", p.getPromoid());
//        messagingIntent.putExtra("promodocumentid", viewedDocumentID);
        messagingIntent.putExtra("intendedpromoid", p.getPromoid());
        startActivity(messagingIntent);
    }

    void showRatingDialog() {

        if (getContext() == null)
            return;

        final Dialog dialog = new Dialog(getContext());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.rating_dialog_layout);
        dialog.setCanceledOnTouchOutside(true);
        final ColorRatingBar newRatingBar = dialog.findViewById(R.id.ratingDialogRb);

        dialog.findViewById(R.id.rateDialogTv).setOnClickListener(v -> {
            if (WifiUtil.checkWifiConnection(getContext())) {

                if (newRatingBar.getRating() > 0) {

                    final HashMap<String, Object> ratingMap = new HashMap<>();
                    ratingMap.put("userid", Objects.requireNonNull(currentUser).getUid());
                    ratingMap.put("rating", newRatingBar.getRating());

                    final CollectionReference ratingsRef =
                            promotionRef.document(viewedDocumentID).collection("ratings");

                    ratingsRef.add(ratingMap)
                            .addOnSuccessListener(documentReference -> {

                                ratingBar.setOnClickListener(view14 -> Toast.makeText(getContext()
                                        , "لقد قمت بتقييم هذا الإعلان من قبل!",
                                        Toast.LENGTH_SHORT).show());

                                ratingsRef.get().addOnSuccessListener(snapshots -> {
                                    if (!snapshots.isEmpty()) {
                                        int size = 0;
                                        double ratingSum = 0;
                                        for (QueryDocumentSnapshot snapshot : snapshots) {
                                            double currentRating = snapshot.getDouble("rating");
                                            if (currentRating != 0) {
                                                ratingSum += currentRating;
                                                size++;
                                            }
                                        }
                                        promotionRef.document(viewedDocumentID).update("rating",
                                                ratingSum / size);

                                        p.setRating(ratingSum / size);
                                        ratingBar.setRating((float) (ratingSum / size));
                                    }
                                }).addOnCompleteListener(task -> ratingBar.setClickable(true));

                                Toast.makeText(getContext(), "تم التقييم!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();

                                if (!currentUser.getUid().equals(p.getUid())) {

                                    FirestoreNotificationSender.sendFirestoreNotification(p.getPromoid(),
                                            p.getUid(),
                                            "rating");

                                    usersRef.whereEqualTo("userId", currentUser.getUid())
                                            .get().addOnSuccessListener(snapshots -> {

                                        final DocumentSnapshot ds = snapshots.getDocuments()
                                                .get(0);

                                        final String currentUserName = ds.getString("username");

                                        final String imageUrl = ds.getString("imageurl");

                                        CloudMessagingNotificationsSender.sendNotification(
                                                p.getUid(),
                                                new Data(
                                                        currentUser.getUid(),
                                                        p.getTitle(),
                                                        currentUserName + " قام بتقييم اعلانك " +
                                                                newRatingBar.getRating() + " نجوم ",
                                                        imageUrl,
                                                        currentUserName,
                                                        "rating",
                                                        p.getPromoid()
                                                ));


                                    });
                                }
//                    if (!GlobalVariables.getPreviousSentNotifications()
//                            .contains(currentUser.getUid() + "rating" + p.getPromoid())) {
//                      usersRef.whereEqualTo("userId", currentUser.getUid())
//                              .get().addOnSuccessListener(snapshots -> {
//
////                              ratingBar.setOnTouchListener((view1, motionEvent) -> {
////                                ratingBar.performClick();
////                                return false;
////                              });
//
////                                ratingBar.setOnTouchListener(null);
//                      }).addOnCompleteListener(task -> {
//                        Toast.makeText(getContext(), "تم التقييم!", Toast.LENGTH_SHORT).show();
//                        dialog.dismiss();
//                      });
//                    }
                            }).addOnFailureListener(e -> {
                        ratingBar.setClickable(true);
                        Toast.makeText(getContext(), "لقد فشل التقييم! حاول مرة اخرى",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                } else {


                    Toast.makeText(getContext(),
                            "لا يمكنك تقييم هذا الإعلان بصفر نجمة", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.findViewById(R.id.cancelDialogTv).setOnClickListener(v -> {
            dialog.dismiss();
            ratingBar.setClickable(true);
        });
        dialog.show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("promoFragments", "promo fragment onDestroy");
        if (exoPlayer != null) {
            promoVideoPlayer.setPlayer(null);
            exoPlayer.release();
            exoPlayer = null;
        }
        deleteShareFile();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("promoFragments", "promo fragment onDetach");

        if (exoPlayer != null) {
            promoVideoPlayer.setPlayer(null);
            exoPlayer.release();
            exoPlayer = null;
        }
        deleteShareFile();

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("promoFragments", "promo fragment onHiddenChanged");
        if (hidden) {
            Log.d("promoFragments", "promo fragment is hidden true");
            if (exoPlayer != null) {
                promoVideoPlayer.setVisibility(View.INVISIBLE);
                exoPlayer.setPlayWhenReady(false);
                lastPosition = exoPlayer.getCurrentPosition();
            }
        } else {
            if (exoPlayer != null) {
                promoVideoPlayer.setVisibility(View.VISIBLE);
                exoPlayer.seekTo(lastPosition);
                exoPlayer.setPlayWhenReady(true);
            }
//      if(promoVideoPlayer!=null){
//        promoVideoPlayer.setVisibility(View.VISIBLE);
//      }
            Log.d("promoFragments", "promo fragment is hidden false");
        }
    }

    void deleteShareFile() {
        if (file != null && file.exists()) {
            Log.d("ttt", "deleted file: " + file.getName());
            file.delete();
            file = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("promoFragments", "promo fragment onResume");
//    if (exoPlayer != null) {
//      exoPlayer.setPlayWhenReady(true);
//    }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("promoFragments", "promo fragment onPause");
        if (exoPlayer != null && exoPlayer.getPlayWhenReady()) {
            exoPlayer.setPlayWhenReady(false);
//      lastPosition = exoPlayer.getCurrentPosition();
        }
//    updatePromoRating();
    }

    void blockUser() {

        usersRef.whereEqualTo("userId", Objects.requireNonNull(currentUser).getUid())
                .get().addOnSuccessListener(snapshots ->
                snapshots.getDocuments().get(0).getReference().update("usersBlocked",
                        FieldValue.arrayUnion(p.getUid())).addOnSuccessListener(aVoid -> {

                    GlobalVariables.getBlockedUsers().add(p.getUid());
                    Toast.makeText(getContext(), "لقد تم حظر هذا المستخدم!",
                            Toast.LENGTH_SHORT).show();

                    Objects.requireNonNull(getActivity()).onBackPressed();

                    //      getDialog().onBackPressed();
                }));
    }

    void getRelatedPromotions() {

        Query relatedQuery =
                promotionRef.whereEqualTo("isBanned", false)
                        .whereEqualTo("isPaused", false)
                        .orderBy("promoid")
                        .whereNotEqualTo("promoid", p.getPromoid());


        if (GlobalVariables.getInstance().getCountryCode() != null) {
            relatedQuery = relatedQuery.whereEqualTo("country",
                    GlobalVariables.getInstance().getCountryCode().toUpperCase());
        }


        if (p.getType().equals("كمبيوتر و لاب توب") || p.getType().equals("الكترونيات")) {

            relatedQuery = relatedQuery
                    .whereIn("type", Arrays.asList("كمبيوتر و لاب توب", "الكترونيات"));
        } else {

            relatedQuery = relatedQuery
                    .whereEqualTo("type", p.getType());

        }


        relatedQuery = relatedQuery.
                orderBy("publishtime", Query.Direction.DESCENDING).limit(4);

        relatedRv.setLayoutManager(new LinearLayoutManager(getContext(),
                RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.width = (int) (getWidth() * 0.4);
                return true;
            }
        });

        relatedPromos = new ArrayList<>();
        final NewestPromosAdapter newestpromosadapter = new NewestPromosAdapter(relatedPromos,
                getContext(), R.layout.newest_promo_item_grid, 3);

        newestpromosadapter.setHasStableIds(true);
        relatedRv.setHasFixedSize(true);
        relatedRv.setAdapter(newestpromosadapter);

        relatedQuery.get().addOnSuccessListener(snapshots -> {

            Log.d("ttt", "related all size: " + snapshots.size());

            if (GlobalVariables.getBlockedUsers().isEmpty()) {
                relatedPromos.addAll(snapshots.toObjects(Promotion.class));

            } else {

                for (DocumentSnapshot snap : snapshots.getDocuments()) {

                    if (GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
                        Log.d("ttt", "removed a blocked promo manually");
                        continue;
                    }

                    relatedPromos.add(snap.toObject(Promotion.class));

                }

            }

        }).addOnCompleteListener(task -> {
            if (relatedPromos.size() > 0) {
                newestpromosadapter.notifyDataSetChanged();
            } else {
                relatedTv.setVisibility(View.GONE);
            }
        });
    }

    void showProfile() {

        UserFragment fragment = new UserFragment();
        if (!p.getUid().equals(Objects.requireNonNull(currentUser).getUid())) {
            Bundle bundle = new Bundle();
            bundle.putString("promouserid", p.getUid());
            fragment.setArguments(bundle);
        }

//    Log.d("ttt",fragment.getTag());
        if (getContext() instanceof HomeActivity) {
            ((HomeActivity) Objects.requireNonNull(getActivity())).addFragmentToHomeContainer(fragment);
        } else if (getContext() instanceof MessagingRealTimeActivity) {
            ((MessagingRealTimeActivity) Objects.requireNonNull(getActivity())).addFragmentToHomeContainer(fragment);
        }


//      fragment.show(getChildFragmentManager(), "UserFragment");
    }

    void inflateOwnerUserMenu() {

        final int menu = p.getIsPaused() ?
                R.menu.user_promo_menu_item_paused_dotted : R.menu.user_promo_menu_item_uppaused_dotted;

        toolbar.inflateMenu(menu);

        toolbar.setOnMenuItemClickListener(item -> {

            if (WifiUtil.checkWifiConnection(getContext())) {
                if (item.getItemId() == R.id.delete_item) {
                    Promotion.deletePromo(getContext(), p);
                } else if (item.getItemId() == R.id.pause_item) {
                    Promotion.pauseOrUnPausePromo(getContext(), p, toolbar.getMenu(), viewedDocumentID);
                } else if (item.getItemId() == R.id.edit_item) {

//                    promotionRef.document(String.valueOf(p.getPromoid()))
//                            .update("isPaused",true).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {

                    final Intent intent = new Intent(requireContext(), PromotionActivity.class)
                            .putExtra("EditablePromotion", p);

                    final String phoneNum = requireActivity()
                            .getSharedPreferences("rbeno", Context.MODE_PRIVATE)
                            .getString("phoneNumber", null);

                    if (phoneNum != null && !phoneNum.isEmpty()) {
                        intent.putExtra("phoneNumber", phoneNum);
                    }
                    startActivityForResult(intent, 2);

                    requireActivity().onBackPressed();

//                        }
//                    });


                }
            }

            return true;
        });

    }

    void showSigninDialog() {
        SigninUtil.getInstance(getContext(), getActivity()).show();
    }

    void inflatePromoMenu() {

        toolbar.inflateMenu(R.menu.promotion_menu);

        if (Objects.requireNonNull(currentUser).isAnonymous()) {

            toolbar.setOnMenuItemClickListener(item -> {

                if (item.getItemId() == R.id.report_this_promo ||
                        item.getItemId() == R.id.block_user) {
                    showSigninDialog();
                }
                return true;
            });

        } else {
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.report_this_promo) {
                    if (WifiUtil.checkWifiConnection(getContext())) {
                        Promotion.reportPromo(getContext(), p.getPromoid(), currentUser.getUid(),
                                viewedDocumentID);
                    }
                } else if (item.getItemId() == R.id.block_user) {
                    if (!GlobalVariables.getBlockedUsers().contains(p.getUid())) {
                        if (WifiUtil.checkWifiConnection(getContext())) {
                            blockUser();
                        }
                    } else {
                        Toast.makeText(getContext(), "لقد تم حظر هذا المستخدم من قبل!"
                                , Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            });
        }

    }

    void checkPromoTypeAndCreateVideoOrImagePager(View view) {

        if (!p.getPromoType().equals(Promotion.TEXT_TYPE)) {

            final ConstraintLayout imageAndVideoConstraint =
                    view.findViewById(R.id.imageAndVideoConstraint);
            imageAndVideoConstraint.setVisibility(View.VISIBLE);

            if (p.getPromoType().equals(Promotion.VIDEO_TYPE)) {

                GlobalVariables.setVideoViewedCount(GlobalVariables.getVideoViewedCount() + 1);
                if (GlobalVariables.getVideoViewedCount() != 0
                        && GlobalVariables.getVideoViewedCount() % 2 == 0) {
//          ((HomeActivity) getActivity()).showRewardVideo();
                    InterstitialAdUtil.showAd(getContext());
                }

                promoVideoPlayer = view.findViewById(R.id.promoVideoPlayer);
                promoVideoPlayer.setVisibility(View.VISIBLE);

                createVideoPlayer();

            } else {

                promotionsPager = view.findViewById(R.id.promotionsPager);
                promotionDotsSlider = view.findViewById(R.id.promotionDotsSlider);

                promotionsPager.setVisibility(View.VISIBLE);
                promotionDotsSlider.setVisibility(View.VISIBLE);

                createImagePager();
            }

        }

    }

    void createVideoPlayer() {

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(Objects.requireNonNull(getContext()));

        trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd());

        exoPlayer = new SimpleExoPlayer.Builder(getContext())
                .setTrackSelector(trackSelector).build();

        final MediaSource mediaSource =
                new ProgressiveMediaSource.Factory(new VideoDataSourceFactory(getContext()))
                        .createMediaSource(MediaItem.fromUri(p.getVideoUrl()));


        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(@NonNull ExoPlaybackException error) {
                if (error.getMessage() != null) {
                    Log.d("exoPlayerPlayback", "Error Message: " + error.getMessage());
                }

                if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                    IOException cause = error.getSourceException();
                    if (cause instanceof HttpDataSource.HttpDataSourceException) {
                        Log.d("exoPlayerPlayback", "HTTP error occurred");
                        // An HTTP error occurred.
                        HttpDataSource.HttpDataSourceException httpError =
                                (HttpDataSource.HttpDataSourceException) cause;
                        // This is the request for which the error occurred.
                        DataSpec requestDataSpec = httpError.dataSpec;
                        // It's possible to find out more about the error both by casting and by
                        // querying the cause.
                        if (httpError instanceof HttpDataSource.InvalidResponseCodeException) {
                            Log.d("exoPlayerPlayback", "httpError, message: " +
                                    httpError.getMessage());
                            // Cast to InvalidResponseCodeException and retrieve the response code,
                            // message and headers.
                        } else {
                            if (httpError.getCause() != null) {

                                Log.d("exoPlayerPlayback", "httpError, cause: " +
                                        httpError.getCause().toString());
                            }


                            // Try calling httpError.getCause() to retrieve the underlying cause,
                            // although note that it may be null.
                        }
                    }
                }
            }
        });


        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);

//        promoVideoPlayer.findViewById(R.id.exo_rew).setOnClickListener(v -> {
//            if (exoPlayer.getCurrentPosition() < 3000) {
//                exoPlayer.seekTo(0);
//            } else {
//                exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 3000);
//            }
//        });
//
//        promoVideoPlayer.findViewById(R.id.exo_ffwd).setOnClickListener(v -> {
//            if (exoPlayer.getCurrentPosition() < exoPlayer.getDuration() - 3000) {
//                exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 3000);
//            } else {
//                exoPlayer.seekTo(exoPlayer.getDuration());
//            }
//        });

//        final long[] firstClickTime = {0};

        promoVideoPlayer.getVideoSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if ((System.currentTimeMillis() - firstClickTime[0]) < 500) {
//                    showFullScreenVideo();
//                }
                showFullScreenVideo();
//                firstClickTime[0] = System.currentTimeMillis();
            }
        });

//        promoVideoPlayer.findViewById(R.id.exoplayer_fullscreen_icon).setOnClickListener(v -> {
//            showFullScreenVideo();
//        });

        promoVideoPlayer.setPlayer(exoPlayer);
    }


    private void showFullScreenVideo() {

        final FullScreenVideoFragment fragment =
                new FullScreenVideoFragment(getContext(), p, exoPlayer.getCurrentPosition());

        fragment.getLifecycle().addObserver((LifecycleEventObserver) (ON_DESTROYED, event) -> {

            if (event == Lifecycle.Event.ON_DESTROY) {
                Log.d("promoFragments", "video fragment ON_DESTROY");

                if (exoPlayer != null) {
                    promoVideoPlayer.setVisibility(View.VISIBLE);
                    exoPlayer.seekTo(fragment.lastPosition);
                    exoPlayer.setPlayWhenReady(true);
                }

                ((HomeActivity) Objects.requireNonNull(getActivity()))
                        .lockDrawer(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

        });

        if (exoPlayer != null) {
            promoVideoPlayer.setVisibility(View.INVISIBLE);
            exoPlayer.setPlayWhenReady(false);
        }

        ((HomeActivity) Objects.requireNonNull(getActivity()))
                .lockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

//      promoVideoPlayer.setVisibility(View.INVISIBLE);
        ((HomeActivity) Objects.requireNonNull(getActivity())).addFragmentToHomeContainer(fragment);
//      fragment.show(getChildFragmentManager(), "videoPagerVertical");
//        dialogFragment.show(getChildFragmentManager(), "fullScreen");

    }

    void createImagePager() {

        final PromotionViewPager viewPagerAdapter =
                new PromotionViewPager(getContext(), p.getPromoimages());

        promotionsPager.setAdapter(viewPagerAdapter);


        if (p.getPromoimages().size() > 1) {

            promotionsPager
                    .setOffscreenPageLimit(viewPagerAdapter.getCount() - 1);

            final ImageView[] dots = new ImageView[viewPagerAdapter.getCount()];

            final LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

            for (int i = 0; i < viewPagerAdapter.getCount(); i++) {

                dots[i] = new ImageView(getContext());

                if (i == 0) {
                    dots[0].setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getContext()),
                            R.drawable.promoton_active_slider));
                } else {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(),
                            R.drawable.promotion_slider_dots));
                }

                final float density = getResources().getDisplayMetrics().density;
                params.setMargins((int) (4 * density), 0, (int) (4 * density), 0);

                promotionDotsSlider.addView(dots[i], params);

            }

            promotionsPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                int previousDot = 0;

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {

                    dots[previousDot].setImageDrawable
                            (ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.promotion_slider_dots));

                    previousDot = position;

                    dots[position].setImageDrawable
                            (ContextCompat.getDrawable(getContext(), R.drawable.promoton_active_slider));

                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }
    }

    void getUserInfo() {
        usersRef.whereEqualTo("userId", p.getUid()).get()
                .addOnSuccessListener(snapshots -> {

                    final DocumentSnapshot ds = snapshots.getDocuments().get(0);

                    final String imageUrl = ds.getString("imageUrl");
                    userNameTv.setText(ds.getString("username"));
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).fit().into(promoUserIv);
                        Picasso.get().load(imageUrl).fit().into(promoUserIv2);
                    }
                });
    }

    void fillPromoData() {

        promotionId.setText("رقم الإعلان: " + p.getPromoid());

        promotionTitle.setText(p.getTitle());

        promotionPublish.setText(new SimpleDateFormat("MM/dd/yyyy",
                Locale.getDefault()).format(p.getPublishtime() * 1000));

//        if (p.getType().equals("اليكترونيات")) {
//            promotionCategoryTv.setText("القسم: الكترونيات");
//        } else {
        promotionCategoryTv.setText("القسم: " + p.getType());
//        }

        SpannableString s = new SpannableString(promotionCategoryTv.getText());
        s.setSpan(new UnderlineSpan(), 0, s.length(), 0);
        promotionCategoryTv.setText(s);

        promotionCategoryTv.setOnClickListener(this);

        final String priceFormatted = String.format(Locale.getDefault(), "%,d", (long) p.getPrice());

        promotionCurrencyTv.setText(Html.fromHtml(
                "<font color='#991914'> " + priceFormatted + "</font>" + " " + CurrencyUtil.getArabicSymbol(p.getCurrency())));


//        Currency currency = null;
//
//        if (p.getCurrency() != null) {
//            try {
//                currency = Currency.getInstance(p.getCurrency());
//            } catch (IllegalArgumentException e) {
//                Log.d("currency", "code shit");
//            }
//        }
//
//        String displayName;
//
////        final Currency arabicCurrency = Currency.getInstance(new Locale("ar","SA"));
//
//        final String price = String.format(Locale.getDefault(), "%,d", ((long) p.getPrice()));
//
//        if (currency != null) {
//            try {
//                Log.d("currency", "arabic display: " + currency.getDisplayName(new Locale("ar")));
//                displayName = currency.getSymbol(new Locale("ar"));
//
//            } catch (IllegalArgumentException e) {
//                Log.d("currency", "arabic display exeption");
//                displayName = p.getCurrency();
//            }
//
//
//            if(!displayName.isEmpty() && displayName.charAt(displayName.length() - 2) == '.'){
//                Log.d("ttt","should remove last dot");
//                displayName = displayName.substring(0,displayName.length() - 2);
//
//            }
//
//        } else {
//            displayName = p.getCurrency();
//        }
//
//        promotionCurrencyTv.setText(Html.fromHtml(price + " "
//                + "<font color='#991914'> " + displayName + "</font>"));

//
//    try{
//      Log.d("currency","english display: "+currency.getDisplayName(new Locale("en")));
//    }catch (IllegalArgumentException e){
//      Log.d("currency","english display exeption");
//    }
//    try{
//      Log.d("currency","display: "+currency.getDisplayName());
//    }catch (IllegalArgumentException e){
//      Log.d("currency","display exeption");
//    }
//
//    try{
//      Log.d("currency","arabic sympol: "+ currency.getSymbol(new Locale("ar")));
//    }catch (IllegalArgumentException e){
//      Log.d("currency","arabic sympol exeption");
//    }
//
//    try{
//      Log.d("currency","english sympol: "+ currency.getSymbol(new Locale("en")));
//    }catch (IllegalArgumentException e){
//      Log.d("currency","english sympol exeption");
//    }
//
//    try{
//      Log.d("currency","sympol: "+ currency.getSymbol());
//    }catch (IllegalArgumentException e){
//      Log.d("currency","sympol exeption");
//    }
//
//    try{
//      Log.d("currency","sympol: "+ Currency.getInstance(p.getCurrency()).getCurrencyCode());
//    }catch (IllegalArgumentException e){
//      Log.d("currency","sympol exeption");
//    }


//        promotionPrice.setText(String.format(Locale.getDefault(),
//                "%,d", ((long) p.getPrice())));


        Log.d("ttt", "(p.getCountry(): " + (p.getCountry()));
        Log.d("ttt", "getCountryName: " + CountryUtil.getCountryName(p.getCountry()));
        Log.d("ttt", "countryCodeToEmoji: " + EmojiUtil.countryCodeToEmoji(p.getCountry()));

        String locationName = CountryUtil.getCountryName(p.getCountry()) + " " +
                EmojiUtil.countryCodeToEmoji(p.getCountry());

        Log.d("ttt", "p.getCityName(): " + p.getCityName());
        if (p.getCityName() != null) {
            locationName = locationName.concat(" - " + p.getCityName());
        }

        promotionCountryTv.setText(locationName);

        promotionDescTv.setText(p.getDescription());

        promotionRef.whereEqualTo("promoid", p.getPromoid()).get()
                .addOnSuccessListener(snaps -> {

                    final DocumentSnapshot ds = snaps.getDocuments().get(0);

                    viewedDocumentID = ds.getId();

                    ds.getReference().update("viewcount",
                            FieldValue.increment(1));

                    ratingBar.setRating(Objects.requireNonNull(ds.getDouble("rating")).floatValue());

                    promotionFavsTv.setText(String.valueOf(ds.getLong("favcount")));
                    promotionViews.setText(String.valueOf(ds.getLong("viewcount")));

                    setRatingCLickListener(ds.getReference());

                });

    }

    void setRatingCLickListener(DocumentReference dr) {
        if (!Objects.requireNonNull(currentUser).isAnonymous()) {
            dr.collection("ratings")
                    .whereEqualTo("userid", currentUser.getUid())
                    .get().addOnCompleteListener(task -> {

                if (!task.getResult().isEmpty()) {
                    ratingBar.setOnClickListener(v ->
                            Toast.makeText(getContext(),
                                    "لقد قمت بتقييم هذا الإعلان من قبل!",
                                    Toast.LENGTH_SHORT).show());
                } else {

                    ratingBar.setOnClickListener(v -> showRatingDialog());
                }

            });
        } else {
            ratingBar.setOnClickListener(v -> SigninUtil.getInstance(getContext(),
                    getActivity()).show());
        }

        ratingBar.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                ratingBar.setClickable(false);
                ratingBar.performClick();
            }
            return true;
        });
    }

    void messageUser() {

        if (currentUserIsBlocked) {
            Toast.makeText(getContext(),
                    "عذرا, لا يمكنك مراسلة هذا المستخدم!", Toast.LENGTH_SHORT).show();
            return;
        }

        messagingLayout.setClickable(false);

//    if(chatsRef==null)
//      chatsRef = FirebaseFirestore.getInstance().collection("chats");


        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Messages").child(currentUser.getUid() + "-" + p.getUid() + "-" + p.getPromoid());


        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    if (snapshot.child("isDeletedFor:" + currentUser.getUid()).getValue(Boolean.class)) {
                        Toast.makeText(getContext(), "عذرا لا يمكنك المراسلة على هذا" +
                                " الإعلان بسبب حذفك للمحادثة!", Toast.LENGTH_LONG).show();
                    } else {
                        messagingLayout.setClickable(true);
                        startMessagingActivity();
                    }
                } else {
                    messagingLayout.setClickable(true);
                    startMessagingActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//    chatsRef.document(
//            p.getUid() + "_" +
//                    Objects.requireNonNull(currentUser).getUid() + "_" +
//                    p.getPromoid())
//            .get().addOnCompleteListener(task -> {
//      if(task.getResult().exists()){
//
//        if(task.getResult().getBoolean("isDeletedFor:"+currentUser.getUid())){
//
//          Toast.makeText(getContext(), "عذرا لا يمكنك المراسلة على هذا" +
//                  " الإعلان بسبب حذفك للمحادثة!", Toast.LENGTH_LONG).show();
//
//        }else{
//
//          messagingLayout.setClickable(true);
//          startMessagingActivity();
//
//        }
//
//      }else{
//
//        chatsRef.document(
//                currentUser.getUid() + "_" +
//                        p.getUid() + "_" +
//                        p.getPromoid())
//                .get().addOnCompleteListener(task1 -> {
//          if (task1.getResult().exists()) {
//            if (task1.getResult().getBoolean("isDeletedFor:"+currentUser.getUid())) {
//              Toast.makeText(getContext(), "عذرا لا يمكنك المراسلة على هذا" +
//                      " الإعلان بسبب حذفك له!", Toast.LENGTH_LONG).show();
//            } else {
//              messagingLayout.setClickable(true);
//              startMessagingActivity();
//            }
//          } else {
//            messagingLayout.setClickable(true);
//            startMessagingActivity();
//          }
//        });
//
//      }
//    });

    }

    void setFavCLickListener() {
        if (WifiUtil.checkWifiConnection(getContext())) {

            favImage.setClickable(false);

            if (GlobalVariables.getFavPromosIds().contains(p.getPromoid())) {
                Log.d("ttt", "already facoured");

                promotionRef.document(viewedDocumentID).update("favcount", FieldValue.increment(-1));

                usersRef.whereEqualTo("userId", Objects.requireNonNull(currentUser).getUid())
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {

                        if (snapshots.isEmpty())
                            return;

                        snapshots.getDocuments().get(0).getReference().update("favpromosids",
                                FieldValue.arrayRemove(p.getPromoid())).addOnSuccessListener(v -> {
                            if (getContext() != null) {

//                GlobalVariables.getFavPromosIds().remove(p.getPromoid());

                                favImage.setImageResource(R.drawable.heart_grey_outlined);

                                promotionFavsTv.setText(String.valueOf(
                                        Integer.parseInt(promotionFavsTv.getText().toString()) - 1));

                                Toast.makeText(getContext(), "تمت الازالة من المفضلة!",
                                        Toast.LENGTH_SHORT).show();
                                favImage.setClickable(true);
                            }
                        }).addOnFailureListener(e -> {

                            Toast.makeText(getContext(), "لقد فشلت الازالة من المفضلة" +
                                    "! حاول مرة اخرى", Toast.LENGTH_SHORT).show();

                            favImage.setClickable(true);

                        });
                    }
                });

                if (!p.getUid().equals(currentUser.getUid())) {
                    FirestoreNotificationSender.deleteFirestoreNotification(p.getPromoid(),
                            p.getUid(), "favourite");
                }


            } else {

                Log.d("ttt", "not facoured");
                promotionFavsTv.setText(String.valueOf(
                        Integer.parseInt(promotionFavsTv.getText().toString()) + 1));

                favImage.setImageResource(R.drawable.heart_icon);
                usersRef.whereEqualTo("userId", Objects.requireNonNull(currentUser).getUid())
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        if (snapshots.isEmpty())
                            return;

                        snapshots.getDocuments().get(0).getReference().update("favpromosids",
                                FieldValue.arrayUnion(p.getPromoid())).addOnCompleteListener(task17 -> {
                            Toast.makeText(getContext(), "تمت الاضافة الى المفضلة!", Toast.LENGTH_SHORT)
                                    .show();
//              GlobalVariables.getFavPromosIds().add(p.getPromoid());
                            favImage.setClickable(true);
                        });

                    }
                });

                promotionRef.document(viewedDocumentID).update("favcount", FieldValue.increment(1));


                if (!p.getUid().equals(currentUser.getUid())) {

                    final String name = currentDs.getString("username");

                    if (!GlobalVariables.getPreviousSentNotifications()
                            .contains(currentUser.getUid() + "favourite" + p.getPromoid())) {

                        final String image = currentDs.getString("imageurl");


                        CloudMessagingNotificationsSender.sendNotification(
                                p.getUid(),
                                new Data(
                                        currentUser.getUid(),
                                        p.getTitle(),
                                        name + " قام بالإعجاب باعلانك رقم: " + p.getPromoid(),
                                        image,
                                        name,
                                        "favourite",
                                        p.getPromoid()
                                ));
                    }

                    FirestoreNotificationSender.sendFirestoreNotification(p.getPromoid(),
                            p.getUid(), "favourite");

                }

            }

        }


    }

    void initFavAndProfileClickers() {
        favImage.setOnClickListener(this);
        promoUserIv2.setOnClickListener(this);
        userNameTv.setOnClickListener(this);
        shareImageIv.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if (id == R.id.promotionCategoryTv) {

            final AllPromosFragment promosFragment = new AllPromosFragment();
            Bundle b = new Bundle();
            b.putString("category", p.getType());
            promosFragment.setArguments(b);

            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) Objects.requireNonNull(getActivity()))
                        .addFragmentToHomeContainer(promosFragment);
            } else {
                ((MessagingRealTimeActivity) Objects.requireNonNull(getActivity()))
                        .addFragmentToHomeContainer(promosFragment);
            }


        } else if (id == R.id.favImage) {
            if (WifiUtil.checkWifiConnection(getContext())) {
                if (currentUserIsBlocked) {
                    favImage.setOnClickListener(view18 -> Toast.makeText(getContext(),
                            "عذرا, لا يمكنك إضافة هذا الإعلان إلى المفضلة!",
                            Toast.LENGTH_SHORT).show());
                } else {
                    setFavCLickListener();
                }
            }
        } else if (id == R.id.promoUserIv2 || id == R.id.userNameTv) {
            if (WifiUtil.checkWifiConnection(getContext())) {
                if (currentUserIsBlocked) {
                    Toast.makeText(getContext(),
                            "عذرا, لا يمكنك زيارة صفحة هذا المستخدم!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    showProfile();
                }
            }
        } else if (id == R.id.shareImageIv) {
            if (WifiUtil.checkWifiConnection(getContext())) {
                file = Promotion.sharePromo(p, getContext(), file, shareImageIv);
            }
        } else if (id == R.id.messagingLayout) {
            if (WifiUtil.checkWifiConnection(getContext())) {

                BottomSheetDialog contactBottomDialog = new BottomSheetDialog(requireContext());
                final View contactLayoutView =
                        getLayoutInflater().inflate(R.layout.contact_bottom_sheet_menu, null);

                contactLayoutView.findViewById(R.id.messageUserBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contactBottomDialog.dismiss();
                        messageUser();
                    }
                });

                final Button phoneNumBtn = contactLayoutView.findViewById(R.id.phoneNumBtn);

                if (!p.isHidePhone() && userPhoneNumber != null && !userPhoneNumber.isEmpty()) {

                    phoneNumBtn.setText("رقم الهاتف: " + userPhoneNumber);
                    phoneNumBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            contactBottomDialog.dismiss();

                            final ClipboardManager clipboardManager = (ClipboardManager)
                                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE);

                            clipboardManager.setPrimaryClip(
                                    ClipData.newPlainText("phoneNum", userPhoneNumber));

                            Toast.makeText(requireContext(), "لقد تم نسخ رقم الهاتف!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    phoneNumBtn.setVisibility(View.GONE);
                }

                contactBottomDialog.setContentView(contactLayoutView);
                contactBottomDialog.show();

            }

        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (promotionDeleteReceiver != null)
            Objects.requireNonNull(getContext()).unregisterReceiver(promotionDeleteReceiver);

    }

    void setupDeletionReceiver() {

        final long oldId = p.getPromoid();

        promotionDeleteReceiver =
                new PromotionDeleteReceiver() {
                    @Override

                    public void onReceive(Context context, Intent intent) {
                        final long promoId = intent.getLongExtra("promoId", 0);

                        Log.d("ttt", "info deleted promoId: " + promoId);
                        Log.d("ttt", "info promoId: " + oldId);

                        if (promoId == oldId) {
//                  Toast.makeText(context, R.string.promo_removed
//                          , Toast.LENGTH_SHORT).show();

                            Objects.requireNonNull(getActivity()).onBackPressed();

                        } else {
                            checkAndDeletePromoFromList(relatedPromos, promoId);
                        }

                    }
                };


        Objects.requireNonNull(getContext()).registerReceiver(promotionDeleteReceiver,
                new IntentFilter(BuildConfig.APPLICATION_ID + ".promoDelete"));

    }

    void checkAndDeletePromoFromList(ArrayList<Promotion> promos, long id) {
        if (promos != null && !promos.isEmpty()) {
            for (Promotion promo : promos) {
                if (promo.getPromoid() == id) {
                    promo.setIsBanned(true);
                    break;
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Log.d("ttt", "onActivityResult");

        if (requestCode == 2 && resultCode == 3 && data != null && data.hasExtra("editedPromo")) {

//            final Fragment fragment = new PromotionInfoFragment((Promotion) data.getSerializableExtra("editedPromo"));
//
//            if(requireActivity() instanceof HomeActivity){
//                ((HomeActivity) requireActivity()).removeAndReplaceFragment(fragment);
//            }else if(requireActivity() instanceof MessagingRealTimeActivity){
//                ((MessagingRealTimeActivity) requireActivity()).removeAndReplaceFragment(fragment);
//
//            }

            Log.d("ttt", "returned from edited");
        }

    }
}