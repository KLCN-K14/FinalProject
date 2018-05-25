package com.klcn.xuant.transporter.mvp.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
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
import android.widget.TextView;
import android.widget.Toast;

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
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Customer;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.mvp.findDriver.CustomerFindDriverActivity;
import com.klcn.xuant.transporter.helper.CustomInfoWindow;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.mvp.history.CustomerHistoryActivity;
import com.klcn.xuant.transporter.mvp.profile.CustomerProfileActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CustomerHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, PlaceSelectionListener,
        View.OnClickListener{


    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
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

    @BindView(R.id.btn_pick_request)
    Button btnPickRequest;

    boolean isChooseDropOff = false;
    PlaceAutocompleteFragment pickDestination;

    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    TextView mTxtPhone;

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

        customers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                customerModel = dataSnapshot.getValue(Customer.class);
//                mTxtPhone.setText(customerModel.getPhoneNum());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

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

        setupLocation();

    }

    private void initView(){

        //Vị trí của bạn
        PlaceAutocompleteFragment autocompleteFragmentPlace = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_location);
        autocompleteFragmentPlace.setOnPlaceSelectedListener(this);
        autocompleteFragmentPlace.setText("Your position");
        ((View)findViewById(R.id.place_autocomplete_search_button)).setVisibility(View.GONE);
        ((View)findViewById(R.id.place_autocomplete_clear_button)).setVisibility(View.GONE);
        autocompleteFragmentPlace.setOnPlaceSelectedListener(new PlaceSelectionListener() {
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


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayService()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
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
    private void setupLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            //Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }else{
            if(checkPlayService()){
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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);

    }

    private void displayLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation!=null){
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();

            //Add marker
            if(mUserMarker!=null)
                mUserMarker.remove();
            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_vitri))
                    .position(new LatLng(latitude,longitude))
                    .title("You"));
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

        GeoQuery geoQuery = gfDriverAvailable.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),distance);
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

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude,location.longitude))
                                        .snippet(driver.getPhoneNum())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.motobike_ver2))
                                        .title(driver.getName())
                                        .flat(true));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

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
        mLastLocation = location;
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
                new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

        if(mUserMarker.isVisible())
            mUserMarker.remove();
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Pickup here")
                .snippet("")
                .position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mUserMarker.showInfoWindow();

        findDriver();
    }

    private void findDriver() {
        Intent intent = new Intent(this, CustomerFindDriverActivity.class);
        intent.putExtra("lat",mLastLocation.getLatitude());
        intent.putExtra("lng",mLastLocation.getLongitude());
        intent.putExtra("destination",mPlaceDestination.getName());
        startActivityForResult(intent,REQUEST_CODE_FIND_DRIVER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FIND_DRIVER) {
            if(resultCode == Activity.RESULT_OK){
                String driverID = data.getStringExtra("driverID");
                if(driverID.equals("0")){ // don't have driver
                    Toast.makeText(getApplicationContext(),"Don't have any driver",Toast.LENGTH_LONG).show();
                }else{

                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
            }
        }
    }

//    private void getDirection(double lat, double lng) {
//        String requestApi = null;
//        try{
//            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
//                    "mode=driving&"+
//                    "transit_routing_preference=less_driving&"+
//                    "origin="+ mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&"+
//                    "destination="+mPlaceDestination.getLatLng().latitude+","+mPlaceDestination.getLatLng().longitude+"&"+
//                    "key="+getResources().getString(R.string.google_direction_api);
//            Log.e("TRANSPORT",requestApi);
//            mService.getPath(requestApi)
//                    .enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(Call<String> call, Response<String> response) {
//                            try {
//                                JSONObject jsonObject = new JSONObject(response.body().toString());
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<String> call, Throwable t) {
//
//                        }
//                    });
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    @Override
    protected void onStop() {
        super.onStop();
        isChooseDropOff = false;
        btnPickRequest.setText("CHOOSE YOUR DROP-OFF");
    }
}
