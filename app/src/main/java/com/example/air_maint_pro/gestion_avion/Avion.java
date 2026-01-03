package com.example.air_maint_pro.gestion_avion;

import java.util.Date;

public class Avion {
    public String id;
    public  String matricule;
    public String modele;
    public String type;
    public String compagnie;
    public String etat; // "Actif", "En maintenance", "Hors service"
    public double heuresVol;
    public Date derniereRevision;
    public Date prochaineRevision;
    public String description;
    public Date createdAt;
    public String createdBy;
    public int interventionCount;
    public String technicienAssignId;
    public String technicienAssignNom;

    // Constructeur vide nécessaire pour Firestore
    public Avion() {}

    // Constructeur avec paramètres
    public Avion(String matricule, String modele, String type, String compagnie,
                 String etat, double heuresVol, Date derniereRevision,
                 Date prochaineRevision, String description , int interventionCount) {
        this.matricule = matricule;
        this.modele = modele;
        this.type = type;
        this.compagnie = compagnie;
        this.etat = etat;
        this.heuresVol = heuresVol;
        this.derniereRevision = derniereRevision;
        this.prochaineRevision = prochaineRevision;
        this.description = description;
        this.createdAt = new Date();
        this.interventionCount = interventionCount;
    }

    // Getters
    public String getTechnicienAssignId() { return technicienAssignId; }
    public void setTechnicienAssignId(String technicienAssignId) { this.technicienAssignId = technicienAssignId; }

    public String getTechnicienAssignNom() { return technicienAssignNom; }
    public void setTechnicienAssignNom(String technicienAssignNom) { this.technicienAssignNom = technicienAssignNom; }
    public int getInterventionCount() {
        return interventionCount;
    }
    public void setInterventionCount(int interventionCount) {
        this.interventionCount = interventionCount;
    }
    public String getId() { return id; }
    public void setId(String id) {
        this.id = id;
    }
    public String getMatricule() { return matricule; }
    public String getModele() { return modele; }

    public String getType() { return type; }
    public String getCompagnie() { return compagnie; }
    public String getEtat() { return etat; }
    public double getHeuresVol() { return heuresVol; }
    public Date getDerniereRevision() { return derniereRevision; }
    public Date getProchaineRevision() { return prochaineRevision; }
    public String getDescription() { return description; }
    public Date getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }

    // Méthode pour affichage
    public String getFullInfo() {
        return matricule + " - " + modele + " (" + type + ")";
    }
}

