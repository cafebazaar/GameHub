package com.farsitel.bazaar.game.callbacks;

import java.io.Serializable;

public interface IEventDoneCallback extends Serializable {

    void onFinish(int status, String message, String stackTrace, String effectiveDoneTime);
}
