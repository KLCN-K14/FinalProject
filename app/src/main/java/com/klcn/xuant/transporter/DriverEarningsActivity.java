package com.klcn.xuant.transporter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

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


        mListBarEntry.add(new BarEntry(20f,0));
        mListBarEntry.add(new BarEntry(50f,1));
        mListBarEntry.add(new BarEntry(100f,2));
        mListBarEntry.add(new BarEntry(70f,3));
        mListBarEntry.add(new BarEntry(20f,4));
        mListBarEntry.add(new BarEntry(30f,5));
        mListBarEntry.add(new BarEntry(30f,6));
        mBarDataSet =new BarDataSet(mListBarEntry,"");
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
