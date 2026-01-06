package com.example.techniciens.data;

public class TechnicianStats {
    public long technicianId;
    public int interventionsDone;
    public int hoursWorked;

    // Needed for Firestore deserialization
    public TechnicianStats() {
    }

    public TechnicianStats(long technicianId, int interventionsDone, int hoursWorked) {
        this.technicianId = technicianId;
        this.interventionsDone = interventionsDone;
        this.hoursWorked = hoursWorked;
    }
}
