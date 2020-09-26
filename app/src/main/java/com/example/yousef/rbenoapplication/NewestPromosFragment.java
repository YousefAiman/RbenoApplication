package com.example.yousef.rbenoapplication;


import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class NewestPromosFragment extends androidx.fragment.app.Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final float scaleFactor = 0.91f;
    public NestedScrollView scrollView;
//    public VideoViewPager viewPagerAdapter;
    VideoViewPagerAdapter videoViewPagerAdapter;
    ExoPlayer exoPlayer;
    ViewPager videoViewPager;
    int lastQuery = 0;
    //    TextView categoryTv1;
//    TextView categoryTv2;
//    TextView categoryTv3;
//    TextView categoryTv4;
    TextView[] catTvs;
    private ArrayList<Promotion> promotions;
//    private ArrayList<Integer> removedItemsPositions;
    private SwipeRefreshLayout swipeRefreshLayout;
    private newestpromosadapter adapter;
    private RecyclerView lv;
    private ArrayList<String> videoUrls;
    private LinearLayout sliderLayout;

    private List<ImageView> dots;
    private ArrayList<Promotion> videoPromotionsItems;
    private boolean isLoading = true;
    private DocumentSnapshot lastVideoResult;
    private DocumentSnapshot lastImageResult;
    private DocumentSnapshot lastTextResult;
    private PlayerView playerView;
    private DataSource.Factory factory;
    private RewardedVideoAd mRewardedVideoAd;
    private Query updatedQuery;
    private Query videosQuery;
    private Query imagesQuery;
    private Query textQuery;
    private boolean noVideosLeft = false;
    private boolean noImagesLeft = false;
    private boolean noTextLeft = false;
    int lastSelectedVideo = -1;
    ListenerRegistration updatedPromosListener;
    public NewestPromosFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_newest_promos, container, false);
        scrollView = view.findViewById(R.id.scrollview);
        lv = view.findViewById(R.id.newestPromosRecyclerView);
        videoViewPager = view.findViewById(R.id.videoViewPager);
        sliderLayout = view.findViewById(R.id.VideosDotsSlider);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
//        promoimage1 = view.findViewById(R.id.promoimage1);
        catTvs = new TextView[]{view.findViewById(R.id.categoryTv1)
                , view.findViewById(R.id.categoryTv2), view.findViewById(R.id.categoryTv3), view.findViewById(R.id.categoryTv4)};
//        categoryTv1 = view.findViewById(R.id.categoryTv1);
//        categoryTv2 = view.findViewById(R.id.categoryTv2);
//        categoryTv3 = view.findViewById(R.id.categoryTv3);
//        categoryTv4 = view.findViewById(R.id.categoryTv4);
        MobileAds.initialize(getContext());
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        videoViewPager.getLayoutParams().height = GlobalVariables.getWindowHeight() / 4;


//        llm.setOrientation(LinearLayoutManager.VERTICAL);
        lv.setLayoutManager(new LinearLayoutManager(getContext()));
        lv.setItemAnimator(null);
//        lv.setItemViewCacheSize(20);
        promotions = new ArrayList<>();
        adapter = new newestpromosadapter(promotions, getContext());
        adapter.setHasStableIds(true);
        lv.setAdapter(adapter);

        videosQuery = FirebaseFirestore.getInstance().collection("promotions")
//                        .whereEqualTo("country", countryName)
                .whereEqualTo("promoType", "video")
                .whereEqualTo("isBanned", false)
                .orderBy("publishtime", Query.Direction.DESCENDING).limit(5);

        imagesQuery = FirebaseFirestore.getInstance().collection("promotions")
//                        .whereEqualTo("country", countryName)
                .whereEqualTo("promoType", "image")
                .whereEqualTo("isBanned", false)
                .orderBy("publishtime", Query.Direction.DESCENDING).limit(8);

        textQuery = FirebaseFirestore.getInstance().collection("promotions")
//                        .whereEqualTo("country", countryName)
                .whereEqualTo("promoType", "text")
                .whereEqualTo("isBanned", false)
                .orderBy("publishtime", Query.Direction.DESCENDING).limit(10);

        getUpdatedPromotions();


        getMostSearchedCategories();
        scrollView.setNestedScrollingEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red));


        videoPromotionsItems = new ArrayList<>();
        videoUrls = new ArrayList<>();

        createPager();



        videoViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            Drawable sliderDot = ContextCompat.getDrawable(getContext(), R.drawable.promotion_slider_dots);
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

//                if(lastSelectedVideo!=-1){
//                    View view = videoViewPager.getChildAt(lastSelectedVideo);
//                    if(view!=null){
//                    playerView = view.findViewById(R.id.videoPlayerView);
//                    if (playerView.getPlayer() != null) {
//                        ConstraintLayout playLayout = view.findViewById(R.id.playVideoLayout);
//                        ImageView videoThumbnail = view.findViewById(R.id.videoThumbnail);
//                        playerView.setPlayer(null);
//                        exoPlayer.release();
//                        exoPlayer = null;
//
//                        if (playLayout.getVisibility() == View.INVISIBLE) {
//                            playLayout.setVisibility(View.VISIBLE);
//                        }
//                        if (videoThumbnail.getVisibility() == View.GONE) {
//                            videoThumbnail.setVisibility(View.VISIBLE);
//                        }
//                    }
//                    }
//                }

                for (int i = 0; i < videoViewPagerAdapter.getCount(); i++) {
                    dots.get(i).setImageDrawable(sliderDot);
                }
                dots.get(position).setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.promo_icon_round_red));

                View viewPagerView = videoViewPager.getChildAt(position);
                ConstraintLayout playLayout = viewPagerView.findViewById(R.id.playVideoLayout);
                playerView = viewPagerView.findViewById(R.id.videoPlayerView);

                factory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "RbenoApp"));

                playerView.setOnClickListener(view1 -> {

                    if (GlobalVariables.getVideoViewedCount() != 0 && GlobalVariables.getVideoViewedCount() % 10 == 0) {
                        showRewardVideo();
                        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                            @Override
                            public void onRewardedVideoAdLoaded() {
                                mRewardedVideoAd.show();
                            }

                            @Override
                            public void onRewardedVideoAdOpened() {
                            }

                            @Override
                            public void onRewardedVideoStarted() {
                            }

                            @Override
                            public void onRewardedVideoAdClosed() {
                            }

                            @Override
                            public void onRewarded(RewardItem rewardItem) {
                            }

                            @Override
                            public void onRewardedVideoAdLeftApplication() {
                            }

                            @Override
                            public void onRewardedVideoAdFailedToLoad(int i) {
                            }

                            @Override
                            public void onRewardedVideoCompleted() {
                            }
                        });
                    } else {
                        GlobalVariables.setVideoViewedCount(GlobalVariables.getVideoViewedCount() + 1);
                    }

//                    playerLayoutClick(position);

                });

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        lv.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            float y = 0;

            @Override
            public void onScrollChanged() {
                if (!scrollView.canScrollVertically(View.SCROLL_AXIS_VERTICAL) && scrollView.getScrollY() > y) {
                    if (!isLoading) {

                        isLoading = true;
                        getUpdatedPromotions();
                    }
                }
                y = scrollView.getScrollY();
            }
        });
    }

    @Override
    public void onRefresh() {
        if (GlobalVariables.isWifiIsOn()) {
            swipeRefreshLayout.setRefreshing(true);

//            if (viewPagerAdapter != null) {
//                videoPromotionsItems.clear();
//                videoUrls.clear();
//                viewPagerAdapter.notifyDataSetChanged();
//            }
//            createPager();


            if (videoViewPagerAdapter != null) {
                videoPromotionsItems.clear();
                videoUrls.clear();
                videoViewPagerAdapter.notifyDataSetChanged();
            }
            createPager();

            getMostSearchedCategories();
            if(updatedPromosListener!=null)updatedPromosListener.remove();

            if (!promotions.isEmpty()) {
                promotions.clear();
                adapter.notifyDataSetChanged();
            }
            isLoading = false;
            lastVideoResult = null;
            lastImageResult = null;
            lastTextResult = null;
            noVideosLeft = false;
            noTextLeft = false;
            noImagesLeft = false;
            getUpdatedPromotions();

        } else {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), getString(R.string.noConnection2), Toast.LENGTH_SHORT).show();
        }

    }

    private void getUpdatedPromotions() {
        if (GlobalVariables.isWifiIsOn()) {

            int previousPos = promotions.size();

            new Thread(() -> {
                if (!noVideosLeft) {
                    if (lastVideoResult == null) {
                        updatedQuery = videosQuery;
                        Log.d("ttt", "query is video");
                    } else {
                        updatedQuery = videosQuery.startAfter(lastVideoResult);
                    }
                    lastQuery = 1;
                } else if (!noImagesLeft) {
                    if (lastImageResult == null) {
                        updatedQuery = imagesQuery;
                        Log.d("ttt", "query is image");
                    } else {
                        updatedQuery = imagesQuery.startAfter(lastImageResult);
                    }
                    lastQuery = 2;
                } else if (!noTextLeft) {
                    if (lastTextResult == null) {
                        updatedQuery = textQuery;
                        Log.d("ttt", "query is text");
                    } else {
                        updatedQuery = textQuery.startAfter(lastTextResult);
                    }
                    lastQuery = 3;
                } else {
                    isLoading = false;
                    swipeRefreshLayout.setRefreshing(false);
                    Log.d("ttt", "no more promos");
                    return;
                }
                swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
                updatedQuery.get().addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        List<DocumentSnapshot> documentSnapshots = snapshots.getDocuments();
                        List<String> blockedUsers = GlobalVariables.getBlockedUsers();
                        if (blockedUsers != null && !blockedUsers.isEmpty()) {
                            for (DocumentSnapshot snap : documentSnapshots) {
                                if (blockedUsers.contains(snap.getString("uid"))) continue;
                                promotions.add(snap.toObject(Promotion.class));
                            }
                        } else {
                            for (DocumentSnapshot snap : documentSnapshots) {
                                promotions.add(snap.toObject(Promotion.class));
                            }
                        }
                        lv.post(() -> adapter.notifyItemRangeInserted(previousPos + 1, promotions.size() - previousPos));
                        swipeRefreshLayout.setRefreshing(false);

                        updatedPromosListener = updatedQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                for(DocumentChange documentChange:value.getDocumentChanges()){
                                    if(documentChange.getType() == DocumentChange.Type.REMOVED){
                                        long removedPromoId = documentChange.getDocument().getLong("promoid");
                                        for(Promotion promo:promotions){
                                            if(promo.getPromoid() == removedPromoId){
                                                promotions.remove(promo);
                                                break;
                                            }
                                        }
                                        for(int i=0;i<videoPromotionsItems.size();i++){
                                            if(videoPromotionsItems.get(i).getPromoid() == removedPromoId){
                                                videoUrls.remove(videoPromotionsItems.get(i).getVideoUrl());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        int newSize = documentSnapshots.size();


                        switch (lastQuery) {
                            case 1:
                                lastVideoResult = documentSnapshots.get(documentSnapshots.size() - 1);
                                if (newSize < 5) {
                                    noVideosLeft = true;
                                    Log.d("ttt", "no more videos");
                                    getUpdatedPromotions();
                                }
                                break;
                            case 2:
                                lastImageResult = documentSnapshots.get(documentSnapshots.size() - 1);
                                if (newSize < 8) {
                                    noImagesLeft = true;
                                    Log.d("ttt", "no more images");
                                    getUpdatedPromotions();
                                }
                                break;
                            case 3:
                                lastTextResult = documentSnapshots.get(documentSnapshots.size() - 1);
                                if (newSize < 10) {
                                    Log.d("ttt", "no more text");
                                    noTextLeft = true;
                                }
                                break;
                        }
                        isLoading = false;
                    } else {
                        isLoading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        if (updatedQuery == videosQuery) {
                            noVideosLeft = true;
                            Log.d("ttt", "no more video");
                            getUpdatedPromotions();

                        } else if (updatedQuery == imagesQuery) {
                            noImagesLeft = true;
                            Log.d("ttt", "no more images");
                            getUpdatedPromotions();

                        } else {
                            noTextLeft = true;
                            Log.d("ttt", "no more text");
                        }
                    }

                });
            }).start();
        } else {
            isLoading = false;
            Toast.makeText(getContext(), getString(R.string.noConnection2), Toast.LENGTH_SHORT).show();
        }
    }

    private void showRewardVideo() {
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getContext());
        mRewardedVideoAd.loadAd("ca-app-pub-6990486336142688/3991520948",
                new AdRequest.Builder().build());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayer != null && exoPlayer.getPlayWhenReady()) {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void onPause() {
        if (exoPlayer != null && exoPlayer.isPlaying()) {
            exoPlayer.setPlayWhenReady(false);
        }
        if (mRewardedVideoAd != null) {
            mRewardedVideoAd.pause(getContext());
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (playerView != null) {
            if (playerView.getPlayer() != null) {
                playerView.setPlayer(null);
                exoPlayer.release();
                exoPlayer = null;
            }
        }
        if (mRewardedVideoAd != null) {
            mRewardedVideoAd.destroy(getContext());
        }
        if(updatedPromosListener!=null)updatedPromosListener.remove();
        super.onDestroy();
    }

    private void createPager() {

        GlobalVariables.setDensity(getResources().getDisplayMetrics().density);

        int viewPagerPadding = (int) (16 * GlobalVariables.getDensity()) + (int) (8 * GlobalVariables.getDensity());

        new Thread(() -> FirebaseFirestore.getInstance().collection("promotions").whereEqualTo("promoType", "video")
                .whereEqualTo("isBanned", false)
                .orderBy("publishtime", Query.Direction.DESCENDING).
                        get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot snap : queryDocumentSnapshots) {
                        Promotion p = snap.toObject(Promotion.class);
                        videoPromotionsItems.add(p);
                        videoUrls.add(p.getVideoUrl());
                    }
//                    viewPagerAdapter = new VideoViewPager(getContext(), videoPromotionsItems);
//                    videoViewPager.setOffscreenPageLimit(2);
//                    videoViewPager.setPadding(viewPagerPadding, 0, viewPagerPadding, 0);
//                    videoViewPager.setPageMargin((int) (10 * GlobalVariables.getDensity()));
//                    videoViewPager.setPageTransformer(false, (page, position) -> page.setScaleY((1 - Math.abs(position) * (1 - scaleFactor))));
//                    videoViewPager.setAdapter(viewPagerAdapter);

                     videoViewPagerAdapter = new VideoViewPagerAdapter(getChildFragmentManager(),
                            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                            videoPromotionsItems);
                    videoViewPager.setOffscreenPageLimit(2);
                    videoViewPager.setPadding(viewPagerPadding, 0, viewPagerPadding, 0);
                    videoViewPager.setPageMargin((int) (10 * GlobalVariables.getDensity()));
                    videoViewPager.setPageTransformer(false, (page, position) -> page.setScaleY((1 - Math.abs(position) * (1 - scaleFactor))));



                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.isComplete()) {
                        if (!task.getResult().isEmpty()) {
                            sliderLayout.setVisibility(View.VISIBLE);
                            videoViewPager.setVisibility(View.VISIBLE);
                            if (sliderLayout.getChildCount() > 0) {
                                sliderLayout.removeAllViews();
                            }
//                            dotsCount = viewPagerAdapter.getCount();

                            dots = new ArrayList<>();

                            Drawable sliderDots = ContextCompat.getDrawable(getContext(), R.drawable.promotion_slider_dots);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins((int) (5 * GlobalVariables.getDensity()), 0, (int) (5 * GlobalVariables.getDensity()), 0);

                            getActivity().runOnUiThread(() -> {
                                videoViewPager.setAdapter(videoViewPagerAdapter);
                                for (int i = 0; i <  videoViewPagerAdapter.getCount(); i++) {
                                    dots.add(new ImageView(getContext()));
                                    dots.get(i).setImageDrawable(sliderDots);
                                    sliderLayout.addView(dots.get(i), params);
                                }
                                if (videoViewPagerAdapter.getCount()>0) {
                                    dots.get(0).setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.promo_icon_round_red));
//                                    videoViewPager.getChildAt(0).findViewById(R.id.videoPlayerView).setOnClickListener(v->
//                                            playerLayoutClick(0));
                                }
                            });

                        } else {
                            videoViewPager.setVisibility(View.GONE);
                            if (sliderLayout.getChildCount() > 0) {
                                sliderLayout.removeAllViews();
                            }
                        }
                    }
                })).start();
    }


    void getMostSearchedCategories() {
        new Thread(() -> FirebaseFirestore.getInstance().collection("searches").orderBy("searchCount", Query.Direction.DESCENDING).limit(4).get().addOnSuccessListener(queryDocumentSnapshots -> {
//            TextView[] catTvs = new TextView[]{categoryTv1, categoryTv2, categoryTv3, categoryTv4};
            List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
            for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                int finalI = i;
                String catId = documentSnapshots.get(finalI).getId();
                catTvs[i].post(() -> {
                    Log.d("ttt", "category: " + catId);
                    catTvs[finalI].setText(catId);
                    switch (catId) {
                        case "موبيلات":
                            catTvs[finalI].setBackgroundColor(getResources().getColor(R.color.blueMobile));
                            break;
                        case "كمبيوتر و لاب توب":
                            catTvs[finalI].setBackgroundColor(getResources().getColor(R.color.pcBlack));
                            break;
                        case "عقارات":
                            catTvs[finalI].setBackgroundColor(getResources().getColor(R.color.houseOrange));
                            break;
                        case "سيارات":
                            catTvs[finalI].setBackgroundColor(getResources().getColor(R.color.red));
                            break;
                        case "اليكترونيات":
                            catTvs[finalI].setBackgroundColor(getResources().getColor(R.color.electGrey));
                            break;
                        case "أثاث":
                            catTvs[finalI].setBackgroundColor(getResources().getColor(R.color.greenFurniture));
                            break;
                    }
                });
                catTvs[i].setOnClickListener(v -> {
                    if (exoPlayer != null) {
                        exoPlayer.setPlayWhenReady(false);
                    }
                    DialogFragment dialogFragment = CategoryFragment.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString("category", catId);
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(getActivity().getSupportFragmentManager(), "category");
                });
            }
        })).start();
    }

    void playerLayoutClick(int videoPosition) {

        if(videoPromotionsItems.size() > videoUrls.size()){
            if(videoUrls.indexOf(videoPromotionsItems.get(videoPosition).getVideoUrl())==-1){
                videoPromotionsItems.remove(videoPosition);
                videoViewPagerAdapter.notifyDataSetChanged();
                removeDeletedVideo(videoPosition);
                return;
            }
        }


        View viewPagerView = videoViewPager.getChildAt(videoPosition);
        playerView = viewPagerView.findViewById(R.id.videoPlayerView);
//        ConstraintLayout playLayout = viewPagerView.findViewById(R.id.playVideoLayout);
//
//        playLayout.setOnClickListener(view1 -> {
//            ImageView videoThumbnail = viewPagerView.findViewById(R.id.videoThumbnail);
//            factory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "RbenoApp"));
//            playLayout.setVisibility(View.INVISIBLE);
//            exoPlayer = new SimpleExoPlayer.Builder(getContext()).build();
//            MediaSource videoSource1 = new ProgressiveMediaSource.Factory(factory)
//                    .createMediaSource(Uri.parse(videoUrls.get(videoPosition)));
//            exoPlayer.prepare(videoSource1);
//            exoPlayer.setPlayWhenReady(true);
//            playerView.setPlayer(exoPlayer);
//            videoThumbnail.setVisibility(View.GONE);
            playerView.setOnClickListener(v -> {

                VideoPagerFragment videoPagerFragment = VideoPagerFragment.newInstance();
                Bundle videoBundle = new Bundle();

                ArrayList<Promotion> videoPromotionItemsInstance = new ArrayList<>(videoPromotionsItems);
                videoPromotionItemsInstance.remove(videoPosition);
                videoPromotionItemsInstance.add(0, videoPromotionsItems.get(videoPosition));

                videoBundle.putSerializable("videoPromotions", videoPromotionItemsInstance);
//                videoBundle.putStringArrayList("videoPromotionsTitles",videoPromotionsItems);
                videoPagerFragment.setArguments(videoBundle);
                exoPlayer.setPlayWhenReady(false);
                videoPagerFragment.show(getChildFragmentManager(), "fullScreen");

//                FullScreenVideoFragment dialogFragment = FullScreenVideoFragment.newInstance();
//                Bundle videoBundle = new Bundle();
//                if (videoUrls.size() > 0) {
//                    videoBundle.putStringArrayList("allVideos", videoUrls);
//                }
//                videoBundle.putString("videoUrl", videoUrls.get(videoPosition));
//                videoBundle.putLong("videoPosition", exoPlayer.getCurrentPosition());
//                dialogFragment.setArguments(videoBundle);
//                exoPlayer.setPlayWhenReady(false);
//                dialogFragment.getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
//                    if (event == Lifecycle.Event.ON_STOP) {
//                        exoPlayer.seekTo(dialogFragment.newExoPlayer.getCurrentPosition());
//                        exoPlayer.setPlayWhenReady(true);
//                        dialogFragment.playerView.setPlayer(null);
//                        dialogFragment.newExoPlayer.release();
//                        dialogFragment.newExoPlayer = null;
//                    }
//                });
//                dialogFragment.show(getChildFragmentManager(), "fullScreen");
            });
        lastSelectedVideo = videoPosition;
//        });
    }

    void removeDeletedVideo(int position){
        if(videoUrls.size()>0){
            Drawable sliderDot = ContextCompat.getDrawable(getContext(), R.drawable.promotion_slider_dots);
            sliderLayout.removeViewAt(position);
            dots.remove(position);
            for (int k = 0; k < videoViewPagerAdapter.getCount(); k++) {
                dots.get(k).setImageDrawable(sliderDot);
            }
            dots.get(videoViewPager.getCurrentItem()).setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.promo_icon_round_red));
        }else{
            videoViewPager.setVisibility(View.GONE);
            sliderLayout.removeAllViews();
        }
    }

    public boolean checkVideoIsDeleted(Promotion promo){
        boolean deleted = false;
        if(videoPromotionsItems.size() > videoUrls.size()){
            Log.d("videoSize","index: "+videoUrls.indexOf(promo.getVideoUrl()));
            if(videoUrls.indexOf(promo.getVideoUrl()) == -1){
                deleted = true;
                int index = videoPromotionsItems.indexOf(promo);
                videoPromotionsItems.remove(promo);
                videoViewPagerAdapter.notifyDataSetChanged();

                if(videoUrls.size()>0){
                    Drawable sliderDot = ContextCompat.getDrawable(getContext(), R.drawable.promotion_slider_dots);
                    sliderLayout.removeViewAt(index);
                    dots.remove(index);
                    for (int k = 0; k < videoViewPagerAdapter.getCount(); k++) {
                        dots.get(k).setImageDrawable(sliderDot);
                    }
                    dots.get(videoViewPager.getCurrentItem()).setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.promo_icon_round_red));
                }else{
                    videoViewPager.setVisibility(View.GONE);
                    sliderLayout.removeAllViews();
                }
            }
        }
        return deleted;
    }
    public void startVerticalVideoFragment(Promotion p){

        VideoPagerFragment videoPagerFragment = VideoPagerFragment.newInstance();
        Bundle videoBundle = new Bundle();
        ArrayList<Promotion> videoPromotionItemsInstance = new ArrayList<>(videoPromotionsItems);
        videoPromotionItemsInstance.remove(p);
        videoPromotionItemsInstance.add(0, p);
        videoBundle.putSerializable("videoPromotions", videoPromotionItemsInstance);
        videoPagerFragment.setArguments(videoBundle);

        videoPagerFragment.show(getChildFragmentManager(), "fullScreen");
    }

    public void deleteVideoFromPromosAdapter(Promotion p){
        if(videoUrls.contains(p.getVideoUrl())) {
            videoUrls.remove(p.getVideoUrl());
            checkVideoIsDeleted(p);
        }
    }

}
