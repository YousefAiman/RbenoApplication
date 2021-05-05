package com.example.yousef.rbenoapplication;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private final NotificationsAdapter mAdapter;

    SwipeToDeleteCallback(NotificationsAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mAdapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (WifiUtil.checkWifiConnection(viewHolder.itemView.getContext())) {
            ((NotificationsAdapter.notificationViewHolder) viewHolder)
                    .deleteNotification(mAdapter.notifications.get(viewHolder.getAdapterPosition()));
        }
    }
}