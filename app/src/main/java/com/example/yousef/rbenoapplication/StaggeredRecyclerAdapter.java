package com.example.yousef.rbenoapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StaggeredRecyclerAdapter extends RecyclerView.Adapter<StaggeredViewHolder> {

    private ArrayList<Promotion> mypromotions;

    StaggeredRecyclerAdapter(ArrayList<Promotion> mypromotions) {
        this.mypromotions = mypromotions;
    }

    @NonNull
    @Override
    public StaggeredViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StaggeredViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.
                relativeitem, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull StaggeredViewHolder holder, int position) {
        holder.bind(mypromotions.get(position), position, holder.itemView.getContext());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mypromotions.size();
    }

    public void clear() {
        mypromotions.clear();
        notifyDataSetChanged();
    }
}
