package com.example.unipicityvibe.models;

// Απλη κλαση για την αποθηκευση συντεταγμενων
public class EventLocation {
    private double latitude;
    private double longitude;

    public EventLocation() {
    }

    public EventLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }

    public void setLatitude(double latitude){
        this.latitude=latitude;
    }
    public void setLongitude(double longitude){
        this.longitude=longitude;
    }

}
