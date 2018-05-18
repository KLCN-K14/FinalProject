package com.klcn.xuant.transporter.mvp.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.klcn.xuant.transporter.R;

public class ListHistoryAdapter extends BaseAdapter {

    private String[] listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public ListHistoryAdapter(Context aContext,  String[] listData) {
        this.context = aContext;
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return listData.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
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
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.mTxtPlaceDistination.setText(listData[i]);
        return view;
    }

    static class ViewHolder {
        TextView mTxtPlaceLocation;
        TextView mTxtPlaceDistination;
        TextView mTxtDateTime;
    }
}
