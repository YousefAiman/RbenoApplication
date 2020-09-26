package com.example.yousef.rbenoapplication;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class favouriteFragment extends Fragment implements onBackPressed {
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ArrayList<Promotion> Promotions;
    private List<Integer> favPromosId;
    private favpromosadapter adapter;
    private TextView favouriteCount;
    private RecyclerView lv;
    private TextView noPromoTv;
    private ImageView noPromosIv;
//    ListenerRegistration listener;

    public favouriteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        Promotions = new ArrayList<>();
        favPromosId = new ArrayList<>();
        lv = view.findViewById(R.id.favPromoRecyclerView);
        noPromoTv = view.findViewById(R.id.noPromosTv);
        favouriteCount = view.findViewById(R.id.favouriteCount);
        noPromosIv = view.findViewById(R.id.noPromosIv);

        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CollectionReference promoRef = firestore.collection("promotions");
        CollectionReference usersRef = firestore.collection("users");

        usersRef.whereEqualTo("userId", auth.getCurrentUser().getUid()).limit(1).get().addOnSuccessListener(snapshots -> {
            DocumentSnapshot document = snapshots.getDocuments().get(0);
            adapter = new favpromosadapter(Promotions, R.layout.favpromoitemdesign, R.layout.favpromoitemdesigntext, getContext(), document.getId());
            adapter.setHasStableIds(true);
            lv.setLayoutManager(new LinearLayoutManager(getContext()));
            lv.setAdapter(adapter);
            favPromosId = (List<Integer>) document.get("favpromosids");
            if (favPromosId != null) {
                if (favPromosId.size() == 0) {
                    noPromoTv.setVisibility(View.VISIBLE);
                    noPromosIv.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.INVISIBLE);
                } else {
                    lv.setVisibility(View.VISIBLE);
                    promoRef.whereIn("promoid", favPromosId).get().addOnSuccessListener(snapshots1 -> {
                        List<DocumentSnapshot> documentSnapshots = snapshots1.getDocuments();
                        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                            Promotions.add(documentSnapshot.toObject(Promotion.class));
                        }
                        adapter.notifyDataSetChanged();
                    });
                    usersRef.document(document.getId()).addSnapshotListener((documentSnapshot, e) -> {
                        int size = ((List<Integer>) documentSnapshot.get("favpromosids")).size();
                        favouriteCount.setText("(" + size + ")");
                        if (size == 0) {
                            noPromoTv.setVisibility(View.VISIBLE);
                            noPromosIv.setVisibility(View.VISIBLE);
                            lv.setVisibility(View.INVISIBLE);
                        } else {
                            noPromoTv.setVisibility(View.GONE);
                            noPromosIv.setVisibility(View.GONE);
                            lv.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

        }).addOnCompleteListener(task -> favouriteCount.setText("(" + favPromosId.size() + ")"));

//        usersRef.whereEqualTo("userId",auth.getCurrentUser().getUid()).limit(1).get().addOnSuccessListener(snapshots -> {
//            DocumentSnapshot document =snapshots.getDocuments().get(0);
//            adapter = new favpromosadapter(Promotions, R.layout.favpromoitemdesign, R.layout.favpromoitemdesigntext, getContext(),document.getId());
//            adapter.setHasStableIds(true);
//            lv.setLayoutManager(new LinearLayoutManager(getContext()));
//            lv.setAdapter(adapter);
//            usersRef.document(document.getId()).addSnapshotListener((documentSnapshot, e) -> {
//
//                List<Integer> newFavPromosId = (List<Integer>) snapshots.getDocuments().get(0).get("favpromosids");
//
//                if(newFavPromosId!=null && !newFavPromosId.isEmpty()){
//                    if(newFavPromosId.size() > favPromosId.size()){
//                        List<Integer> oldFavPromoIdsCopy = new ArrayList<>(favPromosId);
//                        newFavPromosId.removeAll(oldFavPromoIdsCopy);
//                        promoRef.whereEqualTo("promoid",newFavPromosId.get(0)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                            @Override
//                            public void onSuccess(QuerySnapshot snapshots) {
//                                Promotions.add(snapshots.getDocuments().get(0).toObject(Promotion.class));
//                                adapter.notifyItemInserted(Promotions.size());
//                            }
//                        });
//                    }else if(newFavPromosId.size() < favPromosId.size()){
//                        List<Integer> oldFavPromoIdsCopy = new ArrayList<>(favPromosId);
//                        oldFavPromoIdsCopy.removeAll(newFavPromosId);
//                        oldFavPromoIdsCopy.get(0);
//                        for(int i=0;i<Promotions.size();i++){
//                            if(Promotions.get(i).getPromoid() == oldFavPromoIdsCopy.get(0)){
//                                Promotions.remove(Promotions.get(i));
//                                adapter.notifyItemRemoved(i);
//                                break;
//                            }
//                        }
//                    }
//                }
////                  if(newFavPromosId!=null && !newFavPromosId.isEmpty() && newFavPromosId.size() != favPromosId.size()){
////
////                  }
////                favPromosId = (List<Integer>) document.get("favpromosids");
////                favouriteCount.setText("(" + favPromosId.size() + ")");
////                if (favPromosId != null){
////                    if (favPromosId.size() == 0) {
////                        noPromoTv.setVisibility(View.VISIBLE);
////                        lv.setVisibility(View.INVISIBLE);
////                    }else{
////                        listener = promoRef.whereIn("promoid",favPromosId).addSnapshotListener((snapshots1, e2) -> {
////                            List<DocumentChange> documentChanges = snapshots1.getDocumentChanges();
////                            for(DocumentChange documentChange:documentChanges) {
////                                if(documentChange.getType() == DocumentChange.Type.ADDED){
////                                    Promotions.add(documentChange.getDocument().toObject(Promotion.class));
////                                    adapter.notifyItemInserted(Promotions.size());
////                                }else if(documentChange.getType() == DocumentChange.Type.REMOVED){
////                                    long removedPromoId = documentChange.getDocument().getLong("promoid");
////                                    for(int i=0;i<Promotions.size();i++){
////                                        Promotion promotion = Promotions.get(i);
////                                        if(promotion.getPromoid() == removedPromoId){
////                                            Promotions.remove(promotion);
////                                            adapter.notifyItemRemoved(i);
////                                            break;
////                                        }
////                                    }
////
////                                }
////                                else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
//////                                long modifiedPromoId = documentChange.getDocument().getLong("promoid");
//////                                for(int i=0;i<Promotions.size();i++){
//////                                    Promotion promotion = Promotions.get(i);
//////                                    if(promotion.getPromoid() == modifiedPromoId){
//////                                        promotion.setViewcount(document.getLong("viewcount"));
//////                                        promotion.setFavcount(document.getLong("favcount"));
//////                                        adapter.notifyItemChanged(i);
//////                                        break;
//////                                    }
//////                                }
////                                }
////                            }
////
////                        });
////
////                    }
////                }
//            });
//        });
//        firestore.collection("users").whereEqualTo("userId",auth.getCurrentUser().getUid()).limit(1).addSnapshotListener((snapshots, e) -> {
//            DocumentSnapshot document =snapshots.getDocuments().get(0);
//            adapter = new favpromosadapter(Promotions, R.layout.favpromoitemdesign, R.layout.favpromoitemdesigntext, getContext(),document.getId());
//            adapter.setHasStableIds(true);
//            lv.setLayoutManager(new LinearLayoutManager(getContext()));
//            lv.setAdapter(adapter);
//            favPromosId = (List<Integer>) document.get("favpromosids");
//            if (favPromosId != null){
//                if (favPromosId.size() == 0) {
//                    noPromoTv.setVisibility(View.VISIBLE);
//                    lv.setVisibility(View.INVISIBLE);
//                }else{
//                    listener = promoRef.whereIn("promoid",favPromosId).addSnapshotListener((snapshots1, e2) -> {
//
//                        List<DocumentChange> documentChanges = snapshots1.getDocumentChanges();
//
//                        for(DocumentChange documentChange:documentChanges) {
////                            DocumentChange.Type type =  documentChange.getType();
//                            if(documentChange.getType() == DocumentChange.Type.ADDED){
//                                Promotions.add(documentChange.getDocument().toObject(Promotion.class));
//                                adapter.notifyItemInserted(Promotions.size());
//                                favouriteCount.setText("(" + favPromosId.size() + ")");
//                            }else if(documentChange.getType() == DocumentChange.Type.REMOVED){
//
//                                long removedPromoId = documentChange.getDocument().getLong("promoid");
//                                for(int i=0;i<Promotions.size();i++){
//                                    Promotion promotion = Promotions.get(i);
//                                    if(promotion.getPromoid() == removedPromoId){
//                                        Promotions.remove(promotion);
//                                        adapter.notifyItemRemoved(i);
//                                        favouriteCount.setText("(" + favPromosId.size() + ")");
//                                        break;
//                                    }
//                                }
//
//                            }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
////                                long modifiedPromoId = documentChange.getDocument().getLong("promoid");
////                                for(int i=0;i<Promotions.size();i++){
////                                    Promotion promotion = Promotions.get(i);
////                                    if(promotion.getPromoid() == modifiedPromoId){
////                                        promotion.setViewcount(document.getLong("viewcount"));
////                                        promotion.setFavcount(document.getLong("favcount"));
////                                        adapter.notifyItemChanged(i);
////                                        break;
////                                    }
////                                }
//                            }
//                        }
//
//                    });
//                }
//            }
//        });
//        firestore.collection("users").whereEqualTo("userId",auth.getCurrentUser().getUid()).limit(1).addSnapshotListener((snapshots, e) -> {
//            DocumentSnapshot document =snapshots.getDocuments().get(0);
//            adapter = new favpromosadapter(Promotions, R.layout.favpromoitemdesign, R.layout.favpromoitemdesigntext, getContext(),document.getId());
//            adapter.setHasStableIds(true);
//            lv.setLayoutManager(new LinearLayoutManager(getContext()));
//            lv.setAdapter(adapter);
//            favPromosId = (List<Integer>) document.get("favpromosids");
//            if (favPromosId != null){
//                if (favPromosId.size() == 0) {
//                    noPromoTv.setVisibility(View.VISIBLE);
//                    lv.setVisibility(View.INVISIBLE);
//                } else {
//                    lv.setVisibility(View.VISIBLE);
//                    listener = promoRef.whereIn("promoid",favPromosId).addSnapshotListener((snapshots1, e2) -> {
//
//                        List<DocumentChange> documentChanges = snapshots1.getDocumentChanges();
//
//                        for(DocumentChange documentChange:documentChanges) {
////                            DocumentChange.Type type =  documentChange.getType();
//                            if(documentChange.getType() == DocumentChange.Type.ADDED){
//                                Promotions.add(documentChange.getDocument().toObject(Promotion.class));
//                                adapter.notifyItemInserted(Promotions.size());
//                                favouriteCount.setText("(" + favPromosId.size() + ")");
//                            }else if(documentChange.getType() == DocumentChange.Type.REMOVED){
//
//                                long removedPromoId = documentChange.getDocument().getLong("promoid");
//                                for(int i=0;i<Promotions.size();i++){
//                                    Promotion promotion = Promotions.get(i);
//                                    if(promotion.getPromoid() == removedPromoId){
//                                        Promotions.remove(promotion);
//                                        adapter.notifyItemRemoved(i);
//                                        favouriteCount.setText("(" + favPromosId.size() + ")");
//                                        break;
//                                    }
//                                }
//
//                            }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
////                                long modifiedPromoId = documentChange.getDocument().getLong("promoid");
////                                for(int i=0;i<Promotions.size();i++){
////                                    Promotion promotion = Promotions.get(i);
////                                    if(promotion.getPromoid() == modifiedPromoId){
////                                        promotion.setViewcount(document.getLong("viewcount"));
////                                        promotion.setFavcount(document.getLong("favcount"));
////                                        adapter.notifyItemChanged(i);
////                                        break;
////                                    }
////                                }
//                            }
//                        }
//
//                    });
//                }
//            }
//            List<DocumentChange> documentChanges = snapshots.getDocumentChanges();
//            for(DocumentChange documentChange:documentChanges){
//                if(documentChange.getType() == DocumentChange.Type.MODIFIED){
//                    List<Integer> newFavPromosId = (List<Integer>) snapshots.getDocuments().get(0).get("favpromosids");
//                    if(newFavPromosId!=null && !newFavPromosId.isEmpty()&& newFavPromosId.size() != favPromosId.size()){
//                        favPromosId = newFavPromosId;
//                        listener = promoRef.whereIn("promoid",favPromosId).addSnapshotListener((snapshots1, e2) -> {
//                            List<DocumentChange> documentChangesPromos = snapshots1.getDocumentChanges();
//                            for(DocumentChange documentChangePromo:documentChangesPromos) {
//                                DocumentChange.Type type =  documentChangePromo.getType();
//                                if(type == DocumentChange.Type.ADDED){
//                                    Promotions.add(documentChangePromo.getDocument().toObject(Promotion.class));
//                                    adapter.notifyItemInserted(Promotions.size());
//                                    favouriteCount.setText("(" + favPromosId.size() + ")");
//                                }else if(type == DocumentChange.Type.REMOVED){
//
//                                    long removedPromoId = documentChangePromo.getDocument().getLong("promoid");
//                                    for(int i=0;i<Promotions.size();i++){
//                                        Promotion promotion = Promotions.get(i);
//                                        if(promotion.getPromoid() == removedPromoId){
//                                            Promotions.remove(promotion);
//                                            adapter.notifyItemRemoved(i);
//                                            favouriteCount.setText("(" + favPromosId.size() + ")");
//                                            break;
//                                        }
//                                    }
//
//                                }else if(type == DocumentChange.Type.MODIFIED){
////                                    long modifiedPromoId = document.getLong("promoid");
////                                    for(int i=0;i<Promotions.size();i++){
////                                        Promotion promotion = Promotions.get(i);
////                                        if(promotion.getPromoid() == modifiedPromoId){
////                                            promotion.setViewcount(document.getLong("viewcount"));
////                                            promotion.setFavcount(document.getLong("favcount"));
////                                            adapter.notifyItemChanged(i);
////                                            favouriteCount.setText("(" + favPromosId.size() + ")");
////                                            break;
////                                        }
////                                    }
//                                }
//                            }
//
//                        });
//                    }
//                }
//            }
//        });
//        firestore.collection("users").whereEqualTo("userId",auth.getCurrentUser().getUid()).limit(1).get().addOnSuccessListener(snapshots -> {
//
//            DocumentSnapshot document =snapshots.getDocuments().get(0);
//            adapter = new favpromosadapter(Promotions, R.layout.favpromoitemdesign, R.layout.favpromoitemdesigntext, getContext(),document.getId());
//            adapter.setHasStableIds(true);
//            lv.setLayoutManager(new LinearLayoutManager(getContext()));
//            lv.setAdapter(adapter);
//            favPromosId = (List<Integer>) document.get("favpromosids");
//            if (favPromosId != null){
//                if (favPromosId.size() == 0) {
//                    noPromoTv.setVisibility(View.VISIBLE);
//                    lv.setVisibility(View.INVISIBLE);
//                } else {
//                    lv.setVisibility(View.VISIBLE);
//                    listener = promoRef.whereIn("promoid",favPromosId).addSnapshotListener((snapshots1, e) -> {
//
//                        List<DocumentChange> documentChanges = snapshots1.getDocumentChanges();
//
//                        for(DocumentChange documentChange:documentChanges) {
//                            DocumentChange.Type type =  documentChange.getType();
//                            if(type == DocumentChange.Type.ADDED){
//                                Promotions.add(documentChange.getDocument().toObject(Promotion.class));
//                                adapter.notifyItemInserted(Promotions.size());
//
//                            }else if(type == DocumentChange.Type.REMOVED){
//
//                                long removedPromoId = documentChange.getDocument().getLong("promoid");
//                                for(int i=0;i<Promotions.size();i++){
//                                    Promotion promotion = Promotions.get(i);
//                                    if(promotion.getPromoid() == removedPromoId){
//                                        Promotions.remove(promotion);
//                                        adapter.notifyItemRemoved(i);
//                                        break;
//                                    }
//                                }
//
//                            }else if(type == DocumentChange.Type.MODIFIED){
//                                long modifiedPromoId = document.getLong("promoid");
//                                for(int i=0;i<Promotions.size();i++){
//                                    Promotion promotion = Promotions.get(i);
//                                    if(promotion.getPromoid() == modifiedPromoId){
//                                        promotion.setViewcount(document.getLong("viewcount"));
//                                        promotion.setFavcount(document.getLong("favcount"));
//                                        adapter.notifyItemChanged(i);
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                        favouriteCount.setText("(" + favPromosId.size() + ")");
//                    });
////                    promoRef.whereIn("promoid",favPromosId).get().addOnSuccessListener(snapshots1 -> {
////                        List<DocumentSnapshot> documentSnapshots = snapshots1.getDocuments();
////                        for(DocumentSnapshot documentSnapshot:documentSnapshots){
////                            Promotions.add(documentSnapshot.toObject(Promotion.class));
////                        }
////                        adapter.notifyDataSetChanged();
////                    });
////                    for (int i = 0; i < favPromosId.size(); i++) {
////                        promoRef.whereEqualTo("promoid", favPromosId.get(i)).limit(1).get().addOnSuccessListener(snapshots1 -> {
////                            Promotions.add(snapshots1.getDocuments().get(0).toObject(Promotion.class));
//////                          adapter.notifyItemInserted(Promotions.size());
////                        });
////                    }
////                    adapter.notifyDataSetChanged();
//                }
//        }
//        });
    }

    //    @Override
//    public void onResume() {
//        super.onResume();
//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("rbeno", Context.MODE_PRIVATE);
//        int promo = -1;
//        boolean isfav = true;
//        if (sharedPreferences.getInt("promo_id", -1) != -1 && !sharedPreferences.getBoolean("is_fav", true)) {
//            promo = sharedPreferences.getInt("promo_id", -1);
//            isfav = sharedPreferences.getBoolean("is_fav", true);
//            if (!isfav) {
//                for (int i = 0; i < Promotions.size(); i++) {
//                    if (Promotions.get(i).getPromoid() == promo) {
//                        Promotions.remove(Promotions.get(i));
//                        adapter.notifyItemRemoved(i);
//                        // Toast.makeText(getActivity(), "Item Removed", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//            getContext();
//            getActivity().getSharedPreferences("rbeno", Context.MODE_PRIVATE).edit().clear().apply();
//        }
//    }
    @Override
    public void onDestroy() {
        super.onDestroy();
//        if(listener!=null){
//            listener.remove();
//        }
    }

    @Override
    public void onBackPressed() {
//        ((HomeActivity) getContext()).unClickableFrameLayout();
    }
}
