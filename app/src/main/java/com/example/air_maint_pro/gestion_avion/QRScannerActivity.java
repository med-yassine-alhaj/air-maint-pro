package com.example.air_maint_pro.gestion_avion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRScannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialiser le scanner
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scanner le QR Code d'un avion sur le tarmac");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setCameraId(0); // Utiliser la caméra arrière
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan annulé", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String qrContent = result.getContents();
                processQRCode(qrContent);
            }
        }
    }

    private void processQRCode(String qrContent) {
        try {
            // Format: aviation://avion/{id}
            if (qrContent.startsWith("aviation://avion/")) {
                String avionId = qrContent.replace("aviation://avion/", "");
                openAvionDetails(avionId);
            } else {
                // Vérifier si c'est directement un ID d'avion
                FirebaseFirestore.getInstance()
                        .collection("Avions")
                        .document(qrContent)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                openAvionDetails(qrContent);
                            } else {
                                Toast.makeText(this, "Avion non trouvé dans la base de données", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Erreur de recherche: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        });
            }
        } catch (Exception e) {
            Toast.makeText(this, "QR Code invalide", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void openAvionDetails(String avionId) {
        Intent intent = new Intent(this, AvionDetailsActivity.class);
        intent.putExtra("avion_id", avionId);
        startActivity(intent);
        finish();
    }
}