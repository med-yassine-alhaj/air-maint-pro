package com.example.air_maint_pro.Rapport_management;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private TextInputEditText etTitle, etContenu, etType, etStatut;
    private TextInputEditText etDateGeneration, etPerDebut, etPerFin;
    private TextInputLayout tilDateGeneration, tilPerDebut, tilPerFin;
    private MaterialButton btnSubmit;
    private FirebaseFirestore db;
    private OnRapportAddedListener listener;

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
        setupDatePickers(view);
        setupSubmitButton(view);

        return view;
    }

    private void initializeViews(View view) {
        etTitle = view.findViewById(R.id.etTitle);
        etContenu = view.findViewById(R.id.etContenu);
        etType = view.findViewById(R.id.etType);
        etStatut = view.findViewById(R.id.etStatut);
        etDateGeneration = view.findViewById(R.id.etDateGeneration);
        etPerDebut = view.findViewById(R.id.etPerDebut);
        etPerFin = view.findViewById(R.id.etPerFin);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        tilDateGeneration = view.findViewById(R.id.tilDateGeneration);
        tilPerDebut = view.findViewById(R.id.tilPerDebut);
        tilPerFin = view.findViewById(R.id.tilPerFin);
    }

    private void setupDatePickers(View view) {
        // Date Generation
        etDateGeneration.setOnClickListener(v -> showDatePicker(etDateGeneration));

        // Period Start
        etPerDebut.setOnClickListener(v -> showDatePicker(etPerDebut));

        // Period End
        etPerFin.setOnClickListener(v -> showDatePicker(etPerFin));

        // Also set up calendar icon clicks
        if (tilDateGeneration != null) {
            tilDateGeneration.setEndIconOnClickListener(v -> showDatePicker(etDateGeneration));
        }
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
            String type = etType.getText().toString().trim();
            String status = etStatut.getText().toString().trim();
            String dateGenStr = etDateGeneration.getText().toString().trim();
            String periodStartStr = etPerDebut.getText().toString().trim();
            String periodEndStr = etPerFin.getText().toString().trim();

            // Validation
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "Titre et contenu sont obligatoires", Toast.LENGTH_SHORT).show();
                setLoadingState(false);
                return;
            }

            if (type.isEmpty()) {
                type = "Non spécifié";
            }

            if (status.isEmpty()) {
                status = "Brouillon";
            }

            // Convert date strings to Timestamps
            Timestamp dateGeneration = convertStringToTimestamp(dateGenStr);
            Timestamp perDebut = convertStringToTimestamp(periodStartStr);
            Timestamp perFin = convertStringToTimestamp(periodEndStr);

            // Create Rapport object
            Rapport rapport = new Rapport(
                    title,
                    content,
                    type,
                    status,
                    dateGeneration,
                    perDebut,
                    perFin
            );

            // Save to Firebase
            saveRapportToFirebase(rapport);
        });
    }

    private void setLoadingState(boolean isLoading) {
        if (btnSubmit != null) {
            btnSubmit.setEnabled(!isLoading);
            btnSubmit.setText(isLoading ? "Enregistrement..." : "Enregistrer");
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
                    Toast.makeText(requireContext(), "Rapport ajouté avec succès!", Toast.LENGTH_SHORT).show();

                    // Notify listener FIRST
                    if (listener != null) {
                        listener.onRapportAdded();
                    }

                    // THEN dismiss dialog
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoadingState(false); // Re-enable button on error
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