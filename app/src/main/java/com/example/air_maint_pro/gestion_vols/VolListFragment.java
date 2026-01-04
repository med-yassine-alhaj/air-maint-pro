package com.example.air_maint_pro.gestion_vols;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class VolListFragment extends Fragment implements VolAdapter.OnVolClickListener {

    private RecyclerView recyclerView;
    private VolAdapter volAdapter;
    private List<Vol> volList;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> addEditVolLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialiser le ActivityResultLauncher
        addEditVolLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Vol updatedVol = (Vol) data.getSerializableExtra(AddEditVolActivity.EXTRA_VOL);
                        boolean isEdit = data.getBooleanExtra(AddEditVolActivity.EXTRA_IS_EDIT, false);

                        if (isEdit) {
                            // Mise à jour d'un vol existant
                            updateVolInList(updatedVol);
                        } else {
                            // Ajout d'un nouveau vol
                            addVolToList(updatedVol);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vol_list, container, false);

        db = FirebaseFirestore.getInstance();
        volList = new ArrayList<>();

        // Initialiser RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewVols);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        volAdapter = new VolAdapter(volList, getContext(), this);
        recyclerView.setAdapter(volAdapter);

        // Bouton d'ajout
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddVol);
        fabAdd.setOnClickListener(v -> {
            openAddVolActivity();
        });

        // Charger les vols
        loadVols();

        return view;
    }

    private void loadVols() {
        db.collection("vols")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        volList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Vol vol = document.toObject(Vol.class);
                            vol.setId(document.getId());
                            volList.add(vol);
                        }
                        volAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Erreur de chargement des vols",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openAddVolActivity() {
        Intent intent = new Intent(getContext(), AddEditVolActivity.class);
        addEditVolLauncher.launch(intent);
    }

    private void openEditVolActivity(Vol vol) {
        Intent intent = new Intent(getContext(), AddEditVolActivity.class);
        intent.putExtra("VOL_ID", vol.getId());
        addEditVolLauncher.launch(intent);
    }

    // Mettre à jour un vol dans la liste
    private void updateVolInList(Vol updatedVol) {
        for (int i = 0; i < volList.size(); i++) {
            if (volList.get(i).getId().equals(updatedVol.getId())) {
                volList.set(i, updatedVol);
                volAdapter.notifyItemChanged(i);
                Toast.makeText(getContext(), "Vol mis à jour", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    // Ajouter un vol à la liste
    private void addVolToList(Vol newVol) {
        volList.add(0, newVol); // Ajouter au début
        volAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0); // Scroller vers le haut
        Toast.makeText(getContext(), "Vol ajouté", Toast.LENGTH_SHORT).show();
    }

    // Supprimer un vol
    private void deleteVol(Vol vol) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmer la suppression")
                .setMessage("Voulez-vous vraiment supprimer le vol " + vol.getNumeroVol() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    deleteVolFromFirestore(vol);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteVolFromFirestore(Vol vol) {
        db.collection("vols")
                .document(vol.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Supprimer de la liste locale
                    for (int i = 0; i < volList.size(); i++) {
                        if (volList.get(i).getId().equals(vol.getId())) {
                            volList.remove(i);
                            volAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                    Toast.makeText(getContext(), "Vol supprimé", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Implémentation de l'interface OnVolClickListener
    @Override
    public void onVolClick(Vol vol) {
        // Afficher les détails du vol (vous pouvez créer une activité/fragment pour ça)
        Toast.makeText(getContext(), "Détails du vol: " + vol.getNumeroVol(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVolEdit(Vol vol) {
        openEditVolActivity(vol);
    }

    @Override
    public void onVolDelete(Vol vol) {
        deleteVol(vol);
    }
}