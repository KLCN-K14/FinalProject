package com.klcn.xuant.transporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;

public class CustomerTrackingActivity extends AppCompatActivity implements
        View.OnClickListener{
    Button mBtnCancelBook;
    private static final int PICK_REQUEST = 1;
    ImageView mImgChat;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_tracking_activity);


        //Bottom sheet
        View llBottomSheet = (View)findViewById(R.id.bottom_sheet);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                fab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
            }
        });

        mBtnCancelBook = (Button) findViewById(R.id.btn_cancel_book);
        mImgChat = (ImageView) findViewById(R.id.img_ic_chat);

        mBtnCancelBook.setOnClickListener(this);
        mImgChat.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_cancel_book:
                Intent intent = new Intent(CustomerTrackingActivity.this, CancelBookActivity.class);
                startActivityForResult(intent, PICK_REQUEST);
                break;
            case R.id.img_ic_chat:
                Intent intentChat = new Intent(CustomerTrackingActivity.this, ChatActivity.class);
                startActivity(intentChat);
                break;
        }
    }
}
