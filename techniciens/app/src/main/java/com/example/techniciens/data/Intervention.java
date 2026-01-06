package com.example.techniciens.data;

public class Intervention {
    public long id;
    public String title;
    public long scheduledAt; // epoch millis
    public int durationHours;
    public String location;

    // Needed for Firestore deserialization
    public Intervention() {
    }

    public Intervention(String title, long scheduledAt, int durationHours, String location) {
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.durationHours = durationHours;
        this.location = location;
    }
}
