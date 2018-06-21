package com.klcn.xuant.transporter.mvp.history;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.TripInfo;

import java.util.ArrayList;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ItemHistoryActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView mImgback;
    private String position;
    @BindView(R.id.profile_avatar)
    CircleImageView mAvatarDriver;
    @BindView(R.id.profile_name)
    TextView mNameDriver;
    @BindView(R.id.txt_rating_number)
    TextView mRating;
    @BindView(R.id.total_cash)
    TextView mTotal;
    @BindView(R.id.history_time)
    TextView mTime;
    ArrayList<TripInfo> tripInfoArrayList;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference trips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_history);

        mImgback = (ImageView) findViewById(R.id.toolbar_back);
        mImgback.setOnClickListener(this);
        position = getIntent().getStringExtra("POSITION");
        tripInfoArrayList = new ArrayList<>();
        mFirebaseAuth = FirebaseAuth.getInstance();
        trips = FirebaseDatabase.getInstance().getReference().child(Common.trip_info_tbl);
//        mRating.setText(tripInfoArrayList.get(Integer.parseInt(position)).getRating());

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
