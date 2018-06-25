package com.klcn.xuant.transporter.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class OnClearFromRecentService extends Service {

    String test = "";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ClearFromRecentService", "Service Started");

        //get data
        test = intent.getStringExtra("Test").toString();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ClearFromRecentService", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
         Log.e("ClearFromRecentService", test);
        //Code here
//        HashMap<String,Object> maps = new HashMap<>();
//        sendMessageCancelTripToCustomer();
//        maps.put("reasonCancel","Driver destroy app");
//        maps.put("status",Common.trip_info_status_driver_cancel);
//        mTripInfoDatabase.updateChildren(maps);
//        mPickupRequestDatabase.child(driverID).removeValue();

        stopSelf();
    }
}
