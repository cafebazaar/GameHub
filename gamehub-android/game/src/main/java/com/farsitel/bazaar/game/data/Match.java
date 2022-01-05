package com.farsitel.bazaar.game.data;

public class Match {
    public String id, sessionId, metadata;

    public Match(String id, String sessionId, String metadata) {
        this.id = id;
        this.sessionId = sessionId;
        this.metadata = metadata;
    }
}