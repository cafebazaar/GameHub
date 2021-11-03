package com.farsitel.bazaar.game.utils;

import android.util.Log;

public class GHLogger {

    public static String TAG = "GameHub";
    public static boolean debugMode = true;

    public void logInfo(String msg) {
        if (debugMode) Log.i(TAG, msg);
    }

    public void logDebug(String msg) {
        if (debugMode) Log.d(TAG, msg);
    }

    public void logError(String msg) {
        Log.e(TAG, "In-app billing error: " + msg);
    }

    public void logWarn(String msg) {
        Log.w(TAG, "In-app billing warning: " + msg);
    }
}
