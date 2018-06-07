package com.klcn.xuant.transporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;

public class CustomerTrackingActivity extends AppCompatActivity implements
        View.OnClickListener{
    Button mBtnCancelBook;
    private static final int PICK_REQUEST = 1;
    ImageView mImgChat;
    private FirebaseAuth mFirebaseAuth;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mDriversDatabase;


    private String mCurrent_user_id;
    private TextView mNameDriver;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_tracking_activity);

        mBtnCancelBook = (Button) findViewById(R.id.btn_cancel_book);
        mImgChat = (ImageView) findViewById(R.id.img_ic_chat);
        mNameDriver = (TextView) findViewById(R.id.txt_name_driver);

        mFirebaseAuth= FirebaseAuth.getInstance();
        mCurrent_user_id = mFirebaseAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);
        mDriversDatabase = FirebaseDatabase.getInstance().getReference().child("Drivers");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mDriversDatabase.keepSynced(true);
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

                Intent chatIntent = new Intent(CustomerTrackingActivity.this, ChatActivity.class);
                chatIntent.putExtra("user_id", "VxE53ShAMWOdkKbTOQ6KT6J3ZII2");
                chatIntent.putExtra("user_name", "test");
                startActivity(chatIntent);

                break;
        }
    }
}
