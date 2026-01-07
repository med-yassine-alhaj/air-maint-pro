package com.example.air_maint_pro;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditTechnicienActivity extends AppCompatActivity {

    private TextView tvTechId, tvEmail;
    private EditText etNom, etPrenom, etNewPassword, etAge;
    private Spinner spDepartement;
    private Button btnUpdate, btnCancel;

    private FirebaseFirestore db;
    private FirebaseAuth secondaryAuth;
    private String technicienId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_technicien);

        // Initialisation des vues
        tvTechId = findViewById(R.id.tvTechId);
        tvEmail = findViewById(R.id.tvEmail);
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etNewPassword = findViewById(R.id.etNewPassword);
        etAge = findViewById(R.id.etAge);
        spDepartement = findViewById(R.id.spDepartement);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        db = FirebaseFirestore.getInstance();

        // Initialiser l'auth secondaire - version simplifiée
        try {
            // Vérifie si l'app "Secondary" existe déjà
            FirebaseApp secondaryApp = FirebaseApp.getInstance("Secondary");
            secondaryAuth = FirebaseAuth.getInstance(secondaryApp);
        } catch (IllegalStateException e) {
            // Si elle n'existe pas, utilise l'instance par défaut
            secondaryAuth = FirebaseAuth.getInstance();
        }

        // Configurer le Spinner pour les départements
        setupDepartementSpinner();

        // Récupérer les données du technicien
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            technicienId = extras.getString("technicienId");
            String nom = extras.getString("nom");
            String prenom = extras.getString("prenom");
            String email = extras.getString("email");
            String age = String.valueOf(extras.getInt("age"));
            String departement = extras.getString("departement");

            // Afficher les données
            tvTechId.setText(technicienId != null ? technicienId : "");
            tvEmail.setText(email != null ? email : "");
            etNom.setText(nom != null ? nom : "");
            etPrenom.setText(prenom != null ? prenom : "");
            if (age != null && !age.equals("0")) {
                etAge.setText(age);
            }

            // Positionner le spinner sur le département
            if (departement != null) {
                setSpinnerToValue(departement);
            }
        } else {
            Toast.makeText(this, "Aucune donnée reçue", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnUpdate.setOnClickListener(v -> updateTechnicien());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupDepartementSpinner() {
        String[] departements = {
                "Sélectionner un département",
                "Maintenance Électrique",
                "Climatisation",
                "Systèmes de Ventilation",
                "Contrôle Qualité",
                "Support Technique",
                "Administration"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                departements
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDepartement.setAdapter(adapter);
    }

    private void setSpinnerToValue(String value) {
        if (value == null || value.isEmpty()) return;

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spDepartement.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spDepartement.setSelection(position);
            }
        }
    }

    private void updateTechnicien() {
        // Récupération des données
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String departement = spDepartement.getSelectedItem().toString();

        // Validation des champs
        if (nom.isEmpty() || prenom.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(this, "Les champs nom, prénom et âge sont obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 18 || age > 65) {
                etAge.setError("Âge doit être entre 18 et 65 ans");
                return;
            }
        } catch (NumberFormatException e) {
            etAge.setError("Âge invalide");
            return;
        }

        if (departement.equals("Sélectionner un département")) {
            Toast.makeText(this, "Veuillez sélectionner un département", Toast.LENGTH_SHORT).show();
            return;
        }

        if (technicienId == null || technicienId.isEmpty()) {
            Toast.makeText(this, "ID du technicien manquant", Toast.LENGTH_SHORT).show();
            return;
        }

        // Préparer les données de mise à jour
        Map<String, Object> updates = new HashMap<>();
        updates.put("nom", nom);
        updates.put("prenom", prenom);
        updates.put("age", age);
        updates.put("departement", departement);
        updates.put("fullName", nom + " " + prenom);

        // Mise à jour dans Firestore
        db.collection("Users")
                .document(technicienId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Gérer le changement de mot de passe si fourni
                    if (!newPassword.isEmpty()) {
                        handlePasswordChange(newPassword);
                    } else {
                        showSuccessAndFinish("Technicien mis à jour avec succès");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la mise à jour: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handlePasswordChange(String newPassword) {
        if (newPassword.length() < 6) {
            Toast.makeText(this,
                    "Le mot de passe doit contenir au moins 6 caractères. " +
                            "Les informations ont été mises à jour, mais le mot de passe reste inchangé.",
                    Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
            return;
        }

        // Dans une application réelle, vous devriez utiliser Firebase Admin SDK côté serveur
        // ou envoyer un email de réinitialisation
        Toast.makeText(this,
                "Technicien mis à jour. " +
                        "Note: Pour des raisons de sécurité, le mot de passe ne peut pas être modifié directement. " +
                        "Utilisez la fonction 'Mot de passe oublié' depuis la connexion.",
                Toast.LENGTH_LONG).show();

        setResult(RESULT_OK);
        finish();
    }

    private void showSuccessAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}