package com.farsitel.bazaar.game.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;

public class MainActivity extends Activity {
    private GamesServiceConnection service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void connect(View view) {
        Log.i(GameServiceBridge.TAG, "connect");
        IConnectionCallback callback = (status, message, stackTrace) -> {
        };
//        service = new GamesServiceConnection(callback, this, callback);
    }

    public void startTournamentMatch(View view) {
        ITournamentMatchCallback callback = (status, message, stackTrace) -> {
        };
//        service.startTournamentMatch(callback, "match_id", "extra");
        Log.i(GameServiceBridge.TAG, "startTournamentMatch");
    }

    public void endTournamentMatch(View view) {
        ITournamentMatchCallback callback = (status, message, stackTrace) -> {
        };
//        service.endTournamentMatch(callback, "session_id", 0.5f);
        Log.i(GameServiceBridge.TAG, "endTournamentMatch");
    }

    public void showLastTournamentLeaderboard(View view) {
        Log.i(GameServiceBridge.TAG, "showLastTournamentLeaderboard");
    }
}