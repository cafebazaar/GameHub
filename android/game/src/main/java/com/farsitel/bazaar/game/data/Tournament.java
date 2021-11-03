package com.farsitel.bazaar.game.data;

public class Tournament {
    public String id, title;
    long startAt, endAt;

    public Tournament(String id, String title, long startAt, long endAt) {
        this.id = id;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}