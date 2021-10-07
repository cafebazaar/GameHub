package com.farsitel.bazaar.games.callbacks;

public interface IConnectionCallback {
    void onFinish(int status, String message, String stackTrace);
}
