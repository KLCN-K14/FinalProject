package com.klcn.xuant.transporter;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.helper.NotificationHelper;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.TripInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DriverConfirmBillActivity extends AppCompatActivity{

    @BindView(R.id.txt_fixed_fare)
    TextView txtFixedFare;

    @BindView(R.id.edt_toll_other)
    EditText editTollOther;

    @BindView(R.id.txt_total_payout)
    TextView txtTotal;

    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    String keyTrip = "", feedBack = "Nice Trip", otherToll = "0";
    int fixedFare = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_payment);
        ButterKnife.bind(this);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        keyTrip = getIntent().getStringExtra("keyTrip");
        fixedFare = getIntent().getIntExtra("fixedFare",0);
        if(fixedFare == 0){

        }


        txtFixedFare.setText(String.valueOf(fixedFare*1000));
        txtTotal.setText(String.valueOf(fixedFare*1000));


        editTollOther.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                otherToll = charSequence.toString();
                int total = fixedFare*1000+Integer.parseInt(otherToll);
                txtTotal.setText(String.valueOf(total));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Set button dialog
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> maps = new HashMap<>();
                removeWorkingDriver();
                if(Integer.parseInt(otherToll)>500){
                    maps.put("otherToll",otherToll);
                    FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl).child(keyTrip)
                            .updateChildren(maps);
                }
                checkIsTripOverLoad();
                checkIsFirstTrip();
                updateCreditAndCashDriver();
                finish();
            }
        });

    }

    private void checkIsTripOverLoad() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
        final Query mQuery = mData.orderByChild("driverId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.e("CountTrip",String.valueOf(dataSnapshot.getChildrenCount()));
                    if(dataSnapshot.getChildrenCount()>=200){
                        deleteIfTripOverLoad();
                        mQuery.removeEventListener(this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteIfTripOverLoad() {
        ArrayList<TripInfo> tripInfos = new ArrayList<>();
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
        final Query mQuery = mData.orderByChild("dateCreated");
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    TripInfo tripInfo = item.getValue(TripInfo.class);
                    if(tripInfo.getDriverId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        Log.e("Key trip delete", ""+item.getKey());
                        mData.child(item.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.e("Remove OverLoadTrip","Success delete ");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Remove OverLoadTrip",e.getMessage());
                            }
                        });
                        break;
                    }
                }
                mQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateCreditAndCashDriver() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Driver mDriver = dataSnapshot.getValue(Driver.class);
                    Double cashBalance = Double.valueOf(mDriver.getCashBalance());
                    Double credits = Double.valueOf(mDriver.getCredits());

                    if(Integer.parseInt(otherToll)>1000){
                        credits = credits - (fixedFare*1000 + (Integer.parseInt(otherToll))*Common.transport_fee);
                        cashBalance = cashBalance + fixedFare*1000 + (Integer.parseInt(otherToll));
                    }else{
                        credits = credits - fixedFare*Common.transport_fee*1000;
                        cashBalance = cashBalance + fixedFare*1000;
                    }

                    HashMap<String,Object> maps = new HashMap<>();
                    maps.put("credits",String.valueOf(credits));
                    maps.put("cashBalance",String.valueOf(cashBalance));

                    FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(maps);

                    mData.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkIsFirstTrip() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
        Query query = mData.orderByChild("driverId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    int count = 0;
                    for(DataSnapshot item: dataSnapshot.getChildren()){
                        TripInfo tripInfo = item.getValue(TripInfo.class);
                        if(tripInfo.getStatus()!=null){
                            if(tripInfo.getStatus().equals(Common.trip_info_status_complete))
                                count++;
                        }
                    }
                    if(count == 1)
                        checkDriverHaveInviteCode();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkDriverHaveInviteCode() {
        Log.e("Driver","First Trip");
        FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @TargetApi(Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Driver mDriver = dataSnapshot.getValue(Driver.class);
                            if(mDriver.getInviteCode()!=null){
                                Log.e("Driver","have invite code ---- increase money");
                                // increase credits to driver invite
                                // notification to driver invite
                                getDriverInvited(mDriver.getInviteCode());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void getDriverInvited(String inviteCode) {
        Log.e("Driver","getDriverInvited success");
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.drivers_tbl);
        Query query = mData.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    if(item.getKey().substring(0,10).equals(inviteCode)){
                        // send message to driver invited
                        // increase credits to driver invited
                        Log.e("Driver","give invite code ---- increase money");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void removeWorkingDriver() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.driver_working_tbl);

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }
}
