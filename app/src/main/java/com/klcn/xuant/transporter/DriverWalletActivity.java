package com.klcn.xuant.transporter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Driver;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DriverWalletActivity extends AppCompatActivity{


    @BindView(R.id.txt_credits)
    TextView txtCredits;
    @BindView(R.id.txt_cash_balance)
    TextView txtCashBalance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_wallet);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab!=null)
        {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        setupInit();
    }

    private void setupInit() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Driver mDriver = dataSnapshot.getValue(Driver.class);
                    txtCredits.setText(mDriver.getCredits());
                    txtCashBalance.setText(mDriver.getCashBalance());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
