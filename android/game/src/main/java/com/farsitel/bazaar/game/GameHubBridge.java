package com.farsitel.bazaar.game;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.utils.GHLogger;
import com.farsitel.bazaar.game.utils.GHStatus;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class GameHubBridge extends AbstractGameHub {
    private static GameHubBridge instance;

    private Class<?> unityPlayerClass;
    private Field unityPlayerActivityField;
    private ServiceConnection gameHubConnection;
    private GameHub gameHubService;

    public GameHubBridge() {
    }

    public static GameHubBridge getInstance() {
        if (instance == null) {
            instance = new GameHubBridge();
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
        return instance;
    }

    private Activity getCurrentActivity() {
        if (unityPlayerActivityField != null)
            try {
                Activity activity = (Activity) unityPlayerActivityField.get(unityPlayerClass);
                if (activity == null)
                    logger.logError("The Unity Activity does not exist. This could be due to a low memory situation");
                return activity;
            } catch (Exception e) {
                logger.logInfo("Error getting currentActivity: " + e.getMessage());
            }
        return null;
    }


    public void connect(IConnectionCallback callback) {
        Log.i(GameServiceBridge.TAG, "connect");
        gameHubService = new GamesServiceConnection(callback, getCurrentActivity(), callback);
    }

    public void startTournamentMatch(ITournamentMatchCallback callback, String matchId, String metaData) {
        Log.i(GameServiceBridge.TAG, "startTournamentMatch");
        gameHubService.startTournamentMatch(callback, matchId, metaData);
    }

    public void endTournamentMatch(ITournamentMatchCallback callback, String sessionId, float coefficient) {
        Log.i(GameServiceBridge.TAG, "endTournamentMatch");
        gameHubService.endTournamentMatch(callback, sessionId, coefficient);
    }

    public void showLastTournamentLeaderboard() {
        Log.i(GameServiceBridge.TAG, "endTournamentMatch");
    }
}