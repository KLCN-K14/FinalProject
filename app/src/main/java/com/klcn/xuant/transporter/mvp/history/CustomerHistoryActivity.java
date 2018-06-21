package com.klcn.xuant.transporter.mvp.history;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class CustomerHistoryActivity extends AppCompatActivity implements View.OnClickListener{

    ArrayList<TripInfo> listData;
    ImageView mImgback;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference trips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_history);

        mImgback = (ImageView) findViewById(R.id.toolbar_back);
        final ListView listView = (ListView) findViewById(R.id.list_history);

        mFirebaseAuth = FirebaseAuth.getInstance();
        trips = FirebaseDatabase.getInstance().getReference().child(Common.trip_info_tbl);
        Query query = trips.orderByChild("customerId").equalTo(mFirebaseAuth.getCurrentUser().getUid());

        listData= new ArrayList<>();



        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listData.clear();
                for (DataSnapshot item : dataSnapshot.getChildren())
                {
                    TripInfo trip = item.getValue(TripInfo.class);
                    listData.add(trip);

                }
                final ListHistoryAdapter listHistoryAdapter = new ListHistoryAdapter(getApplicationContext(),listData );
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
                intent.putExtra("POSITION", position);
                startActivity(intent);
            }
        });

        mImgback.setOnClickListener(this);



    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.toolbar_back:
                finish();
                break;
        }
    }

}
