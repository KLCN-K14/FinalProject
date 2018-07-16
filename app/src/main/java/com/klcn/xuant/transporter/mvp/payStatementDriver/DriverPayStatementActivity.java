package com.klcn.xuant.transporter.mvp.payStatementDriver;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.TripInfo;
import com.klcn.xuant.transporter.mvp.tripHistoryDriver.DriverTripHistoryActivity;
import com.klcn.xuant.transporter.mvp.tripHistoryDriver.ItemTripHistoryAdapter;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DriverPayStatementActivity extends AppCompatActivity{

    @BindView(R.id.txt_total_payout)
    TextView mTxtTotalPayout;

    @BindView(R.id.list_trip)
    RecyclerView mList;

    String driverID = "";
    ItemPayStatementAdapter mAdapter;

    ArrayList<TripInfo> tripInfos;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_pay_statement);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab!=null)
        {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tripInfos = new ArrayList<>();

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(DriverPayStatementActivity.this);
        mList.setLayoutManager(layoutManager);
        mList.setHasFixedSize(true);

        mTxtTotalPayout.setText(getIntent().getStringExtra("totalPayout"));

        setupInit();
    }

    private void setupInit() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
        final Query mQuery = mData.orderByChild("dateCreated");
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    TripInfo tripInfo = item.getValue(TripInfo.class);
                    if (tripInfo.getStatus() != null) {
                        if (tripInfo.getStatus().equals(Common.trip_info_status_complete) &&
                                tripInfo.getDriverId().equals(driverID)) {
                            tripInfo.setKey(item.getKey());
                            tripInfos.add(tripInfo);
                        }
                    }
                }
                Collections.reverse(tripInfos);
                mAdapter = new ItemPayStatementAdapter(getApplicationContext(), tripInfos);
                mList.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

                mQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
