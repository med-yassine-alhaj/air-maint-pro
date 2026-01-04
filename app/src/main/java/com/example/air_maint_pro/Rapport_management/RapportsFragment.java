package com.example.air_maint_pro.Rapport_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.air_maint_pro.R;
import com.example.air_maint_pro.TechnicienListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class RapportsFragment extends Fragment {

    private RecyclerView rvReports;
    private RapportAdapter adapter;
    private List<Rapport> rapportList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_rapports, container, false);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // RecyclerView setup
        rvReports = view.findViewById(R.id.rvReports);
        rvReports.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RapportAdapter(rapportList, this::showRapportOptions);
        rvReports.setAdapter(adapter);

        // Load rapports
        loadRapports();

        // Setup FAB
        setupFloatingActionButton(view);

        // Setup bottom navigation
        setupBottomNavigation(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh list when returning
        loadRapports();
    }

    public void loadRapports() {
        db.collection("rapport")
                .get()
                .addOnSuccessListener(query -> {
                    rapportList.clear();
                    for (QueryDocumentSnapshot d : query) {
                        Rapport r = d.toObject(Rapport.class);
                        if (r != null) {
                            r.setId(d.getId());
                            rapportList.add(r);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur de chargement des rapports", Toast.LENGTH_SHORT).show();
                });
    }

    private void showRapportOptions(Rapport r) {
        String[] options = {"Voir détails", "Modifier", "Supprimer"};

        new AlertDialog.Builder(requireContext())
                .setTitle(r.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showRapportDetails(r);
                            break;
                        case 1:
                            // TODO: Implement edit
                            Toast.makeText(getContext(), "Modifier (à implémenter)", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            deleteRapport(r);
                            break;
                    }
                })
                .show();
    }

    private void showRapportDetails(Rapport r) {
        String details = "Titre: " + r.getTitle() + "\n" +
                "Contenu: " + (r.getContenu() != null ? r.getContenu() : "Aucun contenu") + "\n" +
                "Type: " + r.getType() + "\n" +
                "Statut: " + r.getStatut() + "\n" +
                "Date génération: " + (r.getDate_generation() != null ?
                r.getDate_generation().toDate().toString() : "Non spécifiée") + "\n" +
                "Période début: " + (r.getPer_debut() != null ?
                r.getPer_debut().toDate().toString() : "Non spécifiée") + "\n" +
                "Période fin: " + (r.getPer_fin() != null ?
                r.getPer_fin().toDate().toString() : "Non spécifiée");

        new AlertDialog.Builder(requireContext())
                .setTitle("Détails du Rapport")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void deleteRapport(Rapport r) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation de suppression")
                .setMessage("Voulez-vous vraiment supprimer '" + r.getTitle() + "' ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    db.collection("rapport")
                            .document(r.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Rapport supprimé", Toast.LENGTH_SHORT).show();
                                loadRapports();
                            })
                            .addOnFailureListener(err -> {
                                Toast.makeText(getContext(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // Setup FAB click listener
    private void setupFloatingActionButton(View view) {
        FloatingActionButton fabAddRapport = view.findViewById(R.id.fabAddRapport);
        fabAddRapport.setOnClickListener(v -> {
            // Create dialog with callback
            AddRapportDialogFragment dialog = AddRapportDialogFragment.newInstance(new AddRapportDialogFragment.OnRapportAddedListener() {
                @Override
                public void onRapportAdded() {
                    // Refresh the list when rapport is added
                    loadRapports();
                }
            });
            dialog.show(getParentFragmentManager(), "AddRapportDialog");
        });
    }

    private void setupBottomNavigation(View view) {
        BottomNavigationView bottomNav = view.findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_reports);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new StatistiqueFragment())
                        .addToBackStack(null)
                        .commit();

                if (requireActivity() instanceof com.example.air_maint_pro.AdminActivity) {
                    ((com.example.air_maint_pro.AdminActivity) requireActivity())
                            .hideMainBottomNav();
                }
                return true;

            } else if (itemId == R.id.nav_reports) {
                return true;

            } else if (itemId == R.id.nav_statistics) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ChartFragment())
                        .addToBackStack(null)
                        .commit();
                return true;


            } else if (itemId == R.id.nav_home) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TechnicienListFragment())
                        .commit();

                if (requireActivity() instanceof com.example.air_maint_pro.AdminActivity) {
                    ((com.example.air_maint_pro.AdminActivity) requireActivity())
                            .showMainBottomNav();
                }
                return true;
            }

            return false;
        });
    }
}