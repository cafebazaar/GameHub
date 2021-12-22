package com.farsitel.bazaar.game;

interface IGameHub {

    boolean isLogin();

    Bundle getTournamentTimes(String packagename);

    Bundle startTournamentMatch(String packagename, String matchId, String metaData);

    Bundle endTournamentMatch(String sessionId, float score);

    Bundle getLeaderboard(int tournamentId);

    Bundle getCurrentLeaderboard(String packagename);
}