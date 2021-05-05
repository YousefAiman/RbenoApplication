package com.example.yousef.rbenoapplication;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryPromotionsFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
        VideosAdapter.VideoViewClickListener {

    private static final int PAGE_SIZE = 6;

    private static final int videoQueryLimit = 4;
    private final ArrayList<Promotion> videoPromotionsAllVideo = new ArrayList<>(), promotions = new ArrayList<>();
    private int itemsAdded = 0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView newestPromosRv, videosRv;
    private Query carsQuery;
    private DocumentSnapshot lastResult = null;
    private NewestPromosAdapter adapter;
    private boolean isLoading = true;
    private TextView noCarPromosTv;
    private ArrayList<String> videoUrlsAdapter;
    private VideosAdapter videosAdapter;
    //  private List<String> allButFirstTen;
    private NestedScrollView scrollview;
    private ViewTreeObserver.OnScrollChangedListener scrollChangedListener;
    private PromotionDeleteReceiver promotionDeleteReceiver;
    private String[] categories;
    private String category;

    public CategoryPromotionsFragment(String[] categories) {
        this.categories = categories;
    }

    public CategoryPromotionsFragment(String category) {
        this.category = category;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        carsQuery = FirebaseFirestore.getInstance().collection("promotions");

        if (GlobalVariables.getInstance().getCountryCode() != null) {
            carsQuery = carsQuery.whereEqualTo("country",
                    GlobalVariables.getInstance().getCountryCode().toUpperCase());
        }

        if (categories != null) {
            carsQuery = carsQuery.whereIn("type", Arrays.asList(categories));
        } else {
            carsQuery = carsQuery.whereEqualTo("type", category);
        }

        carsQuery = carsQuery.whereEqualTo("isBanned", false)
                .whereEqualTo("isPaused", false);

        carsQuery = carsQuery.orderBy("publishtime", Query.Direction.DESCENDING).limit(PAGE_SIZE);


        videoUrlsAdapter = new ArrayList<>();
        videosAdapter = new VideosAdapter(videoPromotionsAllVideo, this);

        adapter = new NewestPromosAdapter(promotions,
                getContext(), R.layout.newest_promo_item_grid, 3);
        adapter.setHasStableIds(true);

    }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_category_promotions, container, false);

      setupDeletionReceiver();

      newestPromosRv = view.findViewById(R.id.newestPromosRv);
      videosRv = view.findViewById(R.id.videosRv);

      swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
      noCarPromosTv = view.findViewById(R.id.noPromosTv);

      swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red));
      scrollview = view.findViewById(R.id.scrollview);
      scrollview.setNestedScrollingEnabled(true);


      swipeRefreshLayout.setOnRefreshListener(this);
      swipeRefreshLayout.setRefreshing(true);
//  ((NestedScrollView)view.findViewById(R.id.scrollview)).setNestedScrollingEnabled(true);

      videosRv.setLayoutManager(new LinearLayoutManager(getContext(),
              RecyclerView.HORIZONTAL, false) {
          @Override
          public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
              lp.width = (int) (getHeight() * 0.55);
              return true;
          }
      });

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
              if (newestPromosRv.getAdapter().getItemViewType(position) == 2) {
                  return 2;
              }
              return 1;
          }
      });

      newestPromosRv.setLayoutManager(glm);


      return view;
  }


  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);


      videosRv.setAdapter(videosAdapter);


      newestPromosRv.setAdapter(adapter);


      getUpdatedPromotions();


//    newestPromosRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
//      @Override
//      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//        super.onScrolled(recyclerView, dx, dy);
//        if (!newestPromosRv.canScrollVertically(1) && dy > 0) {
//          Log.d("ttt","category scrolled to bottom");
//          if (!isLoading) {
//            Log.d("ttt","loading more");
//            getUpdatedPromotions();
//          }
//        }
//      }
//
//      @Override
//      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//        super.onScrollStateChanged(recyclerView, newState);
//        Log.d("ttt","scroll state changed");
//      }
//    });

      getAllVideos();

  }

  @Override
  public void onRefresh() {
      if (WifiUtil.checkWifiConnection(getContext())) {
          videoPromotionsAllVideo.clear();
          promotions.clear();
          videosAdapter.notifyDataSetChanged();
          adapter.notifyDataSetChanged();
          lastResult = null;
          getAllVideos();
          getUpdatedPromotions();
      } else {
          swipeRefreshLayout.setRefreshing(false);
      }
  }

    void addScrollListener() {
        scrollview.getViewTreeObserver()
                .addOnScrollChangedListener(scrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
                    float y = 0;

                    @Override
                    public void onScrollChanged() {
                        if (!scrollview.canScrollVertically(View.SCROLL_AXIS_VERTICAL) && scrollview.getScrollY() > y) {
                            if (!isLoading) {
                                getUpdatedPromotions();
                            }
                        }
                        y = scrollview.getScrollY();
                    }
                });
    }

    void removeScrollListener() {
        scrollview.getViewTreeObserver().removeOnScrollChangedListener(scrollChangedListener);
    }

    private void getUpdatedPromotions() {

        swipeRefreshLayout.setRefreshing(true);
        isLoading = true;
        itemsAdded = 0;

        Query updatedQuery = carsQuery;
        if (lastResult != null) {
            updatedQuery = updatedQuery.startAfter(lastResult);
        }

        updatedQuery.get().addOnSuccessListener(snapshots -> {

            if (GlobalVariables.getBlockedUsers().isEmpty()) {
                promotions.addAll(snapshots.toObjects(Promotion.class));
                itemsAdded += snapshots.size();
            } else {
                final List<DocumentSnapshot> snaps = snapshots.getDocuments();

                for (DocumentSnapshot snap : snaps) {
                    if (GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
                        continue;
                    }
                    promotions.add(snap.toObject(Promotion.class));
                    itemsAdded++;
                }
            }
//      if (categories.length == 1) {
//        if (GlobalVariables.getBlockedUsers().size() > 10) {
//
//          final List<DocumentSnapshot> snaps = snapshots.getDocuments();
//
//          for (DocumentSnapshot snap : snaps) {
//            if (allButFirstTen.contains(snap.getString("uid"))) {
//              continue;
//            }
//            promotions.add(snap.toObject(Promotion.class));
//            itemsAdded++;
//          }
//
//        } else {
//          promotions.addAll(snapshots.toObjects(Promotion.class));
//          itemsAdded += snapshots.size();
//        }
//
//      } else {
//
//        final List<DocumentSnapshot> snaps = snapshots.getDocuments();
//
//        for (DocumentSnapshot snap : snaps) {
//          if (GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
//            continue;
//          }
//          promotions.add(snap.toObject(Promotion.class));
//          itemsAdded++;
//        }
//
//      }
//

//      if (categories.length == 0) {
//
//          if(GlobalVariables.getBlockedUsers().size() > 10){
//            final List<DocumentSnapshot> documentSnapshots = snapshots.getDocuments();
//
//            for (DocumentSnapshot snap : documentSnapshots) {
//              if (GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
//                continue;
//              }
//              promotions.add(snap.toObject(Promotion.class));
//              itemsAdded++;
//            }
//
//          }else{
//            promotions.addAll(snapshots.toObjects(Promotion.class));
//            itemsAdded+= snapshots.size();
//          }
//
//
//      } else {
//        final List<DocumentSnapshot> documentSnapshots = snapshots.getDocuments();
//        if (GlobalVariables.getBlockedUsers().size() <= 10) {
//          promotions.addAll(snapshots.toObjects(Promotion.class));
//          itemsAdded+= snapshots.size();
//        } else {
//          for (DocumentSnapshot snap : documentSnapshots) {
//            if (GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
//              continue;
//            }
//            promotions.add(snap.toObject(Promotion.class));
//            itemsAdded++;
//          }
//        }
//      }
            if (snapshots.size() > 0) {
                lastResult = snapshots.getDocuments().get(snapshots.size() - 1);
            }

        }).addOnCompleteListener(task -> {

            if (itemsAdded > 0) {

                if (itemsAdded == PAGE_SIZE && promotions.size() - itemsAdded == 0) {
                    addScrollListener();
                }

                noCarPromosTv.setVisibility(View.GONE);
                newestPromosRv.setVisibility(View.VISIBLE);
                Log.d("ttt", "updating adapter from: " +
                        (promotions.size() - itemsAdded) + " to " + itemsAdded);

                adapter.notifyItemRangeInserted(
                        promotions.size() - itemsAdded, itemsAdded);


            } else {

                if (scrollChangedListener != null) {
                    removeScrollListener();
                }

                if (promotions.size() == 0) {
                    noCarPromosTv.setVisibility(View.VISIBLE);
                    newestPromosRv.setVisibility(View.GONE);
                }
            }


            isLoading = false;
            swipeRefreshLayout.setRefreshing(false);

//      updatedQuery.addSnapshotListener((value, error) -> {
//        if (value != null) {
//          for (DocumentChange documentChange : value.getDocumentChanges()) {
//            if (documentChange.getType() == DocumentChange.Type.REMOVED) {
//              long removedPromoId = documentChange.getDocument().getLong("promoid");
//              for (Promotion promo : promotions) {
//                if (promo.getPromoid() == removedPromoId) {
//                  promotions.remove(promo);
//                  break;
//                }
//              }
//            }
//          }
//        }
//      });
        });
    }

    void getAllVideos() {

        Query query = carsQuery.whereEqualTo("promoType", "video").limit(videoQueryLimit);

        if (GlobalVariables.getInstance().getCountryCode() != null) {
            query = query.whereEqualTo("country",
                    GlobalVariables.getInstance().getCountryCode().toUpperCase());
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            final List<DocumentSnapshot> snaps = queryDocumentSnapshots.getDocuments();

            if (GlobalVariables.getBlockedUsers().isEmpty()) {
                for (DocumentSnapshot snap : snaps) {
                    final Promotion promotion = snap.toObject(Promotion.class);
                    videoPromotionsAllVideo.add(promotion);
                    videoUrlsAdapter.add(promotion.getVideoUrl());
                }
            } else {
                for (DocumentSnapshot snap : snaps) {
                    if (!GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
                        final Promotion promotion = snap.toObject(Promotion.class);
                        videoPromotionsAllVideo.add(promotion);
                        videoUrlsAdapter.add(promotion.getVideoUrl());
                    }
                }
            }

//      if (categories.length == 0) {
//
//        if(GlobalVariables.getBlockedUsers().isEmpty()){
//          for (DocumentSnapshot snap : snaps) {
//            final Promotion promotion = snap.toObject(Promotion.class);
//            videoPromotionsAllVideo.add(promotion);
//            videoUrlsAdapter.add(promotion.getVideoUrl());
//          }
//        }else{
//          for (DocumentSnapshot snap : snaps) {
//            if (!GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
//              final Promotion promotion = snap.toObject(Promotion.class);
//              videoPromotionsAllVideo.add(promotion);
//              videoUrlsAdapter.add(promotion.getVideoUrl());
//            }
//          }
//        }
//
//      } else {
//        for (DocumentSnapshot snap : snaps) {
//          if (!GlobalVariables.getBlockedUsers().contains(snap.getString("uid"))) {
//            final Promotion promotion = snap.toObject(Promotion.class);
//            videoPromotionsAllVideo.add(promotion);
//            videoUrlsAdapter.add(promotion.getVideoUrl());
//          }
//        }
//      }


        }).addOnCompleteListener(task -> {
            if (videoPromotionsAllVideo.size() > 0) {
                videosAdapter.notifyDataSetChanged();
                videosRv.setVisibility(View.VISIBLE);
            } else {
                videosRv.setVisibility(View.GONE);
            }

        });
    }

    @Override
    public void videoViewClickListener(int position) {
        if (videoPromotionsAllVideo.size() > videoUrlsAdapter.size()) {
            if (!videoUrlsAdapter.contains(videoPromotionsAllVideo.get(position).getVideoUrl())) {
                videoPromotionsAllVideo.remove(position);
                return;
            }
        }

//    final VideoPagerFragment videoPagerFragment = VideoPagerFragment.newInstance();
//    final Bundle videoBundle = new Bundle();

        final ArrayList<Promotion> videoPromotionItemsInstance =
                new ArrayList<>(videoPromotionsAllVideo);

        if (position != 0) {
            final Promotion firstPromo = videoPromotionItemsInstance.get(0);
            videoPromotionItemsInstance.set(0, videoPromotionItemsInstance.get(position));
            videoPromotionItemsInstance.set(position, firstPromo);
        }

//    videoPromotionItemsInstance.remove(position);
//    videoPromotionItemsInstance.add(0, videoPromotionsAllVideo.get(position));
//    videoBundle.putSerializable("videoPromotions", videoPromotionItemsInstance);

        VideoPagerFragment videoPagerFragment = new VideoPagerFragment(videoPromotionItemsInstance);

//    videoPagerFragment.setArguments(videoBundle);
        ((HomeActivity) getActivity()).addFragmentToHomeContainer(videoPagerFragment);
//    videoPagerFragment.show(getChildFragmentManager(), "fullScreen");
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
                        final long id = intent.getLongExtra("promoId", 0);
                        checkAndDeletePromoFromList(promotions, id);
                        checkAndDeletePromoFromList(videoPromotionsAllVideo, id);
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

