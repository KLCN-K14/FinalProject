package com.klcn.xuant.transporter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.TripInfo;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerRateActivity extends AppCompatActivity{

    @BindView(R.id.txt_status_rate)
    TextView txtStatusRate;

    @BindView(R.id.rating_bar)
    RatingBar ratingBar;

    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    @BindView(R.id.edt_comment)
    EditText edtComment;

    Driver mDriver;
    ArrayList<TripInfo> tripInfos;
    String keyTrip = "",driverID = "", feedBack = "Nice Trip";

    Double sumRating = 0.;
    int count = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_customer_rate_driver);
        ButterKnife.bind(this);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        tripInfos = new ArrayList<>();
        keyTrip = getIntent().getStringExtra("keyTrip");
        driverID = getIntent().getStringExtra("driverID");
        getDriverInfo();

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                txtStatusRate.setText("You rated driver "+mDriver.getName()+" "+v+" star");
            }
        });
        edtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                feedBack = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Set button dialog
        btnConfirm.setOnClickListener(view -> {
            if(mDriver!=null){
                HashMap<String,Object> maps = new HashMap<>();
                String rating = String.valueOf(ratingBar.getRating());
                maps.put("rating",rating);
                if(!feedBack.equals("")){
                    maps.put("feedback",feedBack);
                }else
                    maps.put("feedback","Nice ride!");
                FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl).child(keyTrip)
                        .updateChildren(maps).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Double currentBase = 0.;
                        if(mDriver!=null)
                            currentBase = Double.valueOf(mDriver.getAvgRatings());
                        sumRating += ratingBar.getRating();
                        count++;
                        if(count!=0){
                            sumRating = sumRating/count;
                            currentBase = (currentBase+sumRating)/2;
                        }

                        HashMap<String,Object> mapRating = new HashMap<>();
                        maps.put("avgRatings",String.format("%.1f", currentBase));

                        FirebaseDatabase.getInstance().getReference(Common.drivers_tbl).child(driverID)
                                .updateChildren(maps);
                        finish();

                    }
                });
            }else{
                Toast.makeText(getApplicationContext(),"get driver null",Toast.LENGTH_SHORT).show();
            }

        });
    }


    private void getDriverInfo() {
        FirebaseDatabase.getInstance().getReference(Common.drivers_tbl).child(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            mDriver = dataSnapshot.getValue(Driver.class);
                           getTripInfos();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getTripInfos() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
        final Query mQuery = mData.orderByChild("driverId").equalTo(driverID);
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    TripInfo tripInfo = item.getValue(TripInfo.class);
                    tripInfo.setKey(item.getKey());
                    tripInfos.add(tripInfo);
                }
                getRating();
                mQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRating() {
        if(tripInfos!=null){
            for(int i=0;i<tripInfos.size();i++){
                if(tripInfos.get(i).getStatus().equals(Common.trip_info_status_complete)){
                    if(tripInfos.get(i).getRating()!=null){
                        sumRating+=Double.valueOf(tripInfos.get(i).getRating());
                        count++;
                    }
                }
            }
        }
//        return String.format("%.1f", currentBase);
    }
}
