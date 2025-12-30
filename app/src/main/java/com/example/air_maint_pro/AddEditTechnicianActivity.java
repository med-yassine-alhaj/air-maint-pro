package com.example.air_maint_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddEditTechnicianActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, emailEditText,
            phoneEditText, departmentEditText, roleEditText;
    private CheckBox activeCheckBox;
    private Button saveButton, cancelButton;
    private ProgressBar progressBar;
    private TextView titleTextView;

    private FirebaseFirestore db;
    private String technicianId = null; // null = création, non-null = modification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_technician);

        // Initialiser Firebase
        db = FirebaseFirestore.getInstance();

        // Récupérer l'ID du technicien si on est en mode modification
        technicianId = getIntent().getStringExtra("TECHNICIAN_ID");

        Log.d("TECH_ACTIVITY", "Mode: " + (technicianId == null ? "Création" : "Modification ID: " + technicianId));

        initViews();
        setupListeners();

        // Si on est en mode modification, charger les données
        if (technicianId != null && !technicianId.isEmpty()) {
            loadTechnicianData();
        }
    }

    private void initViews() {
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        departmentEditText = findViewById(R.id.departmentEditText);
        roleEditText = findViewById(R.id.roleEditText);
        activeCheckBox = findViewById(R.id.activeCheckBox);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        progressBar = findViewById(R.id.progressBar);
        titleTextView = findViewById(R.id.titleTextView);

        // Si on est en mode modification, changer le titre
        if (technicianId != null && titleTextView != null) {
            titleTextView.setText("✏️ Modifier Technicien");
        }
    }

    private void setupListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTechnician();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Retour à l'écran précédent
            }
        });
    }

    private void loadTechnicianData() {
        showLoading(true);

        Log.d("TECH_ACTIVITY", "Chargement du technicien ID: " + technicianId);

        db.collection("technicians").document(technicianId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        showLoading(false);

                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("TECH_ACTIVITY", "Document trouvé: " + document.getData());

                                // Méthode 1: Convertir en objet Technician
                                Technician technician = document.toObject(Technician.class);
                                if (technician != null) {
                                    // Remplir les champs du formulaire
                                    firstNameEditText.setText(technician.getFirstName());
                                    lastNameEditText.setText(technician.getLastName());
                                    emailEditText.setText(technician.getEmail());
                                    phoneEditText.setText(technician.getPhone());
                                    departmentEditText.setText(technician.getDepartment());
                                    roleEditText.setText(technician.getRole());
                                    activeCheckBox.setChecked(technician.isActive());

                                    Toast.makeText(AddEditTechnicianActivity.this,
                                            "Données chargées", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Méthode 2: Récupérer directement les champs
                                    loadFieldsFromMap(document.getData());
                                }
                            } else {
                                Log.d("TECH_ACTIVITY", "Document non trouvé");
                                Toast.makeText(AddEditTechnicianActivity.this,
                                        "Technicien non trouvé", Toast.LENGTH_SHORT).show();
                                finish(); // Retourner à la liste
                            }
                        } else {
                            Log.e("TECH_ACTIVITY", "Erreur: " + task.getException());
                            Toast.makeText(AddEditTechnicianActivity.this,
                                    "Erreur de chargement: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadFieldsFromMap(Map<String, Object> data) {
        try {
            if (data.containsKey("firstName")) {
                firstNameEditText.setText(data.get("firstName").toString());
            }
            if (data.containsKey("lastName")) {
                lastNameEditText.setText(data.get("lastName").toString());
            }
            if (data.containsKey("email")) {
                emailEditText.setText(data.get("email").toString());
            }
            if (data.containsKey("phone")) {
                phoneEditText.setText(data.get("phone").toString());
            }
            if (data.containsKey("department")) {
                departmentEditText.setText(data.get("department").toString());
            }
            if (data.containsKey("role")) {
                roleEditText.setText(data.get("role").toString());
            }
            if (data.containsKey("isActive")) {
                boolean isActive = Boolean.TRUE.equals(data.get("isActive"));
                activeCheckBox.setChecked(isActive);
            }
        } catch (Exception e) {
            Log.e("TECH_ACTIVITY", "Erreur lors du chargement des champs: " + e.getMessage());
        }
    }

    private void saveTechnician() {
        // Récupérer toutes les valeurs
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String department = departmentEditText.getText().toString().trim();
        String role = roleEditText.getText().toString().trim();
        boolean isActive = activeCheckBox.isChecked();

        // Validation
        if (!validateForm(firstName, lastName, email, phone)) {
            return;
        }

        // Si département vide, mettre "Non spécifié"
        if (TextUtils.isEmpty(department)) {
            department = "Non spécifié";
        }

        // Si rôle vide, mettre "Technicien"
        if (TextUtils.isEmpty(role)) {
            role = "Technicien";
        }

        showLoading(true);

        // Générer ID si création
        if (technicianId == null) {
            technicianId = UUID.randomUUID().toString();
        }

        // Créer l'objet pour Firebase
        Map<String, Object> technician = new HashMap<>();
        technician.put("id", technicianId);
        technician.put("firstName", firstName);
        technician.put("lastName", lastName);
        technician.put("email", email);
        technician.put("phone", phone);
        technician.put("department", department);
        technician.put("role", role);
        technician.put("isActive", isActive);
        technician.put("createdAt", System.currentTimeMillis());

        // Log pour débogage
        Log.d("TECH_SAVE", "Sauvegarde du technicien: " + technician);
        Log.d("TECH_SAVE", "Collection: technicians, Document: " + technicianId);

        // Sauvegarder dans Firebase
        db.collection("technicians")
                .document(technicianId)
                .set(technician)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showLoading(false);
                        Log.d("TECH_SAVE", "✅ Succès: Technicien sauvegardé dans Firestore");

                        Toast.makeText(AddEditTechnicianActivity.this,
                                "✅ " + (technicianId == null ? "Créé" : "Modifié") + " avec succès!",
                                Toast.LENGTH_LONG).show();

                        // Retourner à la liste avec un code de succès
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        Log.e("TECH_SAVE", "❌ Erreur Firestore: " + e.getMessage(), e);

                        Toast.makeText(AddEditTechnicianActivity.this,
                                "❌ Erreur: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateForm(String firstName, String lastName,
                                 String email, String phone) {
        boolean isValid = true;

        // Réinitialiser les erreurs
        firstNameEditText.setError(null);
        lastNameEditText.setError(null);
        emailEditText.setError(null);
        phoneEditText.setError(null);

        // Valider prénom
        if (TextUtils.isEmpty(firstName)) {
            firstNameEditText.setError("Le prénom est requis");
            firstNameEditText.requestFocus();
            isValid = false;
        }

        // Valider nom
        if (TextUtils.isEmpty(lastName)) {
            lastNameEditText.setError("Le nom est requis");
            if (isValid) lastNameEditText.requestFocus();
            isValid = false;
        }

        // Valider email
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("L'email est requis");
            if (isValid) emailEditText.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Format d'email invalide");
            if (isValid) emailEditText.requestFocus();
            isValid = false;
        }

        // Valider téléphone
        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Le téléphone est requis");
            if (isValid) phoneEditText.requestFocus();
            isValid = false;
        } else if (phone.length() < 8) {
            phoneEditText.setError("Numéro trop court");
            if (isValid) phoneEditText.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            saveButton.setEnabled(false);
            cancelButton.setEnabled(false);
            saveButton.setText("Sauvegarde...");

            // Désactiver tous les champs
            setFormEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            saveButton.setText("Sauvegarder");

            // Réactiver tous les champs
            setFormEnabled(true);
        }
    }

    private void setFormEnabled(boolean enabled) {
        firstNameEditText.setEnabled(enabled);
        lastNameEditText.setEnabled(enabled);
        emailEditText.setEnabled(enabled);
        phoneEditText.setEnabled(enabled);
        departmentEditText.setEnabled(enabled);
        roleEditText.setEnabled(enabled);
        activeCheckBox.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TECH_ACTIVITY", "Activity détruite");
    }
}