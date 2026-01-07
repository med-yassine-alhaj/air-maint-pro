package com.example.air_maint_pro.gestion_avion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AvionListFragment extends Fragment {

    private RecyclerView rvAvions;
    private Button btnAddAvion, btnAssignTechnicien;
    private AvionAdapter adapter;
    private List<Avion> avionList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isTechnicienView = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avion_list, container, false);

        // Initialisation
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Vérifier si c'est la vue technicien
        if (getArguments() != null && getArguments().containsKey("technicien_id")) {
            isTechnicienView = true;
        }

        // Initialiser les vues
        initViews(view);

        // Configurer le RecyclerView
        setupRecyclerView();

        // Charger les avions
        loadAvions();

        return view;
    }

    private void initViews(View view) {
        rvAvions = view.findViewById(R.id.rvAvions);
        btnAddAvion = view.findViewById(R.id.btnAddAvion);
        btnAssignTechnicien = view.findViewById(R.id.btnAssignTechnicien); // IMPORTANT: Même ID que XML

        // Vérifier le rôle de l'utilisateur
        String userId = auth.getCurrentUser().getUid();

        // Pour simplifier, on peut vérifier si c'est la vue technicien
        if (isTechnicienView) {
            // Cacher les boutons admin pour les techniciens
            btnAddAvion.setVisibility(View.GONE);
            btnAssignTechnicien.setVisibility(View.GONE);
        } else {
            // Afficher les boutons admin
            btnAddAvion.setVisibility(View.VISIBLE);
            btnAssignTechnicien.setVisibility(View.VISIBLE);

            // Bouton Ajouter
            btnAddAvion.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddAvionActivity.class);
                startActivity(intent);
            });

            // Bouton Assigner Technicien
            btnAssignTechnicien.setOnClickListener(v -> {
                openAssignTechnicienActivity();
            });
        }
    }

    private void openAssignTechnicienActivity() {
        Intent intent = new Intent(getContext(), AssignAvionTechnicienActivity.class);
        startActivity(intent);
    }

    private void setupRecyclerView() {
        adapter = new AvionAdapter(avionList, new AvionAdapter.OnAvionClickListener() {
            @Override
            public void onAvionClick(Avion avion) {
                openAvionDetails(avion);
            }

            @Override
            public void onQrCodeClick(Avion avion) {
                generateQrCode(avion);
            }
        });

        rvAvions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAvions.setAdapter(adapter);
    }

    private void loadAvions() {
        if (isTechnicienView) {
            // Charger seulement les avions assignés au technicien
            String technicienId = auth.getCurrentUser().getUid();

            db.collection("Avions")
                    .whereEqualTo("technicienAssignId", technicienId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            avionList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Avion avion = document.toObject(Avion.class);
                                avion.id = document.getId();
                                avionList.add(avion);
                            }
                            adapter.updateData(avionList);

                            if (avionList.isEmpty()) {
                                Toast.makeText(getContext(),
                                        "Aucun avion assigné pour le moment",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(),
                                    "Erreur de chargement des avions",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Charger tous les avions (vue admin)
            db.collection("Avions")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            avionList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Avion avion = document.toObject(Avion.class);
                                avion.id = document.getId();
                                avionList.add(avion);
                            }
                            adapter.updateData(avionList);
                        } else {
                            Toast.makeText(getContext(),
                                    "Erreur de chargement des avions",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void openAvionDetails(Avion avion) {
        Intent intent = new Intent(getContext(), AvionDetailsActivity.class);
        intent.putExtra("avion_id", avion.id);
        intent.putExtra("matricule", avion.matricule);
        if (isTechnicienView) {
            intent.putExtra("is_technicien_view", true);
        }
        startActivity(intent);
    }

    private void generateQrCode(Avion avion) {
        Intent intent = new Intent(getContext(), QrCodeActivity.class);
        intent.putExtra("avion_id", avion.id);
        intent.putExtra("matricule", avion.matricule);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger la liste quand on revient sur le fragment
        loadAvions();
    }
}