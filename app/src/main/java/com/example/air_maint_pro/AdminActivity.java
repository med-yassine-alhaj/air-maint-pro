package com.example.air_maint_pro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnLogout, btnCreateTechnicien;
    private RecyclerView rvTechnicien;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private List<Technicien> technicienList = new ArrayList<>();
    private TechnicienAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        btnCreateTechnicien = findViewById(R.id.btnCreateTechnicien);
        rvTechnicien = findViewById(R.id.rvTechnicien);

        // Vérification connexion
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        // Afficher le nom d'utilisateur
        String fullName = getIntent().getStringExtra("fullName");
        if (fullName != null && !fullName.isEmpty()) {
            tvWelcome.setText("Bienvenue " + fullName);
        } else {
            // Fallback: charger depuis Firestore
            loadSupervisorInfo();
        }

        // RecyclerView
        rvTechnicien.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TechnicienAdapter(technicienList, this::showTechnicienOptions);
        rvTechnicien.setAdapter(adapter);

        loadTechnicien();

        // Bouton créer Technicien
        btnCreateTechnicien.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, TechnicienActivity.class));
        });

        // Déconnexion
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            redirectToLogin();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rafraîchir la liste quand on revient
        loadTechnicien();
    }

    private void loadSupervisorInfo() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("Users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        redirectToLogin();
                        return;
                    }

                    String role = doc.getString("role");
                    String nom = doc.getString("nom");
                    String prenom = doc.getString("prenom");

                    if (!"supervisor".equals(role)) {
                        redirectToLogin();
                        return;
                    }

                    if (nom != null && prenom != null) {
                        tvWelcome.setText("Bienvenue " + nom + " " + prenom);
                    } else if (nom != null) {
                        tvWelcome.setText("Bienvenue " + nom);
                    } else {
                        tvWelcome.setText("Bienvenue Supervisor");
                    }
                })
                .addOnFailureListener(e -> {
                    tvWelcome.setText("Bienvenue");
                });
    }

    private void loadTechnicien() {
        db.collection("Users")
                .whereEqualTo("role", "technicien")
                .get()
                .addOnSuccessListener(query -> {
                    technicienList.clear();
                    for (DocumentSnapshot d : query) {
                        Technicien e = d.toObject(Technicien.class);
                        if (e != null) {
                            e.id = d.getId();
                            technicienList.add(e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement des techniciens", Toast.LENGTH_SHORT).show();
                });
    }

    private void showTechnicienOptions(Technicien e) {
        String[] options = {"Voir détails", "Modifier", "Supprimer"};

        new AlertDialog.Builder(this)
                .setTitle(e.getFullName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showTechnicienDetails(e);
                            break;
                        case 1:
                            updateTechnicien(e);
                            break;
                        case 2:
                            deleteTechnicien(e);
                            break;
                    }
                })
                .show();
    }

    private void showTechnicienDetails(Technicien e) {
        String details = "Nom complet: " + e.getFullName() + "\n" +
                "Email: " + e.email + "\n" +
                "Âge: " + e.age + " ans\n" +
                "Département: " + e.departement + "\n" +
                "Rôle: " + e.role;

        new AlertDialog.Builder(this)
                .setTitle("Détails du Technicien")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateTechnicien(Technicien e) {
        // Pour simplifier, on va juste modifier le nom pour l'instant
        // Vous pourriez créer une nouvelle activité pour l'édition complète

        new AlertDialog.Builder(this)
                .setTitle("Modifier le technicien")
                .setMessage("Fonctionnalité d'édition complète à implémenter")
                .setPositiveButton("OK", null)
                .show();
    }

    private void deleteTechnicien(Technicien e) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation de suppression")
                .setMessage("Voulez-vous vraiment supprimer " + e.getFullName() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    db.collection("Users")
                            .document(e.id)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Technicien supprimé", Toast.LENGTH_SHORT).show();
                                loadTechnicien();
                            })
                            .addOnFailureListener(err -> {
                                Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}