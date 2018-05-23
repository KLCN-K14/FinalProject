package com.klcn.xuant.transporter;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DriverEarningsFragment extends Fragment implements View.OnClickListener{

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

    public static DriverEarningsFragment newInstance() {
        DriverEarningsFragment fragment = new DriverEarningsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_earnings, container, false);
        ButterKnife.bind(this,view);

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
                R.color.colorActiveNavigation},getActivity());
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

        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.panel_invites:
                Snackbar.make(getView(),"Panel trip invites",Snackbar.LENGTH_LONG).show();
                break;
            case R.id.panel_pay_statement:
                Snackbar.make(getView(),"Panel pay statement",Snackbar.LENGTH_LONG).show();
                break;
            case R.id.panel_trip_history:
                Snackbar.make(getView(),"Panel trip history",Snackbar.LENGTH_LONG).show();
                break;
            default: break;
        }
    }
}
