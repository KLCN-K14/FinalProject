package com.klcn.xuant.transporter.mvp.tripHistoryDriver;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.klcn.xuant.transporter.OnLoadMoreListener;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.TripInfo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ItemTripHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<TripInfo> tripInfos = new ArrayList<>();
    private Context context;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private boolean isLoading;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;

    private OnLoadMoreListener onLoadMoreListener;
    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    public ItemTripHistoryAdapter(RecyclerView recyclerView, Context context, ArrayList<TripInfo> tripInfos){
        this.tripInfos = tripInfos;
        this.context = context;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });

    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        }
    }

    // "Normal item" ViewHolder
    private class TripInfoViewHolder extends RecyclerView.ViewHolder {
        TextView mTxtTimeTrip;
        TextView mTxtPickup;
        TextView mTxtDropoff;
        TextView mTxtPrice;
        TextView mTxtStatus;
        ImageView mImgMoney;
        private Context mContext;

        public TripInfoViewHolder(View view) {
            super(view);
            mTxtTimeTrip = (TextView) view.findViewById(R.id.txt_time_trip);
            mTxtPickup = (TextView) view.findViewById(R.id.txt_pickup);
            mTxtDropoff = (TextView) view.findViewById(R.id.txt_destination);
            mTxtPrice = (TextView) view.findViewById(R.id.txt_price);
            mTxtStatus = (TextView) view.findViewById(R.id.txt_status);
            mImgMoney = (ImageView) view.findViewById(R.id.img_money);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return tripInfos.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_history_driver, parent, false);
            return new TripInfoViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TripInfoViewHolder) {
            TripInfo tripInfo = tripInfos.get(position);
            TripInfoViewHolder tripInfoViewHolder = (TripInfoViewHolder) holder;

            tripInfoViewHolder.mTxtTimeTrip.setText(DateFormat.format("dd-MM-yyyy, HH:mm", tripInfo.getDateCreated()));
            tripInfoViewHolder.mTxtDropoff.setText(tripInfo.getDropoff());
            tripInfoViewHolder.mTxtPickup.setText(tripInfo.getPickup());
            Double price = Double.valueOf(tripInfo.getFixedFare())/1000;
            tripInfoViewHolder.mTxtPrice.setText("VND "+price.intValue()+"K");
            if(tripInfo.getStatus().equals(Common.trip_info_status_driver_cancel)){
                tripInfoViewHolder.mImgMoney.setVisibility(View.GONE);
                tripInfoViewHolder.mTxtPrice.setText("Cause: "+tripInfo.getReasonCancel());
                tripInfoViewHolder.mTxtStatus.setBackground(context.getResources().getDrawable(R.drawable.bg_cancel));
                tripInfoViewHolder.mTxtStatus.setText("Driver canceled");
                tripInfoViewHolder.mTxtStatus.setTextColor(context.getResources().getColor(R.color.red));
            }else if(tripInfo.getStatus().equals(Common.trip_info_status_customer_cancel)){
                tripInfoViewHolder.mImgMoney.setVisibility(View.GONE);
                tripInfoViewHolder.mTxtPrice.setText("Cause: "+tripInfo.getReasonCancel());
                tripInfoViewHolder.mTxtStatus.setBackground(context.getResources().getDrawable(R.drawable.bg_cancel));
                tripInfoViewHolder.mTxtStatus.setText("Customer canceled");
                tripInfoViewHolder.mTxtStatus.setTextColor(context.getResources().getColor(R.color.red));
            }

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return tripInfos == null ? 0 : tripInfos.size();
    }

    public void setLoaded() {
        isLoading = false;
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(tripInfos.get(position).getKey());
    }


}
