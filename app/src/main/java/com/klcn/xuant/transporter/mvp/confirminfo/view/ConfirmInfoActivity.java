package com.klcn.xuant.transporter.mvp.confirminfo.view;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.klcn.xuant.transporter.R;
import com.klcn.xuant.transporter.mvp.home.CustomerHomeActivity;


public class ConfirmInfoActivity extends AppCompatActivity implements View.OnClickListener{

    FloatingActionButton fab;
    EditText mEditPhone;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_info);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        mEditPhone = (EditText) findViewById(R.id.edit_phone);

        phoneNumber=getIntent().getStringExtra("EXTRA_PHONE");
        mEditPhone.setText("0"+phoneNumber.substring(3));
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab:
                Intent intent= new Intent(ConfirmInfoActivity.this, CustomerHomeActivity.class);
                startActivity(intent);
                finish();
                break;

        }
    }
}
