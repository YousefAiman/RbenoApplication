package com.example.yousef.rbenoapplication;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;

@IgnoreExtraProperties
public class MessageMap implements Serializable {


  @PropertyName("content")
  public String content;
  @PropertyName("deleted")
  public boolean deleted;
  @PropertyName("sender")
  public int sender;
  @PropertyName("time")
  public long time;
  @Exclude
  private String id;


  public MessageMap() {
  }


  public MessageMap(String content, boolean deleted, int sender, long time) {
    this.content = content;
    this.deleted = deleted;
    this.sender = sender;
    this.time = time;
  }

  public MessageMap(String content, long time, int sender) {
    this.content = content;
    this.time = time;
    this.sender = sender;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public int getSender() {
    return sender;
  }

  public void setSender(int sender) {
    this.sender = sender;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
