package com.example.yousef.rbenoapplication;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import hyogeun.github.com.colorratingbarlib.ColorRatingBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PromotionInfoFragment extends DialogFragment {

    private final GestureDetector gesture = new GestureDetector(getActivity(),
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
                    final int SWIPE_MIN_DISTANCE = 110;
                    final int SWIPE_MAX_OFF_PATH = 250;
                    final int SWIPE_THRESHOLD_VELOCITY = 200;
                    try {
                        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                            return false;
                        if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            getActivity().onBackPressed();
                        }
                    } catch (Exception e) {
                        // nothing
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });
    Promotion p;
    ImageView nextIv;
    ImageView previousIv;
    //    List<String> blockedUsers;
    private FirebaseAuth auth;
    private DocumentReference dr;
    //    private String imageUrl;
    private ViewPager viewPager;
    private LinearLayout sliderLayout;
    private int dotsCount;
    private ImageView[] dots;
    private ArrayList<String> images;
    private CollectionReference promotionRef;
    private CollectionReference usersRef;
    private String viewedDocumentID;
    private Long promoviews;
    private ColorRatingBar ratingBar;
    private ImageView favImage;
    private String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    //    private List<Double> ratings;
    private boolean ratingChanged;
    //    private double ratingSum;
//    private float ratingCalc;
    private RecyclerView lv;
    private List<Long> favPromosId;
    private Boolean alreadyFav;
    private TextView ratingNumberTv;
    //    private boolean favWasClicked = false;
    private Button deleteBtn;
    private ImageView shareImage;
    private ImageView callImage;
    private TextView promotionViews;
    private TextView promotionTitle;
    private TextView promotionPrice;
    private TextView promotionPublish;
    private TextView promotionId;
    private TextView promoInfoTv;
    //    private String currentUsername;
    private String favouredDocumentId;
    private String phonenum;
    private TextView promotionDescTitleTv2;
    private TextView promoInfoTv2;
    private TextView promotionDescTitleTv;
    private CardView promoVideoCardView;
    private ImageView message_img;
    private CollectionReference notifRef;
    private APIService apiService;
    private PlayerView playerView;
    private SimpleExoPlayer exoPlayer;
    private DocumentSnapshot currentDs;
    //    private boolean isFullScreen = false;
    private CollectionReference chatsRef;
    private ArrayList<Promotion> promotions;
    private StaggeredRecyclerAdapter adapter;
    private DocumentSnapshot lastResult;
    private Query relatedQuery;
    private boolean isLoading = true;

    public PromotionInfoFragment() {
    }

    static PromotionInfoFragment newInstance() {
        return new PromotionInfoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_promotion_info, container, false);
        NestedScrollView scrollview = view.findViewById(R.id.scrollview);
        ratingNumberTv = view.findViewById(R.id.ratingNumberTv);
        deleteBtn = view.findViewById(R.id.promo_delete_button);
        favImage = view.findViewById(R.id.fav_image);
        shareImage = view.findViewById(R.id.share_img);
        callImage = view.findViewById(R.id.call_img);
        ratingBar = view.findViewById(R.id.ratingBar);
        promotionViews = view.findViewById(R.id.promotionViewsTv);
        //   navigation = view.findViewById(R.id.promotionnavigation);
        promotionTitle = view.findViewById(R.id.promotion_title);
        promotionPrice = view.findViewById(R.id.promotion_price);
        promotionPublish = view.findViewById(R.id.promotionPublishtimeTv);
        promotionId = view.findViewById(R.id.promotionIdTv);
        promoInfoTv = view.findViewById(R.id.promoInfoTv);
        viewPager = view.findViewById(R.id.promotionsPager);
        message_img = view.findViewById(R.id.message_img);
        sliderLayout = view.findViewById(R.id.promotiondotsSlider);
        // drawerLayout = view.findViewById(R.id.promotiondrawer);
        lv = view.findViewById(R.id.horizontalRecyclerView);
        promotionDescTitleTv = view.findViewById(R.id.promotionDescTitleTv);
        promotionDescTitleTv2 = view.findViewById(R.id.promotionDescTitleTv2);
        promoInfoTv2 = view.findViewById(R.id.promoInfoTv2);
        promoVideoCardView = view.findViewById(R.id.promoVideoCardView);
        scrollview.setOnTouchListener((v, event) -> gesture.onTouchEvent(event));
        view.findViewById(R.id.promoBackIv).setOnClickListener(v->getActivity().onBackPressed());
//        view.setOnTouchListener(new OnSwipeTouchListener(getActivity()){
//            @Override
//            public void onSwipeRight() {
//                super.onSwipeRight();
//                Toast.makeText(getActivity(), "RIGHT SWIPE", Toast.LENGTH_SHORT).show();
//            }
//        });
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        images = new ArrayList<>();


        usersRef = firestore.collection("users");
        promotionRef = firestore.collection("promotions");
        notifRef = firestore.collection("notifications");

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            p = (Promotion) bundle.getSerializable("promo");

            if (p.getPromoType().equals("image")) {
                images.addAll(p.getPromoimages());

                viewPager.getLayoutParams().height = GlobalVariables.getWindowHeight() / 3;

                promoVideoCardView.setVisibility(View.GONE);
                promotionDescTitleTv2.setVisibility(View.GONE);
                promoInfoTv2.setVisibility(View.GONE);
            } else if (p.getPromoType().equals("video")) {
//                HomeActivity homeActivity = ((HomeActivity) getActivity());
                if (GlobalVariables.getVideoViewedCount() % 10 != 0) {
                    GlobalVariables.setVideoViewedCount(GlobalVariables.getVideoViewedCount() + 1);
                }  //  homeActivity.showRewardVideo();

                viewPager.setVisibility(View.GONE);
                promotionDescTitleTv2.setVisibility(View.GONE);
                promoInfoTv2.setVisibility(View.GONE);
                promoVideoCardView.setVisibility(View.VISIBLE);
                playerView = view.findViewById(R.id.promoVideoPlayer);
                exoPlayer = new SimpleExoPlayer.Builder(getContext()).build();
                exoPlayer.prepare(new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "RbenoApp")))
                        .createMediaSource(Uri.parse(p.getVideoUrl())));
                exoPlayer.setPlayWhenReady(true);

//                if(exoPlayer.getPlayWhenReady()){
                playerView.findViewById(R.id.exo_rew).setOnClickListener(v -> {
                    if (exoPlayer.getCurrentPosition() < 3000) {
                        exoPlayer.seekTo(0);
                    } else {
                        exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 3000);
                    }
                });
                playerView.findViewById(R.id.exo_ffwd).setOnClickListener(v -> {
                    if (exoPlayer.getCurrentPosition() < exoPlayer.getDuration() - 3000) {
                        exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 3000);
                    } else {
                        exoPlayer.seekTo(exoPlayer.getDuration());
                    }
                });
                playerView.findViewById(R.id.exo_previous).setVisibility(View.GONE);
                playerView.findViewById(R.id.exo_nextvideo).setVisibility(View.GONE);
                playerView.findViewById(R.id.exoplayer_fullscreen_icon).setOnClickListener(v -> {
                    FullScreenVideoFragment dialogFragment = FullScreenVideoFragment.newInstance();
                    Bundle videoBundle = new Bundle();
                    videoBundle.putString("videoUrl", p.getVideoUrl());
                    videoBundle.putLong("videoPosition", exoPlayer.getCurrentPosition());
                    dialogFragment.setArguments(videoBundle);
                    exoPlayer.setPlayWhenReady(false);
                    dialogFragment.getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
                        if (event == Lifecycle.Event.ON_STOP) {
                            exoPlayer.seekTo(dialogFragment.newExoPlayer.getCurrentPosition());
                            exoPlayer.setPlayWhenReady(true);
                            dialogFragment.playerView.setPlayer(null);
                            dialogFragment.newExoPlayer.release();
                            dialogFragment.newExoPlayer = null;
                        }
                    });
                    dialogFragment.show(getChildFragmentManager(), "fullScreen");
                });

                playerView.setPlayer(exoPlayer);
                promotionDescTitleTv.setVisibility(View.VISIBLE);
                promoInfoTv.setVisibility(View.VISIBLE);
            } else {
                promoVideoCardView.setVisibility(View.GONE);
                promotionDescTitleTv.setVisibility(View.GONE);
                promoInfoTv.setVisibility(View.GONE);
                viewPager.setVisibility(View.GONE);
            }
        }

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {


            favPromosId = new ArrayList<>();


            usersRef.whereEqualTo("userId", currentUserUid).limit(1).get().addOnCompleteListener(task -> {
                alreadyFav = false;

                currentDs = task.getResult().getDocuments().get(0);

                favPromosId = (List<Long>) currentDs.get("favpromosids");

                if (favPromosId.size() != 0) {
                    if (favPromosId.contains(p.getPromoid())) {
                        alreadyFav = true;
                        favImage.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.promo_icon_round_red));
                    }
                }

                message_img.setOnClickListener(view1 -> {
                    Intent messagingIntent = new Intent(getContext(), MessagingActivity.class);
                    messagingIntent.putExtra("promouserid", p.getUid());
//                    messagingIntent.putExtra("currentuserid", auth.getCurrentUser().getUid());
                    messagingIntent.putExtra("promodocumentid", viewedDocumentID);
                    messagingIntent.putExtra("intendedpromoid", p.getPromoid());

                    startActivity(messagingIntent);
                });

                favImage.setOnClickListener(v -> {
                    favImage.setClickable(false);
//                    favWasClicked = true;
                    String documentId = task.getResult().getDocuments().get(0).getId();

                    if (!alreadyFav) {
                        //Toast.makeText(getContext(), promoUserid, Toast.LENGTH_SHORT).show();
                        sendNotification();
                        favImage.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.promo_icon_round_red));
                        usersRef.document(documentId).update("favpromosids", FieldValue.arrayUnion(p.getPromoid())).addOnCompleteListener(task17 -> {
                            Toast.makeText(getContext(), "تمت الاضافة الى المفضلة!", Toast.LENGTH_SHORT).show();
                            alreadyFav = true;
                        });

                        addToFavCount();

                        addNotification("favourite");

                    } else {
                        subtractFromFavCount();
                        if (favouredDocumentId != null) {
                            notifRef.document(favouredDocumentId).delete();
                        } else {
                            notifRef.whereEqualTo("receiverId", p.getUid()).whereEqualTo("senderId", currentUserUid)
                                    .whereEqualTo("promoId", p.getPromoid()).whereEqualTo("type", "favourite").get().addOnCompleteListener(task14 -> {
                                if (!task14.getResult().isEmpty()) {
                                    notifRef.document(task14.getResult().getDocuments().get(0).getId()).delete();
                                }
                            });
                        }
                        favImage.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.promo_icon_round));
                        usersRef.document(documentId).update("favpromosids", FieldValue.arrayRemove(p.getPromoid())).addOnCompleteListener(task13 -> {
                            Toast.makeText(getContext(), "تمت الازالة من المفضلة!", Toast.LENGTH_SHORT).show();
                            alreadyFav = false;
                        });

                    }
                });

            });

            ratingBar.setIsIndicator(false);
            ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> ratingChanged = fromUser);
        } else {
            ratingBar.setIsIndicator(true);
            message_img.setOnClickListener(v -> showSigninDialog());
            favImage.setOnClickListener(v -> showSigninDialog());
        }


        usersRef.document(p.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            phonenum = documentSnapshot.getString("phonenum");
            dr = usersRef.document(documentSnapshot.getId());
            if (phonenum == null) {
                callImage.setBackgroundResource(R.drawable.promo_icon_round);
            } else {
                callImage.setOnClickListener(v -> {
                    final Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.phone_number_layout);
                    final TextView phoneTv = dialog.findViewById(R.id.phoneNumTv);
                    final ImageView copyImage = dialog.findViewById(R.id.copyImage);
                    phoneTv.setText(phonenum);
                    dialog.show();
                    copyImage.setOnClickListener(v1 -> {
                        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("تم نسخ رقم الهاتف!", phonenum);
                        clipboard.setPrimaryClip(clip);
                        dialog.dismiss();
                        Toast.makeText(getContext(), "تم نسخ رقم الهاتف الى الحافظة!", Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });
        deleteBtn.setClickable(false);
        deleteBtn.setVisibility(View.INVISIBLE);


        promotionRef.whereEqualTo("promoid", p.getPromoid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            DocumentSnapshot ds = queryDocumentSnapshots.getDocuments().get(0);
            promoviews = ds.getLong("viewcount");
            promotionViews.setText(promoviews + "");
            viewedDocumentID = ds.getId();
            if (p.getUid().equals(currentUserUid)) {
                deleteBtn.setVisibility(View.VISIBLE);
                deleteBtn.setClickable(true);

                deleteBtn.setOnClickListener(v -> {

                    Dialog dialog = new Dialog(getContext());
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setContentView(R.layout.delete_promo_alert_layout);
                    TextView closeButton = dialog.findViewById(R.id.delete_close);
                    TextView confirmButton = dialog.findViewById(R.id.delete_confirm);
                    closeButton.setOnClickListener(v12 -> {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "لم يتم حذف الإعلان!", Toast.LENGTH_SHORT).show();
                    });
                    confirmButton.setOnClickListener(v13 -> {

                        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "جاري حذف الإعلان",
                                "الرجاء الإنتظار!", true);
                        progressDialog.show();
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        chatsRef = firestore.collection("chats");
                        promotionRef.document(viewedDocumentID).delete().addOnSuccessListener(task110 -> {
                            promotionRef.document("promotionidnum").update("promoCount", FieldValue.increment(-1));
                            if (images != null && !images.isEmpty()) {
                                for (int i = 0; i < images.size(); i++) {
                                    storage.getReferenceFromUrl(images.get(i)).delete();
                                }
                            }
                            chatsRef.whereEqualTo("intendedpromoid", p.getPromoid()).get().addOnCompleteListener(task19 -> {
                                if (task19.isSuccessful() && !task19.getResult().isEmpty()) {
                                    List<DocumentSnapshot> documents = task19.getResult().getDocuments();
                                    for (DocumentSnapshot ds13 : documents) {
                                        chatsRef.document(ds13.getId()).delete();
                                    }
                                }
                                usersRef.whereArrayContains("favpromosids", Long.toString(p.getPromoid())).get().addOnCompleteListener(task18 -> {
                                    if (task18.isSuccessful()) {
                                        List<DocumentSnapshot> documents = task18.getResult().getDocuments();
                                        for (DocumentSnapshot ds12 : documents) {
                                            usersRef.document(ds12.getId()).update("favpromosids", FieldValue.arrayRemove(Long.toString(p.getPromoid())));
                                        }
                                    }
                                    notifRef.whereEqualTo("promoId", p.getPromoid()).get().addOnSuccessListener(task12 -> {
                                        if (!task12.isEmpty()) {
                                            List<DocumentSnapshot> documents = task12.getDocuments();
                                            for (DocumentSnapshot ds1 : documents) {
                                                notifRef.document(ds1.getId()).delete();
                                            }
                                        }
                                    });
                                });
                            });
                            progressDialog.dismiss();
                            dialog.dismiss();
                            dismiss();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "لقد فشل حذف الإعلان!", Toast.LENGTH_SHORT).show();
                            Log.d("deletePromoError", e.getMessage());
                            progressDialog.dismiss();
                            dialog.dismiss();
                            dismiss();
                        });


                    });
                    dialog.show();

                });

                ratingBar.setIsIndicator(true);
                favImage.setVisibility(View.INVISIBLE);
//                shareImage.setVisibility(View.INVISIBLE);
                callImage.setVisibility(View.INVISIBLE);

            } else if (auth.getCurrentUser() != null) {
                promotionRef.document(viewedDocumentID).update("viewcount", FieldValue.increment(1));
            }

            shareImage.setOnClickListener(v -> sharePromo());

            if (!viewedDocumentID.isEmpty()) {
                promotionRef.document(viewedDocumentID).collection("ratings")
                        .get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    if (!queryDocumentSnapshots1.isEmpty()) {
                        int size = 0;
                        double ratingSum = 0;
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots1) {
                            double currentRating = snapshot.getDouble("rating");
                            if (currentRating != 0) {
                                ratingSum += currentRating;
                                size++;
                            }
                        }
                        ratingNumberTv.setText("(" + size + ")");
                        ratingBar.setRating((float) (ratingSum / size));
                    }
                }).addOnFailureListener(e -> Log.d("ttt", e.toString()));
            }
        });


        if (p.getCurrency() != null && !p.getCurrency().isEmpty()) {
            promotionPrice.setText(p.getPrice() + " " + p.getCurrency());
        } else {
            promotionPrice.setText(String.format(Locale.getDefault(), "%,d", ((long) p.getPrice())));
        }
        promotionTitle.setText(p.getTitle());
        promotionPublish.setText(TimeConvertor.getTimeAgo(p.getPublishtime()));
        promotionId.setText(p.getPromoid() + "#");
        promoInfoTv.setText(p.getDescription());
        promoInfoTv2.setText(p.getDescription());
        if (!images.isEmpty()) {
//            promotionDescTitleTv2.setVisibility(View.GONE);
//            promoInfoTv2.setVisibility(View.GONE);

            PromotionViewPager viewPagerAdapter = new PromotionViewPager(getContext(), images);
            viewPager.setAdapter(viewPagerAdapter);
            viewPager.setPageMargin((int) (15 * GlobalVariables.getDensity()));

//            viewPager.setPadding(10,0,10,0);
            dotsCount = viewPagerAdapter.getCount();
            viewPager.setOffscreenPageLimit(dotsCount - 1);

            if (dotsCount > 1) {
                nextIv = view.findViewById(R.id.pagerPromoNextIv);
                previousIv = view.findViewById(R.id.pagerPromoPreviousIv);
                nextIv.setVisibility(View.VISIBLE);
                nextIv.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));
                previousIv.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() - 1));
            }

            dots = new ImageView[dotsCount];
            for (int i = 0; i < dotsCount; i++) {
                dots[i] = new ImageView(getContext());
                dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.promotion_slider_dots));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(10, 0, 10, 0);
                sliderLayout.addView(dots[i], params);
            }
            dots[0].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.promoton_active_slider));

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    for (int i = 0; i < dotsCount; i++) {
                        dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.promotion_slider_dots));
                    }
                    dots[position].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.promoton_active_slider));

                    if (position == 0) {
                        previousIv.setVisibility(View.GONE);
                    } else {
                        previousIv.setVisibility(View.VISIBLE);
                    }

                    if (position + 1 < dotsCount) {
                        nextIv.setVisibility(View.VISIBLE);
                    } else {
                        nextIv.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
//        else {
//
//            LinearLayout promotiondotsSlider = view.findViewById(R.id.promotiondotsSlider);
//
//
//        }

//        ImageView userImageIndicator = view.findViewById(R.id.userImageIndicator);
//        if (GlobalVariables.getProfileImageUrl() != null) {
//            Picasso.get().load(GlobalVariables.getProfileImageUrl()).fit().centerCrop().into(userImageIndicator);
//        }
        ((AppCompatActivity) getContext()).setSupportActionBar(view.findViewById(R.id.promotiontoolbar));
//        ((AppCompatActivity) getContext()).getSupportActionBar().setTitle("");
//
//        if (auth.getCurrentUser() != null) {
////            blockedUsers = ((HomeActivity) getActivity()).blockedUsers;
//            userImageIndicator.setOnClickListener(v -> ((HomeActivity) getActivity()).showDrawer());
//        } else {
//            userImageIndicator.setOnClickListener(v -> showSigninDialog());
//        }


        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (auth.getCurrentUser() != null) {
                ratingChanged = fromUser;
            }
        });
        if (p.getType().equals("كمبيوتر و لاب توب") || p.getType().equals("اليكترونيات")) {
            relatedQuery = promotionRef
                    .whereIn("type", Arrays.asList("كمبيوتر و لاب توب", "اليكترونيات"));
        } else {
            relatedQuery = promotionRef
                    .whereEqualTo("type", p.getType());
        }
        relatedQuery =
                relatedQuery.whereEqualTo("isBanned", false)
                        .orderBy("publishtime", Query.Direction.DESCENDING)
                        .limit(14);


        lv.setNestedScrollingEnabled(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        lv.setLayoutManager(staggeredGridLayoutManager);
        promotions = new ArrayList<>();
        adapter = new StaggeredRecyclerAdapter(promotions);
        adapter.setHasStableIds(true);
        getUpdatedPromotions();
        lv.setAdapter(adapter);
        lv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!lv.canScrollVertically(View.SCROLL_AXIS_VERTICAL) && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (!isLoading) {
                        isLoading = true;
                        getUpdatedPromotions();
                    }
                }
            }
        });


//        PagedList.Config config = new PagedList.Config.Builder()
//                .setPageSize(4)
//                .build();
//
//        final FirestorePagingOptions<Promotion> options = new FirestorePagingOptions.Builder<Promotion>()
//                .setLifecycleOwner(this)
//                .setQuery(relatedQuery, config, Promotion.class)
//                .build();
//
//        FirestorePagingAdapter<Promotion, StaggeredViewHolder> mAdapter = new FirestorePagingAdapter<Promotion, StaggeredViewHolder>(options) {
//            @NonNull
//            @Override
//            public StaggeredViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                return new StaggeredViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.relativeitem, parent, false));
//            }
//
//            @Override
//            protected void onError(@NonNull Exception e) {
//                super.onError(e);
//            }
//
//            @Override
//            protected void onBindViewHolder(@NonNull StaggeredViewHolder staggeredViewHolder, int i, @NonNull Promotion promotion) {
//                staggeredViewHolder.bind(promotion, i,getContext());
//            }
//        };
//        mAdapter.setHasStableIds(true);
//        lv.setNestedScrollingEnabled(true);
//        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
//        lv.setLayoutManager(staggeredGridLayoutManager);
//        lv.setAdapter(mAdapter);
        lv.setPadding(15, 0, 15, (int) (GlobalVariables.getWindowWidth() / 1.5));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_user_account:
                if (auth.getCurrentUser() != null) {
                    Bundle bundle = new Bundle();
                    UserFragment fragment = new UserFragment();
                    bundle.putString("promouserid", p.getUid());
                    fragment.setArguments(bundle);

                    if (getContext() instanceof HomeActivity) {
                        ((HomeActivity) getContext()).replaceNonPromoFragment(fragment);
                    } else if (getContext() instanceof MessagingActivity) {
                        ((MessagingActivity) getContext()).replaceNonPromoFragment(fragment);
                    }
                    // ((HomeActivity) getContext()).replacePromoFragment(fragment);
                } else {
                    showSigninDialog();
                }
                return true;
            case R.id.report_this_promo:
                if (auth.getCurrentUser() != null) {
                    promotionRef.document(viewedDocumentID).get().addOnSuccessListener(documentSnapshot -> {
                        long currentTimeInMillies = System.currentTimeMillis() / 1000;
                        ArrayList<String> reports = (ArrayList<String>) documentSnapshot.get("reports");
                        if (reports != null) {
                            for (String report : reports) {
                                if (report.split("-")[0].equals(currentUserUid)) {
                                    Toast.makeText(getContext(), "لقد قمت بالإبلاغ عن هذا الاعلان من قبل!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            promotionRef.document(viewedDocumentID).update("reports", FieldValue.arrayUnion(currentUserUid + "-" + currentTimeInMillies)).addOnSuccessListener(aVoid -> {
                                reports.add(currentUserUid + "-" + currentTimeInMillies);
                                if (reports.size() >= 10) {
                                    if (((currentTimeInMillies - Long.parseLong(reports.get(reports.size() - 10).split("-")[1])) / 30) < 86400) {
                                        promotionRef.document(viewedDocumentID).update("isBanned", true);
                                        dismiss();
                                    }
                                }
                                Toast.makeText(getContext(), "لقد تم الإبلاغ عن هذا الإعلان!", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            promotionRef.document(viewedDocumentID).update("reports", FieldValue.arrayUnion(currentUserUid + "-" + currentTimeInMillies)).addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "لقد تم الإبلاغ عن هذا الإعلان!", Toast.LENGTH_SHORT).show());
                        }
                    });
                } else {
                    showSigninDialog();
                }
                return true;

            case R.id.message_publisher:
                if (auth.getCurrentUser() != null) {
                    Intent messagingIntent = new Intent(getContext(), MessagingActivity.class);
                    messagingIntent.putExtra("promouserid", p.getUid());
//                    messagingIntent.putExtra("currentuserid", auth.getCurrentUser().getUid());
                    messagingIntent.putExtra("intendedpromoid", p.getPromoid());
                    startActivity(messagingIntent);
                } else {
                    showSigninDialog();
                }
                return true;
            case R.id.block_user:
                if (auth.getCurrentUser() != null) {
                    ArrayList<String> usersBlocked = (ArrayList<String>) currentDs.get("usersBlocked");
                    if (usersBlocked != null) {
                        if (!usersBlocked.contains(p.getUid())) {
                            blockUser();

                        } else {
                            Toast.makeText(getContext(), "لقد تم حظر هذا المستخدم من قبل!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        blockUser();
                    }
                } else {
                    showSigninDialog();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    public boolean isLoggedIn() {
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        return accessToken != null;
//    }
//
//    private void addRatingSubCollection() {
//        PromoRating promoRating = new PromoRating();
//        promoRating.setUserid(auth.getCurrentUser().getUid());
//        promoRating.setRating();
//        promotionRef.document(viewedDocumentID).collection("ratings").add(promoRating);
//    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (currentUserUid != null && !p.getUid().equals(currentUserUid)) {
            inflater.inflate(R.menu.promotion_menu, menu);
        }
    }

    private void sendNotification() {

        usersRef.whereEqualTo("userId", currentUserUid).limit(1).get().addOnSuccessListener(snapshots -> {
            DocumentSnapshot ds = snapshots.getDocuments().get(0);
            String userName = ds.getString("username");
            Data data = new Data(currentUserUid, R.mipmap.ic_launcher, userName + " قام بالاعجاب باعلانك ", "تسجيل اعحاب", currentUserUid, ds.getString("imageurl"), userName);
            dr.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.contains("token")) {
                    Sender sender = new Sender(data, documentSnapshot.getString("token"));
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            });
        });
    }

    private void addNotification(String type) {

        notifRef.whereEqualTo("promoId", p.getPromoid()).
                whereEqualTo("receiverId", p.getUid())
                .whereEqualTo("senderId", auth.getCurrentUser().getUid())
                .whereEqualTo("type", type).get().addOnSuccessListener(snapshots -> {
            if (snapshots.isEmpty()) {
                Notification notification = new Notification();
                notification.setPromoId(p.getPromoid());

                notification.setSenderId(auth.getCurrentUser().getUid());
                notification.setReceiverId(p.getUid());
                notification.setType(type);
                notification.setTimeCreated(System.currentTimeMillis() / 1000);
                notification.setSeen(false);
                notifRef.add(notification).addOnSuccessListener(documentReference -> favouredDocumentId = documentReference.getId());
            }
        });
    }


    private void showSigninDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.signin_alert_layout);
        TextView closeOption = dialog.findViewById(R.id.alert_close);
        TextView signinOption = dialog.findViewById(R.id.alert_signin);
        dialog.show();
        closeOption.setOnClickListener(v -> dialog.cancel());

        signinOption.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), WelcomeActivity.class);
            startActivity(i);
        });
    }

//    public void onBackPressed() {
//        Toast.makeText(getContext(), "fasdfsdf  ", Toast.LENGTH_SHORT).show();
//        if(playerView!=null) {
//            if (playerView.getPlayer() != null) {
//                playerView.setPlayer(null);
//                exoPlayer.release();
//                exoPlayer = null;
//            }
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            playerView.setPlayer(null);
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
        updatePromoRating();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        updatePromoRating();
    }

    private void addToFavCount() {
        promotionRef.document(viewedDocumentID).update("favcount", FieldValue.increment(1)).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                favImage.setClickable(true);
            }
        });
//        promotionRef.document(viewedDocumentID).get().addOnCompleteListener(task -> promotionRef.document(viewedDocumentID).update("favcount", (long) task.getResult().get("favcount") + 1));
    }

    private void subtractFromFavCount() {
        promotionRef.document(viewedDocumentID).update("favcount", FieldValue.increment(-1)).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                favImage.setClickable(true);
            }
        });
//        promotionRef.document(viewedDocumentID).get().addOnCompleteListener(task -> promotionRef.document(viewedDocumentID).update("favcount", (long) task.getResult().get("favcount") - 1));
    }

//    public void showImageFullScreen(String imageUrl) {
//        Dialog imageDialog = new Dialog(getContext());
//        imageDialog.setCanceledOnTouchOutside(true);
//        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        imageDialog.setContentView(R.layout.full_screen_layout);
//        imageDialog.show();
//        ImageView fillScreenTv = imageDialog.findViewById(R.id.fillScreenTv);
//        Picasso.get().load(imageUrl).into(fillScreenTv);
//    }

    private void getUpdatedPromotions() {

        int oldPosition = promotions.size();
        Query updatedQuery;
        if (lastResult == null) {
            updatedQuery = relatedQuery.limit(4);
        } else {
            updatedQuery = relatedQuery.startAfter(lastResult).limit(4);
        }
        updatedQuery.get().addOnSuccessListener(snapshots -> {
            if (GlobalVariables.getBlockedUsers() != null && !GlobalVariables.getBlockedUsers().isEmpty()) {
                for (QueryDocumentSnapshot snap : snapshots) {
                    if (GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) continue;
                    promotions.add(snap.toObject(Promotion.class));
                }
            } else {
                if (lastResult == null) {
                    for (QueryDocumentSnapshot snap : snapshots) {
                        if (snap.getLong("promoid") == p.getPromoid()) continue;
                        promotions.add(snap.toObject(Promotion.class));
                    }
                } else {
                    for (QueryDocumentSnapshot snap : snapshots) {
                        promotions.add(snap.toObject(Promotion.class));
                    }
                }
            }

            adapter.notifyItemRangeInserted(oldPosition, promotions.size() - oldPosition);
            if (snapshots.size() > 0) {
                lastResult = snapshots.getDocuments().get(snapshots.size() - 1);
            }
            isLoading = false;
        });
    }

    void blockUser() {
        usersRef.document(currentDs.getId()).update("usersBlocked", FieldValue.arrayUnion(p.getUid())).addOnSuccessListener(aVoid -> {
            GlobalVariables.getBlockedUsers().add(p.getUid());
            Toast.makeText(getContext(), "لقد تم حظر هذا المستخدم!", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        });
    }

    void updatePromoRating() {

        if (auth.getCurrentUser() != null) {
            if (ratingChanged) {
                new Thread(() -> promotionRef.document(viewedDocumentID).collection("ratings").whereEqualTo("userid", currentUserUid).limit(1).get().addOnSuccessListener(snapshots -> {
                    if (snapshots.getDocuments().isEmpty()) {
                        PromoRating promoRating = new PromoRating();
                        promoRating.setUserid(auth.getCurrentUser().getUid());
                        promoRating.setRating(ratingBar.getRating());
                        updatePromoRating();
                        promotionRef.document(viewedDocumentID).collection("ratings").add(promoRating)
                                .addOnSuccessListener(documentReference -> addNotification("rating"));
                    } else {
                        promotionRef.document(viewedDocumentID).collection("ratings").
                                document(snapshots.getDocuments().get(0).getId())
                                .update("rating", ratingBar.getRating()).addOnSuccessListener(aVoid -> {
                            addNotification("rating");
                            promotionRef.document(viewedDocumentID).collection("ratings")
                                    .get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                                if (!queryDocumentSnapshots1.isEmpty()) {
                                    int size = 0;
                                    double ratingSum = 0;
                                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots1) {
                                        double currentRating = snapshot.getDouble("rating");
                                        if (currentRating != 0) {
                                            ratingSum += currentRating;
                                            size++;
                                        }
                                    }
                                    promotionRef.document(viewedDocumentID).update("rating", ratingSum / size);
                                }
                            }).addOnFailureListener(e -> Log.d("ttt", e.toString()));
                        });
                    }
                })).start();
            }
        }


    }

    void sharePromo(){
//        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.rbeno_logo_png);
//        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/Share.png";
//        OutputStream out;
//        File file = new File(path);
//        try {
//            out = new FileOutputStream(file);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//            out.flush();
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        path=file.getPath();
//        Uri bmpUri = Uri.parse("file://"+path);

//        Bitmap b =BitmapFactory.decodeResource(getResources(),R.drawable.rbeno_logo_png);
//        Intent share = new Intent(Intent.ACTION_SEND);
//        share.setType("image/webp");
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
//                b, "Title", null);
//        Uri imageUri =  Uri.parse(path);
//
//        Drawable mDrawable = getResources().getDrawable(R.drawable.rbeno_logo_png);
//        Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();
//
//        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), mBitmap, "Image Description", null);
//        Uri uri = Uri.parse(path);
//        String imageUri = "drawable://" + R.drawable.rbeno_logo_png;
//        File file = new File(imageUri);
//        Uri uri = Uri.fromFile(file);
//        shareIntent.setType("image/WEBP");
//        final File photoFile = new File("android.resource://com.example.yousef.rbenoapplication/" + R.drawable.rbeno_logo_png);
//        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photoFile));
//        Uri bmpUri = getBitmapFromDrawable(((BitmapDrawable) getResources().getDrawable(R.drawable.rbeno_logo_png)).getBitmap());
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,R.string.app_name);
        shareIntent.putExtra(Intent.EXTRA_TEXT,p.getTitle()+" - "+p.getPrice()+" "+p.getCurrency()+"\n"+p.getDescription());

        if(p.getPromoimages()!=null && !p.getPromoimages().isEmpty()){
//            shareIntent.putExtra(Intent.EXTRA_STREAM, p.getPromoimages().get(0));
            Picasso.get().load(p.getPromoimages().get(0)).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    shareIntent.putExtra(Intent.EXTRA_STREAM,getLocalBitmapUri(bitmap));
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }else if(p.getVideoThumbnail()!=null && !p.getVideoThumbnail().isEmpty()){
            Picasso.get().load(p.getVideoThumbnail()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    shareIntent.putExtra(Intent.EXTRA_STREAM,getLocalBitmapUri(bitmap));
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
//            shareIntent.putExtra(Intent.EXTRA_STREAM,p.getVideoThumbnail());
//                Uri bmpUri = getBitmapFromDrawable(drawableImage);// see previous remote images section and notes for API > 23
//                // Construct share intent as described above based on bitmap
//                shareIntent = new Intent();
//                shareIntent.setAction(Intent.ACTION_SEND);
//                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
//                shareIntent.setType("image/*");
        }else{
            shareIntent.putExtra(Intent.EXTRA_STREAM,getLocalBitmapUri(BitmapFactory.decodeResource(getResources(),
                    R.drawable.rbeno_logo_png)));
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        }

    }

//    public Uri getBitmapFromDrawable(Bitmap bmp){
//
//        Uri bmpUri = null;
//        try {
//            File file =  new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
//            FileOutputStream out = new FileOutputStream(file);
//            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
//            out.close();
//
//            bmpUri = FileProvider.getUriForFile(getActivity(), "com.example.yousef.rbenoapplication", file);  // use this version for API >= 24
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bmpUri;
//    }
public Uri getLocalBitmapUri(Bitmap bmp) {
    Uri bmpUri = null;
    try {
        File file =  new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
        FileOutputStream out = new FileOutputStream(file);

        bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
        out.close();
        bmpUri = FileProvider.getUriForFile(
                getContext(),
                "com.example.yousef.rbenoapplication.provider",
                file);
//        bmpUri = Uri.fromFile(file);
    } catch (IOException e) {
        e.printStackTrace();
    }
    return bmpUri;
}
}
