package com.farsitel.bazaar.game;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.utils.GHLogger;

public abstract class AbstractGameHub {

    GHLogger logger;
    Context context;
    boolean isSetupFinish = false;

    boolean isDispose = false;

    public AbstractGameHub(GHLogger logger) {
        this.logger = logger;
    }

    protected boolean disposed() {
        return isDispose;
    }

    public GHLogger getLogger() {
        return logger;
    }

    void dispose() {
        isSetupFinish = false;
        isDispose = true;
    }

    boolean connect(Context context, IConnectionCallback callback) {
        this.context = context;
        return true;
    }

    public abstract boolean isLogin();

    public abstract void startTournamentMatch(ITournamentMatchCallback callback, String matchId, String metaData);

    public abstract void endTournamentMatch(ITournamentMatchCallback callback, String sessionId, float coefficient);

    public abstract void showLastTournamentLeaderboard();
}
