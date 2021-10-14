package com.farsitel.bazaar.game.callbacks;

public interface ITournamentMatchCallback {
    void onFinish(int status, String message, String stackTrace, String data);
}
