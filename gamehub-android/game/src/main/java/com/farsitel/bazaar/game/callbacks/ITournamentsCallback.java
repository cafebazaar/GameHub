package com.farsitel.bazaar.game.callbacks;

import com.farsitel.bazaar.game.data.Tournament;

import java.util.List;

public interface ITournamentsCallback {
    void onFinish(int status, String message, String stackTrace, List<Tournament> tournaments);
}
