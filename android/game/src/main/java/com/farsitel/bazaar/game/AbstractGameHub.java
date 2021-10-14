package com.farsitel.bazaar.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.utils.GHLogger;
import com.farsitel.bazaar.game.utils.GHStatus;

public abstract class AbstractGameHub {

    static final int MINIMUM_BAZAAR_VERSION = 1400700;

    GHLogger logger;
    boolean isDispose = false;
    GHStatus connectionState = GHStatus.UNKNOWN;

    public AbstractGameHub(GHLogger logger) {
        this.logger = logger;
    }

    public GHLogger getLogger() {
        return logger;
    }

    abstract void connect(Context context, IConnectionCallback callback);

    GHStatus isCafebazaarInstalled(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo("com.farsitel.bazaar", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (packageInfo == null) {
            startActionViewIntent(context, "https://cafebazaar.ir/install", null);
            return GHStatus.UPDATE_CAFEBAZAAR;
        }
        if (packageInfo.versionCode < MINIMUM_BAZAAR_VERSION) {
            startActionViewIntent(context, "bazaar://details?id=com.farsitel.bazaar", "com.farsitel.bazaar");
            return GHStatus.INSTALL_CAFEBAZAAR;
        }
        return GHStatus.SUCCESS;
    }

    void startActionViewIntent(Context context, String uri, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        context.startActivity(intent);
    }

    public abstract boolean isLogin(Context context, ITournamentMatchCallback callback);

    public abstract void startTournamentMatch(Activity activity, ITournamentMatchCallback callback, String matchId, String metaData);

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
