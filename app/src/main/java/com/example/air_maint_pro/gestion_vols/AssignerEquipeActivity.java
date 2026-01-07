package com.example.air_maint_pro.gestion_vols;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignerEquipeActivity extends AppCompatActivity
        implements EquipesDisponiblesAdapter.OnEquipeClickListener {

    private TextView tvVolInfo, tvAucuneEquipe;
    private RecyclerView recyclerViewEquipes;
    private Button btnRetour;

    private FirebaseFirestore db;
    private Vol vol;
    private List<Equipe> equipesDisponibles = new ArrayList<>();
    private EquipesDisponiblesAdapter equipesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigner_equipe);

        db = FirebaseFirestore.getInstance();

        // Récupérer le vol
        String volId = getIntent().getStringExtra("VOL_ID");

        if (volId == null) {
            Toast.makeText(this, "Vol non spécifié", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadVolDetails(volId);
    }

    private void initViews() {
        tvVolInfo = findViewById(R.id.tvVolInfo);
        tvAucuneEquipe = findViewById(R.id.tvAucuneEquipe);
        recyclerViewEquipes = findViewById(R.id.recyclerViewEquipes);
        btnRetour = findViewById(R.id.btnRetour);

        recyclerViewEquipes.setLayoutManager(new LinearLayoutManager(this));
        equipesAdapter = new EquipesDisponiblesAdapter(equipesDisponibles, this);
        recyclerViewEquipes.setAdapter(equipesAdapter);
    }

    private void setupListeners() {
        btnRetour.setOnClickListener(v -> finish());
    }

    private void loadVolDetails(String volId) {
        db.collection("vols")
                .document(volId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        vol = documentSnapshot.toObject(Vol.class);
                        vol.setId(documentSnapshot.getId());

                        // Afficher les infos du vol
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
                        String info = "Vol " + vol.getNumeroVol() + "\n" +
                                "Départ: " + sdf.format(vol.getDateDepart()) + "\n" +
                                "Itinéraire: " + vol.getVilleDepart() + " → " + vol.getVilleArrivee();
                        tvVolInfo.setText(info);

                        // Charger les équipes disponibles
                        loadEquipesDisponibles();
                    } else {
                        Toast.makeText(this, "Vol non trouvé", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadEquipesDisponibles() {
        // Charger les équipes qui :
        // 1. Sont disponibles (statut = "Disponible")
        // 2. Ont une composition minimale
        // 3. Ne sont pas déjà assignées à un vol

        db.collection("equipes")
                .whereEqualTo("statut", "Disponible")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        equipesDisponibles.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Equipe equipe = document.toObject(Equipe.class);
                            equipe.setId(document.getId());

                            // Filtrer les équipes avec composition minimale
                            if (equipe.aCompositionMinimale() && !equipe.aUnVolAssigné()) {
                                equipesDisponibles.add(equipe);
                            }
                        }

                        equipesAdapter.notifyDataSetChanged();

                        // Afficher message si aucune équipe disponible
                        if (equipesDisponibles.isEmpty()) {
                            tvAucuneEquipe.setVisibility(android.view.View.VISIBLE);
                            recyclerViewEquipes.setVisibility(android.view.View.GONE);
                        } else {
                            tvAucuneEquipe.setVisibility(android.view.View.GONE);
                            recyclerViewEquipes.setVisibility(android.view.View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this,
                                "Erreur de chargement des équipes",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onEquipeClick(Equipe equipe) {
        // Vérifier si le vol a déjà une équipe
        if (vol.aUneEquipeAssignée()) {
            new AlertDialog.Builder(this)
                    .setTitle("Remplacer l'équipe")
                    .setMessage("Ce vol a déjà l'équipe " + vol.getEquipeNom() +
                            " assignée.\n\nVoulez-vous la remplacer par " +
                            equipe.getNomComplet() + " ?")
                    .setPositiveButton("Remplacer", (dialog, which) -> {
                        assignerEquipeAuVol(equipe, true);
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmer l'assignation")
                    .setMessage("Voulez-vous assigner l'équipe " +
                            equipe.getNomComplet() + " à ce vol ?\n\n" +
                            "Membres: " + equipe.getNombreMembres() + " personne(s)")
                    .setPositiveButton("Assigner", (dialog, which) -> {
                        assignerEquipeAuVol(equipe, false);
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        }
    }

    private void assignerEquipeAuVol(Equipe equipe, boolean estRemplacement) {
        if (estRemplacement) {
            // Libérer l'ancienne équipe
            libererAncienneEquipe();
        }

        // 1. Mettre à jour le vol avec l'équipe
        vol.assignerEquipe(equipe);

        db.collection("vols")
                .document(vol.getId())
                .set(vol)
                .addOnSuccessListener(aVoid -> {
                    // 2. Mettre à jour l'équipe
                    equipe.assignerVol(vol.getId(), vol.getNumeroVol());

                    db.collection("equipes")
                            .document(equipe.getId())
                            .set(equipe)
                            .addOnSuccessListener(aVoid1 -> {
                                String message = estRemplacement ?
                                        "Équipe remplacée avec succès" :
                                        "Équipe assignée au vol avec succès";

                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                                // Retourner à la liste des vols
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("VOL_ID", vol.getId());
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this,
                                        "Erreur mise à jour équipe: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Erreur mise à jour vol: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void libererAncienneEquipe() {
        if (vol.getEquipeId() != null) {
            db.collection("equipes")
                    .document(vol.getEquipeId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Equipe ancienneEquipe = documentSnapshot.toObject(Equipe.class);
                            if (ancienneEquipe != null) {
                                ancienneEquipe.setId(documentSnapshot.getId());
                                ancienneEquipe.libererVol();

                                db.collection("equipes")
                                        .document(ancienneEquipe.getId())
                                        .set(ancienneEquipe);
                            }
                        }
                    });
        }
    }
}