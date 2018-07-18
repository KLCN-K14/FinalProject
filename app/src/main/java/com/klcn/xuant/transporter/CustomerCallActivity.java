package com.klcn.xuant.transporter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
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
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.helper.ArcProgressAnimation;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.FCMResponse;
import com.klcn.xuant.transporter.model.Notification;
import com.klcn.xuant.transporter.model.PickupRequest;
import com.klcn.xuant.transporter.model.Sender;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.remote.IFCMService;
import com.klcn.xuant.transporter.remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CustomerCallActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.txt_your_destination)
    TextView mTxtYourDestination;

//    @BindView(R.id.txt_price)
//    TextView mTxtPrice;
//
    @BindView(R.id.txt_distance)
    TextView mTxtDistance;

    @BindView(R.id.txt_time)
    TextView mTxtTime;

    @BindView(R.id.btn_accept_pickup_request)
    Button mBtnAccept;

    @BindView(R.id.btn_cancel_find_driver)
    Button mBtnCancel;

    @BindView(R.id.arc_progress)
    ArcProgress mArcProgress;

    @BindView(R.id.lnl_panel_distance)
    RelativeLayout mPanelDistance;

    IGoogleAPI mService;

    MediaPlayer mediaPlayer;
    double lat,lng;
    String destination;
    String customerId;
    Driver mDriver;

    IFCMService mFCMService;
    private BroadcastReceiver mReceiver;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_customer_call);
        ButterKnife.bind(this);
        mFCMService = Common.getFCMService();

        mPanelDistance.setVisibility(View.GONE);

        mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.notification);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        getInfoDriver();
        removeDriverAvailable();
        mService = Common.getGoogleAPI();
        if(getIntent()!=null){
            lat = Double.parseDouble(getIntent().getStringExtra("lat").toString());
            lng = Double.parseDouble(getIntent().getStringExtra("lng").toString());
            mTxtYourDestination.setText(getNameAdress(lat,lng).toUpperCase());
            destination = getIntent().getStringExtra("destination");
            customerId = getIntent().getStringExtra("customerId");
            getDirection(lat,lng,destination);
        }

        mBtnAccept.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);


        mArcProgress.setMax(15);
        ArcProgressAnimation anim = new ArcProgressAnimation(mArcProgress, 0, 15);
        anim.setDuration(15000);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                try {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer=null;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                addDriverAvailable();
                sendMessageCancelRequest();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },1000);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mArcProgress.startAnimation(anim);

    }

    private void getInfoDriver() {
        FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            mDriver = dataSnapshot.getValue(Driver.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void removeDriverAvailable() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.driver_available_tbl);

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
            }
        });
    }

    public void addDriverAvailable() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.driver_available_tbl);

        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userId,
                new GeoLocation( Common.mLastLocationDriver.getLatitude(),  Common.mLastLocationDriver.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Add marker
                    }
                });
    }

    private void getDirection(double lat, double lng, String destination) {
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ lat+","+lng+"&"+
                    "destination="+destination+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("TRANSPORT",requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");

                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");

                                JSONObject legsObject = legs.getJSONObject(0);

                                JSONObject distanceJS = legsObject.getJSONObject("distance");

                                Double distance = distanceJS.getDouble("value");


                                JSONObject timeJS = legsObject.getJSONObject("duration");

                                Double time = timeJS.getDouble("value");
                                if(distance>15000){
                                    mPanelDistance.setVisibility(View.VISIBLE);
                                    mTxtDistance.setText(distanceJS.getString("text"));
                                    calculateFare(distance,time);
                                }

                                String address = legsObject.getString("start_address");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_cancel_find_driver:
                sendMessageCancelRequest();
                addDriverAvailable();
                finish();
            break;

            case R.id.btn_accept_pickup_request:
                sendMessageAccept();

                DatabaseReference pickupRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
                PickupRequest pickupRequest1 = new PickupRequest();
                pickupRequest1.setLatPickup(String.valueOf(lat));
                pickupRequest1.setLngPickup(String.valueOf(lng));
                pickupRequest1.setDestination(destination);
                pickupRequest1.setCustomerId(customerId);
                pickupRequest.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(pickupRequest1)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull final Exception e) {
                                e.printStackTrace();
                                // send cancel to customer
                            }
                        });

                break;
        }
    }

    private void sendMessageAccept() {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(customerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);
                            Notification notification = new Notification("DriverAccept","Your driver accept your request!");
                            Sender sender = new Sender(token.getToken(),notification);
                            mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1){
                                        Log.e("MessageCancelRequest","Success");
                                    }
                                    else{
                                        Log.e("MessageCancelRequest",response.message());
                                        Log.e("MessageCancelRequest",response.errorBody().toString());
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

    private void sendMessageCancelRequest() {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(customerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);
                            Notification notification = new Notification("Cancel","Your driver denied your request!");
                            Sender sender = new Sender(token.getToken(),notification);
                            mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1){
                                        Log.e("MessageCancelRequest","Success");
                                    }
                                    else{
                                        Log.e("MessageCancelRequest",response.message());
                                        Log.e("MessageCancelRequest",response.errorBody().toString());
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
    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                if(intent.getStringExtra("CancelTrip")!=null){
                    mBtnAccept.setEnabled(false);
                    mBtnCancel.setEnabled(false);
                    Toast.makeText(getApplicationContext(),"Customer cancel request!!!",Toast.LENGTH_LONG).show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },2000);
                }
            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onStop() {
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        this.unregisterReceiver(mReceiver);
        super.onStop();
    }

    @Override
    protected void onPause() {
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private String getNameAdress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat,lng, 1);
            if(!addresses.isEmpty()){
                Address obj = addresses.get(0);
                String namePlacePickup = "";

                if (obj.getSubThoroughfare() != null && obj.getThoroughfare() != null) {
                    namePlacePickup = namePlacePickup + obj.getSubThoroughfare() + " "+ obj.getThoroughfare() + ", ";
                }

                if(obj.getLocality()!=null){
                    namePlacePickup = namePlacePickup + obj.getLocality() + ", ";
                }

                namePlacePickup = namePlacePickup + obj.getSubAdminArea()+", "+obj.getAdminArea();


                return namePlacePickup;

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    private void calculateFare(Double distance, Double time) {
        Double realDistance = (distance/1000);
        Double realTime = time/60;
        Double hour = realTime%60;
        Double minute = realTime - hour*60;
        String timeArrived;
        if(realTime<60.){
            timeArrived = realTime.intValue() + " min";
        }else{
            timeArrived = hour.intValue() + " h "+minute.intValue()+" min";
        }
        mTxtTime.setText(timeArrived);

//        Double fareStandard = Common.base_fare + Common.cost_per_km*realDistance + Common.cost_per_minute_standard*realTime;
//        Double farePremium = Common.base_fare + Common.cost_per_km*realDistance + Common.cost_per_minute_premium*realTime;
//        String textFareStandard = "VND "+Integer.toString(fareStandard.intValue())+"K";
//        String textFarePremium = "VND "+Integer.toString(farePremium.intValue())+"K";
//        if(mDriver!=null){
//            if(mDriver.getServiceVehicle().equals(Common.service_vehicle_standard)){
//                mTxtPrice.setText(textFareStandard);
//            }else{
//                mTxtPrice.setText(textFarePremium);
//            }
//        }
    }
}
