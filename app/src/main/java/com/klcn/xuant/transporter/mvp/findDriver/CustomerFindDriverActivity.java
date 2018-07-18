package com.klcn.xuant.transporter.mvp.findDriver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.CustomerTrackingActivity;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.helper.ArcProgressAnimation;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.FCMResponse;
import com.klcn.xuant.transporter.model.Notification;
import com.klcn.xuant.transporter.model.Sender;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.mvp.home.CustomerHomeActivity;
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

    @BindView(R.id.img_more)
    ImageView mImgMore;

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
    String pickup = "";
    String price = "";
    Double fixedFare = 0.;
    String currentService = "";
    BroadcastReceiver mReceiver;
    Driver mDriver;
    HashMap<String,String> hashDriverFound = new HashMap<>();
    GeoQuery geoQuery;
    ArcProgressAnimation anim;
    Boolean isShowDialog = false;
    Boolean isCustomerResponse = false;
    AlertDialog dialog;
    Handler handler = new Handler();

    boolean isPickupLong = false;

    DatabaseReference mDriversDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);
        ButterKnife.bind(this);
        mRippleBackground.startRippleAnimation();

        mService = Common.getFCMService();
        pickup = getIntent().getStringExtra("pickup");
        price = getIntent().getStringExtra("price");
        fixedFare = Double.parseDouble(getIntent().getStringExtra("fixedFare"));
        Log.e("pickup",pickup);

        destination = getIntent().getStringExtra("destination");
        Log.e("destination",destination);

        currentService = getIntent().getStringExtra("currentService");

        mImgMore.setVisibility(View.GONE);


        mTxtDestination.setText(destination);
        mTxtPrice.setText(price);
        mTxtYourPlace.setText(pickup);

        getNameAdress(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude());
        findDriver(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude());

        mArcProgress.setMax(5);
        anim = new ArcProgressAnimation(mArcProgress, 5, 0);
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

                mTxtYourPlace.setText(namePlacePickup);

            }

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
                    mDriversDatabase = FirebaseDatabase.getInstance().getReference(Common.drivers_tbl);
                    mDriversDatabase.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                boolean isOnline = (Boolean)dataSnapshot.child("isOnline").getValue();
                                mDriver = dataSnapshot.getValue(Driver.class);
                                if(isOnline && mDriver.getServiceVehicle().equals(currentService) &&
                                        fixedFare*Common.transport_fee<Double.parseDouble(mDriver.getCredits())){
                                    if(!isShowDialog){
                                        isShowDialog = true;
                                        showFoundDriverDialog();
                                        driverID = key;
                                        mDriversDatabase.child(key).removeEventListener(this);
                                    }
                                }
                            }

                        }
                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.e("ERROR", "Failed to read value.", error.toException());
                        }
                    });
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
                }else {
                    Log.e("onGeoQueryReady",driverID);
                    geoQuery.removeAllListeners();
                    final Intent resultIntent = new Intent();
                    // finish when don't have any driver available
                    if (driverID.equals("")) {
                        Log.e("onGeoQueryReady","No found driver");
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setResult(RESULT_FIRST_USER, resultIntent);
                                Log.e("FINISH","FINISH FROM NO FOUND DRIVER");
                                finish();
                            }
                        }, 5000);
                    }else{
                        Log.e("onGeoQueryReady","found driver");
                    }
                }
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    public   Runnable customerNoResponse = new Runnable() {
        @Override
        public void run() {
            if(!isCustomerResponse){
                dialog.dismiss();
                mTxtNameDriverFound.setText("No response. Stop find driver");
                Handler newHandler = new Handler();
                newHandler.postDelayed(()->
                                finish()
                        ,1000);}
            }
    };


    private void showFoundDriverDialog() {
        isCustomerResponse = false;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        LayoutInflater inflater = LayoutInflater.from(this);
        View foundDriverLayout = inflater.inflate(R.layout.layout_found_driver, null);

        builder.setView(foundDriverLayout);
        dialog = builder.create();

        final TextView txtNameDriver = foundDriverLayout.findViewById(R.id.txt_name_driver_dialog);
        final TextView txtNameCar = foundDriverLayout.findViewById(R.id.txt_name_car_dialog);
        final TextView txtLicensePlate = foundDriverLayout.findViewById(R.id.txt_license_plate_dialog);
        final RatingBar ratingBar = foundDriverLayout.findViewById(R.id.rating_bar);
        final Button btnOtherDriver = foundDriverLayout.findViewById(R.id.btn_other_driver);
        final Button btnSendRequest = foundDriverLayout.findViewById(R.id.btn_send_request);

        btnOtherDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hashDriverFound.put(driverID,driverID);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isCustomerResponse = true;
                        mTxtNameDriverFound.setText("Finding another dirver. . .");
                        isDriverFound = false;
                        driverID = "";
                        mDriver = null;
                        isShowDialog = false;
                        mRltCancel.setVisibility(View.VISIBLE);
                        mArcProgress.startAnimation(anim);
                    }
                },1000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findDriver(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude());
                    }
                },1000);
                dialog.dismiss();
            }
        });

        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isCustomerResponse = true;

                mTxtNameDriverFound.setText("Driver "+mDriver.getName()+" is responding. . .");
                isDriverFound = true;
                Log.e("ERROR", "Start send request to dirver");
                sendRequestToDriver(driverID);
                dialog.dismiss();
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                isShowDialog = true;
                mRltCancel.setVisibility(View.GONE);
                handler.postDelayed(customerNoResponse,10000);
            }
        });

        txtNameDriver.setText(mDriver.getName());
        txtNameCar.setText(mDriver.getNameVehicle());
        txtLicensePlate.setText(mDriver.getLicensePlate());
        ratingBar.setRating(Float.valueOf(mDriver.getAvgRatings().replace(",",".")));

        try{
            dialog.show();
        }catch (Exception e){
            Log.e("FindDriver",e.getMessage());
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
                                                Log.e("SendMessage","Request sent");
                                            else
                                                Log.e("SendMessage","Request fail"+response.message());
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
        final Intent intent = new Intent();
        final Handler handlerWaitRequest = new Handler();
//        handlerWaitRequest.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setResult(RESULT_CANCELED, intent);
//                finish();
//            }
//        },30000);
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
                    mTxtNameDriverFound.setText("Driver denied your request");
                    hashDriverFound.put(driverID,driverID);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mTxtNameDriverFound.setText("Finding another dirver. . .");
                            isDriverFound = false;
                            driverID = "";
                            mDriver = null;
                            isShowDialog = false;
                            mRltCancel.setVisibility(View.VISIBLE);
                            mArcProgress.startAnimation(anim);
                        }
                    },1000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(Common.mLastLocationCustomer!=null){
                                findDriver(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude());
                            }else{
                                Log.e("FINISH","DRIVER CANCEL REQUEST");

                                finish();
                            }
                        }
                    },2000);
                }else if(intent.getStringExtra("DriverAccept")!=null){
                    Intent resultIntent = new Intent();
                    CustomerFindDriverActivity.this.setResult(RESULT_OK, resultIntent);
                    resultIntent.putExtra("driverID", driverID);
                    Log.e("FINISH","DRIVER ACCEPT REQUEST");

                    finish();
                }

            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);
    }

}
