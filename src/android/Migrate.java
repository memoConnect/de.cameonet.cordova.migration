package de.cameonet.cordova.migration;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.IOException;

public class Migrate extends CordovaPlugin {

    String migrationFileName = "migration_complete";

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        if (action.equals("getOldLocalStorage")) {

            // only execute when we are a crosswalk app
            if (!args.getBoolean(0)) {
                callbackContext.error("Not a crosswalk app");
                return true;
            }

            Context context = cordova.getActivity().getApplicationContext();

            // check migration has already be done
            File migrationFile = new File(context.getFilesDir(), migrationFileName);
            if (migrationFile.exists()) {
                callbackContext.error("Migration already complete");
                return true;
            }

            // start regular webview
            WebView webView = new WebView(context);

            // enable javascript and localstorage
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.setVisibility(View.GONE);

            // set database path for android with old webview
            if (android.os.Build.VERSION.SDK_INT < 19) {
                File databaseDir = context.getDir("database", Context.MODE_PRIVATE);
                webView.getSettings().setDatabasePath(databaseDir.getPath());
            }

            // Inject WebAppInterface to extract content of localstorage
            MigrateInterface migrateInterface = new MigrateInterface(callbackContext);
            webView.addJavascriptInterface(migrateInterface, "Migrate");

            // load migrate.html, it will export the local storage for the file:// context and call the java callback
            webView.loadUrl("file:///android_asset/migrate.html");

            /*try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                //
            }*/

            return true;
        } else if(action.equals("migrationComplete")) {
            Context context = cordova.getActivity().getApplicationContext();
            File migrationFile = new File(context.getFilesDir(), migrationFileName);

            if(!migrationFile.exists()) {
                try {
                    migrationFile.createNewFile();
                    callbackContext.success("true");
                } catch (IOException e) {
                    //
                }
            }
            return true;
        }

        return false;
    }
}

// Class to be injected into webview
class MigrateInterface {

    CallbackContext callbackContext;

    MigrateInterface(CallbackContext newCallbackContext) {
        callbackContext = newCallbackContext;
    }

    public void exportLocalStorage(String value) {
        callbackContext.success(value);
    }
}