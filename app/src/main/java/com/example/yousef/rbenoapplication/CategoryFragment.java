package com.example.yousef.rbenoapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.exoplayer2.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends DialogFragment implements SwipeRefreshLayout.OnRefreshListener {
    TextView filterTitle;
    int lastQuery = 0;
    private RecyclerView filteredRv;
    private ArrayList<Promotion> promotions;
    private newestpromosadapter adapter;
    private boolean isLoading = true;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Query updatedQuery;
    private Query videosQuery;
    private Query imagesQuery;
    private Query textQuery;
    private DocumentSnapshot lastVideoResult;
    private DocumentSnapshot lastImageResult;
    private DocumentSnapshot lastTextResult;
    private boolean noVideosLeft = false;
    private boolean noImagesLeft = false;
    private boolean noTextLeft = false;

    static CategoryFragment newInstance() {
        return new CategoryFragment();
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
        filterTitle = view.findViewById(R.id.filterTitle);
        ImageView backImage = view.findViewById(R.id.backImage);
        filteredRv = view.findViewById(R.id.filteredRv);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.red);
        swipeRefreshLayout.setOnRefreshListener(this);
        backImage.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        String category = getArguments().getString("category");
        filterTitle.setText(category);
        promotions = new ArrayList<>();

        filteredRv.setLayoutManager(new LinearLayoutManager(getContext()));
        filteredRv.setItemAnimator(null);
        promotions = new ArrayList<>();
        adapter = new newestpromosadapter(promotions, getContext());
        adapter.setHasStableIds(true);
        filteredRv.setAdapter(adapter);


        videosQuery = FirebaseFirestore.getInstance().collection("promotions")
//                        .whereEqualTo("country", countryName)
                .whereEqualTo("type", category)
                .whereEqualTo("promoType", "video")
                .whereEqualTo("isBanned", false)
                .orderBy("publishtime", Query.Direction.DESCENDING).limit(5);

        imagesQuery = FirebaseFirestore.getInstance().collection("promotions")
//                        .whereEqualTo("country", countryName)
                .whereEqualTo("type", category)
                .whereEqualTo("promoType", "image")
                .whereEqualTo("isBanned", false)
                .orderBy("publishtime", Query.Direction.DESCENDING).limit(8);

        textQuery = FirebaseFirestore.getInstance().collection("promotions")
//                        .whereEqualTo("country", countryName)
                .whereEqualTo("type", category)
                .whereEqualTo("promoType", "text")
                .whereEqualTo("isBanned", false)
                .orderBy("publishtime", Query.Direction.DESCENDING).limit(10);
        isLoading = true;
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

    @Override
    public void onRefresh() {
        if (GlobalVariables.isWifiIsOn()) {
            swipeRefreshLayout.setRefreshing(true);
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
            lastQuery = 0;
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
                    } else {
                        updatedQuery = videosQuery.startAfter(lastVideoResult);
                    }
                    lastQuery = 1;
                } else if (!noImagesLeft) {
                    if (lastImageResult == null) {
                        updatedQuery = imagesQuery;
                    } else {
                        updatedQuery = imagesQuery.startAfter(lastImageResult);
                    }
                    lastQuery = 2;
                } else if (!noTextLeft) {
                    if (lastTextResult == null) {
                        updatedQuery = textQuery;
                    } else {
                        updatedQuery = textQuery.startAfter(lastTextResult);
                    }
                    lastQuery = 3;
                } else {
                    isLoading = false;
                    swipeRefreshLayout.setRefreshing(false);
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
                        filteredRv.post(() -> adapter.notifyItemRangeInserted(previousPos, promotions.size() - previousPos));
                        swipeRefreshLayout.setRefreshing(false);

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

}
