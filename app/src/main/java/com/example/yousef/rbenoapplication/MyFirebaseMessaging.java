package com.example.yousef.rbenoapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MyFirebaseMessaging extends FirebaseMessagingService {

  static int notificationNum = 0;
  private NotificationManager notificationManager;
  private SharedPreferences sharedPreferences;

  @Override
  public void onNewToken(@NonNull String s) {
    super.onNewToken(s);
    if (FirebaseAuth.getInstance().getCurrentUser() != null) {

      String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

      FirebaseFirestore.getInstance().collection("users")
              .whereEqualTo("userId", currentUid)
              .get()
              .addOnSuccessListener(snapshots ->
                      snapshots.getDocuments().get(0).getReference().update("token", s));
      Log.d("ttt", "new token: " + s);
    }

  }

  @Override
  public void onCreate() {
    super.onCreate();


    Log.d("ttt", "mesageing servie create dman");
  }

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    Log.d("ttt", "message received");

    Log.d("ttt", "from: " + remoteMessage.getData().get("user"));

    final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    if (remoteMessage.getData().get("user").equals(currentUid)) {
      Log.d("ttt", "this notification is from me wtf");
      return;
    }

//  if(remoteMessage.getFrom().equals(currentUid))


//  if (firebaseUser != null) {
    if (notificationManager == null) {
      notificationManager = (NotificationManager)
              getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }
    if (sharedPreferences == null) {
      sharedPreferences = getSharedPreferences("rbeno", Context.MODE_PRIVATE);
    }
    try {
      if (remoteMessage.getData().get("type").equals("message")) {
        if (sharedPreferences.contains("currentMessagingUserId")) {
          final Map<String, String> data = remoteMessage.getData();

          if (data.get("user")
                  .equals(sharedPreferences.getString("currentMessagingUserId", "")) &&
                  Long.parseLong(data.get("promoId")) ==
                          sharedPreferences.getLong("currentMessagingPromoId", 0)) {

            if (sharedPreferences.contains("isPaused") &&
                    sharedPreferences.getBoolean("isPaused", false)) {
              sendNotification(remoteMessage);
            }
          } else {
            sendNotification(remoteMessage);
          }

//          if (!(data.get("user").equals(sharedPreferences.getString("currentMessagingUserId",
//                  "")) &&
//                  Long.parseLong(data.get("promoId")) ==
//                          sharedPreferences.getLong("currentMessagingPromoId", 0)
//          && sharedPreferences.contains("isPaused") &&
//                  !sharedPreferences.getBoolean("isPaused",false))
//          ) {
//            sendNotification(remoteMessage);
//          }
//          if (sharedPreferences.getBoolean("messagingscreen", true)) {
//
//          } else {
//            sendNotification(remoteMessage);
//          }
        } else {
          sendNotification(remoteMessage);
        }
      } else {
        sendNotification(remoteMessage);
      }
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
//        }
  }

  public void sendNotification(RemoteMessage remoteMessage) throws ExecutionException, InterruptedException {

    Log.d("ttt", "sending notification");

    final Map<String, String> data = remoteMessage.getData();
//    final String title = data.get("title");
    final String type = data.get("type");

    Log.d("ttt", "type: " + type);
    createChannel(type);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, type)
            .setSmallIcon(R.drawable.rbeno_logo_png)
            .setContentTitle(data.get("title"))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentText(data.get("body"))
            .setAutoCancel(true);

    builder.setPriority(NotificationCompat.PRIORITY_HIGH);

    if (data.containsKey("imageUrl")) {
      builder.setLargeIcon(
              Glide.with(this)
                      .asBitmap()
                      .apply(new RequestOptions().override(100, 100))
                      .centerCrop()
                      .load(data.get("imageUrl"))
                      .submit()
                      .get());
    } else {
      Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rbeno_logo_png);
      builder.setLargeIcon(bitmap);
    }


    builder.setGroup(type);


    if (GlobalVariables.getMessagesNotificationMap() == null)
      GlobalVariables.setMessagesNotificationMap(new HashMap<>());

    final String identifierTitle = data.get("user") + type + data.get("promoId");

    builder.setDeleteIntent(
            PendingIntent.getBroadcast(this, notificationNum,
                    new Intent(this, NotificationDeleteListener.class)
                            .putExtra("notificationIdentifierTitle", identifierTitle)
                    , PendingIntent.FLAG_UPDATE_CURRENT));

    if (type.equals("message")) {

//      final Intent newIntent = new Intent(this, MessagingActivity.class);
//      final Intent newIntent = new Intent(
//              "com.example.yousef.rbenoapplication.notificationClick");

      final Intent newIntent = new Intent(this, NotificationClickReceiver.class);


      newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//      TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);


      final Bundle messagingBundle = new Bundle();
      messagingBundle.putString("promouserid", data.get("user"));
      messagingBundle.putLong("intendedpromoid", Long.parseLong(data.get("promoId")));

      newIntent.putExtra("messagingBundle", messagingBundle);

//      stackBuilder.addNextIntentWithParentStack(newIntent);
//      newIntent.putExtra("promouserid", data.get("user"));
//      newIntent.putExtra("intendedpromoid", Long.parseLong(data.get("promoId")));
//      newIntent.putExtra("senderusername",data.get("username"));
      Log.d("ttt", "id: " + data.get("promoId") + "-" + data.get("user")
              + "-" + data.get("username"));

//      PendingIntent pendingIntent =
//              stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

//      try {

//      if(notificationClickReceiver == null){
////        Log.d("ttt",);
//        Log.d("ttt","registering NotificationClickReceiver receiver");
////        notificationClickReceiver = new NotificationClickReceiver();
//        NotificationClickReceiver.registerReceiver(this);
////        final IntentFilter intentFilter = new IntentFilter();
////        intentFilter.addAction("com.example.yousef.rbenoapplication.notificationClick");
////        registerReceiver(notificationClickReceiver, intentFilter);
////        registerReceiver(notificationClickReceiver)
////        notificationClickReceiver.registerReceiver(this);
////        registerReceiver(notificationClickReceiver,this);
//
//      }
//      NotificationClickReceiver.registerReceiver(this);
//        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.example.yousef.rbenoapplication.notificationClick");
//        registerReceiver(new NotificationClickReceiver(), intentFilter);
//      } catch(IllegalArgumentException e) {
//        e.printStackTrace();
//      }


      PendingIntent pendingIntent = PendingIntent
              .getBroadcast(this, notificationNum, newIntent,
                      PendingIntent.FLAG_UPDATE_CURRENT);

      builder.setContentIntent(pendingIntent);


//      final String identifierTitle = "رسالة جديد من: " +
//      data.get("user")+" بخصوص اعلان #"+data.get("promoId");
      NotificationManagerCompat manager = NotificationManagerCompat.from(this);

      if (!GlobalVariables.getMessagesNotificationMap().containsKey(identifierTitle)) {
        notificationNum++;
//        builder.setNumber(notificationNum);
        GlobalVariables.getMessagesNotificationMap().put(identifierTitle, notificationNum);
        Log.d("ttt", "this notification doesn't exist so building");
        manager.notify(notificationNum, builder.build());
//        notificationManager.notify(notificationNum, builder.build());
        updateNotificationSent(data.get("user"), data.get("promoId"), type);

        if (Build.VERSION.SDK_INT < 26) {
          BadgeUtil.incrementBadgeNum(this);
        }

      } else {
        Log.d("ttt", "this notification already exists just updating");
        manager.notify(GlobalVariables.getMessagesNotificationMap().get(identifierTitle)
                , builder.build());
//        notificationManager.notify(GlobalVariables.getMessagesNotificationMap()
//                .get(identifierTitle), builder.build());
//        notificationManager.notify(GlobalVariables.getMessagesNotificationMap()
//                .get(identifierTitle), builder.build());
      }
    } else {

//      builder.setDeleteIntent(
//              PendingIntent.getBroadcast(this, notificationNum,
//                      new Intent(this,NotificationDeleteListener.class)
////                                .putExtra("notificationNum",1)
//                      , PendingIntent.FLAG_ONE_SHOT)
//      );

      if (Build.VERSION.SDK_INT < 26) {
        BadgeUtil.incrementBadgeNum(this);
      }
      notificationNum++;
//      builder.setNumber(notificationNum);
//
//      String identifierTitle = data.get("user")+type+data.get("promoId");
//
//      builder.setDeleteIntent(
//              PendingIntent.getBroadcast(this, notificationNum,
//                      new Intent(this,NotificationDeleteListener.class)
//                              .putExtra("notificationIdentifierTitle",identifierTitle)
//                              ,PendingIntent.FLAG_UPDATE_CURRENT));


      GlobalVariables.getMessagesNotificationMap().put(identifierTitle, notificationNum);

      updateNotificationSent(data.get("user"), data.get("promoId"), type);
      notificationManager.notify(notificationNum, builder.build());
    }


//    if(Build.VERSION.SDK_INT < 26){
//      Log.d("ttt","notificationNum: "+notificationNum);
//
//      BadgeUtil.incrementBadgeNum(this);
//    }else{
//
//    }

//    if(previousNotificationsBodies.contains(body)){
//      return;
//    }
//
//    previousNotificationsBodies.add(body);
//
//    int j = Integer.parseInt(data.get("user").replaceAll("[\\D]", "").substring(0, 4));
//    Intent newIntent = new Intent(this, MainActivity.class);
//    newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//    PendingIntent pendingIntent = PendingIntent.getActivity(this, j, newIntent, PendingIntent.FLAG_ONE_SHOT);
//    Notification notification = new NotificationCompat.Builder(this, "channel1")
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .setContentTitle(data.get("title"))
//            .setContentText(data.get("body"))
////            .setContentIntent(pendingIntent)
//            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
////            .setGroup(data.get("type"))
////            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
////            .setNumber(notificationNum)
////            .setAutoCancel(true)
//            .build();
//    if(type.equals("message")){
//      builder.setGroup("message")
//              .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
//              .setGroupSummary(true);
//    }
//    if(previousNotificationsTypes.contains(type)){
//      builder.setStyle(new NotificationCompat.InboxStyle()
//      .addLine("fsd")
//      .setBigContentTitle("")
//      .setSummaryText("+1"));
//    }
//    previousNotificationsBodies.add(body);
//    previousNotificationsTypes.add(type);
//    Log.d("ttt","type: "+data.get("type"));
//    notificationManager.notify(notificationNum,builder.build());
//    int i = 0;
//    if (j > 0) {
//      i = j;
//    }
//    if(previousNotifications.contains(builder.build())){
//      notificationManager.notify(previousNotifications.indexOf(builder.build())+1, builder.build());
//    }else{
//      notificationManager.notify(notificationNum, builder.build());
//    }

//    notificationManager.notify(1, builder.build());
//    notificationManager.notify(notificationNum,notification);
//    if(!messagesNotificationMap.containsKey(title)){
//      if(type.equals("message")) {
//        messagesNotificationMap.put(title,notificationNum);
//        notificationManager.notify(notificationNum,builder.build());
//        sharedPreferences.edit().putInt(title, notificationNum).apply();
//      }else{
//        notificationManager.notify(notificationNum,builder.build());
//      }
//    }else{
//      if(type.equals("message")) {
//        notificationManager.notify(messagesNotificationMap.get(title),builder.build());
//      }
//    }
//
////
//    if(type.equals("message")) {
////      if(messagesNotificationMap == null)
////        messagesNotificationMap = new HashMap<>();
//
//
//      if(!messagesNotificationMap.containsKey(title)){
//        messagesNotificationMap.put(title,notificationNum);
//        notificationManager.notify(notificationNum,builder.build());
//        sharedPreferences.edit().putInt(title, notificationNum).apply();
//      }else{
//        notificationManager.notify(messagesNotificationMap.get(title),builder.build());
//      }
//
//    }else{
//
//      messagesNotificationMap.put(title,notificationNum);
//      notificationManager.notify(notificationNum,builder.build());
////    previousNotifications.add(builder.build())
//    };
  }

  void updateNotificationSent(String user, String promoId, String type) {

    if (FirebaseAuth.getInstance().getCurrentUser() == null)
      return;


    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    Log.d("ttt", "current user id: " + currentUid + " user: " + user + " promoid: " + promoId + " type: " + type);

    FirebaseFirestore.getInstance().collection("notifications")
            .whereEqualTo("receiverId", currentUid).whereEqualTo("senderId", user)
            .whereEqualTo("promoId", Long.valueOf(promoId)).whereEqualTo("type", type).get()
            .addOnSuccessListener(snapshots -> {
              if (!snapshots.isEmpty()) {
                Log.d("ttt", "found this notificaiton and updating it to sent");
                snapshots.getDocuments().get(0).getReference().update("sent", true);
              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        Log.d("ttt", "failed because: " + e.getMessage());
      }
    });
  }

//  public static void setBadge(Context context, int count) {
//    String launcherClassName = getLauncherClassName(context);
//    if (launcherClassName == null) {
//      return;
//    }
//    Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
//    intent.putExtra("badge_count", count);
//    intent.putExtra("badge_count_package_name", context.getPackageName());
//    intent.putExtra("badge_count_class_name", launcherClassName);
//    context.sendBroadcast(intent);
//  }
//
//  public static String getLauncherClassName(Context context) {
//
//    PackageManager pm = context.getPackageManager();
//
//    Intent intent = new Intent(Intent.ACTION_MAIN);
//    intent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//    List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
//    for (ResolveInfo resolveInfo : resolveInfos) {
//      String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
//      if (pkgName.equalsIgnoreCase(context.getPackageName())) {
//        return resolveInfo.activityInfo.name;
//      }
//    }
//    return null;
//  }

  public void createChannel(String channelId) {
    if (Build.VERSION.SDK_INT >= 26) {
      if (notificationManager.getNotificationChannel(channelId) == null) {
        Log.d("ttt", "didn't find: " + channelId);
        Log.d("ttt", "creating notificaiton channel");
        NotificationChannel channel = new NotificationChannel(channelId, channelId + " channel", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.setDescription("notifications");
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);
      }
    }
  }


//   public static class NotificationClickReceiver extends BroadcastReceiver {
//
////  public boolean isRegistered;
////  private static NotificationClickReceiver receiver = null;
//
////  NotificationClickReceiver(){}
//
////  private static NotificationClickReceiver getInstance(){
////
////    if(receiver == null){
////      Log.d("NotificationReceiver","NotificationReceiver is null so creating");
////      receiver = new NotificationClickReceiver();
////    }else{
////      Log.d("NotificationReceiver","NotificationReceiver is null so creating");
////    }
////
////    return(receiver);
////  }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//
//      context.unregisterReceiver(MyFirebaseMessaging.notificationClickReceiver);
//      MyFirebaseMessaging.notificationClickReceiver = null;
//      Log.d("ttt","unregistering NotificationClickReceiver receiver");
//
//      if(GlobalVariables.isAppIsRunning()){
//
//        context.startActivity(new Intent(context, MessagingActivity.class)
//                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                .putExtra("messagingBundle",intent.getBundleExtra("messagingBundle")));
//
//        Log.d("ttt","clicked notificaiton while app is running");
//      }else{
//
//        context.startActivity(new Intent(context, MainActivity.class)
//                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                .putExtra("messagingBundle",intent.getBundleExtra("messagingBundle")));
//
//        Log.d("ttt","clicked notificaiton while app isn't running");
//      }
//
//      Log.d("ttt","recieved message intent with extra: "+
//              intent.hasExtra("messagingBundle"));
//
//    }
//
//    public void registerReceiver(Context context){
////    if(isRegistered){
////      Log.d("ttt", "alreadt registered notification click listerner broadcayst");
////      return;
////    }
//      try {
//        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.example.yousef.rbenoapplication.notificationClick");
//        context.registerReceiver(this, intentFilter);
//      } catch (Exception e) {
//        Log.d("ttt", "registering notification click listerner broadcayst error!!: "
//                + e.getMessage());
//      } finally {
//        Log.d("ttt", "resgitered good notifcaiton click");
//      }
//    }
//
//  }

}
