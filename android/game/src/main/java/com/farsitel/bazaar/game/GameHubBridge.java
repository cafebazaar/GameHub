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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.utils.GHLogger;
import com.farsitel.bazaar.game.utils.GHStatus;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GameHubBridge extends AbstractGameHub {
    private static GameHubBridge instance;

    private ServiceConnection gameHubConnection;
    private IGameHub gameHubService;

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

        logger.logDebug("GameHub service started.");
        gameHubConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                logger.logDebug("GameHub service disconnected.");
                gameHubService = null;
                connectionState = GHStatus.SUCCESS;
                callback.onFinish(connectionState.getLevelCode(), "GameHub service disconnected.", "");
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (disposed()) return;
                logger.logDebug("GameHub service connected.");
                gameHubService = IGameHub.Stub.asInterface(service);
                connectionState = GHStatus.SUCCESS;
                callback.onFinish(connectionState.getLevelCode(), "GameHub service connected.", "");
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
    public void isLogin(Context context, ITournamentMatchCallback callback) {
        try {
            if (gameHubService.isLogin()) {
                return true;
            }
            callback.onFinish(GHStatus.LOGIN_CAFEBAZAAR.getLevelCode(), "Login before start match", "");
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFinish(GHStatus.LOGIN_CAFEBAZAAR.getLevelCode(), e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
        startActionViewIntent(context, "bazaar://login", "com.farsitel.bazaar");
    }

    public void startTournamentMatch(Activity activity, ITournamentMatchCallback callback, String matchId, String metaData) {
        Bundle bundle = null;
        logger.logDebug("startTournamentMatch");
        try {
            bundle = gameHubService.startTournamentMatch(activity.getPackageName(), matchId, metaData);
        } catch (RemoteException e) {
            callback.onFinish(GHStatus.FAILURE.getLevelCode(), e.getMessage(), Arrays.toString(e.getStackTrace()), "");
            e.printStackTrace();
        }
        // for (String key : bundle.keySet()) {
        //     logger.logInfo("start  " + key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
        // }

        int statusCode = bundle.getInt("statusCode");
        if (statusCode != GHStatus.SUCCESS.getLevelCode()) {
            callback.onFinish(statusCode, "Error on startTournamentMatch", "", "");
            return;
        }
        String sessionId = bundle.containsKey("sessionId") ? bundle.getString("sessionId") : "sessionId";
        callback.onFinish(statusCode, sessionId, matchId, metaData);
    }

    public void endTournamentMatch(ITournamentMatchCallback callback, String sessionId, float coefficient) {
        logger.logDebug("endTournamentMatch");
        Bundle bundle = null;
        try {
            bundle = gameHubService.endTournamentMatch(sessionId, coefficient);
        } catch (RemoteException e) {
            callback.onFinish(GHStatus.FAILURE.getLevelCode(), e.getMessage(), Arrays.toString(e.getStackTrace()), "");
            e.printStackTrace();
        }
        // for (String key : bundle.keySet()) {
        //     logger.logInfo("end  " + key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
        // }

        int statusCode = bundle.getInt("statusCode");
        if (statusCode != GHStatus.SUCCESS.getLevelCode()) {
            callback.onFinish(statusCode, "Error on endTournamentMatch", "", "");
            return;
        }
        String matchId = bundle.containsKey("matchId") ? bundle.getString("matchId") : "matchId";
        String metaData = bundle.containsKey("metaData") ? bundle.getString("metaData") : "metaData";
        callback.onFinish(statusCode, sessionId, matchId, metaData);
    }

    public void showLastTournamentLeaderboard(Context context) {
        logger.logDebug("showLastTournamentLeaderboard");
        String data = "bazaar://tournament_leaderboard?id=-1";
        startActionViewIntent(context, data, "com.farsitel.bazaar");
    }
}