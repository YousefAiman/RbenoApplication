package com.example.yousef.rbenoapplication;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.Picasso;


public class MessagesFragment extends Fragment {
    ViewPager messagesViewPager;
    ImageView userImageIndicator;
    TextView receivedTv;
    TextView sentTv;
    ImageView arrowIv;

    public MessagesFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_messages, container, false);
        userImageIndicator = view.findViewById(R.id.userImageIndicator);
        messagesViewPager = view.findViewById(R.id.messagesViewPager);
        receivedTv = view.findViewById(R.id.receivedTv);
        sentTv = view.findViewById(R.id.sentTv);
        arrowIv = view.findViewById(R.id.arrowIv);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userImageIndicator.setOnClickListener(v -> ((HomeActivity) getActivity()).showDrawer());

        if (GlobalVariables.getProfileImageUrl() != null && !GlobalVariables.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(GlobalVariables.getProfileImageUrl()).fit().into(userImageIndicator);
        }
        messagesViewPager.setOffscreenPageLimit(1);
        TabAdapterTitle tabAdapter = new TabAdapterTitle(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        tabAdapter.addFragment(new SentMessagesFragment(), "الرسائل الصادرة");
        tabAdapter.addFragment(new ReceivedMessagesFragment(), "الرسائل الواردة");
        messagesViewPager.setAdapter(tabAdapter);

        messagesViewPager.setCurrentItem(1);

        messagesViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    arrowIv.animate().translationX(sentTv.getLeft() + (sentTv.getMeasuredWidth() / 2)).setDuration(150).start();
                } else {
                    arrowIv.animate().translationX(receivedTv.getLeft() + (receivedTv.getMeasuredWidth() / 2)).setDuration(150).start();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        sentTv.setOnClickListener(v -> {
            if (messagesViewPager.getCurrentItem() != 0) {
                messagesViewPager.setCurrentItem(0);
                arrowIv.animate().translationX(sentTv.getLeft() + (sentTv.getMeasuredWidth() / 2)).setDuration(150).start();
            }
        });
        receivedTv.setOnClickListener(v -> {
            if (messagesViewPager.getCurrentItem() != 1) {
                messagesViewPager.setCurrentItem(1);
                arrowIv.animate().translationX(receivedTv.getLeft() + (receivedTv.getMeasuredWidth() / 2)).setDuration(150).start();
            }
        });
        arrowIv.post(() -> arrowIv.animate().translationX(receivedTv.getLeft() + (receivedTv.getMeasuredWidth() / 2)).setDuration(0).start());

    }
}
