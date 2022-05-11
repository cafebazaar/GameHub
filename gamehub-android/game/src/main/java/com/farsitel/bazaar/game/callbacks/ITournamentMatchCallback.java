package com.farsitel.bazaar.game.callbacks;

import com.farsitel.bazaar.game.data.Match;

public interface ITournamentMatchCallback {
    void onFinish(int status, String message, String stackTrace, Match match);
}
