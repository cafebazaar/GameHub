package com.farsitel.bazaar.game.data;

public class Event {
    String id, startTimestamp, endTimestamp;

    public Event(String id, String startTimestamp, String endTimestamp) {
        this.id = id;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }
}