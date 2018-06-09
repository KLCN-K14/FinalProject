package com.klcn.xuant.transporter;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.mvp.home.CustomerHomeActivity;
import com.klcn.xuant.transporter.mvp.signup.view.CustomerSignUpActivity;
import com.klcn.xuant.transporter.receiver.NetworkStateReceiver;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChooseTypeUserActivity extends AppCompatActivity implements View.OnClickListener,
        NetworkStateReceiver.NetworkStateReceiverListener{

    private NetworkStateReceiver networkStateReceiver;

    @BindView(R.id.btn_login_customer)
    Button mBtnCustomer;

    @BindView(R.id.btn_login_driver)
    Button mBtnDriver;

    Boolean isConnect = false;

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
        setContentView(R.layout.activity_choose_type_user);
        ButterKnife.bind(this);

        mBtnDriver.setOnClickListener(this);
        mBtnCustomer.setOnClickListener(this);

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        getApplicationContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

    }

    @Override
    public void onClick(View view) {
        if(isConnect){
            switch (view.getId()){
                case R.id.btn_login_customer:

                    Intent intentCustomer = new Intent(getApplicationContext(), CustomerSignUpActivity.class);
                    startActivity(intentCustomer);
//                finish();
                    break;
                case R.id.btn_login_driver:

                    Intent intentDriver = new Intent(getApplicationContext(), DriverLoginActivity.class);
                    startActivity(intentDriver);
//                finish();
                    break;

            }
        }else{
            Toast.makeText(getApplicationContext(),"Please connect internet",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void networkAvailable() {
        isConnect = true;
    }

    @Override
    public void networkUnavailable() {
        isConnect = false;
        Toast.makeText(getApplicationContext(),"Please connect internet",Toast.LENGTH_LONG).show();
    }
}
