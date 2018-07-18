package com.klcn.xuant.transporter.mvp.history;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

public class CustomerHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    ArrayList<TripInfo> listData;
    ImageView mImgback;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference trips;
    String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_history);

        mImgback = (ImageView) findViewById(R.id.toolbar_back);
        final ListView listView = (ListView) findViewById(R.id.list_history);

        mFirebaseAuth = FirebaseAuth.getInstance();
        trips = FirebaseDatabase.getInstance().getReference().child(Common.trip_info_tbl);

        customerId = mFirebaseAuth.getCurrentUser().getUid();
        Query query = trips.orderByChild("customerId").equalTo(customerId);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        listData = new ArrayList<>();


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listData.clear();
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    TripInfo trip = item.getValue(TripInfo.class);
//                    if(trip.getStatus().equals(Common.trip_info_status_complete)
//                            && trip.getCustomerId().equals(customerId)){
//                        trip.setKey(item.getKey());
//                    }
                    listData.add(trip);


                }
                Collections.reverse(listData);
                final ListHistoryAdapter listHistoryAdapter = new ListHistoryAdapter(getApplicationContext(), listData);
                listView.setAdapter(listHistoryAdapter);
                listHistoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CustomerHistoryActivity.this, ItemHistoryActivity.class);
                if (listData.get(position).getRating() != null)
                    intent.putExtra("rating", listData.get(position).getRating());

                else
                    intent.putExtra("rating", 0);
                int total=0;
                if(listData.get(position).getFixedFare()!=null && listData.get(position).getOtherToll()!=null)
                    total = Integer.parseInt(listData.get(position).getFixedFare()) + Integer.parseInt(listData.get(position).getOtherToll());
                else if(listData.get(position).getOtherToll()==null)
                    total = Integer.parseInt(listData.get(position).getFixedFare());

                intent.putExtra("pickup", listData.get(position).getPickup());
                intent.putExtra("dropoff", listData.get(position).getDropoff());
                intent.putExtra("feedback", listData.get(position).getFeedback());
                intent.putExtra("total", total+"đ");
                intent.putExtra("time", DateFormat.format("HH:mm", listData.get(position).getDateCreated()));
                intent.putExtra("driverId", listData.get(position).getDriverId());

                startActivity(intent);
            }
        });

        mImgback.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_back:
                finish();
                break;
        }
    }

}
