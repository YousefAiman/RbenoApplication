package com.example.yousef.rbenoapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationDeleteListener extends BroadcastReceiver {
    //  private static NotificationManager notificationManager;
//  public static boolean isRegistered = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("ttt", "notification dismissedd man!!");
//
//    try {
//      LocalBroadcastManager.getInstance(context)
//              .unregisterReceiver(this);
////      context.unregisterReceiver(this);
//    }catch (Exception e){
//      Log.d("ttt", "UNregistering NotificationDeleteListenererror!!: "
//              + e.getMessage());
//    }finally {
//      Log.d("ttt","unregistered good NotificationDeleteListener");
//      isRegistered = false;
//    }


        if (intent.hasExtra("notificationIdentifierTitle") &&
                GlobalVariables.getMessagesNotificationMap() != null)
            GlobalVariables.getMessagesNotificationMap().remove(
                    intent.getStringExtra("notificationIdentifierTitle"));

//    if(intent.hasExtra("promouserid")){
//
//      final String notificationIdentifier = "رسالة جديد من: " +
//              intent.getStringExtra("promouserid") + " بخصوص اعلان #" +
//              intent.getStringExtra("intendedpromoid");
//
//      GlobalVariables.getMessagesNotificationMap().remove(notificationIdentifier);
//
////      BadgeUtil.decrementBadgeNum(context,intent);
//    }else{
////      BadgeUtil.decrementBadgeNum(context);
//    }

//
//    if(notificationManager==null)
//      notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//    notificationManager.cancel(sharedPreferences.getInt(notificationIdentifier, 0));

    }

//  public static void registerReceiver(Context context){
//    if(isRegistered){
//      Log.d("ttt", "already registered NotificationDeleteListener");
//      return;
//    }
//
//    try {
//      final IntentFilter intentFilter = new IntentFilter();
//      intentFilter.addAction("com.example.yousef.rbenoapplication.notificationClick");
//      context.registerReceiver(new NotificationClickReceiver(), intentFilter);
//    } catch (Exception e) {
//      Log.d("ttt", "registering notification click listerner broadcayst error!!: "
//              + e.getMessage());
//    } finally {
//      isRegistered = true;
//      Log.d("ttt", "resgitered good notifcaiton click");
//    }
//  }
}