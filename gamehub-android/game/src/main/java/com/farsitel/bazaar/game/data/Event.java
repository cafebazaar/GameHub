package com.farsitel.bazaar.game.data;

public class Event {
    String eventId, startTimestamp, endTimestamp;

    public Event(String eventId, String startTimestamp, String endTimestamp) {
        this.eventId = eventId;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }
}