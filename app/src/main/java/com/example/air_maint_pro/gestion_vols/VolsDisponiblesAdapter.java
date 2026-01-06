package com.example.air_maint_pro.gestion_vols;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VolsDisponiblesAdapter extends RecyclerView.Adapter<VolsDisponiblesAdapter.VolViewHolder> {

    private List<Vol> volsList;
    private OnVolClickListener listener;

    public interface OnVolClickListener {
        void onVolClick(Vol vol);
    }

    public VolsDisponiblesAdapter(List<Vol> volsList, OnVolClickListener listener) {
        this.volsList = volsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vol_assignable, parent, false);
        return new VolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VolViewHolder holder, int position) {
        Vol vol = volsList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);

        holder.tvNumeroVol.setText(vol.getNumeroVol());
        holder.tvItineraire.setText(vol.getVilleDepart() + " → " + vol.getVilleArrivee());

        if (vol.getDateDepart() != null) {
            holder.tvDateDepart.setText("Départ: " + sdf.format(vol.getDateDepart()));
        }

        holder.tvAvion.setText("Avion: " + vol.getAvionMatricule());
        holder.tvStatut.setText(vol.getStatut());

        // Clic sur l'item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVolClick(vol);
            }
        });
    }

    @Override
    public int getItemCount() {
        return volsList.size();
    }

    static class VolViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumeroVol, tvItineraire, tvDateDepart, tvAvion, tvStatut;

        VolViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumeroVol = itemView.findViewById(R.id.tvNumeroVol);
            tvItineraire = itemView.findViewById(R.id.tvItineraire);
            tvDateDepart = itemView.findViewById(R.id.tvDateDepart);
            tvAvion = itemView.findViewById(R.id.tvAvion);
            tvStatut = itemView.findViewById(R.id.tvStatut);
        }
    }
}