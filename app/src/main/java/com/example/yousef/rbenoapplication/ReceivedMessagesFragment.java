package com.example.yousef.rbenoapplication;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ReceivedMessagesFragment extends Fragment {

    RecyclerView chatsRv;
    List<UserMessage> userMessages;
    ListenerRegistration listener;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    TextView noMessagesTv;
    MessagingUserAdapter adapter;

    public ReceivedMessagesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager_messages, container, false);
        chatsRv = view.findViewById(R.id.chattingUserRv);
        noMessagesTv = view.findViewById(R.id.noMessagesTv);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userMessages = new ArrayList<>();
        adapter = new MessagingUserAdapter(getContext(), userMessages);
        adapter.setHasStableIds(true);
        chatsRv.setAdapter(adapter);
        chatsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        getMessagingUsers();
    }

    private void getMessagingUsers() {

        listener = FirebaseFirestore.getInstance().collection("chats").whereEqualTo("receiver", currentUser.getUid()).addSnapshotListener((snapshots, e) -> {
            if (snapshots.isEmpty()) {
                noMessagesTv.setText("لا يوجد أي رسائل واردة حاليا");
                noMessagesTv.setVisibility(View.VISIBLE);
            } else {
                for (DocumentChange documentChange : snapshots.getDocumentChanges()) {
                    DocumentSnapshot ds = documentChange.getDocument();
                    DocumentChange.Type type = documentChange.getType();
                    if (type == DocumentChange.Type.ADDED) {
                        final GroupedMessages messagegroup = ds.toObject(GroupedMessages.class);
                        final UserMessage userMessage = new UserMessage();
                        FirebaseFirestore.getInstance().collection("users").whereEqualTo("userId", messagegroup.getSender()).limit(1).get().addOnSuccessListener(snapshots1 -> {
                            DocumentSnapshot userSnap = snapshots1.getDocuments().get(0);
                            userMessage.setChattingUsername(userSnap.getString("username"));
                            userMessage.setChattingUserImage(userSnap.getString("imageurl"));
                            List<String> messagesFromGroup = messagegroup.getMessages();
                            String lastMessage = messagesFromGroup.get(messagesFromGroup.size() - 1);
                            String[] lastMessageArray = lastMessage.split("--");
                            if (!lastMessageArray[0].contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com")) {
                                userMessage.setChattingLatestMessage(lastMessageArray[0]);
                            } else {
                                userMessage.setChattingLatestMessage("قام بإرسالة صورة!");
                            }
                            userMessage.setChattingLatestMessageTime(Long.parseLong(lastMessageArray[1]));
                            userMessage.setMessagingUserId(messagegroup.getSender());
                            userMessage.setMessagesCount(messagegroup.getMessages().size());
                            userMessage.setChattingPromoId(messagegroup.getIntendedpromoid());
                            userMessage.setLastMessageRead(ds.getLong(currentUser.getUid() + ":LastSeenMessage"));
                            ArrayList<String> messages = (ArrayList<String>) ds.get("messages");
                            userMessage.setMessagesCount(messages.size());
                            userMessages.add(userMessage);
//                            if(chatsRv)
                            adapter.notifyItemInserted(userMessages.size());
                        });
                    } else if (type == DocumentChange.Type.REMOVED) {
                        for (int i = 0; i < userMessages.size(); i++) {
                            UserMessage userMessage = userMessages.get(i);
                            if (userMessage.getMessagingUserId().equals(ds.getString("sender")) &&
                                    userMessage.getChattingPromoId() == ds.getLong("intendedpromoid")) {
                                userMessages.remove(userMessage);
                                adapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                    } else if (type == DocumentChange.Type.MODIFIED) {
                        for (int i = 0; i < userMessages.size(); i++) {
                            UserMessage userMessage = userMessages.get(i);
                            if (userMessage.getMessagingUserId().equals(ds.getString("sender")) &&
                                    userMessage.getChattingPromoId() == ds.getLong("intendedpromoid")) {
                                List<String> messagesFromGroup = ds.toObject(GroupedMessages.class).getMessages();
                                String lastMessage = messagesFromGroup.get(messagesFromGroup.size() - 1);
                                String[] lastMessageArray = lastMessage.split("--");
                                userMessage.setLastMessageRead(ds.getLong(currentUser.getUid() + ":LastSeenMessage"));
                                if (!lastMessageArray[0].contains("https://firebasestorage.googleapis.com/v0/b/rbenoapplication.appspot.com")) {
                                    userMessage.setChattingLatestMessage(lastMessageArray[0]);
                                } else {
                                    userMessage.setChattingLatestMessage("قام بإرسالة صورة!");
                                }
                                userMessage.setMessagesCount(messagesFromGroup.size());
                                userMessage.setChattingLatestMessageTime(Long.parseLong(lastMessageArray[1]));
                                adapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}
