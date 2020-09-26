package com.example.yousef.rbenoapplication;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class TabAdapterTitle extends FragmentStatePagerAdapter {
    private List<Fragment> FragmentList = new ArrayList<>();
    private List<String> FragmentTitleList = new ArrayList<>();

    TabAdapterTitle(FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentList.get(position);
    }

    void addFragment(Fragment fragment, String title) {
        FragmentList.add(fragment);
        FragmentTitleList.add(title);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return FragmentTitleList.get(position);
    }


    @Override
    public int getCount() {
        return FragmentList.size();
    }

//    public View getTabView(int position) {
//        View view = LayoutInflater.from(context).inflate(R.layout.custom_tab_view, null);
//        TextView tabTextView = view.findViewById(R.id.tabTextView);
//        tabTextView.setText(FragmentTitleList.get(position));
//        return view;
//    }

//
//    public View getSelectedTabView(int position) {
//        View view = LayoutInflater.from(context).inflate(R.layout.custom_tab_view, null);
//        TextView tabTextView = view.findViewById(R.id.tabTextView);
//        tabTextView.setText(FragmentTitleList.get(position));
//        tabTextView.setTextColor(ContextCompat.getColor(context, R.color.red));
//        return view;
//    }
}
