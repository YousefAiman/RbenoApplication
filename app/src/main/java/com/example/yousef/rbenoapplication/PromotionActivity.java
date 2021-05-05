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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PromotionActivity extends AppCompatActivity {

  private static final int
          videoLengthMaxLimit = 30,
          videoLengthMinLimit = 10,
          videoMaxSizeInMB = 15,
          PICK_IMAGE = 1,
          PICK_VIDEO = 2, PICK_CAMERA = 3,
          CAMERA_PERMISSION = 4;

  private final FirebaseAuth auth = FirebaseAuth.getInstance();
  private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
  private final CollectionReference promoRef = FirebaseFirestore.getInstance()
          .collection("promotions");
  private int imageNumber = 0, promoType;
  private ImageView videoPromoImage, videoThumbnailPromoImage, videoThumbnailCloseImage;
  private ProgressBar videoProgressBar, videoThumbnailProgressBar;
  //  private ImageView first_image,second_image,third_image,fourth_image;
  private ProgressBar[] progressBars;
  private ImageView[] promoImages, categoryImageViews, closePromoImages;
  private TextView[] categoryTextViews;
  private ArrayList<String> imageUrisString;
  private Button addPromoButton;
  private String category, videoThumbnail, videoDownloadUri, cameraImageFilePath;
  private Uri videoUri;
  private Bitmap videoThumbnailBitmap;
  private CardView videoCardView;
  private Map<UploadTask, OnSuccessListener<UploadTask.TaskSnapshot>> uploadTasks;
  private List<StorageReference> storageReferences;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_promotion);

    final Intent intent = getIntent();
    final Toolbar toolbar = findViewById(R.id.addPromosToolbar);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());
    if (intent.hasExtra("textPromo")) {
      promoType = 1;
      ((TextView) findViewById(R.id.titleTv)).setText("إضافة اعلان نصي");
      findViewById(R.id.promoTypeTv).setVisibility(View.GONE);
      findViewById(R.id.videoLinearLayout).setVisibility(View.GONE);
      findViewById(R.id.videoSizeInfoTv).setVisibility(View.GONE);
      findViewById(R.id.horizontal_scroll).setVisibility(View.GONE);
    } else if (intent.hasExtra("videoPromo")) {
      promoType = 2;
      ((TextView) findViewById(R.id.titleTv)).setText("إضافة اعلان فيديو");
      ((TextView) findViewById(R.id.promoTypeTv)).setText("قم بإضافة فيديو وصور لاعلانك");
      findViewById(R.id.horizontal_scroll).setVisibility(View.GONE);
      requestPermissions();

      videoPromoImage = findViewById(R.id.videoPromoImage);
      videoProgressBar = findViewById(R.id.videoProgressBar);
      videoCardView = findViewById(R.id.videoCardView);
      videoCardView.setOnClickListener(this::chooseVideo);
      videoThumbnailPromoImage = findViewById(R.id.videoThumbnailPromoImage);
      videoThumbnailCloseImage = findViewById(R.id.videoThumbnailCloseImage);
      videoThumbnailProgressBar = findViewById(R.id.videoThumbnailProgressBar);

      videoThumbnailPromoImage.setOnClickListener(v -> {
        imageNumber = 5;
        showPromoOptions();
      });
    } else {
      promoType = 3;
      ((TextView) findViewById(R.id.titleTv)).setText("إضافة اعلان صورة");
      ((TextView) findViewById(R.id.promoTypeTv)).setText("قم بإضافة صور لاعلانك");
      findViewById(R.id.videoLinearLayout).setVisibility(View.GONE);
      findViewById(R.id.videoSizeInfoTv).setVisibility(View.GONE);
      requestPermissions();

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

    categoryClickListeners();
    final EditText titleEd = findViewById(R.id.titleEd);
    final TextView titleLengthTv = findViewById(R.id.titleLengthTv);
    titleEd.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        titleLengthTv.setText(charSequence.length() + "");
      }

      @Override
      public void afterTextChanged(Editable editable) {
      }
    });

    final EditText priceEd = findViewById(R.id.priceEd);
    final EditText descriptionEd = findViewById(R.id.descEd);
    final TextView descLengthTv = findViewById(R.id.descLengthTv);
    descriptionEd.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        descLengthTv.setText(charSequence.length() + "");
      }

      @Override
      public void afterTextChanged(Editable editable) {
      }
    });

    addPromoButton = findViewById(R.id.addPromoBtn);
    final CheckBox negotiationCheck = findViewById(R.id.negotiationCheck);
    final CheckBox phoneCheckBox = findViewById(R.id.phoneCheckBox);


    addPromoButton.setOnClickListener(v -> {

      String title = titleEd.getText().toString().trim();
      String description = descriptionEd.getText().toString().trim();

      if (promoType == 3 && imageUrisString.isEmpty()) {
        Toast.makeText(this, "الرجاء وضع صور لاعلانك!", Toast.LENGTH_SHORT).show();
        return;
      } else if (promoType == 2 && videoDownloadUri == null) {
        Toast.makeText(this, "الرجاء وضع فيديو لاعلانك!", Toast.LENGTH_SHORT).show();
        return;
      }

      if (category == null || category.isEmpty()) {
        Toast.makeText(this, "الرجاء اختيار القسم لاعلانك!", Toast.LENGTH_SHORT).show();
        return;
      }

      if (title.isEmpty()) {
        Toast.makeText(this, "الرجاء وضع عنوان لاعلانك!", Toast.LENGTH_SHORT).show();
        return;
      }

      title = title.trim();
      title = title.replaceAll("( )+", " ");

      final String[] titleSplit = title.split(" ");

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

      description = description.trim();
      description = description.replaceAll("( )+", " ");

      final String[] descSplit = description.split(" ");

      for (String descSplitting : descSplit) {
        if (isUrlValid(descSplitting)) {
          Toast.makeText(this, "الرجاء عدم وضع رابط في عنوان اعلانك!",
                  Toast.LENGTH_SHORT).show();
          return;
        }

      }

      if (WifiUtil.checkWifiConnection(this)) {

        final ProgressDialog progressDialog = new ProgressDialog(PromotionActivity.this);
        progressDialog.setTitle("جاري اضافة الإعلان");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("الرجاء الإنتظار!");
        progressDialog.show();

        if (promoType == 2) {
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

            String finalTitle = title;
            String finalDescription = description;
            videoImageRef.putBytes(baos.toByteArray()).addOnSuccessListener(taskSnapshot -> {
              videoImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                videoThumbnail = uri.toString();
                Log.d("ttt", "videoThumbnail: " + videoThumbnail);
                startPromoUpload(finalTitle, finalDescription, negotiationCheck.isChecked(),
                        phoneCheckBox.isChecked(),
                        Long.parseLong(priceEd.getText().toString()), progressDialog);
              }).addOnFailureListener(e -> Toast.makeText(PromotionActivity.this,
                      "لقد فشل اضافة الإعلان الرجاء المحاولة مرة أخرى!",
                      Toast.LENGTH_LONG).show());
            });
          } else {
            startPromoUpload(title, description, negotiationCheck.isChecked(),
                    phoneCheckBox.isChecked(),
                    Long.parseLong(priceEd.getText().toString()), progressDialog);
          }
        } else {
          startPromoUpload(title, description, negotiationCheck.isChecked(),
                  phoneCheckBox.isChecked(),
                  Long.parseLong(priceEd.getText().toString()), progressDialog);
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
    });

  }


  void getImage() {

    if (WifiUtil.checkWifiConnection(this)) {
      startActivityForResult(Intent.createChooser(
              new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
              "Select Image"), PICK_IMAGE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == CAMERA_PERMISSION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        getCamera();
      } else {
        Toast.makeText(this, "هذا التطبيق يحتاج الى الوصول الكاميرا!" +
                "", Toast.LENGTH_LONG).show();
      }
    }
  }

  void getCamera() {

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


  public void requestPermissions() {
    final String[] permissions = {
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    if (
//            ContextCompat.checkSelfPermission(getApplicationContext(), permissions[1]) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(getApplicationContext(), permissions[0]) != PackageManager.PERMISSION_GRANTED) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestPermissions(permissions, 1);
      }
    }
  }

  void categoryClickListeners() {

    final int textGreyColor = getResources().getColor(R.color.textGreyColor);
    final int textWhiteColor = getResources().getColor(R.color.white);
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

          if (textView.getText().toString().equals("هواتف")) {
            category = "هواتف";
            return;
          }
          if (textView.getText().toString().equals("الكترونيات")) {
            category = "اليكترونيات";
            return;
          }
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


      if ((timeInMillisec / 1000) > videoLengthMaxLimit) {

        retriever.release();
        videoCardView.setClickable(true);
        Toast.makeText(this, "طول الفيديو يجب ان لا يزيد عن 30 ثانية!",
                Toast.LENGTH_SHORT).show();

      } else if ((timeInMillisec / 1000) < videoLengthMinLimit) {

        videoCardView.setClickable(true);
        Toast.makeText(this, "طول الفيديو يجب ان لا يقل عن 10 ثواني!",
                Toast.LENGTH_SHORT).show();

      } else {

        if (getSizeInMB(videoUri) <= videoMaxSizeInMB) {
          ((TextView) findViewById(R.id.videoSizeTv)).setText("ثانية " + timeInMillisec / 1000);
          videoThumbnailBitmap = retriever.getFrameAtTime(1000);

          videoPromoImage.setImageBitmap(videoThumbnailBitmap);
          videoPromoImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
          videoPromoImage.setClickable(false);
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
      data.put("currency", sharedPreferences.getString("currency", null));
      data.put("keyWords", titleSpilitting);
      data.put("isBanned", false);
      data.put("isPaused", false);
      if (promoType == 2) {
        data.put("videoUrl", videoDownloadUri);
        if (!videoThumbnail.isEmpty()) {
          data.put("videoThumbnail", videoThumbnail);
        }
        data.put("promoType", "video");
      } else if (promoType == 3) {
        data.put("promoimages", imageUrisString);
        data.put("promoType", "image");
      } else {
        data.put("promoType", "text");
      }

      promoRef.add(data).addOnSuccessListener(documentReference ->
                      promoidRef.update("count", FieldValue.increment(1),
                              "promoCount", FieldValue.increment(1))
                              .addOnSuccessListener(aVoid -> {

                                        //              promoidRef.update("promoCount", FieldValue.increment(1)).addOnSuccessListener(aVoid1 ->
//                                        promoRef.document(documentReference.getId()).
//                                                collection("ratings").add(new PromoRating()).addOnSuccessListener(documentReference1 -> {

//                                          CollectionReference usersRef = firestore.collection("users");
//                                          usersRef.whereEqualTo("userId", auth.getCurrentUser().getUid())
//                                                  .get().addOnSuccessListener(snapshots -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(PromotionActivity.this,
                                                "تمت اضافة الإعلان بنجاح!",
                                                Toast.LENGTH_SHORT).show();

                                        Intent output = new Intent();
                                        output.putExtra("addedPromoId", id);
                                        setResult(3, output);
                                        finish();
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
    alert.setMessage("الخروج قبل الانتهاء من اضافة الاعلان سيؤدي الى فقدانه");

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


      super.onBackPressed();
    });
    alert.setNegativeButton("لا", (dialog, which) -> {
      dialog.cancel();
    });
    alert.create().show();

  }


  void checkAndDeleteVideoThumbNailFromStorage() {
    if (videoThumbnail != null && !videoThumbnail.isEmpty()) {
      firebaseStorage.getReferenceFromUrl(videoThumbnail).delete()
              .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                  videoThumbnail = null;
                  Log.d("ttt", "video thumbail deleted");
                }
              }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          Log.d("ttt", "video thumbail deleted error: " + e.getMessage());
        }
      }).addOnCanceledListener(new OnCanceledListener() {
        @Override
        public void onCanceled() {
          Log.d("ttt", "video thumbail delete cancelled");
        }
      });
    }
  }

  void chooseVideo(View view) {
    videoCardView.setClickable(false);
    startActivityForResult(Intent.createChooser(
            new Intent(Intent.ACTION_GET_CONTENT)
                    .setType("video/*")
                    .addCategory(Intent.CATEGORY_OPENABLE), "اختر فيديو لاعلانك"), PICK_VIDEO);
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
            firebaseStorage.getReferenceFromUrl(imageUrisString.get(index)).delete()
                    .addOnCompleteListener(task1 -> {
                      if (task1.isSuccessful()) {

                        uploadTasks.remove(uploadTask);

                        imageUrisString.remove(index);
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

}
