package com.example.air_maint_pro.gestion_avion;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.air_maint_pro.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditAvionActivity extends AppCompatActivity {

    private EditText etImmatriculation, etModele, etType, etCompagnie,
            etEtat, etHeuresVol, etDescription;
    private Button btnUpdate, btnCancel;

    private FirebaseFirestore db;
    private String avionId;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_avion); // Utilise le même layout

        // Initialisation
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        // Récupérer l'ID de l'avion
        avionId = getIntent().getStringExtra("avion_id");
        if (avionId == null) {
            Toast.makeText(this, "Erreur: Aucun avion sélectionné", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        initViews();

        // Charger les données de l'avion
        loadAvionData();

        // Configurer les écouteurs
        setupListeners();
    }

    private void initViews() {
        etImmatriculation = findViewById(R.id.etImmatriculation);
        etModele = findViewById(R.id.etModele);
        etType = findViewById(R.id.etType);
        etCompagnie = findViewById(R.id.etCompagnie);
        etEtat = findViewById(R.id.etEtat);
        etHeuresVol = findViewById(R.id.etHeuresVol);
        etDescription = findViewById(R.id.etDescription);
        btnUpdate = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Changer le texte du bouton
        btnUpdate.setText("Mettre à jour");

        // Configurer le champ État
        setupEtatField();
    }

    private void setupEtatField() {
        etEtat.setOnClickListener(v -> showEtatDialog());
    }

    private void showEtatDialog() {
        String[] etats = {"Actif", "En maintenance", "Hors service"};

        new android.app.AlertDialog.Builder(this)
                .setTitle("Sélectionner l'état")
                .setItems(etats, (dialog, which) -> {
                    etEtat.setText(etats[which]);
                })
                .show();
    }

    private void loadAvionData() {
        db.collection("Avions")
                .document(avionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Avion avion = documentSnapshot.toObject(Avion.class);
                        if (avion != null) {
                            populateFields(avion);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateFields(Avion avion) {
        etImmatriculation.setText(avion.matricule);
        etModele.setText(avion.modele);
        etType.setText(avion.type);
        etCompagnie.setText(avion.compagnie);
        etEtat.setText(avion.etat);
        etHeuresVol.setText(String.valueOf(avion.heuresVol));
        etDescription.setText(avion.description);
    }

    private void setupListeners() {
        btnUpdate.setOnClickListener(v -> updateAvion());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void updateAvion() {
        // Récupérer les valeurs
        String matricule = etImmatriculation.getText().toString().trim().toUpperCase();
        String modele = etModele.getText().toString().trim();
        String type = etType.getText().toString().trim();
        String compagnie = etCompagnie.getText().toString().trim();
        String etat = etEtat.getText().toString().trim();
        String heuresVolStr = etHeuresVol.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(matricule)) {
            etImmatriculation.setError("L'matricule est requise");
            etImmatriculation.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(modele)) {
            etModele.setError("Le modèle est requis");
            etModele.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(type)) {
            etType.setError("Le type est requis");
            etType.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(etat)) {
            etEtat.setError("L'état est requis");
            etEtat.requestFocus();
            return;
        }

        // Convertir heures de vol
        double heuresVol = 0;
        if (!TextUtils.isEmpty(heuresVolStr)) {
            try {
                heuresVol = Double.parseDouble(heuresVolStr);
                if (heuresVol < 0) {
                    etHeuresVol.setError("Les heures de vol doivent être positives");
                    etHeuresVol.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etHeuresVol.setError("Valeur invalide");
                etHeuresVol.requestFocus();
                return;
            }
        }

        // Désactiver le bouton
        btnUpdate.setEnabled(false);
        btnUpdate.setText("Mise à jour...");

        // Préparer les données de mise à jour
        Map<String, Object> updates = new HashMap<>();
        updates.put("matricule", matricule);
        updates.put("modele", modele);
        updates.put("type", type);
        updates.put("compagnie", compagnie);
        updates.put("etat", etat);
        updates.put("heuresVol", heuresVol);
        updates.put("description", description);
        updates.put("updatedAt", new Date());

        // Mettre à jour
        db.collection("Avions")
                .document(avionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditAvionActivity.this,
                            "Avion mis à jour avec succès",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditAvionActivity.this,
                            "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    resetUpdateButton();
                });
    }

    private void resetUpdateButton() {
        btnUpdate.setEnabled(true);
        btnUpdate.setText("Mettre à jour");
    }
}