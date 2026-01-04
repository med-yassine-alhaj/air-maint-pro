package com.example.air_maint_pro.gestion_vols;

import java.util.Date;

public class MembreEquipage {
    private String id;
    private String nom;
    private String prenom;
    private String role; // "Pilote", "Copilote", "Hôtesse", "Steward"
    private String matricule;
    private String email;
    private String telephone;
    private String qualifications; // Certifications, licences
    private boolean disponible;
    private long heuresVolTotal;
    private Date dateEmbauche;
    private Date dateCreated;

    // Constructeur vide pour Firestore
    public MembreEquipage() {
    }

    // Constructeur avec paramètres
    public MembreEquipage(String nom, String prenom, String role,
                          String matricule, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.matricule = matricule;
        this.email = email;
        this.disponible = true;
        this.dateCreated = new Date();
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getQualifications() { return qualifications; }
    public void setQualifications(String qualifications) { this.qualifications = qualifications; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public long getHeuresVolTotal() { return heuresVolTotal; }
    public void setHeuresVolTotal(long heuresVolTotal) { this.heuresVolTotal = heuresVolTotal; }

    public Date getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(Date dateEmbauche) { this.dateEmbauche = dateEmbauche; }

    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }

    // Méthodes utilitaires
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public String getInfoMembre() {
        return getNomComplet() + " - " + role + " (" + matricule + ")";
    }
}