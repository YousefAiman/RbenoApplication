package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.searchviewholder> implements Filterable {
    private List<String> searches;
    private List<String> filteredSearches;
    private Context context;
    private SearchFragment searchFragment;

    RecentSearchAdapter(Context context, List<String> searches, SearchFragment searchFragment) {
        this.searches = searches;
        this.filteredSearches = searches;
        this.context = context;
        this.searchFragment = searchFragment;
    }

    @NonNull
    @Override
    public searchviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new searchviewholder(LayoutInflater.from(context).inflate(R.layout.searchhintitem, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull searchviewholder holder, int position) {
        holder.recentSearchTv.setText(filteredSearches.get(position));
        holder.itemView.setOnClickListener(v -> {
            searchFragment.submitSearch(filteredSearches.get(position));
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return filteredSearches.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();

                final List<String> list = searches;

                int count = list.size();
                final ArrayList<String> nlist = new ArrayList<>(count);

                String filterableString;

                for (int i = 0; i < count; i++) {
                    filterableString = list.get(i);
                    if (filterableString.toLowerCase().contains(filterString)) {
                        nlist.add(filterableString);
                    }
                }
                results.values = nlist;
                results.count = nlist.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredSearches = (ArrayList<String>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    static class searchviewholder extends RecyclerView.ViewHolder {
        TextView recentSearchTv;

        searchviewholder(@NonNull View itemView) {
            super(itemView);
            recentSearchTv = itemView.findViewById(R.id.searchHintTv);
        }
    }
}
