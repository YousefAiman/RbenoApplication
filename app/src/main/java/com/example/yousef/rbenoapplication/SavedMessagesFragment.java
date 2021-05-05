package com.example.yousef.rbenoapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SavedMessagesFragment extends Fragment {

  private final static int PAGINATION = 5;
  private final List<UserMessage> userMessages = new ArrayList<>();
  private final String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  private RecyclerView chatsRv;
  private TextView noMessagesTv, moreMessagesTv;
  private MessagingUserAdapter adapter;
  private int type;
//  private DocumentSnapshot lastSnapShot;

  private final CollectionReference userRef =
          FirebaseFirestore.getInstance().collection("users");

  private String messagingUserId;

  //  private List<String> savedMessagesKeys;
  private Query childQuery;


  private Map<DatabaseReference, ChildEventListener> childEventListeners;
  private Map<DatabaseReference, ValueEventListener> valueEventListeners;
  private List<DataSnapshot> snapshots;

  public SavedMessagesFragment() {

  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    type = getArguments().getInt("type");
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_pager_messages, container, false);
    chatsRv = view.findViewById(R.id.chattingUserRv);
    chatsRv.setNestedScrollingEnabled(false);
    noMessagesTv = view.findViewById(R.id.noMessagesTv);
    moreMessagesTv = view.findViewById(R.id.moreMessagesTv);
    final AdView adView = view.findViewById(R.id.adView);
    adView.loadAd(new AdRequest.Builder().build());
    adView.setAdListener(new AdListener() {
      @Override
      public void onAdLoaded() {
        adView.setVisibility(View.VISIBLE);
      }
    });

    final LinearLayoutManager llm = new LinearLayoutManager(getContext(),
            RecyclerView.VERTICAL, false) {
      @Override
      public void onItemsRemoved(@NonNull RecyclerView recyclerView,
                                 int positionStart, int itemCount) {
        super.onItemsRemoved(recyclerView, positionStart, itemCount);

        Log.d("savedMessages", "on item removed: " + getItemCount());

        if (getItemCount() == 0 && noMessagesTv.getVisibility() == View.GONE) {
          noMessagesTv.setVisibility(View.VISIBLE);
          chatsRv.setVisibility(View.INVISIBLE);
        }

      }

      @Override
      public void onItemsAdded(@NonNull RecyclerView recyclerView,
                               int positionStart, int itemCount) {
        super.onItemsAdded(recyclerView, positionStart, itemCount);

        if (noMessagesTv.getVisibility() == View.VISIBLE) {
          noMessagesTv.setVisibility(View.GONE);
          chatsRv.setVisibility(View.VISIBLE);
        }

      }
    };

    chatsRv.setLayoutManager(llm);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    adapter = new MessagingUserAdapter(getContext(), userMessages);
    chatsRv.setAdapter(adapter);

    getRealTimeMessagingUsers();

  }


  void getRealTimeMessagingUsers() {

    String queryType;

    if (type == 0) {
      queryType = "sender";
      messagingUserId = "receiver";
      noMessagesTv.setText("لا يوجد أي رسائل مرسلة حاليا");
    } else {
      queryType = "receiver";
      messagingUserId = "sender";
      noMessagesTv.setText("لا يوجد أي رسائل واردة حاليا");
    }

//    savedMessagesKeys = new ArrayList<>();

    childQuery = FirebaseDatabase.getInstance().getReference().child("Messages")
            .orderByChild(queryType).equalTo(currentUserUid);

    valueEventListeners = new HashMap<>();
    childEventListeners = new HashMap<>();

    childQuery.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {

        valueEventListeners.put(childQuery.getRef(), this);
//        snapshot.getRef()
//                .orderByChild("messages/time")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                  @Override
//                  public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    for()
//                    Log.d("savedMessages","order count: "+snapshot.getChildrenCount());
//                  }
//
//                  @Override
//                  public void onCancelled(@NonNull DatabaseError error) {
//
//                  }
//                });
//        childQuery.getRef()
//                .orderByChild(currentUserUid+":LastSeenMessage")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                  @Override
//                  public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                    Log.d("savedMessages","query ref count: "+snapshot.getChildrenCount());
//                    for(DataSnapshot dataSnapshot:snapshot.getChildren()){
//                      Log.d("savedMessages","ref dataSnapshot: "+dataSnapshot.getKey());
//                    }
//
//                  }
//
//                  @Override
//                  public void onCancelled(@NonNull DatabaseError error) {
//
//                  }
//                });

        if (snapshot.getChildrenCount() > PAGINATION) {
          snapshots = Lists.newArrayList(snapshot.getChildren());

          for (DataSnapshot dataSnapshot : snapshots) {
            if (dataSnapshot.child("isDeletedFor:" + currentUserUid).getValue(Boolean.class)) {
              snapshots.remove(dataSnapshot);
            }
          }

          if (snapshots.size() == 0) {
            noMessagesTv.setVisibility(View.VISIBLE);
            chatsRv.setVisibility(View.INVISIBLE);
          }

          Collections.sort(snapshots, (snapshot1, snapshot2)
                  -> Long.compare(getTime(snapshot2), getTime(snapshot1)));

          getNextPage();

          final ChildEventListener childEventListener =
                  new PaginationChildEventListener(snapshots.size());

          childQuery.addChildEventListener(childEventListener);

          childEventListeners.put(childQuery.getRef(), childEventListener);

//          Map<DataSnapshot,Long> orderedSnapshots = new HashMap<>();
//          for(DataSnapshot dataSnapshot:snapshots){
//            orderedSnapshots.put(dataSnapshot,getTime(dataSnapshot));
//          }

          Log.d("savedMessages", "getting next page");
        } else if (snapshot.getChildrenCount() > 0) {

          final List<DataSnapshot> snapshots = Lists.newArrayList(snapshot.getChildren());


          for (DataSnapshot dataSnapshot : snapshots) {
            if (dataSnapshot.child("isDeletedFor:" + currentUserUid).getValue(Boolean.class)) {
              snapshots.remove(dataSnapshot);
            }
          }


          if (snapshots.size() == 0) {
            noMessagesTv.setVisibility(View.VISIBLE);
            chatsRv.setVisibility(View.INVISIBLE);
          }

          Collections.sort(snapshots, (snapshot1, snapshot2)
                  -> Long.compare(getTime(snapshot2), getTime(snapshot1)));

//          new Thread(new Runnable() {
//            @Override
//            public void run() {
//              Collections.sort(snapshots, (snapshot1, snapshot2) -> {
//                try {
//                  return Long.compare(getTime(snapshot1,Thread.currentThread()),
//                          getTime(snapshot2,Thread.currentThread()));
//                } catch (InterruptedException e) {
//                  e.printStackTrace();
//                }
//                return 0;
//              });
//              Log.d("savedMessages","sorting ended");
//            }
//          }).start();
//          Collections.sort(snapshots, (snapshot1, snapshot2)
//                  -> Long.compare(getTime(snapshot1,Thread.currentThread()), getTime(snapshot2)));
//
          for (DataSnapshot child : snapshots) {
            addSavedMessageFromDataSnapshot(child, true);
          }

          final ChildEventListener childEventListener =
                  new InitialChildEventListener(snapshots.size());

          childQuery.addChildEventListener(childEventListener);

          childEventListeners.put(childQuery.getRef(), childEventListener);
        } else {

          noMessagesTv.setVisibility(View.VISIBLE);
          chatsRv.setVisibility(View.INVISIBLE);

          final ChildEventListener childEventListener = new InitialChildEventListener(0);
          childQuery.addChildEventListener(childEventListener);

          childEventListeners.put(childQuery.getRef(), childEventListener);

        }

//        FirebaseDatabase.getInstance().getReference()
//                .child("Messages")
//                .orderByChild(queryType)
//                .equalTo(currentUserUid)


      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });
//
//    childQuery.orderByKey()
////                .endAt(snapshots.get(snapshots.size()-1).getKey())
//            .addChildEventListener(new ChildEventListener() {
//              @Override
//              public void onChildAdded(@NonNull DataSnapshot snapshot,
//                                       @Nullable String previousChildName) {
//                Log.d("savedMessages","onChildAdded: "+snapshot.getKey());
//              }
//
//              @Override
//              public void onChildChanged(@NonNull DataSnapshot snapshot,
//                                         @Nullable String previousChildName) {
//                Log.d("savedMessages","onChildChanged: "+snapshot.getKey());
//              }
//
//              @Override
//              public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//              }
//
//              @Override
//              public void onChildMoved(@NonNull DataSnapshot snapshot,
//                                       @Nullable String previousChildName) {
//
//              }
//
//              @Override
//              public void onCancelled(@NonNull DatabaseError error) {
//
//              }
//            });

  }

  long getTime(DataSnapshot snapshot) {

    final DataSnapshot databaseReference = snapshot.child("messages")
            .child(String.valueOf(snapshot.child("messages").getChildrenCount() - 1))
            .child("time");

    return databaseReference.exists() ? databaseReference.getValue(Long.class) : 0;
  }


  void addSavedMessageFromDataSnapshot(DataSnapshot child, boolean isInitial) {

    final UserMessage userMessage = new UserMessage();
    userMessage.setChattingPromoId(child.child("intendedpromoid").getValue(Long.class));
    userMessage.setLastMessageRead(
            child.child(currentUserUid + ":LastSeenMessage").getValue(Long.class));


    userMessage.setMessagingUserId(child.child(messagingUserId).getValue(String.class));

    final DataSnapshot messageChild =
            child.child("messages").child(String.valueOf(
                    child.child("messages").getChildrenCount() - 1));

    userMessage.setChattingLatestMessageMap(messageChild.getValue(MessageMap.class));

//    userMessage.setChattingLatestMessageMap(new MessageMap(
//            messageChild.child("content").getValue(String.class),
//            messageChild.child("deleted").getValue(Boolean.class),
//            messageChild.child("sender").getValue(Integer.class),
//            messageChild.child("time").getValue(Long.class)
//    ));
//


    userMessage.setMessagesCount(Integer.parseInt(messageChild.getKey()) + 1);

    Log.d("savedMessages", "message count: " + userMessage.getMessagesCount());

    addMessagesAdditionAndUpdateListener(child, userMessage, messageChild,
            userMessage.getMessagesCount());

    userRef.whereEqualTo("userId", userMessage.getMessagingUserId())
            .get().addOnSuccessListener(snaps -> {
      final DocumentSnapshot userSnap = snaps.getDocuments().get(0);
      userMessage.setChattingUsername(userSnap.getString("username"));
      userMessage.setChattingUserImage(userSnap.getString("imageurl"));


      if (isInitial) {

        userMessages.add(userMessage);
        adapter.notifyItemInserted(userMessages.size());
//        if(userMessages.size() > 1){
//
//          Log.d("savedMessages","sorting");
//          Collections.sort(userMessages, (userMessage1, userMessage2)
//                  -> Long.compare(
//                  userMessage2.getChattingLatestMessageMap().getTime()
//                  , userMessage1.getChattingLatestMessageMap().getTime()));
//
//          adapter.notifyDataSetChanged();
//        }else{
//          Log.d("savedMessages","added to first");
//          adapter.notifyItemInserted(0);
//        }

        if (snapshots != null && !snapshots.isEmpty()) {
          snapshots.remove(child);
        }
      } else {
        userMessages.add(0, userMessage);
        adapter.notifyItemInserted(0);
      }

      addLastSeenListener(child, userMessage);

      addDeletionListener(child, userMessage);

    });

  }


  void addMessagesAdditionAndUpdateListener(DataSnapshot child,
                                            UserMessage userMessage,
                                            DataSnapshot messageChild,
                                            long startAt) {

    final DatabaseReference databaseReference = child.child("messages").getRef();

    ChildEventListener childEventListener;

    Log.d("savedMessages", "listenting from: " + String.valueOf(startAt));

    databaseReference.orderByKey().startAt(String.valueOf(startAt))
            .addChildEventListener(childEventListener = new ChildEventListener() {
              @Override
              public void onChildAdded(@NonNull DataSnapshot snapshot,
                                       @Nullable String previousChildName) {

                Log.d("savedMessages", "message added");

                if ((Long.parseLong(Objects.requireNonNull(snapshot.getKey())) + 1)
                        > userMessage.getMessagesCount()) {

                  Log.d("savedMessages", "message added after last");


                  userMessage.setChattingLatestMessageMap(snapshot.getValue(MessageMap.class));
//                  userMessage.setChattingLatestMessageMap(new MessageMap(
//                          snapshot.child("content").getValue(String.class),
//                          snapshot.child("deleted").getValue(Boolean.class),
//                          snapshot.child("sender").getValue(Integer.class),
//                          snapshot.child("time").getValue(Long.class)
//                  ));


                  userMessage.setMessagesCount(userMessage.getMessagesCount() + 1);

                  final int index = userMessages.indexOf(userMessage);

                  adapter.notifyItemChanged(index);

                  if (index > 0) {
                    Collections.swap(userMessages, index, 0);
                    adapter.notifyItemMoved(index, 0);
//                    adapter.notifyItemMoved(0,index);
                  }

                }

//                databaseReference.removeEventListener(this);
//
//                addMessagesAdditionAndUpdateListener(child,userMessage,messageChild
//                        ,userMessage.getMessagesCount());

                Log.d("savedMessages", "listenting from: " +
                        (Integer.parseInt(snapshot.getKey()) + 1));

              }

              @Override
              public void onChildChanged(@NonNull DataSnapshot snapshot,
                                         @Nullable String previousChildName) {

                Log.d("savedMessages", "message changed: " + snapshot.getKey());
                if (snapshot.exists() &&
                        (Long.parseLong(Objects.requireNonNull(snapshot.getKey())) + 1)
                                == userMessage.getMessagesCount()) {


                  final MessageMap messageMap = snapshot.getValue(MessageMap.class);

//                  MessageMap messageMap= new MessageMap(
//                          snapshot.child("content").getValue(String.class),
//                          snapshot.child("deleted").getValue(Boolean.class),
//                          snapshot.child("sender").getValue(Integer.class),
//                          snapshot.child("time").getValue(Long.class)
//                  );


                  if (messageMap == null)
                    return;


//                  if(userMessage.getChattingLatestMessageMap().getContent()
//                          .equals(messageMap.getContent())
//                          && messageMap.getTime() == messageMap.getTime()){

                  Log.d("savedMessages", "onChildChanged: " +
                          userMessage.getChattingLatestMessageMap().getContent());


                  userMessage.getChattingLatestMessageMap().setDeleted(true);
                  adapter.notifyItemChanged(userMessages.indexOf(userMessage));

//                  }
                }

              }

              @Override
              public void onChildRemoved(@NonNull DataSnapshot snapshot) {

              }

              @Override
              public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
            });


    childEventListeners.put(databaseReference, childEventListener);

  }

  void addDeletionListener(DataSnapshot child, UserMessage userMessage) {

    final DatabaseReference databaseReference =
            child.child("isDeletedFor:" +
//                    (type == 0?"sender":"receiver")
                            currentUserUid
            ).getRef();

    ValueEventListener valueEventListener;
    databaseReference.addValueEventListener(valueEventListener = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.getValue(Boolean.class)) {

          final DatabaseReference lastSeenRef =
                  child.child(currentUserUid + ":LastSeenMessage").getRef();

          if (valueEventListeners.containsKey(lastSeenRef)) {
            lastSeenRef.removeEventListener(valueEventListeners.get(lastSeenRef));
          } else {
            Log.d("savedMessages", "this last seen listener doesn't exist");
          }

          final DatabaseReference messagesRef = child.child("messages").getRef();
          if (childEventListeners.containsKey(messagesRef)) {
            messagesRef.removeEventListener(childEventListeners.get(messagesRef));
          } else {
            Log.d("savedMessages", "this child messages listener doesn't exist");
          }


          valueEventListeners.remove(databaseReference);
          databaseReference.removeEventListener(this);

          final int index = userMessages.indexOf(userMessage);
          userMessages.remove(index);
          adapter.notifyItemRemoved(index);
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });
    valueEventListeners.put(databaseReference, valueEventListener);

  }

  void addLastSeenListener(DataSnapshot child, UserMessage userMessage) {

    final DatabaseReference lastSeenRef =
            child.child(currentUserUid + ":LastSeenMessage").getRef();

    ValueEventListener valueEventListener;
    lastSeenRef
//            .orderByValue().startAt(userMessage.getLastMessageRead()+1)
            .addValueEventListener(valueEventListener = new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("savedMessages", "last seen changed to: " +
                        snapshot.getValue(Long.class));
                if (snapshot.exists()) {
                  userMessage.setLastMessageRead(snapshot.getValue(Long.class));
                  adapter.notifyItemChanged(userMessages.indexOf(userMessage));
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {
              }
            });

    valueEventListeners.put(lastSeenRef, valueEventListener);

  }


  void getNextPage() {

    if (snapshots.size() > PAGINATION) {

      for (int i = 0; i < PAGINATION; i++) {
        addSavedMessageFromDataSnapshot(snapshots.get(i), true);
      }

      if (moreMessagesTv.getVisibility() == View.GONE) {
        moreMessagesTv.setVisibility(View.VISIBLE);
        moreMessagesTv.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            Log.d("savedMessages", "snapshots: " + snapshots.size());
            getNextPage();
          }
        });
      }

    } else {

      moreMessagesTv.setVisibility(View.GONE);
      moreMessagesTv.setOnClickListener(null);

      for (DataSnapshot child : snapshots) {
        addSavedMessageFromDataSnapshot(child, true);
      }
    }

  }

//  void updateSavedMessage(DataSnapshot snapshot,String queryType){
//
//    Log.d("savedMessages","updating snapshot");
//    if(savedMessagesKeys!=null && !savedMessagesKeys.isEmpty()){
//      final int index = savedMessagesKeys.indexOf(snapshot.getKey());
//      if(index >= 0 && index < userMessages.size()){
//
//        final UserMessage changedUserMessage = userMessages.get(index);
//
//        if(snapshot.child("isDeletedFor:"+currentUserUid).getValue(Boolean.class)){
//          userMessages.remove(index);
//          savedMessagesKeys.remove(index);
//          adapter.notifyItemRemoved(index);
//        }else{
//
//          final long lastMessageRead =
//                  snapshot.child(currentUserUid+":LastSeenMessage").getValue(Long.class);
//
//          if(lastMessageRead > changedUserMessage.getLastMessageRead()){
//            Log.d("savedMessages","last message seen updated");
//            changedUserMessage.setLastMessageRead(lastMessageRead);
//            changedUserMessage.setMessagesCount(snapshot.child("messages").getChildrenCount());
//            adapter.notifyItemChanged(index);
//          }else{
//
//            Log.d("savedMessages","snapshot.getKey(): "+snapshot.getKey());
////            Log.d("savedMessages","children count: "+
////                    snapshot.child("messages").getChildrenCount());
////            DataSnapshot lastChild =
////                    snapshot.child("messages").child(String.valueOf(snapshot.getChildrenCount()-1));
////
////
////            Log.d("savedMessages","last index: "+
////                    String.valueOf(snapshot.getChildrenCount()-1));
////            Log.d("savedMessages","lastChild: "+lastChild.getKey());
////
////            MessageMap lastMessage =
////                    lastChild
////                    .getValue(MessageMap.class);
//            MessageMap lastMessage =
//                    snapshot.child("messages")
//                    .child(String.valueOf(snapshot.child("messages").getChildrenCount()-1))
//                    .getValue(MessageMap.class);
//
//            if(lastMessage == null)
//              return;
//
//
//            if(lastMessage.isDeleted()){
//              if(lastMessage.getContent().equals(
//                      changedUserMessage.getChattingLatestMessageMap().getContent())
//                      && lastMessage.getTime() ==
//                      changedUserMessage.getChattingLatestMessageMap().getTime()){
//                changedUserMessage.getChattingLatestMessageMap().setDeleted(true);
//                adapter.notifyItemChanged(index);
//
//              }
//            }else{
//              if(lastMessage != changedUserMessage.getChattingLatestMessageMap()){
//                final int addedIndex = userMessages.indexOf(changedUserMessage);
//                changedUserMessage.setChattingLatestMessageMap(lastMessage);
//
//                userMessages.remove(addedIndex);
//                userMessages.add(0,changedUserMessage);
//
//                adapter.notifyItemRemoved(addedIndex);
//                adapter.notifyItemInserted(0);
//              }
//            }
//
////            snapshot.getRef().child("messages")
////                    .orderByKey().limitToLast(1)
////                    .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
////              @Override
////              public void onSuccess(DataSnapshot snapshot) {
////                MessageMap lastMessage = snapshot.getValue(MessageMap.class);
//
////                if(lastMessage!=null){
////                  Log.d("savedMessages","lastMessage.getContent(): "+lastMessage.getContent());
////                }else{
////                  Log.d("savedMessages","lastMessage is null");
////                }
////
////                if(changedUserMessage.getChattingLatestMessageMap().isDeleted()){
////                  if(lastMessage != changedUserMessage.getChattingLatestMessageMap()){
////                    changedUserMessage.setChattingLatestMessageMap(lastMessage);
////                    adapter.notifyItemChanged(index);
////                  }
////                }else if(lastMessage.isDeleted()){
////
////                  if(lastMessage.getContent().equals(
////                          changedUserMessage.getChattingLatestMessageMap().getContent())
////                          && lastMessage.getTime() ==
////                          changedUserMessage.getChattingLatestMessageMap().getTime()){
////                    changedUserMessage.getChattingLatestMessageMap().setDeleted(true);
////                    adapter.notifyItemChanged(index);
////
////                  }
////                }
//
////                if(lastMessage.getContent().equals(
////                        changedUserMessage.getChattingLatestMessageMap().getContent())
////                && )
////                if(lastMessage != changedUserMessage.getChattingLatestMessageMap()){
////                  changedUserMessage.setChattingLatestMessageMap(lastMessage);
////                  adapter.notifyItemChanged(index);
////                }
////              }
////            });
//
//          }
//
//        }
//      }
//    }
//
//  }


  class PaginationChildEventListener implements ChildEventListener {

    int count;

    PaginationChildEventListener(int count) {
      this.count = count;

    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
      Log.d("savedMessages", "onChildAdded: " + snapshot.getKey() +
              " previousChildName: " + previousChildName);

      if (count != 0) {

        count--;

      } else {
        if (!snapshot.child("isDeletedFor:" + currentUserUid).getValue(Boolean.class)) {
          addSavedMessageFromDataSnapshot(snapshot, false);
          Log.d("savedMessages", "added after initial hehe");
        } else {
          Log.d("savedMessages", "some guy tried adding to a delted sht");
        }
      }
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
      for (DataSnapshot dataSnapshot : snapshots) {
        if (dataSnapshot.getKey().equals(snapshot.getKey())) {
          Log.d("savedMessages", "exists but not added");
          if (snapshot.child("messages").getChildrenCount()
                  > dataSnapshot.child("messages").getChildrenCount()) {

            Log.d("savedMessages", "added a message to not added");

            snapshots.remove(dataSnapshot);
            if (snapshots.size() == 0) {
              moreMessagesTv.setVisibility(View.GONE);
              moreMessagesTv.setOnClickListener(null);
            }
            addSavedMessageFromDataSnapshot(dataSnapshot, false);

          }
          break;
        }

        Log.d("savedMessages", "onChildChanged: " + snapshot.getKey());
      }
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
  }


  class InitialChildEventListener implements ChildEventListener {
    int count;

    InitialChildEventListener(int count) {
      this.count = count;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
      Log.d("savedMessages", "initial onChildAdded: " + snapshot.getKey() +
              " previousChildName: " + previousChildName);
      if (count != 0) {
        count--;
      } else {

        if (!snapshot.child("isDeletedFor:" + currentUserUid).getValue(Boolean.class)) {
          addSavedMessageFromDataSnapshot(snapshot, false);
          Log.d("savedMessages", "added after initial hehe");
        } else {
          Log.d("savedMessages", "some guy tried adding to a delted sht");
        }
      }
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


      Log.d("savedMessages", "onChildChanged: " + snapshot.getKey());
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
  }


  @Override
  public void onDestroy() {
    super.onDestroy();

    if (childEventListeners != null && !childEventListeners.isEmpty()) {

      for (DatabaseReference reference : childEventListeners.keySet()) {
        reference.removeEventListener(Objects.requireNonNull(childEventListeners.get(reference)));
      }
    }

    if (valueEventListeners != null && !valueEventListeners.isEmpty()) {

      for (DatabaseReference reference : valueEventListeners.keySet()) {
        reference.removeEventListener(Objects.requireNonNull(valueEventListeners.get(reference)));
      }
    }
  }

}
