package com.klcn.xuant.transporter.mvp.feedbackDriver;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.model.TripInfo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ItemFeedBackAdapter extends RecyclerView.Adapter<ItemFeedBackAdapter.TripInfoViewHolder>{

    private ArrayList<TripInfo> tripInfos = new ArrayList<>();
    private Context context;

    public ItemFeedBackAdapter(Context context, ArrayList<TripInfo> tripInfos){
        this.tripInfos = tripInfos;
        this.context = context;
    }

    @NonNull
    @Override
    public TripInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_feedback, parent, false);
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
        @BindView(R.id.txt_content_feedback) TextView mTxtContent;
        @BindView(R.id.rating_bar) RatingBar mRatingBar;
        @BindView(R.id.txt_time) TextView mTxtTime;
        private Context mContext;

        public TripInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            mContext = itemView.getContext();
        }

        public void bindTripInfo(TripInfo tripInfo){
            mTxtTime.setText(DateFormat.format("dd-MM-yyyy, HH:mm", tripInfo.getDateCreated()));
            mRatingBar.setRating(Float.valueOf(tripInfo.getRating()));
            mTxtContent.setText(Html.fromHtml("&ldquo;" +tripInfo.getFeedback() + "&rdquo;"));
        }
    }


}
