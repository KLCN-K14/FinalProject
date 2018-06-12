package com.klcn.xuant.transporter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.receiver.NetworkStateReceiver;
import com.suke.widget.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.LOCATION_SERVICE;

public class DriverHomeFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,NetworkStateReceiver.NetworkStateReceiverListener {

    private NetworkStateReceiver networkStateReceiver;
    private GoogleMap mMap;


    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTERVAL = 2000;
    private static int FASTEST_INTERVAL = 1000;
    private static int DISPLACEMENT = 10;

    DatabaseReference driverAvailable;
    GeoFire mGeoFire;

    float bearing = 0;
    Location oldLocation;

    static boolean isLoggingOut = false;
    String driverID;

    static boolean onTrip = false;

    Marker mMarker;
    SupportMapFragment mapFragment;

    @BindView(R.id.switch_button)
    com.suke.widget.SwitchButton mSwitchButton;

    @BindView(R.id.txt_status)
    TextView mTxtStatus;


    public static DriverHomeFragment newInstance() {
        DriverHomeFragment fragment = new DriverHomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_home, null, false);
        ButterKnife.bind(this, view);

        mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {
                    slideDown(DriverMainActivity.mBottomNavigationView);
                    DriverMainActivity.mBottomNavigationView.setVisibility(View.GONE);
                    mTxtStatus.setText("ONLINE");
                    startLocationUpdate();
                    displayLocation();
                    Snackbar.make(getView(), "You are online", Snackbar.LENGTH_SHORT).show();
                } else {
                    slideUp(DriverMainActivity.mBottomNavigationView);
                    DriverMainActivity.mBottomNavigationView.setVisibility(View.VISIBLE);
                    stopLocationUpdate();
                    if (mMarker != null) {
                        mMarker.remove();
                        offlineDriver();
                    }
                    Snackbar.make(getView(), "You are offline", Snackbar.LENGTH_SHORT).show();
                    mTxtStatus.setText("OFFLINE");
                }
            }
        });

        FirebaseDatabase.getInstance().getReference(Common.driver_working_tbl)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if(dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            mSwitchButton.setChecked(false);
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


        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        getContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        setupInit();

        return view;
    }

    private void updateFireBaseToken() {
        DatabaseReference dbToken = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        dbToken.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void stopLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setupInit() {
        if (checkPlayService()) {
            buildGoogleApiClient();
            createLocationRequest();
            if (mSwitchButton.isChecked())
                displayLocation();
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
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), PLAY_SERVICE_RES_REQUEST).show();
            else {
                Toast.makeText(getContext(), "This device is not supported", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    // create googleapiclient
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    // Show location driver on map
    private void displayLocation() {
        try{
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        Common.mLastLocationDriver = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mLastLocationDriver != null) {
            if (mSwitchButton.isChecked()) {
                final double latitude = Common.mLastLocationDriver.getLatitude();
                final double longitude = Common.mLastLocationDriver.getLongitude();

                // save location driver to firebase to comunicate with customer
                mGeoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
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
                                if (mMarker != null)
                                    mMarker.remove();
                                mMarker = mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_driver))
                                        .position(new LatLng(latitude, longitude))
                                        .anchor(0.5f, 0.5f)
                                        .rotation(bearing)
                                        .title("You"));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
                            }
                        });
            }
        } else {
            Log.e("ERROR", "Can't get your location");
        }
    }

    // Update location driver
    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!onTrip){
            oldLocation = Common.mLastLocationDriver;
            Common.mLastLocationDriver = location;
            displayLocation();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
        // Remove driver when driver not available
        if(isLoggingOut){
            offlineDriver();
        }
    }

    @Override
    public void onStart() {
        buildGoogleApiClient();
        super.onStart();
    }

    public void offlineDriver() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.driver_available_tbl);

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
            }
        });
    }

    public void slideUp(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(1000);
        view.startAnimation(animate);
    }

    public void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(1000);
        view.startAnimation(animate);
    }

    @Override
    public void networkAvailable() {
        Log.e("networkAvailable","networkAvailable");
        driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //Geo Fire
        driverAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_available_tbl);
        mGeoFire = new GeoFire(driverAvailable);
        updateFireBaseToken();
        setupInit();
    }

    @Override
    public void networkUnavailable() {
        Toast.makeText(getActivity(),"Please connect internet !!!",Toast.LENGTH_LONG).show();
        offlineDriver();
    }

    @Override
    public void onResume() {
        super.onResume();

        FirebaseDatabase.getInstance().getReference(Common.ride_info_tbl)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if(dataSnapshot.getKey().equals(driverID)){
                            onTrip = true;
                            mSwitchButton.setChecked(false);
                            slideUp(DriverMainActivity.mBottomNavigationView);
                            DriverMainActivity.mBottomNavigationView.setVisibility(View.VISIBLE);
                            stopLocationUpdate();
                            if (mMarker != null) {
                                mMarker.remove();
                                offlineDriver();
                            }
                            Snackbar.make(getView(), "You are offline", Snackbar.LENGTH_SHORT).show();
                            mTxtStatus.setText("OFFLINE");
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(getContext(),DriverTrackingAcitivity.class);
                                    startActivity(intent);
                                }
                            },1000);
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
}