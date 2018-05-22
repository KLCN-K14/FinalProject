package com.klcn.xuant.transporter.mvp.verifypincode.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.mvp.confirminfo.view.ConfirmInfoActivity;
import com.klcn.xuant.transporter.mvp.home.CustomerHomeActivity;
import com.klcn.xuant.transporter.mvp.signup.view.CustomerSignUpActivity;

import java.util.concurrent.TimeUnit;

public class VerifyPincodeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "VerifyActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    TextView mTxtEditNumber;
    TextView mTxtPhoneNumber;
    private PinEntryEditText mEditPinCode;

    private CountDownTimer countDownTimer;
    String phone;
    private TextView mStatusText;
    private TextView mDetailText;
    TextView mTxtResendCode;

    DatabaseReference postRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_pincode);



        mTxtEditNumber = (TextView) findViewById(R.id.txt_edit_number);
        mEditPinCode = (PinEntryEditText) findViewById(R.id.edit_pincode);
        mTxtPhoneNumber = (TextView) findViewById(R.id.txt_phone);
        mStatusText = (TextView) findViewById(R.id.status);
        mDetailText = (TextView) findViewById(R.id.detail);
        mTxtResendCode = (TextView) findViewById(R.id.txt_request_new_code);

        mTxtResendCode.setOnClickListener(this);

        mTxtEditNumber.setOnClickListener(this);


        phone = getIntent().getStringExtra("EXTRA_PHONE");
        mTxtPhoneNumber.setText(phone);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        postRef = FirebaseDatabase.getInstance().getReference("Customers");


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(final PhoneAuthCredential credential) {

                Log.e(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;

                Toast.makeText(VerifyPincodeActivity.this, "Verification Complete", Toast.LENGTH_SHORT).show();

                updateUI(STATE_VERIFY_SUCCESS, credential);

                if (credential != null) {
                        Log.e(TAG, "Credential != null");

                    if (credential.getSmsCode() != null) {
                        Log.e("STATE_VERIFY_SUCCESS", credential.getSmsCode());
                        mEditPinCode.setText(credential.getSmsCode());
                        Log.e("Pincode edittext:::", mEditPinCode.getText().toString());

                        if (mEditPinCode != null) {

                            if (mEditPinCode.getText().toString().equals(credential.getSmsCode())) {

                                verifyPhoneNumberWithCode(mVerificationId, mEditPinCode.getText().toString());

                                // checkExistUser()
                                postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for(DataSnapshot data: dataSnapshot.getChildren()){
                                            if (data.child(phone).exists()) {
                                                Intent intentHome = new Intent(VerifyPincodeActivity.this, CustomerHomeActivity.class);
                                                startActivity(intentHome);
                                                finish();
                                            } else {
                                                Intent intent = new Intent(VerifyPincodeActivity.this, ConfirmInfoActivity.class);
                                                intent.putExtra("EXTRA_PHONE", phone);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }

                                });
                                Toast.makeText(VerifyPincodeActivity.this, "SUCCESS", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VerifyPincodeActivity.this, "FAIL", Toast.LENGTH_SHORT).show();
                                mEditPinCode.setText(null);
                            }
                        }
                    } else {
                        mEditPinCode.setText(null);

                    }
                }

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;

                Toast.makeText(VerifyPincodeActivity.this, "Verification Failed" + e.getMessage(), Toast.LENGTH_SHORT).show();

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request

                    mEditPinCode.setError("Invalid phone number.");

                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded

                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();

                }

                updateUI(STATE_VERIFY_FAILED);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                // Log.d(TAG, "onCodeSent:" + verificationId);
                Toast.makeText(VerifyPincodeActivity.this,"Verification code has been send on your number",Toast.LENGTH_SHORT).show();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;


                updateUI(STATE_CODE_SENT);
            }
        };
        startPhoneNumberVerification(phone);


    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // [START_EXCLUDE]
        if (mVerificationInProgress) {
            startPhoneNumberVerification(phone);
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

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


    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
        mStatusText.setVisibility(View.INVISIBLE);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                mEditPinCode.setError("Invalid code.");
                            }
                            // Update UI
                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });

    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, final PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                mDetailText.setText(R.string.status_code_sent);
                mDetailText.setTextColor(Color.parseColor("#43a047"));
                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                Log.e("STATE_VERIFY_FAILED::::", "");
                mDetailText.setText(R.string.status_verification_failed);
                mDetailText.setTextColor(Color.parseColor("#dd2c00"));
                break;
            case STATE_VERIFY_SUCCESS:

//                mDetailText.setText("Verfication Sucessfull");
//                mDetailText.setTextColor(Color.parseColor("#43a047"));

                if (cred != null) {

                    if (cred.getSmsCode() != null) {
                        Log.e("STATE_VERIFY_SUCCESS", cred.getSmsCode());
                        mEditPinCode.setText(cred.getSmsCode());
                        Log.e("Pincode edittext:::", mEditPinCode.getText().toString());
                        if (mEditPinCode != null) {
                            mEditPinCode.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                                @Override
                                public void onPinEntered(CharSequence str) {
                                    if (str.toString().equals(cred.getSmsCode())) {
                                        verifyPhoneNumberWithCode(mVerificationId, mEditPinCode.getText().toString());
                                        Toast.makeText(VerifyPincodeActivity.this, "SUCCESS", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(VerifyPincodeActivity.this, "FAIL", Toast.LENGTH_SHORT).show();
                                        mEditPinCode.setText(null);
                                    }
                                }
                            });
                        }
                    } else {
                        mEditPinCode.setText(null);

                    }
                }
                // [END phone_auth_callbacks]


                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                mDetailText.setText(R.string.status_sign_in_failed);
                mDetailText.setTextColor(Color.parseColor("#dd2c00"));
                Toast.makeText(VerifyPincodeActivity.this, "Sign in FAILED", Toast.LENGTH_SHORT).show();
                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check
                mStatusText.setText(R.string.signed_in);
                Toast.makeText(VerifyPincodeActivity.this, "Sign in SUCCESS", Toast.LENGTH_SHORT).show();
                break;
        }

        if (user == null) {
            // Signed out

            mStatusText.setText(R.string.signed_out);
        } else {
            // Signed in
//            Intent intent = new Intent(this, ConfirmInfoActivity.class);
//            startActivity(intent);
//            finish();

        }
    }

    private void startResendCountdown() {
        mTxtResendCode.setEnabled(false);

        countDownTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                long timerProgress = millisUntilFinished / 1000;

                if (!isFinishing()) {
                    mTxtResendCode.setTextColor(getResources().getColor(R.color.gray_50_percent));
                    mTxtResendCode.setText(getResources().getString(R.string.request_new_code_in) + timerProgress);
                }
            }

            public void onFinish() {
                if (!isFinishing()) {
                    mTxtResendCode.setText(getResources().getString(R.string.request_new_code));
                    mTxtResendCode.setEnabled(true);
                    mTxtResendCode.setTextColor(getResources().getColor(R.color.colorYellow));
                }
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_edit_number:
                Intent phoneActivity = new Intent(this, CustomerSignUpActivity.class);
                startActivity(phoneActivity);
                finish();
                break;
            case R.id.txt_request_new_code:
                startResendCountdown();
                resendVerificationCode(phone, mResendToken);
                break;
        }

    }



}
