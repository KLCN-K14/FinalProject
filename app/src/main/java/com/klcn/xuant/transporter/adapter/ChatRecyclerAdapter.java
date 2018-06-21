package com.klcn.xuant.transporter.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.library.bubbleview.BubbleTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Chat;
import com.klcn.xuant.transporter.model.Messages;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    private List<Messages> mChats;
    final String imgUrl;


    public ChatRecyclerAdapter(List<Messages> chats, String imgUrl) {
        mChats = chats;
        this.imgUrl = imgUrl;
        Log.e("INIT",imgUrl);
    }

    public void add(Messages chat) {
        mChats.add(chat);
        notifyItemInserted(mChats.size() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_ME:
                View viewChatMine = layoutInflater.inflate(R.layout.item_message_send, parent, false);
                viewHolder = new MyChatViewHolder(viewChatMine);
                break;
            case VIEW_TYPE_OTHER:
                View viewChatOther = layoutInflater.inflate(R.layout.item_message_recv, parent, false);
                viewHolder = new OtherChatViewHolder(viewChatOther);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (TextUtils.equals(mChats.get(position).getFrom(),
                FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            configureMyChatViewHolder((MyChatViewHolder) holder, position);
        } else {
            configureOtherChatViewHolder((OtherChatViewHolder) holder, position);


        }
    }

    private void configureMyChatViewHolder(MyChatViewHolder myChatViewHolder, int position) {
        Messages chat = mChats.get(position);


        myChatViewHolder.txtChatMessage.setText(chat.getMessage());


        Date currentTime = Calendar.getInstance().getTime();
        long milliseconds = currentTime.getTime();
        final String strTimeFormate = "HH:mm";
        if((DateFormat.format("dd-MM-yyyy", chat.getTime())).equals(DateFormat.format("dd-MM-yyyy", currentTime))){
            if((milliseconds - chat.getTime())<= 3000)
                myChatViewHolder.mMessageTime.setText("Just now");
            else
                myChatViewHolder.mMessageTime.setText("Today " + DateFormat.format(strTimeFormate, chat.getTime()));
        }else
            myChatViewHolder.mMessageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm)", chat.getTime()));
    }

    private void configureOtherChatViewHolder(final OtherChatViewHolder otherChatViewHolder, int position) {
        Messages chat = mChats.get(position);

        otherChatViewHolder.txtChatMessage.setText(chat.getMessage());

        Log.e("ADAPTER",imgUrl);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(otherChatViewHolder.profileImage.getContext()).load(imgUrl).apply(options).into(otherChatViewHolder.profileImage);

        Date currentTime = Calendar.getInstance().getTime();
        long milliseconds = currentTime.getTime();
        final String strTimeFormate = "HH:mm";
        if((DateFormat.format("dd-MM-yyyy", chat.getTime())).equals(DateFormat.format("dd-MM-yyyy", currentTime))){
            if((milliseconds - chat.getTime())<= 3000)
                otherChatViewHolder.mMessageTime.setText("Just now");
            else
                otherChatViewHolder.mMessageTime.setText(DateFormat.format(strTimeFormate, chat.getTime()));
        }else
            otherChatViewHolder.mMessageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm)", chat.getTime()));


    }

    @Override
    public int getItemCount() {
        if (mChats != null) {
            return mChats.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (TextUtils.equals(mChats.get(position).getFrom(),
                FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }

    private static class MyChatViewHolder extends RecyclerView.ViewHolder {
        private BubbleTextView txtChatMessage;
        private TextView mMessageTime;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            txtChatMessage = (BubbleTextView) itemView.findViewById(R.id.text_message);

            mMessageTime = (TextView) itemView.findViewById(R.id.message_time);
        }
    }

    private static class OtherChatViewHolder extends RecyclerView.ViewHolder {
        private BubbleTextView txtChatMessage;
        public CircleImageView profileImage;
        private TextView mMessageTime;

        public OtherChatViewHolder(View itemView) {
            super(itemView);
            txtChatMessage = (BubbleTextView) itemView.findViewById(R.id.text_message_recv);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_layout);
            mMessageTime = (TextView) itemView.findViewById(R.id.message_time_recv);


        }
    }
}
