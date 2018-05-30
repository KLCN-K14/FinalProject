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
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.klcn.xuant.transporter.common.Common;
import com.klcn.xuant.transporter.model.Driver;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
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

    @BindView(R.id.edt_name_vehicle)
    MaterialEditText edtNameVehicle;

    @BindView(R.id.edt_license_plate)
    MaterialEditText edtLicensePlate;

    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.img_register)
    ImageView imgRegister;

    @BindView(R.id.area_code)
    CountryCodePicker countryCodePicker;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase db;
    private DatabaseReference drivers;
    private String serviceVehicle;

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

        spinnerService.setItems("Transport Standard", "Transport Premium");
        spinnerService.setSelectedIndex(0);
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
                String countryCode = countryCodePicker.getSelectedCountryCode();
                String phoneNumber = edtPhone.getText().toString().trim();
                if (phoneNumber.charAt(0) == '0') {
                    phoneNumber = phoneNumber.substring(1);
                }
                String phoneString = "+"+countryCode + phoneNumber;
                String regexStr = "^[+][0-9]{10,13}$";

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
                }
                else {
                    mFirebaseAuth.fetchProvidersForEmail(edtEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if(task.getResult().getProviders().size()>0){
                                        Toast.makeText(getApplicationContext(), "Email already exists",Toast.LENGTH_SHORT)
                                                .show();
                                    }else{
                                        signUpDriver();
                                    }
                                }
                            });
                }
                break;
        }
    }

    private void signUpDriver() {
        final SpotsDialog waitingDialog = new SpotsDialog(DriverRegisterActivity.this);
        waitingDialog.show();
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

                            Driver driver = new Driver();
                            driver.setEmail(edtEmail.getText().toString());
                            driver.setName(edtFirstName.getText().toString()+" "+edtLastName.getText().toString());
                            driver.setPhoneNum(phoneString);
                            driver.setLicensePlate(edtLicensePlate.getText().toString());
                            driver.setNameVehicle(edtNameVehicle.getText().toString());
                            driver.setServiceVehicle(serviceVehicle);
                            drivers.child(mFirebaseAuth.getCurrentUser().getUid())
                                    .setValue(driver)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    waitingDialog.dismiss();
                                                    Toast.makeText(getApplicationContext(), "Register success",
                                                            Toast.LENGTH_SHORT).show();
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
                                                    Toast.makeText(getApplicationContext(), "Register fail"+ e.getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            },2000);
                                        }
                                    });
                        }
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
