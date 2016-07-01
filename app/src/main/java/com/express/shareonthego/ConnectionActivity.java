package com.express.shareonthego;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidzeitgeist.ani.discovery.DiscoveryException;
import com.androidzeitgeist.ani.discovery.DiscoveryListener;
import com.express.shareonthego.shareviahttp.MyHttpServer;
import com.express.shareonthego.shareviahttp.UriInterpretation;
import com.express.shareonthego.shareviahttp.Util;
import com.express.shareonthego.spritzer.Spritzer;
import com.express.shareonthego.spritzer.SpritzerTextView;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.notification.BaseNotificationItem;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;

public class ConnectionActivity extends AppCompatActivity implements View.OnClickListener, DiscoveryListener {

    public static final int REQUEST_CODE = 1221;
    public final static String KEY_PAUSE = "key.filedownloader.notification.pause";

    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_NAME = "name";
    private final static String KEY_ID = "key.filedownloader.notification.id";

    public static String TAG = ConnectionActivity.class.getSimpleName();

    private static MyHttpServer httpServer = null;

    public BroadcastReceiver pauseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(KEY_PAUSE)) {
                final int id = intent.getIntExtra(KEY_ID, 0);
                FileDownloader.getImpl().pause(id);
            }
        }
    };
    private CharSequence[] listOfServerUris;
    private String preferredServerUrl;
    private LinearLayout linearQrCode;
    private SpritzerTextView textViewIpAddress;
    private TextView uriPath;
    private TextView textViewWifi;
    private boolean discoveryStarted = false;
    private FileDownloadNotificationHelper<NotificationItem> notificationHelper;
    private NotificationListener listener;
    private int downloadId = 0;
    //    private CheckBox showNotificationCb;
    private ProgressBar progressBar;

    private void clear() {
        /**
         * why not use {@link FileDownloadNotificationHelper#clear()} directly?
         */
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).
                cancel(downloadId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        notificationHelper = new FileDownloadNotificationHelper<>();

        assignViews();

//        showNotificationCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (!isChecked) {
//                    notificationHelper.clear();
//                    clear();
//                }
//            }
//        });

        ((ShareOnTheGoApplication) getApplicationContext()).discovery.setDisoveryListener(this);
        if (!discoveryStarted) {
            try {
                ((ShareOnTheGoApplication) getApplicationContext()).discovery.enable();
                discoveryStarted = true;
            } catch (DiscoveryException exception) {
                Log.d(TAG, "onResume: " + "* (!) Could not start discovery: " + exception.getMessage());
                discoveryStarted = false;
            }
        }
        Util.context = this;
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        textViewIpAddress = (SpritzerTextView) findViewById(R.id.textViewIpAddress);
        textViewIpAddress.setSpritzText("192.168.43.1:9999 192.168.0.4:9999 192.168.4.1:9999 192.168.4.11:9999 192.168.1.1:9999 192.168.43.15:9999 192.168.11.1:9999 192.168.25.25:9999.");
        textViewIpAddress.setWpm(1000);
        textViewIpAddress.play();

        textViewIpAddress.setOnCompletionListener(new Spritzer.OnCompletionListener() {
            @Override
            public void onComplete() {
                textViewIpAddress.play();
            }
        });

        uriPath = (TextView) findViewById(R.id.uriPath);

        textViewWifi = (TextView) findViewById(R.id.textViewWifi);

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("wifiInfo", wifiInfo.toString());
        Log.d("SSID", wifiInfo.getSSID());

        if (getIntent().getBooleanExtra("hotspot", false)) {
            textViewWifi.setText("Hotspot_Go");
        } else {
            textViewWifi.setText(wifiInfo.getSSID().replace("\"", ""));
        }

        linearQrCode = (LinearLayout) findViewById(R.id.linearQrCode);
        if (linearQrCode != null) {
            linearQrCode.setEnabled(false);
            linearQrCode.setOnClickListener(this);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    textViewIpAddress.pause();
                    boolean isKitKatOrHigher = getResources().getBoolean(R.bool.isKitKatOrHigher);
                    if (isKitKatOrHigher) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setType("*/*");
                        startActivityForResult(intent, REQUEST_CODE);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                }
            });
        }

        listener = new NotificationListener(new WeakReference<>(this));


//        if (downloadId != 0) {
//            // Avoid the task has passed 'pending' status, so we must create notification manually.
//            listener.addNotificationItem(downloadId);
//        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(KEY_PAUSE);
        registerReceiver(pauseReceiver, intentFilter);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private ArrayList<UriInterpretation> getFileUris(Intent data) {
        ArrayList<UriInterpretation> theUris = new ArrayList<UriInterpretation>();
        Uri dataUri = data.getData();
        if (dataUri != null) {
            theUris.add(new UriInterpretation(dataUri));
        } else {
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); ++i) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                theUris.add(new UriInterpretation(uri));
            }
        }
        return theUris;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            ArrayList<UriInterpretation> uriList = getFileUris(data);
            populateUriPath(uriList);
            initHttpServer(uriList);
            saveServerUrlToClipboard();
            setLinkMessageToView();
            linearQrCode.setEnabled(true);
            final Intent intent = new Intent();
            intent.putExtra(EXTRA_MESSAGE, preferredServerUrl);
            intent.putExtra(EXTRA_NAME, uriPath.getText().toString());
            new Thread() {
                public void run() {
                    ((ShareOnTheGoApplication) getApplicationContext()).transmitIntent(intent);
                }
            }.start();
        } else {
            textViewIpAddress.play();
        }
    }

    @Override
    public void onDiscoveryStarted() {
        Log.d(TAG, "onDiscoveryStarted: " + ("* (>) Discovery started"));
    }

    @Override
    public void onDiscoveryStopped() {
        Log.d(TAG, "onDiscoveryStopped: " + ("* (<) Discovery stopped"));
    }

    @Override
    public void onDiscoveryError(Exception exception) {
        Log.d(TAG, "onDiscoveryError: " + "* (!) Discovery error: " + exception.getMessage());
    }

    @Override
    public void onIntentDiscovered(InetAddress address, Intent intent) {
        if (!intent.hasExtra(EXTRA_MESSAGE)) {
            Log.d(TAG, "onIntentDiscovered: " + "* (!) Received Intent without message");
            return;
        }

        String message = intent.getStringExtra(EXTRA_MESSAGE);
        String name = intent.getStringExtra(EXTRA_NAME);
        String sender = address.getHostAddress();
        String savePath = null;
        if (message != null) {
            if (name != null) {
                savePath = FileDownloadUtils.getDefaultSaveRootPath() + File.separator + name;
            }

            downloadId = FileDownloader.getImpl().replaceListener("http://" + message, savePath, listener);

            if (downloadId == 0) {
                downloadId = FileDownloader.getImpl().create("http://" + message)
                        .setPath(savePath)
                        .setListener(listener)
                        .start();
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

//stuff that updates ui
                    progressBar.setVisibility(View.VISIBLE);
                }
            });

        }
        Log.d(TAG, "onIntentDiscovered: " + "<" + sender + "> " + message);
    }

    protected void saveServerUrlToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(preferredServerUrl, preferredServerUrl));

        Snackbar.make(findViewById(android.R.id.content), getString(R.string.url_clipboard), Snackbar.LENGTH_LONG).show();
    }

    protected void setLinkMessageToView() {
        textViewIpAddress.setText(preferredServerUrl);
    }

    protected void initHttpServer(ArrayList<UriInterpretation> myUris) {
        Util.context = this.getApplicationContext();
        if (myUris == null || myUris.size() == 0) {
            finish();
            return;
        }

        httpServer = new MyHttpServer(9999);
        listOfServerUris = httpServer.ListOfIpAddresses();
        preferredServerUrl = MyHttpServer.getLocalIpAddress() + ":9999";

        MyHttpServer.SetFiles(myUris);
    }

    protected void populateUriPath(ArrayList<UriInterpretation> uriList) {
        StringBuilder output = new StringBuilder();
        String sep = "\n";
        boolean first = true;
        for (UriInterpretation thisUriInterpretation : uriList) {
            if (first) {
                first = false;
            } else {
                output.append(sep);
            }
            output.append(thisUriInterpretation.getPath());
        }
        uriPath.setText(output.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linearQrCode:
                generateBarCodeIfPossible();
                break;
        }
    }

    public void generateBarCodeIfPossible() {
        // TODO: Create a QR-Code online and download the image
        // TODO: after the image was downloaded show the QR-Code in own activity
        Intent intent = new Intent("com.google.zxing.client.android.ENCODE");
        intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
        intent.putExtra("ENCODE_DATA", textViewIpAddress.getText().toString());
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.qr_code_not_available), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Utility.isWifiApEnabled()) {
            Utility.startHotSpot(false);
        } else if (Utility.isConnectedToAP(getApplicationContext())) {
            Utility.removeWifiNetwork("Hotspot_Go");
        }
        if (discoveryStarted) {
            ((ShareOnTheGoApplication) getApplicationContext()).discovery.disable();
        }
        unregisterReceiver(pauseReceiver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (downloadId != 0) {
                FileDownloader.getImpl().pause(downloadId);
            }
            notificationHelper.clear();
            clear();
        }
        return super.dispatchKeyEvent(event);
    }

    private void assignViews() {
//        showNotificationCb = (CheckBox) findViewById(R.id.show_notification_cb);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private static class NotificationListener extends FileDownloadNotificationListener {

        private WeakReference<ConnectionActivity> wActivity;

        public NotificationListener(WeakReference<ConnectionActivity> wActivity) {
            super(wActivity.get().notificationHelper);
            this.wActivity = wActivity;
        }

        @Override
        protected BaseNotificationItem create(BaseDownloadTask task) {
            return new NotificationItem(task.getId(), "Share on the GO", "downloading");
        }

        @Override
        public void addNotificationItem(BaseDownloadTask task) {
            super.addNotificationItem(task);
            if (wActivity.get() != null) {
//                wActivity.get().showNotificationCb.setEnabled(false);
            }
        }

        @Override
        public void destroyNotification(BaseDownloadTask task) {
            super.destroyNotification(task);
            if (wActivity.get() != null) {
//                wActivity.get().showNotificationCb.setEnabled(true);
                wActivity.get().downloadId = 0;
            }
        }

        @Override
        protected boolean interceptCancel(BaseDownloadTask task,
                                          BaseNotificationItem n) {
            // in this demo, I don't want to cancel the notification, just show for the test
            // so return true
            return true;
        }

        @Override
        protected boolean disableNotification(BaseDownloadTask task) {
            if (wActivity.get() != null) {
//                return !wActivity.get().showNotificationCb.isChecked();
            }

            return super.disableNotification(task);
        }

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);
            if (wActivity.get() != null) {
                wActivity.get().progressBar.setIndeterminate(true);
            }
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);
            if (wActivity.get() != null) {
                wActivity.get().progressBar.setIndeterminate(false);
                wActivity.get().progressBar.setMax(totalBytes);
                wActivity.get().progressBar.setProgress(soFarBytes);
            }
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            super.completed(task);
            if (wActivity.get() != null) {
                wActivity.get().progressBar.setIndeterminate(false);
                wActivity.get().progressBar.setVisibility(View.GONE);
                wActivity.get().progressBar.setProgress(task.getSmallFileTotalBytes());
            }
        }
    }

    public static class NotificationItem extends BaseNotificationItem {

        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        private NotificationItem(int id, String title, String desc) {
            super(id, title, desc);
            Intent[] intents = new Intent[2];
            intents[0] = Intent.makeMainActivity(new ComponentName(ShareOnTheGoApplication.CONTEXT,
                    MainActivity.class));
            intents[1] = new Intent(ShareOnTheGoApplication.CONTEXT, ConnectionActivity.class);

            this.pendingIntent = PendingIntent.getActivities(ShareOnTheGoApplication.CONTEXT, 0, intents,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder = new NotificationCompat.
                    Builder(FileDownloadHelper.getAppContext());
            Intent pauseIntent = new Intent(KEY_PAUSE);
            pauseIntent.putExtra(KEY_ID, getId());
            PendingIntent pausePendingIntent = PendingIntent.getBroadcast(ShareOnTheGoApplication.CONTEXT,
                    0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setDefaults(Notification.DEFAULT_LIGHTS)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setContentTitle(getTitle())
                    .setContentText(desc)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .addAction(R.mipmap.ic_launcher, "pause", pausePendingIntent);

        }

        @Override
        public void show(boolean statusChanged, int status, boolean isShowProgress) {

            String desc = getDesc();
            switch (status) {
                case FileDownloadStatus.pending:
                    desc += " pending";
                    break;
                case FileDownloadStatus.started:
                    desc += " started";
                    break;
                case FileDownloadStatus.progress:
                    desc += " progress";
                    break;
                case FileDownloadStatus.retry:
                    desc += " retry";
                    break;
                case FileDownloadStatus.error:
                    desc += " error";
                    break;
                case FileDownloadStatus.paused:
                    desc += " paused";
                    break;
                case FileDownloadStatus.completed:
                    desc += " completed";
                    break;
                case FileDownloadStatus.warn:
                    desc += " warn";
                    break;
            }

            builder.setContentTitle(getTitle())
                    .setContentText(desc);


            if (statusChanged) {
                builder.setTicker(desc);
            }

            builder.setProgress(getTotal(), getSofar(), !isShowProgress);
            getManager().notify(getId(), builder.build());
        }

        @Override
        public void cancel() {
            super.cancel();
        }
    }
}
