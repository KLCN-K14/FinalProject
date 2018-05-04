package com.klcn.xuant.transporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.klcn.xuant.transporter.model.Driver;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DriverLoginActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.edt_email)
    EditText mEdtEmail;

    @BindView(R.id.edt_password)
    EditText mEdtPass;

    @BindView(R.id.btn_login)
    Button mBtnLogin;

    @BindView(R.id.btn_registration)
    Button mBtnRegistration;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(getApplicationContext(), DriverMainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mBtnLogin.setOnClickListener(this);
        mBtnRegistration.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final String email = mEdtEmail.getText().toString();
        final String password = mEdtPass.getText().toString();
        switch (view.getId()){
            case R.id.btn_login:

                mFirebaseAuth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(),"Sign in fail", Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Sign in successful", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                break;
            case R.id.btn_registration:

                mFirebaseAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()){
                                    FirebaseAuthException e = (FirebaseAuthException)task.getException();
                                    Toast.makeText(getApplicationContext(),"Sign up fail: "+e.getMessage(), Toast.LENGTH_LONG).show();
                                }else{
                                    String userID = mFirebaseAuth.getCurrentUser().getUid();
                                    Driver driver = new Driver(email,password);
                                    DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference()
                                            .child("Drivers").child(userID);
                                    dbUser.setValue(driver);
                                    Toast.makeText(getApplicationContext(),"Create user successful", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAuth.addAuthStateListener(firebaseAuthStateListener);
    }
}
