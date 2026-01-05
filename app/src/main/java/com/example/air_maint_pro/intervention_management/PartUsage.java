package com.example.air_maint_pro.intervention_management;

public class PartUsage {
    private Piece piece;
    private int quantity;

    // Empty constructor REQUIRED for Firestore
    public PartUsage() {
    }

    // Constructor with parameters
    public PartUsage(Piece piece, int quantity) {
        this.piece = piece;
        this.quantity = quantity;
    }

    // Getters and setters
    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Helper methods
    public float getTotalWeight() {
        if (piece == null) return 0.0f;
        return piece.getWeight() * quantity;
    }

    public float getTotalPrice() {
        if (piece == null) return 0.0f;
        return piece.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return "PartUsage{" +
                "piece=" + piece +
                ", quantity=" + quantity +
                '}';
    }
}



