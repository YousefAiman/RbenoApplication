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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AllPromosFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener {

    private static final int PAGINATION_LIMIT = 6,
            INITIAL_SIZE = 8;


    private RecyclerView allPromosRv;
    private ArrayList<Promotion> promotions;
    private Query query;
    private boolean isLoading;
    private DocumentSnapshot lastResult = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int itemsAdded, type;
    private NewestPromosAdapter newestpromosadapter;
    private String category;
    //  private List<String> allButFirstTen;
    private PromotionDeleteReceiver promotionDeleteReceiver;
    private RecyclerView.OnScrollListener currentScrollListener;

    public AllPromosFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        type = getArguments().getInt("type", 0);

        query = FirebaseFirestore.getInstance()
                .collection("promotions")
                .whereEqualTo("isBanned", false)
                .whereEqualTo("isPaused", false).limit(PAGINATION_LIMIT);

        if (GlobalVariables.getInstance().getCountryCode() != null) {
            query = query.whereEqualTo("country", GlobalVariables.getInstance().getCountryCode().toUpperCase());
        }

        if (getArguments().containsKey("category")) {
            category = getArguments().getString("category");
            query = query.whereEqualTo("type", category);
        }
//    if (!GlobalVariables.getBlockedUsers().isEmpty()) {
//      if (GlobalVariables.getBlockedUsers().size() > 10) {
//
//        allButFirstTen = GlobalVariables.getBlockedUsers().subList(10,
//                GlobalVariables.getBlockedUsers().size());
//
//        query = query.orderBy("uid")
//                .whereNotIn("uid",
//                        GlobalVariables.getBlockedUsers().subList(0, 10));
//
//      } else {
//        query = query.orderBy("uid")
//                .whereNotIn("uid", GlobalVariables.getBlockedUsers());
//      }
//    }
        if (type == 1) {
            query = query.orderBy("publishtime", Query.Direction.DESCENDING);
        } else if (type == 2) {
            query = query.orderBy("viewcount", Query.Direction.DESCENDING);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_promos, container, false);

        setupDeletionReceiver();

        allPromosRv = view.findViewById(R.id.allPromosRv);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        Toolbar toolbar = view.findViewById(R.id.promosToolbar);
        toolbar.setNavigationOnClickListener(view1 -> getActivity().onBackPressed());
        if (type == 1) {
            ((TextView) view.findViewById(R.id.titleTv)).setText("المضاف حديثا");
        } else if (type == 2) {
            ((TextView) view.findViewById(R.id.titleTv)).setText("الأكثر زياراة");
        } else {
            ((TextView) view.findViewById(R.id.titleTv)).setText(category);
        }

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

        swipeRefreshLayout.setColorSchemeResources(R.color.red);
        swipeRefreshLayout.setOnRefreshListener(this);

        promotions = new ArrayList<>();

        final GridLayoutManager glm = new GridLayoutManager(getContext(), 2) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.height = (int) (getWidth() * 0.55);
                return true;
            }
        };

        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (allPromosRv.getAdapter().getItemViewType(position) == 2) {
                    return 2;
                }
                return 1;
            }
        });

        allPromosRv.setLayoutManager(glm);

        newestpromosadapter = new NewestPromosAdapter(promotions, getContext(), R.layout.newest_promo_item_grid, 3);
        newestpromosadapter.setHasStableIds(true);
        allPromosRv.setAdapter(newestpromosadapter);

        getUpdatedPromotions();


    }

    void getUpdatedPromotions() {

        isLoading = true;

        swipeRefreshLayout.setRefreshing(true);
        itemsAdded = 0;
//    final int beforeSize = promotions.size();

        Query updatedQuery = query;

        if (lastResult != null) {
            updatedQuery = updatedQuery.startAfter(lastResult);
        }

        updatedQuery.get().addOnSuccessListener(snapshots -> {

            if (GlobalVariables.getBlockedUsers().isEmpty()) {
                promotions.addAll(snapshots.toObjects(Promotion.class));
                itemsAdded += snapshots.size();
            } else {
                final List<DocumentSnapshot> snaps = snapshots.getDocuments();

                for (DocumentSnapshot snap : snaps) {
                    if (GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
                        continue;
                    }
                    promotions.add(snap.toObject(Promotion.class));
                    itemsAdded++;
                }
            }

            if (snapshots.size() > 0) {
                lastResult = snapshots.getDocuments().get(snapshots.size() - 1);
            }
        }).addOnCompleteListener(task -> {


            if (itemsAdded > 0) {
                newestpromosadapter.notifyItemRangeInserted(
                        promotions.size() - itemsAdded, itemsAdded);
            }


            if (promotions.size() > 0) {

                if (promotions.size() - itemsAdded == 0 && currentScrollListener == null) {

                    if (itemsAdded == INITIAL_SIZE) {
                        allPromosRv.addOnScrollListener(currentScrollListener = new OnScrollListener());
                    }
                } else if (itemsAdded < PAGINATION_LIMIT) {
                    allPromosRv.removeOnScrollListener(currentScrollListener);
                }

            }


            isLoading = false;
            swipeRefreshLayout.setRefreshing(false);

//      query.addSnapshotListener((value, error) -> {
//        if (value != null) {
//          for (DocumentChange documentChange : value.getDocumentChanges()) {
//            if (documentChange.getType() == DocumentChange.Type.REMOVED) {
//              long removedPromoId = documentChange.getDocument().getLong("promoid");
//              for (Promotion promo : promotions) {
//                if (promo.getPromoid() == removedPromoId) {
//                  promotions.remove(promo);
//                  break;
//                }
//              }
//            }
//          }
//        }
//      });
        });
    }

    @Override
    public void onRefresh() {
        if (WifiUtil.checkWifiConnection(getContext())) {
            promotions.clear();
            newestpromosadapter.notifyDataSetChanged();
            lastResult = null;
            getUpdatedPromotions();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

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
                    promo.setIsBanned(true);
                    break;
                }
            }
        }
    }

    class OnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (!allPromosRv.canScrollVertically(1) && dy > 0) {
                if (!isLoading) {
                    getUpdatedPromotions();
                }
            }
        }
    }

}