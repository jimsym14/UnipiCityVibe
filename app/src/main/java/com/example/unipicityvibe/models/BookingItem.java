package com.example.unipicityvibe.models;

public class BookingItem {
    public String id;
    public String title;
    public String code;
    public long timestamp;

    public BookingItem(String id, String title, String code, long timestamp) {
        this.id = id;
        this.title = title;
        this.code = code;
        this.timestamp = timestamp;
    }
}
