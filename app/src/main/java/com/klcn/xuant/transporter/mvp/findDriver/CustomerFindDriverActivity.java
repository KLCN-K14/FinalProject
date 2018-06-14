package com.klcn.xuant.transporter.mvp.findDriver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.nfc.cardemulation.HostNfcFService;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.klcn.xuant.transporter.DriverMainActivity;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.helper.ArcProgressAnimation;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.FCMResponse;
import com.klcn.xuant.transporter.model.Notification;
import com.klcn.xuant.transporter.model.Sender;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.mvp.home.CustomerHomeActivity;
import com.klcn.xuant.transporter.mvp.splashScreen.SplashScreenActivity;
import com.klcn.xuant.transporter.remote.IFCMService;
import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CustomerFindDriverActivity extends AppCompatActivity implements View.OnClickListener{


    @BindView(R.id.ripple_background)
    RippleBackground mRippleBackground;

    @BindView(R.id.txt_your_destination)
    TextView mTxtDestination;

    @BindView(R.id.txt_your_position)
    TextView mTxtYourPlace;

    @BindView(R.id.txt_price)
    TextView mTxtPrice;

    @BindView(R.id.txt_name_driver_found)
    TextView mTxtNameDriverFound;

    @BindView(R.id.rlt_layout_cancel)
    RelativeLayout mRltCancel;

    @BindView(R.id.arc_progress)
    ArcProgress mArcProgress;

    Double  lat, lng;
    int radius = 1;
    boolean isDriverFound = false;
    public static int LIMIT_RANGE = 5 ; // 5km
    IFCMService mService;
    String driverID = "";
    String destination = "";
    BroadcastReceiver mReceiver;
    Driver mDriver;
    HashMap<String,String> hashDriverFound = new HashMap<>();
    GeoQuery geoQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);
        ButterKnife.bind(this);
        mRippleBackground.startRippleAnimation();

        mService = Common.getFCMService();
        destination = getIntent().getStringExtra("destination");
        mTxtDestination.setText(destination);

        getNameAdress(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude());
        findDriver(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude());

        mArcProgress.setMax(5);
        ArcProgressAnimation anim = new ArcProgressAnimation(mArcProgress, 5, 0);
        anim.setDuration(5000);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRltCancel.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mArcProgress.startAnimation(anim);

        mRltCancel.setOnClickListener(this);
    }

    private void getNameAdress(Double lat, Double lng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String namePlacePickup = obj.getSubThoroughfare()+", "+obj.getLocality()+", "+obj.getSubAdminArea();
            mTxtYourPlace.setText(namePlacePickup);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void findDriver(final double lat,final double lng) {
        DatabaseReference driverAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_available_tbl);
        GeoFire gfDrivers = new GeoFire(driverAvailable);

        geoQuery = gfDrivers.queryAtLocation(new GeoLocation(lat,lng), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                if(!isDriverFound && hashDriverFound.get(key)==null){
                    Toast.makeText(getApplicationContext(),"Start send request to dirver",Toast.LENGTH_SHORT).show();
                    isDriverFound = true;
                    sendRequestToDriver(key);
                    DatabaseReference mDriversDatabase = FirebaseDatabase.getInstance().getReference(Common.drivers_tbl);
                    mDriversDatabase.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mDriver = dataSnapshot.getValue(Driver.class);
                            mTxtNameDriverFound.setText("Driver "+mDriver.getName()+" is responding. . .");
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w("ERROR", "Failed to read value.", error.toException());
                        }
                    });
                    driverID = key;
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                Log.e("RADIUS", String.valueOf(radius));
                if(!isDriverFound && radius<=LIMIT_RANGE){
                    radius++;
                    findDriver(lat,lng);
                }else{
                    final Intent resultIntent = new Intent();


                    // finish when don't have any driver available
                    if(driverID.equals("")){
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setResult(RESULT_CANCELED, resultIntent);
                                finish();
                            }
                        },2000);
                    }else{
                        // auto finish when driver not response after 15s
                        final Handler handlerWaitRequest = new Handler();
                        handlerWaitRequest.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                            setResult(RESULT_CANCELED, resultIntent);
                            finish();
                            }
                        },18000);
                        // if driver response
                        FirebaseDatabase.getInstance().getReference(Common.driver_working_tbl)
                                .addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                        if(dataSnapshot.getKey().equals(driverID)){
                                            setResult(RESULT_OK, resultIntent);
                                            resultIntent.putExtra("driverID", driverID);
                                            Log.e("DRIVER",driverID);
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                    }

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendRequestToDriver(String key) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);

//                            String json_lat_lng = new Gson().toJson(new LatLng(lat,lng));
                            String data = "";
                            if(destination!=null){
                                data = Common.mLastLocationCustomer.getLatitude()+Common.keySplit+
                                        Common.mLastLocationCustomer.getLongitude()+Common.keySplit+destination+Common.keySplit+FirebaseAuth.getInstance().getCurrentUser().getUid();
                            }

                            Notification notification = new Notification("Request",data);
                            Sender content = new Sender(token.getToken(),notification);
                            mService.sendMessage(content)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if(response.body().success == 1)
                                                Toast.makeText(getApplicationContext(),"Request sent",Toast.LENGTH_LONG).show();
                                            else
                                                Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        geoQuery.removeAllListeners();
        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rlt_layout_cancel:
                // send cancel to driver
                if(!driverID.equals("")){
                    sendMessageCancel();
                }
                geoQuery.removeAllListeners();
                finish();
                break;
        }
    }

    private void sendMessageCancel() {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);
                            Notification notification = new Notification("CancelTrip","You cancel request!");
                            Sender sender = new Sender(token.getToken(),notification);
                            mService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1){
                                        Log.e("MessageCancel","Sucess");
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
                                        Log.e("MessageCancel",response.message());
                                        Log.e("MessageCancel",response.errorBody().toString());
                                    }
                                }
                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        geoQuery.removeAllListeners();
        finish();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        geoQuery.removeAllListeners();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getStringExtra("Cancel")!=null){
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mTxtNameDriverFound.setText("Driver "+mDriver.getName()+" denied your request");
                        }
                    },1000);
                    mTxtNameDriverFound.setText("Finding another dirver. . .");
                    isDriverFound = false;
                    hashDriverFound.put(driverID,driverID);
                    driverID = "";
                    mDriver = null;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            findDriver(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude());
                        }
                    },2000);
                }

            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);
    }

}
