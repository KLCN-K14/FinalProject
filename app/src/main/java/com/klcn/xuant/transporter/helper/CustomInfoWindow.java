package com.klcn.xuant.transporter.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.common.Common;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter{

    View view;
    Context context;

    public CustomInfoWindow(Context context){
        view = LayoutInflater.from(context)
                .inflate(R.layout.custom_customer_info_window,null);
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        String[] list = marker.getTitle().split(Common.keySplit);

        TextView txtName = (TextView)view.findViewById(R.id.txt_pickup_info);
        txtName.setText(list[0]);

        TextView txtPhone = (TextView)view.findViewById(R.id.txt_pickup_snippet);
        txtPhone.setText(list[1]);

        CircleImageView circleImageView = view.findViewById(R.id.img_avatar);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.avavtar)
                .error(R.drawable.avavtar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(context).load(marker.getSnippet()).apply(options).into(circleImageView);

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
