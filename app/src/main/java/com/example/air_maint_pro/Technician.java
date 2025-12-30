package com.example.air_maint_pro;

public class Technician {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String role;
    private boolean isActive;
    private long createdAt;

    // Constructeur vide REQUIS pour Firebase
    public Technician() {
        // Initialiser les valeurs par défaut
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters et setters
    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName != null ? firstName : "";
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName != null ? lastName : "";
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department != null ? department : "Non spécifié";
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role != null ? role : "Technicien";
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getFullName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        return (first + " " + last).trim();
    }
}