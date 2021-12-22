package com.farsitel.bazaar.game.utils;

import android.util.Log;

public class Logger {

    public static String TAG = "GameHub";

    public void logInfo(String msg) {
        Log.i(TAG, msg);
    }

    public void logDebug(String msg) {
        Log.d(TAG, msg);
    }

    public void logError(String msg) {
        Log.e(TAG, "In-app billing error: " + msg);
    }

    public void logWarn(String msg) {
        Log.w(TAG, "In-app billing warning: " + msg);
    }
}
