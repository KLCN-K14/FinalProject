package com.klcn.xuant.transporter;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.helper.DirectionsJSONParser;
import com.klcn.xuant.transporter.model.Customer;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.FCMResponse;
import com.klcn.xuant.transporter.model.Notification;
import com.klcn.xuant.transporter.model.RideInfo;
import com.klcn.xuant.transporter.model.Sender;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.receiver.NetworkStateReceiver;
import com.klcn.xuant.transporter.remote.IFCMService;
import com.klcn.xuant.transporter.remote.IGoogleAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    Boolean isPickup = true;
    String ressonCancelTrip = "";
    float bearing = 0;
    Location oldLocation;

    Marker mDriverMarker;
    Marker mCustomerMarker;
    Circle mCustomerCircle;
    double customerLat = 10.8397849,customerLng = 106.7935081; // Test location
    String customerID;
    String destination = "Trường Tiểu học Trương Văn Thành";

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

    @BindView(R.id.txt_name_location)
    TextView mTxtNameLocation;

    DatabaseReference mRideInfoDatabase;
    DatabaseReference mCustomerDatabase;
    RideInfo mRideInfo;
    Customer mCustomer;

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

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mBtnCall.setOnClickListener(this);
        mBtnChat.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
        mBtnPickup.setOnClickListener(this);

        driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //Geo Fire
        driverWorking = FirebaseDatabase.getInstance().getReference(Common.driver_working_tbl);
        mRideInfoDatabase = FirebaseDatabase.getInstance().getReference(Common.ride_info_tbl);
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference(Common.customers_tbl);
        mGeoFire = new GeoFire(driverWorking);
        Log.e("DRIVERTRACKING","Start");

        mRideInfoDatabase.child(driverID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mRideInfo = dataSnapshot.getValue(RideInfo.class);
                customerLat = Double.valueOf(mRideInfo.getLatPickup());
                customerLng = Double.valueOf(mRideInfo.getLngPickup());
                destination = mRideInfo.getDestination();
                mCustomerMarker = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_your_place))
                        .position(new LatLng(customerLat, customerLng))
                        .title("Customer"));
                mTxtNameLocation.setText(getNameAdress(customerLat,customerLng));
                getInfoCustomer(mRideInfo.getCustomerId());
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

    private void getInfoCustomer(String id) {
        mCustomerDatabase.child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mCustomer = dataSnapshot.getValue(Customer.class);

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("ERROR", "Failed to read value.", error.toException());
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
                            .title("You"));
                    getDirection();
                    mCustomerMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_your_place))
                            .position(new LatLng(customerLat, customerLng))
                            .title("Customer"));
                    mTxtNameLocation.setText(getNameAdress(customerLat,customerLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));
//                    if(direction!=null){
//                        direction.remove();
//                        Log.e("REMOVE","REMOVE DIRECTION");
//                    }
                }
            });




        } else {
            Log.e("ERROR", "Can't get your location");
        }
    }

    private void getDirection() {
        Toast.makeText(getApplicationContext(),"New direction",Toast.LENGTH_LONG).show();
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ Common.mLastLocationDriver.getLatitude()+","+Common.mLastLocationDriver.getLongitude()+"&"+
                    "destination="+customerLat+","+customerLng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.e("TRANSPORT",requestApi);
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
            case R.id.btn_chat:
                Intent chatIntent = new Intent(DriverTrackingAcitivity.this, ChatActivity.class);
                chatIntent.putExtra("user_id", "VdlChGocK2bqNsnK1K8Jv0c2wXu2");
                chatIntent.putExtra("user_name", "thao le");
                chatIntent.putExtra("from","driver");
                startActivity(chatIntent);

                break;
            case R.id.btn_cancel:
                final String[] listCancel = {"Emergency contact","Customer request to cancel trips"};
                ressonCancelTrip = listCancel[0];
                new AlertDialog.Builder(this)
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
                                Toast.makeText(getApplicationContext(),ressonCancelTrip,Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String t = "1231233.123TRANSORT12321321.123123TRANSORTzxc123zx1c32";
                                String[] list = t.split("TRANSORT");
                                Toast.makeText(getApplicationContext(),list[0]+"---"+list[1]+"---"+list[2],Toast.LENGTH_LONG).show();
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

                                            mTxtNameLocation.setText(destination);
                                            mImgLocation.setImageResource(R.drawable.ic_drop_off);
                                            mMap.clear();
                                            displayLocation();

                                            LatLng dropOffLocation = getLocationFromAddress(getApplicationContext(),destination);
                                            customerLat = dropOffLocation.latitude;
                                            customerLng = dropOffLocation.longitude;
                                            mCustomerMarker = mMap.addMarker(new MarkerOptions()
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_drop_off))
                                                    .position(dropOffLocation)
                                                    .title("Customer"));
                                            getDirection();
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
                                    showPaymentDialog();
                                }
                            })
                            .show();
                }

                break;
        }
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
        // remove any driveravailable
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.driver_available_tbl);

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
            }
        });

        checkDriverNear();
        displayLocation();
    }

    private void checkDriverNear() {
        GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_working_tbl));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(customerLat,customerLng),0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendArrivedNotification(mRideInfo.getCustomerId());
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
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
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

    private void sendArrivedNotification(String customerID) {
        Token token = new Token(customerID);
        Notification notification = new Notification("Arrived","Your driver has arrived here!");
        Sender sender = new Sender(token.getToken(),notification);
        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success == 1)
                    Toast.makeText(getApplicationContext(),"Request sent",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
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

        final Long fixedFare = Long.parseLong(txtFixedFare.getText().toString().replace(".",""));

        editTollOther.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Set button dialog
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"btnConfirm",Toast.LENGTH_LONG).show();
            }
        });

        dialog.show();

    }
    public String formatNumber(int number){
        DecimalFormat decimalFormat = new DecimalFormat("###.###.###");
        String numberAsString = decimalFormat.format(number);
        return numberAsString;
    }
}
