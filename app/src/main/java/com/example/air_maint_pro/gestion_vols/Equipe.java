package com.example.air_maint_pro.gestion_vols;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Equipe implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nom;
    private String code; // Code court unique : "ALPHA", "BRAVO", etc.
    private String statut; // "Disponible", "En mission", "En repos", "Indisponible"
    private String description;

    private List<MembreEquipe> membres;

    @ServerTimestamp
    private Date dateCreation;

    @ServerTimestamp
    private Date dateModification;

    private Date disponibleAPartirDe; // Pour gérer les rotations/repas

    private String createdBy;
    private String volAssignéId; // ID du vol auquel l'équipe est actuellement assignée
    private String volAssignéNumero; // Numéro du vol pour affichage

    // Constructeur vide obligatoire pour Firestore
    public Equipe() {
        this.membres = new ArrayList<>();
        this.statut = "Disponible";
        this.dateCreation = new Date();
        this.dateModification = new Date();
    }

    // Constructeur avec paramètres de base
    public Equipe(String nom, String code) {
        this();
        this.nom = nom;
        this.code = code;
    }

    // Constructeur complet
    public Equipe(String nom, String code, String description, List<MembreEquipe> membres) {
        this(nom, code);
        this.description = description;
        if (membres != null) {
            this.membres = membres;
        }
    }

    // === GETTERS ET SETTERS avec annotations Firestore ===

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

    @PropertyName("code")
    public String getCode() {
        return code;
    }

    @PropertyName("code")
    public void setCode(String code) {
        this.code = code;
    }

    @PropertyName("statut")
    public String getStatut() {
        return statut;
    }

    @PropertyName("statut")
    public void setStatut(String statut) {
        this.statut = statut;
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("membres")
    public List<MembreEquipe> getMembres() {
        return membres;
    }

    @PropertyName("membres")
    public void setMembres(List<MembreEquipe> membres) {
        this.membres = membres;
    }

    @PropertyName("date_creation")
    public Date getDateCreation() {
        return dateCreation;
    }

    @PropertyName("date_creation")
    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    @PropertyName("date_modification")
    public Date getDateModification() {
        return dateModification;
    }

    @PropertyName("date_modification")
    public void setDateModification(Date dateModification) {
        this.dateModification = dateModification;
    }

    @PropertyName("disponible_a_partir_de")
    public Date getDisponibleAPartirDe() {
        return disponibleAPartirDe;
    }

    @PropertyName("disponible_a_partir_de")
    public void setDisponibleAPartirDe(Date disponibleAPartirDe) {
        this.disponibleAPartirDe = disponibleAPartirDe;
    }

    @PropertyName("created_by")
    public String getCreatedBy() {
        return createdBy;
    }

    @PropertyName("created_by")
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @PropertyName("vol_assigné_id")
    public String getVolAssignéId() {
        return volAssignéId;
    }

    @PropertyName("vol_assigné_id")
    public void setVolAssignéId(String volAssignéId) {
        this.volAssignéId = volAssignéId;
    }

    @PropertyName("vol_assigné_numero")
    public String getVolAssignéNumero() {
        return volAssignéNumero;
    }

    @PropertyName("vol_assigné_numero")
    public void setVolAssignéNumero(String volAssignéNumero) {
        this.volAssignéNumero = volAssignéNumero;
    }

    // === MÉTHODES UTILITAIRES ===

    @Exclude
    public String getNomComplet() {
        return nom + " (" + code + ")";
    }

    @Exclude
    public String getDetailsEquipe() {
        return "Équipe: " + nom + " (" + code + ")\n" +
                "Statut: " + statut + "\n" +
                "Membres: " + getNombreMembres() + " personne(s)\n" +
                "Créée le: " + dateCreation;
    }

    @Exclude
    public int getNombreMembres() {
        return membres != null ? membres.size() : 0;
    }

    @Exclude
    public boolean isDisponible() {
        return "Disponible".equals(statut);
    }

    @Exclude
    public boolean isEnMission() {
        return "En mission".equals(statut);
    }

    @Exclude
    public boolean isEnRepos() {
        return "En repos".equals(statut);
    }

    @Exclude
    public boolean isIndisponible() {
        return "Indisponible".equals(statut);
    }

    @Exclude
    public boolean aUnVolAssigné() {
        return volAssignéId != null && !volAssignéId.isEmpty();
    }

    // Vérifier si l'équipe a la composition minimale requise
    @Exclude
    public boolean aCompositionMinimale() {
        if (membres == null || membres.isEmpty()) {
            return false;
        }

        int pilotes = 0;
        int copilotes = 0;
        int personnelCabine = 0;

        for (MembreEquipe membre : membres) {
            String role = membre.getRole();
            if ("Pilote".equals(role) || "Commandant de bord".equals(role)) {
                pilotes++;
            } else if ("Copilote".equals(role)) {
                copilotes++;
            } else if ("Hôtesse de l'air".equals(role) ||
                    "Steward".equals(role) ||
                    "Chef de cabine".equals(role)) {
                personnelCabine++;
            }
        }

        // Composition minimale : au moins 1 pilote, 1 copilote, 2 personnels cabine
        return pilotes >= 1 && copilotes >= 1 && personnelCabine >= 2;
    }

    @Exclude
    public List<MembreEquipe> getPilotes() {
        List<MembreEquipe> pilotes = new ArrayList<>();
        if (membres != null) {
            for (MembreEquipe membre : membres) {
                String role = membre.getRole();
                if ("Pilote".equals(role) || "Commandant de bord".equals(role)) {
                    pilotes.add(membre);
                }
            }
        }
        return pilotes;
    }

    @Exclude
    public List<MembreEquipe> getCopilotes() {
        List<MembreEquipe> copilotes = new ArrayList<>();
        if (membres != null) {
            for (MembreEquipe membre : membres) {
                if ("Copilote".equals(membre.getRole())) {
                    copilotes.add(membre);
                }
            }
        }
        return copilotes;
    }

    @Exclude
    public List<MembreEquipe> getPersonnelCabine() {
        List<MembreEquipe> personnel = new ArrayList<>();
        if (membres != null) {
            for (MembreEquipe membre : membres) {
                String role = membre.getRole();
                if ("Hôtesse de l'air".equals(role) ||
                        "Steward".equals(role) ||
                        "Chef de cabine".equals(role)) {
                    personnel.add(membre);
                }
            }
        }
        return personnel;
    }

    @Exclude
    public List<MembreEquipe> getTechniciens() {
        List<MembreEquipe> techniciens = new ArrayList<>();
        if (membres != null) {
            for (MembreEquipe membre : membres) {
                String role = membre.getRole();
                if ("Ingénieur de vol".equals(role) ||
                        "Mécanicien navigant".equals(role)) {
                    techniciens.add(membre);
                }
            }
        }
        return techniciens;
    }

    // Ajouter un membre à l'équipe
    @Exclude
    public boolean ajouterMembre(MembreEquipe membre) {
        if (membres == null) {
            membres = new ArrayList<>();
        }

        // Vérifier si le membre n'est pas déjà dans l'équipe
        for (MembreEquipe m : membres) {
            if (m.getMembreId().equals(membre.getMembreId())) {
                return false; // Membre déjà présent
            }
        }

        // Limiter à 6 membres maximum
        if (membres.size() >= 6) {
            return false;
        }

        membres.add(membre);
        return true;
    }

    // Retirer un membre de l'équipe
    @Exclude
    public boolean retirerMembre(String membreId) {
        if (membres != null) {
            for (int i = 0; i < membres.size(); i++) {
                if (membres.get(i).getMembreId().equals(membreId)) {
                    membres.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    // Vérifier si un membre spécifique est dans l'équipe
    @Exclude
    public boolean contientMembre(String membreId) {
        if (membres != null) {
            for (MembreEquipe membre : membres) {
                if (membre.getMembreId().equals(membreId)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Obtenir le rôle d'un membre spécifique dans l'équipe
    @Exclude
    public String getRoleMembre(String membreId) {
        if (membres != null) {
            for (MembreEquipe membre : membres) {
                if (membre.getMembreId().equals(membreId)) {
                    return membre.getRole();
                }
            }
        }
        return null;
    }

    // Validation des données de l'équipe
    @Exclude
    public boolean isValid() {
        return nom != null && !nom.trim().isEmpty() &&
                code != null && !code.trim().isEmpty() &&
                membres != null && !membres.isEmpty() &&
                aCompositionMinimale();
    }

    // Mettre à jour la date de modification
    @Exclude
    public void mettreAJourDateModification() {
        this.dateModification = new Date();
    }

    // Assigner un vol à cette équipe
    @Exclude
    public void assignerVol(String volId, String volNumero) {
        this.volAssignéId = volId;
        this.volAssignéNumero = volNumero;
        this.statut = "En mission";
        mettreAJourDateModification();
    }

    // Libérer l'équipe d'un vol (après fin de mission)
    @Exclude
    public void libererVol() {
        this.volAssignéId = null;
        this.volAssignéNumero = null;
        this.statut = "En repos";
        this.disponibleAPartirDe = new Date(); // Disponible après un temps de repos
        mettreAJourDateModification();
    }

    // Marquer l'équipe comme disponible
    @Exclude
    public void marquerCommeDisponible() {
        this.statut = "Disponible";
        this.disponibleAPartirDe = null;
        mettreAJourDateModification();
    }

    // Vérifier si l'équipe est disponible maintenant
    @Exclude
    public boolean estDisponibleMaintenant() {
        if (!"Disponible".equals(statut)) {
            return false;
        }

        if (disponibleAPartirDe != null) {
            Date maintenant = new Date();
            return maintenant.after(disponibleAPartirDe);
        }

        return true;
    }
}