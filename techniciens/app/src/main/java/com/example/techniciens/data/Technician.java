package com.example.techniciens.data;

public class Technician {
    public long id;
    public String name;
    public String specialty;
    public int experienceYears;
    public boolean available;

    // Needed for Firestore deserialization
    public Technician() {
    }

    public Technician(String name, String specialty, int experienceYears, boolean available) {
        this.name = name;
        this.specialty = specialty;
        this.experienceYears = experienceYears;
        this.available = available;
    }
}
