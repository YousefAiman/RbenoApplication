package com.example.yousef.rbenoapplication;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class Notification implements Serializable {

    @PropertyName("promoId")
    public long promoId;
    @PropertyName("senderId")
    public String senderId;
    @PropertyName("receiverId")
    public String receiverId;
    @PropertyName("type")
    public String type;
    @PropertyName("timeCreated")
    public long timeCreated;
//  @PropertyName("sent")
//  private Boolean sent;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public long getPromoId() {
        return promoId;
    }

    public void setPromoId(long promoId) {
        this.promoId = promoId;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

//  public Boolean getSent() {
//    return sent;
//  }
//
//  public void setSent(Boolean sent) {
//    this.sent = sent;
//  }
}
