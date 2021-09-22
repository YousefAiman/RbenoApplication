package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilteredPromosFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {

    private static final int MINUTE_MILLIS = 60,
            HOUR_MILLIS = 60 * MINUTE_MILLIS,
            DAY_MILLIS = 24 * HOUR_MILLIS,
            INITIAL_SIZE = 8,
            PAGINATION_LIMIT = 6;

    private final ArrayList<Promotion> promotions = new ArrayList<>();
    private SwipeRefreshLayout swipe;
    private TextView noPromosTv;
    private RecyclerView filteredRv;
    private NewestPromosAdapter adapter;
    private Query query;
    private int price, views, itemsAdded = 0;
    private boolean isLoading;
    private DocumentSnapshot lastResult;
    private long time = 0;
    private List<String> allButFirstTen;
    private RecyclerView.OnScrollListener currentScrollListener;
    //  private List<ListenerRegistration> listeners;
    private PromotionDeleteReceiver promotionDeleteReceiver;

    public FilteredPromosFragment() {
        // Required empty public constructor
    }

    static FilteredPromosFragment newInstance() {
        return new FilteredPromosFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();

        for (String key : arguments.keySet()) {
            Log.d("filterLog", key + "-" + arguments.get(key).toString());
        }


        price = arguments.getInt("price");
        views = arguments.getInt("views");

        long endTime = 0;
        switch (arguments.getInt("date")) {
            case 1:
                time = System.currentTimeMillis() - MINUTE_MILLIS;
                break;
            case 2:
                time = (System.currentTimeMillis() / 1000) - HOUR_MILLIS;
                break;
            case 3:
                time = (System.currentTimeMillis() / 1000) - DAY_MILLIS;
                break;
            case 4:
                time = (System.currentTimeMillis() / 1000) - DAY_MILLIS * 2;
                break;
            case 5:
                time = (System.currentTimeMillis() / 1000) - DAY_MILLIS * 7;
                break;
            case 6:
                time = (System.currentTimeMillis() / 1000) - (DAY_MILLIS * 7);
                endTime = (System.currentTimeMillis() / 1000) - (DAY_MILLIS * 14);
                break;

            case 7:
                time = (System.currentTimeMillis() / 1000) - (DAY_MILLIS * 10);
                break;
        }

        query = FirebaseFirestore.getInstance().collection("promotions")
                .whereEqualTo("isBanned", false)
                .whereEqualTo("isPaused", false);

        if (GlobalVariables.getInstance().getCountryCode() != null) {
            query = query.whereEqualTo("country",
                    GlobalVariables.getInstance().getCountryCode().toUpperCase());
        }

        List<String> categories = arguments.getStringArrayList("category");


//    if (!GlobalVariables.getBlockedUsers().isEmpty()) {
//      query = query.orderBy("uid")
//              .whereNotIn("uid", GlobalVariables.getBlockedUsers());
//    }
//
//    if (arguments.containsKey("category")) {
//      query = query.whereIn("type",categories);
//    }


        if (categories != null && !categories.isEmpty() && categories.size() < 9) {

            if (categories.size() == 1) {
                query = query.whereEqualTo("type", categories.get(0));

//      addBlockedUsersFilter();
            } else {
//      categories.size();
                query = query.whereIn("type", categories);
            }

        }
//    else if(time == 0){
//        addBlockedUsersFilter();
//    }


        if (arguments.containsKey("promoType")) {
//      query = query.whereIn("promoType", arguments.getStringArrayList("promoType"));
            query = query.whereEqualTo("promoType", arguments.getString("promoType"));
        }

        if (time != 0) {
            if (endTime == 0) {
                query = query.orderBy("publishtime", Query.Direction.DESCENDING)
                        .whereGreaterThan("publishtime", time);
//                .orderBy("publishtime",Query.Direction.DESCENDING);
            } else {
                query = query.orderBy("publishtime", Query.Direction.DESCENDING)
                        .whereLessThan("publishtime", time)
                        .whereGreaterThan("publishtime", endTime);
//                .orderBy("publishtime",Query.Direction.DESCENDING);
            }
        }
//    else {
        if (price != 0) {

            query = query.orderBy("price"
                    , price == 1 ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);

//        switch (price) {
//          case 1:
//            query = query.orderBy("price", Query.Direction.DESCENDING);
//            break;
//          case 2:
//            query = query.orderBy("price", Query.Direction.ASCENDING);
//            break;
//        }
        } else if (views != 0) {

            query = query.orderBy("viewcount"
                    , views == 1 ? Query.Direction.ASCENDING : Query.Direction.DESCENDING);

//        switch (views) {
//          case 1:
//            query = query.orderBy("viewcount", Query.Direction.ASCENDING);
//            break;
//          case 2:
//            query = query.orderBy("viewcount", Query.Direction.DESCENDING);
//            break;
//        }
//      }
        } else {
            query = query.orderBy("publishtime", Query.Direction.DESCENDING);
        }


        if (arguments.containsKey("rating")) {
            query = query.whereEqualTo("rating", arguments.getDouble("rating"));
        }

    }


    void addBlockedUsersFilter() {
        if (!GlobalVariables.getBlockedUsers().isEmpty()) {

            if (GlobalVariables.getBlockedUsers().size() > 10) {

                allButFirstTen = GlobalVariables.getBlockedUsers().subList(10,
                        GlobalVariables.getBlockedUsers().size());

                query = query.orderBy("uid")
                        .whereNotIn("uid", GlobalVariables.getBlockedUsers().subList(0, 10));

            } else {
                query = query.orderBy("uid")
                        .whereNotIn("uid", GlobalVariables.getBlockedUsers());
            }

        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_promos, container, false);

        noPromosTv = view.findViewById(R.id.noPromosTv);
        filteredRv = view.findViewById(R.id.allPromosRv);
        swipe = view.findViewById(R.id.swipeRefreshLayout);
        ((TextView) view.findViewById(R.id.titleTv)).setText("التصنيف");

        Toolbar toolbar = view.findViewById(R.id.promosToolbar);
        toolbar.setNavigationOnClickListener(view1 -> getActivity().onBackPressed());

        final AdView adView = view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

        setupDeletionReceiver();

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        swipe.setColorSchemeResources(R.color.red);
        swipe.setOnRefreshListener(this);


        final GridLayoutManager glm = new GridLayoutManager(getContext(), 2) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.height = (int) (getWidth() * 0.55);
                return true;
            }
        };


        filteredRv.setLayoutManager(glm);

        adapter = new NewestPromosAdapter(promotions, getContext(),
                R.layout.newest_promo_item_grid, 3);
        adapter.setHasStableIds(true);
        filteredRv.setAdapter(adapter);


        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == 2) {
                    return 2;
                }
                return 1;
            }
        });

//    if (time != 0) {
//      getQuery();
//    } else {
//      listeners = new ArrayList<>();
        getNewUpdatedPromotions();

//    }
    }

    @Override
    public void onRefresh() {
        if (WifiUtil.checkWifiConnection(getContext())) {

            promotions.clear();
            adapter.notifyDataSetChanged();

            if (lastResult != null) {
                lastResult = null;

//        removeAllListeners();
                getNewUpdatedPromotions();

            } else {
                getQuery();
            }
        } else {
            swipe.setRefreshing(false);
        }
    }

//  void removeAllListeners(){
//    if(listeners!=null && !listeners.isEmpty())
//      for(ListenerRegistration listener:listeners)
//        listener.remove();
//  }

    void filterAllBlockedUsers(QuerySnapshot snapshots) {
        final List<DocumentSnapshot> snaps = snapshots.getDocuments();

        for (DocumentSnapshot snap : snaps) {
            if (GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
                continue;
            }
            promotions.add(snap.toObject(Promotion.class));
            itemsAdded++;
        }
    }

    void filterRestOfBlockedUsers(QuerySnapshot snapshots) {
        if (GlobalVariables.getBlockedUsers().size() > 10) {

            final List<DocumentSnapshot> snaps = snapshots.getDocuments();

            for (DocumentSnapshot snap : snaps) {
                if (allButFirstTen.contains(snap.getString("uid"))) {
                    continue;
                }
                promotions.add(snap.toObject(Promotion.class));
                itemsAdded++;
            }

        } else {
            promotions.addAll(snapshots.toObjects(Promotion.class));
            itemsAdded += snapshots.size();
        }

    }

    void getNewUpdatedPromotions() {

        swipe.setRefreshing(true);

        isLoading = true;
        itemsAdded = 0;
        int beforeSize = promotions.size();
        Query updatedQuery;
        if (lastResult == null) {
            updatedQuery = query.limit(INITIAL_SIZE);
        } else {
            updatedQuery = query.startAfter(lastResult).limit(PAGINATION_LIMIT);
        }

        updatedQuery.get().addOnSuccessListener(snapshots -> {

            promotions.addAll(snapshots.toObjects(Promotion.class));
            itemsAdded += snapshots.size();
//      for (QueryDocumentSnapshot snap : snapshots) {
//        promotions.add(snap.toObject(Promotion.class));
//        itemsAdded++;
//      }

//      if(categories!=null && !categories.isEmpty()) {
//        if (categories.size() == 1) {
//          if(time == 0){
//            filterRestOfBlockedUsers(snapshots);
//          }else{
//            filterAllBlockedUsers(snapshots);
//          }
//        } else {
//          filterAllBlockedUsers(snapshots);
//        }
//      }else{
//        if(time == 0){
//          filterRestOfBlockedUsers(snapshots);
//        }else{
//          filterAllBlockedUsers(snapshots);
//        }
//      }

            Log.d("filterQuery", "Query result: " + snapshots.size());

            if (snapshots.size() > 0) {

                lastResult = snapshots.getDocuments().get(snapshots.size() - 1);


            }

        }).addOnCompleteListener(task -> {

            if (itemsAdded > 0) {
                adapter.notifyItemRangeInserted(beforeSize, itemsAdded);
            }


            isLoading = false;
            swipe.setRefreshing(false);

            if (promotions.size() > 0) {

                if (promotions.size() - itemsAdded == 0) {
                    noPromosTv.setVisibility(View.GONE);
                    if (itemsAdded == INITIAL_SIZE) {
                        filteredRv.addOnScrollListener(currentScrollListener = new OnScrollListener());
                    }
                } else if (itemsAdded < PAGINATION_LIMIT) {
                    Log.d("filterQuery", "Removing scroll listener");
                    filteredRv.removeOnScrollListener(currentScrollListener);
                }


            } else {
                noPromosTv.setVisibility(View.VISIBLE);
            }

//      if(task.getResult().size()!=0){
//
////        FirebaseFirestore.getInstance().collection("promotions")
////                .whereEqualTo("deleted",true)
////                .addSnapshotListener(new EventListener<QuerySnapshot>() {
////                  @Override
////                  public void onEvent(@Nullable QuerySnapshot value,
////                                      @Nullable FirebaseFirestoreException error) {
////
////                    if(value==null){
////                      Log.d("filterQuery","no document changes");
////                      return;
////                    }
////                    Log.d("filterQuery","document change size: "+
////                            value.getDocumentChanges().size());
////
////                    for (DocumentChange documentChange : value.getDocumentChanges()) {
////                      Log.d("filterQuery","changed type: "+documentChange.getType());
////                    }
////
////                  }
////                });
//
////        Query newQuery = query.whereEqualTo("deleted",true);
////
////        Log.d("filterQuery","current newQuery: "+newQuery.toString());
////
////
////        newQuery
////                .addSnapshotListener((value, error) -> {
////
////                  if(value==null){
////                    Log.d("filterQuery","no document changes");
////                    return;
////                  }
////
////                  Log.d("filterQuery","document change size: "+
////                          value.getDocumentChanges().size());
////
////                  for (DocumentChange documentChange : value.getDocumentChanges()) {
////                    Log.d("filterQuery","changed type: "+documentChange.getType());
////                  }
////
////                });
////        listeners.add(query.limit(8).addSnapshotListener((value, error) -> {
////
////          Log.d("filterQuery","document change size: "+value.getDocumentChanges().size());
////
////          if (value != null) {
////            for (DocumentChange documentChange : value.getDocumentChanges()) {
////              if (documentChange.getType() == DocumentChange.Type.REMOVED) {
////                long removedPromoId = documentChange.getDocument().getLong("promoid");
////                for (Promotion promo : promotions) {
////                  if (promo.getPromoid() == removedPromoId) {
////                    promotions.remove(promo);
////                    break;
////                  }
////                }
////              }
////            }
////          }
////        }));
//
//      }


        });
    }


    void getQuery() {

        swipe.setRefreshing(true);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {

                noPromosTv.setVisibility(View.GONE);

                promotions.addAll(queryDocumentSnapshots.toObjects(Promotion.class));

                if (price != 0) {
                    switch (price) {
                        case 1:
                            Collections.sort(promotions, (o1, o2) -> Double.compare(o2.getPrice(), o1.getPrice()));
                            break;
                        case 2:
                            Collections.sort(promotions, (o1, o2) -> Double.compare(o1.getPrice(), o2.getPrice()));
                            break;
                    }
                } else {
                    if (views != 0) {
                        switch (views) {
                            case 1:
                                Collections.sort(promotions, (o1, o2) -> Double.compare(o1.getViewcount(), o2.getViewcount()));
                                break;
                            case 2:
                                Collections.sort(promotions, (o1, o2) -> Double.compare(o2.getViewcount(), o1.getViewcount()));
                                break;
                        }
                    }
                }
                adapter.notifyDataSetChanged();

            } else {
                noPromosTv.setVisibility(View.VISIBLE);
            }
            swipe.setRefreshing(false);
        });

    }

    class OnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (!filteredRv.canScrollVertically(1) && dy > 0) {
                if (!isLoading) {
                    isLoading = true;
                    getNewUpdatedPromotions();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//    removeAllListeners();

        getContext().unregisterReceiver(promotionDeleteReceiver);
    }

    void setupDeletionReceiver() {

        promotionDeleteReceiver =
                new PromotionDeleteReceiver() {
                    @Override

                    public void onReceive(Context context, Intent intent) {

                        checkAndDeletePromoFromList(promotions,
                                intent.getLongExtra("promoId", 0));

                    }
                };


        getContext().registerReceiver(promotionDeleteReceiver,
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

}
