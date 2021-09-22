package com.example.yousef.rbenoapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;

public class SliderActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager viewPager;
    private Button signinBtn, nextSlideBtn, registerBtn;
    private ImageView[] sliderDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);

        getSharedPreferences("rbeno", Context.MODE_PRIVATE)
                .edit().putBoolean("notFirstTime", true).apply();


        final Integer[] images = {R.drawable.slider1, R.drawable.slider2, R.drawable.slider3, R.drawable.slider4};
        final String[]
                titles = {"بيع واشتري في مكان واحد", "كل الاقسام اللي تحلم بيها", "إعلانك فيديو - صور - و نص"},
                descs = {"عروض إعلانات مميزة في مناطق مختلفة حولك", "يتوفر في التطبيق جميع الأقسام التي يريدها المستخدم ", "إعلانات مع تفاصيل كاملة ووصول أسهل"};

        viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new ViewPagerAdapter(this, images, titles, descs));

        signinBtn = findViewById(R.id.signinBtn);
        registerBtn = findViewById(R.id.registerBtn);
        nextSlideBtn = findViewById(R.id.nextSlideBtn);

        findViewById(R.id.nextTv).setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        signinBtn.setOnClickListener(this);
        nextSlideBtn.setOnClickListener(this);


        sliderDots = new ImageView[]{findViewById(R.id.sliderDotIv1),
                findViewById(R.id.sliderDotIv2),
                findViewById(R.id.sliderDotIv3),
                findViewById(R.id.sliderDotIv4)
        };

        final Drawable nonactive_dot = ContextCompat.getDrawable(getApplicationContext(), R.drawable.nonactive_dot);
        final Drawable full_dot = ContextCompat.getDrawable(getApplicationContext(), R.drawable.full_dot);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                for (int i = 0; i < sliderDots.length; i++) {
                    if (i == position) continue;
                    sliderDots[i].setImageDrawable(nonactive_dot);
                }
                sliderDots[position].setImageDrawable(full_dot);
                if (position == sliderDots.length - 1) {
                    signinBtn.setVisibility(View.VISIBLE);
                    signinBtn.setClickable(true);
                    nextSlideBtn.setVisibility(View.INVISIBLE);
                    nextSlideBtn.setClickable(false);
                    registerBtn.setVisibility(View.VISIBLE);
                    registerBtn.setClickable(true);
                } else {
                    if (registerBtn.getVisibility() == View.VISIBLE) {
                        registerBtn.setVisibility(View.INVISIBLE);
                        registerBtn.setClickable(false);
                        signinBtn.setVisibility(View.INVISIBLE);
                        signinBtn.setClickable(false);
                        nextSlideBtn.setVisibility(View.VISIBLE);
                        nextSlideBtn.setClickable(true);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.signinBtn) {

            startActivity(new Intent(this, SigninActivity.class));
            finish();

        } else if (view.getId() == R.id.registerBtn) {

            startActivity(new Intent(this, RegisterActivity.class));
            finish();

        } else if (view.getId() == R.id.nextSlideBtn) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        } else if (view.getId() == R.id.nextTv) {

            Log.d("ttt", "viewPager.getCurrentItem(): " + viewPager.getCurrentItem());

            if (viewPager.getCurrentItem() == sliderDots.length - 1) {
                //guest

                if (WifiUtil.checkWifiConnection(this)) {

                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("جاري الدخول");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(authResult -> {
                        progressDialog.dismiss();
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(SliderActivity.this,
                                    "فشلت عملية الدخول حاول مرة اخرى", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }


            } else {
                viewPager.setCurrentItem(sliderDots.length - 1);
            }
        }
    }

}
