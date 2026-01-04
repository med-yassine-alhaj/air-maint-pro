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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VolAdapter extends RecyclerView.Adapter<VolAdapter.VolViewHolder> {

    private List<Vol> volList;
    private Context context;
    private OnVolClickListener listener;

    public interface OnVolClickListener {
        void onVolClick(Vol vol);
        void onVolEdit(Vol vol);
        void onVolDelete(Vol vol);
    }

    public VolAdapter(List<Vol> volList, Context context, OnVolClickListener listener) {
        this.volList = volList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vol, parent, false);
        return new VolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VolViewHolder holder, int position) {
        Vol vol = volList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);

        holder.tvNumeroVol.setText(vol.getNumeroVol());
        holder.tvItineraire.setText(vol.getVilleDepart() + " → " + vol.getVilleArrivee());

        if (vol.getDateDepart() != null) {
            holder.tvDateDepart.setText("Départ: " + sdf.format(vol.getDateDepart()));
        }

        holder.tvAvion.setText("Avion: " + vol.getAvionMatricule());
        holder.tvStatut.setText(vol.getStatut());

        // Gérer la couleur du statut
        switch (vol.getStatut()) {
            case "Planifié":
                holder.tvStatut.setTextColor(context.getColor(R.color.blue_dark));
                break;
            case "En cours":
                holder.tvStatut.setTextColor(context.getColor(R.color.status_done));
                break;
            case "Terminé":
                holder.tvStatut.setTextColor(context.getColor(R.color.gray));
                break;
            case "Annulé":
                holder.tvStatut.setTextColor(context.getColor(R.color.red));
                break;
            case "Retardé":
                holder.tvStatut.setTextColor(context.getColor(R.color.orange));
                break;
        }

        // Clic sur l'item pour voir les détails
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVolClick(vol);
            }
        });

        // Bouton d'édition
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVolEdit(vol);
            }
        });

        // Bouton de suppression
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVolDelete(vol);
            }
        });
    }

    @Override
    public int getItemCount() {
        return volList.size();
    }

    static class VolViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumeroVol;
        TextView tvItineraire;
        TextView tvDateDepart;
        TextView tvAvion;
        TextView tvStatut;
        ImageButton btnEdit;
        ImageButton btnDelete;

        VolViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumeroVol = itemView.findViewById(R.id.tvNumeroVol);
            tvItineraire = itemView.findViewById(R.id.tvItineraire);
            tvDateDepart = itemView.findViewById(R.id.tvDateDepart);
            tvAvion = itemView.findViewById(R.id.tvAvion);
            tvStatut = itemView.findViewById(R.id.tvStatut);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}