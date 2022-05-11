package com.farsitel.bazaar.game;

import static com.farsitel.bazaar.game.receiver.GameHubBroadcastReceiver.getAction;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.farsitel.bazaar.game.callbacks.IBroadcastCallback;
import com.farsitel.bazaar.game.constants.Constant;
import com.farsitel.bazaar.game.constants.Method;
import com.farsitel.bazaar.game.constants.Param;
import com.farsitel.bazaar.game.receiver.GameHubBroadcastReceiver;

public class BroadcastService {

    private final Context context;
    private final GameHubBroadcastReceiver receiver = new GameHubBroadcastReceiver();

    BroadcastService(Context context) {
        this.context = context;
        registerBroadcast(context);
    }

    private void registerBroadcast(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getAction(Method.IS_LOGIN));
        intentFilter.addAction(getAction(Method.GET_TOURNAMENTS));
        intentFilter.addAction(getAction(Method.START_TOURNAMENT_MATCH));
        intentFilter.addAction(getAction(Method.END_TOURNAMENT_MATCH));
        intentFilter.addAction(getAction(Method.GET_CURRENT_LEADERBOARD_DATA));
        intentFilter.addAction(getAction(Method.EVENT_DONE_NOTIFY));
        intentFilter.addAction(getAction(Method.GET_EVENTS_BY_PACKAGE_NAME));
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
        receiver.callbacks.put(getAction(Method.IS_LOGIN), callback);
        sendBroadcast(getAction(Method.IS_LOGIN));
    }

    public void getTournamentTimes(IBroadcastCallback callback) {
        receiver.callbacks.put(getAction(Method.GET_TOURNAMENTS), callback);
        sendBroadcast(getAction(Method.GET_TOURNAMENTS));
    }

    public void startTournamentMatch(String matchId, String metadata, IBroadcastCallback callback) {
        Bundle extras = new Bundle();
        extras.putString(Param.MATCH_ID, matchId);
        extras.putString(Param.META_DATA, metadata);
        receiver.callbacks.put(getAction(Method.START_TOURNAMENT_MATCH), callback);
        sendBroadcast(getAction(Method.START_TOURNAMENT_MATCH), extras);
    }

    public void endTournamentMatch(String sessionId, float score, IBroadcastCallback callback) {
        Bundle extras = new Bundle();
        extras.putString(Param.SESSION_ID, sessionId);
        extras.putFloat(Param.SCORE, score);
        receiver.callbacks.put(getAction(Method.END_TOURNAMENT_MATCH), callback);
        sendBroadcast(getAction(Method.END_TOURNAMENT_MATCH), extras);
    }

    public void getCurrentLeaderboard(IBroadcastCallback callback) {
        receiver.callbacks.put(getAction(Method.GET_CURRENT_LEADERBOARD_DATA), callback);
        sendBroadcast(getAction(Method.GET_CURRENT_LEADERBOARD_DATA));
    }

    public void eventDoneNotify(String eventId, IBroadcastCallback callback) {
        Bundle extras = new Bundle();
        extras.putString(Param.EVENT_ID, eventId);
        receiver.callbacks.put(getAction(Method.EVENT_DONE_NOTIFY), callback);
        sendBroadcast(getAction(Method.EVENT_DONE_NOTIFY), extras);
    }

    public void getEventsByPackageName(String packageName, IBroadcastCallback callback) {
        Bundle extras = new Bundle();
        extras.putString(Param.PACKAGE_NAME, packageName);
        receiver.callbacks.put(getAction(Method.GET_EVENTS_BY_PACKAGE_NAME), callback);
        sendBroadcast(getAction(Method.GET_EVENTS_BY_PACKAGE_NAME), extras);
    }

    void dispose() {
        receiver.dispose();
    }
}
