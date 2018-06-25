package com.klcn.xuant.transporter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.klcn.xuant.transporter.common.Common;

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
                finish();
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
