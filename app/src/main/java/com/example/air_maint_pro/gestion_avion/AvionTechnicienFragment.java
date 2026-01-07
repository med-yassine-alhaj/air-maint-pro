package com.example.air_maint_pro.gestion_avion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.air_maint_pro.R;
import com.example.air_maint_pro.intervention_management.TechnicianInterventionsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AvionTechnicienFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView tvStats;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avion_technicien, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews(view);
        loadStats();

        return view;
    }

    private void initViews(View view) {
        tvStats = view.findViewById(R.id.tvStats);

        CardView cardInfoTechniques = view.findViewById(R.id.cardInfoTechniques);
        CardView cardScannerQR = view.findViewById(R.id.cardScannerQR);
        CardView cardInterventions = view.findViewById(R.id.cardInterventions);

        // 1. Consulter informations techniques → Ouvre AvionListFragment
        cardInfoTechniques.setOnClickListener(v -> openAvionsListFragment());

        // 2. Scanner QR Codes
        cardScannerQR.setOnClickListener(v -> openQRScanner());

        // 3. Voir interventions assignées
        cardInterventions.setOnClickListener(v -> openInterventionsFragment());
    }
    private void openAvionDetails(Avion avion) {
        Intent intent = new Intent(getContext(), AvionDetailsActivity.class);
        intent.putExtra("avion_id", avion.id);
        intent.putExtra("matricule", avion.matricule);
        intent.putExtra("is_technicien_view", true); // Toujours true pour ce fragment
        startActivity(intent);
    }

    private void loadStats() {
        String technicienId = auth.getCurrentUser().getUid();

        // Compter les avions assignés
        db.collection("Avions")
                .whereEqualTo("technicienAssignId", technicienId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int avionCount = querySnapshot.size();

                    // Compter les interventions
                    db.collection("interventions")
                            .whereEqualTo("technicienId", technicienId)
                            .get()
                            .addOnSuccessListener(interventionsSnapshot -> {
                                int interventionCount = interventionsSnapshot.size();

                                String stats = String.format(
                                        "📊 Statistiques\n\n" +
                                                "✈️ Avions assignés: %d\n" +
                                                "🔧 Interventions en cours: %d",
                                        avionCount, interventionCount
                                );
                                tvStats.setText(stats);
                            });
                });
    }

    private void openAvionsListFragment() {
        // Créer une instance de AvionListFragment qui affiche les avions du technicien
        AvionListFragment avionListFragment = new AvionListFragment();

        // Vous pouvez passer des arguments si besoin
        Bundle args = new Bundle();
        args.putString("technicien_id", auth.getCurrentUser().getUid());
        avionListFragment.setArguments(args);

        // Remplacer le fragment actuel
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, avionListFragment)
                .addToBackStack("avions_list")
                .commit();
    }

    private void openQRScanner() {
        // Ouvrir l'activité de scanner
        Intent intent = new Intent(getContext(), QRScannerActivity.class);
        startActivity(intent);
    }

    private void openInterventionsFragment() {
        // Naviguer vers le fragment des interventions
        TechnicianInterventionsFragment fragment = new TechnicianInterventionsFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("interventions")
                .commit();
    }
}