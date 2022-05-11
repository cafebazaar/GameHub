package com.farsitel.bazaar.game.callbacks;

import com.farsitel.bazaar.game.data.RankItem;

import java.util.List;

public interface IRankingCallback {
    void onFinish(int status, String message, String stackTrace, List<RankItem> rankItems);
}
