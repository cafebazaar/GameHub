package com.farsitel.bazaar.game.callbacks;

public interface IEventDoneCallback {

    void onFinish(int status, String message, String stackTrace, String effectiveDoneTime);
}
