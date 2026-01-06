package com.example.air_maint_pro.gestion_vols;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailEquipeActivity extends AppCompatActivity {

    private TextView tvNomEquipe, tvCodeEquipe, tvDescription, tvStatut,
            tvDateCreation, tvMembresCount, tvComposition, tvVolAssigné;
    private Button btnEdit, btnAssignerVol, btnLibererVol, btnRetour;
    private LinearLayout layoutVolInfo;
    private TextView tvRoleStats;
    private RecyclerView recyclerViewMembres;

    private FirebaseFirestore db;
    private Equipe equipe;
    private List<MembreEquipe> membresEquipe = new ArrayList<>();
    private MembreEquipeAdapter membresAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_equipe);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();

        // Récupérer l'ID de l'équipe
        String equipeId = getIntent().getStringExtra("EQUIPE_ID");
        if (equipeId != null && !equipeId.isEmpty()) {
            loadEquipeDetails(equipeId);
        } else {
            Toast.makeText(this, "Équipe non trouvée", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvNomEquipe = findViewById(R.id.tvNomEquipe);
        tvCodeEquipe = findViewById(R.id.tvCodeEquipe);
        tvDescription = findViewById(R.id.tvDescription);
        tvStatut = findViewById(R.id.tvStatut);
        tvDateCreation = findViewById(R.id.tvDateCreation);
        tvMembresCount = findViewById(R.id.tvMembresCount);
        tvComposition = findViewById(R.id.tvComposition);
        tvVolAssigné = findViewById(R.id.tvVolAssigné);
        tvRoleStats = findViewById(R.id.tvRoleStats);

        btnEdit = findViewById(R.id.btnEdit);
        btnAssignerVol = findViewById(R.id.btnAssignerVol);
        btnLibererVol = findViewById(R.id.btnLibererVol);
        btnRetour = findViewById(R.id.btnRetour);
        layoutVolInfo = findViewById(R.id.layoutVolInfo);

        recyclerViewMembres = findViewById(R.id.recyclerViewMembres);
        recyclerViewMembres.setLayoutManager(new LinearLayoutManager(this));
        membresAdapter = new MembreEquipeAdapter(membresEquipe);
        recyclerViewMembres.setAdapter(membresAdapter);
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditEquipeActivity.class);
            intent.putExtra("EQUIPE_ID", equipe.getId());
            startActivity(intent);
            finish();
        });

        btnAssignerVol.setOnClickListener(v -> {
            // Ouvrir l'activité d'affectation de vol
            Intent intent = new Intent(this, AssignerVolActivity.class);
            intent.putExtra("EQUIPE_ID", equipe.getId());
            intent.putExtra("EQUIPE_NOM", equipe.getNomComplet());
            startActivityForResult(intent, 100);
        });

        btnLibererVol.setOnClickListener(v -> {
            libererEquipeDuVol();
        });

        btnRetour.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadEquipeDetails(String equipeId) {
        db.collection("equipes")
                .document(equipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        equipe = documentSnapshot.toObject(Equipe.class);
                        if (equipe != null) {
                            equipe.setId(documentSnapshot.getId());
                            displayEquipeDetails();
                        }
                    } else {
                        Toast.makeText(this, "Équipe non trouvée", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayEquipeDetails() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);

        tvNomEquipe.setText(equipe.getNom());
        tvCodeEquipe.setText("Code: " + equipe.getCode());
        tvDescription.setText(equipe.getDescription() != null ?
                equipe.getDescription() : "Aucune description");

        // Statut avec couleur
        tvStatut.setText(equipe.getStatut());
        switch (equipe.getStatut()) {
            case "Disponible":
                tvStatut.setTextColor(getResources().getColor(R.color.status_done));
                tvStatut.setBackgroundResource(R.drawable.bg_status_available);
                break;
            case "En mission":
                tvStatut.setTextColor(getResources().getColor(R.color.orange));
                tvStatut.setBackgroundResource(R.drawable.bg_status_mission);
                break;
            case "En repos":
                tvStatut.setTextColor(getResources().getColor(R.color.blue_dark));
                tvStatut.setBackgroundResource(R.drawable.bg_status_rest);
                break;
            case "Indisponible":
                tvStatut.setTextColor(getResources().getColor(R.color.red));
                tvStatut.setBackgroundResource(R.drawable.bg_status_unavailable);
                break;
        }

        // Dates
        if (equipe.getDateCreation() != null) {
            tvDateCreation.setText("Créée le: " + sdf.format(equipe.getDateCreation()));
        }

        // Membres
        tvMembresCount.setText(equipe.getNombreMembres() + " membre(s)");

        // Composition
        if (equipe.aCompositionMinimale()) {
            tvComposition.setText("✓ Composition complète");
            tvComposition.setTextColor(getResources().getColor(R.color.status_done));
        } else {
            tvComposition.setText("⚠ Composition incomplète");
            tvComposition.setTextColor(getResources().getColor(R.color.orange));
        }

        // Vol assigné
        if (equipe.aUnVolAssigné()) {
            layoutVolInfo.setVisibility(View.VISIBLE);
            tvVolAssigné.setText("Assignée au vol: " + equipe.getVolAssignéNumero());

            // Afficher les boutons appropriés
            btnAssignerVol.setVisibility(View.GONE);
            btnLibererVol.setVisibility(View.VISIBLE);
        } else {
            layoutVolInfo.setVisibility(View.GONE);
            btnAssignerVol.setVisibility(View.VISIBLE);
            btnLibererVol.setVisibility(View.GONE);
        }

        // Charger les détails des membres
        if (equipe.getMembres() != null && !equipe.getMembres().isEmpty()) {
            membresEquipe.clear();
            membresEquipe.addAll(equipe.getMembres());
            membresAdapter.notifyDataSetChanged();

            // Afficher les statistiques des rôles
            displayRoleStatistics();
        }
    }

    private void displayRoleStatistics() {
        // Cette méthode peut être étendue pour afficher des statistiques détaillées
        TextView tvRoleStats = findViewById(R.id.tvRoleStats);
        if (tvRoleStats != null) {
            StringBuilder stats = new StringBuilder("Répartition: ");
            stats.append("Pilotes: ").append(equipe.getPilotes().size()).append(", ");
            stats.append("Copilotes: ").append(equipe.getCopilotes().size()).append(", ");
            stats.append("Cabine: ").append(equipe.getPersonnelCabine().size()).append(", ");
            stats.append("Techniciens: ").append(equipe.getTechniciens().size());
            tvRoleStats.setText(stats.toString());
        }
    }

    private void libererEquipeDuVol() {
        // Mettre à jour l'équipe dans Firestore
        equipe.libererVol();

        db.collection("equipes")
                .document(equipe.getId())
                .set(equipe)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Équipe libérée du vol avec succès",
                            Toast.LENGTH_SHORT).show();

                    // Recharger les détails
                    displayEquipeDetails();

                    // Mettre à jour le vol si nécessaire (nous le ferons plus tard)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Recharger les détails si un vol a été assigné
            if (equipe != null && equipe.getId() != null) {
                loadEquipeDetails(equipe.getId());
            }
        }
    }
}