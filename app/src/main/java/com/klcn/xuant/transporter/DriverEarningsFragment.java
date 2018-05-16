package com.klcn.xuant.transporter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

public class DriverEarningsFragment extends Fragment {

    public static DriverEarningsFragment newInstance() {
        DriverEarningsFragment fragment = new DriverEarningsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_earnings, container, false);
        ButterKnife.bind(this,view);


        return view;
    }

}
