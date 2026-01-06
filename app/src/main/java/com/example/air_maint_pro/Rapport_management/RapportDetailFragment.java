package com.example.air_maint_pro.Rapport_management;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.air_maint_pro.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

    private static final int REQUEST_WRITE_STORAGE = 1001;

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

        // Export button click listener - TEST VERSION (no permissions needed)
        btnExport.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Export en cours...", Toast.LENGTH_SHORT).show();

            // Test export without permissions
            testSimpleExport();

            // For full version with permissions, use:
            // checkStoragePermission();
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

    // ==================== TEST METHOD (NO PERMISSIONS NEEDED) ====================
    private void testSimpleExport() {
        Toast.makeText(getContext(), "Création du fichier...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                // Get rapport data
                Bundle args = getArguments();
                if (args == null) {
                    showError("Pas de données");
                    return;
                }

                Rapport rapport = createRapportFromArguments(args);
                if (rapport == null) {
                    showError("Rapport non trouvé");
                    return;
                }

                // Create file in app's private storage (no permissions needed)
                File internalDir = requireContext().getFilesDir();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH).format(new Date());
                String fileName = "Rapport_" + cleanFileName(rapport.getTitle()) + "_" + timestamp + ".txt";
                File textFile = new File(internalDir, fileName);

                // Write content
                FileOutputStream fos = new FileOutputStream(textFile);
                String content = createRapportContent(rapport);
                fos.write(content.getBytes());
                fos.close();

                // Show success
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "✓ Fichier créé: " + fileName +
                                    "\nEmplacement: " + textFile.getAbsolutePath(),
                            Toast.LENGTH_LONG).show();

                    // Try to share the file
                    shareFile(textFile);
                });

            } catch (Exception e) {
                showError("Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        });
    }

    private String createRapportContent(Rapport rapport) {
        StringBuilder content = new StringBuilder();
        content.append("=================================\n");
        content.append("      RAPPORT D'ACTIVITÉ\n");
        content.append("=================================\n\n");

        content.append("INFORMATIONS DU RAPPORT\n");
        content.append("-----------------------\n");
        content.append("Titre: ").append(rapport.getTitle()).append("\n");
        content.append("Type: ").append(rapport.getType()).append("\n");
        content.append("Statut: ").append(rapport.getStatut()).append("\n");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
        SimpleDateFormat dateSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);

        if (rapport.getDate_generation() != null) {
            content.append("Date de génération: ").append(sdf.format(rapport.getDate_generation().toDate())).append("\n");
        }

        if (rapport.getPer_debut() != null && rapport.getPer_fin() != null) {
            content.append("Période: ").append(dateSdf.format(rapport.getPer_debut().toDate()))
                    .append(" - ").append(dateSdf.format(rapport.getPer_fin().toDate())).append("\n");
        }

        content.append("ID: ").append(rapport.getId() != null ? rapport.getId() : "N/A").append("\n");

        content.append("\nDESCRIPTION\n");
        content.append("-----------\n");
        if (rapport.getContenu() != null && !rapport.getContenu().isEmpty()) {
            content.append(rapport.getContenu()).append("\n");
        } else {
            content.append("Aucun contenu disponible.\n");
        }

        content.append("\n\n=================================\n");
        content.append("Document généré le: ").append(sdf.format(new Date())).append("\n");
        content.append("=================================\n");

        return content.toString();
    }

    private void shareFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Rapport exporté");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Voici le rapport exporté");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Partager le rapport"));

        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Fichier créé, partage impossible: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String cleanFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
    // ==================== END TEST METHOD ====================

    // ==================== FULL VERSION WITH PERMISSIONS ====================
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Request permission
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            } else {
                // Permission already granted
                exportRapport();
            }
        } else {
            // For older versions, permission is granted at install time
            exportRapport();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportRapport();
            } else {
                Toast.makeText(getContext(),
                        "Permission refusée. Impossible d'exporter le rapport.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exportRapport() {
        // Get rapport from arguments
        Bundle args = getArguments();
        if (args == null) {
            Toast.makeText(getContext(), "Données du rapport non disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Rapport object from arguments
        Rapport rapport = createRapportFromArguments(args);
        if (rapport == null) {
            Toast.makeText(getContext(), "Erreur lors de la récupération des données", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading message
        Toast.makeText(getContext(), "Génération du fichier en cours...", Toast.LENGTH_SHORT).show();

        // Run export in background thread
        new Thread(() -> {
            try {
                // Generate file name with current timestamp
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH).format(new Date());
                String fileName = "Rapport_" + cleanFileName(rapport.getTitle()) + "_" + timestamp;

                // Generate the file
                File exportedFile = generateRapportFile(requireContext(), rapport, fileName);

                // Update UI on main thread
                requireActivity().runOnUiThread(() -> {
                    // Show success message
                    Toast.makeText(getContext(),
                            "Fichier généré: " + exportedFile.getName(),
                            Toast.LENGTH_LONG).show();

                    // Open the file
                    openFile(exportedFile);
                });

            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Erreur lors de la génération: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private Rapport createRapportFromArguments(Bundle args) {
        Rapport rapport = new Rapport();
        rapport.setId(args.getString(ARG_RAPPORT_ID));
        rapport.setTitle(args.getString(ARG_RAPPORT_TITLE, "Sans titre"));
        rapport.setContenu(args.getString(ARG_RAPPORT_CONTENT, ""));
        rapport.setType(args.getString(ARG_RAPPORT_TYPE, "Non spécifié"));
        rapport.setStatut(args.getString(ARG_RAPPORT_STATUS, "Non spécifié"));

        long dateGenMillis = args.getLong(ARG_RAPPORT_DATE_GENERATION, 0);
        if (dateGenMillis > 0) {
            rapport.setDate_generation(new Timestamp(new Date(dateGenMillis)));
        }

        long periodStartMillis = args.getLong(ARG_RAPPORT_PER_DEBUT, 0);
        if (periodStartMillis > 0) {
            rapport.setPer_debut(new Timestamp(new Date(periodStartMillis)));
        }

        long periodEndMillis = args.getLong(ARG_RAPPORT_PER_FIN, 0);
        if (periodEndMillis > 0) {
            rapport.setPer_fin(new Timestamp(new Date(periodEndMillis)));
        }

        return rapport;
    }

    private File generateRapportFile(Context context, Rapport rapport, String fileName) throws IOException {
        // Create directory if it doesn't exist
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File appDir = new File(downloadsDir, "AirMaintPro");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        // Create text file
        File textFile = new File(appDir, fileName + ".txt");
        FileOutputStream fos = new FileOutputStream(textFile);

        // Write content
        String content = createRapportContent(rapport);
        fos.write(content.getBytes());
        fos.close();

        return textFile;
    }

    private void openFile(File file) {
        try {
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".provider",
                        file);
            } else {
                uri = Uri.fromFile(file);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/plain");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            // Check if there's an app to handle text files
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Show file location
                Toast.makeText(getContext(),
                        "Fichier sauvegardé dans: Downloads/AirMaintPro/" + file.getName(),
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            // Show file location even if we can't open it
            Toast.makeText(getContext(),
                    "Fichier sauvegardé dans: " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        }
    }
    // ==================== END FULL VERSION ====================

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