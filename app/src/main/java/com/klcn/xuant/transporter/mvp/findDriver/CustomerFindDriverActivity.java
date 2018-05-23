package com.klcn.xuant.transporter.mvp.findDriver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.Service.MyFirebaseMessaging;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.FCMResponse;
import com.klcn.xuant.transporter.model.Notification;
import com.klcn.xuant.transporter.model.Sender;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.remote.IFCMService;
import com.skyfishjy.library.RippleBackground;

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

    Double  lat, lng;
    int radius = 1;
    boolean isDriverFound = false;
    public static int LIMIT_RANGE = 5 ; // 5km
    IFCMService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);
        ButterKnife.bind(this);
        mRippleBackground.startRippleAnimation();

        mService = Common.getFCMService();

        if(getIntent()!=null){
            lat = getIntent().getDoubleExtra("lat",-1.0);
            lng = getIntent().getDoubleExtra("lng",-1.0);
            mTxtDestination.setText(getIntent().getStringExtra("destination"));
           // findDriver(lat,lng);
        }
    }

    private void findDriver(final double lat,final double lng) {
        if(getIntent().getStringExtra("lat")!=null){
            DatabaseReference driverAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_available_tbl);
            GeoFire gfDrivers = new GeoFire(driverAvailable);

            GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(lat,lng), radius);
            geoQuery.removeAllListeners();
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if(!isDriverFound){
                        isDriverFound = true;
                        sendRequestToDriver(key);
                        Intent resultIntent = new Intent();
//                        resultIntent.putExtra("driverID", key);
//                        setResult(RESULT_OK, resultIntent);
//                        finish();
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
                    if(!isDriverFound && radius<=LIMIT_RANGE){
                        radius++;
                        findDriver(lat,lng);
                    }else{
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("driverID", "0");
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }
    }

    private void sendRequestToDriver(String key) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);

                            String json_lat_lng = new Gson().toJson(new LatLng(lat,lng));
                            Notification notification = new Notification("TRANSPORT",json_lat_lng);
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
            case R.id.btn_sign_in:
                break;
            case R.id.btn_register:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
