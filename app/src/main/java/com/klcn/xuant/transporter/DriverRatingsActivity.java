package com.klcn.xuant.transporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.TripInfo;
import com.klcn.xuant.transporter.mvp.feedbackDriver.DriverFeedBackActivity;

import java.util.ArrayList;

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

    String driverID = "";
    Driver mDriver;
    ArrayList<TripInfo> tripInfos;


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

        tripInfos = new ArrayList<>();
        mDriver = new Driver();
        driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupInit();
    }

    private void setupInit() {
        FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                .child(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            mDriver = dataSnapshot.getValue(Driver.class);
                            getTripInfos();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getTripInfos() {
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
                mTxtAvgRating.setText(mDriver.getAvgRatings().replace(",","."));
                mTxtTotalTrip.setText(String.valueOf(tripInfos.size()));
                mTxtAcceptTrip.setText(getCompletedTrip());
                mTxtFullStarTrip.setText(getTripFullStar());
                mQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getTripFullStar() {
        int count = 0;
        if(tripInfos!=null){
            for(int i=0;i<tripInfos.size();i++){
                if(tripInfos.get(i).getStatus().equals(Common.trip_info_status_complete))
                {
                    if(tripInfos.get(i).getRating().equals("5"))
                        count++;
                }
            }
        }

        return String.valueOf(count);
    }

    private String getCompletedTrip() {
        int count = 0;

        if(tripInfos!=null){
            for(int i=0;i<tripInfos.size();i++){
                if(tripInfos.get(i).getStatus().equals(Common.trip_info_status_complete))
                    count++;
            }
        }
        return String.valueOf(count);
    }

//    private String getRating() {
//        Double currentBase = 0.;
//        if(mDriver!=null)
//            currentBase = Double.valueOf(mDriver.getAvgRatings());
//        Double sumRating = 0.;
//        int count = 0;
//        if(tripInfos!=null){
//            for(int i=0;i<tripInfos.size();i++){
//                if(tripInfos.get(i).getStatus().equals(Common.trip_info_status_complete)){
//                    sumRating+=Double.valueOf(tripInfos.get(i).getRating());
//                    count++;
//                }
//            }
//        }
//
//
//        if(count!=0){
//            sumRating = sumRating/count;
//            currentBase = (currentBase+sumRating)/2;
//        }
//
//        return String.format("%.1f", currentBase);
//    }

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
