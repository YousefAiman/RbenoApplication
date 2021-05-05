package com.example.yousef.rbenoapplication;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class MyPromotionsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final ArrayList<Promotion> promotions = new ArrayList<>();
    private Query query;

    private RecyclerView rv;
    private NewestPromosAdapter adapter;
    private SwipeRefreshLayout swipe;
    private boolean isLoading;
    private DocumentSnapshot lastResult;
    private int itemsAdded = 0;
    //  private ListenerRegistration listener;
    private PromotionDeleteReceiver promotionDeleteReceiver;
    private TextView noPromosTv;

    public MyPromotionsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        query = FirebaseFirestore.getInstance().collection("promotions")
                .whereEqualTo("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_promos, container, false);

        setupDeletionReceiver();

        swipe = view.findViewById(R.id.swipeRefreshLayout);
        rv = view.findViewById(R.id.allPromosRv);
        noPromosTv = view.findViewById(R.id.noPromosTv);
        noPromosTv.setText("لا تملك اي اعلانات منشورة حاليا!");
        final Toolbar toolbar = view.findViewById(R.id.promosToolbar);
        toolbar.setNavigationOnClickListener(view1 -> getActivity().onBackPressed());

        ((TextView) view.findViewById(R.id.titleTv)).setText("اعلاناتي");

        final AdView adView = view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        swipe.setColorSchemeColors(getResources().getColor(R.color.red));
        swipe.setOnRefreshListener(this);
        swipe.setRefreshing(true);

        rv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.height = (int) (getWidth() * 0.292);
                return true;
            }
        });

        adapter = new NewestPromosAdapter(promotions, getContext(), R.layout.my_promo_item_layout, 2);
//    adapter.setHasStableIds(true);
        rv.setAdapter(adapter);

        getNewUpdatedPromotions();
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!rv.canScrollVertically(1) && dy > 0) {
                    if (!isLoading) {
                        isLoading = true;
                        getNewUpdatedPromotions();
                    }
                }
            }
        });

    }

    @Override
    public void onRefresh() {
        if (WifiUtil.checkWifiConnection(getContext())) {
            promotions.clear();
            adapter.notifyDataSetChanged();
            lastResult = null;
            getNewUpdatedPromotions();
        } else {
            swipe.setRefreshing(false);
        }
    }

    void getNewUpdatedPromotions() {

        swipe.setRefreshing(true);

        isLoading = true;
        itemsAdded = 0;
        int beforeSize = promotions.size();
        Query updatedQuery;
        if (lastResult == null) {
            updatedQuery = query.limit(8);
        } else {
            updatedQuery = query.startAfter(lastResult).limit(8);
        }

        updatedQuery.get().addOnSuccessListener(snapshots -> {

            promotions.addAll(snapshots.toObjects(Promotion.class));
            itemsAdded += snapshots.size();

            if (snapshots.size() > 0) {
                lastResult = snapshots.getDocuments().get(snapshots.size() - 1);
            }

        }).addOnCompleteListener(task -> {

            if (itemsAdded > 0) {
                noPromosTv.setVisibility(View.GONE);
                adapter.notifyItemRangeInserted(beforeSize, itemsAdded);
            }
            isLoading = false;
            swipe.setRefreshing(false);

//      if (listener == null) {
//        listener = query.addSnapshotListener((value, error) -> {
//          if (value != null) {
//            for (DocumentChange documentChange : value.getDocumentChanges()) {
//              if (documentChange.getType() == DocumentChange.Type.REMOVED) {
//                final long removedPromoId = documentChange.getDocument().getLong("promoid");
//                for (Promotion promo : promotions) {
//                  if (promo.getPromoid() == removedPromoId) {
//                    promotions.remove(promo);
////                    adapter.notifyItemRemoved(index);
//                    break;
//                  }
//                }
//              }
//            }
//          }
//        });
//      }
        });
    }

//  @Override
//  public void onDestroy() {
//    super.onDestroy();
//    if (listener != null) listener.remove();
//  }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (promotionDeleteReceiver != null)
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

                    final int index = promos.indexOf(promo);
                    promos.remove(index);
                    adapter.notifyItemRemoved(index);

                    if (promos.size() == 0) {
                        noPromosTv.setVisibility(View.VISIBLE);
                    }

                    break;
                }
            }
        }
    }


}
