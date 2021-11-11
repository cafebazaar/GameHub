package com.farsitel.bazaar.game.data;

import android.os.Bundle;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;

public class Result {
    public Status status;
    public String message;
    public String stackTrace;
    public Bundle extras;

    public Result() {
    }

    public Result(Status status, String message) {
        this.status = status;
        this.message = message;
        this.stackTrace = "";
    }

    public Result(Status status, String message, String stackTrace) {
        this.status = status;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public static Result fromBundle(Bundle extras) {
        Result result = new Result();
        if (extras.containsKey("statusCode")) {
            result.status = Status.fromLevelCode(extras.getInt("statusCode"));
        }
        result.extras = extras;
        return result;
    }

    public void call(IConnectionCallback callback) {
        callback.onFinish(status.getLevelCode(), message, stackTrace);
    }
}
