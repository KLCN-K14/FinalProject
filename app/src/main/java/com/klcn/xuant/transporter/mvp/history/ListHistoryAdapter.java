package com.klcn.xuant.transporter.mvp.history;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.TripInfo;

import java.util.ArrayList;

public class ListHistoryAdapter extends BaseAdapter {

    private ArrayList<TripInfo> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public ListHistoryAdapter(Context aContext, ArrayList<TripInfo> listData) {
        this.context = aContext;
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int i) {
        return listData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.list_item_history, null);
            holder = new ViewHolder();
            holder.mTxtPlaceLocation = (TextView) view.findViewById(R.id.txt_place_location);
            holder.mTxtPlaceDistination = (TextView) view.findViewById(R.id.txt_place_destination);
            holder.mTxtDateTime = (TextView) view.findViewById(R.id.txt_date_time);
            holder.mStatus = (TextView) view.findViewById(R.id.trip_status);
            holder.mRating = (SimpleRatingBar) view.findViewById(R.id.ratingbar);
            holder.mImgTemp = (ImageView) view.findViewById(R.id.img_distance_temp);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        TripInfo tripInfo = this.listData.get(i);
        holder.mTxtPlaceLocation.setText(tripInfo.getPickup());
        if(tripInfo.getPickup().length()>27){
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(18, 30);
            holder.mImgTemp.setLayoutParams(layoutParams);
        }


        holder.mTxtPlaceDistination.setText(tripInfo.getDropoff());
        if (tripInfo.getRating() != null)
            holder.mRating.setRating(Float.parseFloat(tripInfo.getRating()));
        else
            holder.mRating.setRating(0);

        if (tripInfo.getStatus().equals(Common.trip_info_status_customer_cancel) || tripInfo.getStatus().equals(Common.trip_info_status_driver_cancel)) {

            holder.mTxtDateTime.setText(DateFormat.format("dd/MM/yyyy, HH:mm", tripInfo.getDateCreated()));
            holder.mStatus.setText("Huỷ chuyến");

        } else {
            holder.mTxtDateTime.setText(DateFormat.format("dd/MM/yyyy, HH:mm", tripInfo.getTimeDropoff()));
            holder.mStatus.setText("Hoàn thành");

        }
        return view;
    }

    static class ViewHolder {
        TextView mTxtPlaceLocation;
        TextView mTxtPlaceDistination;
        TextView mTxtDateTime;
        TextView mStatus;
        ImageView mImgTemp;
        SimpleRatingBar mRating;
    }
}
