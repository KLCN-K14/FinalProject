package com.klcn.xuant.transporter;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.helper.CustomInfoWindow;
import com.klcn.xuant.transporter.helper.DirectionsJSONParser;
import com.klcn.xuant.transporter.model.Customer;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.FCMResponse;
import com.klcn.xuant.transporter.model.Notification;
import com.klcn.xuant.transporter.model.PickupRequest;
import com.klcn.xuant.transporter.model.TripInfo;
import com.klcn.xuant.transporter.model.Sender;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.receiver.NetworkStateReceiver;
import com.klcn.xuant.transporter.remote.IFCMService;
import com.klcn.xuant.transporter.remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DriverTrackingAcitivity extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,NetworkStateReceiver.NetworkStateReceiverListener {

    private NetworkStateReceiver networkStateReceiver;

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private BroadcastReceiver mReceiver;

    Boolean isPickup = true;
    Boolean isSendNotification = false;
    String ressonCancelTrip = "";
    float bearing = 0;
    Location oldLocation;

    Marker mDriverMarker;
    Marker mCustomerMarker;
    double customerLat = 0,customerLng = 0; // Test location
    String destination = "";

    private List<LatLng> polyLineList;
    private Polyline direction;
    IGoogleAPI mService;
    IFCMService mFCMService;

    String driverID;
    DatabaseReference driverWorking;
    GeoFire mGeoFire;

    @BindView(R.id.btn_phone)
    RelativeLayout mBtnCall;

    @BindView(R.id.btn_chat)
    RelativeLayout mBtnChat;

    @BindView(R.id.btn_cancel)
    RelativeLayout mBtnCancel;

    @BindView(R.id.btn_pick_up)
    Button mBtnPickup;

    @BindView(R.id.layout_pickup)
    RelativeLayout mLayoutPickup;

    @BindView(R.id.layout_cancel)
    RelativeLayout mLayoutCancel;

    @BindView(R.id.layout_name_direction)
    RelativeLayout mLayoutBellowPickup;

    @BindView(R.id.img_location)
    ImageView mImgLocation;

    @BindView(R.id.ic_position)
    ImageView mImgPosition;

    @BindView(R.id.img_navigation)
    ImageView mImgNavigation;

    @BindView(R.id.img_notification_message)
    ImageView mImgNotificationMessage;

    @BindView(R.id.txt_name_location)
    TextView mTxtNameLocation;

    DatabaseReference mPickupRequestDatabase;
    DatabaseReference mCustomerDatabase;
    DatabaseReference mTripInfoDatabase;
    PickupRequest mPickupRequest;
    TripInfo mTripInfo;
    Customer mCustomer;
    Driver mDriver;
    Boolean onPayment = false;
    HashMap<String,Object> mapTripInfo = new HashMap<>();
    int fixedFare = 0;
    String otherToll = "0", keyTrip = "";
    boolean isCustomerCancel = false, isCompleteTrip = false;
    GeoQuery mGeoQueryCheckNear;
    String pickup = "";

    String currentService = "";

    ValueAnimator animNotification;


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
        setContentView(R.layout.activity_driver_tracking);
        ButterKnife.bind(this);

        currentService = Common.service_vehicle_standard;

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mBtnCall.setOnClickListener(this);
        mBtnChat.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
        mBtnPickup.setOnClickListener(this);
        mImgNavigation.setOnClickListener(this);

        mImgPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Common.mLastLocationDriver!=null){
                    CameraPosition cameraPos = new CameraPosition.Builder()
                            .target(new LatLng(Common.mLastLocationDriver.getLatitude(),
                                    Common.mLastLocationDriver.getLongitude()))
                            .zoom(20.0f).bearing(bearing).tilt(30).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos), null);
                }

            }
        });

        animNotification = new ValueAnimator();

        mImgNotificationMessage.setVisibility(View.GONE);

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();
        driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference().child("Chat")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("onstate").setValue(false);

        //Geo Fire
        driverWorking = FirebaseDatabase.getInstance().getReference(Common.driver_working_tbl);
        mPickupRequestDatabase = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference(Common.customers_tbl);
        mTripInfoDatabase = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl)
                .child(getIntent().getStringExtra("keyTrip"));
        mGeoFire = new GeoFire(driverWorking);

        mapTripInfo = new HashMap<>();
        Log.e("Key",getIntent().getStringExtra("keyTrip"));
        keyTrip = getIntent().getStringExtra("keyTrip");

        mPickupRequestDatabase.child(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            mPickupRequest = dataSnapshot.getValue(PickupRequest.class);
                            if (isPickup) {
                                mTripInfo = new TripInfo();
                                customerLat = Double.valueOf(mPickupRequest.getLatPickup());
                                customerLng = Double.valueOf(mPickupRequest.getLngPickup());
                                destination = mPickupRequest.getDestination();
                                mTxtNameLocation.setText(getNameAdress(customerLat, customerLng).toUpperCase());
                                // create trip info
                                mTripInfo.setPickup(getNameAdress(customerLat, customerLng));
                                pickup = getNameAdress(customerLat, customerLng);
                                mTripInfo.setDropoff(destination);
                                mTripInfo.setCustomerId(mPickupRequest.getCustomerId());
                                mTripInfo.setDriverId(driverID);
                                mTripInfoDatabase.setValue(mTripInfo);
                                mapTripInfo.put("dateCreated", ServerValue.TIMESTAMP);

                                getInfoDriver();

                                getInfoTrip(customerLat,customerLng,destination);
                                getInfoCustomer(mPickupRequest.getCustomerId());
                                sendKeyTripToCustomer(getIntent().getStringExtra("keyTrip"));
                                // remove event listener
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("ERROR", "Failed to read value.", error.toException());
                    }
                });

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

    }



    private void sendKeyTripToCustomer(final String key) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(mPickupRequest.getCustomerId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);
                            Notification notification = new Notification("KeyTrip",key);
                            Sender sender = new Sender(token.getToken(),notification);
                            mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1){
                                        Log.e("KeyTripToCustomer","Sucess");
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
                                        Log.e("KeyTripToCustomer",response.message());
                                        Log.e("KeyTripToCustomer",response.errorBody().toString());
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

    private void getInfoCustomer(String id) {
        mCustomerDatabase.child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            mCustomer = dataSnapshot.getValue(Customer.class);
                            mCustomerMarker = mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_your_place))
                                    .position(new LatLng(customerLat, customerLng))
                                    .snippet(mCustomer.getImgUrl())
                                    .title("Pickup here"+Common.keySplit+mCustomer.getPhoneNum()));
                            mCustomerMarker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("ERROR", "Failed to read value.", error.toException());
                    }
                });
    }

    private void getInfoDriver(){
        FirebaseDatabase.getInstance().getReference(Common.drivers_tbl).child(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            mDriver = dataSnapshot.getValue(Driver.class);
                            currentService = mDriver.getServiceVehicle();
                            mapTripInfo.put("serviceVehicle",mDriver.getServiceVehicle());
                            mTripInfoDatabase.updateChildren(mapTripInfo);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private String getNameAdress(double lat,double lng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat,lng, 1);
            if(!addresses.isEmpty()){
                Address obj = addresses.get(0);
                String namePlacePickup = obj.getSubThoroughfare()+", "+obj.getLocality()+", "+obj.getSubAdminArea();
                return namePlacePickup;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    @Override
    public void networkAvailable() {
        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();
        setupLocation();
    }

    @Override
    public void networkUnavailable() {
        Toast.makeText(this,"Please connect internet !!!",Toast.LENGTH_LONG).show();
    }

    private void call(String phone) {
        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener,
                PhoneStateListener.LISTEN_CALL_STATE);

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+phone));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
//            ActivityCompat.requestPermissions(getParent(),new String[] {Manifest.permission.READ_PHONE_STATE,
//                    Manifest.permission.CALL_PHONE},123);
            return;
        }
        startActivity(callIntent);

    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            try{
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
        // Remove driver when driver not available

    }

    @Override
    public void onStart() {
        buildGoogleApiClient();
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayService()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }else{
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.READ_PHONE_STATE
                    }, MY_PERMISSION_REQUEST_CODE);
                }
        }
    }

    // setup permission
    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_STATE
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayService()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    // create locationrequest
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    // check version googleplayservice
    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    // create googleapiclient
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    // Show location driver on map
    private void displayLocation() {

        try{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        Common.mLastLocationDriver = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mLastLocationDriver != null) {
            final double latitude = Common.mLastLocationDriver.getLatitude();
            final double longitude = Common.mLastLocationDriver.getLongitude();


            mGeoFire.setLocation(driverID, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                    if(oldLocation!=null){
                        Location startingLocation = new Location("starting point");
                        startingLocation.setLatitude(oldLocation.getLatitude());
                        startingLocation.setLongitude(oldLocation.getLongitude());

                        //Get the target location
                        Location endingLocation = new Location("ending point");
                        endingLocation.setLatitude(latitude);
                        endingLocation.setLongitude(longitude);

                        bearing = startingLocation.bearingTo(endingLocation);
                    }
                    //Add marker
                    mMap.clear();

                    mDriverMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_driver))
                            .position(new LatLng(latitude, longitude))
                            .flat(true)
                            .anchor(0.5f, 0.5f)
                            .rotation(bearing)
                            .snippet(mDriver.getImgUrl())
                            .title("You"+Common.keySplit+mDriver.getPhoneNum()));
                    getDirection();

                    if(mCustomer!=null){
                        if(isPickup){
                            mCustomerMarker = mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_your_place))
                                    .position(new LatLng(customerLat, customerLng))
                                    .snippet(mCustomer.getImgUrl())
                                    .title("Pickup here"+Common.keySplit+mCustomer.getPhoneNum()));
                            mCustomerMarker.showInfoWindow();
                        }else{
                            mCustomerMarker = mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_drop_off))
                                    .position(new LatLng(customerLat, customerLng))
                                    .snippet(mCustomer.getImgUrl())
                                    .title("Drop off here"+Common.keySplit+destination));
                            mCustomerMarker.showInfoWindow();
                        }
                    }

                    CameraPosition cameraPos = new CameraPosition.Builder().target(new LatLng(latitude,longitude))
                            .zoom(20.0f).bearing(bearing).tilt(30).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos), null);
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));
                }
            });

        } else {
            Log.e("ERROR", "Can't get your location");
        }
    }

    private void getDirection() {
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ Common.mLastLocationDriver.getLatitude()+","+Common.mLastLocationDriver.getLongitude()+"&"+
                    "destination="+customerLat+","+customerLng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);

            Log.e("getDirection",requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {

                                new ParserTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,response.body().toString());
                            } catch (Exception e) {
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

    // Update location driver
    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private class PhoneCallListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (TelephonyManager.CALL_STATE_RINGING == state) {
            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_phone:
                call(mCustomer.getPhoneNum());
                break;
            case R.id.img_navigation:
                Intent navigation = new Intent(Intent.ACTION_VIEW, Uri
                        .parse("http://maps.google.com/maps?saddr="
                                + Common.mLastLocationDriver.getLatitude()  + ","
                                + Common.mLastLocationDriver.getLongitude() +
                                "&daddr="
                                + customerLat + "," + customerLng));
                navigation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK&Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                navigation.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(navigation);

                break;
            case R.id.btn_chat:
                mImgNotificationMessage.setVisibility(View.GONE);

                Intent chatIntent = new Intent(DriverTrackingAcitivity.this, ChatActivity.class);
                chatIntent.putExtra("user_id", mPickupRequest.getCustomerId());
                chatIntent.putExtra("user_name", mCustomer.getName());
                chatIntent.putExtra("driver", mCustomer.getName());
                startActivity(chatIntent);

                break;
            case R.id.btn_cancel:
                final String[] listCancel = {"Emergency contact","Customer request to cancel trips"};
                ressonCancelTrip = listCancel[0];
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Reason to cancel trips")
                        .setSingleChoiceItems(listCancel,0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int selectedIndex) {
                                ressonCancelTrip = listCancel[selectedIndex];
                            }
                        })
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sendMessageCancelTripToCustomer();
                                removeWorkingDriver();
                                final SpotsDialog waitingDialog = new SpotsDialog(DriverTrackingAcitivity.this);
                                waitingDialog.show();
                                HashMap<String,Object> maps = new HashMap<>();
                                maps.put("reasonCancel",ressonCancelTrip);
                                maps.put("status",Common.trip_info_status_driver_cancel);
                                mTripInfoDatabase.updateChildren(maps).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        mPickupRequestDatabase.child(driverID).removeValue();
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                waitingDialog.dismiss();
                                                Toast.makeText(getApplicationContext(),"Cancel success!",Toast.LENGTH_SHORT).show();
                                                Handler newHandler = new Handler();
                                                newHandler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        finish();
                                                    }
                                                },500);
                                            }
                                        },1000);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        waitingDialog.dismiss();
                                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
                break;
            case R.id.btn_pick_up:
                if(isPickup){
                    new AlertDialog.Builder(this)
                            .setTitle("Pick-up customer")
                            .setMessage("Are you sure?")
                            .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setNeutralButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    mLayoutPickup.animate().alpha(0.0f).setDuration(500).withEndAction(new Runnable() {
                                        @Override
                                        public void run() {

                                            isPickup = false;
                                            sendMessagePickupToCustomer();

                                            mapTripInfo.put("timePickup",ServerValue.TIMESTAMP);
                                            mTripInfoDatabase.updateChildren(mapTripInfo);
                                            mapTripInfo.clear();

                                            LatLng dropOffLocation = getLocationFromAddress(getApplicationContext(),destination);
                                            customerLat = dropOffLocation.latitude;
                                            customerLng = dropOffLocation.longitude;

                                            HashMap<String, Object> timePickup = new HashMap<>();
                                            timePickup.put("timePickup",ServerValue.TIMESTAMP);
                                            // Delete pickup Request and create Ride info

                                            mTxtNameLocation.setText(destination);
                                            Log.e("DESTINATION",destination);
                                            mImgLocation.setImageResource(R.drawable.ic_drop_off);

                                            mMap.clear();
                                            displayLocation();

                                            mLayoutPickup.setVisibility(View.GONE);
                                            mBtnPickup.setBackground(getResources().getDrawable(R.drawable.btn_sign_in_driver));
                                            mBtnPickup.setText("DROP-OFF");
                                            mLayoutCancel.setVisibility(View.GONE);
                                            mLayoutBellowPickup.setBackgroundColor(getResources().getColor(R.color.edtRegister));
                                        }
                                    });
                                }
                            })
                            .show();
                }else{
                    new AlertDialog.Builder(this)
                            .setTitle("Drop-off customer")
                            .setMessage("Are you sure?")
                            .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setNeutralButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    isCompleteTrip = true;
                                    mPickupRequestDatabase.child(driverID).removeValue();
                                    sendMessageDropOffToCustomer();
                                    mapTripInfo.put("timeDropoff",ServerValue.TIMESTAMP);
                                    mapTripInfo.put("status",Common.trip_info_status_complete);
                                    mTripInfoDatabase.updateChildren(mapTripInfo);
                                    mapTripInfo.clear();

                                    Intent intent = new Intent(DriverTrackingAcitivity.this,DriverConfirmBillActivity.class);
                                    intent.putExtra("keyTrip",keyTrip);
                                    intent.putExtra("fixedFare",fixedFare);
                                    intent.putExtra("pickup",pickup);
                                    intent.putExtra("destination",destination);
                                    startActivity(intent);
                                    finish();
//                                    showPaymentDialog();
                                }
                            })
                            .show();
                }

                break;
        }
    }

    private void sendMessageCancelTripToCustomer() {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(mPickupRequest.getCustomerId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);
                            Notification notification = new Notification("DriverCancelTrip","Driver cancel the trip");
                            Sender sender = new Sender(token.getToken(),notification);
                            mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1){
                                        Log.e("MessagePickup","Sucess");
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
                                        Log.e("MessageCancelTrip",response.message());
                                        Log.e("MessageCancelTrip",response.errorBody().toString());
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

    private void sendMessagePickupToCustomer() {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(mPickupRequest.getCustomerId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);
                            Notification notification = new Notification("Pickup","You on the trip now!");
                            Sender sender = new Sender(token.getToken(),notification);
                            mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1){
                                        Log.e("MessagePickup","Sucess");
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
                                        Log.e("MessagePickup",response.message());
                                        Log.e("MessagePickup",response.errorBody().toString());
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

    private void sendMessageDropOffToCustomer() {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(mPickupRequest.getCustomerId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);
                            Notification notification = new Notification("DropOff","Completed trip!");
                            Sender sender = new Sender(token.getToken(),notification);
                            mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1){
                                        Log.e("MessageDropOff","Sucess");
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
                                        Log.e("MessageDropOff",response.message());
                                        Log.e("MessageDropOff",response.errorBody().toString());
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
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        oldLocation = Common.mLastLocationDriver;
        Common.mLastLocationDriver = location;
        // remove any driveravailabled
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.driver_available_tbl);

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
            }
        });
        // Don't send notification again when sent
        if(!isSendNotification)
            checkDriverNear();

        displayLocation();
    }

    private void checkDriverNear() {
        final GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_working_tbl));
        mGeoQueryCheckNear = geoFire.queryAtLocation(new GeoLocation(customerLat,customerLng),0.05f);
        mGeoQueryCheckNear.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!isSendNotification){
                    isSendNotification = true;
                    sendArrivedNotification(mPickupRequest.getCustomerId());
                }

                mGeoQueryCheckNear.removeAllListeners();
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        mMap.setTrafficEnabled(false);
//        mMap.setIndoorEnabled(false);
//        mMap.setBuildingsEnabled(false);
//        mMap.getUiSettings().setZoomControlsEnabled(true);

    }

    private void sendArrivedNotification( String customerID) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(customerID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);
                            Notification notification = new Notification("Arrived","Your driver has arrived here!");
                            Sender sender = new Sender(token.getToken(),notification);
                                mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                    @Override
                                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                        if(response.body().success == 1){
                                            isSendNotification = true;
                                            Log.e("ArrivedNotification","Success");
                                        }
                                        else{
                                            Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
                                            Log.e("ArrivedNotification",response.message());
                                            Log.e("ArrivedNotification",response.errorBody().toString());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                                    }
                                });

                                tokens.orderByKey().equalTo(customerID).removeEventListener(this);
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        HashMap<String,Object> maps = new HashMap<>();
//        sendMessageCancelTripToCustomer();
//        maps.put("reasonCancel","Driver destroy app");
//        maps.put("status",Common.trip_info_status_driver_cancel);
//        mTripInfoDatabase.updateChildren(maps);
//        mPickupRequestDatabase.child(driverID).removeValue();

        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
        this.unregisterReceiver(mReceiver);
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>{

        ProgressDialog mDialog = new ProgressDialog(DriverTrackingAcitivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please waiting...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String,String>>> routes = null;
            try{
                jsonObject = new JSONObject(strings[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions = null;
            for(int i=0;i<lists.size();i++){
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String,String>> path = lists.get(i);
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat,lng);

                    if(i == lists.size()-1 && j == path.size()-1){
                        drawLastRoute(lat,lng);
                    }
                    if(i == 0 && j == 0){
                        drawFirstRoute(lat,lng);
                    }

                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                if(isPickup)
                    polylineOptions.color(getResources().getColor(R.color.rippleEffectColor));
                else
                    polylineOptions.color(getResources().getColor(R.color.colorActiveNavigation));
                polylineOptions.geodesic(true);
            }
            if(polylineOptions!=null)
                direction = mMap.addPolyline(polylineOptions);
        }
    }

    private void drawLastRoute(double lat, double lng) {
        PolylineOptions polylineOptions = new PolylineOptions().add(new LatLng(lat,lng))
                .add(new LatLng(customerLat,customerLng)).width(8);
        if(isPickup)
            polylineOptions.color(getResources().getColor(R.color.rippleEffectColor));
        else
            polylineOptions.color(getResources().getColor(R.color.colorActiveNavigation));
        polylineOptions.geodesic(true);
        polylineOptions.pattern(PATTERN_POLYGON_ALPHA);
        mMap.addPolyline(polylineOptions);
    }

    private void drawFirstRoute(double lat, double lng) {
        PolylineOptions polylineOptions = new PolylineOptions().add(new LatLng(lat,lng))
                .add(new LatLng(Common.mLastLocationDriver.getLatitude(),Common.mLastLocationDriver.getLongitude()))
                .width(8);
        if(isPickup)
            polylineOptions.color(getResources().getColor(R.color.rippleEffectColor));
        else
            polylineOptions.color(getResources().getColor(R.color.colorActiveNavigation));
        polylineOptions.geodesic(true);
        polylineOptions.pattern(PATTERN_POLYGON_ALPHA);
        mMap.addPolyline(polylineOptions);
    }

    public static final int PATTERN_DASH_LENGTH_PX = 20;
    public static final int PATTERN_GAP_LENGTH_PX = 5;
    public static final PatternItem DOT = new Dot();
    public static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    public static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    public static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(DOT, GAP);

    public LatLng getLocationFromAddress(Context context, String inputtedAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng resLatLng = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(inputtedAddress, 5);
            if (address == null) {
                return null;
            }

            if (address.size() == 0) {
                return null;
            }

            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            resLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {

            ex.printStackTrace();
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        return resLatLng;
    }

    private void showPaymentDialog() {
        onPayment = true;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        builder.setCancelable(true);

        LayoutInflater inflater = LayoutInflater.from(this);
        View paymentLayout = inflater.inflate(R.layout.layout_payment,null);

        builder.setView(paymentLayout);
        final AlertDialog dialog;
        dialog = builder.create();

        final TextView txtFixedFare = paymentLayout.findViewById(R.id.txt_fixed_fare);
        final EditText editTollOther = paymentLayout.findViewById(R.id.edt_toll_other);
        final TextView txtTotal = paymentLayout.findViewById(R.id.txt_total_payout);
        final Button btnConfirm = paymentLayout.findViewById(R.id.btn_confirm);
        txtFixedFare.setText(String.valueOf(fixedFare*1000));
        txtTotal.setText(String.valueOf(fixedFare*1000));


        editTollOther.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                otherToll = charSequence.toString();
                int total = fixedFare*1000+Integer.parseInt(otherToll);
                txtTotal.setText(String.valueOf(total));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Set button dialog
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeWorkingDriver();
                if(Integer.parseInt(otherToll)>500){
                    mapTripInfo.put("otherToll",otherToll);
                    mTripInfoDatabase.updateChildren(mapTripInfo);
                }
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();

    }

    private void removeWorkingDriver() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.driver_working_tbl);

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(!onPayment){
            Toast.makeText(getApplicationContext(),"Can't back when on trip",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),"Please commit bill to finish trip",Toast.LENGTH_LONG).show();
        }
    }

    public String formatNumber(int number){
        DecimalFormat decimalFormat = new DecimalFormat("###.###.###");
        String numberAsString = decimalFormat.format(number);
        return numberAsString;
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


        Double fareStandard = Common.base_fare + Common.cost_per_km*realDistance + Common.cost_per_minute_standard*realTime;
        Double farePremium = Common.base_fare + Common.cost_per_km*realDistance + Common.cost_per_minute_premium*realTime;
        String textFareStandard = "VND "+Integer.toString(fareStandard.intValue())+"K";
        String textFarePremium = "VND "+Integer.toString(farePremium.intValue())+"K";
        if(currentService.equals(Common.service_vehicle_standard)){
            fixedFare = fareStandard.intValue();
            mapTripInfo.put("fixedFare",String.valueOf(fixedFare*1000));
        }else{
            fixedFare = farePremium.intValue();
            mapTripInfo.put("fixedFare",String.valueOf(fixedFare*1000));
        }

        Log.e("CalculateFare",""+fixedFare);

        mTripInfoDatabase.updateChildren(mapTripInfo);
    }

    private void getInfoTrip(double lat, double lng, String destination) {
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+lat+","+lng+"&"+
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
                                Log.e("TRACKING","getInfoTrip onResponse");
                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");

                                JSONObject legsObject = legs.getJSONObject(0);

                                JSONObject distanceJS = legsObject.getJSONObject("distance");

                                mapTripInfo.put("distance",distanceJS.getString("text"));
                                Double distance = distanceJS.getDouble("value");
                                Log.e("TRACKING","getInfoTrip onResponse "+distance);
                                JSONObject timeJS = legsObject.getJSONObject("duration");
                                mapTripInfo.put("time",timeJS.getString("text"));

                                mTripInfoDatabase.updateChildren(mapTripInfo);
                                Double time = timeJS.getDouble("value");

                                calculateFare(distance,time);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("ERRORTRACK",e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.e("ERRORTRACK",t.getMessage());
                        }
                    });

        }catch (Exception e){
            Log.e("ERRORTRACK",e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                if(intent.getStringExtra("CustomerCancelTrip")!=null){
                    isCustomerCancel = true;
                    Toast.makeText(getApplicationContext(),"Customer cancel the trip",Toast.LENGTH_LONG).show();
                    mPickupRequestDatabase.child(driverID).removeValue();
                    removeWorkingDriver();
                    final SpotsDialog waitingDialog = new SpotsDialog(DriverTrackingAcitivity.this);
                    waitingDialog.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            waitingDialog.dismiss();
                            finish();
                        }
                    },1500);
                }else if(intent.getStringExtra("Chat")!=null){
                    mImgNotificationMessage.setVisibility(View.VISIBLE);
                }
            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);
    }

    private void startAnimationNotification() {
        animNotification.setIntValues(Color.WHITE, R.color.colorNavigation);
        animNotification.setEvaluator(new ArgbEvaluator());
        animNotification.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if((Integer)valueAnimator.getAnimatedValue()==Color.WHITE){
                    mBtnChat.setBackground(getResources().getDrawable(R.drawable.bg_chat_active));
                }else{
                    mBtnChat.setBackground(getResources().getDrawable(R.drawable.bg_chat));
                }
            }
        });

        animNotification.setDuration(1000);
        animNotification.setRepeatCount(Animation.INFINITE);
        animNotification.start();
    }


}
