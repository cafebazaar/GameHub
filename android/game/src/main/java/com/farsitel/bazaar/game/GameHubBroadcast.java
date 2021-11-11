package com.farsitel.bazaar.game;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.farsitel.bazaar.game.callbacks.IBroadcastCallback;
import com.farsitel.bazaar.game.data.Result;
import com.farsitel.bazaar.game.data.Status;
import com.farsitel.bazaar.game.utils.GHLogger;

import java.util.Map;
import java.util.WeakHashMap;

public class GameHubBroadcast {
    public static final String IS_LOGIN = "com.farsitel.bazaar.isLogin";
    public static final String GET_TOURNAMENTS = "com.farsitel.bazaar.getTournamentTimes";
    public static final String START_TOURNAMENT_MATCH = "com.farsitel.bazaar.startTournamentMatch";
    public static final String END_TOURNAMENT_MATCH = "com.farsitel.bazaar.endTournamentMatch";
    public static final String GET_CURRENT_LEADERBOARD_DATA = "com.farsitel.bazaar.getCurrentLeaderboardData";

    private final GHLogger logger;
    private final Context context;
    private Map<String, IBroadcastCallback> callbacks = new WeakHashMap<>();

    private boolean mDisposed;
    private BroadcastReceiver receiver;

    GameHubBroadcast(Context context, GHLogger logger) {
        this.context = context;
        this.logger = logger;
        createReceiver();
        registerBroadcast(context);
    }

    private void createReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                logger.logDebug(intent.toUri(0));
                String action = intent.getAction();
                if (action == null) {
                    logger.logError("action is null.");
                    return;
                }

                if (disposed()) {
                    logger.logError("Broadcast already disposed.");
                    return;
                }

                if (callbacks.containsKey(action)) {
                    Result result = new Result();
                    if (action.equals(IS_LOGIN)) {
                        boolean isLogin = intent.getBooleanExtra("isLogin", false);
                        result.status = isLogin ? Status.SUCCESS : Status.LOGIN_CAFEBAZAAR;
                        result.message = isLogin ? "" : "Login to Cafebazaar before!";
                        callbacks.get(action).call(result);
                    } else {
                        callbacks.get(action).call(result.setBundle(intent.getExtras()));
                    }
                    callbacks.remove(action);
                }
            }
        };
    }

    private void registerBroadcast(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IS_LOGIN);
        intentFilter.addAction(GET_TOURNAMENTS);
        intentFilter.addAction(START_TOURNAMENT_MATCH);
        intentFilter.addAction(END_TOURNAMENT_MATCH);
        intentFilter.addAction(GET_CURRENT_LEADERBOARD_DATA);
        context.registerReceiver(receiver, intentFilter);
    }

    private void sendBroadcast(String action, Bundle extras) {
        extras.putString("packageName", context.getPackageName());

        Intent broadcastIntent = new Intent();
        broadcastIntent.setPackage("com.farsitel.bazaar");
        broadcastIntent.putExtras(extras);
        broadcastIntent.setAction(action);
        context.sendBroadcast(broadcastIntent);
    }

    public void isLogin(IBroadcastCallback callback) {
        callbacks.put(IS_LOGIN, callback);
        sendBroadcast(IS_LOGIN, new Bundle());
    }

    public void getTournamentTimes(IBroadcastCallback callback) {
        callbacks.put(GET_TOURNAMENTS, callback);
        sendBroadcast(GET_TOURNAMENTS, new Bundle());
    }

    public void startTournamentMatch(String matchId, String metadata, IBroadcastCallback callback) {
        Bundle extras = new Bundle();
        extras.putString("matchId", matchId);
        extras.putString("metaData", metadata);
        callbacks.put(START_TOURNAMENT_MATCH, callback);
        sendBroadcast(START_TOURNAMENT_MATCH, extras);
    }

    public void endTournamentMatch(String sessionId, float score, IBroadcastCallback callback) {
        Bundle extras = new Bundle();
        extras.putString("sessionId", sessionId);
        extras.putFloat("score", score);
        callbacks.put(END_TOURNAMENT_MATCH, callback);
        sendBroadcast(END_TOURNAMENT_MATCH, extras);
    }

    public void getCurrentLeaderboard(IBroadcastCallback callback) {
        callbacks.put(GET_CURRENT_LEADERBOARD_DATA, callback);
        sendBroadcast(GET_CURRENT_LEADERBOARD_DATA, new Bundle());
    }

    protected boolean disposed() {
        return mDisposed;
    }

    void dispose(Context context) {
        callbacks.clear();
        mDisposed = true;
    }
}
