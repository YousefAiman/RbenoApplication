package com.example.yousef.rbenoapplication;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class FavouriteFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

  private static final int FAV_PAGE = 6;
  private RecyclerView favRv;
  private TextView noPromoTv;
  private ImageView noPromosIv;
  private PromotionDeleteReceiver promotionDeleteReceiver;
  private ArrayList<Promotion> promotions;
  private final CollectionReference promoRef =
          FirebaseFirestore.getInstance().collection("promotions");
  private boolean isLoading;
  private RecyclerView.OnScrollListener scrollListener;
  private NewestPromosAdapter adapter;
  private SwipeRefreshLayout swipeRefreshLayout;

  public FavouriteFragment() {
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_favourite, container, false);

    favRv = view.findViewById(R.id.favPromoRecyclerView);
    noPromoTv = view.findViewById(R.id.noPromosTv);
    noPromosIv = view.findViewById(R.id.noPromosIv);
    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

    setupDeletionReceiver();

    ((Toolbar) view.findViewById(R.id.promotiontoolbar)).setOnMenuItemClickListener(item -> {
      getActivity().onBackPressed();
      return true;
    });


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
        if (favRv.getAdapter().getItemViewType(position) == 2) {
          return 2;
        }
        return 1;
      }
    });

    favRv.setLayoutManager(glm);


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

//    CollectionReference usersRef = firestore.collection("users");

    Log.d("ttt", "fav global: " + GlobalVariables.getFavPromosIds().size());

    swipeRefreshLayout.setColorSchemeResources(R.color.red);
    swipeRefreshLayout.setOnRefreshListener(this);

    if (GlobalVariables.getFavPromosIds().size() == 0) {
      noPromoTv.setVisibility(View.VISIBLE);
      noPromosIv.setVisibility(View.VISIBLE);
      favRv.setVisibility(View.INVISIBLE);
    } else {


      promotions = new ArrayList<>();

      adapter = new NewestPromosAdapter(promotions,
              getContext(), R.layout.newest_promo_item_grid, 1);

      favRv.setAdapter(adapter);

      adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
          if (adapter.getItemCount() == 0) {
            noPromoTv.setVisibility(View.VISIBLE);
            noPromosIv.setVisibility(View.VISIBLE);
            favRv.setVisibility(View.GONE);
          }
        }
      });
      //        promoRef.whereIn("promoid", GlobalVariables.getFavPromosIds()).
      //                get().addOnSuccessListener(snapshots1 ->
      //                promotions.addAll(snapshots1.toObjects(Promotion.class)))
      //                .addOnCompleteListener(task -> {
      //          if (promotions.size() > 0) {
      //            adapter.notifyDataSetChanged();
      //          }
      //        });
      //        Query query = promoRef.whereIn("promoid",
      //                GlobalVariables.getFavPromosIds().subList(0,10));
      //
      //        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      //          @Override
      //          public void onSuccess(QuerySnapshot snapshots) {
      //
      //          }
      //        });
      //
      //        List<Long> favIds = GlobalVariables.getFavPromosIds();
      //        for (Long id : favIds) {
      //          promoRef.whereEqualTo("promoid", id).get().addOnSuccessListener(snapshots -> {
      //            if (!snapshots.isEmpty()) {
      //              promotions.add(snapshots.getDocuments().get(0).toObject(Promotion.class));
      //              adapter.notifyItemInserted(promotions.size() - 1);
      //            }
      //          });
      //        }
      //
      getNextFavGroup(0, Math.min(GlobalVariables.getFavPromosIds().size(), FAV_PAGE));
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
                checkAndDeletePromoFromList(intent.getLongExtra("promoId", 0));
              }
            };

    getContext().registerReceiver(promotionDeleteReceiver,
            new IntentFilter(BuildConfig.APPLICATION_ID + ".promoDelete"));

  }


  void getNextFavGroup(int startAt, int endAt) {

    swipeRefreshLayout.setRefreshing(true);
    final int previousSize = promotions.size();

    promoRef.whereIn("promoid", GlobalVariables.getFavPromosIds().subList(startAt, endAt))
            .whereEqualTo("isBanned", false)
            .whereEqualTo("isPaused", false)
            .orderBy("publishtime", Query.Direction.DESCENDING)
            .get().addOnSuccessListener(snapshots -> {

      Log.d("ttt", "snapshots size: " + snapshots.size());

      promotions.addAll(snapshots.toObjects(Promotion.class));

    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {

        if (startAt == 0) {
          adapter.notifyDataSetChanged();
//          adapter.notifyItemRangeInserted(0, promotions.size());
        } else {
          adapter.notifyItemRangeInserted(previousSize, promotions.size() - previousSize);
        }

        if (startAt == 0 && GlobalVariables.getFavPromosIds().size() > FAV_PAGE &&
                scrollListener == null) {

          Log.d("ttt", "adding scroll listener to fav");
          favRv.addOnScrollListener(scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
              super.onScrolled(recyclerView, dx, dy);
              Log.d("ttt", "Scrollled in fav");
              if (!favRv.canScrollVertically(1) && dy > 0) {
                Log.d("ttt", "Scrollled to bottom");
                if (!isLoading) {
                  isLoading = true;
                  Log.d("ttt", "isLoading false");
                  if (GlobalVariables.getFavPromosIds().size() >= promotions.size() + FAV_PAGE) {
                    Log.d("ttt", "10 or more reamin geetting them");
                    getNextFavGroup(promotions.size(), promotions.size() + FAV_PAGE);
                  } else {
                    Log.d("ttt", "reoving since less than 10 remainidnh");
                    favRv.removeOnScrollListener(this);
                    scrollListener = null;
                    if (GlobalVariables.getFavPromosIds().size() > promotions.size()) {
                      Log.d("ttt", "getting last remaining");
                      getNextFavGroup(promotions.size(), GlobalVariables.getFavPromosIds().size());

                    }

                  }

                }
              }
            }
          });

        }
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
      }
    });

  }

  void checkAndDeletePromoFromList(long id) {
    if (promotions != null && !promotions.isEmpty()) {
      for (Promotion promo : promotions) {
        if (promo.getPromoid() == id) {
          promo.setIsBanned(true);
          break;
        }
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d("ttt", "fav frag destroyed");

    if (scrollListener != null) {
      favRv.removeOnScrollListener(scrollListener);
    }

  }

  @Override
  public void onRefresh() {

    if (WifiUtil.checkWifiConnection(getContext())) {
      promotions.clear();
      adapter.notifyDataSetChanged();
      getNextFavGroup(0, Math.min(GlobalVariables.getFavPromosIds().size(), FAV_PAGE));

    } else {
      swipeRefreshLayout.setRefreshing(false);
    }

  }
}
