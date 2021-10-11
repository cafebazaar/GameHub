package com.farsitel.bazaar.game.utils;

import android.util.Log;

public class GHLogger {

    boolean mDebugLog = true;
    String mDebugTag = "GameHub";

    public void logInfo(String msg) {
        if (mDebugLog) Log.i(mDebugTag, msg);
    }

    public void logDebug(String msg) {
        if (mDebugLog) Log.d(mDebugTag, msg);
    }

    public void logError(String msg) {
        Log.e(mDebugTag, "In-app billing error: " + msg);
    }

    public void logWarn(String msg) {
        Log.w(mDebugTag, "In-app billing warning: " + msg);
    }
}
