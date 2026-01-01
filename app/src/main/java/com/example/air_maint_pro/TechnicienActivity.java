package com.example.air_maint_pro;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TechnicienActivity extends AppCompatActivity {

    private EditText etNom, etPrenom, etEmail, etPassword, etAge;
    private Spinner spDepartement;
    private Button btnCreate;

    private FirebaseFirestore db;
    private FirebaseAuth secondaryAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_technicien);

        // Initialisation des vues
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etAge = findViewById(R.id.etAge);
        spDepartement = findViewById(R.id.spDepartement);
        btnCreate = findViewById(R.id.btnCreate);

        db = FirebaseFirestore.getInstance();

        // Initialiser l'auth secondaire
        FirebaseApp secondaryApp;
        try {
            secondaryApp = FirebaseApp.getInstance("Secondary");
        } catch (IllegalStateException e) {
            FirebaseApp.initializeApp(this, FirebaseApp.getInstance().getOptions(), "Secondary");
            secondaryApp = FirebaseApp.getInstance("Secondary");
        }
        secondaryAuth = FirebaseAuth.getInstance(secondaryApp);

        // Configurer le Spinner pour les départements
        setupDepartementSpinner();

        btnCreate.setOnClickListener(v -> createTechnicien());
    }

    private void setupDepartementSpinner() {
        // Liste des départements
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

    private void createTechnicien() {
        // Récupération des données
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String departement = spDepartement.getSelectedItem().toString();

        // Validation des champs
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() ||
                password.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(this, "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Email invalide");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 caractères");
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

        // Création du compte Auth
        secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        Toast.makeText(this, "Erreur lors de la création du compte", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = user.getUid();

                    // Création du document Firestore
                    Map<String, Object> data = new HashMap<>();
                    data.put("nom", nom);
                    data.put("prenom", prenom);
                    data.put("email", email);
                    data.put("age", age);
                    data.put("departement", departement);
                    data.put("role", "technicien");
                    data.put("createdAt", System.currentTimeMillis());
                    data.put("fullName", nom + " " + prenom); // Champ calculé pour faciliter la recherche

                    db.collection("Users")
                            .document(uid)
                            .set(data)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Technicien créé avec succès", Toast.LENGTH_SHORT).show();

                                // Nettoyer le formulaire
                                clearForm();

                                // Déconnecter l'auth secondaire
                                secondaryAuth.signOut();

                                // Retour à l'admin
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erreur lors de l'enregistrement des données", Toast.LENGTH_SHORT).show();
                                // Rollback: supprimer le compte Auth
                                user.delete();
                            });
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage().contains("email address is already in use")) {
                        etEmail.setError("Cet email est déjà utilisé");
                    } else {
                        Toast.makeText(this, "Erreur de création du compte", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void clearForm() {
        etNom.setText("");
        etPrenom.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etAge.setText("");
        spDepartement.setSelection(0);
    }
}