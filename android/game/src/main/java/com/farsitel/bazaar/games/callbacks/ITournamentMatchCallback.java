package com.farsitel.bazaar.games.callbacks;

public interface ITournamentMatchCallback {
    void onFinish(int status, String message, String stackTrace);
}
