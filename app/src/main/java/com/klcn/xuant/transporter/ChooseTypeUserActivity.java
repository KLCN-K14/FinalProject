package com.klcn.xuant.transporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.klcn.xuant.transporter.mvp.signup.view.CustomerSignUpActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChooseTypeUserActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.btn_login_customer)
    Button mBtnCustomer;

    @BindView(R.id.btn_login_driver)
    Button mBtnDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_type_user);
        ButterKnife.bind(this);

        mBtnDriver.setOnClickListener(this);
        mBtnCustomer.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login_customer:

                Intent intentCustomer = new Intent(getApplicationContext(), CustomerSignUpActivity.class);
                startActivity(intentCustomer);
                finish();

                break;
            case R.id.btn_login_driver:

                Intent intentDriver = new Intent(getApplicationContext(), DriverLoginActivity.class);
                startActivity(intentDriver);
                finish();

                break;

        }
    }
}