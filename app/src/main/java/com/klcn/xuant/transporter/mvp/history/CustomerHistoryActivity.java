package com.klcn.xuant.transporter.mvp.history;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;

import com.klcn.xuant.transporter.R;

public class CustomerHistoryActivity extends AppCompatActivity implements View.OnClickListener{

    String[] listDistination= {"145 Trần Não", "Vimcom Quận 9"};
    ImageView mImgback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_history);

        mImgback = (ImageView) findViewById(R.id.toolbar_back);
        ListView listView = (ListView) findViewById(R.id.list_history);
        ListHistoryAdapter listHistoryAdapter = new ListHistoryAdapter(this,listDistination );
        listView.setAdapter(listHistoryAdapter);

        mImgback.setOnClickListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(CustomerHistoryActivity.this,ItemHistoryActivity.class);
                startActivity(intent);
            }
        });


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
