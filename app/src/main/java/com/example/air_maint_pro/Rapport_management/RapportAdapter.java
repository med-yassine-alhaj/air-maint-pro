package com.example.air_maint_pro.Rapport_management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.air_maint_pro.R;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RapportAdapter extends RecyclerView.Adapter<RapportAdapter.ViewHolder> {

    public interface OnRapportClickListener {
        void onRapportClick(Rapport rapport);
    }

    public interface OnRapportActionListener {
        void onLongClick(Rapport rapport);
    }

    private List<Rapport> rapports;
    private OnRapportClickListener clickListener;
    private OnRapportActionListener actionListener;

    // Updated constructor with both listeners
    public RapportAdapter(List<Rapport> rapports,
                          OnRapportClickListener clickListener,
                          OnRapportActionListener actionListener) {
        this.rapports = rapports;
        this.clickListener = clickListener;
        this.actionListener = actionListener;
    }

    // Keep the old constructor for backward compatibility
    public RapportAdapter(List<Rapport> rapports, OnRapportActionListener actionListener) {
        this.rapports = rapports;
        this.actionListener = actionListener;
        this.clickListener = null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rapport, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rapport r = rapports.get(position);

        // Title
        holder.tvTitle.setText(r.getTitle());

        // Format date generation
        Timestamp dateGen = r.getDate_generation();
        if (dateGen != null) {
            String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)
                    .format(dateGen.toDate());
            holder.tvDate.setText(date); // Removed "Généré le: " prefix
        } else {
            holder.tvDate.setText("Date non spécifiée");
        }

        // Type
        holder.tvType.setText(r.getType());

        // Status
        holder.tvStatut.setText(r.getStatut());

        // Show period if exists
        Timestamp perDebut = r.getPer_debut();
        Timestamp perFin = r.getPer_fin();
        if (perDebut != null && perFin != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
            String periode = sdf.format(perDebut.toDate()) + " - " +
                    sdf.format(perFin.toDate());
            holder.tvPeriod.setText("Période: " + periode);
            holder.tvPeriod.setVisibility(View.VISIBLE);
        } else {
            holder.tvPeriod.setVisibility(View.GONE);
        }

        // Set up click listeners
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRapportClick(r);
            }
        });

        // Long click listener (unchanged)
        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null) {
                actionListener.onLongClick(r);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return rapports != null ? rapports.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDate, tvType, tvStatut, tvPeriod;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvRapportTitle);
            tvDate = v.findViewById(R.id.tvRapportDate);
            tvType = v.findViewById(R.id.tvRapportType);
            tvStatut = v.findViewById(R.id.tvRapportStatut);
            tvPeriod = v.findViewById(R.id.tvRapportPeriod);
        }
    }
}