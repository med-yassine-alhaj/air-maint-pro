package com.example.air_maint_pro.gestion_vols;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.air_maint_pro.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditEquipageActivity extends AppCompatActivity {

    private TextInputEditText etNom, etPrenom, etMatricule, etEmail, etTelephone, etQualifications;
    private AutoCompleteTextView autoCompleteRole;
    private Button btnSave, btnCancel;

    private FirebaseFirestore db;
    private MembreEquipage membreEquipage;
    private boolean isEditMode = false;

    // Liste des rôles disponibles
    private String[] rolesArray = {"Pilote", "Copilote", "Hôtesse de l'air", "Steward",
            "Ingénieur de vol", "Mécanicien navigant", "Chef de cabine"};

    // Constantes pour le résultat
    public static final String EXTRA_EQUIPAGE = "membre_equipage";
    public static final String EXTRA_IS_EDIT = "is_edit";
    public static final String EXTRA_EQUIPAGE_ID = "equipage_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_equipage);

        db = FirebaseFirestore.getInstance();

        // Initialiser les vues
        initViews();
        setupListeners();

        // Vérifier si on est en mode édition
        if (getIntent().hasExtra("EQUIPAGE_ID")) {
            isEditMode = true;
            String equipageId = getIntent().getStringExtra("EQUIPAGE_ID");
            if (equipageId != null && !equipageId.isEmpty()) {
                loadEquipageData(equipageId);
            }
        }
    }

    private void initViews() {
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etMatricule = findViewById(R.id.etMatricule);
        etEmail = findViewById(R.id.etEmail);
        etTelephone = findViewById(R.id.etTelephone);
        etQualifications = findViewById(R.id.etQualifications);

        autoCompleteRole = findViewById(R.id.autoCompleteRole);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Configurer l'AutoCompleteTextView pour les rôles
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                rolesArray
        );
        autoCompleteRole.setAdapter(roleAdapter);
        autoCompleteRole.setText("Pilote", false); // Valeur par défaut

        // Configurer le comportement de l'AutoCompleteTextView
        setupAutoCompleteTextView(autoCompleteRole);
    }

    private void setupAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setKeyListener(null);

        autoCompleteTextView.setOnClickListener(v -> {
            autoCompleteTextView.showDropDown();
        });

        autoCompleteTextView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                autoCompleteTextView.showDropDown();
                return true;
            }
            return false;
        });

        autoCompleteTextView.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        autoCompleteTextView.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void setupListeners() {
        // Bouton d'enregistrement
        btnSave.setOnClickListener(v -> saveEquipage());

        // Bouton d'annulation
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void saveEquipage() {
        // Validation des données
        if (!validateForm()) {
            return;
        }

        MembreEquipage equipageToSave = isEditMode ? membreEquipage : new MembreEquipage();

        // Remplir les informations
        equipageToSave.setNom(etNom.getText().toString().trim());
        equipageToSave.setPrenom(etPrenom.getText().toString().trim());
        equipageToSave.setMatricule(etMatricule.getText().toString().trim());
        equipageToSave.setEmail(etEmail.getText().toString().trim());
        equipageToSave.setTelephone(etTelephone.getText().toString().trim());
        equipageToSave.setQualifications(etQualifications.getText().toString().trim());
        equipageToSave.setRole(autoCompleteRole.getText().toString());
        equipageToSave.setDisponible(true); // Par défaut disponible
        equipageToSave.setDateCreated(new Date());

        // Sauvegarder dans Firestore
        saveEquipageToFirestore(equipageToSave);
    }

    private void saveEquipageToFirestore(MembreEquipage equipageToSave) {
        String collectionName = "equipage";

        if (isEditMode) {
            // Mise à jour
            db.collection(collectionName)
                    .document(equipageToSave.getId())
                    .set(equipageToSave)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Membre d'équipage modifié avec succès",
                                Toast.LENGTH_SHORT).show();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_EQUIPAGE, equipageToSave);
                        resultIntent.putExtra(EXTRA_IS_EDIT, true);
                        setResult(RESULT_OK, resultIntent);

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Erreur mise à jour équipage: ", e);
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Ajout
            db.collection(collectionName)
                    .add(equipageToSave)
                    .addOnSuccessListener(documentReference -> {
                        equipageToSave.setId(documentReference.getId());

                        Toast.makeText(this, "Membre d'équipage ajouté avec succès",
                                Toast.LENGTH_SHORT).show();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_EQUIPAGE, equipageToSave);
                        resultIntent.putExtra(EXTRA_IS_EDIT, false);
                        setResult(RESULT_OK, resultIntent);

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Erreur ajout équipage: ", e);
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        // Validation du nom
        String nom = etNom.getText().toString().trim();
        if (nom.isEmpty()) {
            etNom.setError("Le nom est requis");
            isValid = false;
            errorMessage.append("• Nom requis\n");
        } else {
            etNom.setError(null);
        }

        // Validation du prénom
        String prenom = etPrenom.getText().toString().trim();
        if (prenom.isEmpty()) {
            etPrenom.setError("Le prénom est requis");
            isValid = false;
            errorMessage.append("• Prénom requis\n");
        } else {
            etPrenom.setError(null);
        }

        // Validation du matricule
        String matricule = etMatricule.getText().toString().trim();
        if (matricule.isEmpty()) {
            etMatricule.setError("Le matricule est requis");
            isValid = false;
            errorMessage.append("• Matricule requis\n");
        } else {
            etMatricule.setError(null);
        }

        // Validation de l'email
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("L'email est requis");
            isValid = false;
            errorMessage.append("• Email requis\n");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email invalide");
            isValid = false;
            errorMessage.append("• Email invalide\n");
        } else {
            etEmail.setError(null);
        }

        // Validation du rôle
        String role = autoCompleteRole.getText().toString().trim();
        if (role.isEmpty()) {
            autoCompleteRole.setError("Veuillez sélectionner un rôle");
            isValid = false;
            errorMessage.append("• Rôle requis\n");
        } else {
            autoCompleteRole.setError(null);
        }

        // Afficher toutes les erreurs en une fois
        if (!isValid && errorMessage.length() > 0) {
            showErrorDialog(errorMessage.toString());
        }

        return isValid;
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Erreurs de validation")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void loadEquipageData(String equipageId) {
        db.collection("equipage")
                .document(equipageId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        membreEquipage = documentSnapshot.toObject(MembreEquipage.class);
                        if (membreEquipage != null) {
                            membreEquipage.setId(documentSnapshot.getId());
                            populateForm(membreEquipage);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement des données",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateForm(MembreEquipage equipage) {
        etNom.setText(equipage.getNom());
        etPrenom.setText(equipage.getPrenom());
        etMatricule.setText(equipage.getMatricule());
        etEmail.setText(equipage.getEmail());
        etTelephone.setText(equipage.getTelephone());
        etQualifications.setText(equipage.getQualifications());
        autoCompleteRole.setText(equipage.getRole(), false);
    }
}