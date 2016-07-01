package com.express.shareonthego;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class HotspotActivity extends AppCompatActivity implements ConnectivityChangeReceiver.Listener {

    private WifiManager mWifiManager;
    private Button buttonStartHotspot;
    private ConnectivityChangeReceiver receiver;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotspot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiManager.disconnect();

        receiver = new ConnectivityChangeReceiver();
        buttonStartHotspot = (Button) findViewById(R.id.buttonStartHotspot);
        if (buttonStartHotspot != null) {
            if (getIntent().getStringExtra("type").equals("receiver")) {
                buttonStartHotspot.setText("CONNECT TO HOTSPOT");
            }
            buttonStartHotspot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (getIntent().getStringExtra("type").equals("sender")) {
                        Utility.setAndStartHotSpot(getApplicationContext(), true, "Hotspot_Go");
                        startActivity(new Intent(getApplicationContext(), ConnectionActivity.class).putExtra("hotspot", true));
                        finish();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        Utility.connectToHotspot(getApplicationContext(), "Hotspot_Go", "");
                        registerReceiver(
                                receiver,
                                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                        ConnectivityChangeReceiver.setListener(HotspotActivity.this);
                    }

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onUIChanges() {
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(getApplicationContext(), ConnectionActivity.class));
        finish();
    }
}
