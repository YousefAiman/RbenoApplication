package com.example.yousef.rbenoapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilteredPromosFragment extends DialogFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int MINUTE_MILLIS = 60;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    SwipeRefreshLayout swipe;
    private RecyclerView filteredRv;
    private ArrayList<Promotion> promotions;
    private newestpromosadapter adapter;
    private Query query;
    private int price;
    private int views;
    private boolean isLoading = true;
    private DocumentSnapshot lastResult;
    int itemsAdded = 0;
    TextView noPromosTv;
    public FilteredPromosFragment() {
        // Required empty public constructor
    }

    static FilteredPromosFragment newInstance() {
        return new FilteredPromosFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filtered_promos, container, false);
        filteredRv = view.findViewById(R.id.filteredRv);
        swipe = view.findViewById(R.id.swipeRefreshLayout);
        swipe.setOnRefreshListener(this);
        swipe.setColorSchemeColors(getResources().getColor(R.color.red));
        view.findViewById(R.id.backImage).setOnClickListener(v -> dismiss());
        noPromosTv = view.findViewById(R.id.noPromosTv);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Bundle arguments = getArguments();

        price = arguments.getInt("price");
        views = arguments.getInt("views");
        long time = 0;
        long endTime = 0;
        switch (arguments.getInt("date")) {
            case 1:
                time = System.currentTimeMillis() - MINUTE_MILLIS * 2;
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
                .whereEqualTo("isBanned", false);

        if (arguments.containsKey("category")) {
            query = query.whereEqualTo("type", arguments.getString("category"));
        }
        if (arguments.containsKey("promoType")) {
            query = query.whereIn("promoType", arguments.getStringArrayList("promoType"));
        }

        if (time != 0) {
            if (endTime == 0) {
                query = query.whereGreaterThan("publishtime", time)
                        .orderBy("publishtime");
            } else {
                query = query.whereLessThan("publishtime", time).whereGreaterThan("publishtime", endTime)
                        .orderBy("publishtime");
            }
        }else if(price != 0){
                switch (price) {
                    case 1:
                        query = query.orderBy("price", Query.Direction.DESCENDING);
                        break;
                    case 2:
                        query = query.orderBy("price", Query.Direction.ASCENDING);
                        break;
                }
        }else if(views != 0){
            switch (views) {
                case 1:
                    query = query.orderBy("viewcount", Query.Direction.ASCENDING);
                    break;
                case 2:
                    query = query.orderBy("viewcount", Query.Direction.DESCENDING);
                    break;
            }
        }

        if (arguments.containsKey("rating")) {
            query = query.whereEqualTo("rating", arguments.getFloat("rating"));
        }
        promotions = new ArrayList<>();
        adapter = new newestpromosadapter(promotions, getContext());
        adapter.setHasStableIds(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(RecyclerView.VERTICAL);
        filteredRv.setLayoutManager(llm);
        filteredRv.setAdapter(adapter);

        if (time != 0) {
            getQuery();
        }else{
            getUpdatedPromotions();
            filteredRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!filteredRv.canScrollVertically(1) && dy > 0) {
                        if (!isLoading) {
                            isLoading = true;
                            getUpdatedPromotions();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onRefresh() {
        promotions.clear();
        adapter.notifyDataSetChanged();
        if(lastResult!=null){
            lastResult = null;
            getUpdatedPromotions();
            Log.d("query","getUpdatedPromotions");
        }else{
            getQuery();
            Log.d("query","getQuery");
        }
    }

    void getQuery() {
        swipe.setRefreshing(true);

        new Thread(() -> query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(!queryDocumentSnapshots.isEmpty()) {
                List<String> blockedUsers = GlobalVariables.getBlockedUsers();
                if (blockedUsers != null && !blockedUsers.isEmpty()) {
                    for (QueryDocumentSnapshot snap : queryDocumentSnapshots) {
                        if (blockedUsers.contains(snap.getString("uid"))) continue;
                        promotions.add(snap.toObject(Promotion.class));
                    }
                    if(promotions.isEmpty()){
                        Log.d("ttt","empty shit ");
                        noPromosTv.post(()->noPromosTv.setVisibility(View.VISIBLE));
                    }
                } else {
                    for (QueryDocumentSnapshot snap : queryDocumentSnapshots) {
                        promotions.add(snap.toObject(Promotion.class));
                    }

                }

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

                getActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    swipe.setRefreshing(false);
                });

            }else{
                noPromosTv.post(()->noPromosTv.setVisibility(View.VISIBLE));
                swipe.setRefreshing(false);
            }
        }).addOnFailureListener(e -> Log.d("query", e.getMessage()))).start();

    }

    private void getUpdatedPromotions() {

        swipe.setRefreshing(true);
        itemsAdded = 0;
        Query updatedQuery;
        if (lastResult == null) {
            updatedQuery = query.limit(15);
        } else {
            updatedQuery  = query.startAfter(lastResult).limit(10);
        }

        new Thread(() -> updatedQuery.get().addOnSuccessListener(snapshots -> {
            if(!snapshots.isEmpty()){

                lastResult = snapshots.getDocuments().get(snapshots.size() - 1);

                try {
                    for (QueryDocumentSnapshot snap : snapshots) {
                        if (GlobalVariables.getBlockedUsers().contains(snap.getString("uid")))
                            continue;
                        promotions.add(snap.toObject(Promotion.class));
                        itemsAdded++;
                    }
                }finally {
                    if(itemsAdded > 0){
                        try{
                            filteredRv.post(() -> {
                                adapter.notifyItemRangeInserted(promotions.size()-itemsAdded,itemsAdded);
                                swipe.setRefreshing(false);
                            });
                        }finally {
                            isLoading = false;
                        }
                    }else{
                        if(promotions.isEmpty()){
                            Log.d("ttt","empty shit ");
                            noPromosTv.post(()->noPromosTv.setVisibility(View.VISIBLE));
                        }
                        swipe.setRefreshing(false);
                        isLoading = false;
                    }
                }

            }else{
                noPromosTv.post(()->noPromosTv.setVisibility(View.VISIBLE));
                swipe.setRefreshing(false);
                isLoading = false;
            }
        }).addOnFailureListener(e -> Log.d("query",e.toString()))).start();
    }


}
