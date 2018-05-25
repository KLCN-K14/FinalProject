package com.klcn.xuant.transporter.mvp.signup.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;
import com.klcn.xuant.transporter.DriverMainActivity;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.mvp.home.CustomerHomeActivity;
import com.klcn.xuant.transporter.mvp.verifypincode.view.TestVerify;
import com.klcn.xuant.transporter.mvp.verifypincode.view.VerifyPincodeActivity;


public class CustomerSignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PhoneAuthActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;


    private TextView mDetailText;

    private EditText mPhoneNumberField;
    private EditText mVerificationField;

    private FloatingActionButton mFloatingAction;
    private RelativeLayout mTopBackground;
    private TextView mTxtSocialNetwork;
    private RelativeLayout mProgressBarGroup;

    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    ProgressBar progressBar;
    CountryCodePicker countryCodePicker;
    String phoneStr="01645059996";
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab!=null)
        {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        checkIsLogged();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        mPhoneNumberField = (EditText) findViewById(R.id.edit_phone);
        mFloatingAction = (FloatingActionButton) findViewById(R.id.fab);

        mTopBackground = (RelativeLayout) findViewById(R.id.top_background);
        mTxtSocialNetwork = (TextView) findViewById(R.id.txt_social_network);
        mDetailText = (TextView) findViewById(R.id.detail);
        mProgressBarGroup = (RelativeLayout) findViewById(R.id.rl_progressbar_group);
        countryCodePicker = (CountryCodePicker) findViewById(R.id.area_code);

        // Assign click listeners
        mPhoneNumberField.setOnClickListener(this);
        mFloatingAction.setOnClickListener(this);
        mFloatingAction.setVisibility(View.GONE);

        String countryNameCode;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            countryNameCode = this.getResources().getConfiguration().getLocales().get(0).getCountry();
        } else {
            countryNameCode = this.getResources().getConfiguration().locale.getCountry();
        }

        countryCodePicker.setCountryForNameCode(countryNameCode);

    }

    private void checkIsLogged() {
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent intentHome = new Intent(CustomerSignUpActivity.this, CustomerHomeActivity.class);
            startActivity(intentHome);
            finish();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }


    private void updateUI(int uiState) {
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
                mDetailText.setText(null);
                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                mDetailText.setText(R.string.status_verification_failed);
                mDetailText.setTextColor(Color.parseColor("#dd2c00"));
                progressBar.setVisibility(View.INVISIBLE);
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                mDetailText.setText("Verfication Sucessfull");
                mDetailText.setTextColor(Color.parseColor("#43a047"));
                progressBar.setVisibility(View.INVISIBLE);

                // Set the verification text based on the credential


                break;

        }

    }

    private boolean validatePhoneNumber() {
        String phoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberField.setError("Invalid phone number.");
            //mPhoneNumberField.setTextColor(Color.parseColor("#ff1744"));
            return false;
        }

        return true;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (!validatePhoneNumber()) {
                    return;
                }

                ///////hide keyboard start
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                /////////hide keyboard end


                //mStatusText.setText("Authenticating....!");
                progressBar.setVisibility(View.VISIBLE);

                String countryCode = countryCodePicker.getSelectedCountryCode();
                String phoneNumber = mPhoneNumberField.getText().toString().trim();
                if (phoneNumber.charAt(0) == '0') {
                    phoneNumber = phoneNumber.substring(1);
                }

                phoneStr = "+"+countryCode + phoneNumber;

                String regexStr = "^[+][0-9]{10,13}$";

                if(phoneStr.length()<10 || phoneStr.length()>13 || phoneStr.matches(regexStr)==false  ) {
                    updateUI(STATE_VERIFY_FAILED);
                }else {
                    updateUI(STATE_VERIFY_SUCCESS);
                    Intent intent = new Intent(this, TestVerify.class);
                    intent.putExtra("EXTRA_PHONE", phoneStr);
                    startActivity(intent);
                    finish();
                }


                Log.e("on click fab:::::",phoneStr);



                break;
            case R.id.edit_phone:

                mFloatingAction.setVisibility(View.VISIBLE);
                mTopBackground.setVisibility(View.GONE);
                mProgressBarGroup.setVisibility(View.VISIBLE);
                mTxtSocialNetwork.setVisibility(View.GONE);

                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}