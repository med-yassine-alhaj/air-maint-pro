package com.example.air_maint_pro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

public class AdminHomeFragment extends Fragment {

    private TextView tvTotalUsers, tvActiveUsers, tvInactiveUsers, tvTodayUsers;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        initViews(view);

        // Configurer le pull-to-refresh
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadStatistics();
        });

        // Charger les données
        loadStatistics();

        return view;
    }

    private void initViews(View view) {
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvActiveUsers = view.findViewById(R.id.tvActiveUsers);
        tvInactiveUsers = view.findViewById(R.id.tvInactiveUsers);
        tvTodayUsers = view.findViewById(R.id.tvTodayUsers);
    }

    private void loadStatistics() {
        // Compter les utilisateurs total
        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        swipeRefreshLayout.setRefreshing(false);

                        if (task.isSuccessful()) {
                            int totalUsers = task.getResult().size();
                            tvTotalUsers.setText(String.valueOf(totalUsers));

                            // Calculer les statistiques
                            calculateUserStats(task.getResult());
                        } else {
                            Toast.makeText(getContext(), "Erreur de chargement des données", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void calculateUserStats(QuerySnapshot querySnapshot) {
        int activeCount = 0;
        int inactiveCount = 0;
        int todayCount = 0;

        long todayStart = new Date().getTime() - (24 * 60 * 60 * 1000); // Il y a 24h

        for (QueryDocumentSnapshot document : querySnapshot) {
            // Vérifier si l'utilisateur est actif
            // Pour cet exemple, nous allons considérer que tous les utilisateurs sont actifs
            // Vous pouvez ajuster cette logique selon vos besoins

            // Vérifier lastLogin si le champ existe
            Long lastLogin = document.getLong("lastLogin");
            boolean isActive = true; // Par défaut, actif

            if (lastLogin != null) {
                // Si lastLogin est inférieur à 30 jours, l'utilisateur est actif
                isActive = (new Date().getTime() - lastLogin) < (30L * 24 * 60 * 60 * 1000);

                // Compter les connexions d'aujourd'hui
                if (lastLogin > todayStart) {
                    todayCount++;
                }
            }

            // Vérifier si le champ "isActive" existe
            Boolean isActiveField = document.getBoolean("isActive");
            if (isActiveField != null) {
                isActive = isActiveField;
            }

            // Vérifier si c'est un technicien (basé sur le rôle)
            String role = document.getString("role");
            if (role != null && role.equals("technicien")) {
                // Logique spécifique pour les techniciens si nécessaire
            }

            if (isActive) {
                activeCount++;
            } else {
                inactiveCount++;
            }
        }

        tvActiveUsers.setText(String.valueOf(activeCount));
        tvInactiveUsers.setText(String.valueOf(inactiveCount));
        tvTodayUsers.setText(String.valueOf(todayCount));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les données quand le fragment redevient visible
        loadStatistics();
    }
}