package com.farsitel.bazaar.game;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.utils.GHLogger;
import com.farsitel.bazaar.game.utils.GHStatus;

public abstract class AbstractGameHub {

    GHLogger logger;
    boolean isDispose = false;
    GHStatus connectionState = GHStatus.UNKNOWN;

    public AbstractGameHub(GHLogger logger) {
        this.logger = logger;
    }

    public GHLogger getLogger() {
        return logger;
    }

    abstract GHStatus connect(Context context, IConnectionCallback callback);

    }

    boolean connect(Context context, IConnectionCallback callback) {
        this.context = context;
        return true;
    }

    public abstract GHStatus isLogin(Context context);

    public abstract void startTournamentMatch(Context context, ITournamentMatchCallback callback, String matchId, String metaData);

    public abstract void endTournamentMatch(ITournamentMatchCallback callback, String sessionId, float coefficient);

    public abstract void showLastTournamentLeaderboard(Context context);

    boolean disposed() {
        return isDispose;
    }

    void dispose() {
        connectionState = GHStatus.DISCONNECTED;
        isDispose = true;
    }
}
