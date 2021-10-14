package com.farsitel.bazaar.game.utils;

public enum GHStatus {
    FAILURE(-1),

    SUCCESS(0),
    DISCONNECTED(1),
    LOGIN_CAFEBAZAAR(3),
    UPDATE_CAFEBAZAAR(4),
    INSTALL_CAFEBAZAAR(5),
    UNKNOWN(10);

    private final int levelCode;

    GHStatus(int levelCode) {
        this.levelCode = levelCode;
    }

    public int getLevelCode() {
        return this.levelCode;
    }

}
