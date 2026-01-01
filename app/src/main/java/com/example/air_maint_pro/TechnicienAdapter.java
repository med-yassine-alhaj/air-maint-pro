package com.example.air_maint_pro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TechnicienAdapter extends RecyclerView.Adapter<TechnicienAdapter.ViewHolder> {

    public interface OnTechnicienActionListener {
        void onLongClick(Technicien technicien);
    }

    private List<Technicien> techniciens;
    private OnTechnicienActionListener listener;

    public TechnicienAdapter(List<Technicien> techniciens, OnTechnicienActionListener listener) {
        this.techniciens = techniciens;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_technicien, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Technicien e = techniciens.get(position);

        // Affichage du nom complet
        holder.tvFullName.setText(e.getFullName());

        // Email
        holder.tvEmail.setText(e.email);

        // Âge et département
        holder.tvAgeDepartement.setText(e.age + " ans • " + e.departement);

        // Date de création formatée
        if (e.createdAt > 0) {
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
                    .format(new Date(e.createdAt));
            holder.tvDate.setText("Créé le: " + date);
        }

        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(e);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return techniciens.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvFullName, tvEmail, tvAgeDepartement, tvDate;

        ViewHolder(View v) {
            super(v);
            tvFullName = v.findViewById(R.id.tvFullName);
            tvEmail = v.findViewById(R.id.tvEmail);
            tvAgeDepartement = v.findViewById(R.id.tvAgeDepartement);
            tvDate = v.findViewById(R.id.tvDate);
        }
    }
}