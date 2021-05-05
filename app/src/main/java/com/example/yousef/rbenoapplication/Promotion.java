package com.example.yousef.rbenoapplication;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@IgnoreExtraProperties
public class Promotion implements Serializable {

    @PropertyName("type")
    public String type;
    @PropertyName("title")
    public String title;
    //    @PropertyName("city")
//    public String city;
    @PropertyName("description")
    public String description;
    @Exclude
    public boolean negotiable;
    @PropertyName("price")
    public double price;
    @PropertyName("promoid")
    public long promoid;
    @PropertyName("publishtime")
    public long publishtime;
    @PropertyName("rating")
    public double rating;
    @PropertyName("uid")
    public String uid;
    @PropertyName("promoimages")
    public ArrayList<String> promoimages;
    @PropertyName("favcount")
    public long favcount;
    @PropertyName("viewcount")
    public long viewcount;
    @PropertyName("videoUrl")
    public String videoUrl;
    @PropertyName("videoThumbnail")
    public String videoThumbnail;
    @PropertyName("cityName")
    public String cityName;
    @PropertyName("countryCode")
    public String countryCode;
    @Exclude
    public List<String> keyWords;
    @PropertyName("currency")
    public String currency;
    @PropertyName("promoType")
    public String promoType;
    @Exclude
    public boolean isBanned;
    @PropertyName("isPaused")
    private boolean isPaused;

    private boolean isDeleted;


    public Promotion() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public String getCity() {
//        return city;
//    }
//
//    public void setCity(String city) {
//        this.city = city;
//    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public boolean isNegotiable() {
        return negotiable;
    }

    public void setNegotiable(boolean negotiable) {
        this.negotiable = negotiable;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getPromoid() {
        return promoid;
    }

    public void setPromoid(long promoid) {
        this.promoid = promoid;
    }

    public long getPublishtime() {
        return publishtime;
    }

    public void setPublishtime(long publishtime) {
        this.publishtime = publishtime;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public ArrayList<String> getPromoimages() {
        return promoimages;
    }

    public void setPromoimages(ArrayList<String> imagesArray) {
        this.promoimages = imagesArray;
    }


    public long getFavcount() {
        return favcount;
    }

    public void setFavcount(long favcount) {
        this.favcount = favcount;
    }

    public long getViewcount() {
        return viewcount;
    }

    public void setViewcount(long viewcount) {
        this.viewcount = viewcount;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoThumbnail() {
        return videoThumbnail;
    }

    public void setVideoThumbnail(String videoThumbnail) {
        this.videoThumbnail = videoThumbnail;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }


    public List<String> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(List<String> keyWords) {
        this.keyWords = keyWords;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPromoType() {
        return promoType;
    }

    public void setPromoType(String promoType) {
        this.promoType = promoType;
    }

    public boolean getIsBanned() {
        return isBanned;
    }

    public void setIsBanned(boolean isBanned) {
        this.isBanned = isBanned;
    }

    public boolean getIsPaused() {
        return isPaused;
    }

    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }


    static void deletePromo(Context context,
                            Promotion promotion) {

        if (WifiUtil.checkWifiConnection(context)) {
            final Dialog dialog = new Dialog(context);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.delete_promo_alert_layout);

            dialog.findViewById(R.id.delete_close).setOnClickListener(v2 -> dialog.dismiss());

            dialog.findViewById(R.id.delete_confirm).setOnClickListener(v1 -> {
                if (WifiUtil.checkWifiConnection(context)) {

                    final ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle("جاري حذف الإعلان");
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("الرجاء الإنتظار!");
                    progressDialog.show();

                    FirebaseFirestore.getInstance().collection("promotions")
                            .whereEqualTo("promoid", promotion.getPromoid()).get()
                            .addOnSuccessListener(snapshots -> {

                                final DocumentReference ref = snapshots.getDocuments().get(0).getReference();
                                ref.update("isDeleted", true);

                                ref.delete().addOnSuccessListener(v -> {

                                    if (!promotion.getPromoType().equals("text")) {
                                        final FirebaseStorage storage = FirebaseStorage.getInstance();
                                        if (promotion.getPromoimages() != null && !promotion.getPromoimages().isEmpty()) {
                                            for (String imageUrl : promotion.getPromoimages()) {
                                                storage.getReferenceFromUrl(imageUrl).delete();
                                            }
                                        } else if (promotion.getVideoThumbnail() != null
                                                && !promotion.getVideoThumbnail().isEmpty()) {
                                            storage.getReferenceFromUrl(promotion.getVideoThumbnail()).delete();
                                            storage.getReferenceFromUrl(promotion.getVideoUrl()).delete();
                                        }
                                    }

                                    ref.collection("ratings").get()
                                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots2) {
                                                    snapshot.getReference().delete();
                                                }
                                            }).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {

                                            FirebaseFirestore.getInstance().collection("users")
                                                    .whereArrayContains("favpromosids",
                                                            Long.toString(promotion.getPromoid()))
                                                    .get().addOnSuccessListener(snapshots1 -> {
                                                for (DocumentSnapshot snap : snapshots1.getDocuments()) {
                                                    snap.getReference().update("favpromosids",
                                                            FieldValue.arrayRemove(Long.toString(promotion.getPromoid())));
                                                }
                                            }).addOnCompleteListener(task12 -> {
                                                if (task12.isSuccessful()) {
                                                    FirebaseFirestore.getInstance().collection("notifications")
                                                            .whereEqualTo("promoId", promotion.getPromoid()).get()
                                                            .addOnSuccessListener(snapshots1 -> {
                                                                for (DocumentSnapshot snaps : snapshots1.getDocuments()) {
                                                                    snaps.getReference().delete();
                                                                }
                                                            }).addOnCompleteListener(task1 -> {
                                                        dialog.dismiss();
                                                        progressDialog.dismiss();
                                                    });
                                                } else {
                                                    dialog.dismiss();
                                                    progressDialog.dismiss();
                                                }
                                            });
                                        } else {
                                            dialog.dismiss();
                                            progressDialog.dismiss();
                                        }
                                    });

                                }).addOnFailureListener(e -> {

                                    ref.update("isDeleted", false);
                                    dialog.dismiss();
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "لقد فشل حذف الإعلان!", Toast.LENGTH_SHORT).show();
                                    Log.d("ttt", "delete ref failed");
                                });
                            }).addOnFailureListener(e -> {
                        dialog.cancel();
                        progressDialog.dismiss();
                        Toast.makeText(context, "لقد فشل حذف الإعلان!", Toast.LENGTH_SHORT).show();
                    });
                }
            });
            dialog.show();
        }
    }

    static void pauseOrUnPausePromo(
            Context context, Promotion promotion, Menu menu, @Nullable String docId) {


        final CollectionReference collectionReference =
                FirebaseFirestore.getInstance().collection("promotions");

        if (docId != null) {
            continuePausingWithSnap(collectionReference.document(docId), promotion, menu, context);
        } else {

            collectionReference.whereEqualTo("promoid", promotion.getPromoid())
                    .get().addOnSuccessListener(snapshots -> {
                if (!snapshots.isEmpty()) {
                    continuePausingWithSnap(
                            snapshots.getDocuments().get(0).getReference(), promotion, menu, context);
                }
            });

        }

    }

    private static void continuePausingWithSnap(DocumentReference documentReference,
                                                Promotion promotion,
                                                @Nullable Menu menu,
                                                Context context) {

        final boolean pause = !promotion.getIsPaused();
        Log.d("ttt", "updating to: " + pause);
        documentReference.update("isPaused", pause).addOnSuccessListener(v -> {
            promotion.setIsPaused(pause);
            if (promotion.getIsPaused()) {

                if (menu != null) {
                    menu.findItem(R.id.pause_item).setTitle("تشغيل الاعلان");
                }

                Toast.makeText(context, "تم ايقاف هذا الإعلان إلا ان تقوم بإعادة تشغيله!",
                        Toast.LENGTH_SHORT).show();

            } else {
                if (menu != null) {
                    menu.findItem(R.id.pause_item).setTitle("إيقاف الاعلان");
                }

                Toast.makeText(context,
                        "تم إعادة تشغيل هذا الإعلان!", Toast.LENGTH_SHORT).show();

            }
        });

    }


    static void reportPromo(Context context,
                            long promoId,
                            String userId,
                            @Nullable String promoDocId) {

        final CollectionReference promoRef =
                FirebaseFirestore.getInstance().collection("promotions");

        if (promoDocId != null) {
            promoRef.document(promoDocId).get().addOnSuccessListener(documentSnapshot ->
                    continueReportingWithSnap(context, userId, documentSnapshot));
        } else {

            promoRef.whereEqualTo("promoid", promoId).get().addOnSuccessListener(snaps ->
                    continueReportingWithSnap(context, userId, snaps.getDocuments().get(0)));

        }

    }

    private static void continueReportingWithSnap(
            Context context,
            String userId,
            DocumentSnapshot documentSnapshot) {

        final long currentTimeInMillies = System.currentTimeMillis() / 1000;

        if (documentSnapshot.contains("reports")) {

            final List<String> reports =
                    documentSnapshot.get("reports", PromoReports.class).reports;

            if (reports != null) {

                for (String report : reports) {
                    if (report.split("-")[0].equals(userId)) {
                        Toast.makeText(context,
                                "لقد قمت بالإبلاغ عن هذا الاعلان من قبل!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                documentSnapshot.getReference().update("reports",
                        FieldValue.arrayUnion(userId + "-" + currentTimeInMillies))
                        .addOnSuccessListener(v -> {
                            Toast.makeText(context, "لقد تم الإبلاغ بنجاح!", Toast.LENGTH_SHORT).show();
                            reports.add(userId + "-" + currentTimeInMillies);

                            if (reports.size() >= 10) {
                                if (((currentTimeInMillies -
                                        Long.parseLong(reports.get(reports.size() - 10)
                                                .split("-")[1])) / 30) < 86400) {

                                    documentSnapshot.getReference().update("isBanned", true)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
//                        dialog.dismiss();
                                                }
                                            });
                                }

                            }
                        });
            }


        } else {
            documentSnapshot.getReference().update("reports",
                    FieldValue.arrayUnion(userId + "-" + currentTimeInMillies))
                    .addOnSuccessListener(v -> {
                        Toast.makeText(context, "لقد تم الإبلاغ بنجاح!", Toast.LENGTH_SHORT).show();
                    });
        }

    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    private static class PromoReports {
        public List<String> reports;
    }


    static File sharePromo(Promotion p, Context context, File file, ImageView shareImageIv) {

        final File[] sharingFile = {null};
        shareImageIv.setClickable(false);

        Log.d("ttt", "started sharing");

        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
        shareIntent.putExtra(Intent.EXTRA_TEXT, p.getTitle() + " - " + p.getPrice() + " " +
                p.getCurrency() + "\n" + p.getDescription());
        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("image/*");
        if (file != null) {

            try {
                if (Build.VERSION.SDK_INT >= 24) {
                    shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".provider", file));
                } else {
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                }
            } finally {
                context.startActivity(Intent.createChooser(shareIntent, "choose one"));
                shareImageIv.setClickable(true);
            }

        } else {


            if (p.getPromoType().equals("image") || p.getPromoType().equals("video")) {

                Log.d("ttt", "has an image");

                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("جاري المشاركة!");
                progressDialog.setCancelable(false);
                progressDialog.show();

                Glide.with(context).asBitmap().load(
                        p.getPromoType().equals("image") ? p.getPromoimages().get(0) : p.getVideoThumbnail())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource,
                                                        @androidx.annotation.Nullable
                                                                Transition<? super Bitmap> transition) {
                                progressDialog.dismiss();
                                Log.d("ttt", "craeted shared bitmap");
                                sharingFile[0] =
                                        createShareFileAndStartIntent(
                                                shareIntent,
                                                context,
                                                resource,
                                                shareImageIv);
                            }

                            @Override
                            public void onLoadFailed(@androidx.annotation.Nullable Drawable errorDrawable) {
                                progressDialog.dismiss();
                                super.onLoadFailed(errorDrawable);
                            }

                            @Override
                            public void onLoadCleared(@androidx.annotation.Nullable Drawable placeholder) {

                            }
                        });

            } else {
                sharingFile[0] = createShareFileAndStartIntent(
                        shareIntent,
                        context,
                        BitmapFactory.decodeResource(context.getResources(), R.drawable.rbeno_logo_png),
                        shareImageIv);
            }

        }

        return sharingFile[0];
    }

    private static File createShareFileAndStartIntent(Intent shareIntent,
                                                      Context context,
                                                      Bitmap bitmap,
                                                      ImageView shareImageIv) {

        File file = null;
        try {
            Log.d("ttt", "creating file for sharing");
            file = new File(context.getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
            final FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

            if (Build.VERSION.SDK_INT >= 24) {
                shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context,
                        BuildConfig.APPLICATION_ID + ".provider", file));
            } else {
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            }

        } catch (Exception IOException) {
            IOException.printStackTrace();
        } finally {
            Log.d("ttt", "starting sharing activity");
            context.startActivity(Intent.createChooser(shareIntent, "choose one"));
            shareImageIv.setClickable(true);
        }

        return file;
    }

    static void favOrUnFavPromo(Context context,
                                Promotion promotion,
                                String userId,
                                ImageView favIv,
                                TextView favTv,
                                boolean isFromFav) {

        if (WifiUtil.checkWifiConnection(context)) {

            String failMessage, successMessage;
            int successResource, failResource, successValue, failValue;
            FieldValue fieldValue;

            if (!GlobalVariables.getFavPromosIds().contains(promotion.getPromoid())) {

                failMessage = "لقد فشلت الإضافة الى المفضلة!";
                successMessage = "لقد نجحت الإضافة الى المفضلة!";
                successResource = R.drawable.heart_icon;
                failResource = R.drawable.heart_grey_outlined;
                successValue = 1;
                failValue = -1;
                fieldValue = FieldValue.arrayUnion(promotion.getPromoid());

            } else {

                failMessage = "لقد فشلت الإزالة من المفضلة!";
                successMessage = "لقد نجحت الإزالة من المفضلة!";
                successResource = R.drawable.heart_grey_outlined;
                failResource = R.drawable.heart_icon;
                successValue = -1;
                failValue = 1;
                fieldValue = FieldValue.arrayRemove(promotion.getPromoid());

            }

            FirebaseFirestore.getInstance().collection("users")
                    .whereEqualTo("userId", userId).get().addOnSuccessListener(snaps ->
                    snaps.getDocuments().get(0).getReference().update("favpromosids", fieldValue)
                            .addOnSuccessListener(v -> {

                                Log.d("ttt", "updated favpromosids with: " + fieldValue.toString());


                                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show();

                                FirebaseFirestore.getInstance().collection("promotions")
                                        .whereEqualTo("promoid", promotion.getPromoid()).get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot snapshots) {

                                                snapshots.getDocuments().get(0).getReference().update("favcount",
                                                        FieldValue.increment(successValue)).addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                favIv.setClickable(true);

                                                                if (successValue == 1 && !promotion.getUid().equals(userId)) {

                                                                    FirestoreNotificationSender.sendFirestoreNotification(
                                                                            promotion.getPromoid(),
                                                                            promotion.getUid(),
                                                                            "favourite");


                                                                    if (!GlobalVariables.getPreviousSentNotifications()
                                                                            .contains(userId + "favourite" + promotion.getPromoid())) {

                                                                        final DocumentSnapshot docSnap =
                                                                                snaps.getDocuments().get(0);
                                                                        final String username = docSnap.getString("username");

                                                                        final String imageUrl = docSnap.getString("imageurl");

                                                                        CloudMessagingNotificationsSender.sendNotification(
                                                                                promotion.getUid(),
                                                                                new Data(
                                                                                        userId,
                                                                                        promotion.getTitle(),
                                                                                        username + " قام بالإعجاب باعلانك رقم: " +
                                                                                                promotion.getPromoid(),
                                                                                        imageUrl,
                                                                                        username,
                                                                                        "favourite",
                                                                                        promotion.getPromoid()
                                                                                ));
                                                                    }

                                                                } else {

                                                                    FirestoreNotificationSender.deleteFirestoreNotification(
                                                                            promotion.getPromoid(),
                                                                            promotion.getUid(),
                                                                            "favourite");


                                                                }

                                                            }
                                                        });

                                            }
                                        }).addOnFailureListener(e -> favIv.setClickable(true));

                            }).addOnFailureListener(e ->
                            favOrUnFavFailed(context, favIv, favTv, failResource, failValue, failMessage)))
                    .addOnFailureListener(e ->
                            favOrUnFavFailed(context, favIv, favTv, failResource, failValue, failMessage));

            if (!isFromFav) {
                favIv.setImageResource(successResource);
                favTv.setText(String.valueOf(Integer.parseInt(favTv.getText().toString()) + successValue));
            }

        }
    }

    private static void favOrUnFavFailed(Context context,
                                         ImageView favIv,
                                         TextView favTv,
                                         int failResource,
                                         int failValue,
                                         String failMessage) {

        favIv.setImageResource(failResource);
        favTv.setText(String.valueOf(Integer.parseInt(favTv.getText().toString()) + failValue));
        Toast.makeText(context, failMessage, Toast.LENGTH_SHORT).show();
        favIv.setClickable(true);

    }

//  static void changePromoStatus(ArrayList<Promotion> promotions, long id, String changeType,
//                                boolean removeFromList){
//
//      for (Promotion promo:promotions) {
//        if(promo.getPromoid() == id){
//
//          switch (changeType){
//
//            case "isBanned":
//              if (removeFromList) {
//                removePromo(promo);
//              }else{
//                promo.setIsBanned(true);
//              }
//              break;
//            case "isDeleted":
//              if (removeFromList) {
//                removePromo(promo);
//              }else{
//                promo.setDeleted(true);
//              }
//              break;
//
//            case "isPaused":
//
//              promo.setPaused(true);
//
//              break;
//
//            case "isResumed":
//
//              promo.setPaused(false);
//
//              break;
//
//          }
//
//          break;
//        }
//      }
//
//  }
//
//  private static void removePromo(ArrayList<Promotion> promotions,Promotion promotion){
//
//    final int index = promotions.indexOf(promotion);
//    promotions.remove(index);
//    adapter.notifyItemRemoved(index);
//
//    if (promotions.size() == 0) {
//      userPromosTv.setVisibility(View.GONE);
//    }
//
//  }

    static boolean printPromoStatus(Context context, Promotion promotion, String currentUid) {
        int message = 0;

        if (promotion.getIsBanned()) {
            message = R.string.promo_banned;
        } else if (promotion.isDeleted()) {
            message = R.string.promo_deleted;
        } else if (promotion.getIsPaused() && !promotion.getUid().equals(currentUid)) {
            message = R.string.promo_paused;
        }

        if (message != 0) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    static void changePromoStatusFromList(ArrayList<Promotion> promotions, long id, String changeType,
                                          RecyclerView.Adapter adapter) {

        if (promotions != null && !promotions.isEmpty()) {
            for (Promotion promo : promotions) {
                if (promo.getPromoid() == id) {

                    final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    switch (changeType) {

                        case "isBanned":
                            if (promo.getUid().equals(currentUid)) {
                                promo.setIsBanned(true);
                            } else {
                                removePromo(promotions, promo, adapter);
                            }
                            break;
                        case "isDeleted":
                            if (promo.getUid().equals(currentUid)) {
                                removePromo(promotions, promo, adapter);
                            } else {
                                promo.setDeleted(true);
                            }
                            break;

                        case "isPaused":

                            promo.setIsPaused(true);

                            break;

                        case "isResumed":

                            promo.setIsPaused(false);

                            break;

                    }

                    break;
                }
            }
        }

    }

    private static void removePromo(ArrayList<Promotion> promotions, Promotion promotion,
                                    RecyclerView.Adapter adapter) {

        final int index = promotions.indexOf(promotion);
        promotions.remove(index);
        adapter.notifyItemRemoved(index);

    }

}
