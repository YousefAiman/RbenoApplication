package com.example.yousef.rbenoapplication;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;

public class GroupedMessages
        implements Comparable<GroupedMessages> {

  //  @ServerTimestamp
//  private Date date;
  @PropertyName("sender")
  public String sender;
  @PropertyName("receiver")
  public String receiver;
  @PropertyName("messages")
  public ArrayList<String> messages;
  @PropertyName("timsent")
  public long timesent;
  @PropertyName("intendedpromoid")
  public long intendedpromoid;

  public GroupedMessages(String sender, String receiver, ArrayList<String> messages,
                         long timesent, int intendedpromoid) {

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

  public long getTimesent() {
    return timesent;
  }

  public void setTimesent(long timesent) {
    this.timesent = timesent;
  }

  public long getIntendedpromoid() {
    return intendedpromoid;
  }

  public void setIntendedpromoid(long intendedpromoid) {
    this.intendedpromoid = intendedpromoid;
  }

  @Override
  public int compareTo(GroupedMessages groupedMessages) {
    return (int) (this.getTimesent() - groupedMessages.getTimesent());
  }

//  public Date getDate() {
//    return date;
//  }
//
//  public void setDate(Date date) {
//    this.date = date;
//  }
}
