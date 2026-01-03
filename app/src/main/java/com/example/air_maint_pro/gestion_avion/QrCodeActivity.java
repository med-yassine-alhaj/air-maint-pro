package com.example.air_maint_pro.gestion_avion;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.air_maint_pro.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QrCodeActivity extends AppCompatActivity {

    private ImageView ivQrCode;
    private TextView tvImmatriculation, tvInfo;
    private Button btnShare, btnSave, btnClose;

    private FirebaseFirestore db;
    private String avionId;
    private String matricule;
    private Bitmap qrBitmap;
    private Avion avion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        // Initialisation
        db = FirebaseFirestore.getInstance();

        // Récupérer les données
        Intent intent = getIntent();
        avionId = intent.getStringExtra("avion_id");
        matricule = intent.getStringExtra("matricule");

        if (avionId == null || matricule == null) {
            Toast.makeText(this, "Erreur: Données manquantes", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        initViews();

        // Charger les données de l'avion
        loadAvionData();

        // Générer le QR Code
        generateQrCode();

        // Configurer les écouteurs
        setupListeners();
    }

    private void initViews() {
        ivQrCode = findViewById(R.id.ivQrCode);
        tvImmatriculation = findViewById(R.id.tvImmatriculation);
        tvInfo = findViewById(R.id.tvInfo);
        btnShare = findViewById(R.id.btnShare);
        btnSave = findViewById(R.id.btnSave);
        btnClose = findViewById(R.id.btnClose);

        tvImmatriculation.setText(matricule);
    }

    private void loadAvionData() {
        db.collection("Avions")
                .document(avionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        avion = documentSnapshot.toObject(Avion.class);
                        if (avion != null) {
                            updateInfoText();
                        }
                    }
                });
    }

    private void updateInfoText() {
        if (avion != null) {
            String info = String.format(Locale.FRENCH,
                    "%s - %s\nScanner pour voir les détails",
                    avion.matricule,
                    avion.modele);
            tvInfo.setText(info);
        }
    }

    private void generateQrCode() {
        try {
            // Créer le contenu du QR Code
            String qrContent = createQrContent();

            // Générer le QR Code
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder encoder = new BarcodeEncoder();
            qrBitmap = encoder.createBitmap(matrix);

            // Afficher le QR Code
            ivQrCode.setImageBitmap(qrBitmap);

        } catch (WriterException e) {
            Toast.makeText(this, "Erreur de génération du QR Code", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String createQrContent() {
        // Format : aviation://avion/{id}
        // Vous pouvez ajouter plus d'informations si nécessaire
        return "aviation://avion/" + avionId;
    }

    private void setupListeners() {
        btnShare.setOnClickListener(v -> shareQrCode());
        btnSave.setOnClickListener(v -> saveQrCode());
        btnClose.setOnClickListener(v -> finish());
    }

    private void shareQrCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "QR Code non généré", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Sauvegarder temporairement l'image
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "qr_code_" + matricule + ".png");
            FileOutputStream stream = new FileOutputStream(file);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // Partager l'image
            Uri contentUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "QR Code de l'avion " + matricule);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Partager QR Code"));

        } catch (IOException e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQrCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "QR Code non généré", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Créer le nom de fichier
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH).format(new Date());
            String fileName = "QR_" + matricule + "_" + timeStamp + ".png";

            // Sauvegarder dans le dossier Pictures
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File qrDir = new File(picturesDir, "AirMaint_QR");
            if (!qrDir.exists()) {
                qrDir.mkdirs();
            }

            File file = new File(qrDir, fileName);
            FileOutputStream stream = new FileOutputStream(file);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // Ajouter à la galerie
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(file));
            sendBroadcast(mediaScanIntent);

            Toast.makeText(this,
                    "QR Code sauvegardé dans " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(this, "Erreur de sauvegarde: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}