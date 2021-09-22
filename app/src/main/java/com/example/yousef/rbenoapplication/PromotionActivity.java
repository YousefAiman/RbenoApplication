package com.example.yousef.rbenoapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.exoplayer2.util.Log;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PromotionActivity extends AppCompatActivity implements View.OnClickListener {

    //    private static final int DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024;
    private static final int
            videoLengthMaxLimit = 30,
            videoLengthMinLimit = 10,
            videoMaxSizeInMB = 15,
            PICK_IMAGE = 1,
            PICK_VIDEO = 2, PICK_CAMERA = 3,
            CAMERA_PERMISSION = 4,
            IMAGE_STORAGE_REQUEST = 102,
            VIDEO_STORAGE_REQUEST = 103;

    //firebase
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final CollectionReference promoRef = FirebaseFirestore.getInstance()
            .collection("promotions");

    private int imageNumber = 0;

    private String promoType;

    //views
    private ImageView videoPromoImage, videoThumbnailPromoImage, videoThumbnailCloseImage, videoPlayIv;
    private ProgressBar videoProgressBar, videoThumbnailProgressBar;
    private ProgressBar[] progressBars;
    private ImageView[] promoImages, categoryImageViews, closePromoImages;
    private TextView[] categoryTextViews;
    private Spinner currencySpinner;
    private ArrayList<String> imageUrisString;
    private Button addPromoButton;
    private CardView videoCardView;
    private EditText priceEd, titleEd, descriptionEd;
    private TextView titleLengthTv, descLengthTv;
    private CheckBox negotiationCheck, phoneCheckBox;


    //promotion
    private String category, videoThumbnail, videoDownloadUri, cameraImageFilePath;
    private Uri videoUri;
    private Bitmap videoThumbnailBitmap;
    private Map<UploadTask, OnSuccessListener<UploadTask.TaskSnapshot>> uploadTasks;
    private List<StorageReference> storageReferences;
    private String currentlySelectedCurrencyCode;
    private List<Currency> supportedCurrencyCodes;

    //editing
    private boolean isForEditing;
    private Promotion editablePromotion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);

        InterstitialAdUtil.showAd(this);

        final Intent intent = getIntent();

        if (intent.hasExtra("EditablePromotion")) {
            isForEditing = true;
            editablePromotion = (Promotion) intent.getSerializableExtra("EditablePromotion");
            promoType = editablePromotion.getPromoType();

            if (!editablePromotion.getIsPaused()) {

                promoRef.document(String.valueOf(editablePromotion.getPromoid()))
                        .update("isPaused", true).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PromotionActivity.this,
                                "لقد حصلت مشكلة ما! الرجاء محاولة التعديل مرة اخرى",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }


        } else {
            promoType = intent.getStringExtra("promoType");
        }

        getViews();

        checkPromoType();

        attachListeners();

        if (editablePromotion != null) {
            populateFromPreviousPromotion();
        }


    }

    private void getViews() {

        final Toolbar toolbar = findViewById(R.id.addPromosToolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        titleEd = findViewById(R.id.titleEd);
        titleLengthTv = findViewById(R.id.titleLengthTv);

        priceEd = findViewById(R.id.priceEd);
        currencySpinner = findViewById(R.id.currencySpinner);
        createCurrencyCodeSpinner();

        descriptionEd = findViewById(R.id.descEd);
        descLengthTv = findViewById(R.id.descLengthTv);

        addPromoButton = findViewById(R.id.addPromoBtn);
        negotiationCheck = findViewById(R.id.negotiationCheck);
        phoneCheckBox = findViewById(R.id.phoneCheckBox);

        if (!getIntent().hasExtra("phoneNumber")) {
            phoneCheckBox.setVisibility(View.GONE);
        }

        categoryImageViews = new ImageView[]{
                findViewById(R.id.homeScrollIv),
                findViewById(R.id.mobileScrollIv),
                findViewById(R.id.electronicScrollIv),
                findViewById(R.id.furnitureScrollIv),
                findViewById(R.id.carsScrollIv),
                findViewById(R.id.servicesScrollIv),
                findViewById(R.id.animalScrollIv),
                findViewById(R.id.personalScrollIv),
                findViewById(R.id.otherScrollIv)
        };

        categoryTextViews = new TextView[]{
                findViewById(R.id.homeScrollTv),
                findViewById(R.id.mobileScrollTv),
                findViewById(R.id.electronicScrollTv),
                findViewById(R.id.furnitureScrollTv),
                findViewById(R.id.carsScrollTv),
                findViewById(R.id.servicesScrollTv),
                findViewById(R.id.animalScrollTv),
                findViewById(R.id.personalScrollTv),
                findViewById(R.id.otherScrollTv)
        };


    }

    private void populateFromPreviousPromotion() {

        if (promoType.equals(Promotion.IMAGE_TYPE)) {

            final ArrayList<String> previousPromoImages = editablePromotion.getPromoimages();

            imageUrisString.addAll(previousPromoImages);

            for (int i = 0; i < previousPromoImages.size(); i++) {

                final String promoImageUrl = previousPromoImages.get(i);

                progressBars[i].setVisibility(View.VISIBLE);

                final int finalI = i;
                Picasso.get().load(promoImageUrl).fit()
                        .centerCrop().into(promoImages[i], new Callback() {
                    @Override
                    public void onSuccess() {

                        progressBars[finalI].setVisibility(View.INVISIBLE);
                        addPromoButton.setClickable(true);
                        closePromoImages[finalI].setVisibility(View.VISIBLE);

                        closePromoImages[finalI].setOnClickListener(view -> {
                            progressBars[finalI].setVisibility(View.VISIBLE);


//                            firebaseStorage.getReferenceFromUrl(imageUrisString.get(finalI)).delete()
//                                    .addOnCompleteListener(task1 -> {
//                                        if (task1.isSuccessful()) {

                            int removeIndex;

                            if (imageUrisString.size() <= finalI) {
                                removeIndex = Math.min(finalI, imageUrisString.size() - 1);
                            } else {
                                removeIndex = finalI;
                            }


                            imageUrisString.remove(removeIndex);
                            progressBars[finalI].setVisibility(View.INVISIBLE);
                            closePromoImages[finalI].setVisibility(View.INVISIBLE);
                            promoImages[finalI].setImageResource(R.drawable.ic_add_black_24dp);
                            promoImages[finalI].setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                            promoImages[finalI].setOnClickListener(v -> {
                                imageNumber = finalI;
                                showPromoOptions();
                            });

//                                        }
//                                    })
//                                    .addOnFailureListener(e -> Log.d("ttt", e.getLocalizedMessage()));
                        });

                        promoImages[finalI].setOnClickListener(view ->
                                FullScreenImagesUtil.showImageFullScreen(PromotionActivity.this,
                                        previousPromoImages.get(finalI), null));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("ttt", "uri load failed");
                    }
                });

                closePromoImages[i].setOnClickListener(view -> {

                    progressBars[finalI].setVisibility(View.VISIBLE);
                    firebaseStorage.getReferenceFromUrl(promoImageUrl).delete()
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {

                                    int removeIndex;

                                    if (imageUrisString.size() <= finalI) {
                                        removeIndex = Math.min(finalI, imageUrisString.size() - 1);
                                    } else {
                                        removeIndex = finalI;
                                    }

                                    imageUrisString.remove(removeIndex);
                                    progressBars[finalI].setVisibility(View.INVISIBLE);
                                    closePromoImages[finalI].setVisibility(View.INVISIBLE);
                                    promoImages[finalI].setImageResource(R.drawable.ic_add_black_24dp);
                                    promoImages[finalI].setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                                    promoImages[finalI].setOnClickListener(v -> {
                                        imageNumber = finalI;
                                        showPromoOptions();
                                    });

                                }
                            }).addOnFailureListener(e -> Log.d("ttt", e.getLocalizedMessage()));

                });

            }
        } else if (promoType.equals(Promotion.VIDEO_TYPE)) {

            try {

                videoPromoImage.setImageBitmap(retrieveVideoFrameFromUrl(editablePromotion.getVideoUrl()));
                videoPromoImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                videoPromoImage.setClickable(false);

                videoPlayIv.setVisibility(View.VISIBLE);
                videoPromoImage.setOnClickListener(this);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }


            videoThumbnail = editablePromotion.getVideoThumbnail();

            Picasso.get().load(videoThumbnail).fit().centerCrop().into(videoThumbnailPromoImage,
                    new Callback() {
                        @Override
                        public void onSuccess() {
                            videoThumbnailPromoImage.setOnClickListener(view ->
                                    FullScreenImagesUtil.showImageFullScreen(PromotionActivity.this,
                                            videoThumbnail, null));
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d("ttt", "uri load failed");
                        }
                    });

            videoThumbnailCloseImage.setVisibility(View.VISIBLE);

            videoThumbnailCloseImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

//                            editablePromotion.setVideoThumbnail(null);
                    videoThumbnail = null;
                    videoThumbnailProgressBar.setVisibility(View.INVISIBLE);
                    videoThumbnailCloseImage.setVisibility(View.INVISIBLE);
                    videoThumbnailPromoImage.setImageResource(R.drawable.ic_add_black_24dp);

                    videoThumbnailPromoImage.setOnClickListener(v -> {
                        imageNumber = 5;
                        showPromoOptions();
                    });

//                            videoThumbnailProgressBar.setVisibility(View.VISIBLE);
//                            firebaseStorage.getReferenceFromUrl(editablePromotion.getVideoThumbnail()).delete()
//                                    .addOnCompleteListener(task1 -> {
//                                        if (task1.isSuccessful()) {
//
//
//                                        }
//                                    }).addOnFailureListener(e -> Log.d("ttt", e.getLocalizedMessage()));
                }
            });

        }

        for (TextView categoryTextView : categoryTextViews) {
            if (categoryTextView.getText().toString().equals(editablePromotion.getType())) {
                categoryTextView.performClick();
                break;
            }
        }

        titleEd.setText(editablePromotion.getTitle());

        priceEd.setText(String.format(Locale.getDefault(), "%.9f", editablePromotion.getPrice()));

        currencySpinner.setSelection(supportedCurrencyCodes.indexOf(Currency.getInstance(editablePromotion.getCurrency())));

        descriptionEd.setText(editablePromotion.getDescription());
        phoneCheckBox.setChecked(editablePromotion.isHidePhone());


    }

    private void checkPromoType() {

        if (isForEditing) {
            addPromoButton.setText("تعديل الإعلان");
        }

        final TextView titleTv = findViewById(R.id.titleTv),
                promoTypeTv = findViewById(R.id.promoTypeTv);

        switch (promoType) {
            case Promotion.TEXT_TYPE:

                titleTv.setText(isForEditing ? "تعديل الاعلان النصي" : "إضافة اعلان نصي");

                if (!isForEditing) {
                    findViewById(R.id.videoLinearLayout).setVisibility(View.GONE);
                    findViewById(R.id.videoSizeInfoTv).setVisibility(View.GONE);

                    findViewById(R.id.promoTypeTv).setVisibility(View.GONE);
                    findViewById(R.id.horizontal_scroll).setVisibility(View.GONE);
                } else {

                    promoTypeTv.setText("يمكنك تعديل صور الاعلان");

                    setUpForImagePromo();
                }

                break;
            case Promotion.VIDEO_TYPE:


                titleTv.setText(isForEditing ? "تعديل اعلان الفيديو" : "إضافة اعلان فيديو");

//            ((TextView) findViewById(R.id.titleTv)).setText("إضافة اعلان فيديو");

                promoTypeTv.setText(isForEditing ? "يمكنك تعديل صورة الاعلان" :
                        "قم بإضافة فيديو وصورة لاعلانك");

                findViewById(R.id.horizontal_scroll).setVisibility(View.GONE);

                videoPromoImage = findViewById(R.id.videoPromoImage);
                videoProgressBar = findViewById(R.id.videoProgressBar);
                videoCardView = findViewById(R.id.videoCardView);
                videoPlayIv = findViewById(R.id.videoPlayIv);


                videoThumbnailPromoImage = findViewById(R.id.videoThumbnailPromoImage);
                videoThumbnailCloseImage = findViewById(R.id.videoThumbnailCloseImage);
                videoThumbnailProgressBar = findViewById(R.id.videoThumbnailProgressBar);


                if (!isForEditing) {
                    videoCardView.setOnClickListener(this::chooseVideo);

                    videoThumbnailPromoImage.setOnClickListener(v -> {
                        imageNumber = 5;
                        showPromoOptions();
                    });

                }

                break;
            case Promotion.IMAGE_TYPE:

                titleTv.setText(isForEditing ? "تعديل الاعلان بصور" : "إضافة اعلان بصور");

                promoTypeTv.setText(isForEditing ? "يمكنك تعديل او ازالة صور اعلانك" :
                        "قم بإضافة صور لاعلانك");

                setUpForImagePromo();

                break;
        }


    }

    private void setUpForImagePromo() {


        findViewById(R.id.videoLinearLayout).setVisibility(View.GONE);
        findViewById(R.id.videoSizeInfoTv).setVisibility(View.GONE);

        progressBars = new ProgressBar[]{findViewById(R.id.progress_bar1),
                findViewById(R.id.progress_bar2),
                findViewById(R.id.progress_bar3)
//              ,
//              findViewById(R.id.progress_bar4)
        };

        promoImages = new ImageView[]{findViewById(R.id.promoimage1),
                findViewById(R.id.promoimage2),
                findViewById(R.id.promoimage3)
//              ,
//              findViewById(R.id.promoimage4)
        };

        closePromoImages = new ImageView[]{findViewById(R.id.closePromoImage1),
                findViewById(R.id.closePromoImage2),
                findViewById(R.id.closePromoImage3)
//              ,
//              findViewById(R.id.closePromoImage4)
        };

        for (int i = 0; i < 3; i++) {
//        int finalI = i;
            int finalI = i;
            promoImages[i].setOnClickListener(view -> {
//          getImage();
                imageNumber = finalI;
                showPromoOptions();
//          imageNumber = finalI;
            });
        }
        imageUrisString = new ArrayList<>();

    }

    void attachListeners() {


        titleEd.addTextChangedListener(new TextCounter(titleLengthTv));
        descriptionEd.addTextChangedListener(new TextCounter(descLengthTv));
        addPromoButton.setOnClickListener(this);


        final int textGreyColor = getResources().getColor(R.color.textGreyColor),
                textWhiteColor = getResources().getColor(R.color.white);


        for (int i = 0; i < categoryTextViews.length; i++) {
            final TextView textView = categoryTextViews[i];
            final ImageView imageView = categoryImageViews[i];
            textView.setOnClickListener(v -> {
                if (textView.getCurrentTextColor() == textGreyColor) {

                    for (int j = 0; j < categoryTextViews.length; j++) {

                        categoryTextViews[j].setBackgroundResource(R.drawable.filter_grey_back);
                        categoryTextViews[j].setTextColor(textGreyColor);

                        DrawableCompat.setTint(
                                DrawableCompat.wrap(categoryImageViews[j].getDrawable()),
                                textGreyColor
                        );
                        categoryImageViews[j].setBackgroundResource(R.drawable.grey_circle_back);
                    }

                    textView.setBackgroundResource(R.drawable.filter_red_back);
                    textView.setTextColor(textWhiteColor);

                    DrawableCompat.setTint(
                            DrawableCompat.wrap(imageView.getDrawable()),
                            textWhiteColor
                    );
                    imageView.setBackgroundResource(R.drawable.red_circle_back);

//                    if (textView.getText().toString().equals("هواتف")) {
//                        category = "هواتف";
//                        return;
//                    }
//                    if (textView.getText().toString().equals("الكترونيات")) {
//                        category = "الكترونيات";
//                        return;
//                    }
                    category = textView.getText().toString();

                } else {
                    textView.setBackgroundResource(R.drawable.filter_grey_back);
                    textView.setTextColor(textGreyColor);

                    DrawableCompat.setTint(
                            DrawableCompat.wrap(imageView.getDrawable()),
                            textGreyColor
                    );
                    imageView.setBackgroundResource(R.drawable.grey_circle_back);

                    category = "";
                }
            });
            imageView.setOnClickListener(v -> textView.performClick());
        }


    }

    private void getImage() {

        if (needsToRequestStoragePermissions(IMAGE_STORAGE_REQUEST)) {
            if (WifiUtil.checkWifiConnection(this)) {
                startActivityForResult(Intent.createChooser(
                        new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
                        "Select Image"), PICK_IMAGE);
            }
        }

    }

    private void getCamera() {

        if (ActivityCompat.checkSelfPermission(PromotionActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
            }
            return;
        }

        if (WifiUtil.checkWifiConnection(this)) {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(
                            this,
                            "com.example.yousef.rbenoapplication.provider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, PICK_CAMERA);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());

        final String imageFileName = "JPEG_" + timeStamp + "_";

        final File image = File.createTempFile(
                imageFileName,
                ".jpg",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        );

        // Save a file: path for use with ACTION_VIEW intents
        cameraImageFilePath = image.getAbsolutePath();
        return image;
    }

    public boolean needsToRequestStoragePermissions(int code) {
        final String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, code);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCamera();
            } else {
                Toast.makeText(this, "هذا التطبيق يحتاج الى الوصول الكاميرا!",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == IMAGE_STORAGE_REQUEST) {
            if (WifiUtil.checkWifiConnection(this)) {
                startActivityForResult(Intent.createChooser(
                        new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
                        "Select Image"), PICK_IMAGE);
            }
        } else if (requestCode == VIDEO_STORAGE_REQUEST) {

            videoCardView.setClickable(false);
            startActivityForResult(Intent.createChooser(
                    new Intent(Intent.ACTION_GET_CONTENT)
                            .setType("video/*")
                            .addCategory(Intent.CATEGORY_OPENABLE), "اختر فيديو لاعلانك"),
                    PICK_VIDEO);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("ttt", "activity result");

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null
                && data.getData() != null) {
            Log.d("ttt", "picking imgae and data is not null man");
            if (imageNumber == 5) {
                videoImageSelected(data.getData());
            } else {
                Log.d("ttt", "picking image for slider");
                selectedImage(imageNumber, data.getData());
            }

        } else if (resultCode == RESULT_OK && requestCode == PICK_VIDEO && data != null &&
                data.getData() != null) {

            Log.d("videoUpload", "Video is picked");
            videoUri = data.getData();

            if (videoUri != null && isNewGooglePhotosUri(videoUri)) {
                Toast.makeText(this, "لا يمكن رفع فيديو مباشرة من صور جوجل!" +
                        " يجب تحميل الفيديو اولا ثم المحاولة مرة اخرى", Toast.LENGTH_LONG).show();
                videoCardView.setClickable(true);
                return;
            }

            final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, videoUri);
            final String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//      final String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);

            if (time == null) {
                Toast.makeText(PromotionActivity.this, "لقد فشل رفع الفيديو!", Toast.LENGTH_SHORT).show();
                videoCardView.setClickable(false);
                return;
            }

            final long timeInMillisec = Long.parseLong(time);
//      long size = (Long.parseLong(bitrate) / 8 * timeInMillisec / 1000/1000);
//
//      Log.d("ttt","video size: "+size);
            Log.d("ttt", "video size in mb: " + getSizeInMB(videoUri));
            Log.d("ttt", "initial video duration: " + timeInMillisec);

//            MediaExtractor mediaExtractor = new MediaExtractor();
//
//            try {
//                mediaExtractor.setDataSource(videoUri.getPath());
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            mediaExtractor.seekTo(1000,MediaExtractor.SEEK_TO_CLOSEST_SYNC);

//            mediaExtractor.ad

            if ((timeInMillisec / 1000) > videoLengthMaxLimit) {

                retriever.release();
                videoCardView.setClickable(true);

//                Log.d("ttt","video uri path: "+videoUri.getPath());
//                Log.d("ttt","video file path: "+new File(videoUri.getPath()).getPath());
//
//
//                File file = new File("newFile.mp4");
//
//                FileInputStream fis = null;
//                try {
//
//                    fis = new FileInputStream(new File(videoUri.getPath()));
//                    FileDescriptor fd = fis.getFD();
//
//                    Log.d("ttt","initial file size: " + file.length());
//                    try {
//                        genVideoUsingMuxer(fd,file.getPath(),videoUri.getPath(),0,1000,true,true);
//
//                        Log.d("ttt","new file size: " + file.length());
//
//                        final MediaMetadataRetriever newMediaRetreiver = new MediaMetadataRetriever();
//                        newMediaRetreiver.setDataSource(this, Uri.fromFile(file));
//                        final String newDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//
//                        Log.d("ttt","new video duration: " + newDuration);
//
//                    } catch (IOException e) {
//                        Log.d("ttt","exceotion: "+e.getMessage());
//                        e.printStackTrace();
//                    }
//
//                } catch (Exception e) {
//                    Log.d("ttt","FileInputStream: "+e.getMessage());
//                    e.printStackTrace();
//                } finally {
//                    //Release stuff
//                    try {
//                        if(fis != null) {
//                            fis.close();
//                        }
//                    } catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//


                Toast.makeText(this, "طول الفيديو يجب ان لا يزيد عن " + videoLengthMaxLimit + " ثانية!",
                        Toast.LENGTH_SHORT).show();

            } else if ((timeInMillisec / 1000) < videoLengthMinLimit) {

                videoCardView.setClickable(true);
                Toast.makeText(this, "طول الفيديو يجب ان لا يقل عن " + videoLengthMinLimit + " ثواني!",
                        Toast.LENGTH_SHORT).show();

            } else {

                if (getSizeInMB(videoUri) <= videoMaxSizeInMB) {
                    ((TextView) findViewById(R.id.videoSizeTv)).setText("ثانية " + timeInMillisec / 1000);

                    videoThumbnailBitmap = retriever.getFrameAtTime(1000);

                    videoPromoImage.setImageBitmap(videoThumbnailBitmap);
                    videoPromoImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    videoPromoImage.setClickable(false);

                    videoPlayIv.setVisibility(View.VISIBLE);
                    videoPromoImage.setOnClickListener(this);

                    retriever.release();
                    Log.d("videoUpload", "Video bitmap is set");

                    UploadVideo(videoUri);
                } else {

                    videoCardView.setClickable(true);
                    Toast.makeText(this, "حجم الفيديو يجب ان لا يزيد عن 15 ميجا بايت!",
                            Toast.LENGTH_SHORT).show();

                }

            }

        } else if (requestCode == PICK_CAMERA && resultCode == RESULT_OK) {
            Log.d("ttt", "getting camera result");

            if (cameraImageFilePath != null && !cameraImageFilePath.isEmpty()) {
                final Uri uri = Uri.fromFile(new File(cameraImageFilePath));

                if (uri == null) {
                    Toast.makeText(PromotionActivity.this,
                            "لقد فشل رفع الفيديو!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (imageNumber == 5) {

                    videoImageSelected(uri);

                    if (videoCardView != null) {
                        videoCardView.setClickable(true);
                    }
                } else {
                    selectedImage(imageNumber, uri);
                }

            } else {
                Toast.makeText(PromotionActivity.this,
                        "لقد فشل رفع الفيديو!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_VIDEO && resultCode == RESULT_CANCELED) {

            Log.d("ttt", "cancelled picking video");
            videoCardView.setClickable(true);
        }
    }

    private double getSizeInMB(Uri uri) {
        String fileSize;
        try (Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null,
                        null)) {
            if (cursor != null && cursor.moveToFirst()) {
                // get file size
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (!cursor.isNull(sizeIndex)) {
                    fileSize = cursor.getString(sizeIndex);
                    return Long.parseLong(fileSize) / 1e+6;
                }
            }
        }
        return 0;
    }

    private void UploadVideo(Uri videoUri) {

        final StorageReference videoRef = firebaseStorage.getReference().child("videos/" + UUID.randomUUID().toString());

        videoProgressBar.setVisibility(View.VISIBLE);
        if (uploadTasks == null)
            uploadTasks = new HashMap<>();

        final UploadTask uploadTask = videoRef.putFile(videoUri);

        OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener;

        uploadTask.addOnSuccessListener(onSuccessListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                                          uploadTasks.remove(uploadTask);
                            Log.d("videoUpload", "Video is uploaded");
                            videoProgressBar.setVisibility(View.GONE);
                            videoDownloadUri = uri.toString();
                            Toast.makeText(PromotionActivity.this,
                                    "تم تحميل الفيديو الخاص بك!",
                                    Toast.LENGTH_SHORT).show();
                        });

                    }
                }

        ).addOnProgressListener(taskSnapshot ->
        {
            videoProgressBar.setProgress((int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount()));
            Log.d("videoUpload", "Video is uploading: " + videoProgressBar.getProgress());
        }).addOnFailureListener(e -> {
            Log.d("videoUpload", "Video failed to upload");
            videoCardView.setClickable(true);
            videoProgressBar.setVisibility(View.INVISIBLE);
            videoPromoImage.setVisibility(View.VISIBLE);
            Toast.makeText(PromotionActivity.this, "لقد فشل رفع الفيديو!"
                    , Toast.LENGTH_SHORT).show();
            Log.d("ttt", e.toString());
        });

        uploadTasks.put(uploadTask, onSuccessListener);
    }

    boolean isNewGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }

    boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    boolean isPhoneValid(CharSequence phone) {
        return Patterns.PHONE.matcher(phone).matches();
    }

    boolean isUrlValid(CharSequence url) {
        return android.util.Patterns.WEB_URL.matcher(url).matches();
    }

    public void publishPromo(String title, String description, boolean negotitation,
                             boolean hidePhone, double price, List<String> titleSpilitting,
                             ProgressDialog progressDialog) {

        final DocumentReference promoidRef = promoRef.document("promotionidnum");

        addPromoButton.setClickable(false);

        promoidRef.get().addOnSuccessListener(documentSnapshot -> {
            final long id = documentSnapshot.getLong("count");
            Map<String, Object> data = new HashMap<>();
            data.put("type", category);
            data.put("title", title);
//            data.put("city",city);
            data.put("description", description);
            data.put("negotiable", negotitation);
            data.put("hidePhone", hidePhone);
            data.put("price", price);
            data.put("promoid", id);
            data.put("publishtime", System.currentTimeMillis() / 1000);
            data.put("rating", 0);
            data.put("uid", auth.getUid());
            data.put("favcount", 0);
            data.put("viewcount", 0);

            data.put("country", GlobalVariables.getInstance().getCountryCode());

            final SharedPreferences sharedPreferences
                    = getSharedPreferences("rbeno", Context.MODE_PRIVATE);

            data.put("cityName", sharedPreferences.getString("cityName", null));

            String selectedCurrency;
            if (currentlySelectedCurrencyCode != null) {
                selectedCurrency = currentlySelectedCurrencyCode;
            } else {
                selectedCurrency = sharedPreferences.getString("currency", null);
            }

            data.put("currency", selectedCurrency);
            data.put("keyWords", titleSpilitting);
            data.put("isBanned", false);
            data.put("isPaused", false);


            if (promoType.equals(Promotion.VIDEO_TYPE)) {
                data.put("videoUrl", videoDownloadUri);
                if (!videoThumbnail.isEmpty()) {
                    data.put("videoThumbnail", videoThumbnail);
                }
            } else if (promoType.equals(Promotion.IMAGE_TYPE)) {
                data.put("promoimages", imageUrisString);
            }

            data.put("promoType", promoType);
            promoRef.document(String.valueOf(id)).set(data).addOnSuccessListener(documentReference ->
                            promoidRef.update("count", FieldValue.increment(1),
                                    "promoCount", FieldValue.increment(1))
                                    .addOnSuccessListener(aVoid -> {

                                                //              promoidRef.update("promoCount", FieldValue.increment(1)).addOnSuccessListener(aVoid1 ->
//                                        promoRef.document(documentReference.getId()).
//                                                collection("ratings").add(new PromoRating()).addOnSuccessListener(documentReference1 -> {

//                                          CollectionReference usersRef = firestore.collection("users");
//                                          usersRef.whereEqualTo("userId", auth.getCurrentUser().getUid())
//                                                  .get().addOnSuccessListener(snapshots -> {

                                                FirebaseFirestore.getInstance()
                                                        .collection("users")
                                                        .document(auth.getCurrentUser().getUid())
                                                        .update("promoTypesPublished." + promoType,
                                                                FieldValue.increment(1))
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                progressDialog.dismiss();
                                                                Toast.makeText(PromotionActivity.this,
                                                                        "تمت اضافة الإعلان بنجاح!",
                                                                        Toast.LENGTH_SHORT).show();

                                                                Intent output = new Intent();
                                                                output.putExtra("addedPromoId", id);

                                                                setResult(RESULT_OK, output);
                                                                finish();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                    }
                                                });

//                                          });
//                                                .addOnSuccessListener(queryDocumentSnapshots ->
//
//                                                usersRef.document(queryDocumentSnapshots.getDocuments().get(0).getId())
//                                                        .update("MyPromos", FieldValue.arrayUnion(documentReference.getId()))
//                                                        .addOnSuccessListener(aVoid2 -> {
//                                                  progressDialog.dismiss();
//                                                  Toast.makeText(PromotionActivity.this, "تمت اضافة الإعلان بنجاح!", Toast.LENGTH_SHORT).show();
//
//                                                  Intent output = new Intent();
//                                                  output.putExtra("addedPromoId", id);
//                                                  setResult(3, output);
//                                                  finish();
//                                                }));


//                                        });
//              )

                                            }

                                    )
            );
        }).addOnFailureListener(e -> {
            Toast.makeText(PromotionActivity.this, "لقد فشل اضافة الإعلان الرجاء المحاولة مرة اخرى!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            addPromoButton.setClickable(true);
        });
    }

    @Override
    public void onBackPressed() {
        showAlertDialog();
    }

    void showAlertDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("هل تريد الخروج؟");

        String message;
        if (isForEditing) {
            message = "الخروج قبل الانتهاء من اضافة الاعلان سيؤدي الى فقدانه";
        } else {
            message = "الخروج قبل الانتهاء من تعديل الاعلان سيؤدي الى الغاء التعديلات";
        }

        alert.setMessage(message);

        alert.setPositiveButton("نعم", (dialog, which) -> {
            Toast.makeText(PromotionActivity.this, "لم تتم اضافة الاعلان"
                    , Toast.LENGTH_SHORT).show();

            if (storageReferences != null && !storageReferences.isEmpty()) {
                for (StorageReference storageReference : storageReferences) {

                    for (FileDownloadTask task : storageReference.getActiveDownloadTasks()) {

                        task.addOnCanceledListener(new OnCanceledListener() {
                            @Override
                            public void onCanceled() {
                                Log.d("ttt", "cancelled task: " +
                                        task.getSnapshot().getStorage().getPath());
                            }
                        });

                        Log.d("ttt", "caneccling task");

                        task.cancel();

                    }
                }
            }

            if (uploadTasks != null && !uploadTasks.isEmpty()) {
                for (UploadTask uploadTask : uploadTasks.keySet()) {

                    if (uploadTask.isComplete()) {
                        Log.d("ttt", "task complete so deleting from ref");
                        uploadTask.getSnapshot().getStorage().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("ttt", "ref delete sucess");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("ttt", "ref delete failed: " + e.getMessage());
                            }
                        });

                    } else {


                        Log.d("ttt", "task not complete so adding new listener, " +
                                "and trying to cancel: " + uploadTask.cancel());

                        if (uploadTasks.containsKey(uploadTask)) {
                            uploadTask.removeOnSuccessListener(uploadTasks.get(uploadTask));
                        }

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d("ttt", "task new listener sucess");
                                uploadTask.getSnapshot().getStorage().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("ttt", "ref delete sucess");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("ttt", "ref delete failed: " + e.getMessage());
                                    }
                                });

                            }
                        });

                    }

//          if(!uploadTask.isComplete()){
//
//            uploadTask.cancel();
//
//          } else{
//
////            checkAndDeleteVideoThumbNailFromStorage();
//            Log.d("ttt","uploadTask.getSnapshot().getStorage().getPath(): "+
//                    uploadTask.getSnapshot().getStorage().getPath());
//            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//              @Override
//              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                Log.d("ttt","deletion sucess listener");
//              }
//            });
//
//            uploadTask.getSnapshot().getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
//
//              Log.d("ttt","getDownloadUrl "+uri.toString());
//
//            });
//
//          }
                }
            }
//      checkAndDeleteVideoThumbNailFromStorage();

            if (!isForEditing) {

                if (imageUrisString != null && !imageUrisString.isEmpty()) {
                    for (String imageUri : imageUrisString) {
                        firebaseStorage.getReferenceFromUrl(imageUri).delete();
                    }
                }

                if (videoUri != null) {
                    if (videoDownloadUri != null && !videoDownloadUri.isEmpty()) {
                        firebaseStorage.getReferenceFromUrl(videoDownloadUri).delete();
                    }
                }

            } else {

                if (editablePromotion.getType().equals(Promotion.IMAGE_TYPE)) {

                    for (String promoImage : imageUrisString) {
                        if (!editablePromotion.getPromoimages().contains(promoImage)) {
                            firebaseStorage.getReferenceFromUrl(promoImage).delete();
                        }
                    }
                }

                if (editablePromotion.getType().equals(Promotion.VIDEO_TYPE)) {

                    if (!videoThumbnail.equals(editablePromotion.getVideoThumbnail())) {
                        firebaseStorage.getReferenceFromUrl(videoThumbnail).delete();
                    }

                }


            }

            if (isForEditing && editablePromotion != null && !editablePromotion.getIsPaused()) {
                promoRef.document(String.valueOf(editablePromotion.getPromoid()))
                        .update("isPaused", false);
            }


            dialog.dismiss();

            super.onBackPressed();
        });
        alert.setNegativeButton("لا", (dialog, which) -> {
            dialog.cancel();
        });
        alert.create().show();

    }

//    void checkAndDeleteVideoThumbNailFromStorage() {
//        if (videoThumbnail != null && !videoThumbnail.isEmpty()) {
//            firebaseStorage.getReferenceFromUrl(videoThumbnail).delete()
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            videoThumbnail = null;
//                            Log.d("ttt", "video thumbail deleted");
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.d("ttt", "video thumbail deleted error: " + e.getMessage());
//                }
//            }).addOnCanceledListener(new OnCanceledListener() {
//                @Override
//                public void onCanceled() {
//                    Log.d("ttt", "video thumbail delete cancelled");
//                }
//            });
//        }
//    }

    void chooseVideo(View view) {

        if (needsToRequestStoragePermissions(VIDEO_STORAGE_REQUEST)) {
            videoCardView.setClickable(false);
            startActivityForResult(Intent.createChooser(
                    new Intent(Intent.ACTION_GET_CONTENT)
                            .setType("video/*")
                            .addCategory(Intent.CATEGORY_OPENABLE), "اختر فيديو لاعلانك"), PICK_VIDEO);
        }

    }

    void showPromoOptions() {

        final BottomSheetDialog bsd = new BottomSheetDialog(PromotionActivity.this);
        final View parentView = getLayoutInflater().inflate(R.layout.image_chooser_bottom_layout, null);

        parentView.findViewById(R.id.imageGalleryIv).setOnClickListener(v -> {
            getImage();
            bsd.dismiss();
        });
        parentView.findViewById(R.id.imageCameraIv).setOnClickListener(v -> {
            getCamera();
            bsd.dismiss();
        });

        bsd.setContentView(parentView);
        bsd.show();
    }

    void selectedImage(int index, Uri promoImageUri) {


        Log.d("ttt", "selecting image for: " + index);

        progressBars[index].setVisibility(View.VISIBLE);

        Log.d("ttt", "image size: " + promoImages[index].getHeight());

        if (promoImageUri != null) {

            Picasso.get().load(promoImageUri).fit().centerCrop().into(promoImages[index], new Callback() {
                @Override
                public void onSuccess() {
                    promoImages[index].setOnClickListener(view ->
                            FullScreenImagesUtil.showImageFullScreen(PromotionActivity.this,
                                    null, promoImageUri));
                }

                @Override
                public void onError(Exception e) {
                    Log.d("ttt", "uri load failed");
                }
            });


            addPromoButton.setClickable(false);
            progressBars[index].setVisibility(View.VISIBLE);


            final StorageReference reference = firebaseStorage.getReference()
                    .child("images/" + UUID.randomUUID().toString());

            if (uploadTasks == null)
                uploadTasks = new HashMap<>();

            final UploadTask uploadTask = reference.putFile(promoImageUri);

            OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener;


            uploadTask.addOnSuccessListener(onSuccessListener = taskSnapshot -> {

//        uploadTasks.remove(uploadTask);

                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrisString.add(uri.toString());

                }).addOnCompleteListener(task -> {
                    progressBars[index].setVisibility(View.INVISIBLE);
                    addPromoButton.setClickable(true);
                    closePromoImages[index].setVisibility(View.VISIBLE);
                    closePromoImages[index].setOnClickListener(view -> {
                        progressBars[index].setVisibility(View.VISIBLE);

                        int removeIndex;

                        if (imageUrisString.size() <= index) {
                            removeIndex = Math.min(index, imageUrisString.size() - 1);
                        } else {
                            removeIndex = index;
                        }

                        firebaseStorage.getReferenceFromUrl(imageUrisString.get(removeIndex)).delete()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {

                                        uploadTasks.remove(uploadTask);

                                        imageUrisString.remove(removeIndex);
                                        progressBars[index].setVisibility(View.INVISIBLE);
                                        closePromoImages[index].setVisibility(View.INVISIBLE);
                                        promoImages[index].setImageResource(R.drawable.ic_add_black_24dp);
                                        promoImages[index].setScaleType(ImageView.ScaleType.CENTER_INSIDE);

//                promoImages[index].setClickable(true);
                                        promoImages[index].setOnClickListener(v -> {
                                            imageNumber = index;
                                            showPromoOptions();
                                        });

                                    }
                                }).addOnFailureListener(e -> Log.d("ttt", e.getLocalizedMessage()));
                    });
                });
            });

            uploadTasks.put(uploadTask, onSuccessListener);

        }
    }

    void videoImageSelected(Uri uri) {

        Picasso.get().load(uri).fit().centerCrop().into(videoThumbnailPromoImage,
                new Callback() {
                    @Override
                    public void onSuccess() {
                        videoThumbnailPromoImage.setOnClickListener(view ->
                                FullScreenImagesUtil.showImageFullScreen(PromotionActivity.this,
                                        null, uri));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("ttt", "uri load failed");
                    }
                });

        videoThumbnailProgressBar.setVisibility(View.VISIBLE);
        final StorageReference videoImageRef = firebaseStorage.getReference().child("images/" +
                UUID.randomUUID().toString());


        if (storageReferences == null)
            storageReferences = new ArrayList<>();

        storageReferences.add(videoImageRef);

        if (uploadTasks == null)
            uploadTasks = new HashMap<>();

        final UploadTask uploadTask = videoImageRef.putFile(uri);

        OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener;

        uploadTask.addOnSuccessListener(onSuccessListener = taskSnapshot -> {
//      uploadTasks.remove(uploadTask);
            videoImageRef.getDownloadUrl().addOnSuccessListener(url -> {
                videoThumbnailProgressBar.setVisibility(View.GONE);

                videoThumbnail = url.toString();
                Log.d("ttt", "videoThumbnail: " + videoThumbnail);
                addPromoButton.setClickable(true);
                videoThumbnailCloseImage.setVisibility(View.VISIBLE);
                videoThumbnailCloseImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        videoThumbnailProgressBar.setVisibility(View.VISIBLE);
                        firebaseStorage.getReferenceFromUrl(videoThumbnail).delete()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        uploadTasks.remove(uploadTask);

                                        videoThumbnail = null;
                                        videoThumbnailProgressBar.setVisibility(View.INVISIBLE);
                                        videoThumbnailCloseImage.setVisibility(View.INVISIBLE);
                                        videoThumbnailPromoImage.setImageResource(R.drawable.ic_add_black_24dp);

                                        videoThumbnailPromoImage.setOnClickListener(v -> {
                                            imageNumber = 5;
                                            showPromoOptions();
                                        });

                                    }
                                }).addOnFailureListener(e -> Log.d("ttt", e.getLocalizedMessage()));
                    }
                });
            }).addOnFailureListener(e -> Log.d("ttt", "videoThumbnailFAILED : " +
                    e.getMessage()));
        });

        uploadTasks.put(uploadTask, onSuccessListener);
    }

    private Bitmap retrieveVideoFrameFromUrl(String videoPath) throws Throwable {
        Bitmap bitmap;
        MediaMetadataRetriever mediaMetadataRetriever = null;
//        FileOutputStream outStream = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<>());
            bitmap = mediaMetadataRetriever.getFrameAtTime();

//            outStream = new FileOutputStream(new File(getExternalCacheDir().getAbsolutePath()+ "newVideo" + ".jpg"));
//
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, outStream);


        } catch (Exception e) {

            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    void startPromoUpload(String title, String description, boolean negotitation,
                          boolean hidePhone, double price, ProgressDialog progressDialog) {

        if (WifiUtil.checkWifiConnection(this)) {

            publishPromo(title, description, negotitation, hidePhone,
                    price, Arrays.asList(title.split(" ")), progressDialog);

//      promoRef.whereEqualTo("uid", auth.getCurrentUser().getUid()).
//              orderBy("publishtime", Query.Direction.DESCENDING).get().
//              addOnCompleteListener(task -> {
//                if (task.isSuccessful() && !task.getResult().isEmpty()) {
//                  List<DocumentSnapshot> documents = task.getResult().getDocuments();
////                                if (documents.size() < 5) {
////                                    long diff = System.currentTimeMillis() - documents.get(0).getLong("publishtime");
////                                    if (diff <= 12 * HOUR_MILLIS) {
////                                        AlertDialog.Builder alert = new AlertDialog.Builder(PromotionActivity.this);
////                                        alert.setTitle("لا يمكنك نشر اعلان حاليا");
////                                        alert.setMessage("لا يمكنك ان تنشر اعلان الا بعد مرور 12 ساعة على اعلانك السابق!");
////                                        alert.create().show();
////                                    } else {
//
//                  publishPromo(title, description, negotitation, price, Arrays.asList(title.split(" ")), progressDialog);
////                                    }
////                                } else {
////                                    AlertDialog.Builder alert = new AlertDialog.Builder(PromotionActivity.this);
////                                    alert.setTitle("لقد تجاوزت الحد المسموح للاعلانات");
////                                    alert.setMessage("لا يمكنك امتلاك اكثر من خمسة اعلانات منشورة في نفس الوقت!");
////                                    alert.create().show();
////                                }
//                } else {
//                  publishPromo(title, description, negotitation, price, Arrays.asList(title.split(" ")), progressDialog);
//                }
//              }).addOnFailureListener(e -> {
//        progressDialog.dismiss();
//        Toast.makeText(PromotionActivity.this, e.getLocalizedMessage(),
//                Toast.LENGTH_SHORT).show();
//      });
        }
    }

    private void createCurrencyCodeSpinner() {


        supportedCurrencyCodes =
                new ArrayList<>(Currency.getAvailableCurrencies());

        final List<String> spinnerArray = new ArrayList<>(supportedCurrencyCodes.size());


        String defaultCode;

        final SharedPreferences sharedPreferences
                = getSharedPreferences("rbeno", Context.MODE_PRIVATE);


        if (sharedPreferences.contains("currency")) {

            defaultCode = Currency.getInstance(
                    sharedPreferences.getString("currency", null)).getCurrencyCode();

        } else if (GlobalVariables.getInstance().getCountryCode() != null &&
                !GlobalVariables.getInstance().getCountryCode().isEmpty()) {

            defaultCode = GlobalVariables.getInstance().getCountryCode();

        } else {

            defaultCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();

        }

        final Locale arabicLocale = new Locale("ar");


        for (Currency code : supportedCurrencyCodes) {
            Log.d("ttt", "code: " + code);
//                Log.d("ttt", "display code: " + code.getDisplayName(arabicLocale));
            spinnerArray.add(code.getDisplayName(arabicLocale));
        }


        final ArrayAdapter<String> ad = new ArrayAdapter<>(this,
                R.layout.small_spinner_item_layout, spinnerArray);

        ad.setDropDownViewResource(R.layout.small_spinner_item_layout);

        currencySpinner.setAdapter(ad);

        currencySpinner.setSelection(supportedCurrencyCodes.indexOf(Currency.getInstance(defaultCode)));

        currentlySelectedCurrencyCode = defaultCode;

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                currentlySelectedCurrencyCode = supportedCurrencyCodes.get(position).getCurrencyCode();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        Log.d("ttt", "defaultCode: " + defaultCode);

        Log.d("ttt", "currency name: " +
                Currency.getInstance(defaultCode).getDisplayName(arabicLocale));


    }

    private static class TextCounter implements TextWatcher {

        private final TextView countTextView;

        TextCounter(TextView countTextView) {
            this.countTextView = countTextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            countTextView.setText(s.length() + "");
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == addPromoButton.getId()) {

            final String title = titleEd.getText().toString().trim(),
                    description = descriptionEd.getText().toString().trim();

            if (!isForEditing) {

                if (promoType.equals(Promotion.IMAGE_TYPE) && imageUrisString.isEmpty()) {
                    Toast.makeText(this, "الرجاء وضع صور لاعلانك!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (promoType.equals(Promotion.VIDEO_TYPE) && videoDownloadUri == null) {
                    Toast.makeText(this, "الرجاء وضع فيديو لاعلانك!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (category == null || category.isEmpty()) {
                Toast.makeText(this, "الرجاء اختيار القسم لاعلانك!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty()) {
                Toast.makeText(this, "الرجاء وضع عنوان لاعلانك!", Toast.LENGTH_SHORT).show();
                return;
            }

            final String[] titleSplit = title.trim().replaceAll("( )+", " ").split(" ");

            for (String titleSplitting : titleSplit) {
                Log.d("promotion", "current title split: " + titleSplitting);
                if (isEmailValid(titleSplitting)) {
                    Toast.makeText(this, "الرجاء عدم وضع ايميل  في عنوان اعلانك!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (isPhoneValid(titleSplitting)) {
                    Toast.makeText(this, "الرجاء عدم وضع رقم هاتف في عنوان اعلانك!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (isUrlValid(titleSplitting)) {
                    Toast.makeText(this, "الرجاء عدم وضع رابط في عنوان اعلانك!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            if (priceEd.getText().toString().isEmpty()) {
                Toast.makeText(this, "الرجاء وضع سعر لاعلانك!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty()) {
                Toast.makeText(this, "الرجاء وضع وصف لاعلانك!", Toast.LENGTH_SHORT).show();
                return;
            }

            final String[] descSplit =
                    description.trim().replaceAll("( )+", " ").split(" ");


            for (String descSplitting : descSplit) {
                if (isUrlValid(descSplitting)) {
                    Toast.makeText(this, "الرجاء عدم وضع رابط في عنوان اعلانك!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

            }

            if (WifiUtil.checkWifiConnection(this)) {

                final ProgressDialog progressDialog = new ProgressDialog(PromotionActivity.this);
                progressDialog.setTitle(isForEditing ? "جاري تعديل الإعلان" : "جاري اضافة الإعلان");
                progressDialog.setCancelable(false);
                progressDialog.setMessage("الرجاء الإنتظار!");
                progressDialog.show();


                if (isForEditing) {

                    editablePromotion.setTitle(title);
                    editablePromotion.setDescription(description);
                    editablePromotion.setType(category);
                    editablePromotion.setKeyWords(Arrays.asList(titleSplit));
                    editablePromotion.setNegotiable(negotiationCheck.isChecked());
                    editablePromotion.setPrice(Double.parseDouble(priceEd.getText().toString().trim()));
                    editablePromotion.setWasEdited(true);
                    editablePromotion.setLastEditTime(System.currentTimeMillis());
                    editablePromotion.setHidePhone(phoneCheckBox.isChecked());

                    if (currentlySelectedCurrencyCode != null) {
                        editablePromotion.setCurrency(currentlySelectedCurrencyCode);
                    } else if (editablePromotion.getCurrency() == null) {
                        editablePromotion.setCurrency(
                                getSharedPreferences("rbeno", Context.MODE_PRIVATE)
                                        .getString("currency", null));
                    }
                    if (promoType.equals(Promotion.IMAGE_TYPE) && imageUrisString.isEmpty()) {

                        editablePromotion.setPromoType(Promotion.TEXT_TYPE);

                    } else if (promoType.equals(Promotion.TEXT_TYPE) && imageUrisString != null
                            && !imageUrisString.isEmpty()) {

                        editablePromotion.setPromoType(Promotion.IMAGE_TYPE);
                    }

                    if (promoType.equals(Promotion.VIDEO_TYPE)) {


                        Bitmap uploadBitmap = null;

                        if (videoThumbnail == null) {

                            if (videoThumbnailBitmap != null) {
                                uploadBitmap = videoThumbnailBitmap;
                            } else {

                                try {
                                    uploadBitmap = retrieveVideoFrameFromUrl(editablePromotion.getVideoUrl());
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }

                            }

                            if (uploadBitmap != null) {


                                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                uploadBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                                final StorageReference videoImageRef = firebaseStorage.getReference()
                                        .child("images/" + UUID.randomUUID().toString());

                                videoImageRef.putBytes(baos.toByteArray()).addOnSuccessListener(taskSnapshot -> {
                                    videoImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                                    editablePromotion.setVideoThumbnail(uri.toString());
                                        videoThumbnail = uri.toString();
                                        updatePromo(progressDialog);
                                    }).addOnFailureListener(e -> Toast.makeText(PromotionActivity.this,
                                            "لقد فشل اضافة الإعلان الرجاء المحاولة مرة أخرى!",
                                            Toast.LENGTH_LONG).show());
                                });

                            } else {
                                updatePromo(progressDialog);
                            }

                        } else {

                            updatePromo(progressDialog);
                        }

                    } else {

                        Log.d("ttt", "not a video so updating");
                        updatePromo(progressDialog);
                    }


                } else {


                    if (promoType.equals(Promotion.VIDEO_TYPE)) {
                        if (videoThumbnail == null) {

                            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            if (videoThumbnailBitmap != null) {
                                videoThumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            } else {
                                Toast.makeText(PromotionActivity.this,
                                        "لقد فشل اضافة صورة من الفيديو! الرجاء اضافة صورة يدويا",
                                        Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                return;
                            }

                            final StorageReference videoImageRef = firebaseStorage.getReference()
                                    .child("images/" + UUID.randomUUID().toString());

                            videoImageRef.putBytes(baos.toByteArray()).addOnSuccessListener(taskSnapshot -> {
                                videoImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    videoThumbnail = uri.toString();
                                    Log.d("ttt", "videoThumbnail: " + videoThumbnail);
                                    startPromoUpload(title, description, negotiationCheck.isChecked(),
                                            phoneCheckBox.isChecked(),
                                            Double.parseDouble(priceEd.getText().toString().trim()), progressDialog);
                                }).addOnFailureListener(e -> Toast.makeText(PromotionActivity.this,
                                        "لقد فشل اضافة الإعلان الرجاء المحاولة مرة أخرى!",
                                        Toast.LENGTH_LONG).show());
                            });
                        } else {
                            startPromoUpload(title, description, negotiationCheck.isChecked(),
                                    phoneCheckBox.isChecked(),
                                    Double.parseDouble(priceEd.getText().toString().trim()), progressDialog);
                        }
                    } else {
                        startPromoUpload(title, description, negotiationCheck.isChecked(),
                                phoneCheckBox.isChecked(),
                                Double.parseDouble(priceEd.getText().toString().trim()), progressDialog);
                    }
                }


            }

//      if (!title.isEmpty() && !description.isEmpty() && category!=null && !category.isEmpty() && !priceEd.getText().toString().matches("")) {
//
//        if (!isEmailValid(title) && !isPhoneValid(title)) {
//
//          if ((promoType == 1) || (promoType == 3 && !imageUrisString.isEmpty()) || (promoType == 2 && videoDownloadUri != null)) {
//            final ProgressDialog progressDialog = new ProgressDialog(PromotionActivity.this);
//            progressDialog.setTitle("جاري اضافة الإعلان");
//            progressDialog.setCancelable(false);
//            progressDialog.setMessage("الرجاء الإنتظار!");
//            progressDialog.show();
//
//            if (promoType == 2) {
//              if (videoThumbnail == null) {
//
//                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                if (videoThumbnailBitmap != null) {
//                  videoThumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                } else {
//                  Toast.makeText(PromotionActivity.this, "لقد فشل اضافة صورة من الفيديو! الرجاء اضافة صورة يدويا", Toast.LENGTH_LONG).show();
//                  progressDialog.dismiss();
//                  return;
//                }
//
//                final StorageReference videoImageRef = firebaseStorage.getReference().child("images/" + UUID.randomUUID().toString());
//                videoImageRef.putBytes(baos.toByteArray()).addOnSuccessListener(taskSnapshot -> {
//                  videoImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                    videoThumbnail = uri.toString();
//                    Log.d("ttt", "videoThumbnail: " + videoThumbnail);
//                    startPromoUpload(title, description, negotiationCheck.isChecked(), Long.parseLong(priceEd.getText().toString()), progressDialog);
//                  }).addOnFailureListener(e -> Toast.makeText(PromotionActivity.this, "لقد فشل اضافة الإعلان الرجاء المحاولة مرة أخرى!", Toast.LENGTH_LONG).show());
//                });
//              } else {
//                startPromoUpload(title, description, negotiationCheck.isChecked(), Long.parseLong(priceEd.getText().toString()), progressDialog);
//              }
//            } else {
//              startPromoUpload(title, description, negotiationCheck.isChecked(), Long.parseLong(priceEd.getText().toString()), progressDialog);
//            }
//
//          } else {
//            Toast.makeText(PromotionActivity.this, "اعلانك يحتاج الى صورة او فيديو لكي تتم عملية النشر!", Toast.LENGTH_SHORT).show();
//          }
//        } else {
//          Toast.makeText(PromotionActivity.this, "عنوان الاعلان غير مناسب الرجاء تغيير العنوان!", Toast.LENGTH_SHORT).show();
//        }
//      } else {
//        Toast.makeText(PromotionActivity.this, "الرجاء تعبئة حقول الإعلان!", Toast.LENGTH_SHORT).show();
//      }


        } else if (v.getId() == videoPromoImage.getId()) {

            if (editablePromotion != null && editablePromotion.getVideoUrl() != null) {

                new VideoFullScreenFragment(editablePromotion.getVideoUrl()).show(getSupportFragmentManager(), "fullScreenVideo");

            }
//            else if(videoUri!=null){
//
//                new VideoFullScreenFragment(videoUri).show(getSupportFragmentManager(),"fullScreenVideo");
//
//            }

//            FullScreenVideoFragment.show();

        }

    }


    private void updatePromo(ProgressDialog progressDialog) {

        final DocumentReference promoDocumentRef = promoRef.document(String.valueOf(editablePromotion.getPromoid()));

        final List<String> updatableFieldPaths = new ArrayList<>();
        updatableFieldPaths.add("currency");
        updatableFieldPaths.add("description");
        updatableFieldPaths.add("hidePhone");
        updatableFieldPaths.add("keyWords");
        updatableFieldPaths.add("negotiable");
        updatableFieldPaths.add("price");
        updatableFieldPaths.add("promoType");
        updatableFieldPaths.add("title");
        updatableFieldPaths.add("type");


        if (imageUrisString != null) {

            if (editablePromotion.getPromoimages() != null) {
                for (String promoImage : editablePromotion.getPromoimages()) {
                    if (!imageUrisString.contains(promoImage)) {
                        firebaseStorage.getReferenceFromUrl(promoImage).delete();
                    }
                }
            }

            editablePromotion.setPromoimages(imageUrisString);
            updatableFieldPaths.add("promoimages");
        } else if (videoThumbnail != null) {

            if (editablePromotion.getVideoThumbnail() != null) {
                if (!videoThumbnail.equals(editablePromotion.getVideoThumbnail())) {
                    firebaseStorage.getReferenceFromUrl(editablePromotion.getVideoThumbnail()).delete();
                }
            }
            editablePromotion.setVideoThumbnail(videoThumbnail);
            updatableFieldPaths.add("videoThumbnail");
        }


        promoDocumentRef.set(editablePromotion, SetOptions.mergeFields(updatableFieldPaths))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.d("ttt", "updated promo");

                        promoDocumentRef.update(
                                "lastEditTime", System.currentTimeMillis(),
                                "wasEdited", true)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("ttt", "updated last edit time");

                                        Intent output = new Intent();
                                        output.putExtra("addedPromoId", editablePromotion.getPromoid());
                                        output.putExtra("editedPromo", editablePromotion);
                                        setResult(RESULT_OK, output);

                                        if (!editablePromotion.getIsPaused()) {


                                            promoDocumentRef.update("isPaused", false)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            progressDialog.dismiss();
                                                            Toast.makeText(PromotionActivity.this,
                                                                    "تمت تعديل الإعلان بنجاح!",
                                                                    Toast.LENGTH_SHORT).show();


                                                            finish();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Log.d("ttt", "updating ispaused failed: " + e.getMessage());

                                                }
                                            });


                                        } else {

                                            progressDialog.dismiss();
                                            Toast.makeText(PromotionActivity.this,
                                                    "تمت تعديل الإعلان بنجاح!",
                                                    Toast.LENGTH_SHORT).show();

                                            finish();

                                        }

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                addPromoButton.setClickable(true);
                Toast.makeText(PromotionActivity.this,
                        "لقد فشلت عملية تعديل الإعلان! الرجاء المحاولة مرة اخرى",
                        Toast.LENGTH_LONG).show();

                Log.d("ttt", "failed while trying to update promo: " + e.getMessage());

            }
        });

    }


//  private Uri setImageUri(){
//    File folder = new File("${getExternalFilesDir(Environment.DIRECTORY_DCIM)}");
//    folder.mkdirs();
//
//    File file = new File(folder, "Image_Tmp.jpg");
//    if (file.exists())
//      file.delete();
//    try {
//      file.createNewFile();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    Uri imageUri = FileProvider.getUriForFile(
//            this,
//            BuildConfig.APPLICATION_ID + "com.example.yousef.rbenoapplication",
//            file
//    );
//
//    return imageUri;
//  }

//
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private static void genVideoUsingMuxer(FileDescriptor dataSource,String originalFilePath, String dstPath,
//                                           int startMs, int endMs, boolean useAudio, boolean
//                                                   useVideo)
//            throws IOException {
//        // Set up MediaExtractor to read from the source.
//        MediaExtractor extractor = new MediaExtractor();
//        extractor.setDataSource(dataSource);
//        int trackCount = extractor.getTrackCount();
//        // Set up MediaMuxer for the destination.
//        MediaMuxer muxer;
//        muxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        // Set up the tracks and retrieve the max buffer size for selected
//        // tracks.
//        HashMap<Integer, Integer> indexMap = new HashMap<>(trackCount);
//        int bufferSize = -1;
//        for (int i = 0; i < trackCount; i++) {
//            MediaFormat format = extractor.getTrackFormat(i);
//            String mime = format.getString(MediaFormat.KEY_MIME);
//            boolean selectCurrentTrack = false;
//            if (mime.startsWith("audio/") && useAudio) {
//                selectCurrentTrack = true;
//            } else if (mime.startsWith("video/") && useVideo) {
//                selectCurrentTrack = true;
//            }
//            if (selectCurrentTrack) {
//                extractor.selectTrack(i);
//                int dstIndex = muxer.addTrack(format);
//                indexMap.put(i, dstIndex);
//                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
//                    int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
//                    bufferSize = newSize > bufferSize ? newSize : bufferSize;
//                }
//            }
//        }
//        if (bufferSize < 0) {
//            bufferSize = DEFAULT_BUFFER_SIZE;
//        }
//        // Set up the orientation and starting time for extractor.
//        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
//        retrieverSrc.setDataSource(dataSource);
//        String degreesString = retrieverSrc.extractMetadata(
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
//        if (degreesString != null) {
//            int degrees = Integer.parseInt(degreesString);
//            if (degrees >= 0) {
//                muxer.setOrientationHint(degrees);
//            }
//        }
//        if (startMs > 0) {
//            extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//        }
//        // Copy the samples from MediaExtractor to MediaMuxer. We will loop
//        // for copying each sample and stop when we get to the end of the source
//        // file or exceed the end time of the trimming.
//        int offset = 0;
//        int trackIndex = -1;
//        ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
//        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//        try {
//            muxer.start();
//            while (true) {
//                bufferInfo.offset = offset;
//                bufferInfo.size = extractor.readSampleData(dstBuf, offset);
//                if (bufferInfo.size < 0) {
//                    Log.d("ttt", "Saw input EOS.");
//                    bufferInfo.size = 0;
//                    break;
//                } else {
//                    bufferInfo.presentationTimeUs = extractor.getSampleTime();
//                    if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000)) {
//                        Log.d("ttt", "The current sample is over the trim end time.");
//                        break;
//                    } else {
//                        bufferInfo.flags = extractor.getSampleFlags();
//                        trackIndex = extractor.getSampleTrackIndex();
//                        muxer.writeSampleData(indexMap.get(trackIndex), dstBuf,
//                                bufferInfo);
//                        extractor.advance();
//                    }
//                }
//            }
//            muxer.stop();
//
//            //deleting the old file
//            File file = new File(originalFilePath);
//            file.delete();
//        } catch (IllegalStateException e) {
//            // Swallow the exception due to malformed source.
//            Log.d("ttt", "The source video file is malformed");
//        } finally {
//            muxer.release();
//        }
//        return;
//    }

}
