package com.farsitel.bazaar.game.callbacks;

import com.farsitel.bazaar.game.data.Event;

import java.util.List;

public interface IGetEventsCallback {

    void onFinish(int status, String message, String stackTrace, List<Event> events);
}
