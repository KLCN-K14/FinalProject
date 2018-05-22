package com.klcn.xuant.transporter.mvp.history;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.klcn.xuant.transporter.R;

public class ItemHistoryActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView mImgback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_history);

        mImgback = (ImageView) findViewById(R.id.toolbar_back);
        mImgback.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.toolbar_back:
                finish();
                break;
        }
    }
}
