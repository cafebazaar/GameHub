package com.farsitel.bazaar.game.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.farsitel.bazaar.game.GameHub;
import com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback;
import com.farsitel.bazaar.game.data.Status;
import com.farsitel.bazaar.game.utils.Logger;

public class MainActivity extends Activity {

    private GameHub gameHub;
    private String reservedSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameHub = new GameHub();
        Log.i(Logger.TAG, String.format("Version => %s", gameHub.getVersion()));
    }

    public void connect(View view) {
        gameHub.connect(getApplicationContext(), true, (status, message, stackTrace) -> {
            Log.i(Logger.TAG, String.format("Connect => Status: %d, Message: %s, StackTrace: %s", status, message, stackTrace));
        });
    }

    public void getTournaments(View view) {
        gameHub.getTournaments(this, (status, message, stackTrace, tournaments) -> {
            Log.i(Logger.TAG, String.format("Tournaments => Status: %d, Message: %s, StackTrace: %s", status, message, stackTrace));
        });
    }

    public void startTournamentMatch(View view) {
        ITournamentMatchCallback callback = (status, sessionId, matchId, metaData) -> {
            Log.i(Logger.TAG, String.format("Start => Status: %d, SessionId: %s, MatchId: %s, MetaData: %s", status, sessionId, matchId, metaData));
            if (status == Status.SUCCESS.getLevelCode()) {
                reservedSessionId = sessionId;
            }
        };
        gameHub.startTournamentMatch(this, callback, "OgMSbLOC", "extra");
    }

    public void endTournamentMatch(View view) {
        if (reservedSessionId == null) {
            Log.e(Logger.TAG, "Call startTournamentMatch before!");
            return;
        }
        ITournamentMatchCallback callback = (status, sessionId, matchId, metaData) -> {
            Log.i(Logger.TAG, String.format("End => Status: %d, SessionId: %s, MatchId: %s, MetaData: %s", status, sessionId, matchId, metaData));
            reservedSessionId = null;
        };
        gameHub.endTournamentMatch(callback, reservedSessionId, 0.5f);
    }

    public void showTournamentRanking(View view) {
        gameHub.showTournamentRanking(getApplicationContext(), "-1", (status, message, stackTrace) -> {
            Log.i(Logger.TAG, String.format("showTournamentRanking => Status: %d, Message: %s, StackTrace: %s", status, message, stackTrace));
        });
    }

    public void getTournamentRanking(View view) {
        gameHub.getTournamentRanking(getApplicationContext(), "-1", (status, message, stackTrace, rankItems) -> {
            Log.i(Logger.TAG, String.format("getTournamentRanking => Status: %d, Message: %s, StackTrace: %s", status, message, stackTrace));
        });
    }

    public void eventDoneNotify(View view) {
        gameHub.eventDoneNotify(getApplicationContext(), "eventId", (status, message, stackTrace, rankItems) -> {
            Log.i(Logger.TAG, String.format("eventDoneNotify => Status: %d, Message: %s, StackTrace: %s", status, message, stackTrace));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameHub.dispose();
    }
}