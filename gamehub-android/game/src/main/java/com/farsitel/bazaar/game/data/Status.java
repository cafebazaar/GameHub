package com.farsitel.bazaar.game.data;

public enum Status {
    FAILURE(-1),

    SUCCESS(0),
    DISCONNECTED(1),
    LOGIN_BAZAAR(3),
    UPDATE_BAZAAR(4),
    INSTALL_BAZAAR(5),
    UNKNOWN(10);

    private final int levelCode;

    Status(int levelCode) {
        this.levelCode = levelCode;
    }

    public static Status fromLevelCode(int levelCode) {
        switch (levelCode) {
            case -1:
                return FAILURE;
            case 0:
                return SUCCESS;
            case 1:
                return DISCONNECTED;
            case 3:
                return LOGIN_BAZAAR;
            case 4:
                return UPDATE_BAZAAR;
            case 5:
                return INSTALL_BAZAAR;
            default:
                return UNKNOWN;
        }
    }

    public int getLevelCode() {
        return this.levelCode;
    }

}
