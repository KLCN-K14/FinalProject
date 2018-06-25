package com.klcn.xuant.transporter.mvp.invitesDriver;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.TripInfo;
import com.klcn.xuant.transporter.mvp.feedbackDriver.DriverFeedBackActivity;
import com.klcn.xuant.transporter.mvp.feedbackDriver.ItemFeedBackAdapter;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DriverInvitesActivity extends AppCompatActivity{

    @BindView(R.id.txt_invite_code)
    TextView mTxtInviteCode;

    RecyclerView mList;
    TextView mTxtNobody;
    ImageView mImgOpen;
    RelativeLayout mRltOpen;

    BottomSheetBehavior bottomSheetBehavior;

    String driverID = "";
    ArrayList<Driver> drivers;
    ItemInvitesAdapter mAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_invites);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab!=null)
        {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        drivers = new ArrayList<>();
        mTxtInviteCode.setText(driverID.substring(0,10));

        final View llBottomSheet = (View) findViewById(R.id.bottom_list_invites);

        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        mList = llBottomSheet.findViewById(R.id.list_invites);
        mTxtNobody = llBottomSheet.findViewById(R.id.txt_nobody);
        mImgOpen = llBottomSheet.findViewById(R.id.img_open_bottom);
        mRltOpen = llBottomSheet.findViewById(R.id.temp);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(DriverInvitesActivity.this);
        mList.setLayoutManager(layoutManager);
        mList.setHasFixedSize(true);

        mList.setVisibility(View.GONE);

        mRltOpen.setOnClickListener(view -> {
            if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                mImgOpen.setImageResource(R.drawable.ic_double_arrow_top_white);
            }else if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_COLLAPSED){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                mImgOpen.setImageResource(R.drawable.ic_double_arrow_bottom_white);
            }
        });
        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mImgOpen.setImageResource(R.drawable.ic_double_arrow_top_white);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                mImgOpen.setImageResource(R.drawable.ic_double_arrow_bottom_white);
            }
        });


        setupInit();
    }

    private void setupInit() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.drivers_tbl);
        mData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        Driver driver = item.getValue(Driver.class);
                        driver.setKey(item.getKey());
                        if(driver.getInviteCode()!=null){
                            if(driver.getInviteCode().equals(driverID.substring(0,10))){
                                drivers.add(driver);
                                mList.setVisibility(View.VISIBLE);
                                mTxtNobody.setVisibility(View.GONE);
                            }
                        }
                    }
                    Collections.reverse(drivers);
                    mAdapter = new ItemInvitesAdapter(getApplicationContext(), drivers);
                    mList.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
                mData.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
