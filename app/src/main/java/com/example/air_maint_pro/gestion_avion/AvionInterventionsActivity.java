package com.example.air_maint_pro.gestion_avion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;
import com.example.air_maint_pro.intervention_management.Intervention;
import com.example.air_maint_pro.intervention_management.InterventionAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AvionInterventionsActivity extends AppCompatActivity {

    private TextView tvAvionTitle, tvInterventionCount, tvNoInterventions;
    private Button btnAddIntervention;
    private RecyclerView rvInterventions;

    private FirebaseFirestore db;
    private String avionId;
    private String matricule;
    private InterventionAdapter adapter;
    private List<Intervention> interventionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avion_interventions);

        // Initialisation
        db = FirebaseFirestore.getInstance();

        // Récupérer les données
        Intent intent = getIntent();
        avionId = intent.getStringExtra("avion_id");
        matricule = intent.getStringExtra("matricule");

        if (avionId == null || matricule == null) {
            Toast.makeText(this, "Erreur: Données manquantes", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        initViews();

        // Configurer le RecyclerView
        setupRecyclerView();

        // Charger les interventions
        loadInterventions();

        // Configurer les écouteurs
        setupListeners();
    }

    private void initViews() {
        tvAvionTitle = findViewById(R.id.tvAvionTitle);
        tvInterventionCount = findViewById(R.id.tvInterventionCount);
        tvNoInterventions = findViewById(R.id.tvNoInterventions);
        btnAddIntervention = findViewById(R.id.btnAddIntervention);
        rvInterventions = findViewById(R.id.rvInterventions);

        tvAvionTitle.setText("Interventions - " + matricule);
    }

    private void setupRecyclerView() {
        adapter = new InterventionAdapter(interventionList);
        adapter.setOnItemClickListener(intervention -> {
            // Ouvrir les détails de l'intervention
            openInterventionDetails(intervention);
        });

        rvInterventions.setLayoutManager(new LinearLayoutManager(this));
        rvInterventions.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAddIntervention.setOnClickListener(v -> {
            addNewIntervention();
        });
    }

    private void loadInterventions() {
        db.collection("interventions")
                .whereEqualTo("avionId", avionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        interventionList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Intervention intervention = document.toObject(Intervention.class);
                            intervention.setId(document.getId());
                            interventionList.add(intervention);
                        }

                        updateUI();
                        adapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(this,
                                "Erreur de chargement des interventions",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        int count = interventionList.size();
        tvInterventionCount.setText(count + " intervention" + (count > 1 ? "s" : ""));

        if (count == 0) {
            tvNoInterventions.setVisibility(View.VISIBLE);
            rvInterventions.setVisibility(View.GONE);
        } else {
            tvNoInterventions.setVisibility(View.GONE);
            rvInterventions.setVisibility(View.VISIBLE);
        }
    }

    private void addNewIntervention() {
        // Ouvrir l'activité d'ajout d'intervention depuis le module intervention_management
        Toast.makeText(this,
                "Ajouter une nouvelle intervention pour " + matricule,
                Toast.LENGTH_SHORT).show();

        // Note: Vous devrez peut-être adapter cette partie selon comment
        // votre module intervention_management fonctionne
    }

    private void openInterventionDetails(Intervention intervention) {
        // Ouvrir les détails de l'intervention
        Toast.makeText(this,
                "Détails de l'intervention: " + intervention.getTypeIntervention(),
                Toast.LENGTH_SHORT).show();

        // Note: Utilisez votre fragment InterventionDetailFragment ici
        // InterventionDetailFragment detailFragment = InterventionDetailFragment.newInstance(intervention.getId());
        // ...
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les interventions quand on revient
        loadInterventions();
    }
}