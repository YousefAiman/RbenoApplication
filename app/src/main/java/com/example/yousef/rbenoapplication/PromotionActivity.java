package com.example.yousef.rbenoapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import io.opencensus.internal.Utils;

public class PromotionActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    Uri promoImageUri;

    Uri promoImageUri3;
    Uri promoImageUri4;
    int imageNumber = 0;
    ImageView first_image;
    ImageView second_image;
    ImageView third_image;
    ImageView fourth_image;
    String category = "";
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference storageReference;
    ArrayList<String> imageUrisString;
    ArrayList<Uri> imageUris;
//    ProgressBar progressBar1;
//    ProgressBar progressBar2;
//    ProgressBar progressBar3;
//    ProgressBar progressBar4;
    ArrayList<ProgressBar> progressBars;
    Button addPromoButton;
    StorageReference ref1;
    StorageReference ref2;
    StorageReference ref3;
    StorageReference ref4;
    String userName;
    String uri1;
    String uri2;
    String uri3;
    String uri4;
    long price;

//    CardView videoCardView;
//    HorizontalScrollView horizontal_scroll;
//    View addPromoLayoutSeperator;
    ProgressBar videoProgressBar;
    String videoDownloadUri;
    String videoThumbnail;
    MenuItem item;
    ImageView videoImageView;
    Uri videoUri;
    int promoType;
    List<String> titleSpilitting;
    String title;
    String description;
    CollectionReference promoRef;
    Boolean negotitation;
    int duration;
    ProgressDialog progressDialog;

    public Bitmap decodeUri(Uri uri, final int requiredSize)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getApplicationContext().getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (width_tmp / 2 >= requiredSize && height_tmp / 2 >= requiredSize) {
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getApplicationContext().getContentResolver().openInputStream(uri), null, o2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Intent intent = getIntent();
        userName = intent.getStringExtra("username");
        //country = intent.getStringExtra("country");
//        city = intent.getStringExtra("city");
        //currency = intent.getStringExtra("currency");
//        videoCardView = findViewById(R.id.videoCardView);
        videoImageView = findViewById(R.id.videoImageView);
//        videoView = findViewById(R.id.videoView);
        videoProgressBar = findViewById(R.id.videoProgressBar);
        videoProgressBar.setVisibility(View.INVISIBLE);
//        horizontal_scroll = findViewById(R.id.horizontal_scroll);
//        addPromoLayoutSeperator = findViewById(R.id.line4);
        ConstraintLayout parentLayout = findViewById(R.id.promotionConstraintLayout);

        if (intent.hasExtra("textPromo") && intent.getBooleanExtra("textPromo", false)) {
            promoType = 1;
            findViewById(R.id.videoCardView).setVisibility(View.GONE);
            findViewById(R.id.horizontal_scroll).setVisibility(View.GONE);
            findViewById(R.id.line4).setVisibility(View.GONE);
//            addPromoLayoutSeperator.setVisibility(View.GONE);
            findViewById(R.id.imageTv).setVisibility(View.GONE);
        } else if (intent.hasExtra("videoPromo") && intent.getBooleanExtra("videoPromo", false)) {
            promoType = 2;
            requestPermissions();
            findViewById(R.id.horizontal_scroll).setVisibility(View.GONE);
            findViewById(R.id.videoCardView).setOnClickListener(this::chooseVideo);
        } else {
            promoType = 3;
            requestPermissions();
            findViewById(R.id.videoCardView).setVisibility(View.GONE);
            ConstraintSet set = new ConstraintSet();
            set.clone(parentLayout);
            set.connect(R.id.line4, ConstraintSet.TOP,R.id.horizontal_scroll,ConstraintSet.BOTTOM, 10);
            set.setMargin(R.id.line4,ConstraintSet.TOP, (int) (8*GlobalVariables.getDensity()));
            set.applyTo(parentLayout);

            progressBars = new ArrayList<>();
//            progressBar1 = findViewById(R.id.progress_bar1);
//            progressBar2 = findViewById(R.id.progress_bar2);
//            progressBar3 = findViewById(R.id.progress_bar3);
//            progressBar4 = findViewById(R.id.progress_bar4);
            progressBars.add(findViewById(R.id.progress_bar1));
            progressBars.add(findViewById(R.id.progress_bar2));
            progressBars.add(findViewById(R.id.progress_bar3));
            progressBars.add(findViewById(R.id.progress_bar4));

            first_image = findViewById(R.id.promoimage1);
            first_image.setOnClickListener(v -> {
                getImage();
                imageNumber = 1;
            });

            second_image = findViewById(R.id.promoimage2);
            second_image.setOnClickListener(v -> {
                getImage();
                imageNumber = 2;
            });

            third_image = findViewById(R.id.promoimage3);
            third_image.setOnClickListener(v -> {
                getImage();
                imageNumber = 3;
            });

            fourth_image = findViewById(R.id.promoimage4);
            fourth_image.setOnClickListener(v -> {
                getImage();
                imageNumber = 4;
            });


            int width = GlobalVariables.getWindowWidth() / 3;

            first_image.getLayoutParams().width = width;
            second_image.getLayoutParams().width = width;
            third_image.getLayoutParams().width = width;
            fourth_image.getLayoutParams().width = width;
            imageUrisString = new ArrayList<>();
            imageUris = new ArrayList<>();
        }

//        finished = false;

//        progressBar1.setVisibility(View.INVISIBLE);
//        progressBar2.setVisibility(View.INVISIBLE);
//        progressBar3.setVisibility(View.INVISIBLE);
//        progressBar4.setVisibility(View.INVISIBLE);

        auth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();

        storageReference = storage.getReference();

         EditText titleEd = findViewById(R.id.titleEd);
         EditText priceEd = findViewById(R.id.priceEd);
         EditText descriptionEd = findViewById(R.id.descEd);
        addPromoButton = findViewById(R.id.addPromoBtn);
        CheckBox negotiationCheck = findViewById(R.id.negotiationCheck);

        promoRef = firestore.collection("promotions");

        addPromoButton.setOnClickListener(v -> {

            title = titleEd.getText().toString().trim();
            description = descriptionEd.getText().toString().trim();
            if (!priceEd.getText().toString().matches("")) {
                price = Long.parseLong(priceEd.getText().toString());
            }

            if (!title.isEmpty() && !description.isEmpty() && !category.isEmpty() && price != 0) {

                negotitation = negotiationCheck.isChecked();
                titleSpilitting = new ArrayList<>(Arrays.asList(title.split(" ")));
//                boolean titleIsValid = false;
//                for (String s : titleSpilitting) {
//                    titleIsValid = !isEmailValid(s) && !isPhoneValid(s);
//                }
                if (!isEmailValid(title) && !isPhoneValid(title)) {

                    if ((promoType == 1) || (promoType == 3 && !imageUrisString.isEmpty()) || (promoType == 2 && videoDownloadUri != null && videoThumbnail != null)) {
                        progressDialog = ProgressDialog.show(PromotionActivity.this, "جاري اضافة الإعلان",
                                "الرجاء الإنتظار!", true);

                        promoRef.whereEqualTo("uid", auth.getCurrentUser().getUid()).orderBy("publishtime", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
//                                if (documents.size() < 5) {
//                                    long diff = System.currentTimeMillis() - documents.get(0).getLong("publishtime");
//                                    if (diff <= 12 * HOUR_MILLIS) {
//                                        AlertDialog.Builder alert = new AlertDialog.Builder(PromotionActivity.this);
//                                        alert.setTitle("لا يمكنك نشر اعلان حاليا");
//                                        alert.setMessage("لا يمكنك ان تنشر اعلان الا بعد مرور 12 ساعة على اعلانك السابق!");
//                                        alert.create().show();
//                                    } else {

                                publishPromo();
//                                    }
//                                } else {
//                                    AlertDialog.Builder alert = new AlertDialog.Builder(PromotionActivity.this);
//                                    alert.setTitle("لقد تجاوزت الحد المسموح للاعلانات");
//                                    alert.setMessage("لا يمكنك امتلاك اكثر من خمسة اعلانات منشورة في نفس الوقت!");
//                                    alert.create().show();
//                                }
                            } else {
                                publishPromo();
                            }
                        }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(PromotionActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        });

                    } else {
                        Toast.makeText(PromotionActivity.this, "اعلانك يحتاج الى صورة او فيديو لكي تتم عملية النشر!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PromotionActivity.this, "عنوان الاعلان غير مناسب الرجاء تغيير العنوان!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PromotionActivity.this, "الرجاء تعبئة حقول الإعلان!", Toast.LENGTH_SHORT).show();

            }
        });


        final ImageView menu_image = findViewById(R.id.menu_image);
        TextView categoryTv = findViewById(R.id.categoryTv);

        menu_image.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(PromotionActivity.this, categoryTv);
            popup.getMenuInflater().inflate(R.menu.categorymenu, popup.getMenu());
            Menu menu = popup.getMenu();
            if (category != null) {
                switch (category) {
                    case "سيارات":
                        item = menu.findItem(R.id.cars_category);
                        break;
                    case "موبيلات":
                        item = menu.findItem(R.id.phones_category);
                        break;
                    case "كمبيوتر و لاب توب":
                        item = menu.findItem(R.id.computer_category);
                        break;
                    case "عقارات":
                        item = menu.findItem(R.id.realstate_category);
                        break;
                    case "اليكترونيات":
                        item = menu.findItem(R.id.electronics_category);
                        break;
                    case "أثاث":
                        item = menu.findItem(R.id.furniture_category);
                        break;
                }
                if (item != null) {
                    SpannableString s = new SpannableString(item.getTitle());
                    s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.red)), 0, s.length(), 0);
                    item.setTitle(s);
                }
            }
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.cars_category:
                        category = "سيارات";
                        categoryTv.setText(category);
                        return true;
                    case R.id.computer_category:
                        category = "كمبيوتر و لاب توب";
                        categoryTv.setText(category);
                        return true;
                    case R.id.phones_category:
                        category = "موبيلات";
                        categoryTv.setText(category);
                        return true;
                    case R.id.electronics_category:
                        category = "اليكترونيات";
                        categoryTv.setText(category);
                        return true;
                    case R.id.furniture_category:
                        category = "أثاث";
                        categoryTv.setText(category);
                        return true;
                    case R.id.realstate_category:
                        category = "عقارات";
                        categoryTv.setText(category);
                        return true;
                    default:
                        return true;
                }
            });
            popup.show();
        });
        categoryTv.setOnClickListener(v -> menu_image.performClick());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            switch (imageNumber) {
                case (1):
                    progressBars.get(0).setVisibility(View.VISIBLE);
                    Uri promoImageUri = data.getData();

//                    Bitmap bitmap = uriToBitmap(promoImageUri);

                    try {
                        first_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        first_image.setImageBitmap(decodeUri(promoImageUri, 80));
                        first_image.setClickable(false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    //  imageCount++;
                    if (promoImageUri != null) {
                        ProgressBar pr1 = progressBars.get(0);
                        addPromoButton.setClickable(false);
                        pr1.setVisibility(View.VISIBLE);
                        ref1 = storageReference.child("images/" + UUID.randomUUID().toString());
                        ref1.putFile(promoImageUri).addOnSuccessListener(taskSnapshot -> {
                            ref1.getDownloadUrl().addOnSuccessListener(uri -> {
                                uri1 = uri.toString();
                                imageUrisString.add(uri1);
                            }).addOnCompleteListener(task -> {
                                pr1.setVisibility(View.INVISIBLE);
                                addPromoButton.setClickable(true);
                                ImageView closeImage1 = findViewById(R.id.closePromoImage1);
                                closeImage1.setVisibility(View.VISIBLE);
                                closeImage1.setOnClickListener(view -> {
                                    pr1.setVisibility(View.VISIBLE);
                                    storage.getReferenceFromUrl(uri1).delete().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            imageUrisString.remove(uri1);
                                            pr1.setVisibility(View.INVISIBLE);
                                            closeImage1.setVisibility(View.INVISIBLE);
                                            first_image.setImageResource(R.drawable.ic_add_black_24dp);
                                            first_image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                            first_image.setClickable(true);
                                        }
                                    }).addOnFailureListener(e -> Toast.makeText(PromotionActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                                });
                            });
                        });
                    }
                    break;
                case (2):
                    ProgressBar pr2 = progressBars.get(1);
                    pr2.setVisibility(View.VISIBLE);
                    Uri promoImageUri2 = data.getData();

//                    Bitmap bitmap2 = uriToBitmap(promoImageUri2);
                    try {
                        second_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        second_image.setImageBitmap(decodeUri(promoImageUri2, 80));
                        second_image.setClickable(false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (promoImageUri2 != null) {

                        addPromoButton.setClickable(false);
                        pr2.setVisibility(View.VISIBLE);
                        ref2 = storageReference.child("images/" + UUID.randomUUID().toString());
                        ref2.putFile(promoImageUri2).addOnSuccessListener(taskSnapshot -> {
                            ref2.getDownloadUrl().addOnSuccessListener(uri -> {
                                uri2 = uri.toString();
                                imageUrisString.add(uri2);
                            }).addOnCompleteListener(task -> {
                                pr2.setVisibility(View.INVISIBLE);
                                addPromoButton.setClickable(true);
                                ImageView closeImage2 = findViewById(R.id.closePromoImage2);
                                closeImage2.setVisibility(View.VISIBLE);
                                closeImage2.setOnClickListener(view -> {
                                    pr2.setVisibility(View.VISIBLE);
                                    storage.getReferenceFromUrl(uri2).delete().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            imageUrisString.remove(uri2);
                                            pr2.setVisibility(View.INVISIBLE);
                                            closeImage2.setVisibility(View.INVISIBLE);
                                            second_image.setImageResource(R.drawable.ic_add_black_24dp);
                                            second_image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                            second_image.setClickable(true);
                                        }
                                    }).addOnFailureListener(e -> Toast.makeText(PromotionActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                                });
                            });
                        });
                    }
                    break;
                case (3):
                    ProgressBar pr3 = progressBars.get(2);
                    pr3.setVisibility(View.VISIBLE);
                    Uri promoImageUri3 = data.getData();

//                    Bitmap bitmap3 = uriToBitmap(promoImageUri3);
                    try {
                        third_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        third_image.setImageBitmap(decodeUri(promoImageUri3, 80));
                        third_image.setClickable(false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (promoImageUri3 != null) {

                        addPromoButton.setClickable(false);
                        pr3.setVisibility(View.VISIBLE);
                        ref3 = storageReference.child("images/" + UUID.randomUUID().toString());
                        ref3.putFile(promoImageUri3).addOnSuccessListener(taskSnapshot -> {
                            ref3.getDownloadUrl().addOnSuccessListener(uri -> {
                                uri3 = uri.toString();
                                imageUrisString.add(uri3);
                            }).addOnCompleteListener(task -> {
                                pr3.setVisibility(View.INVISIBLE);
                                addPromoButton.setClickable(true);
                                ImageView closeImage3 = findViewById(R.id.closePromoImage3);
                                closeImage3.setVisibility(View.VISIBLE);
                                closeImage3.setOnClickListener(view -> {
                                    pr3.setVisibility(View.VISIBLE);
                                    storage.getReferenceFromUrl(uri3).delete().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            imageUrisString.remove(uri3);
                                            pr3.setVisibility(View.INVISIBLE);
                                            closeImage3.setVisibility(View.INVISIBLE);
                                            third_image.setImageResource(R.drawable.ic_add_black_24dp);
                                            third_image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                            third_image.setClickable(true);
                                        }
                                    }).addOnFailureListener(e -> Toast.makeText(PromotionActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                                });
                            });
                        });
                    }
                    break;
                case (4):
                    ProgressBar pr4 = progressBars.get(3);
                    pr4.setVisibility(View.VISIBLE);
                    Uri promoImageUri4 = data.getData();

//                    Bitmap bitmap4 = uriToBitmap(promoImageUri4);
                    try {
                        fourth_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        fourth_image.setImageBitmap(decodeUri(promoImageUri4, 80));
                        third_image.setClickable(false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (promoImageUri4 != null) {

                        addPromoButton.setClickable(false);
                        pr4.setVisibility(View.VISIBLE);
                        ref4 = storageReference.child("images/" + UUID.randomUUID().toString());
                        ref4.putFile(promoImageUri4).addOnSuccessListener(taskSnapshot -> {
                            ref4.getDownloadUrl().addOnSuccessListener(uri -> {
                                uri4 = uri.toString();
                                imageUrisString.add(uri4);
                            }).addOnCompleteListener(task -> {
                                pr4.setVisibility(View.INVISIBLE);
                                addPromoButton.setClickable(true);
                                ImageView closeImage4 = findViewById(R.id.closePromoImage4);
                                closeImage4.setVisibility(View.VISIBLE);
                                closeImage4.setOnClickListener(view -> {
                                    pr4.setVisibility(View.VISIBLE);
                                    storage.getReferenceFromUrl(uri4).delete().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            imageUrisString.remove(uri4);
                                            pr4.setVisibility(View.INVISIBLE);
                                            closeImage4.setVisibility(View.INVISIBLE);
                                            fourth_image.setImageResource(R.drawable.ic_add_black_24dp);
                                            fourth_image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                            third_image.setClickable(true);
                                        }
                                    }).addOnFailureListener(e -> Toast.makeText(PromotionActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                                });
                            });
                        });
                    }
                    break;
                case (5):
//                    addPromoButton.setClickable(false);
                    videoProgressBar.setVisibility(View.VISIBLE);
                    Uri videoImageUri = data.getData();

//                    Bitmap videoBitmap = uriToBitmap(videoImageUri);
                    try {
                        videoImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        videoImageView.setImageBitmap(decodeUri(videoImageUri, 100));
                        videoImageView.setClickable(false);
                        findViewById(R.id.videoCardView).setClickable(false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (videoImageUri != null) {
                        ProgressBar videoThumbnailPb = findViewById(R.id.videoThumbnailProgressBar);
//                        addPromoButton.setClickable(false);
                        videoThumbnailPb.setVisibility(View.VISIBLE);
                        StorageReference videoImageRef = storageReference.child("images/" + UUID.randomUUID().toString());
                        videoImageRef.putFile(videoImageUri).addOnSuccessListener(taskSnapshot -> {
                            videoImageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                                videoThumbnailPb.setVisibility(View.GONE);
                                videoThumbnail = uri.toString();
                                addPromoButton.setClickable(true);
                            });
                        });
                    }
                    break;
            }
        } else if (resultCode == RESULT_OK && requestCode == PICK_VIDEO && data != null && data.getData() != null) {
            videoUri = data.getData();

            if(isNewGooglePhotosUri(videoUri)){
                Toast.makeText(this, "لا يمكن رفع فيديو مباشرة من صور جوجل! يجب تحميل الفيديو اولا ثم المحاولة مرة اخرى", Toast.LENGTH_LONG).show();
                return;
            }


            MediaPlayer mp = MediaPlayer.create(this, videoUri);
            mp.setOnPreparedListener(mediaPlayer -> duration = mp.getDuration());
            mp.release();

            if ((duration / 1000) > 30) {
                videoImageView.setVisibility(View.VISIBLE);
                Toast.makeText(this, "طول الفيديو يجب ان لا يزيد عن نصف دقيقة واحدة!", Toast.LENGTH_SHORT).show();
            } else {
                UploadVideo(videoUri);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.categorymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        //SharedPreferences sharedPreferences= getSharedPreferences("rbeno", Context.MODE_PRIVATE);

        showAlertDialog();
    }

     void showAlertDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("هل تريد الخروج؟");
        alert.setMessage("الخروج قبل الانتهاء من اضافة الاعلان سيؤدي الى فقدانه");

        alert.setPositiveButton("نعم", (dialog, which) -> {
            PromotionActivity.super.onBackPressed();

            Toast.makeText(PromotionActivity.this, "لم تتم اضافة الاعلان", Toast.LENGTH_SHORT).show();

            if (imageUrisString != null && !imageUrisString.isEmpty()) {
                for (String imageUri:imageUrisString) {
                    storage.getReferenceFromUrl(imageUri).delete().addOnFailureListener(e -> Toast.makeText(PromotionActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                }
            }
            if (videoUri != null) {
                if (videoDownloadUri != null && !videoDownloadUri.isEmpty()) {
                    storage.getReferenceFromUrl(videoDownloadUri).delete();
                }
                if(videoThumbnail!=null && !videoThumbnail.isEmpty()){
                    storage.getReferenceFromUrl(videoThumbnail).delete();
                }
            }
        });
        alert.setNegativeButton("لا", (dialog, which) -> {

        });
        alert.create().show();

    }

     boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

     boolean isPhoneValid(CharSequence phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

     void chooseVideo(View view) {

        //requestPermissions(2);

        Intent videoIntent = new Intent();
        videoIntent.setType("video/*");
        videoIntent.setAction(Intent.ACTION_GET_CONTENT);
        videoIntent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(Intent.createChooser(videoIntent, "اختر فيديو لاعلانك"), PICK_VIDEO);
    }


    public void UploadVideo(Uri videoUri) {


        final StorageReference videoRef = storageReference.child("videos/" + UUID.randomUUID().toString());

        videoProgressBar.setVisibility(View.VISIBLE);
//        addPromoButton.setClickable(false);

        videoRef.putFile(videoUri).addOnSuccessListener(taskSnapshot -> videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
            videoProgressBar.setVisibility(View.INVISIBLE);
            videoDownloadUri = uri.toString();
//            addPromoButton.setClickable(true);
            Toast.makeText(PromotionActivity.this, "تم تحميل الفيديو الخاص بك!", Toast.LENGTH_SHORT).show();
        })
        ).addOnProgressListener(taskSnapshot -> videoProgressBar.setProgress((int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount())))
                .addOnFailureListener(e -> {
                            Toast.makeText(PromotionActivity.this, "لقد فشل رفع الفيديو!", Toast.LENGTH_SHORT).show();
                        Log.d("ttt",e.toString());
                });
        videoImageView.setOnClickListener(v -> {
            getImage();
            imageNumber = 5;
        });
    }



    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private Bitmap uriToBitmap(Uri selectedFileUri) {
        Bitmap image = null;
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor);


            parcelFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }


    public void requestPermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, 1);
            }
        }
    }

    public byte[] compressAndConvert(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.6), (int) (bitmap.getHeight() * 0.6), true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void publishPromo() {

        final DocumentReference promoidRef = promoRef.document("promotionidnum");

        addPromoButton.setClickable(false);

        promoidRef.get().addOnSuccessListener(documentSnapshot -> {

            Map<String, Object> data = new HashMap<>();
            data.put("type", category);
            data.put("title", title);
//            data.put("city",city);
            data.put("description", description);
            data.put("negotiable", negotitation);
            data.put("price", price);
            data.put("promoid", documentSnapshot.getLong("count"));
            data.put("publishtime", System.currentTimeMillis() / 1000);
            data.put("rating", 0);
            data.put("uid", auth.getUid());
            data.put("userName", userName);
            data.put("favcount", 0);
            data.put("viewcount", 0);
            data.put("reports", new ArrayList<>());
            data.put("country", GlobalVariables.getCountry());
            data.put("keyWords", titleSpilitting);
            data.put("currency", GlobalVariables.getCurrency());
            data.put("isBanned", false);
            if (videoDownloadUri != null && !videoDownloadUri.isEmpty() && !videoThumbnail.isEmpty()) {
                data.put("videoUrl", videoDownloadUri);
                data.put("videoThumbnail", videoThumbnail);
                data.put("promoType", "video");
            } else if (!imageUrisString.isEmpty()) {
                data.put("promoimages", imageUrisString);
                data.put("promoType", "image");
            } else {
                data.put("promoType", "text");
            }

            promoRef.add(data).addOnSuccessListener(documentReference -> promoidRef.update("count", FieldValue.increment(1)).addOnSuccessListener(aVoid ->
                    promoidRef.update("promoCount", FieldValue.increment(1)).addOnSuccessListener(aVoid1 ->
                            promoRef.document(documentReference.getId()).
                                    collection("ratings").add(new PromoRating()).addOnSuccessListener(documentReference1 -> {
                                progressDialog.dismiss();
                                Toast.makeText(PromotionActivity.this, "تمت اضافة الإعلان بنجاح!", Toast.LENGTH_SHORT).show();
                                setResult(3);
                                finish();
                            }))));
        }).addOnFailureListener(e -> publishingFailed());
    }

    void publishingFailed() {
        Toast.makeText(PromotionActivity.this, "لقد فشل اضافة الإعلان الرجاء المحاولة مرة اخرى!", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
        addPromoButton.setClickable(true);
    }

    void getImage() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Select Image"), PICK_IMAGE);
    }
    public static boolean isNewGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }
//    public File getPathFromInputStreamUri(Context context, Uri uri) {
//        InputStream inputStream = null;
//        String filePath = null;
//        File photoFile = null;
//        if (uri.getAuthority() != null) {
//            try {
//                inputStream = getContentResolver().openInputStream(uri);
//                 photoFile = createTemporalFileFrom(inputStream);
//                Uri uriFile =Uri.fromFile(photoFile);
//                MediaPlayer mp = MediaPlayer.create(this,uriFile);
//                mp.setOnPreparedListener(mediaPlayer -> duration = mp.getDuration());
//                Log.d("video","video length: "+duration);
//                mp.release();
//                videoUri = uriFile;
//
//            } catch (IOException e) {
//
//            } finally {
//                try {
//                    if (inputStream != null) {
//                        inputStream.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return photoFile;
//    }
//
//    private  File createTemporalFileFrom(InputStream inputStream) throws IOException {
//        File targetFile = null;
//
//        if (inputStream != null) {
//            int read;
//            byte[] buffer = new byte[8 * 1024];
//
//            targetFile = createTemporalFile();
//            OutputStream outputStream = new FileOutputStream(targetFile);
//
//            while ((read = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, read);
//            }
//            outputStream.flush();
//
//            try {
//                outputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return targetFile;
//    }
//
//    private File createTemporalFile() {
//        return new File(getExternalFilesDir("/"), "tempPicture.jpg");
//    }

}
