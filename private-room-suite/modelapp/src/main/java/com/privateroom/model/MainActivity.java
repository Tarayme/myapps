package com.privateroom.model;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.webkit.*;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
  private static final int MEDIA_PERMS = 7;
  WebView web;
  boolean loaded = false;

  @Override public void onCreate(Bundle b){
    super.onCreate(b);

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
    });

    web.setWebChromeClient(new WebChromeClient(){
      @Override public void onPermissionRequest(final PermissionRequest r){
        runOnUiThread(() -> {
          if (hasMediaPermissions()) r.grant(r.getResources());
          else r.deny();
        });
      }
    });

    setContentView(web);

    if (hasMediaPermissions()) {
      loadApp();
    } else if (android.os.Build.VERSION.SDK_INT >= 23) {
      requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO}, MEDIA_PERMS);
    } else {
      loadApp();
    }
  }

  private boolean hasMediaPermissions(){
    if (android.os.Build.VERSION.SDK_INT < 23) return true;
    return checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
      && checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;
  }

  private void loadApp(){
    if (loaded) return;
    loaded=true;
    web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
  }

  @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
    super.onRequestPermissionsResult(requestCode,permissions,grantResults);
    if (requestCode==MEDIA_PERMS) loadApp();
  }

  @Override public void onBackPressed(){ if(web.canGoBack())web.goBack(); else super.onBackPressed(); }
}
