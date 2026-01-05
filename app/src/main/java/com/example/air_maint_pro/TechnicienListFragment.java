package com.example.air_maint_pro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TechnicienListFragment extends Fragment {

    private TextView tvWelcome;
    private Button btnCreateTechnicien;
    private RecyclerView rvTechnicien;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private List<Technicien> technicienList = new ArrayList<>();
    private TechnicienAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_technicien_list, container, false);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        tvWelcome = view.findViewById(R.id.tvWelcome);
        btnCreateTechnicien = view.findViewById(R.id.btnCreateTechnicien);
        rvTechnicien = view.findViewById(R.id.rvTechnicien);

        // RecyclerView
        rvTechnicien.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TechnicienAdapter(technicienList, this::showTechnicienOptions);
        rvTechnicien.setAdapter(adapter);

        loadSupervisorInfo();
        loadTechnicien();

        // Bouton créer Technicien
        btnCreateTechnicien.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), TechnicienActivity.class));
        });



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Rafraîchir la liste quand on revient
        loadTechnicien();
    }

    private void loadSupervisorInfo() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("Users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        return;
                    }

                    String nom = doc.getString("nom");
                    String prenom = doc.getString("prenom");

                    if (nom != null && prenom != null) {
                        tvWelcome.setText("Bienvenue " + nom + " " + prenom);
                    } else if (nom != null) {
                        tvWelcome.setText("Bienvenue " + nom);
                    } else {
                        tvWelcome.setText("Bienvenue superviseur");
                    }
                })
                .addOnFailureListener(e -> {
                    tvWelcome.setText("Bienvenue");
                });
    }

    private void loadTechnicien() {
        db.collection("Users")
                .whereEqualTo("role", "technicien")
                .get()
                .addOnSuccessListener(query -> {
                    technicienList.clear();
                    for (DocumentSnapshot d : query) {
                        Technicien e = d.toObject(Technicien.class);
                        if (e != null) {
                            e.id = d.getId();
                            technicienList.add(e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur de chargement des techniciens", Toast.LENGTH_SHORT).show();
                });
    }

    private void showTechnicienOptions(Technicien e) {
        String[] options = {"Voir détails", "Modifier", "Supprimer"};

        new AlertDialog.Builder(requireContext())
                .setTitle(e.getFullName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showTechnicienDetails(e);
                            break;
                        case 1:
                            updateTechnicien(e);
                            break;
                        case 2:
                            deleteTechnicien(e);
                            break;
                    }
                })
                .show();
    }

    private void showTechnicienDetails(Technicien e) {
        String details = "Nom complet: " + e.getFullName() + "\n" +
                "Email: " + e.email + "\n" +
                "Âge: " + e.age + " ans\n" +
                "Département: " + e.departement + "\n" +
                "Rôle: " + e.role;

        new AlertDialog.Builder(requireContext())
                .setTitle("Détails du Technicien")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    // Modifier la méthode updateTechnicien dans TechnicienListFragment
    private void updateTechnicien(Technicien e) {
        Intent intent = new Intent(getContext(), EditTechnicienActivity.class);
        intent.putExtra("technicienId", e.id);
        intent.putExtra("nom", e.nom);
        intent.putExtra("prenom", e.prenom);
        intent.putExtra("email", e.email);
        intent.putExtra("age", e.age);
        intent.putExtra("departement", e.departement);
        startActivityForResult(intent, 1);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            // Rafraîchir la liste
            loadTechnicien();
            Toast.makeText(getContext(), "Technicien modifié avec succès", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTechnicien(Technicien e) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation de suppression")
                .setMessage("Voulez-vous vraiment supprimer " + e.getFullName() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    db.collection("Users")
                            .document(e.id)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Technicien supprimé", Toast.LENGTH_SHORT).show();
                                loadTechnicien();
                            })
                            .addOnFailureListener(err -> {
                                Toast.makeText(getContext(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}





