package com.klcn.xuant.transporter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CancelBookActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout mLnOrtherReason, mLnFindOrtherCar;
    EditText mEditReasons;
    ImageView mImgback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_book);

        mLnOrtherReason = (LinearLayout) findViewById(R.id.ln_orther_reason);
        mEditReasons = (EditText) findViewById(R.id.edit_reasons);
        mImgback = (ImageView) findViewById(R.id.toolbar_back);
        mLnFindOrtherCar = (LinearLayout) findViewById(R.id.ln_find_other_car);

        mLnOrtherReason.setOnClickListener(this);
        mLnFindOrtherCar.setOnClickListener(this);

        mImgback.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ln_orther_reason:
                mEditReasons.setVisibility(View.VISIBLE);
                break;
            case R.id.ln_find_other_car:
                mEditReasons.setVisibility(View.GONE);
                break;
            case R.id.toolbar_back:
                finish();
                break;
        }

    }
}
