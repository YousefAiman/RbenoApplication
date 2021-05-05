package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchFragment extends Fragment implements RecentSearchAdapter.RecentSearchClickListener {

  private final ArrayList<Promotion> searchedPromotions = new ArrayList<>();
  private final
  CollectionReference newestpromosRef = FirebaseFirestore.getInstance().collection("promotions"),
          userRef = FirebaseFirestore.getInstance().collection("users"),
          searchRef = FirebaseFirestore.getInstance().collection("searches");
  private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
  private SearchView searchView;
  private RecyclerView searchRv, recentSearchesRv;
  private NewestPromosAdapter adapter;
  private TextView searchTv, recentSearchTv;
  private ImageView backImg;
  private String userDocumentID;
  private List<String> recentSearches;
  private RecentSearchAdapter recentSearchAdapter;
  private PromotionDeleteReceiver promotionDeleteReceiver;

  public SearchFragment() {
  }

  static SearchFragment newInstance() {
    return new SearchFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    if (getArguments() != null) {
      userDocumentID = getArguments().getString("userdocument");

    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_search, container, false);

    setupDeletionReceiver();

    searchView = view.findViewById(R.id.searchView);
    searchView.onActionViewExpanded();
    searchRv = view.findViewById(R.id.searchRecyclerView);
    searchTv = view.findViewById(R.id.searchTv);
    backImg = view.findViewById(R.id.backImg);
    recentSearchesRv = view.findViewById(R.id.recentSearchesRv);
    recentSearchTv = view.findViewById(R.id.recentSearchTv);

    final GridLayoutManager glm = new GridLayoutManager(getContext(), 2) {
      @Override
      public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        lp.height = (int) (getWidth() * 0.55);
        return true;
      }
    };

    searchRv.setLayoutManager(glm);


    if (userDocumentID != null) {
      getRecentSearches(userDocumentID);
    }

    adapter = new NewestPromosAdapter(searchedPromotions,
            getContext(), R.layout.newest_promo_item_grid, 3);

//    adapter.setHasStableIds(true);

    glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override
      public int getSpanSize(int position) {
        if (adapter.getItemViewType(position) == 2) {
          return 2;
        }
        return 1;
      }
    });

    searchRv.setAdapter(adapter);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    backImg.setOnClickListener(v -> getActivity().onBackPressed());

    ((TextView) searchView.findViewById(R.id.search_src_text))
            .setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});

    searchView.setOnFocusChangeListener((v, hasFocus) -> {
      if (hasFocus) {
        Log.d("ttt", "searchview has focus");
        searchRv.setVisibility(View.INVISIBLE);
        recentSearchTv.setVisibility(View.VISIBLE);
        searchTv.setVisibility(View.GONE);
        recentSearchesRv.setVisibility(View.VISIBLE);
      } else {
        Log.d("ttt", "search view doesn't have focus");
        searchTv.setVisibility(View.VISIBLE);

        recentSearchesRv.setVisibility(View.INVISIBLE);
        recentSearchTv.setVisibility(View.GONE);
      }
    });

    searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
      if (hasFocus) {
        Log.d("ttt", "text has focus");
        searchRv.setVisibility(View.INVISIBLE);
        recentSearchTv.setVisibility(View.VISIBLE);
        recentSearchesRv.setVisibility(View.VISIBLE);
        searchTv.setVisibility(View.GONE);
      } else {
        Log.d("ttt", "text doesn't have focus");
        if (searchRv.getVisibility() == View.VISIBLE) {
          recentSearchesRv.setVisibility(View.INVISIBLE);
          recentSearchTv.setVisibility(View.GONE);
        }
      }
    });

    searchView.setOnClickListener(v -> {
      //searchView.onActionViewExpanded();
      searchRv.setVisibility(View.INVISIBLE);
      recentSearchTv.setVisibility(View.VISIBLE);
      recentSearchesRv.setVisibility(View.VISIBLE);
      searchTv.setVisibility(View.GONE);
      Log.d("ttt", "clicked searchview");
    });

    searchView.setOnSearchClickListener(v -> {
      searchRv.setVisibility(View.INVISIBLE);
      recentSearchTv.setVisibility(View.VISIBLE);
      recentSearchesRv.setVisibility(View.VISIBLE);
      searchTv.setVisibility(View.INVISIBLE);
      Log.d("ttt", "clicked on search in searchview");
    });

    searchView.setOnCloseListener(() -> {
      searchRv.setVisibility(View.INVISIBLE);
      recentSearchTv.setVisibility(View.VISIBLE);
      recentSearchesRv.setVisibility(View.VISIBLE);
      searchTv.setVisibility(View.INVISIBLE);
      Log.d("ttt", "clicked on close in searchview");
      return false;
    });

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        if (WifiUtil.checkWifiConnection(getContext())) {
          searchPromos(query);
        }
        searchView.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        Log.d("ttt", "onQueryTextChange");
        if (recentSearchesRv.getVisibility() == View.INVISIBLE) {
          searchRv.setVisibility(View.INVISIBLE);
          searchTv.setVisibility(View.INVISIBLE);
          recentSearchesRv.setVisibility(View.VISIBLE);
          recentSearchTv.setVisibility(View.VISIBLE);
        }

        if (recentSearchAdapter != null) {
          recentSearchAdapter.getFilter().filter(newText);
        }
        return true;
      }
    });
  }

  private void searchPromos(String searchString) {
//    searchView.clearFocus();
//    recentSearchesRv.setVisibility(View.INVISIBLE);
    recentSearchTv.setVisibility(View.GONE);

    Query searchQuery = newestpromosRef.orderBy("publishtime", Query.Direction.DESCENDING)
            .whereEqualTo("isBanned", false)
            .whereEqualTo("isPaused", false);

    if (GlobalVariables.getInstance().getCountryCode() != null) {
      searchQuery = searchQuery
              .whereEqualTo("country", GlobalVariables.getInstance().getCountryCode().toUpperCase());
    }

    final String[] splitArr = searchString.split(" ");


    if (splitArr.length == 0) {

      searchQuery = newestpromosRef.whereArrayContains("keyWords", searchString);

    } else if (splitArr.length <= 10) {

      searchQuery = searchQuery.whereArrayContainsAny("keyWords",
              Arrays.asList(splitArr));

    } else if (splitArr.length > 10) {

      searchQuery = searchQuery.whereArrayContainsAny("keyWords",
              Arrays.asList(splitArr).subList(0, 10));
//              .whereArrayContainsAny("keyWords",split.subList(10,split.size()));

    }

    final AtomicInteger itemsAdded = new AtomicInteger();
    searchQuery.get().addOnSuccessListener(snapshots -> {

//      Map<String, Integer> map = new HashMap<>();
//      for (DocumentSnapshot ds : snapshots) {
//        Promotion promotion = ds.toObject(Promotion.class);
//
//        if (map.containsKey(promotion.getType())) {
//          map.put(promotion.getType(), map.get(promotion.getType()) + 1);
//        } else {
//          map.put(promotion.getType(), 1);
//        }
//        searchedPromotions.add(promotion);
////              adapter.notifyItemInserted(searchedPromotions.size());
//      }
//      Objects.requireNonNull(getActivity()).runOnUiThread(() -> adapter.notifyDataSetChanged());
      recentSearchTv.setVisibility(View.GONE);
      recentSearchesRv.setVisibility(View.INVISIBLE);

      if (snapshots.isEmpty()) {

        searchRv.setVisibility(View.INVISIBLE);
        searchTv.setVisibility(View.VISIBLE);

      } else {

        searchedPromotions.clear();

        if (GlobalVariables.getBlockedUsers().isEmpty()) {

          searchedPromotions.addAll(snapshots.toObjects(Promotion.class));
          itemsAdded.addAndGet(snapshots.size());
        } else {

          for (DocumentSnapshot snap : snapshots.getDocuments()) {
            if (!GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
              searchedPromotions.add(snap.toObject(Promotion.class));
              itemsAdded.getAndIncrement();
            }
          }
        }

        searchRv.setVisibility(View.VISIBLE);
        searchTv.setVisibility(View.INVISIBLE);
      }

      if (currentUser.isAnonymous()) {
        createRecentSearchList();
      }

      if (recentSearches.contains(searchString)) {
        final int index = recentSearches.indexOf(searchString);
//          Collections.swap(recentSearches,index,0);
        recentSearches.remove(searchString);
//          adapter.notifyItemMoved(index, 0);
        recentSearchAdapter.notifyItemRemoved(index);
      }

      recentSearches.add(0, searchString);
      recentSearchAdapter.notifyItemInserted(0);


      if (recentSearches.size() == 15) {
        recentSearches.remove(14);
        recentSearchAdapter.notifyItemRemoved(14);
      }

    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//        if(itemsAdded.get() > 0){
//          adapter.notifyItemRangeInserted(0, itemsAdded.get());
//        }

        adapter.notifyDataSetChanged();

      }
    });
  }

  @Override
  public void onPause() {
    if (userDocumentID != null && !userDocumentID.isEmpty()) {
      userRef.document(userDocumentID).update("searchHistory", recentSearches);
    }
    super.onPause();
  }

  void createRecentSearchList() {

    if (recentSearches == null) {
      recentSearches = new ArrayList<>();

      recentSearchAdapter = new RecentSearchAdapter(getContext(), recentSearches,
              this, this);

      recentSearchesRv.setAdapter(recentSearchAdapter);

      recentSearchTv.setVisibility(View.VISIBLE);
    }
  }
//  @Override
//  public void onDismiss(@NonNull DialogInterface dialog) {
//    if(userDocumentID!=null)
//    userRef.document(userDocumentID).update("searchHistory",recentSearches);
//    super.onDismiss(dialog);
//  }

  private void getRecentSearches(String userDocumentID) {
    userRef.document(userDocumentID).get().addOnSuccessListener(documentSnapshot -> {
//      if (!documentSnapshot.contains("searchHistory")) {
//        documentSnapshot.getReference().update("searchHistory",  new ArrayList<>());
//      } else {
//        recentSearches = (List<String>) documentSnapshot.get("searchHistory");
//        if (!recentSearches.isEmpty()) {
//          recentSearchAdapter = new RecentSearchAdapter(getContext(), recentSearches, this,this);
//          recentSearchAdapter.setHasStableIds(true);
//          recentSearchesRv.setAdapter(recentSearchAdapter);
//        }
//      }
      if (documentSnapshot.contains("searchHistory")) {


        recentSearches = new ArrayList<>();

        recentSearches.addAll((List<String>) documentSnapshot.get("searchHistory"));

        if (recentSearches.size() > 0) {
          recentSearchTv.setVisibility(View.VISIBLE);
        }

        recentSearchAdapter = new RecentSearchAdapter(getContext(), recentSearches,
                this, this);

        recentSearchesRv.setAdapter(recentSearchAdapter);

      }

    });
  }

  void getUpdatedRecentSearches() {


    userRef.document(userDocumentID).get().addOnSuccessListener(documentSnapshot -> {
      List<String> newRecentSearches = (List<String>) documentSnapshot.get("searchHistory");
      if (recentSearchAdapter == null) {
        if (recentSearches == null) recentSearches = new ArrayList<>();
        recentSearchAdapter = new RecentSearchAdapter(getContext(), recentSearches,
                this, this);
//        recentSearchAdapter.setHasStableIds(true);
        recentSearchesRv.setAdapter(recentSearchAdapter);
      } else {
        if (newRecentSearches.size() > recentSearches.size()) {
          recentSearches.add(newRecentSearches.get(newRecentSearches.size() - 1));
          recentSearchAdapter.notifyItemInserted(recentSearches.size());
        }
      }
    });
  }

  @Override
  public void setOnRecentSearchClickListener(String search) {
    searchView.setQuery(search, true);
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

                checkAndDeletePromoFromList(searchedPromotions,
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

