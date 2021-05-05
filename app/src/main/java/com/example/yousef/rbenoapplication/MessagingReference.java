package com.example.yousef.rbenoapplication;

public class MessagingReference {

    String sender;
    String receiver;
    long promoId;
    boolean receiverDeleted;
    boolean senderDeleted;

    public MessagingReference() {
    }

    public MessagingReference(String sender, String receiver,
                              long promoId, boolean receiverDeleted, boolean senderDeleted) {
        this.sender = sender;
        this.receiver = receiver;
        this.promoId = promoId;
        this.receiverDeleted = receiverDeleted;
        this.senderDeleted = senderDeleted;
    }
}


