package com.express.shareonthego;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;
import java.util.List;

public class Utility {
    private static WifiManager mWifiManager;

    public static boolean setAndStartHotSpot(Context context, boolean enable, String SSID) {
        //For simple implementation I am creating the open hotspot.
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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

    public static boolean startHotSpot(boolean enable) {
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

    public static boolean connectToHotspot(Context context, String netSSID, String netPass) {

//        isConnectToHotSpotRunning= true;
        WifiConfiguration wifiConf = new WifiConfiguration();
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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

    public static boolean isWifiApEnabled() {
        if (mWifiManager != null) {
            try {
                Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
                return (Boolean) method.invoke(mWifiManager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void removeWifiNetwork(String ssid) {
        if (mWifiManager != null) {
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
    }

    public static String getSecurityMode(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] modes = {"WPA", "EAP", "WEP"};
        for (int i = modes.length - 1; i >= 0; i--) {
            if (cap.contains(modes[i])) {
                return modes[i];
            }
        }
        return "OPEN";
    }

    public static boolean isConnectedToAP(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (info != null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }

}
