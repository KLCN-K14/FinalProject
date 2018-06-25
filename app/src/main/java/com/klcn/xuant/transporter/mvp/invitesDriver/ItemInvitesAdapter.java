package com.klcn.xuant.transporter.mvp.invitesDriver;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.FCMResponse;
import com.klcn.xuant.transporter.model.Notification;
import com.klcn.xuant.transporter.model.Sender;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.model.TripInfo;
import com.klcn.xuant.transporter.remote.IFCMService;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemInvitesAdapter extends RecyclerView.Adapter<ItemInvitesAdapter.TripInfoViewHolder>{

    private ArrayList<Driver> driverInfos = new ArrayList<>();
    private Context context;
    private IFCMService mFCMService;

    public ItemInvitesAdapter(Context context, ArrayList<Driver> driverInfos){
        this.driverInfos = driverInfos;
        this.context = context;
        mFCMService = Common.getFCMService();
    }

    @NonNull
    @Override
    public TripInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invites_driver, parent, false);
        TripInfoViewHolder viewHolder = new TripInfoViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TripInfoViewHolder holder, int position) {
        holder.bindTripInfo(driverInfos.get(position));
    }

    @Override
    public int getItemCount() {
        return driverInfos.size();
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(driverInfos.get(position).getKey());
    }

    public class TripInfoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_avatar_driver)
        ImageView mImgAvatar;
        @BindView(R.id.txt_name_driver) TextView mTxtNameDriver;
        @BindView(R.id.txt_date_created) TextView mTxtTime;
        @BindView(R.id.txt_name_service) TextView mTxtNameService;
        @BindView(R.id.txt_status) TextView mTxtStatus;
        private Context mContext;

        public TripInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            mContext = itemView.getContext();
        }

        public void bindTripInfo(Driver driverInfo){
            mTxtTime.setText(DateFormat.format("dd-MM-yyyy, HH:mm", driverInfo.getDateCreated()));
            mTxtNameService.setText(driverInfo.getServiceVehicle().toString());
            mTxtNameDriver.setText(driverInfo.getName().toUpperCase());
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);
            Glide.with(mContext).load(driverInfo.getImgUrl()).apply(options).into(mImgAvatar);

            final String key = driverInfo.getKey();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Common.trip_info_tbl);
            Query mQuery = databaseReference.orderByChild("driverId").equalTo(driverInfo.getKey());
            mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        mTxtStatus.setTextColor(mContext.getResources().getColor(R.color.colorActiveNavigation));
                        mTxtStatus.setBackground(mContext.getResources().getDrawable(R.drawable.bg_completed));
                    }else{
                        mTxtTime.setText("Need completed first ride");
                        mTxtStatus.setText("Remind");
                        mTxtStatus.setTextColor(mContext.getResources().getColor(R.color.white));
                        mTxtStatus.setBackground(mContext.getResources().getDrawable(R.drawable.bg_waiting));
                        mTxtStatus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendMessageRemind(key);
                            }
                        });
                    }

                    mQuery.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void sendMessageRemind(String key) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);
                            Notification notification = new Notification("RemindFirstTrip","Start your first ride to receive promos");
                            Sender sender = new Sender(token.getToken(),notification);
                            mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1){
                                        Log.e("MessageRemind","Success");
                                    }
                                    else{
                                        Log.e("MessageRemind",response.message());
                                        Log.e("MessageRemind",response.errorBody().toString());
                                    }
                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


}
