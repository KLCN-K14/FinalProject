package com.klcn.xuant.transporter.mvp.tripHistoryDriver;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.OnLoadMoreListener;
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

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.scrollView)
    ScrollView mScrollView;

    String driverID;
    ArrayList<TripInfo> tripInfos, listTripToAdapter;


    ItemTripHistoryAdapter mAdapter;
    TripInfo tripTemp;
    int position = 0;

    @TargetApi(Build.VERSION_CODES.M)
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
        listTripToAdapter = new ArrayList<>();


        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);


        mList.setLayoutManager(llm);
        mList.setHasFixedSize(true);

        mProgressBar.setVisibility(View.GONE);

        setupInit();

    }

    String endAt = "";
    long lenghtList = 0, countItem = 0;
    int numLoad = 5;

    private void setupInit() {
        getLongList();

        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
        final Query mQuery = mData.orderByChild("driverId").equalTo(driverID).limitToLast(numLoad);
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    TripInfo tripInfo = item.getValue(TripInfo.class);
                    if(tripInfo.getStatus()!=null){
                        if(tripInfo.getDriverId().equals(driverID)){
                            tripInfo.setKey(item.getKey());
                            listTripToAdapter.add(tripInfo);
                            countItem++;
                        }
                    }
                }

                Collections.reverse(listTripToAdapter);
                if(!listTripToAdapter.isEmpty())
                    endAt = listTripToAdapter.get(listTripToAdapter.size()-1).getKey();

                mAdapter = new ItemTripHistoryAdapter(mList,getApplicationContext(), listTripToAdapter);
                mList.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

                mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        if(countItem<lenghtList){
                            listTripToAdapter.add(null);
                            mAdapter.notifyItemInserted(listTripToAdapter.size() - 1);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                listTripToAdapter.remove(listTripToAdapter.size() - 1);
                                mAdapter.notifyItemRemoved(listTripToAdapter.size());

                                getMoreData();

                                }
                            }, 3000);
                        }
                    }
                });

                mQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLongList() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
        final Query mQuery = mData.orderByChild("driverId").equalTo(driverID);
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    lenghtList = dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMoreData() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
        final Query mQuery = mData.orderByChild("driverId").equalTo(driverID).limitToLast((int)countItem+numLoad);
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count= 0;
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    TripInfo tripInfo = item.getValue(TripInfo.class);
                    if (item.getKey().equals(endAt)) {
                        break;
                    } else if (tripInfo.getStatus() != null) {
                        if (tripInfo.getDriverId().equals(driverID)) {
                            tripInfo.setKey(item.getKey());
                            tripInfos.add(tripInfo);
                            countItem++;
                            count++;
                        }
                    }
                }

                Collections.reverse(tripInfos);
                if(!tripInfos.isEmpty())
                    endAt = tripInfos.get(tripInfos.size()-1).getKey();
                for(int i= 0 ; i<count;i++){
                    listTripToAdapter.add(tripInfos.get(i));
                }
                tripInfos = new ArrayList<>();

                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();


                mQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
