package com.example.air_maint_pro;


import java.util.Date;

public class Avion {
    private String id;
    private String matricule;
    private String modele;
    private Date dateAchat;
    private int heuresVol;
    private String etat; // Actif, En maintenance, Hors service

    // Required empty constructor for Firestore
    public Avion() {}

    // Constructor
    public Avion(String matricule, String modele, Date dateAchat, int heuresVol, String etat) {
        this.matricule = matricule;
        this.modele = modele;
        this.dateAchat = dateAchat;
        this.heuresVol = heuresVol;
        this.etat = etat;
    }

    // Getters and setters
    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMatricule() {
        return matricule != null ? matricule : "";
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getModele() {
        return modele != null ? modele : "";
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public Date getDateAchat() {
        return dateAchat != null ? dateAchat : new Date();
    }

    public void setDateAchat(Date dateAchat) {
        this.dateAchat = dateAchat;
    }

    public int getHeuresVol() {
        return heuresVol;
    }

    public void setHeuresVol(int heuresVol) {
        this.heuresVol = heuresVol;
    }

    public String getEtat() {
        return etat != null ? etat : "Actif";
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getDisplayString() {
        return matricule + " - " + modele + " (" + heuresVol + "h)";
    }
}