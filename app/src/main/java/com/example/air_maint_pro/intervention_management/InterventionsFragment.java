package com.example.air_maint_pro.intervention_management;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.gestion_avion.Avion;
import com.example.air_maint_pro.R;
import com.example.air_maint_pro.Technicien;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InterventionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private InterventionAdapter adapter;
    private List<Intervention> interventionList;
    private List<Intervention> allInterventionsList; // Store all interventions
    private FirebaseFirestore db;
    private TextView emptyStateText;
    private FloatingActionButton fabAddIntervention;
    private LinearLayout tabActive;
    private LinearLayout tabHistorique;
    private TextView badgeActiveCount;
    private boolean isActiveTabSelected = true;

    // For aircraft and technician data
    private List<Avion> avionList = new ArrayList<>();
    private List<Technicien> technicianList = new ArrayList<>();
    private Map<String, String> avionMap = new HashMap<>(); // display string -> id
    private Map<String, String> technicianMap = new HashMap<>(); // display string -> id

    // Date formatter
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Date selectedDate = new Date();

    // Dropdown adapters
    private ArrayAdapter<String> avionAdapter;
    private ArrayAdapter<String> technicianAdapter;
    private List<String> avionDisplayList = new ArrayList<>();
    private List<String> technicianDisplayList = new ArrayList<>();

    public InterventionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interventions, container, false);

        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewInterventions);
        emptyStateText = view.findViewById(R.id.textEmptyState);
        fabAddIntervention = view.findViewById(R.id.fabAddIntervention);
        tabActive = view.findViewById(R.id.tabActive);
        tabHistorique = view.findViewById(R.id.tabHistorique);
        badgeActiveCount = view.findViewById(R.id.badgeActiveCount);

        interventionList = new ArrayList<>();
        allInterventionsList = new ArrayList<>();
        adapter = new InterventionAdapter(interventionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Setup tab click listeners
        tabActive.setOnClickListener(v -> switchToActiveTab());
        tabHistorique.setOnClickListener(v -> switchToHistoriqueTab());

        // Initialize adapters
        avionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                avionDisplayList
        );

        technicianAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                technicianDisplayList
        );

        // Load initial data
        loadAvions();
        loadTechnicians();
        loadInterventions();

        fabAddIntervention.setOnClickListener(v -> {
            showAddInterventionDialog();
        });

        return view;
    }

    private void loadInterventions() {
        db.collection("interventions")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allInterventionsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Intervention intervention = document.toObject(Intervention.class);
                            intervention.setId(document.getId());
                            allInterventionsList.add(intervention);
                        }
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
            if (isActiveTabSelected) {
                // Active: show everything except completed
                if (!"Terminée".equals(statut) && !"Clôturée".equals(statut)) {
                    interventionList.add(intervention);
                }
            } else {
                // Historique: show only completed
                if ("Terminée".equals(statut) || "Clôturée".equals(statut)) {
                    interventionList.add(intervention);
                }
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

        // Update text colors - first child is the TextView
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

        // Update text colors - first child is the TextView
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

    private void loadAvions() {
        db.collection("Avions")
                .whereEqualTo("etat", "Actif") // Optional: filter by active aircraft
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        avionList.clear();
                        avionMap.clear();
                        avionDisplayList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Avion avion = document.toObject(Avion.class);
                            avion.setId(document.getId());
                            avionList.add(avion);

                            String display = avion.getMatricule() + " - " + avion.getModele();
                            avionDisplayList.add(display);
                            avionMap.put(display, document.getId());
                        }

                        // Update adapter
                        if (avionAdapter != null) {
                            avionAdapter.notifyDataSetChanged();
                        }

                        System.out.println("DEBUG: Loaded " + avionList.size() + " avions");
                    } else {
                        System.out.println("DEBUG: Error loading avions: " + task.getException().getMessage());
                    }
                });
    }

    private void loadTechnicians() {
        db.collection("Users")
                .whereEqualTo("role", "technicien")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        technicianList.clear();
                        technicianMap.clear();
                        technicianDisplayList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Technicien technician = document.toObject(Technicien.class);
                            if (technician != null) {
                                technician.id = document.getId();
                                technicianList.add(technician);

                                String display = technician.getFullName() + " - " + technician.departement;
                                technicianDisplayList.add(display);
                                technicianMap.put(display, document.getId());
                            }
                        }

                        // Update adapter
                        if (technicianAdapter != null) {
                            technicianAdapter.notifyDataSetChanged();
                        }

                        System.out.println("DEBUG: Loaded " + technicianList.size() + " technicians");
                    } else {
                        System.out.println("DEBUG: Error loading technicians: " + task.getException().getMessage());
                    }
                });
    }

    private void showAddInterventionDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Nouvelle Intervention");

        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_intervention, null);
        builder.setView(dialogView);

        // Initialize form fields
        AutoCompleteTextView autoCompleteType = dialogView.findViewById(R.id.autoCompleteType);
        AutoCompleteTextView autoCompleteAvion = dialogView.findViewById(R.id.autoCompleteAvion);
        AutoCompleteTextView autoCompleteTechnicien = dialogView.findViewById(R.id.autoCompleteTechnicien);
        TextInputEditText editTextDuree = dialogView.findViewById(R.id.editTextDuree);
        TextInputEditText editTextDescriptionProbleme = dialogView.findViewById(R.id.editTextDescriptionProbleme);
        AutoCompleteTextView autoCompleteStatut = dialogView.findViewById(R.id.autoCompleteStatut);
        Button buttonSelectDate = dialogView.findViewById(R.id.buttonSelectDate);
        TextView textViewSelectedDate = dialogView.findViewById(R.id.textViewSelectedDate);

        // Setup dropdowns
        setupTypeDropdown(autoCompleteType);
        setupStatusDropdown(autoCompleteStatut);

        // IMPORTANT: Set the adapters to the dropdowns and configure them to show on click
        if (autoCompleteAvion != null) {
            autoCompleteAvion.setAdapter(avionAdapter);
            autoCompleteAvion.setOnClickListener(v -> {
                if (autoCompleteAvion.getAdapter() != null) {
                    autoCompleteAvion.showDropDown();
                }
            });
            autoCompleteAvion.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && autoCompleteAvion.getAdapter() != null) {
                    autoCompleteAvion.showDropDown();
                }
            });
            System.out.println("DEBUG: Set avion adapter with " + avionDisplayList.size() + " items");
        }

        if (autoCompleteTechnicien != null) {
            autoCompleteTechnicien.setAdapter(technicianAdapter);
            autoCompleteTechnicien.setOnClickListener(v -> {
                if (autoCompleteTechnicien.getAdapter() != null) {
                    autoCompleteTechnicien.showDropDown();
                }
            });
            autoCompleteTechnicien.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && autoCompleteTechnicien.getAdapter() != null) {
                    autoCompleteTechnicien.showDropDown();
                }
            });
            System.out.println("DEBUG: Set technician adapter with " + technicianDisplayList.size() + " items");
        }

        // Set current date as default
        textViewSelectedDate.setText(dateFormatter.format(selectedDate));

        // Date picker
        buttonSelectDate.setOnClickListener(v -> showDatePicker(textViewSelectedDate));

        // Set positive button
        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            saveIntervention(
                    autoCompleteType,
                    autoCompleteAvion,
                    autoCompleteTechnicien,
                    editTextDuree,
                    editTextDescriptionProbleme,
                    autoCompleteStatut
            );
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void setupTypeDropdown(AutoCompleteTextView autoCompleteType) {
        String[] interventionTypes = {
                "Maintenance préventive",
                "Maintenance corrective",
                "Révision générale",
                "Inspection technique",
                "Dépannage",
                "Contrôle qualité",
                "Vérification système",
                "Autre"
        };

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                interventionTypes
        );
        autoCompleteType.setAdapter(typeAdapter);
        // Make it behave like a dropdown - show all options on click
        autoCompleteType.setOnClickListener(v -> {
            if (autoCompleteType.getAdapter() != null) {
                autoCompleteType.showDropDown();
            }
        });
        autoCompleteType.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && autoCompleteType.getAdapter() != null) {
                autoCompleteType.showDropDown();
            }
        });
    }

    private void setupStatusDropdown(AutoCompleteTextView autoCompleteStatut) {
        String[] statusOptions = {
                "Planifiée",
                "En cours",
                "Terminée",
                "Clôturée"
        };

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                statusOptions
        );
        autoCompleteStatut.setAdapter(statusAdapter);
        // Make it behave like a dropdown - show all options on click
        autoCompleteStatut.setOnClickListener(v -> {
            if (autoCompleteStatut.getAdapter() != null) {
                autoCompleteStatut.showDropDown();
            }
        });
        autoCompleteStatut.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && autoCompleteStatut.getAdapter() != null) {
                autoCompleteStatut.showDropDown();
            }
        });
    }

    private void showDatePicker(TextView textViewSelectedDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDate = selectedCalendar.getTime();
                    textViewSelectedDate.setText(dateFormatter.format(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void saveIntervention(AutoCompleteTextView autoCompleteType,
                                  AutoCompleteTextView autoCompleteAvion,
                                  AutoCompleteTextView autoCompleteTechnicien,
                                  TextInputEditText editTextDuree,
                                  TextInputEditText editTextDescriptionProbleme,
                                  AutoCompleteTextView autoCompleteStatut) {

        // Validation
        if (autoCompleteType.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Veuillez sélectionner un type d'intervention", Toast.LENGTH_SHORT).show();
            return;
        }

        if (autoCompleteAvion.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Veuillez sélectionner un avion", Toast.LENGTH_SHORT).show();
            return;
        }

        if (autoCompleteTechnicien.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Veuillez sélectionner un technicien", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected values
        String type = autoCompleteType.getText().toString();
        String avionDisplay = autoCompleteAvion.getText().toString();
        String technicianDisplay = autoCompleteTechnicien.getText().toString();

        // Get IDs from maps
        String avionId = avionMap.get(avionDisplay);
        String technicianId = technicianMap.get(technicianDisplay);

        System.out.println("DEBUG: Selected avion display: " + avionDisplay);
        System.out.println("DEBUG: Selected technician display: " + technicianDisplay);
        System.out.println("DEBUG: Found avionId: " + avionId);
        System.out.println("DEBUG: Found technicianId: " + technicianId);

        if (avionId == null || technicianId == null) {
            Toast.makeText(getContext(), "Erreur: sélection invalide. Veuillez sélectionner à nouveau.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new intervention
        Intervention intervention = new Intervention();
        intervention.setTypeIntervention(type);
        intervention.setAvionId(avionId);
        intervention.setTechnicienId(technicianId);
        intervention.setSuperviseurId(getCurrentSupervisorId());

        try {
            float duree = Float.parseFloat(editTextDuree.getText().toString());
            intervention.setDureeHeures(duree);
        } catch (NumberFormatException e) {
            intervention.setDureeHeures(0.0f);
        }

        intervention.setDescriptionProbleme(editTextDescriptionProbleme.getText().toString());
        intervention.setStatut(autoCompleteStatut.getText().toString());
        intervention.setDateIntervention(selectedDate);

        // Save to Firestore
        saveInterventionToFirestore(intervention);
    }

    private String getCurrentSupervisorId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return "";
    }

    private void saveInterventionToFirestore(Intervention intervention) {
        db.collection("interventions")
                .add(intervention)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Intervention ajoutée avec succès!", Toast.LENGTH_SHORT).show();

                    // Update the ID and add to all interventions list
                    intervention.setId(documentReference.getId());
                    allInterventionsList.add(0, intervention);
                    filterInterventions();
                    updateActiveCount();

                    // Scroll to top
                    if (recyclerView != null) {
                        recyclerView.smoothScrollToPosition(0);
                    }

                    // Update empty state
                    if (emptyStateText != null && emptyStateText.getVisibility() == View.VISIBLE) {
                        emptyStateText.setVisibility(View.GONE);
                        if (recyclerView != null) {
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showInterventionDetail(Intervention intervention) {
        InterventionDetailFragment detailFragment = InterventionDetailFragment.newInstance(intervention.getId());
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}