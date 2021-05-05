package com.example.yousef.rbenoapplication;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import hyogeun.github.com.colorratingbarlib.ColorRatingBar;

public class UserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
  private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
  private final
  CollectionReference userRef = FirebaseFirestore.getInstance().collection("users"),
          promotionRef = FirebaseFirestore.getInstance().collection("promotions");
  boolean status;
  private NewestPromosAdapter adapter;
  private ColorRatingBar ratingBar;
  private ArrayList<Promotion> Promotions;
  private double ratingSum;
  private String CurrentUserDocumentId, vistingUserid;
  private ImageView statusImage, profilePic;
  private TextView statusTv, countryTv, staticusername, ratingTextView, userPromosTv, usernameTv;
  private RecyclerView lv;
  private SwipeRefreshLayout swipeRefreshLayout;
  private Toolbar toolbar;
  //  private ListenerRegistration listener;
  private PromotionDeleteReceiver promotionDeleteReceiver;


  public UserFragment() {
  }


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    if (getArguments() != null) {
      vistingUserid = getArguments().getString("promouserid");
    }
  }

//  @NonNull
//  @Override
//  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//    return new Dialog(getActivity(), getTheme()) {
//      @Override
//      public void onBackPressed() {
//        if (getActivity() instanceof HomeActivity) {
////          if(!((HomeActivity)getActivity()).bundles.isEmpty()){
//          getActivity().onBackPressed();
////          }else{
////            dismiss();
////          }
//        } else if (getActivity() instanceof MessagingRealTimeActivity) {
////          if(((MessagingActivity)getActivity()).bundles.size() != 0){
//          getActivity().onBackPressed();
////          }else{
////            dismiss();
////          }
//        }
//
//      }
//    };
//  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_user, container, false);

    setupDeletionReceiver();

//    userNameTv = view.findViewById(R.id.userNameTv);
    profilePic = view.findViewById(R.id.profile_image);
    staticusername = view.findViewById(R.id.staticusername);
    countryTv = view.findViewById(R.id.countryTv);
    ratingBar = view.findViewById(R.id.ratingBar);
//    view.findViewById(R.id.back_arrow).setOnClickListener(v -> Objects.requireNonNull(getActivity()).onBackPressed());
    lv = view.findViewById(R.id.myPromosRecyclerView);

    userPromosTv = view.findViewById(R.id.userPromosTv);
    statusImage = view.findViewById(R.id.StatusImage);
    statusTv = view.findViewById(R.id.StatusTv);
    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    ratingTextView = view.findViewById(R.id.ratingTextView);
    usernameTv = view.findViewById(R.id.usernameTv);
//        profilePic.getLayoutParams().height = GlobalVariables.getWindowHeight() / 5;
//    view.findViewById(R.id.greyLinearLayout).getLayoutParams().height = GlobalVariables.getWindowHeight() / 9;

    final AdView adView = view.findViewById(R.id.adView);
    adView.loadAd(new AdRequest.Builder().build());
    adView.setAdListener(new AdListener() {
      @Override
      public void onAdLoaded() {
        adView.setVisibility(View.VISIBLE);
      }
    });

    toolbar = view.findViewById(R.id.userToolbar);

    if (vistingUserid != null) {
      toolbar.setNavigationIcon(R.drawable.back_arrow_white);
      toolbar.setNavigationOnClickListener(view1 -> getActivity().onBackPressed());
      toolbar.inflateMenu(R.menu.user_visting_account);
      toolbar.setOnMenuItemClickListener(item -> {
        if (item.getItemId() == R.id.block_account_user) {

          if (!currentUser.isAnonymous()) {
            if (!GlobalVariables.getBlockedUsers().contains(vistingUserid)) {
              if (WifiUtil.checkWifiConnection(getContext())) {
                userRef.whereEqualTo("userId", currentUser.getUid())
                        .get().addOnSuccessListener(snapshots ->
                        userRef.document(snapshots.getDocuments().get(0).getId())
                                .update("usersBlocked", FieldValue.arrayUnion(vistingUserid))
                                .addOnSuccessListener(v -> {

                                  GlobalVariables.getBlockedUsers().add(vistingUserid);
                                  userRef.document(CurrentUserDocumentId)
                                          .update("blocks", FieldValue.arrayUnion(currentUser))
                                          .addOnSuccessListener(aVoid12 ->
                                                  Toast.makeText(getContext(),
                                                          "لقد تم حظر المشترك!",
                                                          Toast.LENGTH_SHORT).show());

                                }));
              }
            } else {
              Toast.makeText(getContext(), "لقد تم حظر المشترك من قبل!"
                      , Toast.LENGTH_SHORT).show();
            }

          } else {
            showSigninDialog();
          }
        } else if (item.getItemId() == R.id.report_user) {
          if (!currentUser.isAnonymous()) {
            if (WifiUtil.checkWifiConnection(getContext())) {
              userRef.document(CurrentUserDocumentId).get()
                      .addOnSuccessListener(documentSnapshot -> {
                        long currentTimeInMillies = System.currentTimeMillis() / 1000;
                        ArrayList<String> reports = (ArrayList<String>) documentSnapshot.get("reports");
                        if (reports != null) {
                          for (String report : reports) {
                            if (report.split("-")[0].equals(currentUser.getUid())) {
                              Toast.makeText(getContext(), "لقد قمت بالإبلاغ عن هذا المستخدم من قبل!", Toast.LENGTH_SHORT).show();
                              return;
                            }
                          }
                          userRef.document(CurrentUserDocumentId).update("reports", FieldValue.arrayUnion(currentUser.getUid() + "-" + currentTimeInMillies)).addOnSuccessListener(aVoid -> {
//                                      reports = new ArrayList<>();
                            reports.add(currentUser.getUid() + "-" + currentTimeInMillies);
                            if (reports.size() >= 10) {
                              if (((currentTimeInMillies - Long.parseLong(reports.get(reports.size() - 10).split("-")[1]))) < 86400000) {
                                userRef.document(CurrentUserDocumentId).update("userBanned", true).addOnSuccessListener(aVoid1 -> promotionRef.whereEqualTo("uid", vistingUserid)
                                        .get().addOnSuccessListener(snapshots -> {
                                          for (DocumentSnapshot documentSnapshot1 : snapshots.getDocuments()) {
                                            promotionRef.document(documentSnapshot1.getId())
                                                    .update("isBanned", true);
                                          }
                                        }).addOnCompleteListener(task -> getActivity().onBackPressed()));
                              }
                            }
                            Toast.makeText(getContext(), "لقد تم الإبلاغ عن هذا المستخدم!", Toast.LENGTH_SHORT).show();
                          });
                        } else {
                          userRef.document(CurrentUserDocumentId).update("reports", FieldValue.arrayUnion(currentUser.getUid() + "-" + currentTimeInMillies)).addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "لقد تم الإبلاغ عن هذا المستخدم!", Toast.LENGTH_SHORT).show());
                        }
                      });
            }
          } else {
            showSigninDialog();
          }
        }

        return true;
      });
    } else {
      usernameTv.setText("حسابي");
      toolbar.inflateMenu(R.menu.account_edit_menu);
      toolbar.setOnMenuItemClickListener(item -> {


        if (item.getItemId() == R.id.edit_account) {

          if (WifiUtil.checkWifiConnection(getContext())) {
            ((HomeActivity) getActivity())
                    .addFragmentToHomeContainer(AccountSettingsFragment.newInstance());
          }


//          new AccountSettingsFragment().show(getChildFragmentManager(), "accountSettings");
        } else if (item.getItemId() == R.id.status_present) {
          if (WifiUtil.checkWifiConnection(getContext())) {
            userRef.document(CurrentUserDocumentId)
                    .update("status", !status).addOnSuccessListener(aVoid -> {
              status = !status;
              if (status) {
                setStatus(R.drawable.green_circle, "نشط الأن");
                toolbar.getMenu().findItem(R.id.status_present).setIcon(R.drawable.green_circle);
                ((HomeActivity) getActivity()).changeStatusIcon(R.drawable.green_circle);
              } else {
                toolbar.getMenu().findItem(R.id.status_present).setIcon(R.drawable.red_circle);
                setStatus(R.drawable.red_circle, "غير نشط الأن");
                ((HomeActivity) getActivity()).changeStatusIcon(R.drawable.red_circle);
              }
            });
          }
        } else if (item.getItemId() == R.id.blocked_users) {
          if (WifiUtil.checkWifiConnection(getContext())) {
            ((HomeActivity) getActivity()).addFragmentToHomeContainer(new BlockedUsersFragment());
          }
        }
        return true;
      });
    }

    return view;
  }


  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);


//        swipeRefreshLayout.setRefreshing(false);
    swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red));
    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setRefreshing(true);


    Promotions = new ArrayList<>();
    ratingBar.setIsIndicator(true);

    if (vistingUserid != null) {
      getUserData(vistingUserid);
      getPromos(vistingUserid);
    } else {
      getUserData(currentUser.getUid());
      getPromos(currentUser.getUid());
    }

  }

  //  @SuppressLint("RestrictedApi")
  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    if (vistingUserid != null) {
      inflater.inflate(R.menu.user_visting_account, menu);
    } else {
      inflater.inflate(R.menu.account_edit_menu, menu);
//      if (menu instanceof MenuBuilder) {
//        ((MenuBuilder) menu).setOptionalIconsVisible(true);
//      }
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(@NonNull Menu menu) {
    super.onPrepareOptionsMenu(menu);
    if (status) {
      menu.getItem(1).setIcon(R.drawable.green_circle);
    } else {
      menu.getItem(1).setIcon(R.drawable.red_circle);
    }
  }

  @Override
  public void onRefresh() {
    if (WifiUtil.checkWifiConnection(getContext())) {
      if (vistingUserid != null) {
        getUserData(vistingUserid);
        getPromos(vistingUserid);
      } else {
        getUserData(currentUser.getUid());
        getPromos(currentUser.getUid());
      }
    }

  }

  private void getUserData(String id) {

    Task<QuerySnapshot> task = userRef.whereEqualTo("userId", id).get();
    task.addOnSuccessListener(queryDocumentSnapshots -> {

      if (queryDocumentSnapshots.isEmpty()) {
        Toast.makeText(getContext(), R.string.check_internet,
                Toast.LENGTH_SHORT).show();
        return;
      }

      final DocumentSnapshot ds = queryDocumentSnapshots.getDocuments().get(0);
      CurrentUserDocumentId = ds.getId();
      final String imageUrl = ds.getString("imageurl");
      final String username = ds.getString("username");
      final String country = ds.getString("country");

      final String countryCode = ds.getString("countryCode");

      countryTv.setText(CountryUtil.getCountryName(countryCode) + " " +
              EmojiUtil.countryCodeToEmoji(countryCode));

//      if(countryCode != null){
//
//          final String countryName = new Locale(Locale.getDefault().getLanguage()
//                  ,countryCode).getDisplayCountry();
//
//          Log.d("ttt","countryName: "+countryName);
//          countryTv.setText(countryName+" "+EmojiUtil.countryCodeToEmoji(countryCode));
//
//
//      }else if(country!=null){
//
//        String code = getCountryCode(country,"en");
//        if(code == null){
//          code = getCountryCode(country,"ar");
//        }
//        if(code == null){
//          code = getCountryCode(country, Locale.getDefault().getLanguage());
//        }
//
//        final String countryName = new Locale(code).getDisplayCountry();
//        countryTv.setText(countryName+" "+EmojiUtil.countryCodeToEmoji(code));
//      }


      if (imageUrl != null && !imageUrl.isEmpty()) {
        Picasso.get().load(imageUrl).fit().into(profilePic);

        profilePic.setOnClickListener(v -> FullScreenImagesUtil.showImageFullScreen(getContext(),
                imageUrl, null));

      }

      if (!id.equals(currentUser.getUid())) {
        usernameTv.setText(username);
      }

      staticusername.setText(username);
//      staticusername.post(() -> staticusername.setText("@" + username.toLowerCase().trim()));
      status = ds.getBoolean("status");
      if (getContext() != null) {
        if (status) {
          if (vistingUserid == null) {
            toolbar.getMenu().findItem(R.id.status_present).setIcon(R.drawable.green_circle);
          }
          setStatus(R.drawable.green_circle, "نشط الأن");
        } else {
          if (vistingUserid == null) {
            toolbar.getMenu().findItem(R.id.status_present).setIcon(R.drawable.red_circle);

          }
          setStatus(R.drawable.red_circle, "غير نشط الأن");
        }
      }

    });
  }

  private void getPromos(String id) {

    if (vistingUserid != null) {

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
          if (lv.getAdapter().getItemViewType(position) == 2) {
            return 2;
          }
          return 1;
        }
      });

      lv.setLayoutManager(glm);

      adapter = new NewestPromosAdapter(Promotions, getContext(),
              R.layout.newest_promo_item_grid, 3);
    } else {
      lv.setLayoutManager(new LinearLayoutManager(getContext(),
              RecyclerView.VERTICAL, false) {
        @Override
        public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
          lp.height = (int) (getWidth() * 0.292);
          return true;
        }

        @Override
        public void onItemsRemoved(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
          if (itemCount == 0)
            userPromosTv.setVisibility(View.GONE);
        }
      });
      adapter = new NewestPromosAdapter(Promotions, getContext(), R.layout.my_promo_item_layout, 2);
    }

//    adapter.setHasStableIds(true);
    lv.setAdapter(adapter);

    Promotions.clear();
    final List<Double> ratingsList = new ArrayList<>();
    Query query =
            promotionRef.orderBy("publishtime", Query.Direction.DESCENDING)
                    .whereEqualTo("uid", id)
                    .whereEqualTo("isBanned", false);

    if (vistingUserid != null) {
      query = query.whereEqualTo("isPaused", false);
    }

//    Query finalQuery = query;

    query.get().addOnSuccessListener(snapshots -> {
      for (QueryDocumentSnapshot snapshot : snapshots) {
        final Promotion promotion = snapshot.toObject(Promotion.class);
        Promotions.add(promotion);
        if (promotion.getRating() != 0) ratingsList.add(promotion.getRating());
      }
    }).addOnCompleteListener(task -> {
      if (Promotions.size() > 0) {

        adapter.notifyDataSetChanged();

        ratingSum = 0;
        for (Double rating : ratingsList) {
          ratingSum += rating;
        }
        if (ratingSum > 0) {
          final float ratingCalc = (float) (ratingSum / ratingsList.size());
          ratingBar.setRating(ratingCalc);
          ratingTextView.setText((ratingCalc * 2) * 10 + "%");
        }

        if (vistingUserid == null) {
          userPromosTv.setVisibility(View.VISIBLE);
        }
      } else {
        userPromosTv.setVisibility(View.GONE);
      }

//      if (listener == null) {
//        listener = finalQuery.addSnapshotListener((value, error) -> {
//          if (value != null) {
//            final List<DocumentChange> documentChanges = value.getDocumentChanges();
//            for (DocumentChange documentChange : documentChanges) {
//              if (documentChange.getType() == DocumentChange.Type.REMOVED) {
//                final long removedPromoId = documentChange.getDocument().getLong("promoid");
//                for (Promotion promo : Promotions) {
//                  if (promo.getPromoid() == removedPromoId) {
//                    promo.setPromoid(-1);
//
//                    if (id.equals(currentUser.getUid())) {
//                      final int index = Promotions.indexOf(promo);
//                      Promotions.remove(index);
//                      adapter.notifyItemRemoved(index);
//
//                      if (Promotions.size() == 0) {
//                        userPromosTv.setVisibility(View.GONE);
//                      }
//                    }
//
//                    break;
//                  }
//                }
//              }
//            }
//
//          }
//        });
//      }


      swipeRefreshLayout.setRefreshing(false);
    });
  }

  void showSigninDialog() {
    SigninUtil.getInstance(getContext(), getActivity()).show();
  }

  void setStatus(int drawable, String statusText) {
    statusImage.setImageDrawable(ContextCompat.getDrawable(getContext(), drawable));
    statusTv.setText(statusText);
  }

//  @Override
//  public void onDestroy() {
//    super.onDestroy();
//    if (listener != null)
//      listener.remove();
//  }

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
                Log.d("ttt", "received delete");


                Promotion.changePromoStatusFromList(Promotions,
                        intent.getLongExtra("promoId", 0),
                        intent.getStringExtra("changeType"),
                        adapter);

              }
            };

    getContext().registerReceiver(promotionDeleteReceiver,
            new IntentFilter(BuildConfig.APPLICATION_ID + ".promoDelete"));


  }


}
