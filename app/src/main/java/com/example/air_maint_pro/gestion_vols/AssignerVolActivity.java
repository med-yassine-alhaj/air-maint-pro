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

public class AssignerVolActivity extends AppCompatActivity
        implements VolsDisponiblesAdapter.OnVolClickListener {

    private TextView tvEquipeInfo, tvAucunVol;
    private RecyclerView recyclerViewVols;
    private Button btnRetour;

    private FirebaseFirestore db;
    private String equipeId;
    private String equipeNom;
    private List<Vol> volsDisponibles = new ArrayList<>();
    private VolsDisponiblesAdapter volsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigner_vol);

        db = FirebaseFirestore.getInstance();

        // Récupérer les infos de l'équipe
        equipeId = getIntent().getStringExtra("EQUIPE_ID");
        equipeNom = getIntent().getStringExtra("EQUIPE_NOM");

        if (equipeId == null || equipeNom == null) {
            Toast.makeText(this, "Équipe non spécifiée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadVolsDisponibles();
    }

    private void initViews() {
        tvEquipeInfo = findViewById(R.id.tvEquipeInfo);
        tvAucunVol = findViewById(R.id.tvAucunVol);
        recyclerViewVols = findViewById(R.id.recyclerViewVols);
        btnRetour = findViewById(R.id.btnRetour);

        tvEquipeInfo.setText("Assigner un vol à: " + equipeNom);

        recyclerViewVols.setLayoutManager(new LinearLayoutManager(this));
        volsAdapter = new VolsDisponiblesAdapter(volsDisponibles, this);
        recyclerViewVols.setAdapter(volsAdapter);
    }

    private void setupListeners() {
        btnRetour.setOnClickListener(v -> finish());
    }

    private void loadVolsDisponibles() {
        // Charger les vols qui :
        // 1. N'ont pas d'équipe assignée
        // 2. Sont à venir (date de départ dans le futur)
        // 3. Sont planifiés ou retardés

        db.collection("vols")
                .whereGreaterThan("date_depart", new Date()) // Vols futurs
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        volsDisponibles.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Vol vol = document.toObject(Vol.class);
                            vol.setId(document.getId());

                            // Filtrer les vols sans équipe assignée
                            if (!vol.aUneEquipeAssignée() &&
                                    ("Planifié".equals(vol.getStatut()) ||
                                            "Retardé".equals(vol.getStatut()))) {
                                volsDisponibles.add(vol);
                            }
                        }

                        volsAdapter.notifyDataSetChanged();

                        // Afficher message si aucun vol disponible
                        if (volsDisponibles.isEmpty()) {
                            tvAucunVol.setVisibility(android.view.View.VISIBLE);
                            recyclerViewVols.setVisibility(android.view.View.GONE);
                        } else {
                            tvAucunVol.setVisibility(android.view.View.GONE);
                            recyclerViewVols.setVisibility(android.view.View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this,
                                "Erreur de chargement des vols",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onVolClick(Vol vol) {
        // Afficher la confirmation d'assignation
        new AlertDialog.Builder(this)
                .setTitle("Confirmer l'assignation")
                .setMessage("Voulez-vous assigner l'équipe " + equipeNom +
                        " au vol " + vol.getNumeroVol() + " ?\n\n" +
                        "Départ: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)
                        .format(vol.getDateDepart()) + "\n" +
                        "Itinéraire: " + vol.getVilleDepart() + " → " + vol.getVilleArrivee())
                .setPositiveButton("Assigner", (dialog, which) -> {
                    assignerEquipeAuVol(vol);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void assignerEquipeAuVol(Vol vol) {
        // 1. Charger l'équipe pour avoir ses informations
        db.collection("equipes")
                .document(equipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Equipe equipe = documentSnapshot.toObject(Equipe.class);
                        if (equipe != null) {
                            equipe.setId(documentSnapshot.getId());

                            // 2. Mettre à jour le vol avec l'équipe
                            vol.assignerEquipe(equipe);

                            db.collection("vols")
                                    .document(vol.getId())
                                    .set(vol)
                                    .addOnSuccessListener(aVoid -> {
                                        // 3. Mettre à jour l'équipe
                                        equipe.assignerVol(vol.getId(), vol.getNumeroVol());

                                        db.collection("equipes")
                                                .document(equipe.getId())
                                                .set(equipe)
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(this,
                                                            "Équipe assignée au vol avec succès",
                                                            Toast.LENGTH_SHORT).show();

                                                    // Retourner au détail de l'équipe
                                                    Intent resultIntent = new Intent();
                                                    resultIntent.putExtra("EQUIPE_ID", equipeId);
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
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Erreur chargement équipe: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}