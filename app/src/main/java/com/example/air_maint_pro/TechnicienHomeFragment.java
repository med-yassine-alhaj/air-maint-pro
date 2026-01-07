package com.example.air_maint_pro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TechnicienHomeFragment extends Fragment {

    private TextView tvWelcome, tvDate, tvStats;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_technicien_home, container, false);

        // Initialisation Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvDate = view.findViewById(R.id.tvDate);
        tvStats = view.findViewById(R.id.tvStats);

        // Afficher la date actuelle
        String currentDate = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(new Date());
        tvDate.setText(currentDate);

        // Charger les informations du technicien
        loadTechnicianInfo();

        return view;
    }

    private void loadTechnicianInfo() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nom = documentSnapshot.getString("nom");
                        String prenom = documentSnapshot.getString("prenom");
                        String departement = documentSnapshot.getString("departement");

                        String welcomeText = "Bienvenue";
                        if (prenom != null && nom != null) {
                            welcomeText = "Bonjour " + prenom + " " + nom;
                        } else if (prenom != null) {
                            welcomeText = "Bonjour " + prenom;
                        }

                        tvWelcome.setText(welcomeText);

                        // Charger les statistiques d'interventions
                        loadInterventionStats(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur de chargement", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadInterventionStats(String userId) {
        // Comptez les interventions du technicien
        db.collection("Interventions")
                .whereEqualTo("technicienId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int total = querySnapshot.size();
                    int completed = 0;
                    int pending = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String status = doc.getString("status");
                        if ("terminé".equals(status) || "completé".equals(status)) {
                            completed++;
                        } else {
                            pending++;
                        }
                    }

                    String statsText = "Interventions: " + total + " total\n" +
                            "Terminées: " + completed + "\n" +
                            "En attente: " + pending;
                    tvStats.setText(statsText);
                })
                .addOnFailureListener(e -> {
                    tvStats.setText("Aucune intervention pour le moment");
                });
    }
}







