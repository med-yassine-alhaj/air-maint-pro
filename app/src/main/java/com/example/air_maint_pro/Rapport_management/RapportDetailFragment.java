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
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

        // Export button click listener - Now exports as PDF
        btnExport.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Génération du PDF en cours...", Toast.LENGTH_SHORT).show();

            // Export as PDF
            exportAsPDF();
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

    // ==================== PDF EXPORT FUNCTION ====================
    private void exportAsPDF() {
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

                // Create PDF file in app's private storage
                File internalDir = requireContext().getFilesDir();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH).format(new Date());
                String fileName = "Rapport_" + cleanFileName(rapport.getTitle()) + "_" + timestamp + ".pdf";
                File pdfFile = new File(internalDir, fileName);

                // Generate beautiful PDF
                generateBeautifulPDF(pdfFile, rapport);

                // Show success and share
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "✓ PDF généré: " + fileName,
                            Toast.LENGTH_LONG).show();

                    // Share the PDF
                    sharePDF(pdfFile);
                });

            } catch (Exception e) {
                showError("Erreur PDF: " + e.getMessage());
                e.printStackTrace();

                // Fallback to text file if PDF fails
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Création PDF échouée, export texte à la place", Toast.LENGTH_SHORT).show();
                    exportAsTextFallback();
                });
            }
        }).start();
    }

    private void generateBeautifulPDF(File pdfFile, Rapport rapport) throws IOException {
        // Create PDF writer and document
        PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        try {
            // Load fonts
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // ===== HEADER =====
            Paragraph header = new Paragraph("AIR MAINT PRO")
                    .setFont(fontBold)
                    .setFontSize(24)
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(header);

            Paragraph subHeader = new Paragraph("Rapport d'Activité Professionnel")
                    .setFont(fontBold)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(subHeader);

            // ===== TITLE SECTION =====
            Paragraph title = new Paragraph(rapport.getTitle())
                    .setFont(fontBold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setPadding(10)
                    .setMarginBottom(20);
            document.add(title);

            // ===== INFO TABLE =====
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 3}));
            infoTable.setWidth(UnitValue.createPercentValue(100));
            infoTable.setMarginBottom(20);

            // Add table rows with styling
            addStyledTableRow(infoTable, "Type:", rapport.getType(), fontBold, fontNormal);
            addStyledTableRow(infoTable, "Statut:", rapport.getStatut(), fontBold, fontNormal);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
            SimpleDateFormat dateSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);

            if (rapport.getDate_generation() != null) {
                addStyledTableRow(infoTable, "Date de génération:",
                        sdf.format(rapport.getDate_generation().toDate()), fontBold, fontNormal);
            }

            if (rapport.getPer_debut() != null && rapport.getPer_fin() != null) {
                String periode = dateSdf.format(rapport.getPer_debut().toDate()) +
                        " - " + dateSdf.format(rapport.getPer_fin().toDate());
                addStyledTableRow(infoTable, "Période:", periode, fontBold, fontNormal);
            }

            if (rapport.getId() != null) {
                addStyledTableRow(infoTable, "Référence:", rapport.getId(), fontBold, fontNormal);
            }

            document.add(infoTable);

            // ===== DESCRIPTION SECTION =====
            Paragraph descTitle = new Paragraph("DESCRIPTION")
                    .setFont(fontBold)
                    .setFontSize(16)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(descTitle);

            // Add a horizontal line
            Table lineTable = new Table(UnitValue.createPercentArray(new float[]{1}));
            lineTable.setWidth(UnitValue.createPercentValue(100));
            lineTable.setMarginBottom(15);
            Cell lineCell = new Cell()
                    .setHeight(1)
                    .setBackgroundColor(ColorConstants.GRAY);
            lineTable.addCell(lineCell);
            document.add(lineTable);

            // Content with proper formatting
            String content = rapport.getContenu();
            if (content != null && !content.trim().isEmpty()) {
                Paragraph contentPara = new Paragraph(content)
                        .setFont(fontNormal)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.JUSTIFIED)
                        .setMultipliedLeading(1.5f)
                        .setMarginBottom(20);
                document.add(contentPara);
            } else {
                Paragraph noContent = new Paragraph("Aucun contenu spécifié pour ce rapport.")
                        .setFont(fontNormal)
                        .setFontSize(12)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setItalic()
                        .setMarginBottom(20);
                document.add(noContent);
            }

            // ===== SIGNATURE SECTION =====
            Paragraph signatureTitle = new Paragraph("VALIDATION")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setMarginTop(30)
                    .setMarginBottom(20)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(signatureTitle);

            Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            signatureTable.setWidth(UnitValue.createPercentValue(100));
            signatureTable.setMarginBottom(30);

            // Left signature cell
            Cell leftCell = new Cell()
                    .add(new Paragraph("\n\n\n")
                            .add(new Paragraph("________________________")
                                    .setTextAlignment(TextAlignment.CENTER))
                            .add(new Paragraph("Responsable")
                                    .setFont(fontNormal)
                                    .setFontSize(10)
                                    .setTextAlignment(TextAlignment.CENTER))
                            .add(new Paragraph("Signature")
                                    .setFont(fontNormal)
                                    .setFontSize(9)
                                    .setTextAlignment(TextAlignment.CENTER)))
                    .setBorder(null)
                    .setTextAlignment(TextAlignment.CENTER);

            // Right signature cell
            Cell rightCell = new Cell()
                    .add(new Paragraph("\n\n\n")
                            .add(new Paragraph("________________________")
                                    .setTextAlignment(TextAlignment.CENTER))
                            .add(new Paragraph("Administrateur")
                                    .setFont(fontNormal)
                                    .setFontSize(10)
                                    .setTextAlignment(TextAlignment.CENTER))
                            .add(new Paragraph("Cachet & Signature")
                                    .setFont(fontNormal)
                                    .setFontSize(9)
                                    .setTextAlignment(TextAlignment.CENTER)))
                    .setBorder(null)
                    .setTextAlignment(TextAlignment.CENTER);

            signatureTable.addCell(leftCell);
            signatureTable.addCell(rightCell);
            document.add(signatureTable);

            // ===== FOOTER =====
            Paragraph footer = new Paragraph("Document généré le " + sdf.format(new Date()) +
                    " • Air Maint Pro • Page 1/1")
                    .setFont(fontNormal)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            // ===== WATERMARK (optional) =====
            Paragraph watermark = new Paragraph("AIR MAINT PRO")
                    .setFont(fontBold)
                    .setFontSize(48)
                    .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(230, 230, 230))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setRotationAngle(Math.toRadians(45))
                    .setFixedPosition(50, 300, 500);
            document.add(watermark);

        } finally {
            document.close();
        }
    }

    private void addStyledTableRow(Table table, String label, String value,
                                   PdfFont fontBold, PdfFont fontNormal) {
        // Label cell
        Cell labelCell = new Cell()
                .add(new Paragraph(label)
                        .setFont(fontBold)
                        .setFontSize(11))
                .setPadding(8)
                .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(240, 240, 240))
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        // Value cell
        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "Non spécifié")
                        .setFont(fontNormal)
                        .setFontSize(11))
                .setPadding(8)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void sharePDF(File pdfFile) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider",
                    pdfFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Rapport PDF - " + getRapportTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Veuillez trouver ci-joint le rapport en PDF.");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create chooser with specific title
            Intent chooser = Intent.createChooser(shareIntent, "Partager le PDF");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(chooser);

        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "PDF créé, mais partage impossible: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();

            // Fallback: Open the PDF
            openPDF(pdfFile);
        }
    }

    private void openPDF(File pdfFile) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider",
                    pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            // Check if there's a PDF viewer
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Suggest to install a PDF viewer
                Toast.makeText(getContext(),
                        "Installez un lecteur PDF (comme Adobe Reader) pour ouvrir le fichier",
                        Toast.LENGTH_LONG).show();

                // Open file location
                Toast.makeText(getContext(),
                        "PDF sauvegardé: " + pdfFile.getAbsolutePath(),
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Impossible d'ouvrir le PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private String getRapportTitle() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(ARG_RAPPORT_TITLE, "Sans titre");
        }
        return "Rapport";
    }

    // Fallback text export if PDF fails
    private void exportAsTextFallback() {
        new Thread(() -> {
            try {
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

                File internalDir = requireContext().getFilesDir();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH).format(new Date());
                String fileName = "Rapport_" + cleanFileName(rapport.getTitle()) + "_" + timestamp + ".txt";
                File textFile = new File(internalDir, fileName);

                FileOutputStream fos = new FileOutputStream(textFile);
                String content = createRapportContent(rapport);
                fos.write(content.getBytes());
                fos.close();

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "✓ Fichier texte créé: " + fileName,
                            Toast.LENGTH_LONG).show();

                    shareFile(textFile);
                });

            } catch (Exception e) {
                showError("Erreur texte: " + e.getMessage());
            }
        }).start();
    }
    // ==================== END PDF EXPORT ====================

    // ==================== HELPER METHODS ====================
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
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Partager le rapport"));

        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Fichier créé, partage impossible",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String cleanFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
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

    private void loadAdditionalDetails(String rapportId, TextView tvCreatedBy, TextView tvLastModified) {
        db.collection("rapport").document(rapportId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Rapport rapport = documentSnapshot.toObject(Rapport.class);
                        if (rapport != null) {
                            tvCreatedBy.setText("Administrateur");
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
                            tvLastModified.setText(sdf.format(new Date()));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    tvCreatedBy.setText("Non disponible");
                    tvLastModified.setText("Non disponible");
                });
    }
}