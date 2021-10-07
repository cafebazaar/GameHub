package com.farsitell.bazaar.games;

import android.os.Bundle;

interface GameHub {

    Bundle startTournamentMatch(String packagename, String matchId, String metaData);

    Bundle endTournamentMatch(String sessionId, float coefficient);

    void showLastTournamentLeaderboard();
}