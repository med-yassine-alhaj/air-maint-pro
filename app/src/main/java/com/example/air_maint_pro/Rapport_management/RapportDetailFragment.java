package com.example.air_maint_pro.Rapport_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.air_maint_pro.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RapportDetailFragment extends Fragment {

    private static final String ARG_RAPPORT_ID = "rapport_id";
    private static final String ARG_RAPPORT_TITLE = "rapport_title";
    private static final String ARG_RAPPORT_CONTENT = "rapport_content";
    private static final String ARG_RAPPORT_TYPE = "rapport_type";
    private static final String ARG_RAPPORT_STATUS = "rapport_status";
    private static final String ARG_RAPPORT_DATE_GENERATION = "rapport_date_generation";
    private static final String ARG_RAPPORT_PER_DEBUT = "rapport_per_debut";
    private static final String ARG_RAPPORT_PER_FIN = "rapport_per_fin";

    private FirebaseFirestore db;

    public static RapportDetailFragment newInstance(Rapport rapport) {
        RapportDetailFragment fragment = new RapportDetailFragment();
        Bundle args = new Bundle();

        if (rapport != null) {
            args.putString(ARG_RAPPORT_ID, rapport.getId());
            args.putString(ARG_RAPPORT_TITLE, rapport.getTitle() != null ? rapport.getTitle() : "Sans titre");
            args.putString(ARG_RAPPORT_CONTENT, rapport.getContenu() != null ? rapport.getContenu() : "");
            args.putString(ARG_RAPPORT_TYPE, rapport.getType() != null ? rapport.getType() : "Non spécifié");
            args.putString(ARG_RAPPORT_STATUS, rapport.getStatut() != null ? rapport.getStatut() : "Non spécifié");

            // Convert Timestamps to longs for passing in Bundle
            if (rapport.getDate_generation() != null) {
                args.putLong(ARG_RAPPORT_DATE_GENERATION, rapport.getDate_generation().toDate().getTime());
            }
            if (rapport.getPer_debut() != null) {
                args.putLong(ARG_RAPPORT_PER_DEBUT, rapport.getPer_debut().toDate().getTime());
            }
            if (rapport.getPer_fin() != null) {
                args.putLong(ARG_RAPPORT_PER_FIN, rapport.getPer_fin().toDate().getTime());
            }
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rapport_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvType = view.findViewById(R.id.tvDetailType);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatut);
        TextView tvContent = view.findViewById(R.id.tvDetailContent);
        TextView tvNoContent = view.findViewById(R.id.tvNoContent);
        TextView tvPeriod = view.findViewById(R.id.tvDetailPeriod);
        TextView tvCreatedBy = view.findViewById(R.id.tvDetailCreatedBy);
        TextView tvId = view.findViewById(R.id.tvDetailId);
        TextView tvLastModified = view.findViewById(R.id.tvDetailLastModified);
        View layoutPeriod = view.findViewById(R.id.layoutPeriod);
        MaterialButton btnBack = view.findViewById(R.id.btnBack);
        MaterialButton btnExport = view.findViewById(R.id.btnExport);

        // Date formatter
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);

        // Get data from arguments
        Bundle args = getArguments();
        if (args != null) {
            // Set basic information
            tvTitle.setText(args.getString(ARG_RAPPORT_TITLE, "Sans titre"));
            tvType.setText(args.getString(ARG_RAPPORT_TYPE, "Non spécifié"));
            tvStatus.setText(args.getString(ARG_RAPPORT_STATUS, "Non spécifié"));

            // Set date generation
            long dateGenMillis = args.getLong(ARG_RAPPORT_DATE_GENERATION, 0);
            if (dateGenMillis > 0) {
                String dateStr = dateTimeFormat.format(new Date(dateGenMillis));
                tvDate.setText(dateStr);
            } else {
                tvDate.setText("Date non spécifiée");
            }

            // Set period if available
            long periodStartMillis = args.getLong(ARG_RAPPORT_PER_DEBUT, 0);
            long periodEndMillis = args.getLong(ARG_RAPPORT_PER_FIN, 0);

            if (periodStartMillis > 0 && periodEndMillis > 0) {
                layoutPeriod.setVisibility(View.VISIBLE);
                String periodStartStr = dateFormat.format(new Date(periodStartMillis));
                String periodEndStr = dateFormat.format(new Date(periodEndMillis));
                tvPeriod.setText(periodStartStr + " - " + periodEndStr);
            } else if (periodStartMillis > 0) {
                layoutPeriod.setVisibility(View.VISIBLE);
                String periodStartStr = dateFormat.format(new Date(periodStartMillis));
                tvPeriod.setText("À partir du " + periodStartStr);
            } else if (periodEndMillis > 0) {
                layoutPeriod.setVisibility(View.VISIBLE);
                String periodEndStr = dateFormat.format(new Date(periodEndMillis));
                tvPeriod.setText("Jusqu'au " + periodEndStr);
            }

            // Set content
            String content = args.getString(ARG_RAPPORT_CONTENT, "");
            if (content != null && !content.trim().isEmpty()) {
                tvContent.setText(content);
                tvContent.setVisibility(View.VISIBLE);
                tvNoContent.setVisibility(View.GONE);
            } else {
                tvContent.setVisibility(View.GONE);
                tvNoContent.setVisibility(View.VISIBLE);
            }

            // Set ID
            String fullId = args.getString(ARG_RAPPORT_ID, "N/A");
            tvId.setText(fullId);
        }

        // Back button click listener
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Export button click listener
        btnExport.setOnClickListener(v -> {
            handleExport();
        });

        // Load additional details if we have an ID
        String rapportId = args != null ? args.getString(ARG_RAPPORT_ID) : null;
        if (rapportId != null && !rapportId.isEmpty()) {
            loadAdditionalDetails(rapportId, tvCreatedBy, tvLastModified);
        } else {
            // Hide additional info section if no ID
            tvCreatedBy.setText("Non disponible");
            tvLastModified.setText("Non disponible");
        }
    }

    private void handleExport() {
        // Show export options or implement export logic here
        Toast.makeText(getContext(),
                "Fonctionnalité d'export à implémenter",
                Toast.LENGTH_SHORT).show();

        // You can show/hide export options card if needed
        // View cardExportOptions = requireView().findViewById(R.id.cardExportOptions);
        // cardExportOptions.setVisibility(cardExportOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void loadAdditionalDetails(String rapportId, TextView tvCreatedBy, TextView tvLastModified) {
        db.collection("rapport").document(rapportId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Rapport rapport = documentSnapshot.toObject(Rapport.class);
                        if (rapport != null) {
                            // Set created by if available in your model
                            // For now, using default value
                            tvCreatedBy.setText("Administrateur");

                            // Set last modified date if available
                            // You might want to add a last_modified field to your Rapport model
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
                            tvLastModified.setText(sdf.format(new Date()));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Silent fail - these are optional fields
                    tvCreatedBy.setText("Non disponible");
                    tvLastModified.setText("Non disponible");
                });
    }
}