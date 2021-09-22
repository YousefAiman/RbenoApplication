package com.example.yousef.rbenoapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class InfoFragment extends Fragment {

    public static final int ABOUT_US = 1, PRIVACY_POLICY = 2, CONTACT_US = 3;
    private int type;

    public InfoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        type = getArguments().getInt("type");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =
                inflater.inflate(R.layout.fragment_info, container, false);


        final AdView adView = view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });


        ((Toolbar) view.findViewById(R.id.toolbar)).setNavigationOnClickListener(view1 ->
                getActivity().onBackPressed());

        final TextView titleTv = view.findViewById(R.id.titleTv);
        final TextView contentTv = view.findViewById(R.id.contentTv);

        String toolbarText;
        int contentText;

        switch (type) {
            case ABOUT_US:
                toolbarText = "عن التطبيق";
                contentText = R.string.about_us;
                break;

            case PRIVACY_POLICY:
                toolbarText = "سياسة الخصوصية";
                contentText = R.string.privacy_policy;
                break;

            case CONTACT_US:
                toolbarText = "الاتصال بنا";
                contentText = R.string.contact_us;
                break;
            default:
                throw new IllegalStateException("Unexpected type value: " + type);
        }

        titleTv.setText(toolbarText);
        contentTv.setText(contentText);


        return view;
    }
}