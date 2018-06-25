package com.klcn.xuant.transporter.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.klcn.xuant.transporter.CustomerCallActivity;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.helper.NotificationHelper;

import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    NotificationHelper mNotificationHelper;
    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

//        LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);
        mNotificationHelper = new NotificationHelper(this);

        if(remoteMessage.getNotification().getTitle().equals("Arrived")){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMessaging.this,"Your driver near here!",Toast.LENGTH_LONG).show();
                }
            });
            NotificationCompat.Builder builder = mNotificationHelper.getNotificationChannel("Arrived","Your driver near here!");
            mNotificationHelper.getManager().notify(new Random().nextInt(),builder.build());
//            showNotificationArrived(remoteMessage.getNotification().getBody());
        }else if(remoteMessage.getNotification().getTitle().equals("Request")){
            String[] list = remoteMessage.getNotification().getBody().split(Common.keySplit);
            Intent intent = new Intent(getApplicationContext(), CustomerCallActivity.class);
            intent.putExtra("lat",list[0]);
            intent.putExtra("lng",list[1]);
            intent.putExtra("destination",list[2]);
            intent.putExtra("customerId",list[3]);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if(remoteMessage.getNotification().getTitle().equals("Pickup")){

            Intent i = new Intent("android.intent.action.MAIN").putExtra("Pickup", remoteMessage.getNotification().getBody().toString());
            this.sendBroadcast(i);
        }else if(remoteMessage.getNotification().getTitle().equals("Cancel")){
            Intent i = new Intent("android.intent.action.MAIN").putExtra("Cancel", remoteMessage.getNotification().getBody().toString());
            this.sendBroadcast(i);
        }else if(remoteMessage.getNotification().getTitle().equals("DropOff")){
            Intent i = new Intent("android.intent.action.MAIN").putExtra("DropOff", remoteMessage.getNotification().getBody().toString());
            this.sendBroadcast(i);
        }else if(remoteMessage.getNotification().getTitle().equals("CancelTrip")){
            Intent i = new Intent("android.intent.action.MAIN").putExtra("CancelTrip", remoteMessage.getNotification().getBody().toString());
            this.sendBroadcast(i);
        }else if(remoteMessage.getNotification().getTitle().equals("KeyTrip")){
            Intent i = new Intent("android.intent.action.MAIN").putExtra("KeyTrip", remoteMessage.getNotification().getBody().toString());
            this.sendBroadcast(i);
        }else if(remoteMessage.getNotification().getTitle().equals("CustomerCancelTrip")){
            NotificationCompat.Builder builder = mNotificationHelper.getNotificationChannel("Trip Cancel","Customer cancel the trip!");
            mNotificationHelper.getManager().notify(new Random().nextInt(),builder.build());
//            showNotification(remoteMessage.getNotification().getBody());
            Intent i = new Intent("android.intent.action.MAIN").putExtra("CustomerCancelTrip", remoteMessage.getNotification().getBody().toString());
            this.sendBroadcast(i);
        }else if(remoteMessage.getNotification().getTitle().equals("DriverCancelTrip")){
            NotificationCompat.Builder builder = mNotificationHelper.getNotificationChannel("Trip Cancel","Your driver cancel the trip!");
            mNotificationHelper.getManager().notify(new Random().nextInt(),builder.build());
//            showNotification(remoteMessage.getNotification().getBody());
            Intent i = new Intent("android.intent.action.MAIN").putExtra("DriverCancelTrip", remoteMessage.getNotification().getBody().toString());
            this.sendBroadcast(i);
        }else if(remoteMessage.getNotification().getTitle().equals("Chat")){
            // 0 message 1 sender
            String[] list = remoteMessage.getNotification().getBody().split(Common.keySplit);
            String title = "";
            if(list[1].equals(Common.drivers_tbl))
                title = "Chat from driver";
            else
                title = "Chat from customer";
            NotificationCompat.Builder builder = mNotificationHelper.getNotificationChannel(title,list[0]);
            mNotificationHelper.getManager().notify(new Random().nextInt(),builder.build());

            Intent i = new Intent("android.intent.action.MAIN").putExtra("Chat", remoteMessage.getNotification().getBody().toString());
            this.sendBroadcast(i);
        }else if(remoteMessage.getNotification().getTitle().equals("InviteSucess")){
            NotificationCompat.Builder builder = mNotificationHelper.getNotificationChannel("Invite Sucess",
                    "You have invited successfully driver with email "+remoteMessage.getNotification().getBody());
            mNotificationHelper.getManager().notify(new Random().nextInt(),builder.build());
        }else if(remoteMessage.getNotification().getTitle().equals("RemindFirstTrip")){
            NotificationCompat.Builder builder = mNotificationHelper.getNotificationChannel("Remind Transport",
                    "Complete first trip to receive promo");
            mNotificationHelper.getManager().notify(new Random().nextInt(),builder.build());
        }else if(remoteMessage.getNotification().getTitle().equals("DriverAccept")){
            Intent i = new Intent("android.intent.action.MAIN").putExtra("DriverAccept", remoteMessage.getNotification().getBody().toString());
            this.sendBroadcast(i);
        }

    }

    private void showNotification(String body) {
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Cancel")
                .setContentText(body)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,builder.build());
    }

    private void showNotificationArrived(String body) {
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,builder.build());

    }


}
