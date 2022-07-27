package com.psr.financial;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
//import android.support.v4.content.FileProvider;
//import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.net.URLConnection;

public class WebviewLayout extends AppCompatActivity {

    String name, fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);

        if (getIntent().getExtras().containsKey("name")) {
            name = getIntent().getExtras().getString("name");
        }
        if (getIntent().getExtras().containsKey("fileName")) {
            fileName =  getIntent().getExtras().getString("fileName");
        }

        WebView urlWebView = (WebView)findViewById(R.id.containWebView);
        urlWebView.setWebViewClient(new AppWebViewClients());
        urlWebView.getSettings().setJavaScriptEnabled(true);
        urlWebView.getSettings().setUseWideViewPort(true);

        String excelFile = (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + CustomerDetailsActivity.FOLDER_NAME + File.separator + fileName);
        File file = new File(excelFile);
        String fileUrl = file.getAbsolutePath();
        urlWebView.loadUrl("http://docs.google.com/gview?embedded=true&url="+ fileUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.webview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.share_button:
                shareFile();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareFile() {
        String excelFile = (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + CustomerDetailsActivity.FOLDER_NAME + File.separator + fileName);
        File file = new File(excelFile);

        //System.out.println("file://"+file.getAbsolutePath());
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

        intentShareFile.setType(URLConnection.guessContentTypeFromName(file.getName()));
        if (Build.VERSION.SDK_INT < 24) {
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
        } else {
            Uri apkURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
            //intentShareFile.setDataAndType(apkURI, "application/xls");
            intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentShareFile.putExtra(Intent.EXTRA_STREAM, apkURI);
        }
        //if you need
        intentShareFile.putExtra(Intent.EXTRA_SUBJECT, name + " Finance Details");
        intentShareFile.putExtra(Intent.EXTRA_TEXT, "World Finance Team");

        startActivity(Intent.createChooser(intentShareFile, "Share File"));
    }
}
