package com.farsitel.bazaar.game.data;

import android.os.Bundle;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.IRankingCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentsCallback;

public class Result {
    public Status status;
    public String message = "";
    public String stackTrace = "";
    public Bundle extras;

    public Result() {
    }

    public Result(Status status) {
        this.status = status;
    }

    public Result(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Result(Status status, String message, String stackTrace) {
        this.status = status;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public Result setBundle(Bundle extras) {
        if (extras.containsKey("statusCode")) {
            status = Status.fromLevelCode(extras.getInt("statusCode"));
            extras.remove("statusCode");
        }
        this.extras = extras;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder log = new StringBuilder("[Result => status:" + status);
        if (message != null) {
            log.append(", message: ").append(message);
        }
        if (!stackTrace.equals("")) {
            log.append(", tackTrace: ").append(stackTrace);
        }
        if (extras != null) {
            for (String key : extras.keySet()) {
                log.append(", ").append(key).append(": ").append(extras.get(key));
            }
        }
        return log.append("]").toString();
    }
}
