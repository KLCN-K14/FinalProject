package com.klcn.xuant.transporter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.model.TripInfo;
import com.klcn.xuant.transporter.mvp.history.CustomerHistoryActivity;
import com.klcn.xuant.transporter.mvp.profile.CustomerProfileActivity;
import com.klcn.xuant.transporter.receiver.NetworkStateReceiver;
import com.suke.widget.SwitchButton;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class DriverHomeActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,NetworkStateReceiver.NetworkStateReceiverListener {

    TextView mTxtNameDriver,mTxtNameService;
    CircleImageView mAvatar;

    private NetworkStateReceiver networkStateReceiver;
    private GoogleMap mMap;


    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private static final int REQUEST_HAVE_TRIP = 7002;
    private static final int REQUEST_ACTIVITY_ACCOUNT = 7003;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTERVAL = 2000;
    private static int FASTEST_INTERVAL = 1000;
    private static int DISPLACEMENT = 10;

    DatabaseReference driverAvailable;
    GeoFire mGeoFire;

    float bearing = 0;
    Location oldLocation;

    String driverID;

    boolean isFirstTime = true;
    static boolean onTrip = false;

    Marker mMarker;

    @BindView(R.id.switch_button)
    com.suke.widget.SwitchButton mSwitchButton;

    @BindView(R.id.txt_status)
    TextView mTxtStatus;

    @BindView(R.id.root_layout)
    CoordinatorLayout mRoot;
    TextView mTxtPercentAcceptance,mTxtPercentCancellation,mTxtRatings,
            mTxtNameServiceBottom,mTxtNameCarBottom,mTxtLicensePlateBottom,
            mTxtAcceptBottom,mTxtCancelBottom;
    BottomSheetBehavior bottomSheetBehavior;

    Driver mDriver;
    ArrayList<TripInfo> tripInfos;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_driver_home);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab!=null)
        {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        mTxtNameDriver = (TextView) header.findViewById(R.id.txt_name_driver);
        mTxtNameService = (TextView) header.findViewById(R.id.txt_name_service);
        mAvatar = (CircleImageView) header.findViewById(R.id.img_avatar_nav);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {
                    Snackbar.make(mRoot, "You are online", Snackbar.LENGTH_SHORT).show();
                    mTxtStatus.setText("ONLINE");
                    mTxtStatus.setTextColor(getResources().getColor(R.color.colorActiveNavigation));
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    updateOnline();
                    startLocationUpdate();
                    displayLocation();
                } else {
                    Snackbar.make(mRoot, "You are offline", Snackbar.LENGTH_SHORT).show();
                    mTxtStatus.setText("OFFLINE");
                    mTxtStatus.setTextColor(getResources().getColor(R.color.white));
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    stopLocationUpdate();
                    if (mMarker != null) {
                        mMarker.remove();
                        updateOffline();
                    }

                }
            }
        });
        mDriver = new Driver();
        tripInfos = new ArrayList<>();

        driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getInfoDriver();
        getTripInfo();

        final View llBottomSheet = (View) findViewById(R.id.bottom_sheet_driver);

        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        mTxtPercentAcceptance = llBottomSheet.findViewById(R.id.txt_percent_accecpt);
        mTxtRatings = llBottomSheet.findViewById(R.id.txt_rating);
        mTxtPercentCancellation = llBottomSheet.findViewById(R.id.txt_percent_cancellation);
        mTxtNameServiceBottom = llBottomSheet.findViewById(R.id.txt_name_service);
        mTxtLicensePlateBottom = llBottomSheet.findViewById(R.id.txt_license_plate);
        mTxtNameCarBottom = llBottomSheet.findViewById(R.id.txt_name_car);
        mTxtAcceptBottom = llBottomSheet.findViewById(R.id.txt_percent_accecpt);
        mTxtCancelBottom = llBottomSheet.findViewById(R.id.txt_percent_cancellation);

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void getTripInfo() {
        tripInfos = new ArrayList<>();
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
        final Query mQuery = mData.orderByChild("driverId").equalTo(driverID);
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    TripInfo tripInfo = item.getValue(TripInfo.class);
                    tripInfo.setKey(item.getKey());
                    tripInfos.add(tripInfo);
                }
                mTxtAcceptBottom.setText(getAcceptTrip());
                mTxtCancelBottom.setText(getCancelTrip());
                mQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getCancelTrip() {
        int count = 0, countCompleted = 0;
        for(int i=0;i<tripInfos.size();i++){
            if(tripInfos.get(i).getStatus().equals(Common.trip_info_status_complete)
                    || tripInfos.get(i).getStatus().equals(Common.trip_info_status_driver_cancel)){
                count++;
                if(tripInfos.get(i).equals(Common.trip_info_status_driver_cancel))
                    countCompleted++;
            }
        }

        if(count==0)
            return "0%";

        Double percent = Double.valueOf(countCompleted/count);
        return percent.intValue()+"%";
    }

    private String getAcceptTrip() {
        int count = 0, countCompleted = 0;
        for(int i=0;i<tripInfos.size();i++){
            if(tripInfos.get(i).equals(Common.trip_info_status_complete))
                countCompleted++;
            if(tripInfos.get(i).equals(Common.trip_info_status_driver_cancel) ||
                    tripInfos.get(i).equals(Common.trip_info_status_complete))
                count++;
        }
        if(count==0)
            return "100%";

        Double percent = Double.valueOf(countCompleted/count);
        return percent.intValue()+"%";
    }

    private void getInfoDriver(){
        FirebaseDatabase.getInstance().getReference(Common.drivers_tbl).child(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            mDriver = dataSnapshot.getValue(Driver.class);
                            mTxtNameDriver.setText(mDriver.getName().toUpperCase());
                            mTxtNameService.setText(mDriver.getServiceVehicle());
                            mTxtNameServiceBottom.setText(mDriver.getServiceVehicle());
                            mTxtLicensePlateBottom.setText(mDriver.getLicensePlate().toUpperCase());
                            mTxtNameCarBottom.setText(mDriver.getNameVehicle().toUpperCase());
                            Double rating = Double.valueOf(mDriver.getAvgRatings().replace(",","."));
                            mTxtRatings.setText(String.format("%.1f",rating));
                            RequestOptions options = new RequestOptions()
                                    .centerCrop()
                                    .placeholder(R.drawable.avavtar)
                                    .error(R.drawable.avavtar)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .priority(Priority.HIGH);
                            Glide.with(getApplication()).load(mDriver.getImgUrl()).apply(options).into(mAvatar);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void updateOffline() {
        HashMap<String,Object> maps = new HashMap<>();
        maps.put("isOnline",false);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .updateChildren(maps);

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.driver_available_tbl);

            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                }
            });
        }

    }

    private void updateOnline() {
        HashMap<String,Object> maps = new HashMap<>();
        maps.put("isOnline",true);
        FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(maps);
    }


    private void updateFireBaseToken() {
        DatabaseReference dbToken = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        dbToken.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void stopLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }catch (Exception e){
            e.printStackTrace();
        }
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
        try {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                    GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
                else {
                    Toast.makeText(getApplicationContext(), "This device is not supported", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        }catch (NullPointerException e){
            e.printStackTrace();
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
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                FirebaseDatabase.getInstance().getReference(Common.driver_working_tbl)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.exists() && mGeoFire!=null){
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
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        } else {
            Log.e("ERROR", "Can't get your location");
        }
    }

    // Update location driver
    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_HAVE_TRIP){
            onTrip = false;
        }else{
            if(requestCode == REQUEST_ACTIVITY_ACCOUNT){
                if(resultCode == RESULT_OK){
                    updateOffline();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getApplicationContext(), ChooseTypeUserActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
        // Remove driver when driver not available
        updateOffline();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
        // Remove driver when driver not available
        updateOffline();

    }

    @Override
    public void onStart() {
        buildGoogleApiClient();
        super.onStart();
    }

    @Override
    public void networkAvailable() {
        Log.e("networkAvailable","networkAvailable");
        driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //Geo Fire
        driverAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_available_tbl);
        mGeoFire = new GeoFire(driverAvailable);
        if(mSwitchButton.isChecked())
            updateOnline();
        else
            updateOffline();
        updateFireBaseToken();
        setupGPS();
    }

    @Override
    public void networkUnavailable() {
        Toast.makeText(this,"Please connect internet !!!",Toast.LENGTH_LONG).show();
        updateOffline();
    }


    @Override
    public void onResume() {
        super.onResume();

        getTripInfo();

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

        if(mSwitchButton.isChecked())
            updateOnline();
        else
            updateOffline();

        FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if(dataSnapshot.getKey().equals(driverID) && !onTrip){
                            onTrip = true;
                            mSwitchButton.setChecked(false);
                            stopLocationUpdate();
                            if (mMarker != null) {
                                mMarker.remove();
                                updateOffline();
                            }
                            Snackbar.make(mRoot, "You are offline", Snackbar.LENGTH_SHORT).show();
                            mTxtStatus.setText("OFFLINE");
                            Handler handler = new Handler();

                            FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl).removeEventListener(this);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(getApplicationContext(),DriverTrackingAcitivity.class);
                                    intent.putExtra("keyTrip",
                                            FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl).push().getKey());
                                    startActivityForResult(intent,REQUEST_HAVE_TRIP);
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

        if (id == R.id.navigation_home) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.navigation_earnings) {
            Intent intentHistory = new Intent(DriverHomeActivity.this, DriverEarningsActivity.class);
            startActivity(intentHistory);

        } else if (id == R.id.navigation_ratings) {
            Intent intentHistory = new Intent(DriverHomeActivity.this, DriverRatingsActivity.class);
            startActivity(intentHistory);
        } else if (id == R.id.navigation_account) {
            Intent intentHistory = new Intent(DriverHomeActivity.this, DriverAccountActivity.class);
            startActivityForResult(intentHistory,REQUEST_ACTIVITY_ACCOUNT);
        }else if (id == R.id.nav_sign_out) {
            updateOffline();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), ChooseTypeUserActivity.class);
                startActivity(intent);
                finish();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
