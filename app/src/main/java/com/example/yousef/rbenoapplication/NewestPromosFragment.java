package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class NewestPromosFragment extends androidx.fragment.app.Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        VideosAdapter.VideoViewClickListener {

    private static final int mostViewedQueryLimit = 5;
    private static final int videoQueryLimit = 5;
    //  private static final float scaleFactor = 0.92f;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference promosRef = firestore.collection("promotions");
    SwipeRefreshLayout swipeRefreshLayout;
    NestedScrollView nestedScrollView;
    private RecyclerView videosRv, newestPromosRv1, mostViewedRv;
    private final ArrayList<Promotion> videoPromotionsAllVideo = new ArrayList<>(),
            newestPromos1 = new ArrayList<>(), mostViewedPromos = new ArrayList<>();


    private ListenerRegistration newestListener, videoPagerListener;
    private NewestPromosAdapter newestPromosAdapter1, mostViewedAdapter;
    private VideosAdapter videosAdapter;
    private PromotionDeleteReceiver promotionDeleteReceiver;

    public NewestPromosFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//    setRetainInstance(true);

        Log.d("ttt", "newest on create");
        setupDeletionReceiver();

        videosAdapter = new VideosAdapter(videoPromotionsAllVideo, this);

        for (int i = 0; i < 8; i++) {
            newestPromos1.add(new Promotion());
        }

        newestPromosAdapter1 = new NewestPromosAdapter(newestPromos1,
                getContext(), R.layout.newest_promo_item_grid, 3);

        mostViewedAdapter = new NewestPromosAdapter(mostViewedPromos,
                getContext(), R.layout.most_viewed_item_layout, 0);

    }

//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        Log.d("ttt", "newest onAttach");
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d("ttt", "newest onResume");
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        Log.d("ttt", "newest onDetach");
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_newest_promos, container, false);

        Log.d("ttt", "newest onCreateView");

        videosRv = view.findViewById(R.id.videosRv);
        nestedScrollView = view.findViewById(R.id.scrollview);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        newestPromosRv1 = view.findViewById(R.id.newestPromosRv1);
        mostViewedRv = view.findViewById(R.id.mostViewedRv);


//        mostViewedRv.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
//            @Override
//            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//
//
//                final int action = e.getAction();
//
//                if (action == MotionEvent.ACTION_MOVE) {
//                    Log.d("ttt", "ACTION_MOVE");
//                }
//
//
//
//
//                if(rv.canScrollHorizontally(RecyclerView.FOCUS_FORWARD)){
//                    if(action == MotionEvent.ACTION_MOVE){
//                        rv.getParent().requestDisallowInterceptTouchEvent(true);
//                    }
//
//                    return false;
//                }else{
//
//                    if(action == MotionEvent.ACTION_MOVE){
//                        rv.getParent().requestDisallowInterceptTouchEvent(false);
//                    }
//
////                    rv.removeOnItemTouchListener(this);
//                    return true;
//                }
//
//            }
//
//            @Override
//            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//
//            }
//
//            @Override
//            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//
//            }
//        });


        final AdView adView = view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

        videosRv.setNestedScrollingEnabled(false);
//        mostViewedRv.setNestedScrollingEnabled(true);

        newestPromosRv1.setAdapter(newestPromosAdapter1);
        videosRv.setAdapter(videosAdapter);
        mostViewedRv.setAdapter(mostViewedAdapter);


        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red));
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);

//    nestedScrollView.setNestedScrollingEnabled(false);

        final LinearLayoutManager videosLinearLayoutManager = new LinearLayoutManager(getContext(),
                RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.width = (int) (getHeight() * 0.55);
                return true;
            }
        };

        videosLinearLayoutManager.setSmoothScrollbarEnabled(false);
        videosRv.setLayoutManager(videosLinearLayoutManager);


        newestPromosRv1.setLayoutManager(new GridLayoutManager(getContext(), 2) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.height = (int) (getWidth() * 0.55);
                return true;
            }
        });

        final LinearLayoutManager mostViewedLlm = new LinearLayoutManager(getContext(),
                RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.width = (int) (getWidth() * 0.73);
                return true;
            }
        };
        mostViewedLlm.setSmoothScrollbarEnabled(false);
        mostViewedRv.setLayoutManager(mostViewedLlm);

        newestPromosRv1.setAdapter(newestPromosAdapter1);


        view.findViewById(R.id.allNewestPromosTv).setOnClickListener(v ->
                startAllNewestPromos(1));
        view.findViewById(R.id.allMostViewedPromosTv).setOnClickListener(v ->
                startAllNewestPromos(2));

//        mostViewedRv.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
//            int lastX = 0;
//            @Override
//            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//                switch (e.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        lastX = (int) e.getX();
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        boolean isScrollingRight = e.getX() < lastX;
//                        if ((isScrollingRight && mostViewedLlm.findLastCompletelyVisibleItemPosition() ==
//                                mostViewedAdapter.getItemCount() - 1) ||
//                                (!isScrollingRight && mostViewedLlm.findFirstCompletelyVisibleItemPosition() == 0)) {
//                            parentViewPager.setUserInputEnabled(true);
//                        } else {
//                            parentViewPager.setUserInputEnabled(false);
//                        }
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        lastX = 0;
//                        parentViewPager.setUserInputEnabled(true);
//                        break;
//                }
//                return false;
//            }
//
//            @Override
//            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//            }
//
//            @Override
//            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//
//            }
//        });


        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Log.d("ttt", "newest onViewCreated");

        Log.d("ttt", "country code: " + GlobalVariables.getInstance().getCountryCode());
        getAllVideos();

    }

    @Override
    public void onRefresh() {
        if (WifiUtil.checkWifiConnection(getContext())) {
            if (newestListener != null) {
                newestListener.remove();
                newestListener = null;
            }
            if (videoPagerListener != null) {
                videoPagerListener.remove();
                videoPagerListener = null;
            }

            videoPromotionsAllVideo.clear();
            videosAdapter.notifyDataSetChanged();

            mostViewedPromos.clear();
            mostViewedAdapter.notifyDataSetChanged();

            newestPromos1.clear();

            for (int i = 0; i < 8; i++) {
                newestPromos1.add(new Promotion());
            }

//            mostViewedAdapter.notifyDataSetChanged();

            getAllVideos();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    void getAllVideos() {

        Query query = promosRef
                .orderBy("publishtime", Query.Direction.DESCENDING)
                .whereEqualTo("promoType", Promotion.VIDEO_TYPE)
                .whereEqualTo("isBanned", false)
                .whereEqualTo("isPaused", false)
                .limit(videoQueryLimit);

        if (GlobalVariables.getInstance().getCountryCode() != null) {
            query = query.whereEqualTo("country",
                    GlobalVariables.getInstance().getCountryCode().toUpperCase());
        }

//    if (!GlobalVariables.getBlockedUsers().isEmpty()) {
//
//      if (GlobalVariables.getBlockedUsers().size() > 10) {
//
////        final List<String> firstTenBlockedUsersList = new ArrayList<>();
////
////        for (int i = 0; i < 10; i++) {
////          firstTenBlockedUsersList.add(GlobalVariables.getBlockedUsers().get(i));
////        }
//
//        query = query.orderBy("uid")
//                .whereNotIn("uid",GlobalVariables.getBlockedUsers().subList(0, 10));
//
//      } else {
//        query = query.orderBy("uid")
//                .whereNotIn("uid", GlobalVariables.getBlockedUsers());
//      }
//
//    }
//    query = query.orderBy("publishtime", Query.Direction.DESCENDING).limit(videoQueryLimit);
        query.get().addOnSuccessListener(snapshots -> {
            final List<DocumentSnapshot> documentSnapshots = snapshots.getDocuments();
            if (documentSnapshots.size() > 0) {

                if (GlobalVariables.getBlockedUsers().isEmpty()) {
//                    for(int i=0;i<8;i++){
                    videoPromotionsAllVideo.addAll(snapshots.toObjects(Promotion.class));
//                    }

                } else {

                    final List<DocumentSnapshot> snaps = snapshots.getDocuments();
                    for (DocumentSnapshot snap : snaps) {
                        if (!GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
                            videoPromotionsAllVideo.add(snap.toObject(Promotion.class));
                        }
                    }
                }

            } else {

                videosRv.setVisibility(View.GONE);
                requireView().findViewById(R.id.viewLine1).setVisibility(View.GONE);
            }
        }).addOnCompleteListener(task -> {

            if (videoPromotionsAllVideo.size() > 0) {
                videosRv.setVisibility(View.VISIBLE);
                requireView().findViewById(R.id.viewLine1).setVisibility(View.VISIBLE);
                videosAdapter.notifyDataSetChanged();
            }

            getNewestPromos();
        });
    }

    void getNewestPromos() {

        Query query = promosRef
                .whereEqualTo("isBanned", false)
                .whereEqualTo("isPaused", false);

        Log.d("ttt", "GlobalVariables.getCountryCode(): " + GlobalVariables.getInstance().getCountryCode());

        if (GlobalVariables.getInstance().getCountryCode() != null) {
            query = query.whereEqualTo("country",
                    GlobalVariables.getInstance().getCountryCode());
        }
//    if (!GlobalVariables.getBlockedUsers().isEmpty()) {
//
//      if (GlobalVariables.getBlockedUsers().size() > 10) {
//
//        query = query.orderBy("uid")
//                .whereNotIn("uid", GlobalVariables.getBlockedUsers().subList(0,10));
//
//      } else {
//        query = query.orderBy("uid")
//                .whereNotIn("uid", GlobalVariables.getBlockedUsers());
//      }
//    }

        query = query.orderBy("publishtime", Query.Direction.DESCENDING).limit(8);

//    Query finalQuery = query;
        final AtomicInteger addedCount = new AtomicInteger();
//        final boolean[] wasEmpty = new boolean[1];

        query.get().addOnSuccessListener(snapshots -> {

            if (snapshots.size() > 0) {

//                if(isInitial){

                final List<DocumentSnapshot> snaps = snapshots.getDocuments();

                if (GlobalVariables.getBlockedUsers().isEmpty()) {

                    for (int i = 0; i < snaps.size(); i++) {
                        newestPromos1.set(i, snaps.get(i).toObject(Promotion.class));
                    }
                    addedCount.set(snaps.size());

//                        if(wasEmpty[0] = newestPromos1.isEmpty()){
//
//                            newestPromos1.addAll(snapshots.toObjects(Promotion.class));
//
//                            addedCount.set(snapshots.size());
//
//                        }else{
//
//                            final List<DocumentSnapshot> snaps = snapshots.getDocuments();
//
//                            for (int i = 0; i < snaps.size(); i++) {
//                                newestPromos1.set(i, snaps.get(i).toObject(Promotion.class));
//                            }
//                            addedCount.set(snaps.size());
//                        }

                } else {

                    int index = 0;
                    for (int i = 0; i < snaps.size(); i++) {
                        if (!GlobalVariables.getBlockedUsers().contains(snaps.get(i).getString("uid"))) {

                            newestPromos1.set(index, snaps.get(i).toObject(Promotion.class));

//                                    if(newestPromos1.size() > i){
//                                        newestPromos1.set(index, snaps.get(i).toObject(Promotion.class));
//                                    }else{
//
//                                    }
                            index++;
                        }
                    }
                    addedCount.set(index);

//                        if(wasEmpty[0] = newestPromos1.isEmpty()){
//
//                            newestPromos1.addAll(snapshots.toObjects(Promotion.class));
//
//                            addedCount.set(snapshots.size());
//
//                        }else{
//
//                            int index = 0;
//                            for (int i = 0; i < snaps.size(); i++) {
//                                if (!GlobalVariables.getBlockedUsers().contains(snaps.get(i).getString("uid"))) {
//
//                                    newestPromos1.set(index, snaps.get(i).toObject(Promotion.class));
//
////                                    if(newestPromos1.size() > i){
////                                        newestPromos1.set(index, snaps.get(i).toObject(Promotion.class));
////                                    }else{
////
////                                    }
//                                    index++;
//                                }
//                            }
//                            addedCount.set(index);
//                        }

                }
//                }else{
//
//                    if (GlobalVariables.getBlockedUsers().isEmpty()) {
//                        newestPromos1.addAll(snapshots.toObjects(Promotion.class));
//                        addedCount.set(snapshots.size());
//                    } else {
//
//                        for (DocumentSnapshot snap : snapshots.getDocuments()) {
//                            if (GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
//                                continue;
//                            }
//                            newestPromos1.add(snap.toObject(Promotion.class));
//                            addedCount.getAndIncrement();
//                        }
//                    }
//
//                }


            } else {
                newestPromos1.clear();
//                newestPromosAdapter1.notifyDataSetChanged();
            }
        }).addOnCompleteListener(task -> {

            if (newestPromos1.size() > 0) {

//                if(isInitial){
//                    if(wasEmpty[0]){
//
//                        newestPromosAdapter1.notifyDataSetChanged();
//
//                    }else {

                if (addedCount.get() < 8) {


                    Log.d("ttt", "addedCount.get(): " + addedCount.get());
                    newestPromosAdapter1.notifyItemRangeChanged(0, addedCount.get());

                    final int difference = newestPromos1.size() - addedCount.get();

                    Log.d("ttt", "difference: " + difference);

                    final int originalSize = newestPromos1.size();
                    for (int i = 1; i <= difference; i++) {
                        newestPromos1.remove(originalSize - i);
                    }

                    newestPromosAdapter1.notifyItemRangeRemoved(
                            newestPromos1.size(), difference);

                } else {

                    newestPromosAdapter1.notifyItemRangeChanged(0, newestPromos1.size());

                }
                Log.d("ttt", "not empyu");

//                    }

//                }else if (addedCount.get() > 0) {
//
//                        newestPromosAdapter1.notifyItemRangeInserted(
//                                newestPromos1.size() - addedCount.get(),
//                                addedCount.get());
//
//                }

                if (newestPromosRv1.getVisibility() == View.GONE) {
                    newestPromosRv1.setVisibility(View.VISIBLE);
                    requireView().findViewById(R.id.newestLl).setVisibility(View.VISIBLE);
                    requireView().findViewById(R.id.viewLine2).setVisibility(View.VISIBLE);
                }

//        newestPromosAdapter1.notifyItemRangeChanged(0,8);
//        newestPromosAdapter1.notifyDataSetChanged();
            } else if (newestPromosRv1.getVisibility() == View.VISIBLE) {
                Log.d("ttt", "empty");

                newestPromosRv1.setVisibility(View.GONE);
                requireView().findViewById(R.id.newestLl).setVisibility(View.GONE);
                requireView().findViewById(R.id.viewLine2).setVisibility(View.GONE);

            }

//            if(addedCount.get() == 0){
//
//            }


            getMostViewedPromos();
        });

    }

    void getMostViewedPromos() {

        Query query = promosRef
                .whereEqualTo("isBanned", false)
                .whereEqualTo("isPaused", false)
                .orderBy("viewcount", Query.Direction.DESCENDING)
                .limit(mostViewedQueryLimit);


        if (GlobalVariables.getInstance().getCountryCode() != null) {
            query = query.whereEqualTo("country",
                    GlobalVariables.getInstance().getCountryCode().toUpperCase());
        }

        query.get().addOnSuccessListener(snapshots -> {

            if (snapshots.size() > 0) {

                if (!GlobalVariables.getBlockedUsers().isEmpty()) {
                    for (DocumentSnapshot snap : snapshots.getDocuments()) {
                        if (!GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
                            mostViewedPromos.add(snap.toObject(Promotion.class));
                        }
                    }
                } else {
                    mostViewedPromos.addAll(snapshots.toObjects(Promotion.class));
                }

            }
        }).addOnCompleteListener(task -> {

            if (mostViewedPromos.size() > 0) {
                mostViewedRv.setVisibility(View.VISIBLE);
                mostViewedAdapter.notifyDataSetChanged();

                requireView().findViewById(R.id.mostViewedLl).setVisibility(View.VISIBLE);

            } else {
                mostViewedRv.setVisibility(View.GONE);
                requireView().findViewById(R.id.mostViewedLl).setVisibility(View.GONE);
            }

            if (videoPromotionsAllVideo.isEmpty() && newestPromos1.isEmpty() &&
                    mostViewedPromos.isEmpty()) {
                requireView().findViewById(R.id.noPromosTv).setVisibility(View.VISIBLE);
            } else {
                requireView().findViewById(R.id.noPromosTv).setVisibility(View.GONE);
            }

            swipeRefreshLayout.setRefreshing(false);
        });


    }

    @Override
    public void videoViewClickListener(int position) {

        if (videoPromotionsAllVideo.get(position).getPromoid() == -1) {
            videosAdapter.notifyItemChanged(position);
//      Toast.makeText(getContext(), R.string.promo_removed, Toast.LENGTH_SHORT).show();
            return;
        }

        if (GlobalVariables.getVideoViewedCount() != 0 &&
                GlobalVariables.getVideoViewedCount() % 2 == 0) {
            InterstitialAdUtil.showAd(getContext());
        }


        final ArrayList<Promotion> videoPromotionItemsInstance
                = new ArrayList<>(videoPromotionsAllVideo);

        videoPromotionItemsInstance.remove(position);
        videoPromotionItemsInstance.add(0, videoPromotionsAllVideo.get(position));

        final VideoPagerFragment videoPagerFragment =
                new VideoPagerFragment(videoPromotionItemsInstance);
        Log.d("ttt", "videoPromotionsAllVideo size: " + videoPromotionsAllVideo.size());


        ((HomeActivity) Objects.requireNonNull(getActivity()))
                .lockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ((HomeActivity) getActivity()).addFragmentToHomeContainer(videoPagerFragment);

    }

    void startAllNewestPromos(int type) {

        AllPromosFragment dialogFragment = new AllPromosFragment();
        Bundle b = new Bundle();
        b.putInt("type", type);
        dialogFragment.setArguments(b);

        ((HomeActivity) requireActivity()).addFragmentToHomeContainer(dialogFragment);

//    dialogFragment.show(getChildFragmentManager(), "AllPromosFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireContext().unregisterReceiver(promotionDeleteReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (newestListener != null) newestListener.remove();
        if (videoPagerListener != null) videoPagerListener.remove();

    }

    void getAddedPromo(long promoId) {

        promosRef.whereEqualTo("promoid", promoId)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                newestPromos1.add(0, task.getResult().getDocuments().get(0).toObject(Promotion.class));
                newestPromosAdapter1.notifyItemInserted(0);
                if (newestPromos1.size() == 9) {
                    newestPromos1.remove(8);
                    newestPromosAdapter1.notifyItemRemoved(8);
                }
            }
        });

    }

    void setupDeletionReceiver() {

        promotionDeleteReceiver =
                new PromotionDeleteReceiver() {
                    @Override

                    public void onReceive(Context context, Intent intent) {

                        final long id = intent.getLongExtra("promoId", 0);
                        final String changeType = intent.getStringExtra("changeType");

                        Promotion.changePromoStatusFromList(newestPromos1, id, changeType,
                                newestPromosAdapter1);

                        Promotion.changePromoStatusFromList(videoPromotionsAllVideo, id, changeType,
                                videosAdapter);

                        Promotion.changePromoStatusFromList(mostViewedPromos, id, changeType,
                                mostViewedAdapter);


                        if (videoPromotionsAllVideo.isEmpty() && newestPromos1.isEmpty() &&
                                mostViewedPromos.isEmpty()) {
                            requireView().findViewById(R.id.noPromosTv).setVisibility(View.VISIBLE);
                        } else {
                            requireView().findViewById(R.id.noPromosTv).setVisibility(View.GONE);
                        }


                        if (videoPromotionsAllVideo.isEmpty()) {
                            videosRv.setVisibility(View.GONE);
                            requireView().findViewById(R.id.viewLine1).setVisibility(View.GONE);
                        }

                        if (newestPromos1.isEmpty()) {
                            newestPromosRv1.setVisibility(View.GONE);
                            requireView().findViewById(R.id.newestLl).setVisibility(View.GONE);
                            requireView().findViewById(R.id.viewLine2).setVisibility(View.GONE);
                        }

                        if (mostViewedPromos.isEmpty()) {
                            mostViewedRv.setVisibility(View.GONE);
                            requireView().findViewById(R.id.mostViewedLl).setVisibility(View.GONE);
                        }
//
//                checkAndDeletePromoFromList(newestPromos1,id);
//
//                checkAndDeletePromoFromList(videoPromotionsAllVideo,id);
//
//                checkAndDeletePromoFromList(mostViewedPromos,id);
                    }
                };


        requireContext().registerReceiver(promotionDeleteReceiver,
                new IntentFilter(BuildConfig.APPLICATION_ID + ".promoDelete"));

    }

//  void checkAndDeletePromoFromList(ArrayList<Promotion> promos,long id){
//    if(promos!=null && !promos.isEmpty()) {
//      for (Promotion promo:promos) {
//        if(promo.getPromoid() == id){
//          promo.setIsBanned(true);
//          break;
//        }
//      }
//    }
//  }

}
