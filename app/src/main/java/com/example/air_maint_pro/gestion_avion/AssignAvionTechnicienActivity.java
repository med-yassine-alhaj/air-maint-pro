package com.example.air_maint_pro.gestion_avion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.air_maint_pro.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignAvionTechnicienActivity extends AppCompatActivity {

    private Spinner spinnerAvions, spinnerTechniciens;
    private Button btnAssign;
    private FirebaseFirestore db;
    private List<String> avionsList = new ArrayList<>();
    private List<String> techniciensList = new ArrayList<>();
    private Map<String, String> avionsMap = new HashMap<>(); // ID -> Matricule
    private Map<String, String> techniciensMap = new HashMap<>(); // ID -> Nom
    private String avionIdFromIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_avion_technicien);

        db = FirebaseFirestore.getInstance();
        spinnerAvions = findViewById(R.id.spinnerAvions);
        spinnerTechniciens = findViewById(R.id.spinnerTechniciens);
        btnAssign = findViewById(R.id.btnAssign);

        // Récupérer l'ID de l'avion si passé en intent
        if (getIntent() != null && getIntent().hasExtra("avion_id")) {
            avionIdFromIntent = getIntent().getStringExtra("avion_id");
        }

        loadAvions();
        loadTechniciens();

        btnAssign.setOnClickListener(v -> assignerAvion());
    }

    private void loadAvions() {
        db.collection("Avions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    avionsList.clear();
                    avionsList.add("Sélectionner un avion");

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String avionId = document.getId();
                        String matricule = document.getString("matricule");
                        String modele = document.getString("modele");
                        String displayText = matricule + " - " + modele;

                        avionsList.add(displayText);
                        avionsMap.put(displayText, avionId);

                        // Si on a un avion spécifique, le sélectionner
                        if (avionIdFromIntent != null && avionId.equals(avionIdFromIntent)) {
                            int position = avionsList.indexOf(displayText);
                            if (position != -1) {
                                spinnerAvions.setSelection(position);
                                spinnerAvions.setEnabled(false); // Empêcher de changer si venant de détails
                            }
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, avionsList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerAvions.setAdapter(adapter);
                });
    }

    private void loadTechniciens() {
        db.collection("Users")
                .whereEqualTo("role", "technicien")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    techniciensList.clear();
                    techniciensList.add("Sélectionner un technicien");

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String technicienId = document.getId();
                        String nom = document.getString("nom");
                        String prenom = document.getString("prenom");
                        String displayText = prenom + " " + nom;

                        techniciensList.add(displayText);
                        techniciensMap.put(displayText, technicienId);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, techniciensList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTechniciens.setAdapter(adapter);
                });
    }

    private void assignerAvion() {
        String selectedAvion = spinnerAvions.getSelectedItem().toString();
        String selectedTechnicien = spinnerTechniciens.getSelectedItem().toString();

        if (selectedAvion.equals("Sélectionner un avion") ||
                selectedTechnicien.equals("Sélectionner un technicien")) {
            Toast.makeText(this, "Veuillez sélectionner un avion et un technicien",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String avionId = avionIdFromIntent != null ? avionIdFromIntent : avionsMap.get(selectedAvion);
        String technicienId = techniciensMap.get(selectedTechnicien);
        String technicienNom = selectedTechnicien;

        // Mettre à jour l'avion avec le technicien assigné
        Map<String, Object> updates = new HashMap<>();
        updates.put("technicienAssignId", technicienId);
        updates.put("technicienAssignNom", technicienNom);

        db.collection("Avions")
                .document(avionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Avion assigné avec succès", Toast.LENGTH_SHORT).show();

                    // Retourner à l'activité précédente avec succès
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}