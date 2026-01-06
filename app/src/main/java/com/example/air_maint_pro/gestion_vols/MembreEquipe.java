package com.example.air_maint_pro.gestion_vols;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class MembreEquipe implements Serializable {
    private static final long serialVersionUID = 1L;

    private String membreId; // Référence à l'ID dans la collection "equipage"
    private String nomComplet;
    private String role;
    private String matricule;

    // Constructeur vide obligatoire pour Firestore
    public MembreEquipe() {
    }

    // Constructeur avec paramètres
    public MembreEquipe(String membreId, String nomComplet, String role, String matricule) {
        this.membreId = membreId;
        this.nomComplet = nomComplet;
        this.role = role;
        this.matricule = matricule;
    }

    // Constructeur depuis un MembreEquipage
    public MembreEquipe(MembreEquipage membre) {
        this.membreId = membre.getId();
        this.nomComplet = membre.getNomComplet();
        this.role = membre.getRole();
        this.matricule = membre.getMatricule();
    }

    // === GETTERS ET SETTERS ===

    @PropertyName("membre_id")
    public String getMembreId() {
        return membreId;
    }

    @PropertyName("membre_id")
    public void setMembreId(String membreId) {
        this.membreId = membreId;
    }

    @PropertyName("nom_complet")
    public String getNomComplet() {
        return nomComplet;
    }

    @PropertyName("nom_complet")
    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    @PropertyName("role")
    public String getRole() {
        return role;
    }

    @PropertyName("role")
    public void setRole(String role) {
        this.role = role;
    }

    @PropertyName("matricule")
    public String getMatricule() {
        return matricule;
    }

    @PropertyName("matricule")
    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    // === MÉTHODES UTILITAIRES ===

    @Exclude
    public String getInfo() {
        return nomComplet + " - " + role + " (" + matricule + ")";
    }

    @Exclude
    public boolean isPilote() {
        return "Pilote".equals(role) || "Commandant de bord".equals(role);
    }

    @Exclude
    public boolean isCopilote() {
        return "Copilote".equals(role);
    }

    @Exclude
    public boolean isPersonnelCabine() {
        return "Hôtesse de l'air".equals(role) ||
                "Steward".equals(role) ||
                "Chef de cabine".equals(role);
    }

    @Exclude
    public boolean isTechnicien() {
        return "Ingénieur de vol".equals(role) ||
                "Mécanicien navigant".equals(role);
    }

    @Exclude
    public boolean isValid() {
        return membreId != null && !membreId.trim().isEmpty() &&
                nomComplet != null && !nomComplet.trim().isEmpty() &&
                role != null && !role.trim().isEmpty() &&
                matricule != null && !matricule.trim().isEmpty();
    }
}