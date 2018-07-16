package com.klcn.xuant.transporter.mvp.tripHistoryDriver;

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
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DriverTripHistoryActivity extends AppCompatActivity {

    @BindView(R.id.list_history)
    RecyclerView mList;

    String driverID;
    ArrayList<TripInfo> tripInfos;

    ItemTripHistoryAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_trip_history);
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
                new LinearLayoutManager(DriverTripHistoryActivity.this);
        mList.setLayoutManager(layoutManager);
        mList.setHasFixedSize(true);
        setupInit();
    }

    private void setupInit() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
        final Query mQuery = mData.orderByChild("dateCreated");
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    TripInfo tripInfo = item.getValue(TripInfo.class);
                    if(tripInfo.getStatus()!=null){
                        if(tripInfo.getStatus().equals(Common.trip_info_status_complete) &&
                                tripInfo.getDriverId().equals(driverID)){
                            tripInfo.setKey(item.getKey());
                            tripInfos.add(tripInfo);
                        }
                    }
                }
                Collections.reverse(tripInfos);
                mAdapter = new ItemTripHistoryAdapter(getApplicationContext(), tripInfos);
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
