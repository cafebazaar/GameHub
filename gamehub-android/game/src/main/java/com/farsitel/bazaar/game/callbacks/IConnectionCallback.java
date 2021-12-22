package com.farsitel.bazaar.game.callbacks;

public interface IConnectionCallback {
    void onFinish(int status, String message, String stackTrace);
}
