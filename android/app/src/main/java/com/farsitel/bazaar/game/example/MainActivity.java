package com.farsitel.bazaar.game.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.farsitel.bazaar.game.GameHubBridge;
import com.farsitel.bazaar.game.callbacks.IConnectionCallback;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;

public class MainActivity extends Activity {

    private GameHubBridge gameHubBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameHubBridge = new GameHubBridge();
    }

    public void connect(View view) {
        IConnectionCallback callback = (status, message, stackTrace) -> {
        };
        gameHubBridge.connect(this, callback);
    }

    public void startTournamentMatch(View view) {
        ITournamentMatchCallback callback = (status, message, stackTrace) -> {
        };
        gameHubBridge.startTournamentMatch(this, callback, "match_id", "extra");
    }

    public void endTournamentMatch(View view) {
        ITournamentMatchCallback callback = (status, message, stackTrace) -> {
        };
        gameHubBridge.endTournamentMatch(callback, "session_id", 0.5f);
    }

    public void showLastTournamentLeaderboard(View view) {
        gameHubBridge.showLastTournamentLeaderboard(this);
    }
}