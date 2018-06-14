package com.klcn.xuant.transporter.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

//        LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);

        if(remoteMessage.getNotification().getTitle().equals("Arrived")){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMessaging.this,"Your driver near here!",Toast.LENGTH_LONG).show();
                }
            });
            showNotificationArrived(remoteMessage.getNotification().getBody());
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
        }
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
