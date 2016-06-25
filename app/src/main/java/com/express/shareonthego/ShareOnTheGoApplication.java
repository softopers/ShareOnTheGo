package com.express.shareonthego;

import android.app.Application;
import android.content.Intent;

import com.androidzeitgeist.ani.discovery.Discovery;
import com.androidzeitgeist.ani.transmitter.Transmitter;
import com.androidzeitgeist.ani.transmitter.TransmitterException;

public class ShareOnTheGoApplication extends Application {

    public Transmitter transmitter;
    public Discovery discovery;

    public void transmitIntent(final Intent intent) {
        try {
            transmitter.transmit(intent);
        } catch (TransmitterException exception) {
            exception.getMessage();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        transmitter = new Transmitter();
        discovery = new Discovery();
    }

}
