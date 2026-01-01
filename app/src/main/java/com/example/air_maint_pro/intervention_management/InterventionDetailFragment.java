package com.example.air_maint_pro.intervention_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.air_maint_pro.Avion;
import com.example.air_maint_pro.R;
import com.example.air_maint_pro.Technicien;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InterventionDetailFragment extends Fragment {

    private static final String ARG_INTERVENTION_ID = "intervention_id";

    private FirebaseFirestore db;
    private String interventionId;
    private Intervention intervention;

    // Views
    private TextView textInterventionId;
    private TextView textStatus;
    private TextView textType;
    private ImageView imageCompletionStatus;
    private LinearLayout tabInfo;
    private LinearLayout tabRemarques;
    private LinearLayout tabSolution;
    private LinearLayout tabClose;
    private ImageView iconRemarquesCheck;
    private FrameLayout contentContainer;
    private ImageButton buttonBack;

    // Content views
    private View infoView;
    private View remarquesView;
    private View solutionView;
    private View closeView;

    // Form fields
    private TextInputEditText editTextRemarques;
    private TextInputEditText editTextSolution;
    private TextInputEditText editTextActualDuration;
    private Button buttonSaveRemarques;
    private Button buttonSaveSolution;
    private Button buttonCloseIntervention;

    // Status indicators
    private ImageView iconRemarquesStatus;
    private ImageView iconSolutionStatus;

    private String currentTab = "Info";
    private boolean hasRemarques = false;
    private boolean hasSolution = false;

    public static InterventionDetailFragment newInstance(String interventionId) {
        InterventionDetailFragment fragment = new InterventionDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INTERVENTION_ID, interventionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intervention_detail, container, false);

        if (getArguments() != null) {
            interventionId = getArguments().getString(ARG_INTERVENTION_ID);
        }

        db = FirebaseFirestore.getInstance();

        // Initialize views
        textInterventionId = view.findViewById(R.id.textInterventionId);
        textStatus = view.findViewById(R.id.textStatus);
        textType = view.findViewById(R.id.textType);
        imageCompletionStatus = view.findViewById(R.id.imageCompletionStatus);
        tabInfo = view.findViewById(R.id.tabInfo);
        tabRemarques = view.findViewById(R.id.tabRemarques);
        tabSolution = view.findViewById(R.id.tabSolution);
        tabClose = view.findViewById(R.id.tabClose);
        iconRemarquesCheck = view.findViewById(R.id.iconRemarquesCheck);
        contentContainer = view.findViewById(R.id.contentContainer);
        buttonBack = view.findViewById(R.id.buttonBack);

        // Setup tab listeners
        tabInfo.setOnClickListener(v -> switchToTab("Info"));
        tabRemarques.setOnClickListener(v -> switchToTab("Remarques"));
        tabSolution.setOnClickListener(v -> switchToTab("Solution"));
        tabClose.setOnClickListener(v -> switchToTab("Close"));

        // Back button
        buttonBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Load intervention data
        loadIntervention();

        return view;
    }

    private void loadIntervention() {
        db.collection("interventions").document(interventionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        intervention = documentSnapshot.toObject(Intervention.class);
                        if (intervention != null) {
                            intervention.setId(documentSnapshot.getId());
                            // Check if remarques and solution exist
                            hasRemarques = intervention.getRemarques() != null && !intervention.getRemarques().isEmpty();
                            hasSolution = intervention.getDescriptionSolution() != null && !intervention.getDescriptionSolution().isEmpty();
                            updateUI();
                            initializeContentViews();
                            switchToTab("Info");
                        }
                    } else {
                        Toast.makeText(getContext(), "Intervention introuvable", Toast.LENGTH_SHORT).show();
                        if (getParentFragmentManager() != null) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur de chargement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (intervention == null) return;

        // Set intervention ID
        textInterventionId.setText(intervention.getId());

        // Set status
        String statut = intervention.getStatut();
        textStatus.setText(statut);
        int statusColor;
        switch (statut) {
            case "Planifiée":
                statusColor = getResources().getColor(R.color.status_waiting, null);
                break;
            case "En cours":
                statusColor = getResources().getColor(R.color.status_in_progress, null);
                break;
            case "Terminée":
            case "Clôturée":
                statusColor = getResources().getColor(R.color.status_done, null);
                imageCompletionStatus.setVisibility(View.VISIBLE);
                break;
            default:
                statusColor = getResources().getColor(R.color.muted, null);
        }
        textStatus.setBackgroundColor(statusColor);

        // Set type
        textType.setText(intervention.getTypeIntervention());
        textType.setBackgroundColor(getResources().getColor(R.color.purple_200, null));

        // Update checkmark visibility (only if view is initialized)
        if (iconRemarquesCheck != null && hasRemarques) {
            iconRemarquesCheck.setVisibility(View.VISIBLE);
        }

        updateCloseTabStatus();
    }

    private void initializeContentViews() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // Info view
        infoView = inflater.inflate(R.layout.content_intervention_info, contentContainer, false);
        populateInfoView();

        // Remarques view
        remarquesView = inflater.inflate(R.layout.content_intervention_remarques, contentContainer, false);
        editTextRemarques = remarquesView.findViewById(R.id.editTextRemarques);
        buttonSaveRemarques = remarquesView.findViewById(R.id.buttonSaveRemarques);
        buttonSaveRemarques.setOnClickListener(v -> saveRemarques());

        // Solution view
        solutionView = inflater.inflate(R.layout.content_intervention_solution, contentContainer, false);
        editTextSolution = solutionView.findViewById(R.id.editTextSolution);
        editTextActualDuration = solutionView.findViewById(R.id.editTextActualDuration);
        buttonSaveSolution = solutionView.findViewById(R.id.buttonSaveSolution);
        buttonSaveSolution.setOnClickListener(v -> saveSolution());

        // Close view
        closeView = inflater.inflate(R.layout.content_intervention_close, contentContainer, false);
        iconRemarquesStatus = closeView.findViewById(R.id.iconRemarquesStatus);
        iconSolutionStatus = closeView.findViewById(R.id.iconSolutionStatus);
        buttonCloseIntervention = closeView.findViewById(R.id.buttonCloseIntervention);
        buttonCloseIntervention.setOnClickListener(v -> closeIntervention());
    }

    private void populateInfoView() {
        if (intervention == null || infoView == null) return;

        // Load aircraft
        db.collection("avions").document(intervention.getAvionId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Avion avion = documentSnapshot.toObject(Avion.class);
                        if (avion != null) {
                            TextView textAircraft = infoView.findViewById(R.id.textAircraft);
                            textAircraft.setText(avion.getMatricule() + " - " + avion.getModele());
                        }
                    }
                });

        // Load technician
        db.collection("Users").document(intervention.getTechnicienId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Technicien technician = documentSnapshot.toObject(Technicien.class);
                        if (technician != null) {
                            TextView textTechnician = infoView.findViewById(R.id.textTechnician);
                            textTechnician.setText(technician.getFullName());
                        }
                    }
                });

        // Load supervisor (if available)
        if (intervention.getSuperviseurId() != null && !intervention.getSuperviseurId().isEmpty()) {
            // Load supervisor - you may need to create a Supervisor class or use a different collection
            TextView textSupervisor = infoView.findViewById(R.id.textSupervisor);
            textSupervisor.setText("ID Superviseur: " + intervention.getSuperviseurId());
        }

        // Set duration
        TextView textDuration = infoView.findViewById(R.id.textDuration);
        textDuration.setText(String.format(Locale.getDefault(), "%.1f heures", intervention.getDureeHeures()));

        // Set problem description
        TextView textProblemDescription = infoView.findViewById(R.id.textProblemDescription);
        textProblemDescription.setText(intervention.getDescriptionProbleme() != null ? 
                intervention.getDescriptionProbleme() : "Aucune description fournie");
    }

    private void switchToTab(String tab) {
        currentTab = tab;

        // Update tab backgrounds
        tabInfo.setBackgroundResource(tab.equals("Info") ? R.drawable.tab_background_selected : R.drawable.tab_background_unselected);
        tabRemarques.setBackgroundResource(tab.equals("Remarques") ? R.drawable.tab_background_selected : R.drawable.tab_background_unselected);
        tabSolution.setBackgroundResource(tab.equals("Solution") ? R.drawable.tab_background_selected : R.drawable.tab_background_unselected);
        tabClose.setBackgroundResource(tab.equals("Close") ? R.drawable.tab_background_selected : R.drawable.tab_background_unselected);

        // Update tab text colors
        updateTabTextColor(tabInfo, tab.equals("Info"));
        updateTabTextColor(tabRemarques, tab.equals("Remarques"));
        updateTabTextColor(tabSolution, tab.equals("Solution"));
        updateTabTextColor(tabClose, tab.equals("Close"));

        // Show appropriate content
        contentContainer.removeAllViews();
        switch (tab) {
            case "Info":
                contentContainer.addView(infoView);
                break;
            case "Remarques":
                contentContainer.addView(remarquesView);
                if (hasRemarques && intervention.getRemarques() != null) {
                    editTextRemarques.setText(intervention.getRemarques());
                }
                break;
            case "Solution":
                contentContainer.addView(solutionView);
                if (hasSolution && intervention.getDescriptionSolution() != null && !intervention.getDescriptionSolution().isEmpty()) {
                    editTextSolution.setText(intervention.getDescriptionSolution());
                    editTextActualDuration.setText(String.format(Locale.getDefault(), "%.1f", intervention.getDureeHeures()));
                }
                break;
            case "Close":
                contentContainer.addView(closeView);
                updateCloseTabStatus();
                break;
        }
    }

    private void updateTabTextColor(LinearLayout tab, boolean isSelected) {
        if (tab.getChildCount() > 0 && tab.getChildAt(0) instanceof TextView) {
            TextView textView = (TextView) tab.getChildAt(0);
            textView.setTextColor(getResources().getColor(
                    isSelected ? R.color.card_text : R.color.muted_text, null));
        }
    }

    private void saveRemarques() {
        String remarques = editTextRemarques.getText().toString().trim();
        if (remarques.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez saisir des remarques", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("remarques", remarques);
        updates.put("updatedAt", new java.util.Date());

        db.collection("interventions").document(interventionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Remarques enregistrées avec succès", Toast.LENGTH_SHORT).show();
                    hasRemarques = true;
                    intervention.setRemarques(remarques);
                    iconRemarquesCheck.setVisibility(View.VISIBLE);
                    updateCloseTabStatus();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de l'enregistrement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveSolution() {
        String solution = editTextSolution.getText().toString().trim();
        if (solution.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez saisir une solution", Toast.LENGTH_SHORT).show();
            return;
        }

        final float actualDuration;
        try {
            String durationText = editTextActualDuration.getText().toString().trim();
            if (!durationText.isEmpty()) {
                actualDuration = Float.parseFloat(durationText);
            } else {
                actualDuration = 0.0f;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Durée invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("descriptionSolution", solution);
        updates.put("dureeHeures", actualDuration);
        updates.put("updatedAt", new java.util.Date());

        final String finalSolution = solution; // Make solution final for lambda
        db.collection("interventions").document(interventionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Solution enregistrée avec succès", Toast.LENGTH_SHORT).show();
                    hasSolution = true;
                    intervention.setDescriptionSolution(finalSolution);
                    intervention.setDureeHeures(actualDuration);
                    updateCloseTabStatus();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de l'enregistrement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCloseTabStatus() {
        if (closeView == null) return;

        // Update status icons
        if (hasRemarques) {
            iconRemarquesStatus.setImageResource(android.R.drawable.checkbox_on_background);
            iconRemarquesStatus.setColorFilter(getResources().getColor(R.color.status_done, null));
        } else {
            iconRemarquesStatus.setImageResource(android.R.drawable.checkbox_off_background);
            iconRemarquesStatus.setColorFilter(getResources().getColor(R.color.error, null));
        }

        if (hasSolution) {
            iconSolutionStatus.setImageResource(android.R.drawable.checkbox_on_background);
            iconSolutionStatus.setColorFilter(getResources().getColor(R.color.status_done, null));
        } else {
            iconSolutionStatus.setImageResource(android.R.drawable.checkbox_off_background);
            iconSolutionStatus.setColorFilter(getResources().getColor(R.color.error, null));
        }

        // Enable close button only if both are completed
        buttonCloseIntervention.setEnabled(hasRemarques && hasSolution);
    }

    private void closeIntervention() {
        if (!hasRemarques || !hasSolution) {
            Toast.makeText(getContext(), "Veuillez compléter les remarques et la solution avant de fermer", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("statut", "Terminée");
        updates.put("updatedAt", new java.util.Date());

        db.collection("interventions").document(interventionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Intervention fermée avec succès", Toast.LENGTH_SHORT).show();
                    intervention.setStatut("Terminée");
                    updateUI();
                    // Go back to list
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de la fermeture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

