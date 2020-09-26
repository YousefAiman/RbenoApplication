package com.example.yousef.rbenoapplication;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;

public class GroupedMessages {
    @PropertyName("sender")
    public String sender;
    @PropertyName("receiver")
    public String receiver;
    @PropertyName("messages")
    public ArrayList<String> messages;
    @PropertyName("timesent")
    public String timesent;
    @PropertyName("intendedpromoid")
    public long intendedpromoid;

    public GroupedMessages(String sender, String receiver, ArrayList<String> messages, String timesent, int intendedpromoid) {
        this.setSender(sender);
        this.setReceiver(receiver);
        this.setMessages(messages);
        this.setTimesent(timesent);
        this.setIntendedpromoid(intendedpromoid);
    }

    public GroupedMessages() {

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<String> message) {
        this.messages = message;
    }

    public String getTimesent() {
        return timesent;
    }

    public void setTimesent(String timesent) {
        this.timesent = timesent;
    }

    public long getIntendedpromoid() {
        return intendedpromoid;
    }

    public void setIntendedpromoid(long intendedpromoid) {
        this.intendedpromoid = intendedpromoid;
    }

}
