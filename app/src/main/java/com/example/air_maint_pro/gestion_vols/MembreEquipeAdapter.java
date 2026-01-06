package com.example.air_maint_pro.gestion_vols;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;

import java.util.List;

public class MembreEquipeAdapter extends RecyclerView.Adapter<MembreEquipeAdapter.MembreViewHolder> {

    private List<MembreEquipe> membresList;

    public MembreEquipeAdapter(List<MembreEquipe> membresList) {
        this.membresList = membresList;
    }

    @NonNull
    @Override
    public MembreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_membre_detail, parent, false);
        return new MembreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MembreViewHolder holder, int position) {
        MembreEquipe membre = membresList.get(position);

        holder.tvNomMembre.setText(membre.getNomComplet());
        holder.tvRoleMembre.setText(membre.getRole());
        holder.tvMatricule.setText("Matricule: " + membre.getMatricule());

        // Indicateur de type de rôle
        if (membre.isPilote()) {
            holder.tvRoleType.setText("👨‍✈️ Pilote");
            holder.tvRoleType.setBackgroundResource(R.drawable.bg_role_pilot);
        } else if (membre.isCopilote()) {
            holder.tvRoleType.setText("👨‍✈️ Copilote");
            holder.tvRoleType.setBackgroundResource(R.drawable.bg_role_copilot);
        } else if (membre.isPersonnelCabine()) {
            holder.tvRoleType.setText("👩‍✈️ Cabine");
            holder.tvRoleType.setBackgroundResource(R.drawable.bg_role_cabin);
        } else if (membre.isTechnicien()) {
            holder.tvRoleType.setText("👨‍🔧 Technicien");
            holder.tvRoleType.setBackgroundResource(R.drawable.bg_role_tech);
        } else {
            holder.tvRoleType.setText(membre.getRole());
            holder.tvRoleType.setBackgroundResource(R.drawable.bg_role_other);
        }
    }

    @Override
    public int getItemCount() {
        return membresList.size();
    }

    static class MembreViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomMembre, tvRoleMembre, tvMatricule, tvRoleType;

        MembreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomMembre = itemView.findViewById(R.id.tvNomMembre);
            tvRoleMembre = itemView.findViewById(R.id.tvRoleMembre);
            tvMatricule = itemView.findViewById(R.id.tvMatricule);
            tvRoleType = itemView.findViewById(R.id.tvRoleType);
        }
    }
}