package com.klcn.xuant.transporter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class DriverLoginActivity extends AppCompatActivity implements View.OnClickListener{

//    @BindView(R.id.edt_email)
//    EditText mEdtEmail;
//
    @BindView(R.id.root_layout)
    RelativeLayout mRootLayout;

    @BindView(R.id.btn_sign_in)
    Button mBtnSignIn;

    @BindView(R.id.btn_register)
    Button mBtnRegister;

    @BindView(R.id.txt_forgot_pass)
    TextView mTxtForgotPass;

    private int REQUEST_REGISTER_CODE = 2896;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private FirebaseDatabase db;
    private DatabaseReference drivers;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                            .setDefaultFontPath("fonts/Arkhip_font.ttf")
                                            .setFontAttrId(R.attr.fontPath)
                                            .build());
        setContentView(R.layout.activity_driver_login);
        ButterKnife.bind(this);

        // Init Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        drivers = db.getReference(Common.drivers_tbl);

//        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                if(user!=null){
//                    Intent intent = new Intent(getApplicationContext(), DriverMainActivity.class);
//                    startActivity(intent);
//                    finish();
//                }
//            }
//        };

        // Init view
        mBtnSignIn.setOnClickListener(this);
        mTxtForgotPass.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sign_in:
                showSignInDialog();
                break;
            case R.id.txt_forgot_pass:
                showForgotPassDialog();
                break;
            case R.id.btn_register:
                Intent intent = new Intent(DriverLoginActivity.this,DriverRegisterActivity.class);
                startActivityForResult(intent,REQUEST_REGISTER_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REGISTER_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Intent intent = new Intent(DriverLoginActivity.this,DriverHomeActivity.class);
                startActivity(intent);
                finish();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
            }
        }
    }

    private void showForgotPassDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("FORGOT PASSWORD");
        builder.setMessage("Enter your email address");
        builder.setCancelable(true);

        LayoutInflater inflater = LayoutInflater.from(this);
        View registerLayout = inflater.inflate(R.layout.layout_forgot_password,null);

        builder.setView(registerLayout);
        final AlertDialog dialog;
        dialog = builder.create();

        final MaterialEditText editEmail = registerLayout.findViewById(R.id.edt_email_sign_in);
        final TextView txtReset = registerLayout.findViewById(R.id.txt_reset);
        final TextView txtCancel = registerLayout.findViewById(R.id.txt_cancel);

        // Set button dialog
        txtReset.setOnClickListener(view -> {
            if(TextUtils.isEmpty(editEmail.getText())){
                Toast.makeText(getApplicationContext(), "Please enter your email",Toast.LENGTH_SHORT)
                        .show();
            }else if(!isEmailValid(editEmail.getText().toString())){
                Toast.makeText(getApplicationContext(), "Email not valid",Toast.LENGTH_SHORT)
                        .show();
            }else{
                final SpotsDialog waitingDialog = new SpotsDialog(DriverLoginActivity.this);
                dialog.dismiss();
                waitingDialog.show();

                FirebaseAuth.getInstance().sendPasswordResetEmail(editEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                waitingDialog.dismiss();
                                Snackbar.make(mRootLayout,"Check your mail to reset password",Snackbar.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(mRootLayout,""+e.getMessage(),Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });

        txtCancel.setOnClickListener(view -> dialog.dismiss());

        dialog.show();

    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void showSignInDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SIGN IN");
        builder.setCancelable(true);

        LayoutInflater inflater = LayoutInflater.from(this);
        View signInLayout = inflater.inflate(R.layout.layout_sign_in_driver,null);

        builder.setView(signInLayout);
        final AlertDialog dialog;
        dialog = builder.create();

        final MaterialEditText editEmail = signInLayout.findViewById(R.id.edt_email_sign_in);
        final MaterialEditText editPassword = signInLayout.findViewById(R.id.edt_pass_sign_in);
        final TextView txtSignIn = signInLayout.findViewById(R.id.txt_sign_in);
        final TextView txtCancel = signInLayout.findViewById(R.id.txt_cancel);

        // Set button dialog
        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(editEmail.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Please enter email address",Toast.LENGTH_SHORT)
                            .show();
                }else if(TextUtils.isEmpty(editPassword.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Please enter password",Toast.LENGTH_SHORT)
                            .show();
                }else {
                    final SpotsDialog waitingDialog = new SpotsDialog(DriverLoginActivity.this);
                    dialog.dismiss();
                    waitingDialog.show();
                    mFirebaseAuth.signInWithEmailAndPassword(editEmail.getText().toString(),editPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull final Task<AuthResult> task) {
                                    if(!task.isSuccessful()){
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                waitingDialog.dismiss();
                                                Snackbar.make(mRootLayout, ""+task.getException().getMessage(),
                                                        Snackbar.LENGTH_LONG).show();
                                            }
                                        },2000);

                                    }else{
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                waitingDialog.dismiss();
                                                Snackbar.make(mRootLayout, "Sign in success ",
                                                        Snackbar.LENGTH_SHORT).show();
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intentHome = new Intent(DriverLoginActivity.this, DriverHomeActivity.class);
                                                        startActivity(intentHome);
                                                        finish();
                                                    }
                                                },1000);
                                            }
                                        },2000);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            waitingDialog.dismiss();
                            Snackbar.make(mRootLayout, ""+e.getMessage(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mFirebaseAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mFirebaseAuth.addAuthStateListener(firebaseAuthStateListener);
    }
}
