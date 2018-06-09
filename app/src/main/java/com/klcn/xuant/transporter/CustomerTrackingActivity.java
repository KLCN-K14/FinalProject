package com.klcn.xuant.transporter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.helper.CustomInfoWindow;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.receiver.NetworkStateReceiver;
import com.klcn.xuant.transporter.remote.IGoogleAPI;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerTrackingActivity extends AppCompatActivity implements
        View.OnClickListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, NetworkStateReceiver.NetworkStateReceiverListener {

    private NetworkStateReceiver networkStateReceiver;

    private static final int PICK_REQUEST = 1;
    private FirebaseAuth mFirebaseAuth;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mDriversDatabase;


    private String mCurrent_user_id;
    private String mDriverID;
    private String mPickUp;
    private String mDestination;
    Driver mDriver;
    private TextView mNameDriver;

    @BindView(R.id.txt_license_plate)
    TextView mTxtLicensePlate;

    @BindView(R.id.txt_name_driver)
    TextView mTxtNameDriver;

    @BindView(R.id.txt_place_location)
    TextView mTxtPlaceLocation;

    @BindView(R.id.txt_place_destination)
    TextView mTxtPlaceDestination;

    @BindView(R.id.btn_cancel_book)
    Button mBtnCancelBook;

    @BindView(R.id.img_ic_chat)
    ImageView mImgChat;

    @BindView(R.id.img_ic_phone)
    ImageView mImgPhone;

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    String ressonCancelTrip = "";
    float bearing = 0;
    Location oldLocationDriver;

    DatabaseReference driverFound;

    Marker mDriverMarker;
    Marker mCustomerMarker;
    Circle mCustomerCircle;

    private List<LatLng> polyLineList;
    private Polyline direction;
    IGoogleAPI mService;

    private static int LIMIT_RANGE = 5;// 5km

    int distance = 1;
    HashMap<String,Marker> hashMapMarker = new HashMap<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_tracking_activity);
        ButterKnife.bind(this);


        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mFirebaseAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);
        mDriversDatabase = FirebaseDatabase.getInstance().getReference().child("Drivers");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mDriversDatabase.keepSynced(true);

        mDriverID = getIntent().getStringExtra("driverID");
        mDestination = getIntent().getStringExtra("destination");
        mPickUp = getIntent().getStringExtra("pickup");
        if (mDriverID != null) {
            Toast.makeText(getApplicationContext(),mDriverID,Toast.LENGTH_LONG);
            mDriversDatabase.child(mDriverID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mDriver = dataSnapshot.getValue(Driver.class);

                    mTxtNameDriver.setText(mDriver.getName());
                    mTxtLicensePlate.setText(mDriver.getLicensePlate());
                    mTxtPlaceLocation.setText(mPickUp);
                    mTxtPlaceDestination.setText(mDestination);

                    showFoundDriverDialog();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("ERROR", "Failed to read value.", error.toException());
                }
            });
        }else{
            Toast.makeText(getApplicationContext(),"Null",Toast.LENGTH_LONG).show();
        }

        //Bottom sheet
        View llBottomSheet = (View) findViewById(R.id.bottom_sheet);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                fab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mBtnCancelBook.setOnClickListener(this);
        mImgChat.setOnClickListener(this);
        mImgPhone.setOnClickListener(this);

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel_book:
                Intent intent = new Intent(CustomerTrackingActivity.this, CancelBookActivity.class);
                startActivityForResult(intent, PICK_REQUEST);
                break;
            case R.id.img_ic_chat:

                Intent chatIntent = new Intent(CustomerTrackingActivity.this, ChatActivity.class);
                chatIntent.putExtra("user_id", "VxE53ShAMWOdkKbTOQ6KT6J3ZII2");
                chatIntent.putExtra("user_name", "test");
                startActivity(chatIntent);

                break;
            case R.id.img_ic_phone:
                call(mDriver.getPhoneNum());
                break;
        }
    }

    private void showFoundDriverDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        LayoutInflater inflater = LayoutInflater.from(this);
        View foundDriverLayout = inflater.inflate(R.layout.layout_found_driver, null);

        builder.setView(foundDriverLayout);
        final AlertDialog dialog;
        dialog = builder.create();

        final TextView txtNameDriver = foundDriverLayout.findViewById(R.id.txt_name_driver_dialog);
        final TextView txtNameCar = foundDriverLayout.findViewById(R.id.txt_name_car_dialog);
        final TextView txtLicensePlate = foundDriverLayout.findViewById(R.id.txt_license_plate_dialog);
        final RatingBar ratingBar = foundDriverLayout.findViewById(R.id.rating_bar);

        txtNameDriver.setText(mDriver.getName());
        txtNameCar.setText(mDriver.getNameVehicle());
        txtLicensePlate.setText(mDriver.getLicensePlate());
        ratingBar.setRating(Float.valueOf(mDriver.getAvgRatings()));

        dialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 3000);
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
        Common.mLastLocationCustomer = location;
        displayLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mCustomerCircle = mMap.addCircle(new CircleOptions()
            .center(new LatLng(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude()))
            .radius(10)
            .strokeColor(Color.BLUE)
            .fillColor(0x220000FF)
            .strokeWidth(5.0f));
    }

    private void loadDriverFound() {
        mMap.clear();
        mCustomerMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_vitri))
                .position(new LatLng(Common.mLastLocationCustomer.getLatitude(), Common.mLastLocationCustomer.getLongitude()))
                .flat(true)
                .title("You"));

        mCustomerCircle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude()))
                .radius(10)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f));

        DatabaseReference driverAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_working_tbl);
        GeoFire gfDriverAvailable = new GeoFire(driverAvailable);

        GeoQuery geoQuery = gfDriverAvailable.queryAtLocation(new GeoLocation(Common.mLastLocationCustomer.getLatitude(),
                Common.mLastLocationCustomer.getLongitude()),distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                if(key.equals(mDriverID)){
                    FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                            .child(key)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(oldLocationDriver!=null){
                                        Location startingLocation = new Location("starting point");
                                        startingLocation.setLatitude(oldLocationDriver.getLatitude());
                                        startingLocation.setLongitude(oldLocationDriver.getLongitude());

                                        //Get the target location
                                        Location endingLocation = new Location("ending point");
                                        endingLocation.setLatitude(location.latitude);
                                        endingLocation.setLongitude(location.longitude);

                                        bearing = startingLocation.bearingTo(endingLocation);
                                    }

                                    Driver driver = dataSnapshot.getValue(Driver.class);
                                    Marker mMarker = hashMapMarker.get(dataSnapshot.getKey());

                                    if(mMarker==null){
                                        mMarker = mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(location.latitude,location.longitude))
                                                .snippet(driver.getPhoneNum())
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.motobike_ver2))
                                                .title(driver.getName())
                                                .flat(true)
                                                .anchor(0.5f, 0.5f)
                                                .rotation(bearing));
                                        hashMapMarker.put(dataSnapshot.getKey(),mMarker);
                                    }
                                    oldLocationDriver.setLatitude(location.latitude);
                                    oldLocationDriver.setLongitude(location.longitude);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onKeyExited(String key) {
//                Marker marker = hashMapMarker.get(key);
//                if(marker!=null){
//                    marker.remove();
//                    hashMapMarker.remove(key);
//                    Log.e("REMOVE",key+"  out hashMapMarker");
//                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
//                Marker marker = hashMapMarker.get(key);
//                if(marker!=null){
//                    marker.remove();
//                    hashMapMarker.remove(key);
//                    Log.e("REMOVE",key+"  out hashMapMarker");
//                }
            }

            @Override
            public void onGeoQueryReady() {
                if(distance<=LIMIT_RANGE){
                    distance++;
                    loadDriverFound();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void networkAvailable() {
        mService = Common.getGoogleAPI();
        setupLocation();
    }

    // Update location driver
    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    // setup permission
    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
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
        Common.mLastLocationCustomer = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mLastLocationCustomer != null) {
            final double latitude = Common.mLastLocationCustomer.getLatitude();
            final double longitude = Common.mLastLocationCustomer.getLongitude();


            driverFound = FirebaseDatabase.getInstance().getReference(Common.driver_working_tbl);
            driverFound.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    loadDriverFound();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //Add marker
            if (mCustomerMarker != null)
                mCustomerMarker.remove();
            mCustomerMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_vitri))
                            .position(new LatLng(latitude, longitude))
                            .flat(true)
                            .title("You"));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));
            loadDriverFound();
        } else {
            Log.e("ERROR", "Can't get your location");
        }
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
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }
}
