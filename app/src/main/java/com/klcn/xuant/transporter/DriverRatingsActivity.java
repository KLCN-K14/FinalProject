package com.klcn.xuant.transporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DriverRatingsActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.txt_accept_trip)
    TextView mTxtAcceptTrip;

    @BindView(R.id.txt_full_star_trip)
    TextView mTxtFullStarTrip;

    @BindView(R.id.txt_total_trip)
    TextView mTxtTotalTrip;

    @BindView(R.id.txt_avg_ratings)
    TextView mTxtAvgRating;

    @BindView(R.id.panel_customer_feedback)
    RelativeLayout mPanelCustomerFeedBack;

    @BindView(R.id.panel_pro_tips)
    RelativeLayout mPanelProTips;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_ratings);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab!=null)
        {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mPanelCustomerFeedBack.setOnClickListener(this);
        mPanelProTips.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.panel_customer_feedback:
                Intent intentFeedBack = new Intent(this,DriverFeedBackActivity.class);
                startActivity(intentFeedBack);
                break;
            case R.id.panel_pro_tips:
                Toast.makeText(this,"Panel pro tips",Toast.LENGTH_LONG).show();
                break;
        }
    }
}
