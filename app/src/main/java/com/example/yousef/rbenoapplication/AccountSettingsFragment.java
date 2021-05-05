package com.example.yousef.rbenoapplication;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AccountSettingsFragment extends Fragment {

  private static final int PICK_IMAGE = 1;
  private final CollectionReference userRef = FirebaseFirestore.getInstance().collection("users");
  private DocumentSnapshot userDocRef;
  private ImageView profile_image;
  private Uri newImageUri;
  private StorageReference ref;
  private ProgressDialog progressDialog;
  private EditText phonenumEd;
  private Spinner phoneSpinner;
  private PhoneNumberUtil phoneNumberUtil;

  public AccountSettingsFragment() {
  }

  static AccountSettingsFragment newInstance() {
    return new AccountSettingsFragment();
  }


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    phoneNumberUtil = PhoneNumberUtil.getInstance();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_account_settings, container, false);
    profile_image = view.findViewById(R.id.profile_image);
    phonenumEd = view.findViewById(R.id.phonenumEd);
    phoneSpinner = view.findViewById(R.id.phoneSpinner);
    ((Toolbar) view.findViewById(R.id.editAccountToolbar)).setNavigationOnClickListener(view1 ->
            getActivity().onBackPressed());


//        String[] spinnerArray = new String[phoneCodesSet.size()];


    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

//


//
//
//    spinnerArray.add("item1");
//    spinnerArray.add("item2");
//
//
//    ((TextView) findViewById(R.id.toolbarTitleTv)).setText("Rbeno "+
//            EmojiUtil.countryCodeToEmoji(GlobalVariables.getCountryCode()) );


    new Thread(() -> userRef.whereEqualTo("userId",
            FirebaseAuth.getInstance().getCurrentUser().getUid())
            .get().addOnSuccessListener(queryDocumentSnapshots -> {
              userDocRef = queryDocumentSnapshots.getDocuments().get(0);
              String username = userDocRef.getString("username");
              String imageurl = userDocRef.getString("imageurl");
              if (imageurl != null && !imageurl.isEmpty()) {
                profile_image.post(() -> Picasso.get().load(imageurl).into(profile_image));
              }

              String email = userDocRef.getString("email");
              String phonenum = userDocRef.getString("phonenum");
              getActivity().runOnUiThread(() -> {

                ((TextView) view.findViewById(R.id.usernameTv)).setText(username);
                ((TextView) view.findViewById(R.id.emailTv)).setText(email);
                if (phonenum != null) {
                  phonenumEd.setHint(phonenum);
                  Linkify.addLinks(phonenumEd, Linkify.ALL);
                } else {
                  phonenumEd.setHint("لا يوجد رقم هاتف حالي");
                }
              });
            })).start();

    createCountryCodeSpinner();

    view.findViewById(R.id.editBtn).setOnClickListener(v -> {

      showAlertDialog();
    });

    view.findViewById(R.id.editProfileImage).setOnClickListener(v -> {
      Intent i = new Intent(Intent.ACTION_GET_CONTENT);
      i.setType("image/*");
      startActivityForResult(Intent.createChooser(i, "Select Image"), PICK_IMAGE);
    });

  }

  void showConfirmDialog() {

    progressDialog.dismiss();

    final Dialog dialog2 = new Dialog(getContext());
    dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    dialog2.setContentView(R.layout.update_success_alert_layout);

    dialog2.findViewById(R.id.confirm_close).setOnClickListener(view -> {
      dialog2.cancel();
      phonenumEd.clearFocus();
    });

    dialog2.show();
  }

  private void showAlertDialog() {

    final Dialog dialog = new Dialog(getContext());

    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    dialog.setContentView(R.layout.update_info_alert_layout);

    final TextView closeButton = dialog.findViewById(R.id.update_close);
    final TextView confirmButton = dialog.findViewById(R.id.update_confirm);

    confirmButton.setEnabled(true);
    closeButton.setEnabled(true);


    confirmButton.setOnClickListener(v -> {
      if (WifiUtil.checkWifiConnection(getContext())) {

        if (progressDialog == null) {
          progressDialog = new ProgressDialog(getContext());
          progressDialog.setTitle("جاري تعديل بيانات الحساب");
          progressDialog.setMessage("الرجاء الإنتظار!");
        }

        progressDialog.show();

        final String newPhoneNum = phonenumEd.getText().toString();

        if (!newPhoneNum.isEmpty()) {
          if (!checkPhoneNumber(newPhoneNum
                  , phoneSpinner.getSelectedItem().toString().split("\\+")[1])) {

            progressDialog.dismiss();

            Toast.makeText(getContext(), "رقم الهاتف غير صالح!" +
                    "الرجاء التأكد من الرقم", Toast.LENGTH_LONG).show();

            return;
          }

          userDocRef.getReference().update("phonenum", newPhoneNum)
                  .addOnSuccessListener(aVoid -> {
                    if (newImageUri == null) {

                      phonenumEd.setText("");
                      phonenumEd.setHint(newPhoneNum);

                      dialog.cancel();
                      showConfirmDialog();
                    }
                  });

        }

        if (newImageUri != null) {

          ref = FirebaseStorage.getInstance().getReference().child("images/" +
                  UUID.randomUUID().toString());

          ref.putFile(newImageUri).addOnSuccessListener(taskSnapshot -> {

            ref.getDownloadUrl().addOnSuccessListener(uri ->
                    userDocRef.getReference().update("imageurl", uri.toString())
                            .addOnSuccessListener(aVoid -> {
                              dialog.cancel();
                              showConfirmDialog();
                              ((HomeActivity) getActivity()).changeProfileImage(newImageUri);
                            }));

          });

        }
//        if (newImageUri != null && newPhoneNum.equals("")) {
//          progressDialog = ProgressDialog.show(getContext(), "جاري تعديل بيانات الحساب",
//                  "الرجاء الإنتظار!", true);
//          ref = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());
//          ref.putFile(newImageUri).addOnSuccessListener(taskSnapshot -> {
//            loadingDialog = ProgressDialog.show(getContext(), "جاري تحديث البيانات",
//                    "الرجاء الإنتظار!", true);
//            //      Toast.makeText(AccountSettingActivity.this, "Image is being updated", Toast.LENGTH_SHORT).show();
//          }).addOnCompleteListener(task -> {
//            loadingDialog.dismiss();
//            ref.getDownloadUrl().addOnSuccessListener(uri -> downloadUri = uri.toString()).addOnCompleteListener(task12 -> {
//              ((HomeActivity) getActivity()).changeProfileImage(downloadUri);
//              userRef.document(userDocRef.getId()).update("imageurl", downloadUri).addOnCompleteListener(task1 -> {
//                dialog.cancel();
//                progressDialog.dismiss();
//                dialog2.show();
//
//                confirmCloseButton.setOnClickListener(v12 -> {
//                  dialog2.cancel();
//                  phonenumEd.clearFocus();
//
//                });
//              });
//            }).addOnFailureListener(e -> Log.d("ttt", e.toString()));
//          }).addOnFailureListener(e -> {
//            loadingDialog.dismiss();
//            Log.d("ttt", e.toString());
//          });
//
//
//        } else if (newImageUri != null) {
//          progressDialog = ProgressDialog.show(getContext(), "جاري تعديل بيانات الحساب",
//                  "الرجاء الإنتظار!", true);
//          userRef.document(userDocRef.getId()).update("phonenum", newPhoneNum).addOnCompleteListener(task -> {
//
//            dialog.cancel();
//            phonenumEd.setHint(newPhoneNum);
//            if (newImageUri == null) {
//              progressDialog.dismiss();
//              dialog2.show();
//              confirmCloseButton.setOnClickListener(v1 -> {
//                dialog2.cancel();
//                phonenumEd.clearFocus();
//              });
//            }
//          });
//        }
      }
    });
    closeButton.setOnClickListener(v -> dialog.dismiss());
    dialog.show();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == -1 && requestCode == PICK_IMAGE && data != null) {
      newImageUri = data.getData();
      Picasso.get().load(newImageUri).fit().centerCrop().into(profile_image);
    }
  }

  void createCountryCodeSpinner() {
    new Thread(() -> {

      List<String> supportedCountryCodes =
              new ArrayList<>(phoneNumberUtil.getSupportedRegions());

      String defaultCode;

      if (GlobalVariables.getInstance().getCountryCode() != null &&
              !GlobalVariables.getInstance().getCountryCode().isEmpty()) {
        defaultCode = GlobalVariables.getInstance().getCountryCode().toUpperCase();
      } else {
        defaultCode = Locale.getDefault().getCountry();
      }

      defaultCode = defaultCode.toUpperCase();

      final String defaultSpinnerChoice = EmojiUtil.countryCodeToEmoji(defaultCode)
              + " +" + phoneNumberUtil.getCountryCodeForRegion(defaultCode);


      final List<String> spinnerArray = new ArrayList<>(supportedCountryCodes.size());

      for (String code : supportedCountryCodes) {

        spinnerArray.add(EmojiUtil.countryCodeToEmoji(code)
                + " +" + phoneNumberUtil.getCountryCodeForRegion(code));
      }

      supportedCountryCodes = null;

      Collections.sort(spinnerArray, new Comparator<String>() {
        @Override
        public int compare(String s, String t1) {
          return extractCode(s) - extractCode(t1);
        }

        int extractCode(String s) {
          return Integer.parseInt(s.split("\\+")[1]);
        }
      });


      Log.d("ttt", "list size: " + spinnerArray.size());
      if (getActivity() != null) {

        final ArrayAdapter<String> ad
                = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_item_layout,
                spinnerArray);

        ad.setDropDownViewResource(R.layout.spinner_item_layout);

        phoneSpinner.post(() -> {
          phoneSpinner.setAdapter(ad);

          phoneSpinner.setSelection(spinnerArray.indexOf(defaultSpinnerChoice));
        });
      }
    }).start();
  }

  boolean checkPhoneNumber(String number, String code) {

    final PhoneNumber newNum = new PhoneNumber();

    newNum.setCountryCode(Integer.parseInt(code)).setNationalNumber(Long.parseLong(number));

    return phoneNumberUtil.isValidNumber(newNum);
  }

}
