package com.example.air_maint_pro.gestion_vols;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class MembreEquipage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nom;
    private String prenom;
    private String role; // "Pilote", "Copilote", "Hôtesse", "Steward", etc.
    private String matricule;
    private String email;
    private String telephone;
    private String qualifications; // Certifications, licences
    private boolean disponible;
    private long heuresVolTotal;

    @ServerTimestamp
    private Date dateEmbauche;

    @ServerTimestamp
    private Date dateCreated;

    // Constructeur vide obligatoire pour Firestore
    public MembreEquipage() {
        this.disponible = true;
        this.heuresVolTotal = 0;
        this.dateCreated = new Date();
    }

    // Constructeur avec paramètres principaux
    public MembreEquipage(String nom, String prenom, String role,
                          String matricule, String email) {
        this();
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.matricule = matricule;
        this.email = email;
    }

    // Getters et Setters avec annotations pour Firestore

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("nom")
    public String getNom() {
        return nom;
    }

    @PropertyName("nom")
    public void setNom(String nom) {
        this.nom = nom;
    }

    @PropertyName("prenom")
    public String getPrenom() {
        return prenom;
    }

    @PropertyName("prenom")
    public void setPrenom(String prenom) {
        this.prenom = prenom;
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

    @PropertyName("email")
    public String getEmail() {
        return email;
    }

    @PropertyName("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @PropertyName("telephone")
    public String getTelephone() {
        return telephone;
    }

    @PropertyName("telephone")
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @PropertyName("qualifications")
    public String getQualifications() {
        return qualifications;
    }

    @PropertyName("qualifications")
    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    @PropertyName("disponible")
    public boolean isDisponible() {
        return disponible;
    }

    @PropertyName("disponible")
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @PropertyName("heures_vol_total")
    public long getHeuresVolTotal() {
        return heuresVolTotal;
    }

    @PropertyName("heures_vol_total")
    public void setHeuresVolTotal(long heuresVolTotal) {
        this.heuresVolTotal = heuresVolTotal;
    }

    @PropertyName("date_embauche")
    public Date getDateEmbauche() {
        return dateEmbauche;
    }

    @PropertyName("date_embauche")
    public void setDateEmbauche(Date dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    @PropertyName("date_created")
    public Date getDateCreated() {
        return dateCreated;
    }

    @PropertyName("date_created")
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    // Méthodes utilitaires

    @Exclude
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Exclude
    public String getInfoMembre() {
        return getNomComplet() + " - " + role + " (" + matricule + ")";
    }

    @Exclude
    public String getDetailsMembre() {
        return getNomComplet() + "\n" +
                "Rôle: " + role + "\n" +
                "Matricule: " + matricule + "\n" +
                "Email: " + email + "\n" +
                "Téléphone: " + (telephone != null ? telephone : "Non renseigné") + "\n" +
                "Disponible: " + (disponible ? "Oui" : "Non") + "\n" +
                "Heures de vol total: " + heuresVolTotal + "h";
    }

    @Exclude
    public boolean isPilote() {
        return "Pilote".equalsIgnoreCase(role) ||
                "Copilote".equalsIgnoreCase(role) ||
                "Commandant de bord".equalsIgnoreCase(role);
    }

    @Exclude
    public boolean isPersonnelCabine() {
        return "Hôtesse de l'air".equalsIgnoreCase(role) ||
                "Steward".equalsIgnoreCase(role) ||
                "Chef de cabine".equalsIgnoreCase(role);
    }

    @Exclude
    public boolean isTechnicien() {
        return "Ingénieur de vol".equalsIgnoreCase(role) ||
                "Mécanicien navigant".equalsIgnoreCase(role);
    }

    @Exclude
    public void ajouterHeuresVol(long heures) {
        this.heuresVolTotal += heures;
    }

    @Exclude
    public boolean isValid() {
        return nom != null && !nom.trim().isEmpty() &&
                prenom != null && !prenom.trim().isEmpty() &&
                role != null && !role.trim().isEmpty() &&
                matricule != null && !matricule.trim().isEmpty() &&
                email != null && !email.trim().isEmpty();
    }

    @Exclude
    public static String getRoleDisplay(String role) {
        switch (role) {
            case "Pilote":
                return "👨‍✈️ Pilote";
            case "Copilote":
                return "👨‍✈️ Copilote";
            case "Hôtesse de l'air":
                return "👩‍✈️ Hôtesse de l'air";
            case "Steward":
                return "👨‍✈️ Steward";
            case "Chef de cabine":
                return "👨‍✈️ Chef de cabine";
            case "Ingénieur de vol":
                return "👨‍🔧 Ingénieur de vol";
            case "Mécanicien navigant":
                return "👨‍🔧 Mécanicien navigant";
            default:
                return role;
        }
    }

    @Exclude
    public String getRoleDisplay() {
        return getRoleDisplay(this.role);
    }
}