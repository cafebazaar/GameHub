package com.farsitel.bazaar.game;

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

                // Check login to cafebazaar
                new Handler(Looper.getMainLooper()).post(() -> {
                    Object[] results = isLogin(context);
                    connectionState = ((GHStatus) results[0]);
                    if (connectionState == GHStatus.SUCCESS) {
                        results[1] = "GameHub service connected.";
                    }
                    callback.onFinish(connectionState.getLevelCode(), (String) results[1], (String) results[2]);
                });
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
    public Object[] isLogin(Context context) {
        Bundle resultBundle = null;
        Bundle argsBundle = new Bundle();
        argsBundle.putString("methodName", "isLoginMethod");
        String message = "Login before start match";
        String stackTrace = "";
        try {
            resultBundle = gameHubService.callMethod(argsBundle);
            boolean isLogin = resultBundle.containsKey("isLogin") && resultBundle.getBoolean("isLogin");
            if (isLogin) {
                return new Object[]{
                        GHStatus.SUCCESS, "", ""
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            stackTrace = Arrays.toString(e.getStackTrace());
        }
        startActionViewIntent(context, "bazaar://login", "com.farsitel.bazaar");
        return new Object[]{
                GHStatus.LOGIN_CAFEBAZAAR, message, stackTrace
        };
    }

    public void startTournamentMatch(Activity activity, ITournamentMatchCallback callback, String matchId, String metaData) {
        Bundle resultBundle = null;
        Bundle argsBundle = new Bundle();
        argsBundle.putString("methodName", "startTournamentMatchMethod");
        argsBundle.putString("packageName", activity.getPackageName());
        argsBundle.putString("matchId", matchId);
        argsBundle.putString("metaData", metaData);
        logger.logDebug("startTournamentMatch");
        try {
            resultBundle = gameHubService.callMethod(argsBundle);
        } catch (RemoteException e) {
            callback.onFinish(GHStatus.FAILURE.getLevelCode(), e.getMessage(), Arrays.toString(e.getStackTrace()), "");
            e.printStackTrace();
        }
        //  for (String key : Objects.requireNonNull(resultBundle).keySet()) {
        //      logger.logInfo("start  " + key + " : " + (resultBundle.get(key) != null ? resultBundle.get(key) : "NULL"));
        //  }

        int statusCode = resultBundle.getInt("statusCode");
        if (statusCode != GHStatus.SUCCESS.getLevelCode()) {
            callback.onFinish(statusCode, "Error on startTournamentMatch", "", "");
            return;
        }
        String sessionId = resultBundle.containsKey("sessionId") ? resultBundle.getString("sessionId") : "sessionId";
        callback.onFinish(statusCode, sessionId, matchId, metaData);
    }

    public void endTournamentMatch(ITournamentMatchCallback callback, String sessionId, float score) {
        logger.logDebug("endTournamentMatch");
        Bundle resultBundle = null;
        Bundle argsBundle = new Bundle();
        argsBundle.putString("methodName", "endTournamentMatchMethod");
        argsBundle.putString("sessionId", sessionId);
        argsBundle.putFloat("score", score);

        try {
            resultBundle = gameHubService.callMethod(argsBundle);
        } catch (RemoteException e) {
            callback.onFinish(GHStatus.FAILURE.getLevelCode(), e.getMessage(), Arrays.toString(e.getStackTrace()), "");
            e.printStackTrace();
        }
        //  for (String key : Objects.requireNonNull(resultBundle).keySet()) {
        //      logger.logInfo("end  " + key + " : " + (resultBundle.get(key) != null ? resultBundle.get(key) : "NULL"));
        //  }

        int statusCode = resultBundle.getInt("statusCode");
        if (statusCode != GHStatus.SUCCESS.getLevelCode()) {
            callback.onFinish(statusCode, "Error on endTournamentMatch", "", "");
            return;
        }
        String matchId = resultBundle.containsKey("matchId") ? resultBundle.getString("matchId") : "matchId";
        String metaData = resultBundle.containsKey("metadata") ? resultBundle.getString("metadata") : "metadata";
        callback.onFinish(statusCode, sessionId, matchId, metaData);
    }

    public void showLastTournamentLeaderboard(Context context, IConnectionCallback callback) {
        logger.logDebug("showLastTournamentLeaderboard");
        GHStatus statusCode = isCafebazaarInstalled(context);
        if (statusCode != GHStatus.SUCCESS) {
            callback.onFinish(statusCode.getLevelCode(), "Install / Update new version of Cafebazaar.", "");
            return;
        }

        // Check login to cafebazaar
        new Handler(Looper.getMainLooper()).post(() -> {
            Object[] results = isLogin(context);
            connectionState = ((GHStatus) results[0]);
            if (connectionState == GHStatus.SUCCESS) {
                results[1] = "GameHub service connected.";
                String data = "bazaar://tournament_leaderboard?id=-1";
                startActionViewIntent(context, data, "com.farsitel.bazaar");
            }
            callback.onFinish(connectionState.getLevelCode(), (String) results[1], (String) results[2]);
        });
    }
}