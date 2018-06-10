package com.klcn.xuant.transporter.mvp.findDriver;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
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

    @BindView(R.id.btn_cancel_find_driver)
    Button mBtnCancel;

    Double  lat, lng;
    int radius = 1;
    boolean isDriverFound = false;
    public static int LIMIT_RANGE = 5 ; // 5km
    IFCMService mService;
    String driverID = "";
    String destination = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);
        ButterKnife.bind(this);
        mRippleBackground.startRippleAnimation();

        mService = Common.getFCMService();
        destination = getIntent().getStringExtra("destination");

        getNameAdress(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude());
        findDriver(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude());

        mBtnCancel.setOnClickListener(this);
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

        GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(lat,lng), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                if(!isDriverFound){
                    Toast.makeText(getApplicationContext(),"Start send request to dirver",Toast.LENGTH_SHORT).show();
                    isDriverFound = true;
                    sendRequestToDriver(key);
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
                        setResult(RESULT_CANCELED, resultIntent);
                        finish();
                    }else{
                        // auto finish when driver not response after 15s
                        final Handler handlerWaitRequest = new Handler();
                        handlerWaitRequest.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setResult(RESULT_CANCELED, resultIntent);
                                finish();
                            }
                        },20000);
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
                                data = Common.mLastLocationCustomer.getLatitude()+Common.keySplit+Common.mLastLocationCustomer.getLongitude()+
                                        Common.keySplit+destination+Common.keySplit+FirebaseAuth.getInstance().getCurrentUser().getUid();
                                String[] list = data.split(Common.keySplit);
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
        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_cancel_find_driver:
                cancelPickupRequest();
                break;
        }
    }

    private void cancelPickupRequest() {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        finish();
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
        cancelPickupRequest();
        finish();
    }


}
