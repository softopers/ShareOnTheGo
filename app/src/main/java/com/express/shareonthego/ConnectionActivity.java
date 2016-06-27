package com.express.shareonthego;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.express.shareonthego.shareviahttp.MyHttpServer;
import com.express.shareonthego.shareviahttp.UriInterpretation;
import com.express.shareonthego.shareviahttp.Util;

import java.util.ArrayList;

public class ConnectionActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 1221;
    private static MyHttpServer httpServer = null;
    private CharSequence[] listOfServerUris;
    private String preferredServerUrl;
    private LinearLayout linearQrCode;
    private TextView textViewIpAddress;
    private TextView uriPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        textViewIpAddress = (TextView) findViewById(R.id.textViewIpAddress);
        uriPath = (TextView) findViewById(R.id.uriPath);

        linearQrCode = (LinearLayout) findViewById(R.id.linearQrCode);
        if (linearQrCode != null) {
            linearQrCode.setOnClickListener(this);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
        }
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

        httpServer.SetFiles(myUris);
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
}
