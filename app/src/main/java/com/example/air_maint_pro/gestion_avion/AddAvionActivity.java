package com.example.air_maint_pro.gestion_avion;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.air_maint_pro.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddAvionActivity extends AppCompatActivity {

    private EditText etImmatriculation, etModele, etType, etCompagnie,
            etEtat, etHeuresVol, etDescription;
    private Button btnSave, btnCancel;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_avion);

        // Initialisation
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialiser les vues
        initViews();

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
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Configurer le champ État comme cliquable
        setupEtatField();
    }

    private void setupEtatField() {
        etEtat.setOnClickListener(v -> showEtatDialog());
    }

    private void showEtatDialog() {
        String[] etats = {"Actif", "En maintenance", "Hors service"};

        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Sélectionner l'état")
                .setItems(etats, (dialog, which) -> {
                    etEtat.setText(etats[which]);
                })
                .show();
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveAvion());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveAvion() {
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
            Toast.makeText(this, "Veuillez sélectionner un état", Toast.LENGTH_SHORT).show();
            etEtat.requestFocus();
            return;
        }

        // Convertir heures de vol
        double heuresVol = 0;
        if (!TextUtils.isEmpty(heuresVolStr)) {
            try {
                heuresVol = Double.parseDouble(heuresVolStr);
            } catch (NumberFormatException e) {
                etHeuresVol.setError("Valeur invalide");
                etHeuresVol.requestFocus();
                return;
            }
        }

        // Désactiver le bouton pendant l'enregistrement
        btnSave.setEnabled(false);
        btnSave.setText("Enregistrement...");

        // Créer l'objet avion
        Map<String, Object> avionData = new HashMap<>();
        avionData.put("matricule", matricule);
        avionData.put("modele", modele);
        avionData.put("type", type);
        avionData.put("compagnie", compagnie);
        avionData.put("etat", etat);
        avionData.put("heuresVol", heuresVol);
        avionData.put("description", description);
        avionData.put("derniereRevision", null);
        avionData.put("prochaineRevision", null);
        avionData.put("createdAt", new Date());
        avionData.put("createdBy", auth.getCurrentUser().getUid());
        avionData.put("interventionCount", 0);
        avionData.put("technicienAssignId", "");
        avionData.put("technicienAssignNom", "");

        // Vérifier si l'immatriculation existe déjà
        db.collection("Avions")
                .whereEqualTo("matricule", matricule)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Immatriculation existe déjà
                        etImmatriculation.setError("Cette matricule existe déjà");
                        etImmatriculation.requestFocus();
                        resetSaveButton();
                        return;
                    }

                    // Ajouter l'avion
                    db.collection("Avions")
                            .add(avionData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AddAvionActivity.this,
                                        "Avion ajouté avec succès",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddAvionActivity.this,
                                        "Erreur: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                resetSaveButton();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Erreur de vérification: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    resetSaveButton();
                });
    }

    private void resetSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setText("Enregistrer");
    }
}