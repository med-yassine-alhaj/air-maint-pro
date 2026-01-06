package com.example.air_maint_pro.Rapport_management;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.air_maint_pro.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddRapportDialogFragment extends DialogFragment {

    // Interface for callback
    public interface OnRapportAddedListener {
        void onRapportAdded();
    }

    private TextInputEditText etTitle, etContenu, etPerDebut, etPerFin;
    private TextInputLayout tilPerDebut, tilPerFin;
    private AutoCompleteTextView actvType, actvStatus;
    private MaterialButton btnSubmit;
    private FirebaseFirestore db;
    private OnRapportAddedListener listener;

    // Types et statuts disponibles
    private static final String[] RAPPORT_TYPES = {"Maintenance", "Vol", "Technicien", "Global"};
    private static final String[] RAPPORT_STATUS = {"mensuel", "trimestriel", "hebdomadaire"};

    // Static method to create dialog with listener
    public static AddRapportDialogFragment newInstance(OnRapportAddedListener listener) {
        AddRapportDialogFragment fragment = new AddRapportDialogFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(OnRapportAddedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_rapport, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews(view);
        setupDropdowns(view);
        setupDatePickers(view);
        setupSubmitButton(view);

        return view;
    }

    private void initializeViews(View view) {
        etTitle = view.findViewById(R.id.etTitle);
        etContenu = view.findViewById(R.id.etContenu);
        actvType = view.findViewById(R.id.actvType);
        actvStatus = view.findViewById(R.id.actvStatus);
        etPerDebut = view.findViewById(R.id.etPerDebut);
        etPerFin = view.findViewById(R.id.etPerFin);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        tilPerDebut = view.findViewById(R.id.tilPerDebut);
        tilPerFin = view.findViewById(R.id.tilPerFin);
    }

    private void setupDropdowns(View view) {
        // Vérifier que les vues existent
        if (actvType == null || actvStatus == null) {
            Toast.makeText(requireContext(), "Erreur d'initialisation", Toast.LENGTH_SHORT).show();
            return;
        }

        // Setup Type dropdown avec layout système Android
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                RAPPORT_TYPES
        );
        actvType.setAdapter(typeAdapter);

        // Setup Status dropdown avec layout système Android
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                RAPPORT_STATUS
        );
        actvStatus.setAdapter(statusAdapter);

        // Set default values
        actvType.setText(RAPPORT_TYPES[0], false); // "Maintenance" par défaut
        actvStatus.setText(RAPPORT_STATUS[0], false); // "mensuel" par défaut
    }

    private void setupDatePickers(View view) {
        // Period Start
        etPerDebut.setOnClickListener(v -> showDatePicker(etPerDebut));

        // Period End
        etPerFin.setOnClickListener(v -> showDatePicker(etPerFin));

        // Also set up calendar icon clicks
        if (tilPerDebut != null) {
            tilPerDebut.setEndIconOnClickListener(v -> showDatePicker(etPerDebut));
        }
        if (tilPerFin != null) {
            tilPerFin.setEndIconOnClickListener(v -> showDatePicker(etPerFin));
        }
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%02d/%02d/%04d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    editText.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void setupSubmitButton(View view) {
        btnSubmit.setOnClickListener(v -> {
            // Disable button and show loading
            setLoadingState(true);

            // Get data from form
            String title = etTitle.getText().toString().trim();
            String content = etContenu.getText().toString().trim();
            String type = actvType.getText().toString().trim();
            String status = actvStatus.getText().toString().trim();
            String periodStartStr = etPerDebut.getText().toString().trim();
            String periodEndStr = etPerFin.getText().toString().trim();

            // Validation
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "Titre et contenu sont obligatoires", Toast.LENGTH_SHORT).show();
                setLoadingState(false);
                return;
            }

            if (type.isEmpty()) {
                type = "Maintenance"; // Valeur par défaut
            }

            if (status.isEmpty()) {
                status = "mensuel"; // Valeur par défaut
            }

            // Date de génération = maintenant
            Timestamp dateGeneration = Timestamp.now();

            // Convert period strings to Timestamps
            Timestamp perDebut = convertStringToTimestamp(periodStartStr);
            Timestamp perFin = convertStringToTimestamp(periodEndStr);

            // Create Rapport object - CORRECTION: utilise les bons noms de paramètres
            Rapport rapport = new Rapport(
                    title,           // title
                    content,         // contenu
                    type,            // type
                    status,          // statut
                    dateGeneration,  // date_generation
                    perDebut,        // per_debut
                    perFin           // per_fin
            );

            // Save to Firebase
            saveRapportToFirebase(rapport);
        });
    }

    private void setLoadingState(boolean isLoading) {
        if (btnSubmit != null) {
            btnSubmit.setEnabled(!isLoading);
            btnSubmit.setText(isLoading ? "Enregistrement..." : "Générer le rapport");
        }
    }

    private Timestamp convertStringToTimestamp(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
            Date date = sdf.parse(dateStr);
            return new Timestamp(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveRapportToFirebase(Rapport rapport) {
        db.collection("rapport")
                .add(rapport)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "✅ Rapport ajouté avec succès!", Toast.LENGTH_SHORT).show();

                    // D'abord dismiss le dialog
                    dismiss();

                    // Ensuite notifier avec un petit délai
                    if (listener != null) {
                        new android.os.Handler().postDelayed(() -> {
                            listener.onRapportAdded();
                        }, 300); // 300ms de délai
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoadingState(false);
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make dialog full width
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}