package com.farsitel.bazaar.game.data;

public enum Status {
    FAILURE(-1),

    SUCCESS(0),
    DISCONNECTED(1),
    LOGIN_CAFEBAZAAR(3),
    UPDATE_CAFEBAZAAR(4),
    INSTALL_CAFEBAZAAR(5),
    UNKNOWN(10);

    private final int levelCode;

    Status(int levelCode) {
        this.levelCode = levelCode;
    }

    public int getLevelCode() {
        return this.levelCode;
    }

}
