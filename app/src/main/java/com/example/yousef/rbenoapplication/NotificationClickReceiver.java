package com.example.yousef.rbenoapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class NotificationClickReceiver extends BroadcastReceiver {


    private SharedPreferences sharedPreferences;
//  public static boolean isRegistered = false;
//  private static NotificationClickReceiver receiver = null;

//  NotificationClickReceiver(){}

//  private static NotificationClickReceiver getInstance(){
//
//    if(receiver == null){
//      Log.d("NotificationReceiver","NotificationReceiver is null so creating");
//      receiver = new NotificationClickReceiver();
//    }else{
//      Log.d("NotificationReceiver","NotificationReceiver is null so creating");
//    }
//
//    return(receiver);
//  }

    @Override
    public void onReceive(Context context, Intent intent) {

//    try {
//     LocalBroadcastManager.getInstance(context)
//                    .unregisterReceiver(this);
////      context.unregisterReceiver(this);
//    }catch (Exception e){
//      Log.d("ttt", "UNregistering notification broadcayst error!!: "
//              + e.getMessage());
//    }finally {
//      Log.d("ttt","unregistered good notifacaiton");
//      isRegistered = false;
//    }

        if (intent.hasExtra("messagingBundle")) {

            if (GlobalVariables.isAppIsRunning()) {

                if (sharedPreferences == null) {
                    sharedPreferences = context.getSharedPreferences("rbeno", Context.MODE_PRIVATE);
                }

                Intent intent1 = new Intent(context, MessagingRealTimeActivity.class)
                        .putExtra("messagingBundle",
                                intent.getBundleExtra("messagingBundle"));

                if (sharedPreferences.contains("currentMessagingUserId")) {
//          intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    final Bundle messagingBundle = intent.getBundleExtra("messagingBundle");
                    if (messagingBundle.getString("promouserid")
                            .equals(sharedPreferences.getString("currentMessagingUserId", "")) &&
                            messagingBundle.getLong("intendedpromoid") ==
                                    sharedPreferences.getLong("currentMessagingPromoId", 0)) {
                        Log.d("ttt", "this messaging activity is already open man");
//          intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//          Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
//          intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//          intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//          intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    } else {
                        Log.d("ttt", "current messaging is not this");
                        intent1.setFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                } else {
                    Log.d("ttt", "no current messaging in shared");
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                context.startActivity(intent1);
                Log.d("ttt", "clicked notificaiton while app is running");
            } else {

                context.startActivity(new Intent(context, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("messagingBundle",
                                intent.getBundleExtra("messagingBundle")));

                Log.d("ttt", "clicked notificaiton while app isn't running");
            }


        }

    }

//  public static void registerReceiver(Context context){
//    if(isRegistered){
//      Log.d("ttt", "already registered notification broadcayst");
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

//
//
//  }
//
//  public static void unregisterReceiver(Context context) {
//    if(receiver!=null){
//      Log.d("ttt","registered click notificaiton receiver");
//      context.unregisterReceiver(getInstance());
//    }
//  }
//  public void unregisterReceiver(Context context){
//
//    try{
//      LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
//        }catch (Exception e){
//      Log.d("ttt","unregister notification click listerner broadcayst error!!: "
//      +e.getMessage());
//    }finally {
//      Log.d("ttt","unregister notification click listerner broadcayst");
//    }
//
//  }
}
