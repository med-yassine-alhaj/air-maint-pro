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

public class EquipeListFragment extends Fragment implements EquipeAdapter.OnEquipeClickListener {

    private RecyclerView recyclerView;
    private EquipeAdapter equipeAdapter;
    private List<Equipe> equipeList;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> addEditEquipeLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addEditEquipeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Equipe updatedEquipe = (Equipe) data.getSerializableExtra(
                                AddEditEquipeActivity.EXTRA_EQUIPE
                        );
                        boolean isEdit = data.getBooleanExtra(
                                AddEditEquipeActivity.EXTRA_IS_EDIT, false
                        );

                        if (isEdit) {
                            updateEquipeInList(updatedEquipe);
                        } else {
                            addEquipeToList(updatedEquipe);
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
        View view = inflater.inflate(R.layout.fragment_equipe_list, container, false);

        db = FirebaseFirestore.getInstance();
        equipeList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerViewEquipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        equipeAdapter = new EquipeAdapter(equipeList, getContext(), this);
        recyclerView.setAdapter(equipeAdapter);

        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddEquipe);
        fabAdd.setOnClickListener(v -> openAddEquipeActivity());

        loadEquipes();

        return view;
    }

    private void loadEquipes() {
        db.collection("equipes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        equipeList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Equipe equipe = document.toObject(Equipe.class);
                            equipe.setId(document.getId());
                            equipeList.add(equipe);
                        }
                        equipeAdapter.notifyDataSetChanged();

                        // Afficher message si liste vide
                        if (equipeList.isEmpty()) {
                            Toast.makeText(getContext(), "Aucune équipe trouvée",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Erreur de chargement des équipes",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openAddEquipeActivity() {
        Intent intent = new Intent(getContext(), AddEditEquipeActivity.class);
        addEditEquipeLauncher.launch(intent);
    }

    private void openEditEquipeActivity(Equipe equipe) {
        Intent intent = new Intent(getContext(), AddEditEquipeActivity.class);
        intent.putExtra("EQUIPE_ID", equipe.getId());
        addEditEquipeLauncher.launch(intent);
    }

    private void openDetailEquipeActivity(Equipe equipe) {
        Intent intent = new Intent(getContext(), DetailEquipeActivity.class);
        intent.putExtra("EQUIPE_ID", equipe.getId());
        startActivity(intent);
    }

    private void updateEquipeInList(Equipe updatedEquipe) {
        for (int i = 0; i < equipeList.size(); i++) {
            if (equipeList.get(i).getId().equals(updatedEquipe.getId())) {
                equipeList.set(i, updatedEquipe);
                equipeAdapter.notifyItemChanged(i);
                Toast.makeText(getContext(), "Équipe mise à jour", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void addEquipeToList(Equipe newEquipe) {
        equipeList.add(0, newEquipe);
        equipeAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
        Toast.makeText(getContext(), "Équipe ajoutée", Toast.LENGTH_SHORT).show();
    }

    private void deleteEquipe(Equipe equipe) {
        // Vérifier si l'équipe est assignée à un vol
        if (equipe.aUnVolAssigné()) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Impossible de supprimer")
                    .setMessage("Cette équipe est actuellement assignée au vol " +
                            equipe.getVolAssignéNumero() +
                            ". Libérez-la d'abord du vol.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Confirmer la suppression")
                .setMessage("Voulez-vous vraiment supprimer l'équipe " +
                        equipe.getNomComplet() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteEquipeFromFirestore(equipe))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteEquipeFromFirestore(Equipe equipe) {
        db.collection("equipes")
                .document(equipe.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    for (int i = 0; i < equipeList.size(); i++) {
                        if (equipeList.get(i).getId().equals(equipe.getId())) {
                            equipeList.remove(i);
                            equipeAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                    Toast.makeText(getContext(), "Équipe supprimée", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Implémentation de l'interface OnEquipeClickListener
    @Override
    public void onEquipeClick(Equipe equipe) {
        openDetailEquipeActivity(equipe);
    }

    @Override
    public void onEquipeEdit(Equipe equipe) {
        openEditEquipeActivity(equipe);
    }

    @Override
    public void onEquipeDelete(Equipe equipe) {
        deleteEquipe(equipe);
    }

    @Override
    public void onEquipeAssignerVol(Equipe equipe) {
        // À implémenter plus tard : ouvrir la sélection de vol
        Toast.makeText(getContext(), "Assigner un vol à " + equipe.getNom(),
                Toast.LENGTH_SHORT).show();
    }
}