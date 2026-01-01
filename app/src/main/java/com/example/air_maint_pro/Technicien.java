package com.example.air_maint_pro;

public class Technicien {

    public String id;
    public String nom;
    public String prenom;
    public String email;
    public int age;
    public String departement;
    public String role;
    public long createdAt;

    // Constructeur vide requis pour Firestore
    public Technicien() {
    }

    // Constructeur complet
    public Technicien(String nom, String prenom, String email, int age, String departement) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.age = age;
        this.departement = departement;
        this.role = "technicien";
        this.createdAt = System.currentTimeMillis();
    }

    // Méthode utilitaire pour obtenir le nom complet
    public String getFullName() {
        return nom + " " + prenom;
    }
}