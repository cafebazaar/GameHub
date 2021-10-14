package com.farsitel.bazaar.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.utils.GHLogger;
import com.farsitel.bazaar.game.utils.GHStatus;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class GameHubBridge extends AbstractGameHub {
    private static GameHubBridge instance;

    private ServiceConnection gameHubConnection;
    private GameHub gameHubService;

    public GameHubBridge() {
        super(new GHLogger());
    }

    public static GameHubBridge getInstance() {
        if (instance == null) {
            instance = new GameHubBridge();
        }
        return instance;
    }


    @Override
    public void connect(Context context, IConnectionCallback callback) {
        // Check cafebazaar application version
        connectionState = isCafebazaarInstalled(context);
        if (connectionState != GHStatus.SUCCESS) {
            callback.onFinish(connectionState.getLevelCode(), "Install / Update new version of Cafebazaar.", "");
            return;
        }

        gameHubConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                logger.logDebug("GameHub service disconnected.");
                callback.onFinish(GHStatus.DISCONNECTED.getLevelCode(), "GameHub service disconnected.", "");
                gameHubService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (disposed()) return;
                gameHubService = GameHub.Stub.asInterface(service);
                connectionState = GHStatus.SUCCESS;
            }
        };

        // Bind to bazaar game hub
        Intent serviceIntent = new Intent("com.farsitel.bazaar.Game.BIND");
        serviceIntent.setPackage("com.farsitel.bazaar");

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> intentServices = pm.queryIntentServices(serviceIntent, 0);
        if (!intentServices.isEmpty()) {
            // service available to handle that Intent
            context.bindService(serviceIntent, gameHubConnection, Context.BIND_AUTO_CREATE);
        }
        }

    @Override
    public GHStatus isLogin(Context context) {
        GHStatus status = GHStatus.SUCCESS;
        try {
            if (!gameHubService.isLogin()) {
                status = GHStatus.LOGIN_CAFEBAZAAR;
                startActionViewIntent(context, "bazaar://login", "com.farsitel.bazaar");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public void startTournamentMatch(Context context, ITournamentMatchCallback callback, String matchId, String metaData) {
        // Check login to cafebazaar
        GHStatus status = isLogin(context);

        if (status == GHStatus.SUCCESS) {
            logger.logDebug("Billing service connected.");
            callback.onFinish(status.getLevelCode(), "", "");
        } else {
            callback.onFinish(status.getLevelCode(), "Login to Cafebazaar app", "");
    }

        Bundle bundle = null;
        try {
            bundle = gameHubService.startTournamentMatch(context.getPackageName(), matchId, metaData);
        } catch (RemoteException e) {
            callback.onFinish(GHStatus.FAILURE.getLevelCode(), "Error on startTournamentMatch", e.getMessage());
            e.printStackTrace();
        }

        callback.onFinish(GHStatus.SUCCESS.getLevelCode(), "no data", "");
    }

    public void endTournamentMatch(ITournamentMatchCallback callback, String sessionId, float coefficient) {
        Bundle bundle = null;
        try {
            bundle = gameHubService.endTournamentMatch(sessionId, coefficient);
        } catch (RemoteException e) {
            callback.onFinish(GHStatus.FAILURE.getLevelCode(), "Error on endTournamentMatch", e.getMessage());
            e.printStackTrace();
        }

        callback.onFinish(GHStatus.SUCCESS.getLevelCode(), "no data", "");
    }

    public void showLastTournamentLeaderboard() {
        Log.i(GameServiceBridge.TAG, "endTournamentMatch");
    }
}