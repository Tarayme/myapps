package com.privateroom.model;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
  WebView web;
  @Override public void onCreate(Bundle b){
    super.onCreate(b);
    if(android.os.Build.VERSION.SDK_INT>=23) requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},7);
    web=new WebView(this);
    WebSettings s=web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setMediaPlaybackRequiresUserGesture(false);
    final WebViewAssetLoader loader=new WebViewAssetLoader.Builder()
      .addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this))
      .build();
    web.setWebViewClient(new WebViewClient(){
      @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){
        return loader.shouldInterceptRequest(r.getUrl());
      }
      @Override public void onPageFinished(WebView v,String url){
        super.onPageFinished(v,url);
        v.evaluateJavascript(
          "(function(){setInterval(function(){var b=document.getElementById('onlineBtn');var s=document.getElementById('status');if(b&&s){s.textContent=(b.textContent.indexOf('Offline Ol')>=0)?'● Online':'Offline';}},400);})();",
          null
        );
      }
    });
    web.setWebChromeClient(new WebChromeClient(){
      @Override public void onPermissionRequest(final PermissionRequest r){
        runOnUiThread(()->r.grant(r.getResources()));
      }
    });
    setContentView(web);
    web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
  }
  @Override public void onBackPressed(){ if(web.canGoBack())web.goBack(); else super.onBackPressed(); }
}
