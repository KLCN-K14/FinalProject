package com.klcn.xuant.transporter;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.FCMResponse;
import com.klcn.xuant.transporter.model.Notification;
import com.klcn.xuant.transporter.model.Sender;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.remote.IFCMService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CancelBookActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout mLnOrtherReason,mLnFindOrtherCar,mLnDriverAskCancel,mLnWaitedLong,mLnWrongAddress,mLnDriverFar;
    EditText mEditReasons;
    ImageView mImgback;
    Button btnSend;
    String currentResson="";
    String driverID="";
    IFCMService mFCMService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_book);
        driverID = getIntent().getStringExtra("driverID");
        mFCMService = Common.getFCMService();
        mLnOrtherReason = (LinearLayout) findViewById(R.id.ln_orther_reason);
        mEditReasons = (EditText) findViewById(R.id.edit_reasons);
        mImgback = (ImageView) findViewById(R.id.toolbar_back);
        mLnFindOrtherCar = (LinearLayout) findViewById(R.id.ln_find_other_car);
        mLnDriverAskCancel = (LinearLayout) findViewById(R.id.ln_driver_ask_cancel);
        mLnWaitedLong = (LinearLayout) findViewById(R.id.ln_waited_long);
        mLnWrongAddress = (LinearLayout) findViewById(R.id.ln_wrong_address);
        mLnDriverFar = (LinearLayout) findViewById(R.id.ln_driver_far);
        btnSend = (Button) findViewById(R.id.btn_send);

        mLnOrtherReason.setOnClickListener(this);
        mLnFindOrtherCar.setOnClickListener(this);
        mLnDriverAskCancel.setOnClickListener(this);
        mLnWaitedLong.setOnClickListener(this);
        mLnWrongAddress.setOnClickListener(this);
        mLnDriverFar.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        mImgback.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ln_find_other_car:
                currentResson = "I found another vehicle";
                mEditReasons.setVisibility(View.INVISIBLE);
                refreshColor();
                mLnFindOrtherCar.setBackground(getResources().getDrawable(R.color.colorYellow));
                break;
            case R.id.ln_wrong_address:
                currentResson = "I entered the wrong address";
                mEditReasons.setVisibility(View.INVISIBLE);
                refreshColor();
                mLnWrongAddress.setBackground(getResources().getDrawable(R.color.colorYellow));
                break;
            case R.id.ln_driver_ask_cancel:
                currentResson = "My driver ask me to cancel";
                mEditReasons.setVisibility(View.INVISIBLE);
                refreshColor();
                mLnDriverAskCancel.setBackground(getResources().getDrawable(R.color.colorYellow));
                break;
            case R.id.ln_waited_long:
                currentResson = "I waited so long";
                mEditReasons.setVisibility(View.INVISIBLE);
                refreshColor();
                mLnWaitedLong.setBackground(getResources().getDrawable(R.color.colorYellow));
                break;
            case R.id.ln_driver_far:
                currentResson = "My driver is way too far";
                mEditReasons.setVisibility(View.INVISIBLE);
                refreshColor();
                mLnDriverFar.setBackground(getResources().getDrawable(R.color.colorYellow));
                break;
            case R.id.ln_orther_reason:
                mEditReasons.setVisibility(View.VISIBLE);
                mEditReasons.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        currentResson = charSequence.toString();
                        Toast.makeText(getApplicationContext(),currentResson,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                refreshColor();
                mLnOrtherReason.setBackground(getResources().getDrawable(R.color.colorYellow));
                break;
            case R.id.toolbar_back:
                finish();
                break;
            case R.id.btn_send:
                if(currentResson.equals("")){
                    Toast.makeText(getApplicationContext(),"Choose your reason to cancel trip",Toast.LENGTH_LONG).show();
                }else{
                    // cancel trip
                    new AlertDialog.Builder(this)
                            .setTitle("Are you sure?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent();
                                    intent.putExtra("reasonCancel",currentResson);
                                    setResult(RESULT_OK, intent);
                                    sendMessageCancelTrip();
                                    finish();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent();
                                    setResult(RESULT_CANCELED, intent);
                                    finish();
                                }
                            })
                            .show();

                }
                break;
        }

    }

    private void sendMessageCancelTrip() {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);
                            Notification notification = new Notification("CustomerCancelTrip","Customer cancel the trip!");
                            Sender sender = new Sender(token.getToken(),notification);
                            mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1){
                                        Log.e("MessageCancelTrip","Sucess");
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
                                        Log.e("MessageCancelTrip",response.message());
                                        Log.e("MessageCancelTrip",response.errorBody().toString());
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

    private void refreshColor() {
        mLnFindOrtherCar.setBackground(getResources().getDrawable(R.color.gray_20_precent));
        mLnDriverAskCancel.setBackground(getResources().getDrawable(R.color.gray_20_precent));
        mLnWaitedLong.setBackground(getResources().getDrawable(R.color.gray_20_precent));
        mLnWrongAddress.setBackground(getResources().getDrawable(R.color.gray_20_precent));
        mLnDriverFar.setBackground(getResources().getDrawable(R.color.gray_20_precent));
        mLnOrtherReason.setBackground(getResources().getDrawable(R.color.gray_20_precent));
    }
}
