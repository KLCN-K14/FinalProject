package com.klcn.xuant.transporter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.receiver.NetworkStateReceiver;
import com.suke.widget.SwitchButton;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DriverMainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener{

//    @BindView(R.id.navigation)
    private NetworkStateReceiver networkStateReceiver;
    private static Context context;
    static boolean isFirstTime = true;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;

    public static BottomNavigationView mBottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedFragment = DriverHomeFragment.newInstance();
                    break;
                case R.id.navigation_earnings:
                    selectedFragment = DriverEarningsFragment.newInstance();
                    break;
                case R.id.navigation_ratings:
                    selectedFragment = DriverRatingsFragment.newInstance();
                    break;
                case R.id.navigation_account:
                    selectedFragment = DriverAccountFragment.newInstance();
                    break;
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, selectedFragment);
            transaction.commit();
            return true;
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_driver_main);
        ButterKnife.bind(this);

        mBottomNavigationView = findViewById(R.id.navigation);

        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, DriverHomeFragment.newInstance());
        transaction.commit();

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        getContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        setupGPS();
        disableShiftMode(mBottomNavigationView);
    }

    public static Context getContext() {
        return context;
    }

    private void setupGPS() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            setupPermissionLocation();
        }else{
            displayPromptForEnablingGPS(this);
        }
    }

    // setup permission
    private void setupPermissionLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
            //Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }else{
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, MY_PERMISSION_REQUEST_CODE);
                }
        }
    }


    @SuppressLint("RestrictedApi")
    private void disableShiftMode(BottomNavigationView mBottomNavigationView) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) mBottomNavigationView.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }

    @Override
    public void networkAvailable() {
    }

    @Override
    public void networkUnavailable() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstTime){
            isFirstTime = false;
        }
        else{
            LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                setupPermissionLocation();
            }else{
                displayShouldPromptForEnablingGPS(this);
            }
        }
    }
}
