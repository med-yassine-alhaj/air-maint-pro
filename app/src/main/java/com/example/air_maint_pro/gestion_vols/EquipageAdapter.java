package com.example.air_maint_pro.gestion_vols;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;

import java.util.List;

public class EquipageAdapter extends RecyclerView.Adapter<EquipageAdapter.EquipageViewHolder> {

    private List<MembreEquipage> equipageList;
    private Context context;
    private OnEquipageClickListener listener;

    public interface OnEquipageClickListener {
        void onEquipageClick(MembreEquipage equipage);
        void onEquipageEdit(MembreEquipage equipage);
        void onEquipageDelete(MembreEquipage equipage);
    }

    public EquipageAdapter(List<MembreEquipage> equipageList,
                           Context context,
                           OnEquipageClickListener listener) {
        this.equipageList = equipageList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EquipageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipage, parent, false);
        return new EquipageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipageViewHolder holder, int position) {
        MembreEquipage membre = equipageList.get(position);

        holder.tvNomComplet.setText(membre.getNomComplet());
        holder.tvRole.setText(membre.getRole());
        holder.tvMatricule.setText("Matricule: " + membre.getMatricule());
        holder.tvEmail.setText(membre.getEmail());

        // Indicateur de disponibilité
        if (membre.isDisponible()) {
            holder.tvDisponible.setText("Disponible");
            holder.tvDisponible.setTextColor(context.getColor(R.color.status_done));
        } else {
            holder.tvDisponible.setText("Indisponible");
            holder.tvDisponible.setTextColor(context.getColor(R.color.red));
        }

        // Clic sur l'item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEquipageClick(membre);
            }
        });

        // Bouton d'édition
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEquipageEdit(membre);
            }
        });

        // Bouton de suppression
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEquipageDelete(membre);
            }
        });
    }

    @Override
    public int getItemCount() {
        return equipageList.size();
    }

    static class EquipageViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomComplet, tvRole, tvMatricule, tvEmail, tvDisponible;
        ImageButton btnEdit, btnDelete;

        EquipageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomComplet = itemView.findViewById(R.id.tvNomComplet);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvMatricule = itemView.findViewById(R.id.tvMatricule);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvDisponible = itemView.findViewById(R.id.tvDisponible);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}