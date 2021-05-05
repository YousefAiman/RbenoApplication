package com.example.yousef.rbenoapplication;

public class Data {
    private String user;
    private String username;
    //  private int icon;
    private String body;
    private String title;
    //  private String sent;
    private String imageUrl;
    private String type;
    private long promoId;
    private String promoTitle;

//  Data(String user,
////       int icon,
//       String body, String title, String sent, String imageUrl, String username,String type,String promoTitle) {
//    this.user = user;
////    this.icon = icon;
//    this.body = body;
//    this.title = title;
//    this.sent = sent;
//    this.imageUrl = imageUrl;
//    this.username = username;
//    this.type = type;
//    this.promoTitle = promoTitle;
//  }

    Data(String user,
//       int icon,
         String body, String title
//       String sent
            , String imageUrl, String username, String type, long promoId) {
        this.user = user;
//    this.icon = icon;
        this.body = body;
        this.title = title;
//    this.sent = sent;
        this.imageUrl = imageUrl;
        this.username = username;
        this.type = type;
        this.promoId = promoId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

//  public int getIcon() {
//    return icon;
//  }
//
//  public void setIcon(int icon) {
//    this.icon = icon;
//  }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//  public String getSent() {
//    return sent;
//  }
//
//  public void setSent(String sent) {
//    this.sent = sent;
//  }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getPromoTitle() {
        return promoTitle;
    }

    public void setPromoTitle(String promoTitle) {
        this.promoTitle = promoTitle;
    }
}
