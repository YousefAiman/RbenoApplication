package com.example.yousef.rbenoapplication;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.util.UUID;

public class AccountSettingsFragment extends DialogFragment {

    private static final int PICK_IMAGE = 1;
    private DocumentSnapshot userDocRef;
    private CollectionReference userRef = FirebaseFirestore.getInstance().collection("users");
    private String newPhoneNum;
    private ImageView profile_image;
    private Uri newImageUri;
    private StorageReference ref;
    private String downloadUri;
    private Dialog loadingDialog;
    private ProgressDialog progressDialog;
    private EditText phonenumEd;

    public AccountSettingsFragment() {
    }

    static AccountSettingsFragment newInstance() {
        return new AccountSettingsFragment();
    }

    private static Bitmap decodeUri(Context c, Uri uri)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        while (width_tmp / 2 >= 250 && height_tmp / 2 >= 250) {
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);
        profile_image = view.findViewById(R.id.profile_image);
        phonenumEd = view.findViewById(R.id.phonenumEd);
        ((TextView) view.findViewById(R.id.toolbarTitleTv)).setText("تعديل الملف");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        new Thread(() -> userRef.whereEqualTo("userId", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            userDocRef = queryDocumentSnapshots.getDocuments().get(0);
            String username = userDocRef.getString("username");
            String imageurl = userDocRef.getString("imageurl");
            if(imageurl!=null && !imageurl.isEmpty()){
                profile_image.post(()->Picasso.get().load(imageurl).into(profile_image));
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
        view.findViewById(R.id.editBtn).setOnClickListener(v -> {
            newPhoneNum = phonenumEd.getText().toString();
            showAlertDialog();
        });

        view.findViewById(R.id.editProfileImage).setOnClickListener(v -> {
            startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/"), PICK_IMAGE);
        });

    }

    private void showAlertDialog() {

        final Dialog dialog = new Dialog(getContext());

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final Dialog dialog2 = new Dialog(getContext());
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog2.setContentView(R.layout.update_success_alert_layout);
        final TextView confirmCloseButton = dialog2.findViewById(R.id.confirm_close);


        dialog.setContentView(R.layout.update_info_alert_layout);

        TextView closeButton = dialog.findViewById(R.id.update_close);
        TextView confirmButton = dialog.findViewById(R.id.update_confirm);

        confirmButton.setEnabled(true);
        closeButton.setEnabled(true);
        confirmButton.setOnClickListener(v -> {

            if (!newPhoneNum.equals("")) {
                progressDialog = ProgressDialog.show(getContext(), "جاري تعديل بيانات الحساب",
                        "الرجاء الإنتظار!", true);

                userRef.document(userDocRef.getId()).update("phonenum", newPhoneNum).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    dialog.cancel();
                    phonenumEd.setHint(newPhoneNum);
                    if (newImageUri == null) {
                        dialog2.show();
                        confirmCloseButton.setOnClickListener(v14 -> {
                            dialog2.cancel();
                            phonenumEd.clearFocus();
                        });
                    } else {

                        progressDialog = ProgressDialog.show(getContext(), "جاري تعديل بيانات الحساب",
                                "الرجاء الإنتظار!", true);

                        ref = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());
                        ref.putFile(newImageUri).addOnSuccessListener(taskSnapshot -> loadingDialog = ProgressDialog.show(getContext(), "جاري تحديث البيانات",
                                "الرجاء الإنتظار!", true)).addOnCompleteListener(task15 -> {
                            loadingDialog.dismiss();
                            ref.getDownloadUrl().addOnSuccessListener(uri -> downloadUri = uri.toString()).addOnCompleteListener(task14 -> userRef.document(userDocRef.getId()).update("imageurl", downloadUri).addOnCompleteListener(task13 -> {
                                dialog.cancel();
                                progressDialog.dismiss();
                                dialog2.show();
                                ((HomeActivity) getActivity()).changeProfileImage(downloadUri);
                                confirmCloseButton.setOnClickListener(v13 -> {
                                    dialog2.cancel();
                                    phonenumEd.clearFocus();
                                });
                            })).addOnFailureListener(e ->
                                    Log.d("ttt", e.toString()));
                        }).addOnFailureListener(e -> {
                            loadingDialog.dismiss();
                            Log.d("ttt", e.toString());
                        });


                    }
                });

            }
            if (newImageUri != null && newPhoneNum.equals("")) {
                progressDialog = ProgressDialog.show(getContext(), "جاري تعديل بيانات الحساب",
                        "الرجاء الإنتظار!", true);
                ref = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());
                ref.putFile(newImageUri).addOnSuccessListener(taskSnapshot -> {
                    loadingDialog = ProgressDialog.show(getContext(), "جاري تحديث البيانات",
                            "الرجاء الإنتظار!", true);
                    //      Toast.makeText(AccountSettingActivity.this, "Image is being updated", Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    ref.getDownloadUrl().addOnSuccessListener(uri -> downloadUri = uri.toString()).addOnCompleteListener(task12 -> {
                        ((HomeActivity) getActivity()).changeProfileImage(downloadUri);
                        userRef.document(userDocRef.getId()).update("imageurl", downloadUri).addOnCompleteListener(task1 -> {
                            dialog.cancel();
                            progressDialog.dismiss();
                            dialog2.show();

                            confirmCloseButton.setOnClickListener(v12 -> {
                                dialog2.cancel();
                                phonenumEd.clearFocus();

                            });
                        });
                    }).addOnFailureListener(e -> Log.d("ttt", e.toString()));
                }).addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Log.d("ttt", e.toString());
                });


            } else if (newImageUri != null) {
                progressDialog = ProgressDialog.show(getContext(), "جاري تعديل بيانات الحساب",
                        "الرجاء الإنتظار!", true);
                userRef.document(userDocRef.getId()).update("phonenum", newPhoneNum).addOnCompleteListener(task -> {

                    dialog.cancel();
                    phonenumEd.setHint(newPhoneNum);
                    if (newImageUri == null) {
                        progressDialog.dismiss();
                        dialog2.show();
                        confirmCloseButton.setOnClickListener(v1 -> {
                            dialog2.cancel();
                            phonenumEd.clearFocus();
                        });
                    }
                });
            }
        });
        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            newImageUri = data.getData();
            try {
                profile_image.setImageBitmap(decodeUri(getContext(), newImageUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
