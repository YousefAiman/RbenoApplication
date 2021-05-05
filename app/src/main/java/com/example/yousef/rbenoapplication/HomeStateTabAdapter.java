package com.example.yousef.rbenoapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;

import java.util.List;

public class HomeStateTabAdapter extends FragmentStateAdapter {

  private final List<Fragment> fragments;

  public HomeStateTabAdapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> fragments) {
    super(fragmentActivity);
    this.fragments = fragments;
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return fragments.get(position);
  }


  @Override
  public void onBindViewHolder(@NonNull FragmentViewHolder holder, int position, @NonNull List<Object> payloads) {
    super.onBindViewHolder(holder, position, payloads);

  }


  @Override
  public void onViewDetachedFromWindow(@NonNull FragmentViewHolder holder) {

//    if(holder.getAdapterPosition() == 0)
//      return;

    super.onViewDetachedFromWindow(holder);

  }


  @Override
  public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
    super.onDetachedFromRecyclerView(recyclerView);
  }

  @Override
  public int getItemCount() {
    return fragments.size();
  }

}
