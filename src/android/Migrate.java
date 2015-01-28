package de.cameonet.cordova.migration;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Migrate extends CordovaPlugin {

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {

        Context context = cordova.getActivity().getApplicationContext();
        MigrateInterface migrateInterface = new MigrateInterface(callbackContext);

        // start regular webview
        WebView webView = new WebView(context);

        // enable javascript and localstorage
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setVisibility(View.GONE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e("CAMEO-CORDOVA", "Error in webview: " + errorCode + " : " + description);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e("CAMEO-CORDOVA", "Loaded page in webview: " + url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.e("CAMEO-CORDOVA", "Loading page in webview: " + url);
            }
        });


        // Inject WebAppInterface to extract content of localstorage
        webView.addJavascriptInterface(migrateInterface, "Migrate");

        // load migrate.html, it will export the local storage for the file:// context and call the java callback
        webView.loadUrl("file:///android_asset/migrate.html");

        return true;
    }
}

// Class to be injected into webview
class MigrateInterface {

    CallbackContext callbackContext;

    MigrateInterface(CallbackContext newCallbackContext) {
        callbackContext = newCallbackContext;
    }

    @JavascriptInterface
    public void exportLocalStorage(String value) {
        Log.d("cordova", "huup: " + value);
        callbackContext.success(value);
    }
}