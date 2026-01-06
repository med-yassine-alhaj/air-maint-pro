package com.example.air_maint_pro.gestion_vols;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;

import java.util.List;

public class EquipesDisponiblesAdapter extends RecyclerView.Adapter<EquipesDisponiblesAdapter.EquipeViewHolder> {

    private List<Equipe> equipesList;
    private OnEquipeClickListener listener;

    public interface OnEquipeClickListener {
        void onEquipeClick(Equipe equipe);
    }

    public EquipesDisponiblesAdapter(List<Equipe> equipesList, OnEquipeClickListener listener) {
        this.equipesList = equipesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EquipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipe_assignable, parent, false);
        return new EquipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipeViewHolder holder, int position) {
        Equipe equipe = equipesList.get(position);

        holder.tvNomEquipe.setText(equipe.getNomComplet());
        holder.tvDescription.setText(equipe.getDescription() != null ?
                equipe.getDescription() : "Aucune description");
        holder.tvMembresCount.setText(equipe.getNombreMembres() + " membre(s)");

        // Afficher la composition
        StringBuilder composition = new StringBuilder();
        composition.append("Pilotes: ").append(equipe.getPilotes().size()).append(" ");
        composition.append("Copilotes: ").append(equipe.getCopilotes().size()).append(" ");
        composition.append("Cabine: ").append(equipe.getPersonnelCabine().size()).append(" ");
        composition.append("Tech: ").append(equipe.getTechniciens().size());
        holder.tvComposition.setText(composition.toString());

        // Clic sur l'item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEquipeClick(equipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return equipesList.size();
    }

    static class EquipeViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomEquipe, tvDescription, tvMembresCount, tvComposition;

        EquipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomEquipe = itemView.findViewById(R.id.tvNomEquipe);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvMembresCount = itemView.findViewById(R.id.tvMembresCount);
            tvComposition = itemView.findViewById(R.id.tvComposition);
        }
    }
}