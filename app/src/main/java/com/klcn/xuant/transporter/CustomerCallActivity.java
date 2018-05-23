package com.klcn.xuant.transporter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.remote.IGoogleAPI;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCallActivity extends AppCompatActivity {

    @BindView(R.id.txt_your_position)
    TextView mTxtYourPosition;

    @BindView(R.id.txt_your_destination)
    TextView mTxtYourDestination;

    @BindView(R.id.txt_price)
    TextView mTxtPrice;

    @BindView(R.id.txt_distance)
    TextView mTxtDistance;

    @BindView(R.id.txt_time)
    TextView mTxtTime;

    @BindView(R.id.btn_accept_pickup_request)
    Button mBtnAccept;

    @BindView(R.id.btn_cancel_find_driver)
    Button mBtnCancel;

    @BindView(R.id.ripple_background)
    RippleBackground mRippleBackground;

    IGoogleAPI mService;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);
        ButterKnife.bind(this);

        mRippleBackground.startRippleAnimation();

        if(getIntent()!=null){
            double lat = getIntent().getDoubleExtra("lat",-1.0);
            double lng = getIntent().getDoubleExtra("lng",-1.0);
            mTxtYourDestination.setText(getIntent().getStringExtra("destination"));
            getDirection(lat,lng);
        }

        mService = Common.getGoogleAPI();
    }

    private void getDirection(double lat, double lng) {
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ Common.mLastLocationDriver.getLatitude()+","+Common.mLastLocationDriver.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("TRANSPORT",requestApi);
//            mService.getPath(requestApi)
//                    .enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(Call<String> call, Response<String> response) {
//                            try {
//                                JSONObject jsonObject = new JSONObject(response.body().toString());
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<String> call, Throwable t) {
//
//                        }
//                    });

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
