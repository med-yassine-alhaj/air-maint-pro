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

public class EquipeAdapter extends RecyclerView.Adapter<EquipeAdapter.EquipeViewHolder> {

    private List<Equipe> equipeList;
    private Context context;
    private OnEquipeClickListener listener;

    public interface OnEquipeClickListener {
        void onEquipeClick(Equipe equipe);
        void onEquipeEdit(Equipe equipe);
        void onEquipeDelete(Equipe equipe);
        void onEquipeAssignerVol(Equipe equipe);
    }

    public EquipeAdapter(List<Equipe> equipeList,
                         Context context,
                         OnEquipeClickListener listener) {
        this.equipeList = equipeList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EquipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipe, parent, false);
        return new EquipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipeViewHolder holder, int position) {
        Equipe equipe = equipeList.get(position);

        holder.tvNomEquipe.setText(equipe.getNomComplet());
        holder.tvDescription.setText(equipe.getDescription());
        holder.tvMembresCount.setText(equipe.getNombreMembres() + " membres");

        // Statut avec couleur
        holder.tvStatut.setText(equipe.getStatut());
        switch (equipe.getStatut()) {
            case "Disponible":
                holder.tvStatut.setTextColor(context.getColor(R.color.status_done));
                holder.tvStatut.setBackgroundResource(R.drawable.bg_status_available);
                break;
            case "En mission":
                holder.tvStatut.setTextColor(context.getColor(R.color.orange));
                holder.tvStatut.setBackgroundResource(R.drawable.bg_status_mission);
                break;
            case "En repos":
                holder.tvStatut.setTextColor(context.getColor(R.color.blue_dark));
                holder.tvStatut.setBackgroundResource(R.drawable.bg_status_rest);
                break;
            case "Indisponible":
                holder.tvStatut.setTextColor(context.getColor(R.color.red));
                holder.tvStatut.setBackgroundResource(R.drawable.bg_status_unavailable);
                break;
        }

        // Vol assigné
        if (equipe.aUnVolAssigné()) {
            holder.tvVolAssigné.setText("Vol: " + equipe.getVolAssignéNumero());
            holder.tvVolAssigné.setVisibility(View.VISIBLE);
        } else {
            holder.tvVolAssigné.setVisibility(View.GONE);
        }

        // Composition
        if (equipe.aCompositionMinimale()) {
            holder.tvComposition.setText("✓ Composition OK");
            holder.tvComposition.setTextColor(context.getColor(R.color.status_done));
        } else {
            holder.tvComposition.setText("⚠ Composition incomplète");
            holder.tvComposition.setTextColor(context.getColor(R.color.orange));
        }

        // Clic sur l'item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEquipeClick(equipe);
            }
        });

        // Bouton d'édition
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEquipeEdit(equipe);
            }
        });

        // Bouton de suppression
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEquipeDelete(equipe);
            }
        });

        // Bouton assigner vol (visible seulement si disponible)
        if (equipe.isDisponible() && equipe.aCompositionMinimale()) {
            holder.btnAssignerVol.setVisibility(View.VISIBLE);
            holder.btnAssignerVol.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEquipeAssignerVol(equipe);
                }
            });
        } else {
            holder.btnAssignerVol.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return equipeList.size();
    }

    static class EquipeViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomEquipe, tvDescription, tvMembresCount, tvStatut, tvVolAssigné, tvComposition;
        ImageButton btnEdit, btnDelete, btnAssignerVol;

        EquipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomEquipe = itemView.findViewById(R.id.tvNomEquipe);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvMembresCount = itemView.findViewById(R.id.tvMembresCount);
            tvStatut = itemView.findViewById(R.id.tvStatut);
            tvVolAssigné = itemView.findViewById(R.id.tvVolAssigné);
            tvComposition = itemView.findViewById(R.id.tvComposition);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnAssignerVol = itemView.findViewById(R.id.btnAssignerVol);
        }
    }
}