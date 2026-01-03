package com.example.air_maint_pro.gestion_avion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.air_maint_pro.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AvionDetailsActivity extends AppCompatActivity {

    private TextView tvImmatriculation, tvEtat, tvModele, tvType, tvCompagnie,
            tvHeuresVol, tvDerniereRevision, tvProchaineRevision, tvDescription;
    private Button btnQrCode, btnInterventions, btnEdit, btnDelete;

    private FirebaseFirestore db;
    private String avionId;
    private Avion avion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avion_details);

        // Initialisation
        db = FirebaseFirestore.getInstance();

        // Récupérer l'ID de l'avion
        Intent intent = getIntent();
        avionId = intent.getStringExtra("avion_id");

        if (avionId == null) {
            Toast.makeText(this, "Erreur: Aucun avion sélectionné", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        initViews();

        // Charger les données de l'avion
        loadAvionDetails();

        // Configurer les écouteurs
        setupListeners();
    }

    private void initViews() {
        tvImmatriculation = findViewById(R.id.tvImmatriculation);
        tvEtat = findViewById(R.id.tvEtat);
        tvModele = findViewById(R.id.tvModele);
        tvType = findViewById(R.id.tvType);
        tvCompagnie = findViewById(R.id.tvCompagnie);
        tvHeuresVol = findViewById(R.id.tvHeuresVol);
        tvDerniereRevision = findViewById(R.id.tvDerniereRevision);
        tvProchaineRevision = findViewById(R.id.tvProchaineRevision);
        tvDescription = findViewById(R.id.tvDescription);

        btnQrCode = findViewById(R.id.btnQrCode);
        btnInterventions = findViewById(R.id.btnInterventions);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void loadAvionDetails() {
        db.collection("Avions")
                .document(avionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        avion = documentSnapshot.toObject(Avion.class);
                        if (avion != null) {
                            avion.id = documentSnapshot.getId();
                            displayAvionDetails();
                        }
                    } else {
                        Toast.makeText(this, "Avion introuvable", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayAvionDetails() {
        if (avion == null) return;

        // Immatriculation
        tvImmatriculation.setText(avion.matricule);

        // État avec couleur
        tvEtat.setText(avion.etat);
        switch (avion.etat) {
            case "Actif":
                tvEtat.setBackgroundResource(R.drawable.bg_etat_actif);
                break;
            case "En maintenance":
                tvEtat.setBackgroundResource(R.drawable.bg_etat_maintenance);
                break;
            case "Hors service":
                tvEtat.setBackgroundResource(R.drawable.bg_etat_hors_service);
                break;
        }

        // Modèle
        tvModele.setText(avion.modele);

        // Type
        tvType.setText(avion.type != null ? avion.type : "Non spécifié");

        // Compagnie
        tvCompagnie.setText(avion.compagnie != null ? avion.compagnie : "Non spécifiée");

        // Heures de vol
        tvHeuresVol.setText(String.format(Locale.FRENCH, "%.0f h", avion.heuresVol));

        // Dates de révision
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);

        if (avion.derniereRevision != null) {
            tvDerniereRevision.setText(dateFormat.format(avion.derniereRevision));
        } else {
            tvDerniereRevision.setText("Non définie");
        }

        if (avion.prochaineRevision != null) {
            tvProchaineRevision.setText(dateFormat.format(avion.prochaineRevision));
        } else {
            tvProchaineRevision.setText("Non définie");
        }

        // Description
        if (avion.description != null && !avion.description.isEmpty()) {
            tvDescription.setText(avion.description);
        } else {
            tvDescription.setText("Aucune description");
        }
    }

    private void setupListeners() {
        // QR Code
        btnQrCode.setOnClickListener(v -> {
            generateQrCode();
        });

        // Interventions
        btnInterventions.setOnClickListener(v -> {
            openInterventions();
        });

        // Modifier
        btnEdit.setOnClickListener(v -> {
            editAvion();
        });

        // Supprimer
        btnDelete.setOnClickListener(v -> {
            deleteAvion();
        });
    }
    private void openInterventions() {
        // Ouvrir la liste des interventions pour cet avion
        if (avion != null) {
            Intent intent = new Intent(this, AvionInterventionsActivity.class);
            intent.putExtra("avion_id", avionId);
            intent.putExtra("matricule", avion.matricule);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Erreur: Avion non chargé", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateQrCode() {
        // Ouvrir l'activité QR Code
        if (avion != null) {
            Intent intent = new Intent(this, QrCodeActivity.class);
            intent.putExtra("avion_id", avionId);
            intent.putExtra("matricule", avion.matricule);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Erreur: Avion non chargé", Toast.LENGTH_SHORT).show();
        }
    }



    private void editAvion() {
        // Ouvrir l'activité de modification
        Intent intent = new Intent(this, EditAvionActivity.class);
        intent.putExtra("avion_id", avionId);
        startActivity(intent);
    }

    private void deleteAvion() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer cet avion ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    performDelete();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void performDelete() {
        db.collection("Avions")
                .document(avionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Avion supprimé avec succès",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les données si on revient de l'édition
        if (avionId != null) {
            loadAvionDetails();
        }
    }
}