package com.example.air_maint_pro.Rapport_management;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.air_maint_pro.R;
import com.example.air_maint_pro.SettingsFragment;
import com.example.air_maint_pro.TechnicienListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RapportsFragment extends Fragment implements RapportAdapter.OnRapportActionListener, RapportAdapter.OnRapportClickListener {

    private RecyclerView rvReports;
    private RapportAdapter adapter;
    private List<Rapport> rapportList = new ArrayList<>();
    private List<Rapport> filteredList = new ArrayList<>();
    private FirebaseFirestore db;
    private EditText searchEditText;
    private ImageButton clearSearchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_rapports, container, false);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        searchEditText = view.findViewById(R.id.searchEditText);
        clearSearchButton = view.findViewById(R.id.clearSearchButton);

        // RecyclerView setup
        rvReports = view.findViewById(R.id.rvReports);
        rvReports.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with empty list
        adapter = new RapportAdapter(filteredList, this, this);
        rvReports.setAdapter(adapter);

        // Setup search functionality
        setupSearch();

        // Load rapports
        loadRapports();

        // Setup FAB
        setupFloatingActionButton(view);

        // Setup bottom navigation
        setupBottomNavigation(view);

        return view;
    }

    private void setupSearch() {
        // Add text change listener to search field
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRapports(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear search button
        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            filterRapports("");
        });

        // Show/hide clear button based on text
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterRapports(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            // If search is empty, show all rapports
            filteredList.addAll(rapportList);
        } else {
            // Filter the list
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            for (Rapport rapport : rapportList) {
                // Search in title, type, status, and content
                if (rapport.getTitle() != null && rapport.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        rapport.getType() != null && rapport.getType().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        rapport.getStatut() != null && rapport.getStatut().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        rapport.getContenu() != null && rapport.getContenu().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredList.add(rapport);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // Show message if no results found
        if (!query.isEmpty() && filteredList.isEmpty()) {
            Toast.makeText(getContext(), "Aucun rapport trouvé pour \"" + query + "\"", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLongClick(Rapport rapport) {
        showRapportOptions(rapport);
    }

    @Override
    public void onRapportClick(Rapport rapport) {
        openRapportDetail(rapport);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh list when returning
        loadRapports();
    }

    public void loadRapports() {
        db.collection("rapport")
                .orderBy("date_generation", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    rapportList.clear();
                    for (QueryDocumentSnapshot d : query) {
                        try {
                            Rapport r = d.toObject(Rapport.class);
                            if (r != null) {
                                r.setId(d.getId());
                                rapportList.add(r);
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Erreur parsing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    // Update filtered list with all rapports initially
                    filterRapports(searchEditText.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur de chargement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showRapportOptions(Rapport r) {
        String[] options = {"Voir détails", "Modifier", "Supprimer"};

        new AlertDialog.Builder(requireContext())
                .setTitle(r.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openRapportDetail(r);
                            break;
                        case 1:
                            Toast.makeText(getContext(), "Modifier (à implémenter)", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            deleteRapport(r);
                            break;
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void openRapportDetail(Rapport r) {
        RapportDetailFragment detailFragment = RapportDetailFragment.newInstance(r);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack("rapport_detail")
                .commit();
    }

    private void showRapportDetails(Rapport r) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);

        String dateGen = r.getDate_generation() != null ?
                sdf.format(r.getDate_generation().toDate()) : "Non spécifiée";
        String perDebut = r.getPer_debut() != null ?
                sdf.format(r.getPer_debut().toDate()) : "Non spécifiée";
        String perFin = r.getPer_fin() != null ?
                sdf.format(r.getPer_fin().toDate()) : "Non spécifiée";

        String details = "Titre: " + r.getTitle() + "\n\n" +
                "Contenu: " + (r.getContenu() != null ?
                (r.getContenu().length() > 100 ?
                        r.getContenu().substring(0, 100) + "..." :
                        r.getContenu()) :
                "Aucun contenu") + "\n\n" +
                "Type: " + r.getType() + "\n\n" +
                "Statut: " + r.getStatut() + "\n\n" +
                "Date génération: " + dateGen + "\n\n" +
                "Période début: " + perDebut + "\n\n" +
                "Période fin: " + perFin;

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

    private void setupFloatingActionButton(View view) {
        FloatingActionButton fabAddRapport = view.findViewById(R.id.fabAddRapport);
        fabAddRapport.setOnClickListener(v -> {
            AddRapportDialogFragment dialog = AddRapportDialogFragment.newInstance(() -> {
                loadRapports();
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
                        .commit();
                return true;

            } else if (itemId == R.id.nav_reports) {
                return true;

            } else if (itemId == R.id.nav_statistics) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ChartFragment())
                        .commit();
                return true;

            }else if (itemId == R.id.nav_settings) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
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