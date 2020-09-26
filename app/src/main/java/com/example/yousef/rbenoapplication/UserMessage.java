package com.example.yousef.rbenoapplication;

class UserMessage {

    private String chattingUserImage;
    private String chattingUsername;
    private long chattingPromoId;
    private String chattingLatestMessage;
    private long chattingLatestMessageTime;
    private String messagingUserId;
    private long lastMessageRead;
    private long messagesCount;

    String getChattingUserImage() {
        return chattingUserImage;
    }

    void setChattingUserImage(String chattingUserImage) {
        this.chattingUserImage = chattingUserImage;
    }

    String getChattingUsername() {
        return chattingUsername;
    }

    void setChattingUsername(String chattingUsername) {
        this.chattingUsername = chattingUsername;
    }

    long getChattingPromoId() {
        return chattingPromoId;
    }

    void setChattingPromoId(long chattingPromoId) {
        this.chattingPromoId = chattingPromoId;
    }

    String getChattingLatestMessage() {
        return chattingLatestMessage;
    }

    void setChattingLatestMessage(String chattingLatestMessage) {
        this.chattingLatestMessage = chattingLatestMessage;
    }

    long getChattingLatestMessageTime() {
        return chattingLatestMessageTime;
    }

    void setChattingLatestMessageTime(long chattingLatestMessageTime) {
        this.chattingLatestMessageTime = chattingLatestMessageTime;
    }

    String getMessagingUserId() {
        return messagingUserId;
    }

    void setMessagingUserId(String messagingUserId) {
        this.messagingUserId = messagingUserId;
    }

    long getLastMessageRead() {
        return lastMessageRead;
    }

    void setLastMessageRead(long lastMessageRead) {
        this.lastMessageRead = lastMessageRead;
    }

    long getMessagesCount() {
        return messagesCount;
    }

    void setMessagesCount(long messagesCount) {
        this.messagesCount = messagesCount;
    }
}
