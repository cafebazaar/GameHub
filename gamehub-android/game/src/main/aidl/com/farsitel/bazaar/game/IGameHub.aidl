package com.farsitel.bazaar.game;

interface IGameHub {

    boolean isLogin();

    Bundle getTournamentTimes(String packageName);

    Bundle startTournamentMatch(String packageName, String matchId, String metaData);

    Bundle endTournamentMatch(String sessionId, float score);

    Bundle getLeaderboard(int tournamentId);

    Bundle getCurrentLeaderboard(String packageName);

    /**
     * Notify Bazaar when event id done by user
     * @params: id of the event
     * @Return: Bundle with the following data:
                STATUS
                EVENT_DONE_TIMESTAMP
     */
    Bundle eventDoneNotify(String eventId);
}