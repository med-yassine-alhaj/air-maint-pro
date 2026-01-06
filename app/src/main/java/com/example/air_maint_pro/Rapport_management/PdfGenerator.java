package com.example.air_maint_pro.Rapport_management;

import android.content.Context;
import android.os.Environment;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfGenerator {

    public static File generateRapportPDF(Context context, Rapport rapport, String fileName) throws IOException {
        // Create directory if it doesn't exist
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File appDir = new File(downloadsDir, "AirMaintPro");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        // Create PDF file
        File pdfFile = new File(appDir, fileName + ".pdf");
        PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Load fonts
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Set document margins
            document.setMargins(50, 50, 50, 50);

            // Title
            Paragraph title = new Paragraph("RAPPORT D'ACTIVITÉ")
                    .setFont(fontBold)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Divider line
            document.add(new Paragraph(" ").setMarginBottom(20));

            // Basic Information Table
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 3}));
            infoTable.setWidth(UnitValue.createPercentValue(100));

            // Add rows
            addTableRow(infoTable, "Titre:", rapport.getTitle(), font, fontBold);
            addTableRow(infoTable, "Type:", rapport.getType(), font, fontBold);
            addTableRow(infoTable, "Statut:", rapport.getStatut(), font, fontBold);

            // Format dates
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
            SimpleDateFormat dateSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);

            if (rapport.getDate_generation() != null) {
                addTableRow(infoTable, "Date de génération:",
                        sdf.format(rapport.getDate_generation().toDate()), font, fontBold);
            }

            if (rapport.getPer_debut() != null && rapport.getPer_fin() != null) {
                String periode = dateSdf.format(rapport.getPer_debut().toDate()) +
                        " - " + dateSdf.format(rapport.getPer_fin().toDate());
                addTableRow(infoTable, "Période:", periode, font, fontBold);
            }

            document.add(infoTable);
            document.add(new Paragraph(" ").setMarginBottom(20));

            // Content Section
            Paragraph contentTitle = new Paragraph("DESCRIPTION")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setMarginBottom(10);
            document.add(contentTitle);

            if (rapport.getContenu() != null && !rapport.getContenu().isEmpty()) {
                Paragraph content = new Paragraph(rapport.getContenu())
                        .setFont(font)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.JUSTIFIED);
                document.add(content);
            } else {
                Paragraph noContent = new Paragraph("Aucun contenu disponible.")
                        .setFont(font)
                        .setFontSize(12);
                document.add(noContent);
            }

            document.add(new Paragraph(" ").setMarginBottom(30));

            // Footer
            Paragraph footer = new Paragraph("Document généré le: " +
                    sdf.format(new Date()))
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(footer);

        } catch (Exception e) {
            throw new IOException("Erreur lors de la génération du PDF: " + e.getMessage());
        } finally {
            document.close();
        }

        return pdfFile;
    }

    private static void addTableRow(Table table, String label, String value,
                                    PdfFont font, PdfFont fontBold) {
        table.addCell(new Cell()
                .add(new Paragraph(label)
                        .setFont(fontBold)
                        .setFontSize(12))
                .setPadding(5)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));

        table.addCell(new Cell()
                .add(new Paragraph(value != null ? value : "N/A")
                        .setFont(font)
                        .setFontSize(12))
                .setPadding(5)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));
    }
}