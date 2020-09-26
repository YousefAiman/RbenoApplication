package com.example.yousef.rbenoapplication;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class newestpromosadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private ArrayList<Promotion> newpromotions;
    private Context context;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference notificationsRef = firestore.collection("notifications");
    private CollectionReference usersRef = firestore.collection("users");
    private ArrayList<Long> favPromosId = new ArrayList<>();
    private CollectionReference promoRef = firestore.collection("promotions");
    private CollectionReference chatsRef = firestore.collection("chats");
    private String userId;

    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    // Picasso picasso;
    private List<String> banners = Arrays.asList("6990486336142688/8279246162", "6990486336142688/3624974840", "6990486336142688/2040430436", "6990486336142688/6909613736",
            "6990486336142688/6272347151", "6990486336142688/4767693796", "6990486336142688/1949958763", "6990486336142688/1465715369", "6990486336142688/1758387074",
            "6990486336142688/6819142067");
    private FirebaseUser user = auth.getCurrentUser();


    newestpromosadapter(ArrayList<Promotion> newpromotions, Context context) {
        this.newpromotions = newpromotions;
        this.context = context;

        if (auth.getCurrentUser() != null) {
            userId = user.getUid();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position != 0 && position % 15 == 0) {
            return 3;
        } else {
            if (newpromotions.get(position).getPromoType().equals("text")) {
                return 2;
            } else {
                return 1;
            }
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1:
                return new newestpromosviewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.newestpromoitemdesign, parent, false));
            case 2:
                return new newestpromotextviewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.newestpromoitemdesigntext, parent, false));
            case 3:
                return new newestpromosadviewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.newestpromosaditemdesign, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final Promotion p = newpromotions.get(position);
        switch (holder.getItemViewType()) {
            case 1:
                final newestpromosviewholder vh1 = (newestpromosviewholder) holder;
                vh1.bind(p);
                vh1.promomenuimage.setOnClickListener(v -> {

                    if(newpromotions.indexOf(p) == -1){
                        notifyItemRemoved(position);
                        Toast.makeText(context, "لقد تم حذف هذا الإعلان!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (user == null) {
                        showSigninDialog();
                        return;
                    }
                    if (userId.equals(p.getUid())) {
                        PopupMenu userPromoMenu = new PopupMenu(context, vh1.promomenuimage);
                        showMenuIcons(userPromoMenu);
                        userPromoMenu.getMenuInflater().inflate(R.menu.userpromomenu,userPromoMenu.getMenu());

                        userPromoMenu.setOnMenuItemClickListener(menuItem -> {
                            deletePromo(position);
                            return true;
                        });
                        userPromoMenu.show();
                    } else {
                        usersRef.whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
                            DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                            favPromosId = (ArrayList<Long>) snapshot.get("favpromosids");

                            boolean alreadyFav = false;
                            final String documentId = snapshot.getId();
                            if (favPromosId.contains(p.getPromoid())) {
                                alreadyFav = true;
                            }

                            if (!alreadyFav) {
                                final PopupMenu MenuNonFav = new PopupMenu(context, vh1.promomenuimage);
                                showMenuIcons(MenuNonFav);
                                MenuNonFav.getMenuInflater().inflate(R.menu.vieweruserpromo,MenuNonFav.getMenu());
                                MenuNonFav.setOnMenuItemClickListener(item -> {
                                    switch (item.getItemId()) {
                                        case R.id.add_to_fav:
                                            usersRef.document(documentId).update("favpromosids", FieldValue.arrayUnion(p.getPromoid()))
                                                    .addOnSuccessListener(task1 -> Toast.makeText(context, "تمت الاضافة الى المفضلة!", Toast.LENGTH_SHORT).show());
                                            promoRef.whereEqualTo("promoid", p.getPromoid()).limit(1).get().addOnSuccessListener(task17 -> {
                                                promoRef.document(task17.getDocuments().get(0).getId()).update("favcount", FieldValue.increment(1));
                                            });
                                            return true;
                                        case R.id.report_promo:
                                            reportThisPromo(p.getPromoid());
                                            return true;
                                    }
                                    return false;
                                });
                                MenuNonFav.show();
                            } else {
                                final PopupMenu MenuFav = new PopupMenu(context, vh1.promomenuimage);
                                MenuFav.getMenuInflater().inflate(R.menu.vieweruserpromofav, MenuFav.getMenu());
                                showMenuIcons(MenuFav);
                                MenuFav.setOnMenuItemClickListener(item -> {
                                    switch (item.getItemId()) {
                                        case R.id.remove_fav_from_favlist:
                                            usersRef.document(documentId).update("favpromosids", FieldValue.arrayRemove(p.getPromoid())).addOnSuccessListener(task114 -> Toast.makeText(context, "تمت الازالة من المفضلة!", Toast.LENGTH_SHORT).show());
                                            promoRef.whereEqualTo("promoid", p.getPromoid()).limit(1).get().addOnSuccessListener(task17 -> {
                                                promoRef.document(task17.getDocuments().get(0).getId()).update("favcount", FieldValue.increment(-1));
                                            });
                                            return true;
                                        case R.id.report_promo:
                                            reportThisPromo(p.getPromoid());
                                            return true;
                                    }
                                    return false;
                                });
                                MenuFav.show();
                            }
                        });
                    }
                });
                vh1.itemView.setOnClickListener(v -> {

                    if(newpromotions.indexOf(p) == -1){
                        notifyItemRemoved(position);
                        Toast.makeText(context, "لقد تم حذف هذا الإعلان!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Bundle bundle = new Bundle();
                    PromotionInfoFragment frag = new PromotionInfoFragment();
                    bundle.putSerializable("promo", p);
                    frag.setArguments(bundle);
                    if (context instanceof HomeActivity) {
                        ((HomeActivity) context).pauseVideo();
                        ((HomeActivity) context).replacePromoFragment(frag);
                    } else if (context instanceof MessagingActivity) {
                        ((MessagingActivity) context).replacePromoFragment(frag);
                    }
                });
                break;
            case 2:
                final newestpromotextviewholder vh2 = (newestpromotextviewholder) holder;
                vh2.bind(p);
                vh2.promomenuimage.setOnClickListener(v -> {

                    if(newpromotions.indexOf(p) == -1){
                        notifyItemRemoved(position);
                        Toast.makeText(context, "لقد تم حذف هذا الإعلان!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (auth.getCurrentUser() != null) {
                        if (userId.equals(p.getUid())) {
                            PopupMenu userPromoMenu = new PopupMenu(context, vh2.promomenuimage);
                            showMenuIcons(userPromoMenu);
                            userPromoMenu.getMenuInflater().inflate(R.menu.userpromomenu,userPromoMenu.getMenu());

                            userPromoMenu.setOnMenuItemClickListener(menuItem -> {
                                deletePromo(position);
                                return true;
                            });

                            userPromoMenu.show();
                        } else {
                            usersRef.whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
                                DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                                favPromosId = (ArrayList<Long>) snapshot.get("favpromosids");
                                boolean alreadyFav = false;
                                final String documentId = snapshot.getId();
                                if (favPromosId.size() != 0) {
                                    if (favPromosId.contains(p.getPromoid())) {
                                        alreadyFav = true;
                                    }
                                }
                                if (!alreadyFav) {
                                    final PopupMenu viewerPromoMenuNonFav = new PopupMenu(context, vh2.promomenuimage);
                                    showMenuIcons(viewerPromoMenuNonFav);
                                    viewerPromoMenuNonFav.getMenuInflater().inflate(R.menu.vieweruserpromo, viewerPromoMenuNonFav.getMenu());
                                    viewerPromoMenuNonFav.setOnMenuItemClickListener(item -> {
                                        switch (item.getItemId()) {
                                            case R.id.add_to_fav:
                                                usersRef.document(documentId).update("favpromosids", FieldValue.arrayUnion(p.getPromoid())).addOnSuccessListener(task111 ->
                                                        Toast.makeText(context, "تمت الاضافة الى المفضلة!", Toast.LENGTH_SHORT).show());
                                                promoRef.whereEqualTo("promoid", p.getPromoid()).limit(1).get().addOnSuccessListener(task17 -> {
                                                    promoRef.document(task17.getDocuments().get(0).getId()).update("favcount", FieldValue.increment(1));
                                                });
                                                return true;
                                            case R.id.report_promo:
                                                reportThisPromo(p.getPromoid());
                                                return true;
                                        }
                                        return false;
                                    });
                                    viewerPromoMenuNonFav.show();
                                } else {
                                    final PopupMenu viewerPromoMenuFav = new PopupMenu(context, vh2.promomenuimage);
                                    showMenuIcons(viewerPromoMenuFav);
                                    viewerPromoMenuFav.getMenuInflater().inflate(R.menu.vieweruserpromofav, viewerPromoMenuFav.getMenu());
                                    viewerPromoMenuFav.setOnMenuItemClickListener(item -> {
                                        switch (item.getItemId()) {
                                            case R.id.remove_fav_from_favlist:
                                                usersRef.document(documentId).update("favpromosids", FieldValue.arrayRemove(p.getPromoid())).addOnSuccessListener(task15 -> Toast.makeText(context, "تمت الازالة من المفضلة!", Toast.LENGTH_SHORT).show());
                                                promoRef.whereEqualTo("promoid", p.getPromoid()).limit(1).get().addOnSuccessListener(task17 -> {
                                                    promoRef.document(task17.getDocuments().get(0).getId()).update("favcount", FieldValue.increment(-1));
                                                });
                                                return true;
                                            case R.id.report_promo:
                                                reportThisPromo(p.getPromoid());
                                                return true;
                                        }
                                        return false;
                                    });
                                    viewerPromoMenuFav.show();
                                }
                            });
                        }
                    } else {
                        showSigninDialog();
                    }
                });
                vh2.itemView.setOnClickListener(v -> {
                    if(newpromotions.indexOf(p) == -1){
                        notifyItemRemoved(position);
                        Toast.makeText(context, "لقد تم حذف هذا الإعلان!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Bundle bundle = new Bundle();
                    PromotionInfoFragment fragment = new PromotionInfoFragment();
                    bundle.putSerializable("promo", p);
                    fragment.setArguments(bundle);
                    if (context instanceof HomeActivity) {
                        ((HomeActivity) context).pauseVideo();
                        ((HomeActivity) context).replacePromoFragment(fragment);
                    } else if (context instanceof MessagingActivity) {
                        ((MessagingActivity) context).replacePromoFragment(fragment);
                    }
                });
                break;
            case 3:
                newestpromosadviewholder vh3 = ((newestpromosadviewholder) holder);
                AdView mAdView = new AdView(context);
                mAdView.setAdSize(AdSize.BANNER);
                mAdView.setAdUnitId("ca-app-pub-" + banners.get(new Random().nextInt(banners.size())));
                mAdView.loadAd(new AdRequest.Builder().build());
                vh3.adLayout.addView(mAdView);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return newpromotions.size();
    }

    private void showSigninDialog() {

        ((HomeActivity) context).pauseVideo();
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.signin_alert_layout);
        TextView closeOption = dialog.findViewById(R.id.alert_close);
        TextView signinOption = dialog.findViewById(R.id.alert_signin);
        dialog.show();
        closeOption.setOnClickListener(v -> dialog.cancel());
        signinOption.setOnClickListener(v -> {
            Intent i = new Intent(context, WelcomeActivity.class);
            context.startActivity(i);
        });
    }

    public void reportThisPromo(long promoId) {
        promoRef.whereEqualTo("promoid", promoId).get().addOnCompleteListener(task1 -> {
            DocumentSnapshot ds = task1.getResult().getDocuments().get(0);
            ArrayList<String> reports = (ArrayList<String>) ds.get("reports");
            if (reports != null) {
                if (!reports.contains(userId)) {
                    promoRef.document(ds.getId()).update("reports", FieldValue.arrayUnion(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (reports.size() + 1 > 10) {
                                promoRef.document(ds.getId()).update("isBanned", "true");
                            }
                            Toast.makeText(context, "لقد تم الإبلاغ عن هذا الإعلان!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(context, "لقد قمت بالإبلاغ عن هذا الاعلان من قبل!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public static class newestpromosadviewholder extends RecyclerView.ViewHolder {
        AdView adView;
        ConstraintLayout adLayout;

        newestpromosadviewholder(@NonNull View itemView) {
            super(itemView);
            adView = itemView.findViewById(R.id.adView);
            adLayout = itemView.findViewById(R.id.adLayout);
        }
    }

    void showMenuIcons(PopupMenu menu){
        try {
            Field[] fields = menu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(menu);
                    Class.forName(menuPopupHelper.getClass().getName()).getMethod("setForceShowIcon", boolean.class).invoke(menuPopupHelper,true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void deletePromo(int position){
        Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.delete_promo_alert_layout);
        dialog.findViewById(R.id.delete_close).setOnClickListener(v2 -> dialog.dismiss());
        dialog.findViewById(R.id.delete_confirm).setOnClickListener(v1 -> {
            final ProgressDialog progressDialog = ProgressDialog.show(context, "جاري حذف الإعلان",
                    "الرجاء الإنتظار!", true);
//                                    progressDialog.show();
            Promotion promo = newpromotions.get(position);
            long promoId = promo.getPromoid();
            ArrayList<String> images = promo.getPromoimages();

            promoRef.whereEqualTo("promoid", promoId).limit(1).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    DocumentReference documentReference = promoRef.document(task.getResult().getDocuments().get(0).getId());

                    documentReference.collection("ratings").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(QueryDocumentSnapshot queryDocumentSnapshot:queryDocumentSnapshots){
                                documentReference.collection("ratings").document(queryDocumentSnapshot.getId()).delete();
                            }
                        }
                    });
                    documentReference.delete().addOnCompleteListener(task110 -> {
                        if (task110.isSuccessful()) {

                            promoRef.document("promotionidnum").update("promoCount", FieldValue.increment(-1));
                            if (images != null && !images.isEmpty()) {
                                for (int i = 0; i < images.size(); i++) {
                                    firebaseStorage.getReferenceFromUrl(images.get(i)).delete();
                                }
                            }else if(promo.getVideoThumbnail()!=null && !promo.getVideoThumbnail().isEmpty()){
                                firebaseStorage.getReferenceFromUrl(promo.getVideoThumbnail()).delete();
                                firebaseStorage.getReferenceFromUrl(promo.getVideoUrl()).delete();
                            }
                            chatsRef.whereEqualTo("intendedpromoid", promoId).get().addOnCompleteListener(task19 -> {
                                if (task19.isSuccessful() && !task19.getResult().isEmpty()) {
                                    for (DocumentSnapshot ds13 : task19.getResult().getDocuments()) {
                                        chatsRef.document(ds13.getId()).delete();
                                    }
                                }
                            });
                            usersRef.whereArrayContains("favpromosids", promoId).get().addOnCompleteListener(task18 -> {
                                if (task18.isSuccessful() && !task18.getResult().isEmpty()) {
                                    for (DocumentSnapshot ds12 : task18.getResult().getDocuments()) {
                                        usersRef.document(ds12.getId()).update("favpromosids", FieldValue.arrayRemove(promoId));
                                    }
                                }
                            });
                            notificationsRef.whereEqualTo("promoId", promoId).get().addOnCompleteListener(task12 -> {
                                if (task12.isSuccessful() && !task12.getResult().isEmpty()) {
                                    for (DocumentSnapshot ds1 : task12.getResult().getDocuments()) {
                                        notificationsRef.document(ds1.getId()).delete();
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
                ((HomeActivity) context).isFromUserFragment();
                dialog.cancel();
                progressDialog.dismiss();
                newpromotions.remove(position);
                ((HomeActivity) context).newestPromosFragment.deleteVideoFromPromosAdapter(promo);
                notifyItemRemoved(position);
            }).addOnFailureListener(e -> {
                dialog.cancel();
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            });
        });
        dialog.show();
        ((HomeActivity) context).pauseVideo();
    }
}
