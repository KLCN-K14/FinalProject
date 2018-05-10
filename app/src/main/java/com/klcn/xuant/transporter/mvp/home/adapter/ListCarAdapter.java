package com.klcn.xuant.transporter.mvp.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.klcn.xuant.transporter.R;

public class ListCarAdapter extends BaseAdapter {
    private String[] listCar;
    private String[] listTimeWait;
    private LayoutInflater layoutInflater;
    private Context context;

    public ListCarAdapter(Context context,  String[] listCar, String[] lisTimeWait) {
        this.context = context;
        this.listCar = listCar;
        this.listTimeWait = lisTimeWait;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listCar.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_list_car, null);
            holder = new ViewHolder();
            holder.imgCar = (ImageView) view.findViewById(R.id.imgv_car);
            holder.txtTypeCar = (TextView) view.findViewById(R.id.txt_type_car);
            holder.txtTime = (TextView) view.findViewById(R.id.txt_time);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.txtTypeCar.setText(listCar[i]);
        holder.txtTime.setText(listTimeWait[i]);
        holder.imgCar.setImageResource(R.drawable.ic_directions_car);

        return view;
    }
    static class ViewHolder {
        ImageView imgCar;
        TextView txtTypeCar;
        TextView txtTime;
    }
}
