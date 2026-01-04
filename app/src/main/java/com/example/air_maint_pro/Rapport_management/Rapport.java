package com.example.air_maint_pro.Rapport_management;

import com.google.firebase.Timestamp;

public class Rapport {
    private String id;
    private String title;
    private String contenu;
    private String type;
    private String statut;
    private Timestamp date_generation;
    private Timestamp per_debut;
    private Timestamp per_fin;

    // Empty constructor for Firebase
    public Rapport() {}

    // Constructor with all parameters (NO id parameter)
    public Rapport(String title, String contenu, String type, String statut,
                   Timestamp date_generation, Timestamp per_debut, Timestamp per_fin) {
        this.title = title;
        this.contenu = contenu;
        this.type = type;
        this.statut = statut;
        this.date_generation = date_generation;
        this.per_debut = per_debut;
        this.per_fin = per_fin;
    }

    // Getters and setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Timestamp getDate_generation() { return date_generation; }
    public void setDate_generation(Timestamp date_generation) { this.date_generation = date_generation; }

    public Timestamp getPer_debut() { return per_debut; }
    public void setPer_debut(Timestamp per_debut) { this.per_debut = per_debut; }

    public Timestamp getPer_fin() { return per_fin; }
    public void setPer_fin(Timestamp per_fin) { this.per_fin = per_fin; }
}