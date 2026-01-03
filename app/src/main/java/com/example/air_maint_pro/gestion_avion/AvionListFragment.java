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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AvionListFragment extends Fragment {

    private RecyclerView rvAvions;
    private Button btnAddAvion;
    private AvionAdapter adapter;
    private List<Avion> avionList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avion_list, container, false);

        // Initialisation
        db = FirebaseFirestore.getInstance();

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

        // Bouton Ajouter
        btnAddAvion.setOnClickListener(v -> {
            // Ouvrir l'activité d'ajout
            Intent intent = new Intent(getContext(), AddAvionActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        adapter = new AvionAdapter(avionList, new AvionAdapter.OnAvionClickListener() {
            @Override
            public void onAvionClick(Avion avion) {
                // Ouvrir les détails
                openAvionDetails(avion);
            }

            @Override
            public void onQrCodeClick(Avion avion) {
                // Générer QR Code - TEMPORAIREMENT DÉSACTIVÉ
                // TODO: Créer l'activité QrCodeActivity plus tard
                Toast.makeText(getContext(),
                        "QR Code - Fonctionnalité à venir",
                        Toast.LENGTH_SHORT).show();
                // generateQrCode(avion);
            }
        });

        rvAvions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAvions.setAdapter(adapter);
    }

    private void loadAvions() {
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

    private void openAvionDetails(Avion avion) {
        // Ouvrir l'activité de détails
        Intent intent = new Intent(getContext(), AvionDetailsActivity.class);
        intent.putExtra("avion_id", avion.id);
        intent.putExtra("immatriculation", avion.matricule);
        startActivity(intent);
    }

    /*
    private void generateQrCode(Avion avion) {
        // Ouvrir l'activité QR Code - À CRÉER PLUS TARD
        Intent intent = new Intent(getContext(), QrCodeActivity.class);
        intent.putExtra("avion_id", avion.id);
        intent.putExtra("immatriculation", avion.immatriculation);
        startActivity(intent);
    }
    */

    @Override
    public void onResume() {
        super.onResume();
        // Recharger la liste quand on revient sur le fragment
        loadAvions();
    }
}