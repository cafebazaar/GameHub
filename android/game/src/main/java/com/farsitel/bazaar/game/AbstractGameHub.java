package com.farsitel.bazaar.game;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
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

    void startActionViewIntent(Context context, String uri, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        context.startActivity(intent);
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
