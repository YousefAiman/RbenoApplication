package com.example.yousef.rbenoapplication;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;

public class CategoryPromotionsFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {
  int itemsAdded = 0;
  private SwipeRefreshLayout swipeRefreshLayout;
  private RecyclerView lv;
  private Query carsQuery;
  private FirestorePagingAdapter<Promotion, StaggeredViewHolder> firestorePagingAdapter;
  private DocumentSnapshot lastResult = null;
  private ArrayList<Promotion> promotions;
  private StaggeredRecyclerAdapter adapter;
  private boolean isLoading = true;
  private TextView noCarPromosTv;

  public CategoryPromotionsFragment() {
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("fragment", "fragment created");
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_category_promotions, container, false);
    lv = view.findViewById(R.id.carPromosRecyclerView);
    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    noCarPromosTv = view.findViewById(R.id.noPromosTv);

    swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red));
    swipeRefreshLayout.setOnRefreshListener(this);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    carsQuery = FirebaseFirestore.getInstance().collection("promotions")
            .whereIn("type", Arrays.asList(getArguments().getStringArray("category")))
            .whereEqualTo("isBanned", false)
            .orderBy("publishtime", Query.Direction.DESCENDING);

    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
      if (GlobalVariables.getBlockedUsers() != null
              && !GlobalVariables.getBlockedUsers().isEmpty()) {
        initializePagingAdapter();
      } else {
        getPaging();
      }
    } else {
      getPaging();
    }

  }

  @Override
  public void onRefresh() {
    if (firestorePagingAdapter != null) {
      firestorePagingAdapter.refresh();
    } else {
      promotions.clear();
      adapter.notifyDataSetChanged();
      lastResult = null;
      getUpdatedPromotions();
    }
  }

  private void getPaging() {

    PagedList.Config config = new PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(10)
            .build();

    FirestorePagingOptions<Promotion> options = new FirestorePagingOptions.Builder<Promotion>()
            .setLifecycleOwner(this)
            .setQuery(carsQuery, config, snapshot -> snapshot.toObject(Promotion.class)).build();

    firestorePagingAdapter = new FirestorePagingAdapter<Promotion, StaggeredViewHolder>(options) {
      @NonNull
      @Override
      public StaggeredViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StaggeredViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.relativeitem, parent, false));
      }

      @Override
      protected void onError(@NonNull Exception e) {
        super.onError(e);
      }

      @Override
      protected void onBindViewHolder(@NonNull StaggeredViewHolder staggeredViewHolder,
                                      int i, @NonNull Promotion promotion) {
        staggeredViewHolder.bind(promotion, i, getContext());
      }

      @Override
      protected void onLoadingStateChanged(@NonNull LoadingState state) {
        super.onLoadingStateChanged(state);
        switch (state) {
          case LOADING_INITIAL:
          case LOADING_MORE:

            if (firestorePagingAdapter.getCurrentList().size() == 0) {
              noCarPromosTv.setVisibility(View.VISIBLE);
              lv.setVisibility(View.GONE);
            } else {
              noCarPromosTv.setVisibility(View.GONE);
              lv.setVisibility(View.VISIBLE);
            }

            swipeRefreshLayout.setRefreshing(true);
            break;
          case LOADED:
          case FINISHED:
            if (firestorePagingAdapter.getCurrentList().size() == 0) {
              noCarPromosTv.setVisibility(View.VISIBLE);
              lv.setVisibility(View.GONE);
            } else {
              noCarPromosTv.setVisibility(View.GONE);
              lv.setVisibility(View.VISIBLE);
            }
            swipeRefreshLayout.setRefreshing(false);
            break;
          case ERROR:
            swipeRefreshLayout.setRefreshing(false);
            break;

            default:
                break;
        }
      }
    };

    lv.setPadding((int) (25 * GlobalVariables.getDensity()), 0,
            (int) (25 * GlobalVariables.getDensity()), 0);
    firestorePagingAdapter.setHasStableIds(true);
    lv.setAdapter(firestorePagingAdapter);

  }

  private void getUpdatedPromotions() {
    swipeRefreshLayout.setRefreshing(true);
    itemsAdded = 0;
    Query updatedQuery;
    if (lastResult == null) {
      updatedQuery = carsQuery.limit(8);
    } else {
      updatedQuery = carsQuery.startAfter(lastResult).limit(10);
    }

    new Thread(() -> updatedQuery.get().addOnSuccessListener(snapshots -> {
      if (!snapshots.isEmpty()) {
        for (QueryDocumentSnapshot snap : snapshots) {
          if (GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
            continue;
          }
          promotions.add(snap.toObject(Promotion.class));
          itemsAdded++;
        }
        lastResult = snapshots.getDocuments().get(snapshots.size() - 1);
        if (itemsAdded > 0) {
          noCarPromosTv.setVisibility(View.GONE);
          lv.setVisibility(View.VISIBLE);
          lv.post(() -> {
            adapter.notifyItemRangeInserted(promotions.size(), itemsAdded);
            swipeRefreshLayout.setRefreshing(false);
          });
          isLoading = false;
        } else {
          swipeRefreshLayout.setRefreshing(false);
          noCarPromosTv.setVisibility(View.VISIBLE);
          isLoading = false;
          lv.setVisibility(View.GONE);
        }
      } else {
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
      }
    })).start();
  }

  void initializePagingAdapter() {
    promotions = new ArrayList<>();

    adapter = new StaggeredRecyclerAdapter(promotions);
    adapter.setHasStableIds(true);
    lv.setPadding((int) (8 * GlobalVariables.getDensity()), 0,
            (int) (8 * GlobalVariables.getDensity()), (int) (80 * GlobalVariables.getDensity()));
    lv.setAdapter(adapter);
    getUpdatedPromotions();

    lv.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (!lv.canScrollVertically(1) && dy > 0) {
          if (!isLoading) {
            isLoading = true;
            getUpdatedPromotions();
          }
        }
      }
    });

  }
}

