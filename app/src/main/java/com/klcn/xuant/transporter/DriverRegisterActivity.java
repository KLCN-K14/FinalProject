package com.klcn.xuant.transporter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.model.FCMResponse;
import com.klcn.xuant.transporter.model.Notification;
import com.klcn.xuant.transporter.model.Sender;
import com.klcn.xuant.transporter.model.Token;
import com.klcn.xuant.transporter.remote.IFCMService;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DriverRegisterActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.edt_first_name)
    MaterialEditText edtFirstName;

    @BindView(R.id.edt_last_name)
    MaterialEditText edtLastName;

    @BindView(R.id.edt_phone)
    MaterialEditText edtPhone;

    @BindView(R.id.edt_email)
    MaterialEditText edtEmail;

    @BindView(R.id.edt_pass)
    MaterialEditText edtPass;

    @BindView(R.id.spinner_service)
    MaterialSpinner spinnerService;

    @BindView(R.id.edt_invite_code)
    MaterialEditText edtInviteCode;

    @BindView(R.id.edt_name_vehicle)
    MaterialEditText edtNameVehicle;

    @BindView(R.id.edt_license_plate)
    MaterialEditText edtLicensePlate;

    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.container)
    LinearLayout mRootLayout;

    @BindView(R.id.img_register)
    ImageView imgRegister;

    @BindView(R.id.area_code)
    CountryCodePicker countryCodePicker;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase db;
    private DatabaseReference drivers;
    private String serviceVehicle;
    IFCMService mFCMService;

    String nameDriver = "", inviteCode = "";
    SpotsDialog waitingDialog;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_register_driver);
        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        drivers = db.getReference(Common.drivers_tbl);

        mFCMService = Common.getFCMService();
        waitingDialog = new SpotsDialog(DriverRegisterActivity.this);

        spinnerService.setItems("Transport Standard", "Transport Premium");
        spinnerService.setSelectedIndex(0);
        serviceVehicle = "Transport Standard";
        spinnerService.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                serviceVehicle = item;
            }
        });


        imgBack.setOnClickListener(this);
        imgRegister.setOnClickListener(this);

    }

    String phoneString = "";
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.img_back:
                finish();
                break;
            case R.id.img_register:

                String regexStr = "^[+][0-9]{10,13}$";
                if(!TextUtils.isEmpty(edtPhone.getText().toString())){
                    String countryCode = countryCodePicker.getSelectedCountryCode();
                    String phoneNumber = edtPhone.getText().toString().trim();
                    if (phoneNumber.charAt(0) == '0') {
                        phoneNumber = phoneNumber.substring(1);
                    }
                    phoneString = "+"+countryCode + phoneNumber;
                }

                if(TextUtils.isEmpty(edtEmail.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Please enter email address",Toast.LENGTH_SHORT)
                            .show();
                }else if(TextUtils.isEmpty(edtPass.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Please enter password",Toast.LENGTH_SHORT)
                            .show();
                }else if(TextUtils.isEmpty(edtFirstName.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Please enter your first name",Toast.LENGTH_SHORT)
                            .show();
                }else if(TextUtils.isEmpty(edtLastName.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Please enter your last number",Toast.LENGTH_SHORT)
                            .show();
                }else if(TextUtils.isEmpty(edtPhone.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Please enter phone number",Toast.LENGTH_SHORT)
                            .show();
                }else if(TextUtils.isEmpty(edtNameVehicle.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Please enter name vehicle",Toast.LENGTH_SHORT)
                            .show();
                }else if(TextUtils.isEmpty(edtLicensePlate.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Please enter license plate",Toast.LENGTH_SHORT)
                            .show();
                }else if(edtPass.getText().toString().length()<6){
                    Toast.makeText(getApplicationContext(), "Password must be more than 6 characters",Toast.LENGTH_SHORT)
                            .show();
                }else if(!isEmailValid(edtEmail.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Email not valid",Toast.LENGTH_SHORT)
                            .show();
                }else if(phoneString.length()<10 || phoneString.length()>13 || phoneString.matches(regexStr)==false){
                    Toast.makeText(getApplicationContext(), "Phone not valid",Toast.LENGTH_SHORT)
                            .show();
                }else{
                    checkEmailExist();
                }
                break;
        }
    }

    private void checkEmailExist() {
        waitingDialog.show();
        mFirebaseAuth.fetchProvidersForEmail(edtEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                        if(task.getResult().getProviders().size()>0){
                            waitingDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Email already exists",Toast.LENGTH_SHORT)
                                    .show();
                        }else{
                            if(TextUtils.isEmpty(edtInviteCode.getText().toString())){
                                signUpDriver();
                            }else
                                checkIfCodeValid();
                        }
                    }
                });
    }


    private void signUpDriver() {
        mFirebaseAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPass.getText().toString())
                .addOnCompleteListener(DriverRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    waitingDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Register fail"+ task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            },2000);
                        }else{
                            createAccount(nameDriver, inviteCode);
                        }
                    }
                });
    }

    private void checkIfCodeValid() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference(Common.drivers_tbl);
        final Query mQuery = mData.orderByKey();
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    if(item.getKey().substring(0,10).equals(edtInviteCode.getText().toString())){
                        sendMessageInvitesSucess(item.getKey(),edtEmail.getText().toString());

                        Driver mDriver = item.getValue(Driver.class);
                        nameDriver = mDriver.getName();
                        inviteCode = item.getKey().substring(0,10);

                        Double wallet = Double.valueOf(mDriver.getCredits())+50000;
                        String walletS = String.valueOf(wallet);
                        HashMap<String,Object> maps = new HashMap<>();
                        maps.put("creadits",walletS);

                        FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                                .child(item.getKey())
                                .updateChildren(maps);
                    }
                }
                if(nameDriver.equals("")){
                    waitingDialog.dismiss();
                    Toast.makeText(getApplicationContext(),"Invite code wrong",Toast.LENGTH_SHORT).show();
                    edtInviteCode.setText("");
                }else{
                    signUpDriver();
                }
                mQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void createAccount(String nameDriver, String inviteCode) {
        Driver driver = new Driver();
        driver.setEmail(edtEmail.getText().toString());
        driver.setName(edtFirstName.getText().toString()+" "+edtLastName.getText().toString());
        driver.setPhoneNum(phoneString);
        driver.setLicensePlate(edtLicensePlate.getText().toString().toUpperCase());
        driver.setNameVehicle(edtNameVehicle.getText().toString().toUpperCase());
        driver.setServiceVehicle(serviceVehicle);
        driver.setAvgRatings("5");
        if(!nameDriver.equals(""))
        {
            driver.setInviteCode(inviteCode);
        }
        driver.setCredits("100000");

        driver.setCashBalance("0");
        driver.setImgUrl("https://firebasestorage.googleapis.com/v0/b/transporter-80ff6.appspot.com/o/images%2Fdriver_ava.png?alt=media&token=7d9384d4-962d-4d91-b0a0-4dc0091d4e7f");
        driver.setImgVehicle("https://firebasestorage.googleapis.com/v0/b/transporter-80ff6.appspot.com/o/images%2Fcar_ava.jpg?alt=media&token=d54d727d-387d-459f-8be2-7c909346e2db");
        drivers.child(mFirebaseAuth.getCurrentUser().getUid())
                .setValue(driver)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(!TextUtils.isEmpty(edtInviteCode.getText())){
                            // check if invite code right. add cash to driver invite and send message to driver
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                waitingDialog.dismiss();
                                if(!nameDriver.equals(""))
                                    Snackbar.make(mRootLayout, "Register success with code invites of driver "+
                                                nameDriver,Snackbar.LENGTH_SHORT).show();
                                else
                                    Snackbar.make(mRootLayout, "Register success",
                                            Snackbar.LENGTH_SHORT).show();
                                HashMap<String,Object> maps = new HashMap<>();
                                maps.put("dateCreated", ServerValue.TIMESTAMP);

                                FirebaseDatabase.getInstance().getReference(Common.drivers_tbl)
                                        .child(mFirebaseAuth.getCurrentUser().getUid())
                                        .updateChildren(maps);

                                Intent intent = new Intent();
                                setResult(RESULT_OK,intent);
                                finish();
                            }
                        },2000);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                waitingDialog.dismiss();
                                Snackbar.make(mRootLayout, "Register fail",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        },2000);
                    }
                });
    }

    private void sendMessageInvitesSucess(String key,String mailDriver) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.tokens_tbl);

        tokens.orderByKey().equalTo(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postData: dataSnapshot.getChildren()){
                            Token token = postData.getValue(Token.class);
                            Notification notification = new Notification("InviteSucess",mailDriver);
                            Sender sender = new Sender(token.getToken(),notification);
                            mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1){
                                        Log.e("MessageInvitesSucess","Success");
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
                                        Log.e("MessageInvitesSucess",response.message());
                                        Log.e("MessageInvitesSucess",response.errorBody().toString());
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

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
