package com.example.air_maint_pro.gestion_vols;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;

import java.util.List;

public class MembreSelectionAdapter extends RecyclerView.Adapter<MembreSelectionAdapter.MembreViewHolder> {

    private List<MembreEquipage> membresList;
    private OnMembreClickListener listener;

    public interface OnMembreClickListener {
        void onMembreClick(MembreEquipage membre);
    }

    public MembreSelectionAdapter(List<MembreEquipage> membresList, OnMembreClickListener listener) {
        this.membresList = membresList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MembreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_membre_selectable, parent, false);
        return new MembreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MembreViewHolder holder, int position) {
        MembreEquipage membre = membresList.get(position);

        holder.tvNomMembre.setText(membre.getNomComplet());
        holder.tvRoleMembre.setText(membre.getRole() + " (" + membre.getMatricule() + ")");
        holder.tvEmailMembre.setText(membre.getEmail());

        // Indicateur de disponibilité
        if (membre.isDisponible()) {
            holder.tvDisponible.setText("Disponible");
            holder.tvDisponible.setTextColor(holder.itemView.getContext()
                    .getColor(R.color.status_done));
        } else {
            holder.tvDisponible.setText("Indisponible");
            holder.tvDisponible.setTextColor(holder.itemView.getContext()
                    .getColor(R.color.red));
        }

        // Clic sur l'item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMembreClick(membre);
            }
        });
    }

    @Override
    public int getItemCount() {
        return membresList.size();
    }

    static class MembreViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomMembre, tvRoleMembre, tvEmailMembre, tvDisponible;

        MembreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomMembre = itemView.findViewById(R.id.tvNomMembre);
            tvRoleMembre = itemView.findViewById(R.id.tvRoleMembre);
            tvEmailMembre = itemView.findViewById(R.id.tvEmailMembre);
            tvDisponible = itemView.findViewById(R.id.tvDisponible);
        }
    }
}