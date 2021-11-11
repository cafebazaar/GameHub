package com.farsitel.bazaar.game.utils;

import android.os.Handler;
import android.os.Looper;

public class MainThread {
    static public void run(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
