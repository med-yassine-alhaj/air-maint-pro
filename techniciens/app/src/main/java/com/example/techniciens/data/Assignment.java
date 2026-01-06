package com.example.techniciens.data;

public class Assignment {
    public long id;
    public long technicianId;
    public long interventionId;
    public long assignedAt;
    public String status; // e.g., planned, done, canceled

    // Needed for Firestore deserialization
    public Assignment() {
    }

    public Assignment(long technicianId, long interventionId, long assignedAt, String status) {
        this.technicianId = technicianId;
        this.interventionId = interventionId;
        this.assignedAt = assignedAt;
        this.status = status;
    }
}
