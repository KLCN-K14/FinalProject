package com.klcn.xuant.transporter.mvp.home;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.android.SphericalUtil;
import com.klcn.xuant.transporter.ChooseTypeUserActivity;
import com.klcn.xuant.transporter.CustomerTrackingActivity;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Customer;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.mvp.findDriver.CustomerFindDriverActivity;
import com.klcn.xuant.transporter.helper.CustomInfoWindow;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.mvp.history.CustomerHistoryActivity;
import com.klcn.xuant.transporter.mvp.profile.CustomerProfileActivity;
import com.klcn.xuant.transporter.receiver.NetworkStateReceiver;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class CustomerHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, PlaceSelectionListener,
        View.OnClickListener,NetworkStateReceiver.NetworkStateReceiverListener{

    private NetworkStateReceiver networkStateReceiver;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    private static int REQUEST_CODE_FIND_DRIVER = 1111;
    private static int LIMIT_RANGE = 5;// 5km

    int distance = 1;

    Place mPlaceDestination = null;
    GeoFire geoFire;
    Marker mUserMarker;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference customers;
    Customer customerModel;

    Driver currentDriver = null;
    float bearing = 0;
    GeoLocation oldLocationDriver;

    @BindView(R.id.btn_pick_request)
    Button btnPickRequest;
    @BindView(R.id.img_double_up_down)
    ImageView mImgUpDown;

    boolean isChooseDropOff = false;
    boolean isFirstTime = true;
    PlaceAutocompleteFragment pickDestination;
    PlaceAutocompleteFragment pickPickupPlace;
    AutocompleteFilter mAutocompleteFilter;

    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    TextView mTxtPhone;
    CircleImageView mAvatar;

    HashMap<String,Marker> hashMapMarker = new HashMap<>();
    HashMap<String,GeoLocation> hashMapLocation = new HashMap<>();
    HashMap<String,Float> hashMapBearing = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab!=null)
        {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        mFirebaseAuth= FirebaseAuth.getInstance();
        customers = FirebaseDatabase.getInstance().getReference().child("Customers").child(mFirebaseAuth.getCurrentUser().getUid());
        Log.e("Hom activity:::",customers.toString());

        mTxtPhone = (TextView) findViewById(R.id.txt_phone);
        mAvatar = (CircleImageView) findViewById(R.id.img_avatar_nav);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initView();
        btnPickRequest.setOnClickListener(this);
//        ref = FirebaseDatabase.getInstance().getReference("Drivers");
//        geoFire = new GeoFire(ref);
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        getApplicationContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        //Bottom sheet
        View llBottomSheet = (View) findViewById(R.id.bottom_sheet_home);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED){
                    mImgUpDown.setImageResource(R.drawable.ic_double_up);

                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                fab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
                mImgUpDown.setImageResource(R.drawable.ic_double_down);
            }
        });
//        getUserInfo();
    }


    private void updateFireBaseToken() {
        DatabaseReference dbToken = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        dbToken.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void initView(){

        //Vị trí của bạn
        pickPickupPlace= (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_location);
        pickPickupPlace.setOnPlaceSelectedListener(this);
        pickPickupPlace.setText("Your position");
        ((View)findViewById(R.id.place_autocomplete_search_button)).setVisibility(View.GONE);
        ((View)findViewById(R.id.place_autocomplete_clear_button)).setVisibility(View.GONE);
        pickPickupPlace.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.e("CustomerHomeActivity"," "+place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.e("error:::::"," "+status);
            }
        });

        //Bạn muốn đi đâu
        pickDestination = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_destination);
        pickDestination.setOnPlaceSelectedListener(this);
        pickDestination.setHint("Where are you going?");
        pickDestination.setBoundsBias(BOUNDS_MOUNTAIN_VIEW);
        pickDestination.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        pickDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceDestination = place;
                isChooseDropOff = true;
                btnPickRequest.setText("BOOK");
                Log.e("CustomerHomeActivity"," "+place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.e("error 2:::::"," "+status);
            }
        });

        mAutocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayService()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }else{
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, MY_PERMISSION_REQUEST_CODE);
                }
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,PLAY_SERVICE_RES_REQUEST).show();
            else{
                Toast.makeText(this,"This device is not supported",Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    public static void displayPromptForEnablingGPS(final Activity activity){

        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        builder.setTitle("Turn on GPS");
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Open GPS setting to start?";

        final AlertDialog dialog = builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                displayShouldPromptForEnablingGPS(activity);
                            }
                        }).create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(activity.getResources().getColor(R.color.red));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(activity.getResources().getColor(R.color.rippleEffectColor));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
            }
        });
        dialog.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        displayShouldPromptForEnablingGPS(activity);
                    }
                }
        );
        dialog.show();
    }

    public static void displayShouldPromptForEnablingGPS(final Activity activity){
        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        builder.setTitle("Turn on GPS");
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "App will not work if GPS disable?";

        final AlertDialog dialog = builder.setMessage(message)
                .setPositiveButton("Open GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Quit app",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.finish();
                            }
                        }).create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(activity.getResources().getColor(R.color.red));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(activity.getResources().getColor(R.color.rippleEffectColor));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
            }
        });

        dialog.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        displayShouldPromptForEnablingGPS(activity);
                    }
                }
        );
        dialog.show();
    }

    private void setupGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            setupPermissionLocation();
        }else{
            displayPromptForEnablingGPS(this);
        }

    }
    // setup permission
    private void setupPermissionLocation() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
            //Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayService()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void startLocationUpdate(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            return;
        }
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void displayLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            return;
        }
        Common.mLastLocationCustomer = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(Common.mLastLocationCustomer!=null){


            LatLng center = new LatLng(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude());
            // distance is meters. heading 0 is northside 90 is east 180 is south 270 is west
            LatLng southSide = SphericalUtil.computeOffset(center,100000,180);
            LatLng northSide = SphericalUtil.computeOffset(center,100000,0);

            LatLngBounds latLngBounds = LatLngBounds.builder()
                    .include(southSide)
                    .include(northSide)
                    .build();

            pickPickupPlace.setBoundsBias(latLngBounds);
            pickPickupPlace.setFilter(mAutocompleteFilter);

            pickDestination.setBoundsBias(latLngBounds);
            pickDestination.setFilter(mAutocompleteFilter);

            final double latitude = Common.mLastLocationCustomer.getLatitude();
            final double longitude = Common.mLastLocationCustomer.getLongitude();
            pickPickupPlace.setText(getNameAdress(Common.mLastLocationCustomer));
            //Add marker
            if(mUserMarker!=null)
                mUserMarker.remove();
            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_your_place))
                    .position(new LatLng(latitude,longitude))
                    .title("You"));
            mUserMarker.setZIndex(1);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15.0f));

            // load drivers available in map
            loadAllDriverAvailable();
        }else{
            Log.e("ERROR","Can't get your location");
        }
    }

    private void loadAllDriverAvailable() {
        DatabaseReference driverAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_available_tbl);
        GeoFire gfDriverAvailable = new GeoFire(driverAvailable);

        GeoQuery geoQuery = gfDriverAvailable.queryAtLocation(new GeoLocation(Common.mLastLocationCustomer.getLatitude(),
                Common.mLastLocationCustomer.getLongitude()),distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Driver driver = dataSnapshot.getValue(Driver.class);
                                Marker mMarker = hashMapMarker.get(dataSnapshot.getKey());
                                if(hashMapBearing.get(dataSnapshot.getKey())!=null)
                                    bearing = hashMapBearing.get(dataSnapshot.getKey());
                                if(mMarker==null){
                                    mMarker = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(location.latitude,location.longitude))
                                            .snippet(driver.getPhoneNum())
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.motobike_ver2))
                                            .title(driver.getName())
                                            .flat(true)
                                            .anchor(0.5f, 0.5f)
                                            .rotation(bearing));
                                    bearing = 0;
                                    hashMapMarker.put(dataSnapshot.getKey(),mMarker);
                                    hashMapLocation.put(dataSnapshot.getKey(),location);
                                }
                                Log.e("PUT",dataSnapshot.getKey()+"  into hashMapMarker");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {
                Marker marker = hashMapMarker.get(key);
                if(marker!=null){
                    marker.remove();
                    hashMapMarker.remove(key);
                    Log.e("REMOVE",key+"  out hashMapMarker");
                }
            }

            @Override
            public void onKeyMoved(final String key,final GeoLocation location) {
                Marker marker = hashMapMarker.get(key);
                if(marker!=null){
                    marker.remove();
                    hashMapMarker.remove(key);
                    FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                            .child(key)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Driver driver = dataSnapshot.getValue(Driver.class);
                                    Marker mMarker = hashMapMarker.get(dataSnapshot.getKey());
                                    oldLocationDriver = hashMapLocation.get(key);
                                    hashMapLocation.remove(key);
                                    if(oldLocationDriver!=null){
                                        Location startingLocation = new Location("starting point");
                                        startingLocation.setLatitude(oldLocationDriver.latitude);
                                        startingLocation.setLongitude(oldLocationDriver.longitude);

                                        //Get the target location
                                        Location endingLocation = new Location("ending point");
                                        endingLocation.setLatitude(location.latitude);
                                        endingLocation.setLongitude(location.longitude);

                                        bearing = startingLocation.bearingTo(endingLocation);
                                    }

                                    if(mMarker==null){
                                        mMarker = mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(location.latitude,location.longitude))
                                                .snippet(driver.getPhoneNum())
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.motobike_ver2))
                                                .title(driver.getName())
                                                .flat(true)
                                                .anchor(0.5f, 0.5f)
                                                .rotation(bearing));
                                        hashMapBearing.put(dataSnapshot.getKey(),bearing);
                                        hashMapMarker.put(dataSnapshot.getKey(),mMarker);
                                        hashMapLocation.put(dataSnapshot.getKey(),location);
                                    }
                                    Log.e("PUT",dataSnapshot.getKey()+"  into hashMapMarker");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onGeoQueryReady() {
                if(distance<=LIMIT_RANGE){
                    distance++;
                    loadAllDriverAvailable();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intentHistory = new Intent(CustomerHomeActivity.this, CustomerProfileActivity.class);
            startActivity(intentHistory);
        } else if (id == R.id.nav_history) {
            Intent intentHistory = new Intent(CustomerHomeActivity.this, CustomerHistoryActivity.class);
            startActivity(intentHistory);

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_sign_out) {
            mFirebaseAuth.signOut();
            Intent intent = new Intent(getApplicationContext(),ChooseTypeUserActivity.class);
            startActivity(intent);
            finish();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null){
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));
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
    public void onLocationChanged(Location location) {
        Common.mLastLocationCustomer = location;
        displayLocation();
    }

    @Override
    public void onPlaceSelected(Place place) {

    }

    @Override
    public void onError(Status status) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_pick_request:
                if(isChooseDropOff){
                    requestPickUpHere();
                }else{
                    final View root = pickDestination.getView();
                    root.findViewById(R.id.place_autocomplete_search_input)
                            .performClick();
                }
        }
    }

    private void requestPickUpHere() {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                new GeoLocation(Common.mLastLocationCustomer.getLatitude(),
                        Common.mLastLocationCustomer.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

        if(mUserMarker.isVisible())
            mUserMarker.remove();
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Pickup here")
                .snippet("")
                .position(new LatLng(Common.mLastLocationCustomer.getLatitude(),Common.mLastLocationCustomer.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mUserMarker.showInfoWindow();

        findDriver();
    }

    private void findDriver() {
        Intent intent = new Intent(this, CustomerFindDriverActivity.class);
        intent.putExtra("destination",mPlaceDestination.getName());
        startActivityForResult(intent,REQUEST_CODE_FIND_DRIVER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FIND_DRIVER) {
            if(resultCode == Activity.RESULT_OK){
                String driverID = data.getStringExtra("driverID");
                Intent intent = new Intent(CustomerHomeActivity.this, CustomerTrackingActivity.class);
                intent.putExtra("driverID",driverID);
                intent.putExtra("destination",mPlaceDestination.getName());
                intent.putExtra("pickup",getNameAdress(Common.mLastLocationCustomer));
                startActivity(intent);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                showNotFoundDriverDialog();
                Snackbar.make(getCurrentFocus(),"Cancel book",Snackbar.LENGTH_SHORT).show();
            }
        }
    }



    private void showNotFoundDriverDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        LayoutInflater inflater = LayoutInflater.from(this);
        View notFoundDriverLayout = inflater.inflate(R.layout.layout_not_found_driver,null);

        builder.setView(notFoundDriverLayout);
        final AlertDialog dialog;
        dialog = builder.create();

        dialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        },3000);
    }

    private String getNameAdress(Location mLastLocation) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
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
    public void onStop() {
        super.onStop();
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
        // Remove driver when driver not available
        isChooseDropOff = false;

    }

    @Override
    public void onStart() {
        buildGoogleApiClient();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstTime){
            isFirstTime = false;
        }
        else{
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                setupPermissionLocation();
            }else{
                displayShouldPromptForEnablingGPS(this);
            }
        }
    }

    private void getUserInfo() {
        customers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("phoneNum") != null)
                        mTxtPhone.setText(map.get("phoneNum").toString());

                    if (map.get("imgUrl") != null) {
                        RequestOptions options = new RequestOptions()
                                .centerCrop()
                                .placeholder(R.drawable.avavtar)
                                .error(R.drawable.avavtar)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .priority(Priority.HIGH);
                        Glide.with(getApplication()).load(map.get("imgUrl").toString()).apply(options).into(mAvatar);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    @Override
    public void networkAvailable() {
        updateFireBaseToken();
        setupGPS();
    }

    @Override
    public void networkUnavailable() {
        Toast.makeText(getApplicationContext(),"Please connect internet !!!",Toast.LENGTH_LONG).show();
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
