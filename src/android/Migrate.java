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

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        if (action.equals("getOldLocalStorage")) {

            // only execute when we are a crosswalk app
            if (!args.getBoolean(0)) {
                callbackContext.error("Not a crosswalk app");
                return true;
            }

            String migrationFileName = "migration_complete";

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

            // Inject WebAppInterface to extract content of localstorage
            MigrateInterface migrateInterface = new MigrateInterface(callbackContext, migrationFile);
            webView.addJavascriptInterface(migrateInterface, "Migrate");

            // load migrate.html, it will export the local storage for the file:// context and call the java callback
            webView.loadUrl("file:///android_asset/migrate.html");

            return true;
        }

        return false;
    }
}

// Class to be injected into webview
class MigrateInterface {

    CallbackContext callbackContext;
    File migrationFile;

    MigrateInterface(CallbackContext newCallbackContext, File newMigrationFile) {
        callbackContext = newCallbackContext;
        migrationFile = newMigrationFile;
    }

    public void exportLocalStorage(String value) {
        try {
            migrationFile.createNewFile();
        } catch (IOException e) {
            //
        }

        callbackContext.success(value);
    }
}