package com.example.yousef.rbenoapplication;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NewestPromosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private final static int USER_TYPE = 2, AD_TYPE = 2, PREVIEW_TYPE = 4, TEXT_TYPE = 3, IMAGE_TYPE = 1;
  private final ArrayList<Promotion> newpromotions;
  private final Context context;

  private final List<String> banners =
          Arrays.asList("6990486336142688/8279246162", "6990486336142688/3624974840",
                  "6990486336142688/2040430436", "6990486336142688/6909613736",
                  "6990486336142688/6272347151", "6990486336142688/4767693796",
                  "6990486336142688/1949958763", "6990486336142688/1465715369",
                  "6990486336142688/1758387074", "6990486336142688/6819142067");

  private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
  private final int layoutId, type;

  NewestPromosAdapter(ArrayList<Promotion> newpromotions, Context context, int layoutId, int type) {
    this.newpromotions = newpromotions;
    this.context = context;
    this.layoutId = layoutId;
    this.type = type;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public int getItemViewType(int position) {

    if (newpromotions.get(position).getTitle() == null) {
      return PREVIEW_TYPE;
    } else {
      if (position + 1 % 10 == 0) {
        return AD_TYPE;
      } else if ((type == TEXT_TYPE || type == 1) &&
              newpromotions.get(position).getPromoType().equals("text")) {
        return TEXT_TYPE;
      } else {
        return IMAGE_TYPE;
      }
    }

  }


  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    switch (viewType) {

      case IMAGE_TYPE:
        return new newestpromosviewholder(LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false));
      case AD_TYPE:
        return new newestpromosadviewholder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.newestpromosaditemdesign, parent, false));
      case PREVIEW_TYPE:
        return new previewViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.newest_promo_item_grid_preview, parent, false));
      default:
        return new newestpromosviewholder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.newest_promo_text_item_grid, parent, false));

    }
  }


  @Override
  public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
    final Promotion p = newpromotions.get(position);

    if (holder.getItemViewType() == PREVIEW_TYPE) {
      return;
    }

    if (holder.getItemViewType() == AD_TYPE) {
      newestpromosadviewholder vh3 = ((newestpromosadviewholder) holder);
      final AdView mAdView = new AdView(context);
      holder.itemView.setVisibility(View.GONE);
//        notifyItemRemoved(position);
      holder.itemView.setLayoutParams(new GridLayoutManager.LayoutParams(0, 0));
      mAdView.setAdSize(AdSize.BANNER);
      mAdView.setAdUnitId("ca-app-pub-" + banners.get(new Random().nextInt(banners.size())));


      mAdView.loadAd(new AdRequest.Builder().build());
      mAdView.setAdListener(new AdListener() {
        @Override
        public void onAdLoaded() {
          super.onAdLoaded();
          holder.itemView.setVisibility(View.VISIBLE);
          holder.itemView.setLayoutParams(new GridLayoutManager
                  .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.WRAP_CONTENT));
          Log.d("ttt", "ad loaded in adapter");
        }
      });
      vh3.adLayout.addView(mAdView);
      return;
    }

    final newestpromosviewholder vh1 = (newestpromosviewholder) holder;
    if (holder.getItemViewType() == TEXT_TYPE) {
      vh1.bindTextPromo(p);
    } else {
      vh1.bind(p);
    }

    vh1.itemView.setOnClickListener(v -> {


      if (Promotion.printPromoStatus(context, p, user.getUid())) {

        vh1.makePromoDeleted();

        return;
      }


//      final Bundle bundle = new Bundle();
//      final PromotionInfoFragment frag = new PromotionInfoFragment(p);
//      bundle.putSerializable("promo", p);
//      frag.setArguments(bundle);
      if (context instanceof HomeActivity) {
//            ((HomeActivity) context).pauseVideo();
        ((HomeActivity) context).addFragmentToHomeContainer(new PromotionInfoFragment(p));
      } else if (context instanceof MessagingRealTimeActivity) {
        ((MessagingRealTimeActivity) context).addFragmentToHomeContainer(new PromotionInfoFragment(p));
      }
    });

    if (type == USER_TYPE) {
      final ImageView menuIv = holder.itemView.findViewById(R.id.myPrommoMenuIv);

      menuIv.setOnClickListener(view -> {

        if (Promotion.printPromoStatus(context, p, user.getUid())) {

          vh1.makePromoDeleted();

          return;
        }


        final PopupMenu userPromoMenu = new PopupMenu(context, menuIv);
        showMenuIcons(userPromoMenu);

        final int menu = p.getIsPaused() ?
                R.menu.user_promo_menu_item_paused : R.menu.user_promo_menu_item_unpaused;

        userPromoMenu.getMenuInflater().inflate(menu, userPromoMenu.getMenu());

        userPromoMenu.setOnMenuItemClickListener(menuItem -> {
          if (WifiUtil.checkWifiConnection(context)) {

            if (menuItem.getItemId() == R.id.delete_item) {
              Promotion.deletePromo(context, newpromotions.get(position));
            } else if (menuItem.getItemId() == R.id.pause_item) {
              Log.d("ttt", "pasued: " + p.getIsPaused());
              Promotion.pauseOrUnPausePromo(context, p, null, null);
            }
          }
          return true;
        });
        userPromoMenu.show();
      });
    }

    vh1.myPromoHeartIv.setOnClickListener(view -> {

      if (user.isAnonymous()) {
        SigninUtil.getInstance(context, (Activity) context).show();

        return;
      }

      if (Promotion.printPromoStatus(context, p, user.getUid())) {

        vh1.makePromoDeleted();

        return;
      }

      vh1.myPromoHeartIv.setClickable(false);

      Promotion.favOrUnFavPromo(context, p, user.getUid(), vh1.myPromoHeartIv, vh1.favTv,
              type == 1);

      if (type == 1) {
        final int index = newpromotions.indexOf(p);
        newpromotions.remove(index);
        notifyItemRemoved(index);
      }

    });

  }

  @Override
  public int getItemCount() {
    return newpromotions.size();
  }

  void showMenuIcons(PopupMenu menu) {
    try {
      final Field[] fields = menu.getClass().getDeclaredFields();
      for (Field field : fields) {
        if ("mPopup".equals(field.getName())) {
          field.setAccessible(true);
          Object menuPopupHelper = field.get(menu);
          Class.forName(menuPopupHelper.getClass().getName())
                  .getMethod("setForceShowIcon", boolean.class)
                  .invoke(menuPopupHelper, true);
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static class newestpromosadviewholder extends RecyclerView.ViewHolder {
    AdView adView;
    ConstraintLayout adLayout;

    newestpromosadviewholder(@NonNull View itemView) {
      super(itemView);
      adView = itemView.findViewById(R.id.adView);
      adLayout = itemView.findViewById(R.id.adLayout);
    }
  }

  static class previewViewHolder extends RecyclerView.ViewHolder {

    previewViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }

}
