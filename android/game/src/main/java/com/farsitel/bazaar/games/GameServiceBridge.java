package com.farsitel.bazaar.games;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.farsitel.bazaar.games.callbacks.IConnectionCallback;
import com.farsitel.bazaar.games.callbacks.ITournamentMatchCallback;

import java.lang.reflect.Field;

public class GameServiceBridge {
    public static String TAG = "bazaar_games";
    private static GameServiceBridge instance;

    private Class<?> mUnityPlayerClass;
    private Field mUnityPlayerActivityField;
    private GamesServiceConnection serveic;

    private GameServiceBridge() {
        try {
            // Using reflection to remove reference to Unity library.
            mUnityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer");
            mUnityPlayerActivityField = mUnityPlayerClass.getField("currentActivity");
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "Could not find UnityPlayer class: " + e.getMessage());
        } catch (NoSuchFieldException e) {
            Log.i(TAG, "Could not find currentActivity field: " + e.getMessage());
        } catch (Exception e) {
            Log.i(TAG, "Unknown exception occurred locating UnitySendMessage(): " + e.getMessage());
        }
    }

    public static GameServiceBridge getInstance() {
        if (instance == null) {
            instance = new GameServiceBridge();
        }
        return instance;
    }

    private Activity getCurrentActivity() {
        if (mUnityPlayerActivityField != null)
            try {
                Activity activity = (Activity) mUnityPlayerActivityField.get(mUnityPlayerClass);
                if (activity == null)
                    Log.e(TAG, "The Unity Activity does not exist. This could be due to a low memory situation");
                return activity;
            } catch (Exception e) {
                Log.i(TAG, "Error getting currentActivity: " + e.getMessage());
            }
        return null;
    }

    public void connect(IConnectionCallback callback) {
        Log.i(GameServiceBridge.TAG, "connect");
        serveic = new GamesServiceConnection(callback, getCurrentActivity(), callback);
    }

    public void startTournamentMatch(ITournamentMatchCallback callback, String matchId, String metaData) {
        Log.i(GameServiceBridge.TAG, "startTournamentMatch");
        serveic.startTournamentMatch(callback, matchId, metaData);
    }

    public void endTournamentMatch(ITournamentMatchCallback callback, String sessionId, float coefficient) {
        Log.i(GameServiceBridge.TAG, "endTournamentMatch");
        serveic.endTournamentMatch(callback, sessionId, coefficient);
    }

    public void showLastTournamentLeaderboard() {
        Log.i(GameServiceBridge.TAG, "endTournamentMatch");
    }
}