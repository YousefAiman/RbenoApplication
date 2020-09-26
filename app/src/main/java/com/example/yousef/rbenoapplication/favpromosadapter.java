package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

public class favpromosadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private ArrayList<Promotion> favpromotions;
    private int favPromoItemLayoutId, favPromoItemTextLayoutId;
    private Context context;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = firestore.collection("users");
    private Picasso picasso = Picasso.get();
    //    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private CollectionReference promoRef = firestore.collection("promotions");
    private String documentId;
    private Locale locale = Locale.getDefault();

    favpromosadapter(ArrayList<Promotion> favpromotions, int favPromoItemLayoutId, int favPromoItemTextLayoutId, Context context, String documentId) {
        this.favpromotions = favpromotions;
        this.favPromoItemLayoutId = favPromoItemLayoutId;
        this.context = context;
        this.favPromoItemTextLayoutId = favPromoItemTextLayoutId;
        this.documentId = documentId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1:
                return new favpromosviewholder(LayoutInflater.from(parent.getContext()).inflate(favPromoItemLayoutId, parent, false));
            case 2:
                return new favpromotextviewholder(LayoutInflater.from(parent.getContext()).inflate(favPromoItemTextLayoutId, parent, false));
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return favpromotions.get(position).hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {


        final Promotion p = favpromotions.get(position);
        switch (holder.getItemViewType()) {
            case 1:
                final favpromosviewholder vh1 = (favpromosviewholder) holder;

                if (p.getPromoimages() != null && !p.getPromoimages().isEmpty()) {
                    picasso.load(p.getPromoimages().get(0))
                            .fit().centerCrop().into(vh1.promoimage);
                    vh1.promoimage.setOnClickListener(v -> ((HomeActivity) context).showImageFullScreen(p.getPromoimages().get(0)));
                } else {
                    picasso.load(p.getVideoThumbnail())
                            .fit().centerCrop().into(vh1.promoimage);
                    vh1.promoimage.setOnClickListener(v -> ((HomeActivity) context).showImageFullScreen(p.getVideoThumbnail()));
                }
//              picasso.load(p.getPromoimages().get(0)).fit().centerCrop().into(vh1.promoimage);
                vh1.titleTv.setText(p.getTitle());
                vh1.categoryTv.setText(p.getType());
                vh1.ownerTv.setText(p.getUserName());
                vh1.priceTv.setText(String.format(locale, "%,d", ((long) p.getPrice())));
                vh1.favTv.setText(String.valueOf(p.getFavcount()));
                vh1.viewsTv.setText(String.valueOf(p.getViewcount()));
                vh1.promoidTv.setText("#" + p.getPromoid());
                vh1.currencyTv.setText(p.getCurrency());
                vh1.removeImageView.setOnClickListener(v ->


                        usersRef.document(documentId).update("favpromosids", FieldValue.arrayRemove(p.getPromoid())).addOnSuccessListener(task12 -> {
                            favpromotions.remove(p);
                            notifyItemRemoved(favpromotions.indexOf(p));
                            Toast.makeText(context, "تمت الازالة من المفضلة!", Toast.LENGTH_SHORT).show();
                            promoRef.whereEqualTo("promoid", p.getPromoid()).limit(1).get().addOnSuccessListener(task17 -> promoRef.document(task17.getDocuments().get(0).getId()).update("favcount", FieldValue.increment(-1)));
                        }));
                break;
            case 2:
                final favpromotextviewholder vh2 = (favpromotextviewholder) holder;

                vh2.titleTv.setText(p.getTitle());
                vh2.categoryTv.setText(p.getType());
                vh2.ownerTv.setText(p.getUserName());
                vh2.priceTv.setText(String.format(locale, "%,d", ((long) p.getPrice())));
                vh2.favTv.setText(String.valueOf(p.getFavcount()));
                vh2.viewsTv.setText(String.valueOf(p.getViewcount()));
                vh2.promoidTv.setText("#" + p.getPromoid());
                vh2.descTv.setText(p.getDescription());
                vh2.currencyTv.setText(p.getCurrency());
                vh2.removeImageView.setOnClickListener(v ->
                        usersRef.document(documentId).update("favpromosids", FieldValue.arrayRemove(p.getPromoid())).addOnSuccessListener(task12 -> {
                            favpromotions.remove(p);
                            notifyItemRemoved(favpromotions.indexOf(p));
                            Toast.makeText(context, "تمت الازالة من المفضلة!", Toast.LENGTH_SHORT).show();
                            promoRef.whereEqualTo("promoid", p.getPromoid()).limit(1).get().addOnSuccessListener(task17 -> promoRef.document(task17.getDocuments().get(0).getId()).update("favcount", FieldValue.increment(-1)));
                        }));

                break;
        }

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            PromotionInfoFragment fragment = new PromotionInfoFragment();
//            bundle.putString("promotitle", p.getTitle());
//            bundle.putString("promotype", p.getType());
//            bundle.putDouble("promoprice", p.getPrice());
//            bundle.putString("promocurrency", p.getCurrency());
//            bundle.putString("promodescription", p.getDescription());
//            bundle.putLong("promofavs", p.getFavcount());
//            bundle.putLong("promoid", p.getPromoid());
//            bundle.putDouble("promorating", p.getRating());
//            bundle.putLong("promopublish", p.getPublishtime());
//            bundle.putString("promoPromotype", p.getPromoType());
////            if (p.getPromoimages() != null && !p.getPromoimages().isEmpty()) {
////                bundle.putStringArrayList("promoimages", p.getPromoimages());
////            }
//            bundle.putLong("promoviews", p.getViewcount());
//            bundle.putString("promouser", p.getUid());
            bundle.putSerializable("promo", p);
            fragment.setArguments(bundle);
            ((HomeActivity) context).replacePromoFragment(fragment);
        });

    }

    @Override
    public int getItemCount() {
        return favpromotions.size();
    }

    @Override
    public int getItemViewType(int position) {
        Promotion p = favpromotions.get(position);
        if ((p.getPromoimages() != null && !p.getPromoimages().isEmpty()) || p.getVideoThumbnail() != null) {
            return 1;
        } else {
            return 2;
        }
//        if(favpromotions.get(position).getPromoimages() !=null){
//            if (!favpromotions.get(position).getPromoimages().isEmpty()) {
//                return 1;
//            } else {
//                return 2;
//            }
//        }else{
//            return 2;
//        }

    }
}
