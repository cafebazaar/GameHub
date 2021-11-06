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
import com.farsitel.bazaar.game.callbacks.ITournamentsCallback;
import com.farsitel.bazaar.game.data.Tournament;
import com.farsitel.bazaar.game.utils.GHLogger;
import com.farsitel.bazaar.game.data.Result;
import com.farsitel.bazaar.game.data.Status;

import java.util.ArrayList;
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
    public Result isLogin(Context context, boolean showPrompts) {
        Result result = new Result(Status.SUCCESS, "");
        if(gameHubService == null){
            result.status = Status.DISCONNECTED;
            result.message = "Connect to service before!";
            return result;
        }
        try {
            if (gameHubService.isLogin()) {
                return result;
            }
            result.message = "Login to Cafebazaar before!";
        } catch (Exception e) {
            e.printStackTrace();
            result.message = e.getMessage();
            result.stackTrace = Arrays.toString(e.getStackTrace());
        }
        result.status = Status.LOGIN_CAFEBAZAAR;
        if (showPrompts) {
        startActionViewIntent(context, "bazaar://login", "com.farsitel.bazaar");
        }
        return result;
    }

    @Override
    public void connect(Context context, boolean showPrompts, IConnectionCallback callback) {
        connectionState = isCafebazaarInstalled(context, showPrompts);
        if (connectionState.status != Status.SUCCESS) {
                connectionState.call(callback);
                return;
            }
            logger.logDebug("GameHub service started.");
            gameHubConnection = new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    logger.logDebug("GameHub service disconnected.");
                    gameHubService = null;
                connectionState = new Result(Status.DISCONNECTED, "GameHub service disconnected.", "");
                    connectionState.call(callback);
                }

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (disposed()) return;
                    logger.logDebug("GameHub service connected.");
                    gameHubService = IGameHub.Stub.asInterface(service);
                connectionState.status = Status.SUCCESS;
                new Handler(Looper.getMainLooper()).post(() -> {
                    // Check login to cafebazaar
                    connectionState = isLogin(context, showPrompts);
                    if (connectionState.status == Status.SUCCESS) {
                    connectionState.message = "GameHub service connected.";
                    connectionState.call(callback);
                }
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


    public void getTournaments(Activity activity, ITournamentsCallback callback) {
        if(gameHubService == null){
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), "Connect to service before!", "", null);
            return;
    }

        Bundle resultBundle = null;
        try {
            resultBundle = gameHubService.getTournamentTimes(activity.getPackageName());
        } catch (RemoteException e) {
            callback.onFinish(Status.FAILURE.getLevelCode(), e.getMessage(), Arrays.toString(e.getStackTrace()), null);
            e.printStackTrace();
        }
//        for (String key : Objects.requireNonNull(resultBundle).keySet()) {
//            logger.logInfo("start  " + key + " : " + (resultBundle.get(key) != null ? resultBundle.get(key) : "NULL"));
//        }

        int statusCode = Objects.requireNonNull(resultBundle).getInt("statusCode");
        if (statusCode != Status.SUCCESS.getLevelCode()) {
            callback.onFinish(statusCode, "Error on endTournaments method", "", null);
            return;
        }

        long startAt = resultBundle.containsKey("startTimeStamp") ? resultBundle.getLong("startTimeStamp") : 0;
        long endAt = resultBundle.containsKey("endTimeStamp") ? resultBundle.getLong("endTimeStamp") : 0;
        List<Tournament> tournaments = new ArrayList<>();
        tournaments.add(new Tournament("-1", "Tournament -1", startAt, endAt));
        callback.onFinish(Status.SUCCESS.getLevelCode(), "Get Tournaments", "", tournaments);
    }

    public void startTournamentMatch(Activity activity, ITournamentMatchCallback
            callback, String matchId, String metaData) {
        logger.logDebug("startTournamentMatch");
        if (gameHubService == null) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), "Connect to service before!", "", null);
            return;
        }

        Bundle resultBundle = null;
        try {
            resultBundle = gameHubService.startTournamentMatch(activity.getPackageName(), matchId, metaData);
        } catch (RemoteException e) {
            callback.onFinish(Status.FAILURE.getLevelCode(), e.getMessage(), Arrays.toString(e.getStackTrace()), "");
            e.printStackTrace();
        }
//        for (String key : Objects.requireNonNull(resultBundle).keySet()) {
//            logger.logInfo("start  " + key + " : " + (resultBundle.get(key) != null ? resultBundle.get(key) : "NULL"));
//        }

        int statusCode = Objects.requireNonNull(resultBundle).getInt("statusCode");
        if (statusCode != Status.SUCCESS.getLevelCode()) {
            callback.onFinish(statusCode, "Error on startTournamentMatch", "", "");
            return;
        }
        String sessionId = resultBundle.containsKey("sessionId") ? resultBundle.getString("sessionId") : "sessionId";
        callback.onFinish(statusCode, sessionId, matchId, metaData);
    }

    public void endTournamentMatch(ITournamentMatchCallback callback, String sessionId,
                                   float score) {
        logger.logDebug("endTournamentMatch");
        Bundle resultBundle = null;
        try {
            resultBundle = gameHubService.endTournamentMatch(sessionId, score);
        } catch (RemoteException e) {
            callback.onFinish(Status.FAILURE.getLevelCode(), e.getMessage(), Arrays.toString(e.getStackTrace()), "");
            e.printStackTrace();
        }
//        for (String key : Objects.requireNonNull(resultBundle).keySet()) {
//            logger.logInfo("end  " + key + " : " + (resultBundle.get(key) != null ? resultBundle.get(key) : "NULL"));
//        }

        int statusCode = Objects.requireNonNull(resultBundle).getInt("statusCode");
        if (statusCode != Status.SUCCESS.getLevelCode()) {
            callback.onFinish(statusCode, "Error on endTournamentMatch", "", "");
            return;
        }
        String matchId = resultBundle.containsKey("matchId") ? resultBundle.getString("matchId") : "matchId";
        String metaData = resultBundle.containsKey("metadata") ? resultBundle.getString("metadata") : "metadata";
        callback.onFinish(statusCode, sessionId, matchId, metaData);
    }

    public void showTournamentLeaderboard(Context context, String tournamentId, IConnectionCallback callback) {
        logger.logDebug("showLastTournamentLeaderboard");
        // Check cafebazaar application version
        connectionState = isCafebazaarInstalled(context, true);
        if (connectionState.status != Status.SUCCESS) {
                connectionState.call(callback);
                return;
            }

        // Check login to cafebazaar
        new Handler(Looper.getMainLooper()).post(() -> {
            connectionState = isLogin(context, true);
            if (connectionState.status == Status.SUCCESS) {
                connectionState.message = "Last tournament leaderboard shown.";
                String data = "bazaar://tournament_leaderboard?package_name=" + context.getPackageName();
                logger.logInfo(data);
            startActionViewIntent(context, data, "com.farsitel.bazaar");
            }
            connectionState.call(callback);
        });
    }

    public void getTournamentLeaderboardData(Context context, String tournamentId, IConnectionCallback callback) {    }
}