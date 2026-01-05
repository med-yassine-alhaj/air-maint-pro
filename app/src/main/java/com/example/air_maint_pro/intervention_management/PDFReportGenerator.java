package com.example.air_maint_pro.intervention_management;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.air_maint_pro.gestion_avion.Avion;
import com.example.air_maint_pro.Technicien;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PDFReportGenerator {

    private Context context;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public PDFReportGenerator(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public interface PDFGenerationCallback {
        void onSuccess(String filePath);
        void onFailure(String error);
    }

    public void generateInterventionReport(Intervention intervention, PDFGenerationCallback callback) {
        try {
            String fileName = "Intervention_Report_" + intervention.getId() + "_" + 
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore API for Android 10+
                generatePDFWithMediaStore(intervention, fileName, callback);
            } else {
                // Use traditional file system for older Android versions
                File pdfFile = createPDFFile(intervention, fileName);
                if (pdfFile == null) {
                    callback.onFailure("Failed to create PDF file");
                    return;
                }

                PdfWriter writer = new PdfWriter(pdfFile);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                loadAndGeneratePDF(intervention, document, pdf, pdfFile.getAbsolutePath(), callback);
            }

        } catch (Exception e) {
            callback.onFailure("Error creating PDF: " + e.getMessage());
        }
    }

    private void generatePDFWithMediaStore(Intervention intervention, String fileName, PDFGenerationCallback callback) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        if (uri == null) {
            callback.onFailure("Failed to create PDF file in Downloads");
            return;
        }

        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream == null) {
                callback.onFailure("Failed to open output stream");
                return;
            }

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            String filePath = "Downloads/" + fileName;
            loadAndGeneratePDF(intervention, document, pdf, filePath, callback);

        } catch (FileNotFoundException e) {
            callback.onFailure("Error creating PDF: " + e.getMessage());
        }
    }

    private void loadAndGeneratePDF(Intervention intervention, Document document, PdfDocument pdf, String filePath, PDFGenerationCallback callback) {
        // Load aircraft data
        db.collection("Avions").document(intervention.getAvionId())
                .get()
                .addOnSuccessListener(avionSnapshot -> {
                    Avion avion;
                    if (avionSnapshot.exists()) {
                        avion = avionSnapshot.toObject(Avion.class);
                    } else {
                        avion = null;
                    }

                    // Load technician data
                    db.collection("Users").document(intervention.getTechnicienId())
                            .get()
                            .addOnSuccessListener(techSnapshot -> {
                                Technicien technician = null;
                                if (techSnapshot.exists()) {
                                    technician = techSnapshot.toObject(Technicien.class);
                                }

                                // Generate PDF content
                                try {
                                    generatePDFContent(document, intervention, avion, technician);
                                    document.close();
                                    callback.onSuccess(filePath);
                                } catch (Exception e) {
                                    callback.onFailure("Error generating PDF content: " + e.getMessage());
                                    document.close();
                                }
                            })
                            .addOnFailureListener(e -> {
                                callback.onFailure("Error loading technician: " + e.getMessage());
                                document.close();
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Error loading aircraft: " + e.getMessage());
                    document.close();
                });
    }

    private void generatePDFContent(Document document, Intervention intervention, Avion avion, Technicien technician) {
        // Title
        Paragraph title = new Paragraph("INTERVENTION REPORT")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Intervention ID
        document.add(new Paragraph("Intervention ID: " + intervention.getId())
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        // Date
        if (intervention.getDateIntervention() != null) {
            document.add(new Paragraph("Date: " + dateFormat.format(intervention.getDateIntervention()))
                    .setFontSize(12)
                    .setMarginBottom(10));
        }

        // Status and Type
        document.add(new Paragraph("Status: " + intervention.getStatut())
                .setFontSize(12)
                .setMarginBottom(5));
        document.add(new Paragraph("Type: " + intervention.getTypeIntervention())
                .setFontSize(12)
                .setMarginBottom(15));

        // Aircraft Information
        document.add(new Paragraph("AIRCRAFT INFORMATION")
                .setFontSize(16)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));
        
        if (avion != null) {
            document.add(new Paragraph("Registration: " + avion.getMatricule())
                    .setFontSize(12)
                    .setMarginBottom(5));
            document.add(new Paragraph("Model: " + avion.getModele())
                    .setFontSize(12)
                    .setMarginBottom(15));
        } else {
            document.add(new Paragraph("Aircraft information not available")
                    .setFontSize(12)
                    .setMarginBottom(15));
        }

        // Technician Information
        document.add(new Paragraph("TECHNICIAN INFORMATION")
                .setFontSize(16)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));
        
        if (technician != null) {
            document.add(new Paragraph("Name: " + technician.getFullName())
                    .setFontSize(12)
                    .setMarginBottom(15));
        } else {
            document.add(new Paragraph("Technician information not available")
                    .setFontSize(12)
                    .setMarginBottom(15));
        }

        // Duration
        document.add(new Paragraph("Duration: " + String.format(Locale.getDefault(), "%.1f hours", intervention.getDureeHeures()))
                .setFontSize(12)
                .setMarginBottom(15));

        // Problem Description
        document.add(new Paragraph("PROBLEM DESCRIPTION")
                .setFontSize(16)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));
        
        String problemDesc = intervention.getDescriptionProbleme();
        if (problemDesc != null && !problemDesc.isEmpty()) {
            document.add(new Paragraph(problemDesc)
                    .setFontSize(12)
                    .setMarginBottom(15));
        } else {
            document.add(new Paragraph("No problem description provided")
                    .setFontSize(12)
                    .setMarginBottom(15));
        }

        // Remarks
        document.add(new Paragraph("REMARKS / OBSERVATIONS")
                .setFontSize(16)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));
        
        String remarks = intervention.getRemarques();
        if (remarks != null && !remarks.isEmpty()) {
            document.add(new Paragraph(remarks)
                    .setFontSize(12)
                    .setMarginBottom(15));
        } else {
            document.add(new Paragraph("No remarks provided")
                    .setFontSize(12)
                    .setMarginBottom(15));
        }

        // Solution
        document.add(new Paragraph("SOLUTION")
                .setFontSize(16)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));
        
        String solution = intervention.getDescriptionSolution();
        if (solution != null && !solution.isEmpty()) {
            document.add(new Paragraph(solution)
                    .setFontSize(12)
                    .setMarginBottom(15));
        } else {
            document.add(new Paragraph("No solution provided")
                    .setFontSize(12)
                    .setMarginBottom(15));
        }

        // Parts Used
        document.add(new Paragraph("PARTS USED")
                .setFontSize(16)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));

        List<PartUsage> parts = intervention.getParts();
        if (parts != null && !parts.isEmpty()) {
            // Create table for parts
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 2}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            // Table header
            table.addHeaderCell(new Paragraph("Part Name").setBold().setFontSize(11));
            table.addHeaderCell(new Paragraph("Qty").setBold().setFontSize(11));
            table.addHeaderCell(new Paragraph("Weight (kg)").setBold().setFontSize(11));
            table.addHeaderCell(new Paragraph("Unit Price").setBold().setFontSize(11));
            table.addHeaderCell(new Paragraph("Total").setBold().setFontSize(11));

            float totalWeight = 0.0f;
            float totalCost = 0.0f;

            // Add parts to table
            for (PartUsage partUsage : parts) {
                Piece piece = partUsage.getPiece();
                if (piece != null) {
                    table.addCell(new Paragraph(piece.getName()).setFontSize(10));
                    table.addCell(new Paragraph(String.valueOf(partUsage.getQuantity())).setFontSize(10));
                    table.addCell(new Paragraph(String.format(Locale.getDefault(), "%.2f", piece.getWeight())).setFontSize(10));
                    table.addCell(new Paragraph(currencyFormat.format(piece.getPrice())).setFontSize(10));
                    table.addCell(new Paragraph(currencyFormat.format(partUsage.getTotalPrice())).setFontSize(10));

                    totalWeight += partUsage.getTotalWeight();
                    totalCost += partUsage.getTotalPrice();
                }
            }

            document.add(table);

            // Summary
            document.add(new Paragraph("SUMMARY")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(10)
                    .setMarginBottom(10));
            
            document.add(new Paragraph("Total Parts: " + parts.size())
                    .setFontSize(12)
                    .setMarginBottom(5));
            document.add(new Paragraph("Total Weight: " + String.format(Locale.getDefault(), "%.2f kg", totalWeight))
                    .setFontSize(12)
                    .setMarginBottom(5));
            document.add(new Paragraph("Total Cost: " + currencyFormat.format(totalCost))
                    .setFontSize(12)
                    .setBold()
                    .setMarginBottom(15));
        } else {
            document.add(new Paragraph("No parts used")
                    .setFontSize(12)
                    .setMarginBottom(15));
        }

        // Footer
        document.add(new Paragraph("Generated on: " + dateFormat.format(new Date()))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));
    }

    private File createPDFFile(Intervention intervention, String fileName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        
        return new File(downloadsDir, fileName);
    }
}

