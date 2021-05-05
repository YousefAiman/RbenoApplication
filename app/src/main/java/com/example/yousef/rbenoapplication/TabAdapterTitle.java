package com.example.yousef.rbenoapplication;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TabAdapterTitle extends FragmentStatePagerAdapter {
    private final List<Fragment> FragmentList = new ArrayList<>();

    TabAdapterTitle(FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return FragmentList.get(position);
    }

    void addFragment(Fragment fragment) {
        FragmentList.add(fragment);
    }

    @Override
    public int getCount() {
        return FragmentList.size();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    }
}
