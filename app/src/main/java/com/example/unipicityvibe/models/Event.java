package com.example.unipicityvibe.models;

import java.math.BigDecimal;

public class Event {
    private String eventId;
    private String title;
    private String description;
    private long timestamp;
    private double ticketPrice;
    private String imageResName;
    private String imageUrl; // New field for linked images
    private EventLocation location;
    private float distanceMeter;


    public Event() {
    }

    public Event(String eventId, String title, String description, long timestamp, double ticketPrice, String imageResName, String imageUrl, EventLocation location) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.ticketPrice = ticketPrice;
        this.imageResName = imageResName;
        this.imageUrl = imageUrl;
        this.location = location;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getImageResName() {
        return imageResName;
    }

    public void setImageResName(String imageResName) {
        this.imageResName = imageResName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public EventLocation getLocation() {
        return location;
    }

    public void setLocation(EventLocation location) {
        this.location = location;
    }

    public float getDistanceMeter() {
        return distanceMeter;
    }

    public void setDistanceMeter(float distanceMeter) {
        this.distanceMeter = distanceMeter;
    }

    public String getFormattedDistance() {
        if (distanceMeter >= 1000) {
            return String.format(java.util.Locale.US, "%.1f km", distanceMeter / 1000);
        } else {
            return String.format(java.util.Locale.US, "%.0f m", distanceMeter);
        }
    }
}
