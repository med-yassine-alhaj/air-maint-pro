package com.example.air_maint_pro.gestion_avion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TechnicienAvionsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private AvionAdapter adapter;
    private List<Avion> avionList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_technicien_avions_list, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews(view);
        setupRecyclerView();
        loadAvionsDuTechnicien();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void setupRecyclerView() {
        adapter = new AvionAdapter(avionList, new AvionAdapter.OnAvionClickListener() {
            @Override
            public void onAvionClick(Avion avion) {
                // Ouvrir les détails complets
                openAvionDetails(avion);
            }

            @Override
            public void onQrCodeClick(Avion avion) {
                // Générer QR Code
                generateQrCode(avion);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadAvionsDuTechnicien() {
        String technicienId = auth.getCurrentUser().getUid();

        db.collection("Avions")
                .whereEqualTo("technicienAssignId", technicienId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        avionList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Avion avion = document.toObject(Avion.class);
                            avion.id = document.getId();
                            avionList.add(avion);
                        }

                        adapter.updateData(avionList);

                        if (avionList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            tvEmpty.setText("Aucun avion assigné pour le moment");
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void openAvionDetails(Avion avion) {
        Intent intent = new Intent(getContext(), AvionDetailsActivity.class);
        intent.putExtra("avion_id", avion.id);
        intent.putExtra("matricule", avion.matricule);
        startActivity(intent);
    }

    private void generateQrCode(Avion avion) {
        Intent intent = new Intent(getContext(), QrCodeActivity.class);
        intent.putExtra("avion_id", avion.id);
        intent.putExtra("matricule", avion.matricule);
        startActivity(intent);
    }
}