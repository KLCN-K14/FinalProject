package com.klcn.xuant.transporter.mvp.payStatementDriver;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.model.TripInfo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ItemPayStatementAdapter extends RecyclerView.Adapter<ItemPayStatementAdapter.TripInfoViewHolder>{

    private ArrayList<TripInfo> tripInfos = new ArrayList<>();
    private Context context;

    public ItemPayStatementAdapter(Context context, ArrayList<TripInfo> tripInfos){
        this.tripInfos = tripInfos;
        this.context = context;
    }

    @NonNull
    @Override
    public TripInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_pay_statement, parent, false);
        TripInfoViewHolder viewHolder = new TripInfoViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TripInfoViewHolder holder, int position) {
        holder.bindTripInfo(tripInfos.get(position));
    }

    @Override
    public int getItemCount() {
        return tripInfos.size();
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(tripInfos.get(position).getKey());
    }

    public class TripInfoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_time) TextView mTxtTimeTrip;
        @BindView(R.id.txt_name_service) TextView mTxtNameService;
        @BindView(R.id.txt_price) TextView mTxtPrice;
        private Context mContext;

        public TripInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            mContext = itemView.getContext();
        }

        public void bindTripInfo(TripInfo tripInfo){
            mTxtTimeTrip.setText(DateFormat.format("dd-MM-yyyy, HH:mm", tripInfo.getDateCreated()));
            mTxtNameService.setText(tripInfo.getServiceVehicle());
            Double price = Double.valueOf(tripInfo.getFixedFare())/1000;
            mTxtPrice.setText("VND "+price.intValue()+"K");
        }
    }


}
