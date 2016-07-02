package com.express.shareonthego;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.express.discovery.Discovery;
import com.express.transmitter.Transmitter;
import com.express.transmitter.TransmitterException;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadHelper;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class ShareOnTheGoApplication extends Application {

    public static Context CONTEXT;
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
        CONTEXT = this;

        // just for open the log in this demo project.

        /**
         * just for cache Application's Context, and ':filedownloader' progress will NOT be launched
         * by below code, so please do not worry about performance.
         * @see FileDownloader#init(Context)
         */
        FileDownloader.init(getApplicationContext(),
                new FileDownloadHelper.OkHttpClientCustomMaker() { // is not has to provide.
                    @Override
                    public OkHttpClient customMake() {
                        // just for OkHttpClient customize.
                        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
                        // you can set the connection timeout.
                        builder.connectTimeout(15_000, TimeUnit.MILLISECONDS);
                        // you can set the HTTP proxy.
                        builder.proxy(Proxy.NO_PROXY);
                        // etc.
                        return builder.build();
                    }
                });


        transmitter = new Transmitter();
        discovery = new Discovery();
    }

}
