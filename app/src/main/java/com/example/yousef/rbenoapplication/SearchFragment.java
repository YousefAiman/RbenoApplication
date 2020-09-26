package com.example.yousef.rbenoapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SearchFragment extends DialogFragment {

    private SearchView searchView;
    private RecyclerView searchRv;
    private ArrayList<Promotion> searchedPromotions = new ArrayList<>();
    private newestpromosadapter adapter;
    private CollectionReference newestpromosRef;
    private TextView searchTv;
    private ImageView backImg;
    private String userDocumentID;
    private CollectionReference userRef;
    private RecyclerView recentSearchesRv;
    private List<String> recentSearches;
    private RecentSearchAdapter recentSearchAdapter;
    private TextView recentSearchTv;
    public SearchFragment() {
    }

    static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        searchView = view.findViewById(R.id.searchView);
        searchView.onActionViewExpanded();
        ImageView searchClose = searchView.findViewById(R.id.search_close_btn);
        searchClose.setImageResource(R.drawable.close_icon);
        searchRv = view.findViewById(R.id.searchRecyclerView);
        searchTv = view.findViewById(R.id.searchTv);
        backImg = view.findViewById(R.id.backImg);
        recentSearchesRv = view.findViewById(R.id.recentSearchesRv);
        recentSearchTv = view.findViewById(R.id.recentSearchTv);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        userRef = firestore.collection("users");
        newestpromosRef = firestore.collection("promotions");
        backImg.setOnClickListener(v -> dismiss());

        adapter = new newestpromosadapter(searchedPromotions, getContext());
        adapter.setHasStableIds(true);
        searchRv.setLayoutManager(new LinearLayoutManager(getContext()));
        searchRv.setAdapter(adapter);

        if (getArguments() != null) {
            userDocumentID = getArguments().getString("userdocument");
            getRecentSearches(userDocumentID);
        }

        searchView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d("ttt", "searchview has focus");
                searchRv.setVisibility(View.GONE);
                recentSearchTv.setVisibility(View.VISIBLE);
                searchTv.setVisibility(View.GONE);
                recentSearchesRv.setVisibility(View.VISIBLE);

            } else {
                Log.d("ttt", "search view doesn't have focus");
                searchTv.setVisibility(View.VISIBLE);
                recentSearchesRv.setVisibility(View.GONE);
                recentSearchTv.setVisibility(View.GONE);
            }
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d("ttt", "text has focus");
                searchRv.setVisibility(View.GONE);
                recentSearchTv.setVisibility(View.VISIBLE);
                recentSearchesRv.setVisibility(View.VISIBLE);
                searchTv.setVisibility(View.GONE);
            } else {
                Log.d("ttt", "text doesn't have focus");
                if (searchRv.getVisibility() != View.VISIBLE) {
                    recentSearchesRv.setVisibility(View.GONE);
                    recentSearchTv.setVisibility(View.GONE);
                }
            }
        });

        searchView.setOnClickListener(v -> {
            //searchView.onActionViewExpanded();
            searchRv.setVisibility(View.GONE);
            recentSearchTv.setVisibility(View.VISIBLE);
            recentSearchesRv.setVisibility(View.VISIBLE);
            searchTv.setVisibility(View.GONE);
            Log.d("ttt", "clicked searchview");
        });

        searchView.setOnSearchClickListener(v -> {
            searchRv.setVisibility(View.GONE);
            recentSearchTv.setVisibility(View.VISIBLE);
            recentSearchesRv.setVisibility(View.VISIBLE);
            searchTv.setVisibility(View.GONE);
            Log.d("ttt", "clicked on search in searchview");
        });

        searchView.setOnCloseListener(() -> {
            searchRv.setVisibility(View.GONE);
            recentSearchTv.setVisibility(View.VISIBLE);
            recentSearchesRv.setVisibility(View.VISIBLE);
            searchTv.setVisibility(View.GONE);
            Log.d("ttt", "clicked on close in searchview");
            return true;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPromos(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (recentSearchAdapter != null) {
                    recentSearchAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });
    }

    //    public ArrayList<Promotion> getSearchedPromotions() {
//        promotions = new ArrayList<>();
//        newestpromosRef = firestore.collection("promotions");
//        Task<QuerySnapshot> task = newestpromosRef.get();
//        task.addOnSuccessListener(queryDocumentSnapshots -> {
//            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
//                Promotion promotion = queryDocumentSnapshot.toObject(Promotion.class);
////                if (promotion.getPromoimages() == null || promotion.getPromoimages().isEmpty())
////                    continue;
//                promotions.add(promotion);
//                //adapter.notifyDataSetChanged();
//            }
//        });
//        return promotions;
//    }
    private void searchPromos(String searchString) {
        searchView.clearFocus();
        recentSearchesRv.setVisibility(View.GONE);
        //recentSearchLv.setVisibility(View.INVISIBLE);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userRef.document(userDocumentID).update("searchHistory", FieldValue.arrayUnion(searchString));
        }
        searchedPromotions.clear();
        Query searchQuery;
        if (searchString.split(" ").length > 0) {
            String[] searchWords = searchString.split(" ");
            searchQuery = newestpromosRef.whereArrayContainsAny("keyWords", Arrays.asList(searchWords));
        } else {
            searchQuery = newestpromosRef.whereArrayContains("keyWords", searchString);
        }

        searchQuery.get().addOnSuccessListener(snapshots -> {

            Map<String, Integer> map = new HashMap<>();

            for (DocumentSnapshot ds : snapshots) {
                Promotion promotion = ds.toObject(Promotion.class);

                if (map.containsKey(promotion.getType())) {
                    map.put(promotion.getType(), map.get(promotion.getType()) + 1);
                } else {
                    map.put(promotion.getType(), 1);
                }
                searchedPromotions.add(promotion);
//              adapter.notifyItemInserted(searchedPromotions.size());
            }

            Objects.requireNonNull(getActivity()).runOnUiThread(() -> adapter.notifyDataSetChanged());

            if (snapshots.isEmpty()) {
                searchTv.setVisibility(View.VISIBLE);
                searchRv.setVisibility(View.GONE);
                recentSearchTv.setVisibility(View.VISIBLE);
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    getUpdatedRecentSearches();
                }
            } else {
                Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
                CollectionReference searchRef = FirebaseFirestore.getInstance().collection("searches");
                for (Map.Entry<String, Integer> entry : entrySet) {
                    searchRef.document(entry.getKey()).update("searchCount", FieldValue.increment(entry.getValue()));
                }
//                searchView.clearFocus();
                searchRv.setVisibility(View.VISIBLE);
                recentSearchTv.setVisibility(View.GONE);
                searchTv.setVisibility(View.GONE);
                recentSearchesRv.setVisibility(View.GONE);
            }
        });
    }

    private void getRecentSearches(String userDocumentID) {
        userRef.document(userDocumentID).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.contains("searchHistory")) {
                ArrayList<String> searches = new ArrayList<>();
                userRef.document(userDocumentID).update("searchHistory", searches);
            } else {
                recentSearches = (List<String>) documentSnapshot.get("searchHistory");
                if (!recentSearches.isEmpty()) {
                    recentSearchAdapter = new RecentSearchAdapter(getContext(), recentSearches, this);
                    recentSearchAdapter.setHasStableIds(true);
                    recentSearchesRv.setLayoutManager(new LinearLayoutManager(getContext()));
                    recentSearchesRv.setAdapter(recentSearchAdapter);
//                    recentSearchesRv.setOnItemClickListener((parent, view1, position, id) -> {
//                        searchView.setQuery(recentSearchAdapter.getItem(position).toString(),true);
//                        //searchPromos(recentSearchAdapter.getItem(position).toString());
//                    });
                }
            }
        });
    }

    private void getUpdatedRecentSearches() {

        userRef.document(userDocumentID).get().addOnSuccessListener(documentSnapshot -> {
            List<String> newRecentSearches = (List<String>) documentSnapshot.get("searchHistory");
            if (recentSearchAdapter == null) {
                if(recentSearches==null)recentSearches = new ArrayList<>();
                recentSearchAdapter = new RecentSearchAdapter(getContext(), recentSearches, this);
                recentSearchAdapter.setHasStableIds(true);
                recentSearchesRv.setLayoutManager(new LinearLayoutManager(getContext()));
                recentSearchesRv.setAdapter(recentSearchAdapter);

//                recentSearchLv.setOnItemClickListener((parent, view1, position, id) -> {
//                    searchView.setQuery(recentSearchAdapter.getItem(position).toString(),true);
////                searchPromos(recentSearchAdapter.getItem(position).toString());
//                });
            } else {
//                recentSearches.clear();
                if (newRecentSearches.size() > recentSearches.size()) {
                    recentSearches.add(newRecentSearches.get(newRecentSearches.size() - 1));
                    recentSearchAdapter.notifyItemInserted(recentSearches.size());
                }
//                recentSearchAdapter.notifyDataSetChanged();
            }
        });
    }

    void submitSearch(String search) {
        searchView.setQuery(search, true);
    }
}

