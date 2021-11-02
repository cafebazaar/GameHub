package com.farsitel.bazaar.game;

interface IGameHub {

    boolean isLogin();

    Bundle startTournamentMatch(String packagename, String matchId, String metaData);

    Bundle endTournamentMatch(String sessionId, float coefficient);
    
    Bundle callMethod(in Bundle data);
}