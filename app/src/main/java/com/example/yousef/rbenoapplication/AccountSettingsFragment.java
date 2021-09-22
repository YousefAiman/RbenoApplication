package com.example.yousef.rbenoapplication;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class AccountSettingsFragment extends Fragment {


    private static final int PICK_IMAGE = 1, USERNAME_EDITING_DELAY_DAY_COUNT = 30;
    private final CollectionReference userRef = FirebaseFirestore.getInstance().collection("users");
    private DocumentSnapshot userDocRef;
    private ImageView profile_image;
    private Uri newImageUri;
    private StorageReference ref;
    private ProgressDialog progressDialog;
    private EditText phonenumEd, usernameEd;
    private TextView nameNoteTv;
    private Spinner phoneSpinner;
    private PhoneNumberUtil phoneNumberUtil;

    private FirebaseUser user;
    private List<Task<?>> updateTasks;

    public AccountSettingsFragment() {
    }

    static AccountSettingsFragment newInstance() {
        return new AccountSettingsFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        phoneNumberUtil = PhoneNumberUtil.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);
        profile_image = view.findViewById(R.id.profile_image);
        usernameEd = view.findViewById(R.id.usernameEd);
        phonenumEd = view.findViewById(R.id.phonenumEd);
        phoneSpinner = view.findViewById(R.id.phoneSpinner);
        nameNoteTv = view.findViewById(R.id.nameNoteTv);
        ((Toolbar) view.findViewById(R.id.editAccountToolbar)).setNavigationOnClickListener(view1 ->
                getActivity().onBackPressed());


        final AdView adView = view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

//        if(System.currentTimeMillis() - user.getMetadata().getCreationTimestamp()
//        < DateUtils.DAY_IN_MILLIS)

//        String[] spinnerArray = new String[phoneCodesSet.size()];


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        userRef.document(user.getUid())
                .get().addOnSuccessListener(docRef -> {
            userDocRef = docRef;
            final String username = userDocRef.getString("username");
            final String imageurl = userDocRef.getString("imageurl");
            if (imageurl != null && !imageurl.isEmpty()) {
                profile_image.post(() -> Picasso.get().load(imageurl).fit().centerCrop().into(profile_image));
            }

            final String email = userDocRef.getString("email");
            final String phonenum = userDocRef.getString("phonenum");

            usernameEd.setText(username);
            ((TextView) view.findViewById(R.id.emailTv)).setText(email);
            if (phonenum != null) {
                phonenumEd.setHint(phonenum);
                Linkify.addLinks(phonenumEd, Linkify.ALL);
            } else {
                phonenumEd.setHint("لا يوجد رقم هاتف حالي");
            }

            if (docRef.contains("latestNameUpdateTime")) {

                final long latestNameUpdateTime = docRef.getLong("latestNameUpdateTime");

                if (latestNameUpdateTime == 0) {
                    nameNoteTv.setText("يمكنك تعديل اسمك الشخصي بعد 30 يوم من الان");

                } else {

                    if (System.currentTimeMillis() - latestNameUpdateTime >= DateUtils.DAY_IN_MILLIS * USERNAME_EDITING_DELAY_DAY_COUNT) {
                        nameNoteTv.setText("يمكن تغيير اسمك الشخصي مرة كل 30 يوم");
                        enableNameEd();
                    } else {

                        final int daysToWait = USERNAME_EDITING_DELAY_DAY_COUNT -
                                (int) ((System.currentTimeMillis() - latestNameUpdateTime) / DateUtils.DAY_IN_MILLIS);


                        nameNoteTv.setText("يمكنك تعديل اسمك الشخصي بعد " + daysToWait + " يوم من الان");
                    }

                }


            } else {

                enableNameEd();

                nameNoteTv.setText("يمكن تغيير اسمك الشخصي مرة كل 30 يوم");

            }
        });

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

    private void enableNameEd() {
        usernameEd.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        usernameEd.setFocusable(true);
        usernameEd.setFocusableInTouchMode(true);
        usernameEd.setClickable(true);
    }

    void showConfirmDialog() {

        progressDialog.dismiss();

        final Dialog dialog2 = new Dialog(getContext());
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog2.setContentView(R.layout.update_success_alert_layout);

        dialog2.findViewById(R.id.confirm_close).setOnClickListener(view -> {
            dialog2.cancel();
//            phonenumEd.clearFocus();
        });

        dialog2.show();
    }

    private void showAlertDialog() {

        final Dialog updateDialog = new Dialog(getContext());

        updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        updateDialog.setContentView(R.layout.update_info_alert_layout);

        final TextView closeButton = updateDialog.findViewById(R.id.update_close);
        final TextView confirmButton = updateDialog.findViewById(R.id.update_confirm);

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
                updateTasks = new ArrayList<>();

                Task<QuerySnapshot> usernameFindingTask = null;
                AtomicBoolean usernameAlreadyUsed = new AtomicBoolean();

                if (usernameEd.isFocusable() && usernameEd.isClickable()) {

                    final String newName = usernameEd.getText().toString();

                    if (newName != null && !newName.isEmpty()) {

                        if (!userDocRef.getString("username").equals(newName)) {

                            final String lowerCaseTrimmedUsername = newName.toLowerCase()
                                    .trim().replaceAll("\\s", "");

                            usernameFindingTask = userRef
                                    .whereEqualTo("usernameForSearch", lowerCaseTrimmedUsername)
                                    .limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot snapshots) {

                                            if (snapshots != null && !snapshots.isEmpty()) {

                                                Toast.makeText(requireContext(),
                                                        "اسم المستخدم الجديد مستخدم بالفعل من قبل شخص اخر!" +
                                                                " الرجاء استخدام اسم اخر", Toast.LENGTH_LONG).show();


                                                usernameAlreadyUsed.set(true);

                                            } else {

                                                updateTasks.add(userDocRef.getReference()
                                                        .update("username", newName,
                                                                "usernameForSearch", lowerCaseTrimmedUsername,
                                                                "latestNameUpdateTime", System.currentTimeMillis()));

                                            }

                                        }
                                    });


                        } else {

                            Toast.makeText(requireContext(),
                                    "يجب عليك اضافة اسم جديد لتحديثه", Toast.LENGTH_SHORT).show();

                            return;
                        }

                    }
                }

                if (usernameFindingTask != null && !usernameFindingTask.isComplete()) {

                    usernameFindingTask.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (!usernameAlreadyUsed.get()) {
                                checkAndUpdatePhoneNumber();
                                checkAndUpdateImage();
                                updateInfo(updateDialog, progressDialog);
                            } else {
                                updateDialog.dismiss();
                                progressDialog.dismiss();
                            }
                        }
                    });

                } else {

                    checkAndUpdatePhoneNumber();
                    checkAndUpdateImage();
                    updateInfo(updateDialog, progressDialog);
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

        closeButton.setOnClickListener(v -> updateDialog.dismiss());

        updateDialog.show();
    }


    private void checkAndUpdatePhoneNumber() {

        final String newPhoneNum = phonenumEd.getText().toString();

        if (!newPhoneNum.isEmpty()) {
            if (!checkPhoneNumber(newPhoneNum
                    , phoneSpinner.getSelectedItem().toString().split("\\+")[1])) {

                progressDialog.dismiss();

                Toast.makeText(getContext(), "رقم الهاتف غير صالح!" +
                        "الرجاء التأكد من الرقم", Toast.LENGTH_LONG).show();

                return;
            }

            updateTasks.add(
                    userDocRef.getReference().update("phonenum", newPhoneNum)
                            .addOnSuccessListener(aVoid -> {
                                if (newImageUri == null) {

                                    phonenumEd.setText("");
                                    phonenumEd.setHint(newPhoneNum);

//                                    progressDialog.cancel();
//                                    showConfirmDialog();
                                }
                            }));

        }


    }

    private void checkAndUpdateImage() {

        if (newImageUri != null) {

            ref = FirebaseStorage.getInstance().getReference().child("images/" +
                    UUID.randomUUID().toString());

            updateTasks.add(
                    ref.putFile(newImageUri).addOnSuccessListener(taskSnapshot -> {

                        updateTasks.add(
                                ref.getDownloadUrl().addOnSuccessListener(uri ->
                                        updateTasks.add(
                                                userDocRef.getReference().update("imageurl", uri.toString())
                                                        .addOnSuccessListener(aVoid -> {
                                                            ((HomeActivity) getActivity()).changeProfileImage(newImageUri);
                                                        }))));

                    }));

        }
    }

    private void updateInfo(Dialog dialog, ProgressDialog progressDialog) {

        if (updateTasks != null && !updateTasks.isEmpty()) {
            Tasks.whenAllComplete(updateTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                @Override
                public void onComplete(@NonNull Task<List<Task<?>>> task) {
                    showConfirmDialog();
                    progressDialog.dismiss();
                    dialog.dismiss();
                }
            }).addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
                @Override
                public void onSuccess(List<Task<?>> tasks) {

                }
            });
        }

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

        try {
            newNum.setCountryCode(Integer.parseInt(code)).setNationalNumber(Long.parseLong(number));
        } catch (NumberFormatException e) {
            return false;
        }

        return phoneNumberUtil.isValidNumber(newNum);
    }

}
