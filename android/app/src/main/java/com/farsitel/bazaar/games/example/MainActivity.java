package com.farsitel.bazaar.games.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void connect(View view) {
        Log.i(GameServiceBridge.TAG, "connect");
    }

    public void startTournamentMatch(View view) {
        Log.i(GameServiceBridge.TAG, "startTournamentMatch");
    }

    public void endTournamentMatch(View view) {
        Log.i(GameServiceBridge.TAG, "endTournamentMatch");
    }

    public void showLastTournamentLeaderboard(View view) {
        Log.i(GameServiceBridge.TAG, "showLastTournamentLeaderboard");
    }
}