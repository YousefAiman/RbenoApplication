package com.example.yousef.rbenoapplication;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


public class MessagesFragment extends Fragment {
    private ViewPager messagesViewPager;
    private TextView receivedTv;
    private TextView sentTv;

    public MessagesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_messages, container, false);
        messagesViewPager = view.findViewById(R.id.messagesViewPager);
        receivedTv = view.findViewById(R.id.receivedTv);
        sentTv = view.findViewById(R.id.sentTv);
        ((Toolbar) view.findViewById(R.id.messagesToolBar)).setNavigationOnClickListener(view1 ->
                ((HomeActivity) getActivity()).showDrawer());
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//    messagesViewPager.setOffscreenPageLimit(1);
        final TabAdapterTitle tabAdapter = new TabAdapterTitle(getChildFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        final Fragment sentFrag = new SavedMessagesFragment();
        final Bundle b = new Bundle();
        b.putInt("type", 0);
        sentFrag.setArguments(b);
        tabAdapter.addFragment(sentFrag);

        final Fragment receivedFrag = new SavedMessagesFragment();
        final Bundle b2 = new Bundle();
        b2.putInt("type", 1);
        receivedFrag.setArguments(b2);
        tabAdapter.addFragment(receivedFrag);

        messagesViewPager.setAdapter(tabAdapter);

        messagesViewPager.setCurrentItem(1);

        messagesViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
//          arrowIv.animate().translationX(sentTv.getLeft() + (sentTv.getMeasuredWidth() / 2)).setDuration(150).start();
                    receivedTv.setTextColor(getResources().getColor(R.color.red));
                    sentTv.setTextColor(getResources().getColor(R.color.light_grey));
                } else {
//          arrowIv.animate().translationX(receivedTv.getLeft() + (receivedTv.getMeasuredWidth() / 2)).setDuration(150).start();
                    sentTv.setTextColor(getResources().getColor(R.color.red));
                    receivedTv.setTextColor(getResources().getColor(R.color.light_grey));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        sentTv.setOnClickListener(v -> {
            if (messagesViewPager.getCurrentItem() != 0) {
                messagesViewPager.setCurrentItem(0);
//        arrowIv.animate().translationX(sentTv.getLeft() + (sentTv.getMeasuredWidth() / 2)).setDuration(150).start();
                sentTv.setTextColor(getResources().getColor(R.color.red));
                receivedTv.setTextColor(getResources().getColor(R.color.light_grey));
            }
        });
        receivedTv.setOnClickListener(v -> {
            if (messagesViewPager.getCurrentItem() != 1) {
                messagesViewPager.setCurrentItem(1);
//        arrowIv.animate().translationX(receivedTv.getLeft() + (receivedTv.getMeasuredWidth() / 2)).setDuration(150).start();
                receivedTv.setTextColor(getResources().getColor(R.color.red));
                sentTv.setTextColor(getResources().getColor(R.color.light_grey));
            }
        });
//    arrowIv.post(() -> arrowIv.animate().translationX(receivedTv.getLeft() + (receivedTv.getMeasuredWidth() / 2)).setDuration(0).start());
    }

    public void resetToFirstFragment() {
        receivedTv.performClick();
    }
}
