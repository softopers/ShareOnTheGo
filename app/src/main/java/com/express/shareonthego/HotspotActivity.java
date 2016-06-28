package com.express.shareonthego;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.Method;
import java.util.List;

public class HotspotActivity extends AppCompatActivity implements ConnectivityChangeReceiver.Listener {

    private WifiManager mWifiManager;
    private Button buttonStartHotspot;
    private ConnectivityChangeReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotspot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        receiver = new ConnectivityChangeReceiver();
        buttonStartHotspot = (Button) findViewById(R.id.buttonStartHotspot);
        if (buttonStartHotspot != null) {
            if (getIntent().getStringExtra("type").equals("receiver")) {
                buttonStartHotspot.setText("CONNECT TO HOTSPOT");
            }
            buttonStartHotspot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getIntent().getStringExtra("type").equals("sender")) {
                        setAndStartHotSpot(true, "Hotspot_Go");
                        startActivity(new Intent(getApplicationContext(), ConnectionActivity.class));
                    } else {
                        connectToHotspot("Hotspot_Go", "");
                        registerReceiver(
                                receiver,
                                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                        ConnectivityChangeReceiver.setListener(HotspotActivity.this);
                    }

                }
            });
        }
    }

    public void removeWifiNetwork(String ssid) {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (config.SSID.contains(ssid)) {
                    mWifiManager.disableNetwork(config.networkId);
                    mWifiManager.removeNetwork(config.networkId);
                }
            }
        }
        mWifiManager.saveConfiguration();
    }

    public boolean connectToHotspot(String netSSID, String netPass) {

//        isConnectToHotSpotRunning= true;
        WifiConfiguration wifiConf = new WifiConfiguration();
        List<ScanResult> scanResultList = mWifiManager.getScanResults();

        if (mWifiManager.isWifiEnabled()) {

            for (ScanResult result : scanResultList) {

                if (result.SSID.equals(netSSID)) {

                    removeWifiNetwork(result.SSID);
                    String mode = getSecurityMode(result);

                    if (mode.equalsIgnoreCase("OPEN")) {

                        wifiConf.SSID = "\"" + netSSID + "\"";
                        wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        int res = mWifiManager.addNetwork(wifiConf);
                        mWifiManager.disconnect();
                        mWifiManager.enableNetwork(res, true);
                        mWifiManager.reconnect();
                        mWifiManager.setWifiEnabled(true);
//                        isConnectToHotSpotRunning=false;
                        return true;

                    } else if (mode.equalsIgnoreCase("WEP")) {

                        wifiConf.SSID = "\"" + netSSID + "\"";
                        wifiConf.wepKeys[0] = "\"" + netPass + "\"";
                        wifiConf.wepTxKeyIndex = 0;
                        wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        wifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                        int res = mWifiManager.addNetwork(wifiConf);
                        mWifiManager.disconnect();
                        mWifiManager.enableNetwork(res, true);
                        mWifiManager.reconnect();
                        mWifiManager.setWifiEnabled(true);
//                        isConnectToHotSpotRunning=false;
                        return true;

                    } else {

                        wifiConf.SSID = "\"" + netSSID + "\"";
                        wifiConf.preSharedKey = "\"" + netPass + "\"";
                        wifiConf.hiddenSSID = true;
                        wifiConf.status = WifiConfiguration.Status.ENABLED;
                        wifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                        wifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        wifiConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                        wifiConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        wifiConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        wifiConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                        int res = mWifiManager.addNetwork(wifiConf);
                        mWifiManager.disconnect();
                        mWifiManager.enableNetwork(res, true);
                        mWifiManager.reconnect();
                        mWifiManager.saveConfiguration();
                        mWifiManager.setWifiEnabled(true);
//                        isConnectToHotSpotRunning=false;
                        return true;

                    }
                }
            }
        }
//        isConnectToHotSpotRunning=false;
        return false;
    }

    public String getSecurityMode(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] modes = {"WPA", "EAP", "WEP"};
        for (int i = modes.length - 1; i >= 0; i--) {
            if (cap.contains(modes[i])) {
                return modes[i];
            }
        }
        return "OPEN";
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
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onUIChanges() {
        startActivity(new Intent(getApplicationContext(), ConnectionActivity.class));
        finish();
    }
}
