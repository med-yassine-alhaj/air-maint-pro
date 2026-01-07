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

public class EquipageListFragment extends Fragment implements EquipageAdapter.OnEquipageClickListener {

    private RecyclerView recyclerView;
    private EquipageAdapter equipageAdapter;
    private List<MembreEquipage> equipageList;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> addEditEquipageLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addEditEquipageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        MembreEquipage updatedEquipage = (MembreEquipage) data.getSerializableExtra(
                                AddEditEquipageActivity.EXTRA_EQUIPAGE
                        );
                        boolean isEdit = data.getBooleanExtra(
                                AddEditEquipageActivity.EXTRA_IS_EDIT, false
                        );

                        if (isEdit) {
                            updateEquipageInList(updatedEquipage);
                        } else {
                            addEquipageToList(updatedEquipage);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_equipage_list, container, false);

        db = FirebaseFirestore.getInstance();
        equipageList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerViewEquipage);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        equipageAdapter = new EquipageAdapter(equipageList, getContext(), this);
        recyclerView.setAdapter(equipageAdapter);

        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddEquipage);
        fabAdd.setOnClickListener(v -> openAddEquipageActivity());

        loadEquipage();

        return view;
    }

    private void loadEquipage() {
        db.collection("equipage")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        equipageList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MembreEquipage membre = document.toObject(MembreEquipage.class);
                            membre.setId(document.getId());
                            equipageList.add(membre);
                        }
                        equipageAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Erreur de chargement des équipages",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openAddEquipageActivity() {
        Intent intent = new Intent(getContext(), AddEditEquipageActivity.class);
        addEditEquipageLauncher.launch(intent);
    }

    private void openEditEquipageActivity(MembreEquipage equipage) {
        Intent intent = new Intent(getContext(), AddEditEquipageActivity.class);
        intent.putExtra("EQUIPAGE_ID", equipage.getId());
        addEditEquipageLauncher.launch(intent);
    }

    private void updateEquipageInList(MembreEquipage updatedEquipage) {
        for (int i = 0; i < equipageList.size(); i++) {
            if (equipageList.get(i).getId().equals(updatedEquipage.getId())) {
                equipageList.set(i, updatedEquipage);
                equipageAdapter.notifyItemChanged(i);
                Toast.makeText(getContext(), "Équipage mis à jour", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void addEquipageToList(MembreEquipage newEquipage) {
        equipageList.add(0, newEquipage);
        equipageAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
        Toast.makeText(getContext(), "Équipage ajouté", Toast.LENGTH_SHORT).show();
    }

    private void deleteEquipage(MembreEquipage equipage) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmer la suppression")
                .setMessage("Voulez-vous vraiment supprimer " + equipage.getNomComplet() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteEquipageFromFirestore(equipage))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteEquipageFromFirestore(MembreEquipage equipage) {
        db.collection("equipage")
                .document(equipage.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    for (int i = 0; i < equipageList.size(); i++) {
                        if (equipageList.get(i).getId().equals(equipage.getId())) {
                            equipageList.remove(i);
                            equipageAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                    Toast.makeText(getContext(), "Équipage supprimé", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEquipageClick(MembreEquipage equipage) {
        Toast.makeText(getContext(), "Détails de: " + equipage.getNomComplet(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEquipageEdit(MembreEquipage equipage) {
        openEditEquipageActivity(equipage);
    }

    @Override
    public void onEquipageDelete(MembreEquipage equipage) {
        deleteEquipage(equipage);
    }
}