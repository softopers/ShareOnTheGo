package com.express.shareonthego;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity implements View.OnClickListener //DiscoveryListener
{

    private static final String EXTRA_MESSAGE = "message";
    public static String TAG = MainActivity.class.getSimpleName();
    private boolean discoveryStarted;
    private Button buttonSend;
    private Button buttonConnect;
    private Button buttonReceive;

    public static String getWifiApIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && (inetAddress.getAddress().length == 4)) {
                            Log.d("TAG", inetAddress.getHostAddress());
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("TAG", ex.toString());
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        ((ShareOnTheGoApplication) getApplicationContext()).discovery.setDisoveryListener(this);

        buttonSend = (Button) findViewById(R.id.buttonSend);
        if (buttonSend != null) {
            buttonSend.setOnClickListener(this);
        }

        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        if (buttonConnect != null) {
            buttonConnect.setOnClickListener(this);
        }

        buttonReceive = (Button) findViewById(R.id.buttonReceive);
        if (buttonReceive != null) {
            buttonReceive.setOnClickListener(this);
        }
//        final Intent intent = new Intent();
//        intent.putExtra(EXTRA_MESSAGE, "hiiiii");
//        new Thread() {
//            public void run() {
//                ((ShareOnTheGoApplication) getApplicationContext()).transmitIntent(intent);
//            }
//        }.start();

    }
//
//    @Override
//    public void onDiscoveryStarted() {
//        Log.d(TAG, "onDiscoveryStarted: " + ("* (>) Discovery started"));
//    }
//
//    @Override
//    public void onDiscoveryStopped() {
//        Log.d(TAG, "onDiscoveryStopped: " + ("* (<) Discovery stopped"));
//    }
//
//    @Override
//    public void onDiscoveryError(Exception exception) {
//        Log.d(TAG, "onDiscoveryError: " + "* (!) Discovery error: " + exception.getMessage());
//    }
//
//    @Override
//    public void onIntentDiscovered(InetAddress address, Intent intent) {
//        if (!intent.hasExtra(EXTRA_MESSAGE)) {
//            Log.d(TAG, "onIntentDiscovered: " + "* (!) Received Intent without message");
//            return;
//        }
//
//        String message = intent.getStringExtra(EXTRA_MESSAGE);
//        String sender = address.getHostAddress();
//
//        Log.d(TAG, "onIntentDiscovered: " + "<" + sender + "> " + message);
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
//
//        try {
//            ((ShareOnTheGoApplication) getApplicationContext()).discovery.enable();
//            discoveryStarted = true;
//        } catch (DiscoveryException exception) {
//            Log.d(TAG, "onResume: " + "* (!) Could not start discovery: " + exception.getMessage());
//            discoveryStarted = false;
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (discoveryStarted) {
//            ((ShareOnTheGoApplication) getApplicationContext()).discovery.disable();
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonConnect:
                startActivity(new Intent(getApplicationContext(), ConnectionActivity.class));
                break;
            case R.id.buttonSend:
                startActivity(new Intent(getApplicationContext(), HotspotActivity.class).putExtra("type", "sender"));
                break;
            case R.id.buttonReceive:
                startActivity(new Intent(getApplicationContext(), HotspotActivity.class).putExtra("type", "receiver"));
                break;
        }
    }
}
