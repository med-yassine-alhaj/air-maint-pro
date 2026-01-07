package com.example.air_maint_pro.gestion_vols;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.air_maint_pro.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditVolActivity extends AppCompatActivity {

    private TextInputEditText etNumeroVol, etVilleDepart, etVilleArrivee, etObservations;
    private AutoCompleteTextView autoCompleteAvion, autoCompleteStatut;
    private TextView textViewSelectedDateDepart, textViewSelectedDateArrivee;
    private Button btnSave, btnCancel, buttonSelectDateDepart, buttonSelectDateArrivee;

    private FirebaseFirestore db;
    private Calendar calendarDepart, calendarArrivee;
    private Vol vol; // Pour l'édition
    private boolean isEditMode = false;

    private List<String> avionIds = new ArrayList<>(); // Pour stocker les IDs des avions
    private List<String> avionDisplayList = new ArrayList<>(); // Pour afficher les avions

    // Liste des statuts disponibles
    private String[] statutsArray = {"Planifié", "En cours", "Terminé", "Annulé", "Retardé"};

    // Constantes pour le résultat
    public static final String EXTRA_VOL = "vol";
    public static final String EXTRA_IS_EDIT = "is_edit";
    public static final String EXTRA_VOL_ID = "vol_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_vol);

        db = FirebaseFirestore.getInstance();
        calendarDepart = Calendar.getInstance();
        calendarArrivee = Calendar.getInstance();

        // Initialiser les vues D'ABORD
        initViews();

        // Ensuite initialiser les dates avec l'heure actuelle + 1 heure pour départ
        calendarDepart.add(Calendar.HOUR, 1);
        calendarArrivee.add(Calendar.HOUR, 2);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);

        // Vérifier que les TextView ne sont pas null avant de les utiliser
        if (textViewSelectedDateDepart != null) {
            textViewSelectedDateDepart.setText(sdf.format(calendarDepart.getTime()));
        }

        if (textViewSelectedDateArrivee != null) {
            textViewSelectedDateArrivee.setText(sdf.format(calendarArrivee.getTime()));
        }

        setupListeners();

        // Vérifier si on est en mode édition
        if (getIntent().hasExtra("VOL_ID")) {
            isEditMode = true;
            String volId = getIntent().getStringExtra("VOL_ID");
            if (volId != null && !volId.isEmpty()) {
                loadVolData(volId);
            }
        }
    }

    private void initViews() {
        etNumeroVol = findViewById(R.id.etNumeroVol);
        etVilleDepart = findViewById(R.id.etVilleDepart);
        etVilleArrivee = findViewById(R.id.etVilleArrivee);
        etObservations = findViewById(R.id.etObservations);

        autoCompleteAvion = findViewById(R.id.autoCompleteAvion);
        autoCompleteStatut = findViewById(R.id.autoCompleteStatut);

        textViewSelectedDateDepart = findViewById(R.id.textViewSelectedDateDepart);
        textViewSelectedDateArrivee = findViewById(R.id.textViewSelectedDateArrivee);

        buttonSelectDateDepart = findViewById(R.id.buttonSelectDateDepart);
        buttonSelectDateArrivee = findViewById(R.id.buttonSelectDateArrivee);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Vérifier que tous les éléments sont bien trouvés
        if (textViewSelectedDateDepart == null) {
            Log.e("InitViews", "textViewSelectedDateDepart est null!");
        }
        if (textViewSelectedDateArrivee == null) {
            Log.e("InitViews", "textViewSelectedDateArrivee est null!");
        }

        // Configurer l'AutoCompleteTextView pour les statuts
        ArrayAdapter<String> statutAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statutsArray
        );
        autoCompleteStatut.setAdapter(statutAdapter);
        autoCompleteStatut.setText("Planifié", false); // Valeur par défaut

        // Configurer le comportement de l'AutoCompleteTextView pour les statuts
        setupAutoCompleteTextView(autoCompleteStatut);

        // Charger les avions
        loadAvions();
    }

    private void setupAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView) {
        // Configurer pour afficher la liste déroulante
        autoCompleteTextView.setThreshold(1); // Afficher dès le premier caractère

        // Empêcher l'édition manuelle (seulement sélection)
        autoCompleteTextView.setKeyListener(null);

        // Ouvrir la liste au clic
        autoCompleteTextView.setOnClickListener(v -> {
            autoCompleteTextView.showDropDown();
        });

        // Ouvrir la liste au toucher (pour meilleure UX)
        autoCompleteTextView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                autoCompleteTextView.showDropDown();
                return true; // Consommer l'événement
            }
            return false;
        });

        // Configurer la taille et position de la liste déroulante
        autoCompleteTextView.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        autoCompleteTextView.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        autoCompleteTextView.setDropDownVerticalOffset(4);
        autoCompleteTextView.setDropDownHorizontalOffset(0);
    }

    private void loadAvions() {
        // Récupérer les avions disponibles depuis Firebase
        db.collection("Avions")
                .whereEqualTo("etat", "Actif")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        avionDisplayList.clear();
                        avionIds.clear();

                        // Ajouter un élément vide au début
                        avionDisplayList.add("-- Sélectionner un avion --");
                        avionIds.add("");

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String matricule = document.getString("matricule");
                            String modele = document.getString("modele");
                            String compagnie = document.getString("compagnie");
                            String id = document.getId();

                            if (matricule != null && modele != null) {
                                String displayText;
                                if (compagnie != null && !compagnie.isEmpty()) {
                                    displayText = matricule + " - " + modele + " (" + compagnie + ")";
                                } else {
                                    displayText = matricule + " - " + modele;
                                }
                                avionDisplayList.add(displayText);
                                avionIds.add(id);
                            }
                        }

                        runOnUiThread(() -> {
                            // Créer l'adapter pour l'AutoCompleteTextView
                            ArrayAdapter<String> avionAdapter = new ArrayAdapter<>(
                                    AddEditVolActivity.this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    avionDisplayList
                            );

                            // Configurer l'AutoCompleteTextView des avions
                            autoCompleteAvion.setAdapter(avionAdapter);
                            setupAutoCompleteTextView(autoCompleteAvion);

                            // Afficher un message avec le nombre d'avions
                            if (avionDisplayList.size() > 1) {
                                String message = (avionDisplayList.size() - 1) + " avion(s) disponible(s)";
                                Toast.makeText(AddEditVolActivity.this, message, Toast.LENGTH_SHORT).show();

                                // Si un seul avion disponible, le sélectionner automatiquement
                                if (avionDisplayList.size() == 2) {
                                    autoCompleteAvion.setText(avionDisplayList.get(1), false);
                                }
                            } else {
                                Toast.makeText(AddEditVolActivity.this,
                                        "Aucun avion disponible", Toast.LENGTH_LONG).show();
                                autoCompleteAvion.setEnabled(false);
                            }

                            // Si on est en mode édition, sélectionner l'avion du vol
                            if (isEditMode && vol != null && vol.getAvionId() != null) {
                                int position = avionIds.indexOf(vol.getAvionId());
                                if (position >= 0 && position < avionDisplayList.size()) {
                                    autoCompleteAvion.setText(avionDisplayList.get(position), false);
                                }
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditVolActivity.this,
                                    "Erreur de chargement des avions",
                                    Toast.LENGTH_LONG).show();
                            autoCompleteAvion.setEnabled(false);
                        });
                    }
                });
    }

    private void setupListeners() {
        // Sélecteur de date/heure pour le départ
        buttonSelectDateDepart.setOnClickListener(v -> showDateTimePicker(true));

        // Sélecteur de date/heure pour l'arrivée
        buttonSelectDateArrivee.setOnClickListener(v -> showDateTimePicker(false));

        // Bouton d'enregistrement
        btnSave.setOnClickListener(v -> saveVol());

        // Bouton d'annulation
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void showDateTimePicker(final boolean isDepart) {
        final Calendar calendar = isDepart ? calendarDepart : calendarArrivee;

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Après la date, choisir l'heure
                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                // Mettre à jour le TextView
                                SimpleDateFormat sdf = new SimpleDateFormat(
                                        "dd/MM/yyyy HH:mm", Locale.FRENCH);
                                if (isDepart) {
                                    textViewSelectedDateDepart.setText(sdf.format(calendar.getTime()));
                                } else {
                                    textViewSelectedDateArrivee.setText(sdf.format(calendar.getTime()));
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void saveVol() {
        // Validation des données
        if (!validateForm()) {
            return;
        }

        Vol volToSave = isEditMode ? vol : new Vol();

        // Remplir les informations de base
        volToSave.setNumeroVol(etNumeroVol.getText().toString().trim().toUpperCase());
        volToSave.setVilleDepart(etVilleDepart.getText().toString().trim());
        volToSave.setVilleArrivee(etVilleArrivee.getText().toString().trim());
        volToSave.setDateDepart(calendarDepart.getTime());
        volToSave.setDateArrivee(calendarArrivee.getTime());
        volToSave.setObservations(etObservations.getText().toString().trim());
        volToSave.setStatut(autoCompleteStatut.getText().toString());

        // Récupérer l'avion sélectionné
        String selectedAvionText = autoCompleteAvion.getText().toString().trim();

        if (!selectedAvionText.isEmpty() && !selectedAvionText.equals("-- Sélectionner un avion --")) {
            int position = avionDisplayList.indexOf(selectedAvionText);

            if (position > 0 && position < avionIds.size()) {
                String selectedAvionId = avionIds.get(position);

                // Extraire le matricule de l'avion
                String[] parts = selectedAvionText.split(" - ");
                if (parts.length > 0) {
                    String matricule = parts[0].trim();

                    volToSave.setAvionId(selectedAvionId);
                    volToSave.setAvionMatricule(matricule);

                    // Sauvegarder le vol
                    saveVolToFirestore(volToSave);
                } else {
                    Toast.makeText(this, "Format d'avion invalide", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(this, "Avion sélectionné invalide", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "Veuillez sélectionner un avion", Toast.LENGTH_SHORT).show();
            autoCompleteAvion.setError("Sélection obligatoire");
            return;
        }
    }

    private void saveVolToFirestore(Vol volToSave) {
        String collectionName = "vols";

        if (isEditMode) {
            // Mise à jour du vol existant
            db.collection(collectionName)
                    .document(volToSave.getId())
                    .set(volToSave)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Vol modifié avec succès", Toast.LENGTH_SHORT).show();

                        // Retourner le vol modifié
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_VOL, volToSave);
                        resultIntent.putExtra(EXTRA_IS_EDIT, true);
                        setResult(RESULT_OK, resultIntent);

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Erreur mise à jour vol: ", e);
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Ajout d'un nouveau vol
            db.collection(collectionName)
                    .add(volToSave)
                    .addOnSuccessListener(documentReference -> {
                        // Récupérer l'ID généré
                        volToSave.setId(documentReference.getId());

                        Toast.makeText(this, "Vol ajouté avec succès", Toast.LENGTH_SHORT).show();

                        // Retourner le nouveau vol
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_VOL, volToSave);
                        resultIntent.putExtra(EXTRA_IS_EDIT, false);
                        setResult(RESULT_OK, resultIntent);

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Erreur ajout vol: ", e);
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        // Validation du numéro de vol
        String numeroVol = etNumeroVol.getText().toString().trim();
        if (numeroVol.isEmpty()) {
            etNumeroVol.setError("Le numéro de vol est requis");
            isValid = false;
            errorMessage.append("• Numéro de vol requis\n");
        } else if (numeroVol.length() < 2) {
            etNumeroVol.setError("Numéro de vol trop court (min 2 caractères)");
            isValid = false;
            errorMessage.append("• Numéro de vol trop court\n");
        } else {
            etNumeroVol.setError(null);
        }

        // Validation de la ville de départ
        String villeDepart = etVilleDepart.getText().toString().trim();
        if (villeDepart.isEmpty()) {
            etVilleDepart.setError("La ville de départ est requise");
            isValid = false;
            errorMessage.append("• Ville de départ requise\n");
        } else {
            etVilleDepart.setError(null);
        }

        // Validation de la ville d'arrivée
        String villeArrivee = etVilleArrivee.getText().toString().trim();
        if (villeArrivee.isEmpty()) {
            etVilleArrivee.setError("La ville d'arrivée est requise");
            isValid = false;
            errorMessage.append("• Ville d'arrivée requise\n");
        } else {
            etVilleArrivee.setError(null);
        }

        // Validation des dates
        String dateDepartText = textViewSelectedDateDepart.getText().toString();
        String dateArriveeText = textViewSelectedDateArrivee.getText().toString();

        if (dateDepartText.equals("Non sélectionnée")) {
            errorMessage.append("• Date de départ requise\n");
            isValid = false;
            // Mettre en évidence le bouton
            buttonSelectDateDepart.setBackgroundTintList(
                    ColorStateList.valueOf(getResources().getColor(R.color.red)));
        } else {
            buttonSelectDateDepart.setBackgroundTintList(
                    ColorStateList.valueOf(getResources().getColor(R.color.main)));
        }

        if (dateArriveeText.equals("Non sélectionnée")) {
            errorMessage.append("• Date d'arrivée requise\n");
            isValid = false;
            buttonSelectDateArrivee.setBackgroundTintList(
                    ColorStateList.valueOf(getResources().getColor(R.color.red)));
        } else {
            buttonSelectDateArrivee.setBackgroundTintList(
                    ColorStateList.valueOf(getResources().getColor(R.color.main)));
        }

        // Validation de l'avion
        String selectedAvion = autoCompleteAvion.getText().toString().trim();
        if (selectedAvion.isEmpty() || selectedAvion.equals("-- Sélectionner un avion --")) {
            autoCompleteAvion.setError("Veuillez sélectionner un avion");
            isValid = false;
            errorMessage.append("• Sélection d'un avion requise\n");
        } else {
            autoCompleteAvion.setError(null);
        }

        // Validation du statut
        String selectedStatut = autoCompleteStatut.getText().toString().trim();
        if (selectedStatut.isEmpty()) {
            autoCompleteStatut.setError("Veuillez sélectionner un statut");
            isValid = false;
            errorMessage.append("• Sélection d'un statut requise\n");
        } else {
            autoCompleteStatut.setError(null);
        }

        // Vérification cohérence des dates
        if (isValid && dateDepartText != null && dateArriveeText != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);
                Date dateDepart = sdf.parse(dateDepartText);
                Date dateArrivee = sdf.parse(dateArriveeText);

                if (dateArrivee != null && dateDepart != null) {
                    if (dateArrivee.before(dateDepart)) {
                        errorMessage.append("• La date d'arrivée doit être après le départ\n");
                        isValid = false;
                        Toast.makeText(this,
                                "La date d'arrivée doit être après la date de départ",
                                Toast.LENGTH_LONG).show();
                    }

                    // Vérifier que le vol dure au moins 30 minutes
                    long diff = dateArrivee.getTime() - dateDepart.getTime();
                    long minutes = diff / (60 * 1000);
                    if (minutes < 30) {
                        errorMessage.append("• Le vol doit durer au moins 30 minutes\n");
                        isValid = false;
                        Toast.makeText(this,
                                "Le vol doit durer au moins 30 minutes",
                                Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e("DateValidation", "Erreur parsing date", e);
            }
        }

        // Afficher toutes les erreurs en une fois
        if (!isValid && errorMessage.length() > 0) {
            showErrorDialog(errorMessage.toString());
        }

        return isValid;
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Erreurs de validation")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void loadVolData(String volId) {
        db.collection("vols")
                .document(volId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        vol = documentSnapshot.toObject(Vol.class);
                        if (vol != null) {
                            vol.setId(documentSnapshot.getId());
                            populateForm(vol);
                        }
                    }
                });
    }

    private void populateForm(Vol vol) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);

        etNumeroVol.setText(vol.getNumeroVol());
        etVilleDepart.setText(vol.getVilleDepart());
        etVilleArrivee.setText(vol.getVilleArrivee());
        etObservations.setText(vol.getObservations());

        // Mettre à jour les dates
        if (vol.getDateDepart() != null) {
            textViewSelectedDateDepart.setText(sdf.format(vol.getDateDepart()));
            calendarDepart.setTime(vol.getDateDepart());
        }

        if (vol.getDateArrivee() != null) {
            textViewSelectedDateArrivee.setText(sdf.format(vol.getDateArrivee()));
            calendarArrivee.setTime(vol.getDateArrivee());
        }

        // Sélectionner le statut
        autoCompleteStatut.setText(vol.getStatut(), false);

        // Note: L'avion sera sélectionné automatiquement dans loadAvions()
        // une fois que les données seront chargées
    }
}