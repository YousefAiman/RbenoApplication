package com.example.yousef.rbenoapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BlockedUsersFragment extends Fragment implements
        BlockedUserAdapter.BlockedClickListener {

  private final CollectionReference usersRef = FirebaseFirestore.getInstance()
          .collection("users");
  private RecyclerView blockedRv;
  private List<BlockedUser> blockedUsers;
  private BlockedUserAdapter adapter;
  private boolean isLoading = false;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_blocked_users, container, false);


    ((Toolbar) view.findViewById(R.id.promotiontoolbar)).setOnMenuItemClickListener(item -> {
      getActivity().onBackPressed();
      return true;
    });

    blockedRv = view.findViewById(R.id.blockedRv);

    return view;
  }


  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    blockedUsers = new ArrayList<>();

    adapter = new BlockedUserAdapter(blockedUsers, this);
//    adapter.setHasStableIds(true);
    blockedRv.setAdapter(adapter);


    getAllBlockedUsers();


    final AdView mAdView = view.findViewById(R.id.adView);
    mAdView.loadAd(new AdRequest.Builder().build());
    mAdView.setAdListener(new AdListener() {
      @Override
      public void onAdLoaded() {
        mAdView.setVisibility(View.VISIBLE);
      }
    });

  }

  void getBlockedUsersFromBlockedList(List<String> blockedList) {

    isLoading = true;
    final int previousSize = blockedUsers.size();

    usersRef.whereIn("userId", blockedList)
            .get()
            .addOnSuccessListener(snapshots -> {
              for (DocumentSnapshot snapshot : snapshots) {

                blockedUsers.add(new BlockedUser(
                        snapshot.getString("username"),
                        snapshot.getString("imageurl"),
                        snapshot.getString("userId"),
                        snapshot.getBoolean("status")
                ));

              }
            }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {

        if (previousSize == 0) {
          adapter.notifyItemRangeInserted(0, blockedUsers.size());
        } else {
          adapter.notifyItemRangeInserted(previousSize, blockedUsers.size()
                  - previousSize);
        }

        isLoading = false;
      }
    });

  }

  void getAllBlockedUsers() {

    if (!GlobalVariables.getBlockedUsers().isEmpty()) {

      if (GlobalVariables.getBlockedUsers().size() > 10) {

        getBlockedUsersFromBlockedList(GlobalVariables.getBlockedUsers().subList(0, 10));

        blockedRv.addOnScrollListener(new RecyclerView.OnScrollListener() {

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (!blockedRv.canScrollVertically(1) && dy > 0) {
              if (!isLoading) {
                isLoading = true;

                if (GlobalVariables.getBlockedUsers().size() >= blockedUsers.size() + 10) {
                  getBlockedUsersFromBlockedList(GlobalVariables.getBlockedUsers().subList(
                          blockedUsers.size(), blockedUsers.size() + 10));
                } else {
                  blockedRv.removeOnScrollListener(this);

                  if (GlobalVariables.getBlockedUsers().size() > blockedUsers.size()) {
                    getBlockedUsersFromBlockedList(GlobalVariables.getBlockedUsers().subList(
                            blockedUsers.size(), GlobalVariables.getBlockedUsers().size()));
                  }


                }

              }
            }
          }
        });


      } else {

        getBlockedUsersFromBlockedList(GlobalVariables.getBlockedUsers());

      }
    }
  }

  @Override
  public void onBlockedClickListener(String id) {

    final UserFragment fragment = new UserFragment();
    Bundle bundle = new Bundle();
    bundle.putString("promouserid", id);
    fragment.setArguments(bundle);

    ((HomeActivity) getActivity()).addFragmentToHomeContainer(fragment);

  }

}
