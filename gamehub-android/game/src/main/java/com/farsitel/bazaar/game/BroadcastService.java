package com.farsitel.bazaar.game;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.farsitel.bazaar.game.callbacks.IBroadcastCallback;
import com.farsitel.bazaar.game.constants.Constant;
import com.farsitel.bazaar.game.constants.Method;
import com.farsitel.bazaar.game.constants.Param;
import com.farsitel.bazaar.game.data.Result;
import com.farsitel.bazaar.game.data.Status;
import com.farsitel.bazaar.game.utils.Logger;

import java.util.Map;
import java.util.WeakHashMap;

public class BroadcastService {

    private final Logger logger;
    private final Context context;
    private final Map<String, IBroadcastCallback> callbacks = new WeakHashMap<>();

    private static String getAction(String methodName) {
        return Constant.BAZAAR_PACKAGE_NAME + "." + methodName;
    }

    private boolean mDisposed;
    private BroadcastReceiver receiver;

    BroadcastService(Context context, Logger logger) {
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
                    if (action.equals(getAction(Method.IS_LOGIN))) {
                        boolean isLogin = intent.getBooleanExtra(Method.IS_LOGIN, false);
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
        intentFilter.addAction(getAction(Method.IS_LOGIN));
        intentFilter.addAction(getAction(Method.GET_TOURNAMENTS));
        intentFilter.addAction(getAction(Method.START_TOURNAMENT_MATCH));
        intentFilter.addAction(getAction(Method.END_TOURNAMENT_MATCH));
        intentFilter.addAction(getAction(Method.GET_CURRENT_LEADERBOARD_DATA));
        intentFilter.addAction(getAction(Method.EVENT_DONE_NOTIFY));
        context.registerReceiver(receiver, intentFilter);
    }

    private void sendBroadcast(String action) {
        sendBroadcast(action, new Bundle());
    }

    private void sendBroadcast(String action, Bundle extras) {
        extras.putString(Param.PACKAGE_NAME, context.getPackageName());

        Intent broadcastIntent = new Intent();
        broadcastIntent.setPackage(Constant.BAZAAR_PACKAGE_NAME);
        broadcastIntent.putExtras(extras);
        broadcastIntent.setAction(action);
        context.sendBroadcast(broadcastIntent);
    }

    public void isLogin(IBroadcastCallback callback) {
        callbacks.put(getAction(Method.IS_LOGIN), callback);
        sendBroadcast(getAction(Method.IS_LOGIN));
    }

    public void getTournamentTimes(IBroadcastCallback callback) {
        callbacks.put(getAction(Method.GET_TOURNAMENTS), callback);
        sendBroadcast(getAction(Method.GET_TOURNAMENTS));
    }

    public void startTournamentMatch(String matchId, String metadata, IBroadcastCallback callback) {
        Bundle extras = new Bundle();
        extras.putString(Param.MATCH_ID, matchId);
        extras.putString(Param.META_DATA, metadata);
        callbacks.put(getAction(Method.START_TOURNAMENT_MATCH), callback);
        sendBroadcast(getAction(Method.START_TOURNAMENT_MATCH), extras);
    }

    public void endTournamentMatch(String sessionId, float score, IBroadcastCallback callback) {
        Bundle extras = new Bundle();
        extras.putString(Param.SESSION_ID, sessionId);
        extras.putFloat(Param.SCORE, score);
        callbacks.put(getAction(Method.END_TOURNAMENT_MATCH), callback);
        sendBroadcast(getAction(Method.END_TOURNAMENT_MATCH), extras);
    }

    public void getCurrentLeaderboard(IBroadcastCallback callback) {
        callbacks.put(getAction(Method.GET_CURRENT_LEADERBOARD_DATA), callback);
        sendBroadcast(getAction(Method.GET_CURRENT_LEADERBOARD_DATA));
    }

    public void eventDoneNotify(String eventId, IBroadcastCallback callback) {
        Bundle extras = new Bundle();
        extras.putString(Param.EVENT_ID, eventId);
        callbacks.put(getAction(Method.EVENT_DONE_NOTIFY), callback);
        sendBroadcast(getAction(Method.EVENT_DONE_NOTIFY), extras);
    }

    protected boolean disposed() {
        return mDisposed;
    }

    void dispose() {
        callbacks.clear();
        mDisposed = true;
    }
}
