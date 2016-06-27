package com.express.shareonthego;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.Method;

public class HotspotActivity extends AppCompatActivity {

    private WifiManager mWifiManager;
    private Button buttonStartHotspot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotspot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        buttonStartHotspot = (Button) findViewById(R.id.buttonStartHotspot);
        buttonStartHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAndStartHotSpot(true, "Hotspot_Go");
            }
        });
    }


    public boolean setAndStartHotSpot(boolean enable, String SSID) {
        //For simple implementation I am creating the open hotspot.
        Method[] mMethods = mWifiManager.getClass().getDeclaredMethods();
        for (Method mMethod : mMethods) {
            {
                if (mMethod.getName().equals("setWifiApEnabled")) {
                    WifiConfiguration netConfig = new WifiConfiguration();
                    netConfig.SSID = SSID;
                    netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    try {
                        mMethod.invoke(mWifiManager, netConfig, true);
                    } catch (Exception e) {
                        return false;
                    }
                    startHotSpot(enable);
                }
            }
        }
        return enable;
    }

    public boolean startHotSpot(boolean enable) {
        mWifiManager.setWifiEnabled(false);
        Method[] mMethods = mWifiManager.getClass().getDeclaredMethods();
        for (Method mMethod : mMethods) {
            if (mMethod.getName().equals("setWifiApEnabled")) {
                try {
                    mMethod.invoke(mWifiManager, null, enable);
                    startActivity(new Intent(getApplicationContext(), ConnectionActivity.class));
                    finish();
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            }
        }
        return false;
    }

}
