package com.farsitel.bazaar.game.utils;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;

public class GHResult {
    public GHStatus status;
    public String message;
    public String stackTrace;

    public GHResult(GHStatus status, String message){
        this.status = status;
        this.message = message;
        this.stackTrace = "";
    }

    public GHResult(GHStatus status, String message, String stackTrace){
        this.status = status;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public void call(IConnectionCallback callback) {
        callback.onFinish(status.getLevelCode(), message, stackTrace);
    }
}
