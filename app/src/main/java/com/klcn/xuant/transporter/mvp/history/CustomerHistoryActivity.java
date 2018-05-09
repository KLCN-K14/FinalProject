package com.klcn.xuant.transporter.mvp.history;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TabHost;

import com.klcn.xuant.transporter.R;

public class CustomerHistoryActivity extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_history);

        final TabHost tabHost = getTabHost();

        TabHost.TabSpec datxespec = tabHost.newTabSpec("DatXe");
        datxespec.setIndicator("Đặt xe", getResources().getDrawable(R.drawable.ic_menu_camera));
        Intent photosIntent = new Intent(this, HistoryBookCarActivity.class);
        datxespec.setContent(photosIntent);

        TabHost.TabSpec orderspec = tabHost.newTabSpec("Orders");
        orderspec.setIndicator("Orders", getResources().getDrawable(R.drawable.ic_menu_gallery));
        Intent songsIntent = new Intent(this, HistoryOrdersActivity.class);
        orderspec.setContent(songsIntent);

        //Thêm các TabSpec trên vào TabHost
        tabHost.addTab(datxespec);
        tabHost.addTab(orderspec);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            public void onTabChanged(String arg0) {
                for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
                    tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff")); // unselected
                }
                tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(Color.parseColor("#fbbf20")); // selected

            }
        });
    }
}
