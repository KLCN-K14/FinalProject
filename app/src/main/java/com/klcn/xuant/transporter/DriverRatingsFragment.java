package com.klcn.xuant.transporter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DriverRatingsFragment extends Fragment implements View.OnClickListener{

    @BindView(R.id.txt_accept_trip)
    TextView mTxtAcceptTrip;

    @BindView(R.id.txt_full_star_trip)
    TextView mTxtFullStarTrip;

    @BindView(R.id.txt_total_trip)
    TextView mTxtTotalTrip;

    @BindView(R.id.txt_avg_ratings)
    TextView mTxtAvgRating;

    @BindView(R.id.panel_customer_feedback)
    RelativeLayout mPanelCustomerFeedBack;

    @BindView(R.id.panel_pro_tips)
    RelativeLayout mPanelProTips;

    public static DriverRatingsFragment newInstance() {
        DriverRatingsFragment fragment = new DriverRatingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_ratings, container, false);
        ButterKnife.bind(this,view);

        mPanelCustomerFeedBack.setOnClickListener(this);
        mPanelProTips.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.panel_customer_feedback:
                Toast.makeText(getContext(),"Panel customer feedback",Toast.LENGTH_LONG).show();
                break;
            case R.id.panel_pro_tips:
                Toast.makeText(getContext(),"Panel pro tips",Toast.LENGTH_LONG).show();
                break;
        }
    }
}
