package com.farsitel.bazaar.game.data;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;

public class Result {
    public Status status;
    public String message;
    public String stackTrace;

    public Result(Status status, String message){
        this.status = status;
        this.message = message;
        this.stackTrace = "";
    }

    public Result(Status status, String message, String stackTrace){
        this.status = status;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public void call(IConnectionCallback callback) {
        callback.onFinish(status.getLevelCode(), message, stackTrace);
    }
}
