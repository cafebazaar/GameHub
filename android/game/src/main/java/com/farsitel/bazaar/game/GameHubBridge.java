package com.farsitel.bazaar.game;

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
import com.farsitel.bazaar.game.callbacks.IRankingCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentsCallback;
import com.farsitel.bazaar.game.data.RankItem;
import com.farsitel.bazaar.game.data.Tournament;
import com.farsitel.bazaar.game.utils.GHLogger;
import com.farsitel.bazaar.game.data.Result;
import com.farsitel.bazaar.game.data.Status;
import com.farsitel.bazaar.game.utils.MainThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public String getVersion() {
        return BuildConfig.GAMEHUB_VERSION;
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
                MainThread.run(() -> {
                    // Check login to cafebazaar
                    isLogin(context, showPrompts, result -> {
                        connectionState = result;
                    if (connectionState.status == Status.SUCCESS) {
                        connectionState.message = "GameHub service connected.";
                        connectionState.call(callback);
                    }
                });
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
    public void isLogin(Context context, boolean showPrompts, IBroadcastCallback callback) {
        Result result = new Result(Status.SUCCESS, "");
        if (gameHubService == null && gameHubBroadcast == null) {
            result.status = Status.DISCONNECTED;
            result.message = "Connect to service before!";
            callback.call(result);
            return;
        }
        try {
            if (gameHubService.isLogin()) {
                callback.call(result);
                return;
            }
            result.message = "Login to Cafebazaar before!";
        } catch (Exception e) {
            e.printStackTrace();
            result.status = Status.FAILURE;
            result.message = e.getMessage();
            result.stackTrace = Arrays.toString(e.getStackTrace());
        }
        result.status = Status.LOGIN_CAFEBAZAAR;
        if (showPrompts) {
            startActionViewIntent(context, "bazaar://login", "com.farsitel.bazaar");
        }
        callback.call(result);
    }

    public void getTournaments(Activity activity, ITournamentsCallback callback) {
        logger.logDebug("getTournaments");
        if (gameHubService == null && gameHubBroadcast == null) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), "Connect to service before!", "", null);
            return;
        }

        Result result = new Result();
        try {
            Bundle resultBundle = gameHubService.getTournamentTimes(activity.getPackageName());
            result = Result.fromBundle(resultBundle);
        } catch (RemoteException e) {
            e.printStackTrace();
            result.status = Status.FAILURE;
            result.message = e.getMessage();
            result.stackTrace = Arrays.toString(e.getStackTrace());
        }
        tournamentsCallback(result, callback);
    }

    void tournamentsCallback(Result result, ITournamentsCallback callback) {
        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }

        long startAt = result.extras.containsKey("startTimestamp") ? result.extras.getLong("startTimestamp") : 0;
        long endAt = result.extras.containsKey("endTimestamp") ? result.extras.getLong("endTimestamp") : 0;
        List<Tournament> tournaments = new ArrayList<>();
        tournaments.add(new Tournament("-1", "Tournament -1", startAt, endAt));
        callback.onFinish(Status.SUCCESS.getLevelCode(), "Get Tournaments", "", tournaments);
    }

    public void startTournamentMatch(Activity activity, ITournamentMatchCallback
            callback, String matchId, String metadata) {
        logger.logDebug("startTournamentMatch");
        if (gameHubService == null && gameHubBroadcast == null) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), "Connect to service before!", "", null);
            return;
        }

        Result result = new Result();
        try {
            Bundle bundle = gameHubService.startTournamentMatch(activity.getPackageName(), matchId, metadata);
            result = Result.fromBundle(bundle);
        } catch (RemoteException e) {
            e.printStackTrace();
            result.status = Status.FAILURE;
            result.message = e.getMessage();
            result.stackTrace = Arrays.toString(e.getStackTrace());
        }
        startTournamentMatchCallback(result, callback, matchId, metadata);
    }

    void startTournamentMatchCallback(Result result, ITournamentMatchCallback callback, String matchId, String metadata) {
        for (String key : Objects.requireNonNull(result.extras).keySet()) {
            logger.logDebug("start  " + key + " : " + (result.extras.get(key) != null ? result.extras.get(key) : "NULL"));
        }

        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }
        String sessionId = result.extras.containsKey("sessionId") ? result.extras.getString("sessionId") : "sessionId";
        callback.onFinish(result.status.getLevelCode(), sessionId, matchId, metadata);
    }

    public void endTournamentMatch(ITournamentMatchCallback callback, String sessionId,
                                   float score) {
        logger.logDebug("endTournamentMatch");
        if (gameHubService == null && gameHubBroadcast == null) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), "Connect to service before!", "", null);
            return;
        }
        Result result = new Result();
        try {
            Bundle bundle = gameHubService.endTournamentMatch(sessionId, score);
            result = Result.fromBundle(bundle);
        } catch (RemoteException e) {
            e.printStackTrace();
            result.status = Status.FAILURE;
            result.message = e.getMessage();
            result.stackTrace = Arrays.toString(e.getStackTrace());
        }
        endTournamentMatchCallback(result, callback, sessionId);
    }

    void endTournamentMatchCallback(Result result, ITournamentMatchCallback callback, String sessionId) {
        for (String key : Objects.requireNonNull(result.extras).keySet()) {
            logger.logDebug("end  " + key + " : " + (result.extras.get(key) != null ? result.extras.get(key) : "NULL"));
        }

        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }
        String matchId = result.extras.containsKey("matchId") ? result.extras.getString("matchId") : "matchId";
        String metaData = result.extras.containsKey("metadata") ? result.extras.getString("metadata") : "metadata";
        callback.onFinish(result.status.getLevelCode(), sessionId, matchId, metaData);
    }

    public void showTournamentRanking(Context context, String tournamentId, IConnectionCallback callback) {
        logger.logDebug("showTournamentRanking");

        // Check cafebazaar application version
        connectionState = isCafebazaarInstalled(context, true);
        if (connectionState.status != Status.SUCCESS) {
            connectionState.call(callback);
            return;
        }

        // Check login to cafebazaar
        isLogin(context, true, loginResult -> {
            if (loginResult.status != Status.SUCCESS) {
                callback.onFinish(loginResult.status.getLevelCode(), loginResult.message, loginResult.stackTrace);
                return;
            }

                connectionState.message = "Last tournament ranking table shown.";
                String data = "bazaar://tournament_leaderboard?package_name=" + context.getPackageName();
                logger.logInfo(data);
                try {
                    startActionViewIntent(context, data, "com.farsitel.bazaar");
                } catch (Exception e) {
                    callback.onFinish(Status.UPDATE_CAFEBAZAAR.getLevelCode(), "Get Ranking-data needs to new version of CafeBazaar!", Arrays.toString(e.getStackTrace()));
                    return;
            }
            connectionState.call(callback);
        });
    }

    @Override
    public void getTournamentRanking(Context context, String tournamentId, IRankingCallback
            callback) {
        logger.logDebug("getTournamentRanking");
        if (gameHubService == null && gameHubBroadcast == null) {
            callback.onFinish(Status.DISCONNECTED.getLevelCode(), "Connect to service before!", "", null);
            return;
        }

        Result result = new Result();
        try {
            Bundle bundle = gameHubService.getCurrentLeaderboard(context.getPackageName());
            result = Result.fromBundle(bundle);
        } catch (Exception e) {
            e.printStackTrace();
            result.status = Status.FAILURE;
            result.message = e.getMessage();
            result.stackTrace = Arrays.toString(e.getStackTrace());
        }
        getTournamentRankingCallback(result, callback);
        }

    void getTournamentRankingCallback(Result result, IRankingCallback callback) {

        if (result.extras == null) {
            callback.onFinish(Status.UPDATE_CAFEBAZAAR.getLevelCode(), "Get Ranking-data needs to new version of CafeBazaar!", "", null);
            return;
        }
        for (String key : result.extras.keySet()) {
            logger.logDebug("Ranking =>  " + key + " : " + (result.extras.get(key) != null ? result.extras.get(key) : "NULL"));
        }

        if (result.status != Status.SUCCESS) {
            callback.onFinish(result.status.getLevelCode(), result.message, result.stackTrace, null);
            return;
        }

        List<RankItem> rankItems = new ArrayList<>();
        JSONArray participants = null;
        String jsonString = result.extras.getString("leaderboardData");
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            participants = jsonObject.optJSONArray("participants");
            int participantsCount = participants != null ? participants.length() : 0;
            for (int i = 0; i < participantsCount; i++) {
                JSONObject obj = participants.getJSONObject(i);
                rankItems.add(new RankItem(obj.getString("nickname"),
                        obj.getString("score"),
                        obj.getString("award"),
                        obj.getBoolean("hasFollowingEllipsis"),
                        obj.getBoolean("isCurrentUser"),
                        obj.getBoolean("isWinner")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onFinish(Status.FAILURE.getLevelCode(), "Error on Ranking data parsing!", "", null);
            return;
        }

        callback.onFinish(result.status.getLevelCode(), "getTournamentRanking", jsonString, rankItems);
    }
}