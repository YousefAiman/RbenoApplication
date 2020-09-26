package com.example.yousef.rbenoapplication;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hyogeun.github.com.colorratingbarlib.ColorRatingBar;

public class UserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private newestpromosadapter adapter;
    private String currentUserid;
    private ColorRatingBar ratingBar;
    private ArrayList<Promotion> Promotions;
    private CollectionReference promotionRef;
    private double ratingSum;
    private CollectionReference userRef;
    private String CurrentUserDocumentId;
    private ImageView statusImage;
    private TextView statusTv;
    private TextView countryTv;
    private String vistingUserid;
    private Toolbar toolbar;
    private ImageView profilePic;
    private TextView staticusername;
    private RecyclerView lv;
    private TextView ratingTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DocumentSnapshot ds;
    private TextView userNameTv;
    private Task<QuerySnapshot> task;
    boolean status;
    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        toolbar = view.findViewById(R.id.promotiontoolbar);
        userNameTv = view.findViewById(R.id.userNameTv);
        profilePic = view.findViewById(R.id.profile_image);
        staticusername = view.findViewById(R.id.staticusername);
        countryTv = view.findViewById(R.id.countryTv);
        ratingBar = view.findViewById(R.id.ratingBar);
        view.findViewById(R.id.back_arrow).setOnClickListener(v -> Objects.requireNonNull(getActivity()).onBackPressed());
        lv = view.findViewById(R.id.myPromosRecyclerView);

        statusImage = view.findViewById(R.id.StatusImage);
        statusTv = view.findViewById(R.id.StatusTv);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        ratingTextView = view.findViewById(R.id.ratingTextView);
//        profilePic.getLayoutParams().height = GlobalVariables.getWindowHeight() / 5;
        view.findViewById(R.id.greyLinearLayout).getLayoutParams().height = GlobalVariables.getWindowHeight() / 9;

        ((AdView) view.findViewById(R.id.adView)).loadAd(new AdRequest.Builder().build());


        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


//        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red));
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);

        promotionRef = FirebaseFirestore.getInstance().collection("promotions");
        userRef = FirebaseFirestore.getInstance().collection("users");
        currentUserid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Promotions = new ArrayList<>();

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
        setHasOptionsMenu(true);
        ratingBar.setIsIndicator(true);
        //Locale locale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
        //countryTv.setText(locale.getDisplayCountry());

//        backArrow.setOnClickListener(v -> getActivity().onBackPressed());

        adapter = new newestpromosadapter(Promotions, getContext());
        adapter.setHasStableIds(true);
        lv.setLayoutManager(new LinearLayoutManager(getContext()));
        lv.setAdapter(adapter);
        if (getArguments() != null) {
            vistingUserid = getArguments().getString("promouserid");
        }

        if (vistingUserid != null) {
            getUserData(vistingUserid);
            getPromos(vistingUserid);
        } else {
            getUserData(currentUserid);
            getPromos(currentUserid);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        final MenuInflater menuInflater = getActivity().getMenuInflater();
        if (vistingUserid != null) {
            menuInflater.inflate(R.menu.user_visting_account, menu);
        } else {
            menuInflater.inflate(R.menu.account_edit_menu, menu);
            if(menu instanceof MenuBuilder){
                MenuBuilder menuBuilder = (MenuBuilder) menu;
                menuBuilder.setOptionalIconsVisible(true);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(status){
            menu.getItem(1).setIcon(R.drawable.green_circle);
        }else{
            menu.getItem(1).setIcon(R.drawable.red_circle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.edit_account:
                new AccountSettingsFragment().show(getChildFragmentManager(), "accountSettings");
                return true;

            case R.id.status_present:
                if(status){
                    status = false;
                    userRef.document(CurrentUserDocumentId).update("status",false).addOnSuccessListener(aVoid -> {
                        if(status){
                            setStatus(R.drawable.green_circle, "نشط الأن");
                            ((HomeActivity) getActivity()).changeStatusIcon(R.drawable.green_circle);
                        }else{
                            setStatus(R.drawable.red_circle, "غير نشط الأن");
                            ((HomeActivity) getActivity()).changeStatusIcon(R.drawable.red_circle);
                        }
                    });
                }else{
                    status = true;
                    userRef.document(CurrentUserDocumentId).update("status",true).addOnSuccessListener(aVoid -> {
                        if(status){
                            setStatus(R.drawable.green_circle, "نشط الأن");
                            ((HomeActivity) getActivity()).changeStatusIcon(R.drawable.green_circle);
                        }else{
                            setStatus(R.drawable.red_circle, "غير نشط الأن");
                            ((HomeActivity) getActivity()).changeStatusIcon(R.drawable.red_circle);
                        }
                    });
                }

                return true;
//
//            case R.id.status_gone:
//                userRef.document(CurrentUserDocumentId).update("status", false).addOnSuccessListener(aVoid -> {
//                    setStatus(R.drawable.red_circle, "غير نشط الأن");
////                    statusImage.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.red_circle));
//                    ((HomeActivity) getActivity()).changeStatusIcon(R.drawable.red_circle);
////                    statusTv.setText("غير نشط الأن");
//                });
//                return true;
            case R.id.block_account_user:
                userRef.document(CurrentUserDocumentId).get().addOnSuccessListener(snapshots ->
                        userRef.document(snapshots.getId()).update("usersBlocked", FieldValue.arrayUnion(vistingUserid)).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "لقد تم حظر المشترك!", Toast.LENGTH_SHORT).show();
                            }
                        }));
                return true;
            case R.id.report_user:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                    userRef.document(ds.getId()).get().addOnSuccessListener(documentSnapshot -> {
                        long currentTimeInMillies = System.currentTimeMillis() / 1000;
                        ArrayList<String> reports = (ArrayList<String>) documentSnapshot.get("reports");
                        if (reports != null) {
                            for (String report : reports) {
                                if (report.split("-")[0].equals(currentUserid)) {
                                    Toast.makeText(getContext(), "لقد قمت بالإبلاغ عن هذا المستخدم من قبل!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            userRef.document(ds.getId()).update("reports", FieldValue.arrayUnion(currentUserid + "-" + currentTimeInMillies)).addOnSuccessListener(aVoid -> {
//                                      reports = new ArrayList<>();
                                reports.add(currentUserid + "-" + currentTimeInMillies);
                                if (reports.size() >= 10) {
                                    if (((currentTimeInMillies - Long.parseLong(reports.get(reports.size() - 10).split("-")[1]))) < 86400000) {
                                        userRef.document(ds.getId()).update("userBanned", true);
                                        getActivity().onBackPressed();
                                    }
                                }
                                Toast.makeText(getContext(), "لقد تم الإبلاغ عن هذا المستخدم!", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            userRef.document(ds.getId()).update("reports", FieldValue.arrayUnion(currentUserid + "-" + currentTimeInMillies)).addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "لقد تم الإبلاغ عن هذا المستخدم!", Toast.LENGTH_SHORT).show());
                        }
                    });
                } else {
                    showSigninDialog();
                }

//                if (reports != null) {
//                    if (!reports.contains(currentUserid)) {
//                        userRef.document(ds.getId()).update("reports", FieldValue.arrayUnion(currentUserid)).addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "لقد تم الإبلاغ عن هذا المشترك!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(getContext(), "لقد فشلت عملية الإبلاغ عن هذا المشترك!", Toast.LENGTH_SHORT).show());
//                    } else {
//                        Toast.makeText(getContext(), "لقد قمت بالإبلاغ عن هذا المستخدم من قبل!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                firestore.collection("users").whereEqualTo("userId", vistingUserid).get().addOnSuccessListener(snapshots -> firestore.collection("users").document(snapshots.getDocuments().get(0).getId()).update("reports", FieldValue.arrayUnion(currentUserid)).addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(getContext(), "لقد تم الابلاغ عن المشترك!", Toast.LENGTH_SHORT).show();
//                    }
//                }));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRefresh() {
        if (vistingUserid != null) {
            getUserData(vistingUserid);
            getPromos(vistingUserid);
        } else {
            getUserData(currentUserid);
            getPromos(currentUserid);
        }
    }

    private void getUserData(String id) {

        task = userRef.whereEqualTo("userId", id).get();
        new Thread(() -> task.addOnSuccessListener(queryDocumentSnapshots -> {
            ds = queryDocumentSnapshots.getDocuments().get(0);
            CurrentUserDocumentId = ds.getId();
            String imageUrl = ds.getString("imageurl");
            String username = ds.getString("username");
            String country = ds.getString("country");
            if(imageUrl!=null && !imageUrl.isEmpty()){
                profilePic.post(() -> Picasso.get().load(imageUrl).fit().into(profilePic));
            }
            userNameTv.post(() -> userNameTv.setText(username));
            staticusername.post(() -> staticusername.setText("@" + username.toLowerCase().trim()));
            status = ds.getBoolean("status");
            if (getContext() != null) {
                if (status) {
                    setStatus(R.drawable.green_circle, "نشط الأن");
                } else {
                    setStatus(R.drawable.red_circle, "غير نشط الأن");
                }
            }
            countryTv.post(() -> countryTv.setText(country));
        })).start();
    }

    private void getPromos(String id) {
        Promotions.clear();
        new Thread(() -> {
            List<Double> ratingsList = new ArrayList<>();
            promotionRef.whereEqualTo("uid", id).get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                    Promotion promotion = queryDocumentSnapshot.toObject(Promotion.class);
                    Promotions.add(promotion);
                    if(promotion.getRating()!=0)ratingsList.add(promotion.getRating());
                }
            }).addOnCompleteListener(task -> {
                if(Promotions.size()>0) {
                    ratingSum = 0;
                    for (int i = 0; i < ratingsList.size(); i++) {
                        ratingSum += ratingsList.get(i);
                    }
                    if (ratingSum > 0) {
                        float ratingCalc = (float) (ratingSum / ratingsList.size());
                        ratingBar.setRating(ratingCalc);
                        ratingTextView.setText((ratingCalc * 2) * 10 + "%");
                    }
                    lv.post(() -> {
                        adapter.notifyDataSetChanged();
                    });
                    swipeRefreshLayout.setRefreshing(false);
                    getView().findViewById(R.id.userPromosTv).setVisibility(View.VISIBLE);
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                    getView().findViewById(R.id.userPromosTv).setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private void showSigninDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.signin_alert_layout);
        dialog.findViewById(R.id.alert_close).setOnClickListener(v -> dialog.cancel());
        dialog.findViewById(R.id.alert_signin).setOnClickListener(v -> startActivity(new Intent(getContext(), WelcomeActivity.class)));
        dialog.show();
    }

    void setStatus(int drawable, String statusText) {
        statusImage.setImageDrawable(ContextCompat.getDrawable(getContext(), drawable));
        statusTv.setText(statusText);
    }
}
