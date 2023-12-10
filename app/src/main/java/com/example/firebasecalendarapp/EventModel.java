package com.example.firebasecalendarapp;

public class EventModel {
    private String date;
    private String eventName;

    public EventModel(String date, String eventName) {
        this.date = date;
        this.eventName = eventName;
    }

    public String getDate() {
        return date;
    }

    public String getEventName() {
        return eventName;
    }
}

