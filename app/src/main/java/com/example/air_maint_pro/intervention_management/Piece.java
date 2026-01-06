package com.example.air_maint_pro.intervention_management;

public class Piece {
    private String name;
    private String description;
    private float weight; // in kg
    private float price; // unit price

    // Empty constructor REQUIRED for Firestore
    public Piece() {
    }

    // Constructor with all parameters
    public Piece(String name, String description, float weight, float price) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.price = price;
    }

    // Getters and setters
    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Piece{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", weight=" + weight +
                ", price=" + price +
                '}';
    }
}








