package com.example.yousef.rbenoapplication;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyPromotionsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    ArrayList<Promotion> Promotions;
    RecyclerView lv;
    MyPromotionsAdapter myPromotionsAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    public MyPromotionsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_promotions, container, false);
        lv = view.findViewById(R.id.myPromosRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red));
        swipeRefreshLayout.setRefreshing(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Promotions = new ArrayList<>();
        myPromotionsAdapter = new MyPromotionsAdapter(Promotions, getContext());
        myPromotionsAdapter.setHasStableIds(true);
        lv.setAdapter(myPromotionsAdapter);
        lv.setLayoutManager(new LinearLayoutManager(getContext()));

        view.findViewById(R.id.profileImage).setOnClickListener(v -> ((HomeActivity) getActivity()).showDrawer());

        swipeRefreshLayout.setOnRefreshListener(this);

        if (GlobalVariables.getProfileImageUrl() != null) {
            Picasso.get().load(GlobalVariables.getProfileImageUrl()).fit().into(((ImageView) view.findViewById(R.id.profileImage)));
        }

        getMyPromos();

    }

    @Override
    public void onRefresh() {
        Promotions.clear();
        myPromotionsAdapter.notifyDataSetChanged();
        getMyPromos();
    }

    void getMyPromos() {
        new Thread(() -> FirebaseFirestore.getInstance().collection("promotions").whereEqualTo("uid", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Promotions.add(queryDocumentSnapshot.toObject(Promotion.class));
                    }
                    lv.post(() -> {
                        myPromotionsAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    });
//            Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
//                myPromotionsAdapter.notifyDataSetChanged();
//                swipeRefreshLayout.setRefreshing(false);
//            });
                })).start();
    }
}
