package com.klcn.xuant.transporter.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.klcn.xuant.transporter.R;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter{

    View view;

    public CustomInfoWindow(Context context){
        view = LayoutInflater.from(context)
                .inflate(R.layout.custom_customer_info_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView txtPickUpInfo = (TextView)view.findViewById(R.id.txt_pickup_info);
        txtPickUpInfo.setText(marker.getTitle());

        TextView txtPickUpSnippet = (TextView)view.findViewById(R.id.txt_pickup_snippet);
        txtPickUpSnippet.setText(marker.getSnippet());

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
