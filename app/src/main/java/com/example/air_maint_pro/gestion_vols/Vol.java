package com.example.air_maint_pro.gestion_vols;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class Vol implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String numeroVol;
    private String villeDepart;
    private String villeArrivee;
    private Date dateDepart;
    private Date dateArrivee;
    private String avionId;
    private String avionMatricule;
    private String statut; // "Planifié", "En cours", "Terminé", "Annulé", "Retardé"
    private String observations;

    @ServerTimestamp
    private Date createdAt;

    private String createdBy;

    // Constructeur vide obligatoire pour Firestore
    public Vol() {
        this.createdAt = new Date();
        this.statut = "Planifié"; // Valeur par défaut
    }

    // Constructeur avec paramètres principaux
    public Vol(String numeroVol, String villeDepart, String villeArrivee,
               Date dateDepart, Date dateArrivee, String avionId,
               String avionMatricule) {
        this();
        this.numeroVol = numeroVol;
        this.villeDepart = villeDepart;
        this.villeArrivee = villeArrivee;
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
        this.avionId = avionId;
        this.avionMatricule = avionMatricule;
    }

    // Getters et Setters avec annotations pour Firestore si nécessaire

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("numero_vol")
    public String getNumeroVol() {
        return numeroVol;
    }

    @PropertyName("numero_vol")
    public void setNumeroVol(String numeroVol) {
        this.numeroVol = numeroVol;
    }

    @PropertyName("ville_depart")
    public String getVilleDepart() {
        return villeDepart;
    }

    @PropertyName("ville_depart")
    public void setVilleDepart(String villeDepart) {
        this.villeDepart = villeDepart;
    }

    @PropertyName("ville_arrivee")
    public String getVilleArrivee() {
        return villeArrivee;
    }

    @PropertyName("ville_arrivee")
    public void setVilleArrivee(String villeArrivee) {
        this.villeArrivee = villeArrivee;
    }

    @PropertyName("date_depart")
    public Date getDateDepart() {
        return dateDepart;
    }

    @PropertyName("date_depart")
    public void setDateDepart(Date dateDepart) {
        this.dateDepart = dateDepart;
    }

    @PropertyName("date_arrivee")
    public Date getDateArrivee() {
        return dateArrivee;
    }

    @PropertyName("date_arrivee")
    public void setDateArrivee(Date dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    @PropertyName("avion_id")
    public String getAvionId() {
        return avionId;
    }

    @PropertyName("avion_id")
    public void setAvionId(String avionId) {
        this.avionId = avionId;
    }

    @PropertyName("avion_matricule")
    public String getAvionMatricule() {
        return avionMatricule;
    }

    @PropertyName("avion_matricule")
    public void setAvionMatricule(String avionMatricule) {
        this.avionMatricule = avionMatricule;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    @PropertyName("created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @PropertyName("created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("created_by")
    public String getCreatedBy() {
        return createdBy;
    }

    @PropertyName("created_by")
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // Méthodes utilitaires pour l'affichage

    @Exclude
    public String getInfoVol() {
        return numeroVol + " : " + villeDepart + " → " + villeArrivee;
    }

    @Exclude
    public String getDetailsVol() {
        return "Vol " + numeroVol + "\n" +
                "Départ: " + villeDepart + " - " + dateDepart + "\n" +
                "Arrivée: " + villeArrivee + " - " + dateArrivee + "\n" +
                "Avion: " + (avionMatricule != null ? avionMatricule : "Non assigné") + "\n" +
                "Statut: " + statut;
    }

    // Méthode pour vérifier si le vol est en cours
    @Exclude
    public boolean isEnCours() {
        Date now = new Date();
        return "En cours".equals(statut) ||
                (dateDepart != null && dateArrivee != null &&
                        now.after(dateDepart) && now.before(dateArrivee));
    }

    // Méthode pour vérifier si le vol est terminé
    @Exclude
    public boolean isTermine() {
        Date now = new Date();
        return "Terminé".equals(statut) ||
                (dateArrivee != null && now.after(dateArrivee));
    }

    // Méthode pour vérifier si le vol est à venir
    @Exclude
    public boolean isAVenir() {
        Date now = new Date();
        return "Planifié".equals(statut) ||
                (dateDepart != null && now.before(dateDepart));
    }

    // Validation des données du vol
    @Exclude
    public boolean isValid() {
        return numeroVol != null && !numeroVol.trim().isEmpty() &&
                villeDepart != null && !villeDepart.trim().isEmpty() &&
                villeArrivee != null && !villeArrivee.trim().isEmpty() &&
                dateDepart != null && dateArrivee != null &&
                dateArrivee.after(dateDepart) &&
                avionId != null && !avionId.trim().isEmpty();
    }
}