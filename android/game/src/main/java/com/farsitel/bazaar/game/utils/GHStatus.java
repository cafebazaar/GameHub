package com.farsitel.bazaar.game.utils;

public enum GHStatus {
    FAILURE(-1),

    SUCCESS(0),
    DISCONNECTED(1),
    UNKNOWN(5);

    private final int levelCode;

    GHStatus(int levelCode) {
        this.levelCode = levelCode;
    }

    public int getLevelCode() {
        return this.levelCode;
    }

}
