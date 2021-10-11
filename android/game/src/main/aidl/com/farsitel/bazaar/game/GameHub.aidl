package com.farsitel.bazaar.game;

import android.os.Bundle;

interface GameHub {

    boolean isLogin();

    Bundle startTournamentMatch(String packagename, String matchId, String metaData);

    Bundle endTournamentMatch(String sessionId, float coefficient);
}