package com.psr.financial;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AppWebViewClients extends WebViewClient {


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO Auto-generated method stub
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        // TODO Auto-generated method stub
        super.onPageFinished(view, url);

    }
}
