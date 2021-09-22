package com.example.yousef.rbenoapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class InterstitialAdUtil {

    private static InterstitialAd mInterstitialAd = null;
    private static boolean isAdShowing;

    private InterstitialAdUtil() {
    }

    public static InterstitialAd getInstance(Context context) {

        Log.d("videoPager", "getInstance called");
        if (mInterstitialAd == null) {
            Log.d("videoPager", "mInterstitialAd null so creating");

            loadAd(context);

            if (mInterstitialAd != null) {

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d("videoPager", "mInterstitialAd ad closed");
                        setIsShowing(false);

                        loadAd(context);

                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        mInterstitialAd = null;
                        setIsShowing(true);
                    }
                });
            }


        }

        return (mInterstitialAd);
    }

    private static void loadAd(Context context) {

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context,
                "ca-app-pub-6990486336142688/9237240622",
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;

                        super.onAdLoaded(interstitialAd);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        mInterstitialAd = null;
                    }
                });


    }

    private static boolean isIsAdShowing() {
        return InterstitialAdUtil.isAdShowing;
    }

    private static void setIsShowing(boolean isShowing) {
        InterstitialAdUtil.isAdShowing = isShowing;
    }
//
//  static class CloseListener extends AdListener{
//    @Override
//    public void onAdClosed() {
//      super.onAdClosed();
//      Log.d("videoPager","mInterstitialAd ad closed");
//      setIsShowing(false);
//
//      mInterstitialAd.loadAd(new AdRequest.Builder().build());
//    }
//    @Override
//    public void onAdOpened() {
//      super.onAdOpened();
//      Log.d("videoPager","ad opened from close listener");
//      setIsShowing(true);
//
//    }
//  }

    static void showAd(Context context) {
        Log.d("videoPager", "showAd called");
        if (mInterstitialAd == null) {
            Log.d("videoPager", "creating ad before showing");
            getInstance(context);
        }

        if (mInterstitialAd != null) {
            new Handler().post(() -> mInterstitialAd.show((Activity) context));
        }

//    if (mInterstitialAd.isLoaded()) {
//      Log.d("videoPager","mInterstitialAd.isLoaded so showing");
//      new Handler().post(() -> mInterstitialAd.show());
//    }else{
//      Log.d("videoPager","mInterstitialAd is not loaded so adding listener");
//      mInterstitialAd.setAdListener(new AdListener() {
//        @Override
//        public void onAdLoaded() {
//          setIsShowing(true);
//          mInterstitialAd.show();
//
//          Log.d("videoPager","ad loaded so showing it");
//          mInterstitialAd.setAdListener(new CloseListener());
//        }
//
//        @Override
//        public void onAdOpened() {
//          super.onAdOpened();
//          Log.d("videoPager","ad opened from load listener");
//          setIsShowing(true);
//
//        }
//
//        @Override
//        public void onAdClosed() {
//          super.onAdClosed();
//          Log.d("videoPager","ad closed");
//          setIsShowing(false);
//
//        }
//      });
//    }
    }

}
