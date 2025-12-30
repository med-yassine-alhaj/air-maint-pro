package com.example.air_maint_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TechniciansListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TechnicianAdapter adapter;
    private List<Technician> technicianList;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAdd;
    private TextView emptyTextView;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technicians_list);

        Log.d("LIST_ACTIVITY", "Activity créée");

        // Initialiser Firebase
        db = FirebaseFirestore.getInstance();
        technicianList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupListeners();

        // Charger les techniciens
        loadTechnicians();
    }

    private void initViews() {
        Log.d("LIST_ACTIVITY", "Initialisation des vues");

        try {
            recyclerView = findViewById(R.id.recyclerView);
            if (recyclerView == null) {
                Log.e("LIST_ACTIVITY", "recyclerView est null!");
                Toast.makeText(this, "Erreur: recyclerView non trouvé", Toast.LENGTH_LONG).show();
                return;
            }

            progressBar = findViewById(R.id.progressBar);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            fabAdd = findViewById(R.id.fabAdd);
            emptyTextView = findViewById(R.id.emptyTextView);

            Log.d("LIST_ACTIVITY", "Vues initialisées avec succès");
        } catch (Exception e) {
            Log.e("LIST_ACTIVITY", "Erreur initViews: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur d'initialisation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclerView() {
        try {
            adapter = new TechnicianAdapter(technicianList, new TechnicianAdapter.OnTechnicianClickListener() {
                @Override
                public void onTechnicianClick(Technician technician) {
                    // MODIFIER le technicien
                    Log.d("LIST_ACTIVITY", "Clic sur technicien: " + technician.getFullName());
                    openEditTechnician(technician);
                }

                @Override
                public void onTechnicianLongClick(Technician technician) {
                    // SUPPRIMER le technicien
                    Log.d("LIST_ACTIVITY", "Clic long sur technicien: " + technician.getFullName());
                    showDeleteDialog(technician);
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            Log.d("LIST_ACTIVITY", "RecyclerView configuré");
        } catch (Exception e) {
            Log.e("LIST_ACTIVITY", "Erreur setupRecyclerView: " + e.getMessage(), e);
        }
    }

    private void setupListeners() {
        // Refresh on pull
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadTechnicians();
                }
            });
        }

        // Add new technician
        if (fabAdd != null) {
            fabAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("LIST_ACTIVITY", "Bouton Ajouter cliqué");
                    openAddTechnician();
                }
            });
        }
    }

    private void loadTechnicians() {
        Log.d("LIST_ACTIVITY", "Chargement des techniciens...");

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (emptyTextView != null) {
            emptyTextView.setVisibility(View.GONE);
        }

        db.collection("technicians")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        if (task.isSuccessful()) {
                            Log.d("LIST_ACTIVITY", "Succès! " + task.getResult().size() + " documents trouvés");

                            technicianList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Log.d("LIST_ACTIVITY", "Document: " + document.getId());

                                    // Méthode 1: Convertir en objet Technician
                                    Technician technician = document.toObject(Technician.class);
                                    if (technician != null) {
                                        technician.setId(document.getId()); // S'assurer que l'ID est défini
                                        technicianList.add(technician);
                                    } else {
                                        // Méthode 2: Créer manuellement
                                        technician = createTechnicianFromDocument(document);
                                        if (technician != null) {
                                            technicianList.add(technician);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("LIST_ACTIVITY", "Erreur conversion document: " + e.getMessage(), e);
                                }
                            }

                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }

                            // Afficher message si vide
                            if (technicianList.isEmpty()) {
                                Log.d("LIST_ACTIVITY", "Liste vide");
                                if (emptyTextView != null) {
                                    emptyTextView.setVisibility(View.VISIBLE);
                                    emptyTextView.setText("Aucun technicien trouvé");
                                }
                            } else {
                                Log.d("LIST_ACTIVITY", "Liste chargée: " + technicianList.size() + " techniciens");
                                if (emptyTextView != null) {
                                    emptyTextView.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            Log.e("LIST_ACTIVITY", "Erreur Firebase: " + task.getException());
                            Toast.makeText(TechniciansListActivity.this,
                                    "❌ Erreur: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();

                            if (emptyTextView != null) {
                                emptyTextView.setVisibility(View.VISIBLE);
                                emptyTextView.setText("Erreur de chargement");
                            }
                        }
                    }
                });
    }

    private Technician createTechnicianFromDocument(QueryDocumentSnapshot document) {
        try {
            Technician technician = new Technician();
            technician.setId(document.getId());

            if (document.contains("firstName")) {
                technician.setFirstName(document.getString("firstName"));
            }
            if (document.contains("lastName")) {
                technician.setLastName(document.getString("lastName"));
            }
            if (document.contains("email")) {
                technician.setEmail(document.getString("email"));
            }
            if (document.contains("phone")) {
                technician.setPhone(document.getString("phone"));
            }
            if (document.contains("department")) {
                technician.setDepartment(document.getString("department"));
            }
            if (document.contains("role")) {
                technician.setRole(document.getString("role"));
            }
            if (document.contains("isActive")) {
                Boolean isActive = document.getBoolean("isActive");
                if (isActive != null) {
                    technician.setActive(isActive);
                }
            }

            return technician;
        } catch (Exception e) {
            Log.e("LIST_ACTIVITY", "Erreur création technician: " + e.getMessage(), e);
            return null;
        }
    }

    private void openAddTechnician() {
        try {
            Log.d("LIST_ACTIVITY", "Ouverture formulaire d'ajout");
            Intent intent = new Intent(this, AddEditTechnicianActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("LIST_ACTIVITY", "Erreur ouverture AddEdit: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openEditTechnician(Technician technician) {
        try {
            if (technician == null || technician.getId() == null) {
                Log.e("LIST_ACTIVITY", "Technician ou ID null!");
                Toast.makeText(this, "Erreur: technicien invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("LIST_ACTIVITY", "Ouverture modification pour: " + technician.getId());
            Intent intent = new Intent(this, AddEditTechnicianActivity.class);
            intent.putExtra("TECHNICIAN_ID", technician.getId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e("LIST_ACTIVITY", "Erreur ouverture modification: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteDialog(final Technician technician) {
        try {
            if (technician == null) {
                Log.e("LIST_ACTIVITY", "Technician null dans showDeleteDialog");
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Supprimer le technicien")
                    .setMessage("Voulez-vous vraiment supprimer " +
                            (technician.getFullName() != null ? technician.getFullName() : "ce technicien") +
                            " ?")
                    .setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteTechnician(technician.getId());
                        }
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        } catch (Exception e) {
            Log.e("LIST_ACTIVITY", "Erreur showDeleteDialog: " + e.getMessage(), e);
        }
    }

    private void deleteTechnician(String technicianId) {
        if (technicianId == null || technicianId.isEmpty()) {
            Log.e("LIST_ACTIVITY", "ID null pour suppression");
            Toast.makeText(this, "Erreur: ID invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        Log.d("LIST_ACTIVITY", "Suppression du technicien ID: " + technicianId);

        db.collection("technicians")
                .document(technicianId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        if (task.isSuccessful()) {
                            Log.d("LIST_ACTIVITY", "✅ Suppression réussie");
                            Toast.makeText(TechniciansListActivity.this,
                                    "✅ Technicien supprimé avec succès",
                                    Toast.LENGTH_SHORT).show();
                            loadTechnicians(); // Recharger la liste
                        } else {
                            Log.e("LIST_ACTIVITY", "❌ Erreur suppression: " + task.getException());
                            Toast.makeText(TechniciansListActivity.this,
                                    "❌ Erreur: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LIST_ACTIVITY", "onResume - Rechargement des données");
        loadTechnicians(); // Recharger quand on revient à cette activité
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("LIST_ACTIVITY", "Activity détruite");
    }
}