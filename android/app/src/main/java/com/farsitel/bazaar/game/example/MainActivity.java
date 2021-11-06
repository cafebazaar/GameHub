package com.farsitel.bazaar.game.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.farsitel.bazaar.game.GameHubBridge;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.data.Status;
import com.farsitel.bazaar.game.data.Tournament;
import com.farsitel.bazaar.game.utils.GHLogger;

public class MainActivity extends Activity {

    private GameHubBridge gameHubBridge;
    private String reservedSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameHubBridge = new GameHubBridge();
    }

    public void connect(View view) {
        gameHubBridge.connect(this, true, (status, message, stackTrace) -> {
            Log.i(GHLogger.TAG, String.format("Connect => Status: %d, Message: %s, StackTrace: %s", status, message, stackTrace));
        });
    }

    public void getTournaments(View view) {
        gameHubBridge.getTournaments(this, (status, message, stackTrace, tournaments) -> {
            Log.i(GHLogger.TAG, String.format("Tournaments => Status: %d, Message: %s, StackTrace: %s", status, message, stackTrace));
        });
    }

    public void startTournamentMatch(View view) {
        ITournamentMatchCallback callback = (status, sessionId, matchId, metaData) -> {
            Log.i(GHLogger.TAG, String.format("Start => Status: %d, SessionId: %s, MatchId: %s, MetaData: %s", status, sessionId, matchId, metaData));
            if (status == Status.SUCCESS.getLevelCode()) {
                reservedSessionId = sessionId;
            }
        };
        gameHubBridge.startTournamentMatch(this, callback, "-1", "extra");
    }

    public void endTournamentMatch(View view) {
        if (reservedSessionId == null) {
            Log.e(GHLogger.TAG, "Call startTournamentMatch before!");
            return;
        }
        ITournamentMatchCallback callback = (status, sessionId, matchId, metaData) -> {
            Log.i(GHLogger.TAG, String.format("End => Status: %d, SessionId: %s, MatchId: %s, MetaData: %s", status, sessionId, matchId, metaData));
            reservedSessionId = null;
        };
        gameHubBridge.endTournamentMatch(callback, reservedSessionId, 0.5f);
    }

    public void showLastTournamentLeaderboard(View view) {
        gameHubBridge.showTournamentLeaderboard(this, "-1", (status, message, stackTrace) -> {
            Log.i(GHLogger.TAG, String.format("showLeaderboard => Status: %d, Message: %s, StackTrace: %s", status, message, stackTrace));
        });
    }
}