package com.farsitel.bazaar.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.utils.GHLogger;
import com.farsitel.bazaar.game.utils.GHResult;
import com.farsitel.bazaar.game.utils.GHStatus;

public abstract class AbstractGameHub {

    static final int MINIMUM_BAZAAR_VERSION = 1400700;

    GHLogger logger;
    boolean isDispose = false;
    GHResult connectionState;

    public AbstractGameHub(GHLogger logger) {
        this.logger = logger;
    }

    public GHLogger getLogger() {
        return logger;
    }


    GHResult isAvailable(Context context) {

        // Check cafebazaar application version
        connectionState = isCafebazaarInstalled(context);
        if (connectionState.status != GHStatus.SUCCESS) {
            return connectionState;
        }

        // Check login to cafebazaar
        return isLogin(context);
    }

    GHResult isCafebazaarInstalled(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo("com.farsitel.bazaar", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (packageInfo == null) {
            startActionViewIntent(context, "https://cafebazaar.ir/install", null);
            return new GHResult(GHStatus.INSTALL_CAFEBAZAAR, "Install cafebazaar to support GameHub!");
        }
        if (packageInfo.versionCode < MINIMUM_BAZAAR_VERSION) {
            startActionViewIntent(context, "bazaar://details?id=com.farsitel.bazaar", "com.farsitel.bazaar");
            return new GHResult(GHStatus.UPDATE_CAFEBAZAAR, "Install new version of cafebazaar to support GameHub!");
        }
        return new GHResult(GHStatus.SUCCESS, "");
    }

    abstract GHResult isLogin(Context context);

    void startActionViewIntent(Context context, String uri, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        context.startActivity(intent);
    }

    abstract void connect(Context context, IConnectionCallback callback);

    abstract void startTournamentMatch(Activity activity, ITournamentMatchCallback callback, String matchId, String metaData);

    abstract void endTournamentMatch(ITournamentMatchCallback callback, String sessionId, float coefficient);

    abstract void showLastTournamentLeaderboard(Context context, IConnectionCallback callback);

    boolean disposed() {
        return isDispose;
    }

    void dispose() {
        connectionState.status = GHStatus.DISCONNECTED;
        isDispose = true;
    }
}
