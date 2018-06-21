package com.klcn.xuant.transporter;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.TripInfo;
import com.klcn.xuant.transporter.mvp.tripHistoryDriver.DriverTripHistoryActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.klcn.xuant.transporter.utils.DateUtils.isDateInCurrentWeek;

public class DriverEarningsActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.barchart)
    BarChart mBarChart;

    @BindView(R.id.txt_payout_week)
    TextView mTxtPayoutWeek;

    @BindView(R.id.txt_last_trip_price)
    TextView mTxtLastTripPrice;

    @BindView(R.id.txt_total_payout)
    TextView mTxtTotalPayout;

    @BindView(R.id.panel_invites)
    RelativeLayout mPanelInvites;

    @BindView(R.id.panel_pay_statement)
    RelativeLayout mPanelPayStatement;

    @BindView(R.id.panel_trip_history)
    RelativeLayout mPanelTripHistory;

    private BarData mBarData;
    private BarDataSet mBarDataSet;
    ArrayList<BarEntry> mListBarEntry;
    ArrayList<String> mListEntryLabel;

    String driverID = "";
    ArrayList<TripInfo> tripInfos;

    public static DriverEarningsActivity newInstance() {
        DriverEarningsActivity fragment = new DriverEarningsActivity();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_earnings);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab!=null)
        {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mListBarEntry = new ArrayList<>();
        mListEntryLabel = new ArrayList<>();


        mPanelInvites.setOnClickListener(this);
        mPanelTripHistory.setOnClickListener(this);
        mPanelPayStatement.setOnClickListener(this);

        driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tripInfos = new ArrayList<>();
        setupInit();

    }

    private void setupInit() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
        final Query mQuery = mData.orderByChild("driverId").equalTo(driverID);
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    TripInfo tripInfo = item.getValue(TripInfo.class);
                    if(tripInfo.getStatus().equals(Common.trip_info_status_complete)){
                        tripInfo.setKey(item.getKey());
                        tripInfos.add(tripInfo);
                    }
                }
                setupChart();
                mTxtPayoutWeek.setText(getPayoutWeek());
                mTxtTotalPayout.setText(getTotalPayout());
                mTxtLastTripPrice.setText(getLastTripPrice());
                mQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getLastTripPrice() {
        TripInfo tripMax = new TripInfo();
        if(!tripInfos.isEmpty()){
            tripMax = tripInfos.get(0);
        }else
            return "";
        for(int i=1;i<tripInfos.size();i++){
            if(tripInfos.get(i).getDateCreated()>tripMax.getDateCreated()){
                tripMax = tripInfos.get(i);
            }
        }
        Double lastTrip = Double.valueOf(tripMax.getFixedFare())/1000;
        return "Last trip: VND "+String.valueOf(lastTrip.intValue())+"K";
    }

    private void setupChart() {
        mListBarEntry.add(new BarEntry(getValues(1),0));
        mListBarEntry.add(new BarEntry(getValues(2),1));
        mListBarEntry.add(new BarEntry(getValues(3),2));
        mListBarEntry.add(new BarEntry(getValues(4),3));
        mListBarEntry.add(new BarEntry(getValues(5),4));
        mListBarEntry.add(new BarEntry(getValues(6),5));
        mListBarEntry.add(new BarEntry(getValues(7),6));
        mBarDataSet =new BarDataSet(mListBarEntry,"VND");
        mBarDataSet.setColors(new int[] {R.color.colorActiveNavigation,R.color.colorActiveNavigation,R.color.colorActiveNavigation,
                R.color.colorActiveNavigation,R.color.colorActiveNavigation,R.color.colorActiveNavigation,
                R.color.colorActiveNavigation},this);
        mBarDataSet.setValueTextColor(Color.rgb(255, 255, 83));
        mBarDataSet.setValueTextSize(10);
        mListEntryLabel.add("Mon");
        mListEntryLabel.add("Tue");
        mListEntryLabel.add("Wed");
        mListEntryLabel.add("Thu");
        mListEntryLabel.add("Fri");
        mListEntryLabel.add("Sat");
        mListEntryLabel.add("Sun");

        mBarData = new BarData(mListEntryLabel,mBarDataSet);

        mBarChart.setData(mBarData);
        mBarChart.setBackgroundColor(getResources().getColor(R.color.colorNavigation));
        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setTextColor(getResources().getColor(android.R.color.white));

        YAxis xLeftAxis = mBarChart.getAxisLeft();
        xLeftAxis.setTextColor(getResources().getColor(android.R.color.white));

        YAxis xRightAxis = mBarChart.getAxisRight();
        xRightAxis.setTextColor(getResources().getColor(R.color.colorNavigation));

        Legend legend = mBarChart.getLegend();
        legend.setEnabled(false);
        mBarChart.setDescription("");
        mBarChart.setDrawValueAboveBar(true);




    }

    private String getPayoutWeek() {
        ArrayList<TripInfo> list = new ArrayList<>();
        for(int i=0;i<tripInfos.size();i++){
            Calendar calendarTemp = Calendar.getInstance();
            calendarTemp.setTimeInMillis(tripInfos.get(i).getDateCreated());
            if(isDateInCurrentWeek(calendarTemp.getTime())){
                list.add(tripInfos.get(i));
            }
        }

        Double sum = 0.;
        for(int i=0;i<list.size();i++){
            sum+= Float.valueOf(list.get(i).getFixedFare())/1000;
        }

        return "VND "+String.valueOf(sum.intValue())+"K";
    }

    private String getTotalPayout() {
        Double sum = 0.;
        for(int i=0;i<tripInfos.size();i++){
            sum+= Float.valueOf(tripInfos.get(i).getFixedFare())/1000;
        }

        return "Total payout: VND "+String.valueOf(sum.intValue())+"K";
    }

    // get value for day
    private float getValues(int day) {
        ArrayList<TripInfo> list = new ArrayList<>();
        for(int i=0;i<tripInfos.size();i++){
            Calendar calendarTemp = Calendar.getInstance();
            calendarTemp.setTimeInMillis(tripInfos.get(i).getDateCreated());
            if(calendarTemp.getTime().getDay()==day && isDateInCurrentWeek(calendarTemp.getTime())){
                list.add(tripInfos.get(i));
            }
        }

        float sum = 0;
        for(int i=0;i<list.size();i++){
            sum+= Float.valueOf(list.get(i).getFixedFare())/1000;
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        return Float.valueOf(decimalFormat.format(sum));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.panel_invites:
                Intent intentInvites = new Intent(this,DriverInvitesActivity.class);
                startActivity(intentInvites);
                break;
            case R.id.panel_pay_statement:
                Intent intentPayStatement = new Intent(this,DriverPayStatementActivity.class);
                startActivity(intentPayStatement);
                break;
            case R.id.panel_trip_history:
                Intent intentTripHistory = new Intent(this,DriverTripHistoryActivity.class);
                startActivity(intentTripHistory);
                break;
            default: break;
        }
    }
}
