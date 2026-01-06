package com.example.air_maint_pro.gestion_vols;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddEditEquipeActivity extends AppCompatActivity
        implements MembreSelectionAdapter.OnMembreClickListener {

    private TextInputEditText etNomEquipe, etCodeEquipe, etDescription;
    private Button btnSave, btnCancel;
    private TextView tvMembreCount, tvCompositionStatus;
    private LinearLayout layoutMembresSelectionnes;
    private RecyclerView recyclerViewMembres;

    private FirebaseFirestore db;
    private Equipe equipe;
    private boolean isEditMode = false;

    private List<MembreEquipage> tousLesMembres = new ArrayList<>();
    private List<MembreEquipe> membresSelectionnes = new ArrayList<>();
    private MembreSelectionAdapter membresAdapter;

    // Constantes pour le résultat
    public static final String EXTRA_EQUIPE = "equipe";
    public static final String EXTRA_IS_EDIT = "is_edit";
    public static final String EXTRA_EQUIPE_ID = "equipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_equipe);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
        setupRecyclerView();
        loadMembresDisponibles();

        // Vérifier si on est en mode édition
        if (getIntent().hasExtra("EQUIPE_ID")) {
            isEditMode = true;
            String equipeId = getIntent().getStringExtra("EQUIPE_ID");
            if (equipeId != null && !equipeId.isEmpty()) {
                loadEquipeData(equipeId);
            }
        }
    }

    private void initViews() {
        etNomEquipe = findViewById(R.id.etNomEquipe);
        etCodeEquipe = findViewById(R.id.etCodeEquipe);
        etDescription = findViewById(R.id.etDescription);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        tvMembreCount = findViewById(R.id.tvMembreCount);
        tvCompositionStatus = findViewById(R.id.tvCompositionStatus);
        layoutMembresSelectionnes = findViewById(R.id.layoutMembresSelectionnes);
        recyclerViewMembres = findViewById(R.id.recyclerViewMembres);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveEquipe());
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void setupRecyclerView() {
        recyclerViewMembres.setLayoutManager(new LinearLayoutManager(this));
        membresAdapter = new MembreSelectionAdapter(tousLesMembres, this);
        recyclerViewMembres.setAdapter(membresAdapter);
    }

    private void loadMembresDisponibles() {
        // Charger les membres disponibles (non assignés à une équipe)
        db.collection("equipage")
                .whereEqualTo("disponible", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tousLesMembres.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MembreEquipage membre = document.toObject(MembreEquipage.class);
                            membre.setId(document.getId());
                            tousLesMembres.add(membre);
                        }

                        // Si en mode édition, exclure les membres déjà dans l'équipe
                        if (isEditMode && equipe != null && equipe.getMembres() != null) {
                            for (MembreEquipe membreEquipe : equipe.getMembres()) {
                                tousLesMembres.removeIf(m ->
                                        m.getId().equals(membreEquipe.getMembreId()));
                            }
                        }

                        membresAdapter.notifyDataSetChanged();

                        if (tousLesMembres.isEmpty()) {
                            Toast.makeText(this,
                                    "Aucun membre disponible. Ajoutez d'abord des membres d'équipage.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this,
                                "Erreur de chargement des membres",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadEquipeData(String equipeId) {
        db.collection("equipes")
                .document(equipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        equipe = documentSnapshot.toObject(Equipe.class);
                        if (equipe != null) {
                            equipe.setId(documentSnapshot.getId());
                            populateForm(equipe);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Erreur de chargement de l'équipe",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateForm(Equipe equipe) {
        etNomEquipe.setText(equipe.getNom());
        etCodeEquipe.setText(equipe.getCode());
        etDescription.setText(equipe.getDescription());

        // Charger les membres sélectionnés
        if (equipe.getMembres() != null) {
            membresSelectionnes.clear();
            membresSelectionnes.addAll(equipe.getMembres());
            updateMembresSelectionnesUI();
        }

        // Mettre à jour le titre
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            tvTitle.setText("Modifier l'équipe");
        }
    }

    @Override
    public void onMembreClick(MembreEquipage membre) {
        // Vérifier si le membre est déjà sélectionné
        for (MembreEquipe membreEquipe : membresSelectionnes) {
            if (membreEquipe.getMembreId().equals(membre.getId())) {
                Toast.makeText(this,
                        "Ce membre est déjà dans l'équipe",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Vérifier la limite de 6 membres
        if (membresSelectionnes.size() >= 6) {
            Toast.makeText(this,
                    "Maximum 6 membres par équipe",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Ajouter le membre à la sélection
        MembreEquipe nouveauMembre = new MembreEquipe(
                membre.getId(),
                membre.getNomComplet(),
                membre.getRole(),
                membre.getMatricule()
        );

        membresSelectionnes.add(nouveauMembre);
        updateMembresSelectionnesUI();

        // Retirer de la liste des disponibles
        tousLesMembres.remove(membre);
        membresAdapter.notifyDataSetChanged();
    }

    private void updateMembresSelectionnesUI() {
        // Mettre à jour le compteur
        tvMembreCount.setText(membresSelectionnes.size() + " / 6 membres");

        // Mettre à jour le statut de composition
        Equipe tempEquipe = new Equipe();
        tempEquipe.setMembres(membresSelectionnes);

        if (tempEquipe.aCompositionMinimale()) {
            tvCompositionStatus.setText("✓ Composition complète");
            tvCompositionStatus.setTextColor(getResources().getColor(R.color.status_done));
        } else {
            tvCompositionStatus.setText("⚠ Composition incomplète");
            tvCompositionStatus.setTextColor(getResources().getColor(R.color.orange));

            // Afficher les détails de ce qui manque
            StringBuilder details = new StringBuilder("Manque: ");
            int pilotes = tempEquipe.getPilotes().size();
            int copilotes = tempEquipe.getCopilotes().size();
            int personnelCabine = tempEquipe.getPersonnelCabine().size();

            if (pilotes < 1) details.append("Pilote, ");
            if (copilotes < 1) details.append("Copilote, ");
            if (personnelCabine < 2) details.append("Personnel cabine (" + personnelCabine + "/2), ");

            // Retirer la dernière virgule
            String message = details.toString();
            if (message.endsWith(", ")) {
                message = message.substring(0, message.length() - 2);
            }
            tvCompositionStatus.append("\n" + message);
        }

        // Afficher les membres sélectionnés
        layoutMembresSelectionnes.removeAllViews();

        for (MembreEquipe membre : membresSelectionnes) {
            View membreView = getLayoutInflater().inflate(
                    R.layout.item_membre_selectionne,
                    layoutMembresSelectionnes,
                    false);

            TextView tvNom = membreView.findViewById(R.id.tvNomMembre);
            TextView tvRole = membreView.findViewById(R.id.tvRoleMembre);

            tvNom.setText(membre.getNomComplet());
            tvRole.setText(membre.getRole() + " (" + membre.getMatricule() + ")");

            // Bouton pour retirer le membre
            Button btnRetirer = membreView.findViewById(R.id.btnRetirerMembre);
            btnRetirer.setOnClickListener(v -> {
                retirerMembre(membre);
            });

            layoutMembresSelectionnes.addView(membreView);
        }
    }

    private void retirerMembre(MembreEquipe membre) {
        // Retirer de la sélection
        membresSelectionnes.remove(membre);

        // Re-ajouter à la liste des disponibles
        MembreEquipage membreComplet = null;
        for (MembreEquipage m : tousLesMembres) {
            if (m.getId().equals(membre.getMembreId())) {
                membreComplet = m;
                break;
            }
        }

        if (membreComplet == null) {
            // Reconstruire l'objet MembreEquipage
            membreComplet = new MembreEquipage();
            membreComplet.setId(membre.getMembreId());
            membreComplet.setNom(membre.getNomComplet().split(" ")[1]); // Approximatif
            membreComplet.setPrenom(membre.getNomComplet().split(" ")[0]);
            membreComplet.setRole(membre.getRole());
            membreComplet.setMatricule(membre.getMatricule());
        }

        tousLesMembres.add(membreComplet);
        membresAdapter.notifyDataSetChanged();
        updateMembresSelectionnesUI();
    }

    private void saveEquipe() {
        if (!validateForm()) {
            return;
        }

        Equipe equipeToSave = isEditMode ? equipe : new Equipe();

        // Remplir les informations
        equipeToSave.setNom(etNomEquipe.getText().toString().trim());
        equipeToSave.setCode(etCodeEquipe.getText().toString().trim().toUpperCase());
        equipeToSave.setDescription(etDescription.getText().toString().trim());
        equipeToSave.setMembres(membresSelectionnes);
        equipeToSave.setDateModification(new Date());

        if (!isEditMode) {
            equipeToSave.setDateCreation(new Date());
            equipeToSave.setStatut("Disponible");
        }

        // Sauvegarder dans Firestore
        saveEquipeToFirestore(equipeToSave);
    }

    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        // Validation du nom
        String nom = etNomEquipe.getText().toString().trim();
        if (nom.isEmpty()) {
            etNomEquipe.setError("Le nom de l'équipe est requis");
            isValid = false;
            errorMessage.append("• Nom requis\n");
        } else {
            etNomEquipe.setError(null);
        }

        // Validation du code
        String code = etCodeEquipe.getText().toString().trim();
        if (code.isEmpty()) {
            etCodeEquipe.setError("Le code de l'équipe est requis");
            isValid = false;
            errorMessage.append("• Code requis\n");
        } else if (code.length() < 2 || code.length() > 10) {
            etCodeEquipe.setError("Le code doit faire entre 2 et 10 caractères");
            isValid = false;
            errorMessage.append("• Code invalide\n");
        } else {
            etCodeEquipe.setError(null);
        }

        // Validation des membres
        if (membresSelectionnes.isEmpty()) {
            errorMessage.append("• Ajoutez au moins un membre\n");
            isValid = false;
        } else if (membresSelectionnes.size() < 4) {
            errorMessage.append("• Minimum 4 membres requis\n");
            isValid = false;
        }

        // Vérifier la composition minimale
        Equipe tempEquipe = new Equipe();
        tempEquipe.setMembres(membresSelectionnes);
        if (!tempEquipe.aCompositionMinimale()) {
            errorMessage.append("• Composition incomplète (1 pilote, 1 copilote, 2 personnels cabine)\n");
            isValid = false;
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

    private void saveEquipeToFirestore(Equipe equipeToSave) {
        String collectionName = "equipes";

        if (isEditMode) {
            // Mise à jour
            db.collection(collectionName)
                    .document(equipeToSave.getId())
                    .set(equipeToSave)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this,
                                "Équipe modifiée avec succès",
                                Toast.LENGTH_SHORT).show();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_EQUIPE, equipeToSave);
                        resultIntent.putExtra(EXTRA_IS_EDIT, true);
                        setResult(RESULT_OK, resultIntent);

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Erreur mise à jour équipe: ", e);
                        Toast.makeText(this, "Erreur: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Ajout
            db.collection(collectionName)
                    .add(equipeToSave)
                    .addOnSuccessListener(documentReference -> {
                        equipeToSave.setId(documentReference.getId());

                        Toast.makeText(this,
                                "Équipe créée avec succès",
                                Toast.LENGTH_SHORT).show();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_EQUIPE, equipeToSave);
                        resultIntent.putExtra(EXTRA_IS_EDIT, false);
                        setResult(RESULT_OK, resultIntent);

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Erreur création équipe: ", e);
                        Toast.makeText(this, "Erreur: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }
}