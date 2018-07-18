package com.klcn.xuant.transporter.mvp.confirminfo.view;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.model.Customer;
import com.klcn.xuant.transporter.mvp.home.CustomerHomeActivity;

import java.util.HashMap;


public class ConfirmInfoActivity extends AppCompatActivity implements View.OnClickListener {

    FloatingActionButton fab;
    EditText mEditPhone, mEditName, mEditFisrtName, mEditEmail;
    String phoneNumber;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference customers;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_info);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        mEditPhone = (EditText) findViewById(R.id.edit_phone);
        mEditName = (EditText) findViewById(R.id.edit_name);
        mEditFisrtName = (EditText) findViewById(R.id.edit_fisrt_name);
        mEditEmail = (EditText) findViewById(R.id.edit_mail);

        phoneNumber = getIntent().getStringExtra("EXTRA_PHONE");
        mEditPhone.setText("0" + phoneNumber.substring(3));


        fab.setOnClickListener(this);
        mFirebaseAuth= FirebaseAuth.getInstance();
        customers = FirebaseDatabase.getInstance().getReference().child("Customers");

    }

    private void validateEmail() {
        final String email = mEditEmail.getText().toString();
        mEditEmail.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

                if (email.matches(emailPattern) && s.length() > 0) {
                    Toast.makeText(getApplicationContext(), "valid email address", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // other stuffs
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // other stuffs
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                Log.e("ConfirmInfo:",mFirebaseAuth.getCurrentUser().getUid());

                if (TextUtils.isEmpty(mEditFisrtName.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Please enter your first name", Toast.LENGTH_SHORT)
                            .show();
                } else if (TextUtils.isEmpty(mEditName.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Please enter your name", Toast.LENGTH_SHORT)
                            .show();
                } else if (TextUtils.isEmpty(mEditEmail.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Please enter your email", Toast.LENGTH_SHORT)
                            .show();
                } else if (TextUtils.isEmpty(mEditPhone.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Please enter phone number", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Customer customer = new Customer();
                    customer.setName(mEditFisrtName.getText().toString() + " " + mEditName.getText().toString());
                    customer.setPhoneNum(phoneNumber);
                    customer.setEmail(mEditEmail.getText().toString());
                    customer.setCountCancel(0);
                    customer.setCountTrip(0);
                    customer.setImgUrl(null);
                    customers.child(mFirebaseAuth.getCurrentUser().getUid())
                            .setValue(customer)
                            .addOnSuccessListener(aVoid -> {

                                HashMap<String,Object> maps = new HashMap<>();
                                maps.put("dateCreated", ServerValue.TIMESTAMP);

                                customers.child(mFirebaseAuth.getCurrentUser().getUid())
                                        .updateChildren(maps)
                                        .addOnCompleteListener(task -> {
                                            Toast.makeText(ConfirmInfoActivity.this, "Register success", Toast.LENGTH_LONG)
                                                    .show();

                                            Intent intent = new Intent(ConfirmInfoActivity.this, CustomerHomeActivity.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show());


                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ConfirmInfoActivity.this, "Create fail! " + e.getMessage(), Toast.LENGTH_LONG)
                                            .show();
                                }
                            });
                }

                break;

        }
    }
}
