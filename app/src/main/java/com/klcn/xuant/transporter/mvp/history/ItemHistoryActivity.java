package com.klcn.xuant.transporter.mvp.history;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.TripInfo;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ItemHistoryActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView mImgback,mImgTemp;
    CircleImageView mAvatarDriver;
    TextView mNameDriver, mRating, mTotal, mTime, mPickUp, mDropOff, mFeedback;
    DatabaseReference drivers;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_history);

        mImgback = (ImageView) findViewById(R.id.toolbar_back);
        mRating = (TextView) findViewById(R.id.txt_rating_number);
        mAvatarDriver = (CircleImageView) findViewById(R.id.profile_avatar);
        mNameDriver = (TextView) findViewById(R.id.profile_name);
        mTime = (TextView) findViewById(R.id.history_time);
        mTotal = (TextView) findViewById(R.id.total_cash);
        mPickUp = (TextView) findViewById(R.id.txt_place_location);
        mDropOff = (TextView) findViewById(R.id.txt_place_destination);
        mFeedback = (TextView) findViewById(R.id.feedback_history);
        mImgTemp = (ImageView) findViewById(R.id.img_temp);

        mImgback.setOnClickListener(this);


        String rating = getIntent().getStringExtra("rating");
        String pickup = getIntent().getStringExtra("pickup");
        String dropoff = getIntent().getStringExtra("dropoff");
        String feedback = getIntent().getStringExtra("feedback");
        String total = getIntent().getStringExtra("total");
        String time = getIntent().getStringExtra("time");
        String driverId = getIntent().getStringExtra("driverId");

        drivers = FirebaseDatabase.getInstance().getReference().child(Common.drivers_tbl).child(driverId);

        drivers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null)
                        mNameDriver.setText(map.get("name").toString());
                    if (map.get("imgUrl") != null) {
                        RequestOptions options = new RequestOptions()
                                .centerCrop()
                                .placeholder(R.drawable.avavtar)
                                .error(R.drawable.avavtar)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .priority(Priority.HIGH);
                        Glide.with(getApplication()).load(map.get("imgUrl").toString()).apply(options).into(mAvatarDriver);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mRating.setText(rating);
        mPickUp.setText(pickup);

        if(pickup.length()>30){
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(18, 45);
            mImgTemp.setLayoutParams(layoutParams);
        }
        mDropOff.setText(dropoff);
        mFeedback.setText(feedback);
        mTotal.setText(total);
        mTime.setText(time);



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
