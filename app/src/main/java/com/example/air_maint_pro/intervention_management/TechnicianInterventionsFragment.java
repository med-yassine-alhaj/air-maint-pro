package com.example.air_maint_pro.intervention_management;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;
import com.example.air_maint_pro.gestion_avion.Avion;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TechnicianInterventionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private InterventionAdapter adapter;
    private List<Intervention> interventionList;
    private List<Intervention> allInterventionsList;
    private FirebaseFirestore db;
    private TextView emptyStateText;
    private LinearLayout tabActive;
    private LinearLayout tabHistorique;
    private TextView badgeActiveCount;
    private TextInputEditText editTextSearch;
    private String searchQuery = "";
    private Map<String, String> interventionAvionMatriculeMap = new HashMap<>(); // intervention id -> matricule
    private boolean isActiveTabSelected = true;

    private FirebaseAuth auth;
    private String technicianId;

    public TechnicianInterventionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interventions, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get technician ID from Firebase Auth
        if (auth.getCurrentUser() != null) {
            technicianId = auth.getCurrentUser().getUid();
        }

        recyclerView = view.findViewById(R.id.recyclerViewInterventions);
        emptyStateText = view.findViewById(R.id.textEmptyState);
        tabActive = view.findViewById(R.id.tabActive);
        tabHistorique = view.findViewById(R.id.tabHistorique);
        badgeActiveCount = view.findViewById(R.id.badgeActiveCount);
        editTextSearch = view.findViewById(R.id.editTextSearch);

        // Hide FAB for technicians
        View fab = view.findViewById(R.id.fabAddIntervention);
        if (fab != null) {
            fab.setVisibility(View.GONE);
        }

        interventionList = new ArrayList<>();
        allInterventionsList = new ArrayList<>();
        adapter = new InterventionAdapter(interventionList);
        adapter.setOnItemClickListener(intervention -> {
            // Only show detail for active interventions
            if (!"Terminée".equals(intervention.getStatut()) && !"Clôturée".equals(intervention.getStatut())) {
                showInterventionDetail(intervention);
            }
        });
        adapter.setOnPDFExportClickListener(intervention -> {
            exportInterventionToPDF(intervention);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Setup tab click listeners
        tabActive.setOnClickListener(v -> switchToActiveTab());
        tabHistorique.setOnClickListener(v -> switchToHistoriqueTab());

        // Setup search functionality
        setupSearchBar();

        // Load interventions
        loadInterventions();

        return view;
    }

    private void loadInterventions() {
        // Check if technician ID is available
        if (technicianId == null || technicianId.isEmpty()) {
            emptyStateText.setText("Erreur: Technicien non identifié");
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        // Filter by technician ID
        db.collection("interventions")
                .whereEqualTo("technicienId", technicianId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allInterventionsList.clear();
                        interventionAvionMatriculeMap.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Intervention intervention = document.toObject(Intervention.class);
                            intervention.setId(document.getId());
                            allInterventionsList.add(intervention);
                            
                            // Load aircraft matricule for search
                            loadAircraftMatriculeForIntervention(intervention);
                        }
                        // Sort by createdAt in descending order (newest first)
                        allInterventionsList.sort((i1, i2) -> {
                            if (i1.getCreatedAt() == null && i2.getCreatedAt() == null) return 0;
                            if (i1.getCreatedAt() == null) return 1;
                            if (i2.getCreatedAt() == null) return -1;
                            return i2.getCreatedAt().compareTo(i1.getCreatedAt());
                        });
                        filterInterventions();
                        updateActiveCount();
                    } else {
                        emptyStateText.setText("Erreur de chargement: " + task.getException().getMessage());
                        emptyStateText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });
    }

    private void filterInterventions() {
        interventionList.clear();
        for (Intervention intervention : allInterventionsList) {
            String statut = intervention.getStatut();
            boolean matchesStatus = false;
            
            if (isActiveTabSelected) {
                // Active: show everything except completed
                matchesStatus = !"Terminée".equals(statut) && !"Clôturée".equals(statut);
            } else {
                // Historique: show only completed
                matchesStatus = "Terminée".equals(statut) || "Clôturée".equals(statut);
            }

            // Apply search filter if query exists
            if (matchesStatus && !searchQuery.isEmpty()) {
                if (matchesSearchQuery(intervention)) {
                    interventionList.add(intervention);
                }
            } else if (matchesStatus) {
                interventionList.add(intervention);
            }
        }
        adapter.notifyDataSetChanged();

        if (interventionList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private boolean matchesSearchQuery(Intervention intervention) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return true;
        }

        String query = searchQuery.toLowerCase().trim();
        String matricule = interventionAvionMatriculeMap.get(intervention.getId());
        
        if (matricule != null && matricule.toLowerCase().contains(query)) {
            return true;
        }
        
        return false;
    }

    private void loadAircraftMatriculeForIntervention(Intervention intervention) {
        String avionId = intervention.getAvionId();
        if (avionId == null || avionId.isEmpty()) {
            return;
        }

        // Load from Firestore
        db.collection("Avions").document(avionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Avion avion = documentSnapshot.toObject(Avion.class);
                        if (avion != null) {
                            interventionAvionMatriculeMap.put(intervention.getId(), avion.getMatricule());
                            // Re-filter if search is active
                            if (!searchQuery.isEmpty()) {
                                filterInterventions();
                            }
                        }
                    }
                });
    }

    private void setupSearchBar() {
        if (editTextSearch != null) {
            editTextSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString();
                    filterInterventions();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void updateActiveCount() {
        int activeCount = 0;
        for (Intervention intervention : allInterventionsList) {
            String statut = intervention.getStatut();
            if (!"Terminée".equals(statut) && !"Clôturée".equals(statut)) {
                activeCount++;
            }
        }
        if (activeCount > 0) {
            badgeActiveCount.setText(String.valueOf(activeCount));
            badgeActiveCount.setVisibility(View.VISIBLE);
        } else {
            badgeActiveCount.setVisibility(View.GONE);
        }
    }

    private void switchToActiveTab() {
        isActiveTabSelected = true;
        tabActive.setBackgroundResource(R.drawable.tab_background_selected);
        tabHistorique.setBackgroundResource(R.drawable.tab_background_unselected);

        if (tabActive.getChildCount() > 0 && tabActive.getChildAt(0) instanceof TextView) {
            TextView activeText = (TextView) tabActive.getChildAt(0);
            activeText.setTextColor(getResources().getColor(R.color.card_text, null));
        }
        if (tabHistorique.getChildCount() > 0 && tabHistorique.getChildAt(0) instanceof TextView) {
            TextView historiqueText = (TextView) tabHistorique.getChildAt(0);
            historiqueText.setTextColor(getResources().getColor(R.color.muted_text, null));
        }

        filterInterventions();
    }

    private void switchToHistoriqueTab() {
        isActiveTabSelected = false;
        tabActive.setBackgroundResource(R.drawable.tab_background_unselected);
        tabHistorique.setBackgroundResource(R.drawable.tab_background_selected);

        if (tabActive.getChildCount() > 0 && tabActive.getChildAt(0) instanceof TextView) {
            TextView activeText = (TextView) tabActive.getChildAt(0);
            activeText.setTextColor(getResources().getColor(R.color.muted_text, null));
        }
        if (tabHistorique.getChildCount() > 0 && tabHistorique.getChildAt(0) instanceof TextView) {
            TextView historiqueText = (TextView) tabHistorique.getChildAt(0);
            historiqueText.setTextColor(getResources().getColor(R.color.card_text, null));
        }

        filterInterventions();
    }

    private void showInterventionDetail(Intervention intervention) {
        // Replace current fragment with detail fragment
        // Note: fragment_container is in the activity, not this fragment
        InterventionDetailFragment detailFragment = InterventionDetailFragment.newInstance(intervention.getId());
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void exportInterventionToPDF(Intervention intervention) {
        PDFReportGenerator pdfGenerator = new PDFReportGenerator(requireContext());
        pdfGenerator.generateInterventionReport(intervention, new PDFReportGenerator.PDFGenerationCallback() {
            @Override
            public void onSuccess(String filePath) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), 
                            "PDF report saved to Downloads: " + filePath, 
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error generating PDF: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}