package com.klcn.xuant.transporter.mvp.splashScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.ChooseTypeUserActivity;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.DriverHomeActivity;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.mvp.home.CustomerHomeActivity;
import com.klcn.xuant.transporter.service.OnClearFromRecentService;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashScreenActivity extends AppCompatActivity {


    View mLayout;
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
        setContentView(R.layout.activity_splash_screen);
        mLayout = findViewById(R.id.main_layout);
//        Intent intent = new Intent(getBaseContext(),OnClearFromRecentService.class);
//        intent.putExtra("Test","Triet");
//        startService(intent);
        Thread mThread = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(1000);
//                    stopService(intent);
                    checkIsLogged();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        mThread.start();
    }

    private void checkIsLogged() {
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            FirebaseDatabase.getInstance().getReference(Common.customers_tbl)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                Intent intentHome = new Intent(SplashScreenActivity.this, CustomerHomeActivity.class);
                                startActivity(intentHome);
                                finish();
                            }else{
                                Intent intentHome = new Intent(SplashScreenActivity.this, DriverHomeActivity.class);
                                startActivity(intentHome);
                                finish();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        }else{
            Intent intent = new Intent(getApplicationContext(), ChooseTypeUserActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkIsDriverLogged() {
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                Intent intentHome = new Intent(SplashScreenActivity.this, DriverHomeActivity.class);
                                startActivity(intentHome);
                                finish();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        }else{
            Intent intent = new Intent(getApplicationContext(), ChooseTypeUserActivity.class);
            startActivity(intent);
            finish();
        }
    }


}