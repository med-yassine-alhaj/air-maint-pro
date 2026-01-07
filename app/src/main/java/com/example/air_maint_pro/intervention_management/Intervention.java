package com.example.air_maint_pro.intervention_management;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Intervention {
    private String id; // Using String for Firestore document IDs
    private String avionId; // Changed from idAvion: int to String for Firestore
    private String technicienId; // Changed from idTechnicien: int to String
    private String superviseurId; // Changed from idSuperviseur: int to String
    private String typeIntervention;
    private float dureeHeures;
    private String statut;
    private String descriptionProbleme;
    private String remarques; // Observations/Remarks
    private String descriptionSolution;
    private Date createdAt;
    private Date updatedAt;
    private Date dateIntervention;
    private List<PartUsage> parts; // List of parts used in this intervention

    // Empty constructor REQUIRED for Firestore
    public Intervention() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.dateIntervention = new Date();
        this.statut = "Planifiée"; // Default status
        this.dureeHeures = 0.0f;
        this.parts = new ArrayList<>();
    }

    // Constructor with basic parameters
    public Intervention(String typeIntervention, String avionId, String technicienId) {
        this();
        this.typeIntervention = typeIntervention;
        this.avionId = avionId;
        this.technicienId = technicienId;
    }

    // Getters and setters with null safety

    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
        this.updatedAt = new Date();
    }

    public String getAvionId() {
        return avionId != null ? avionId : "";
    }

    public void setAvionId(String avionId) {
        this.avionId = avionId;
        this.updatedAt = new Date();
    }

    public String getTechnicienId() {
        return technicienId != null ? technicienId : "";
    }

    public void setTechnicienId(String technicienId) {
        this.technicienId = technicienId;
        this.updatedAt = new Date();
    }

    public String getSuperviseurId() {
        return superviseurId != null ? superviseurId : "";
    }

    public void setSuperviseurId(String superviseurId) {
        this.superviseurId = superviseurId;
        this.updatedAt = new Date();
    }

    public String getTypeIntervention() {
        return typeIntervention != null ? typeIntervention : "";
    }

    public void setTypeIntervention(String typeIntervention) {
        this.typeIntervention = typeIntervention;
        this.updatedAt = new Date();
    }

    public float getDureeHeures() {
        return dureeHeures;
    }

    public void setDureeHeures(float dureeHeures) {
        this.dureeHeures = dureeHeures;
        this.updatedAt = new Date();
    }

    public String getStatut() {
        return statut != null ? statut : "Planifiée";
    }

    public void setStatut(String statut) {
        this.statut = statut;
        this.updatedAt = new Date();
    }

    public String getDescriptionProbleme() {
        return descriptionProbleme != null ? descriptionProbleme : "";
    }

    public void setDescriptionProbleme(String descriptionProbleme) {
        this.descriptionProbleme = descriptionProbleme;
        this.updatedAt = new Date();
    }

    public String getRemarques() {
        return remarques != null ? remarques : "";
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
        this.updatedAt = new Date();
    }

    public String getDescriptionSolution() {
        return descriptionSolution != null ? descriptionSolution : "";
    }

    public void setDescriptionSolution(String descriptionSolution) {
        this.descriptionSolution = descriptionSolution;
        this.updatedAt = new Date();
    }

    public Date getCreatedAt() {
        return createdAt != null ? createdAt : new Date();
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt != null ? updatedAt : new Date();
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getDateIntervention() {
        return dateIntervention != null ? dateIntervention : new Date();
    }

    public void setDateIntervention(Date dateIntervention) {
        this.dateIntervention = dateIntervention;
        this.updatedAt = new Date();
    }

    public List<PartUsage> getParts() {
        if (parts == null) {
            parts = new ArrayList<>();
        }
        return parts;
    }

    public void setParts(List<PartUsage> parts) {
        this.parts = parts;
        this.updatedAt = new Date();
    }

    // Helper methods

    public boolean isCompleted() {
        return "Terminée".equals(statut) || "Clôturée".equals(statut);
    }

    public boolean isInProgress() {
        return "En cours".equals(statut);
    }

    public boolean isPlanned() {
        return "Planifiée".equals(statut);
    }

    // Override toString for debugging
    @Override
    public String toString() {
        return "Intervention{" +
                "id='" + id + '\'' +
                ", type='" + typeIntervention + '\'' +
                ", statut='" + statut + '\'' +
                ", avionId='" + avionId + '\'' +
                ", technicienId='" + technicienId + '\'' +
                '}';
    }
}