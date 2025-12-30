package com.example.air_maint_pro;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TechnicianAdapter extends RecyclerView.Adapter<TechnicianAdapter.ViewHolder> {

    private List<Technician> technicianList;
    private OnTechnicianClickListener listener;

    public interface OnTechnicianClickListener {
        void onTechnicianClick(Technician technician);
        void onTechnicianLongClick(Technician technician);
    }

    public TechnicianAdapter(List<Technician> technicianList, OnTechnicianClickListener listener) {
        this.technicianList = technicianList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_technician, parent, false);
            return new ViewHolder(view);
        } catch (Exception e) {
            // Retourner une vue vide en cas d'erreur
            return new ViewHolder(new View(parent.getContext()));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            if (technicianList == null || position >= technicianList.size()) {
                return;
            }

            Technician technician = technicianList.get(position);
            if (technician == null) {
                return;
            }

            // Nom complet
            if (holder.nameTextView != null) {
                String fullName = technician.getFullName();
                holder.nameTextView.setText(fullName != null ? fullName : "Nom inconnu");
            }

            // Email
            if (holder.emailTextView != null && technician.getEmail() != null) {
                holder.emailTextView.setText("📧 " + technician.getEmail());
            }

            // Téléphone
            if (holder.phoneTextView != null && technician.getPhone() != null) {
                holder.phoneTextView.setText("📞 " + technician.getPhone());
            }

            // Département
            if (holder.departmentTextView != null && technician.getDepartment() != null) {
                holder.departmentTextView.setText("🏢 " + technician.getDepartment());
            }

            // Statut
            if (holder.statusTextView != null) {
                if (technician.isActive()) {
                    holder.statusTextView.setText("✅ Actif");
                    holder.statusTextView.setBackgroundColor(Color.parseColor("#4CAF50"));
                } else {
                    holder.statusTextView.setText("❌ Inactif");
                    holder.statusTextView.setBackgroundColor(Color.parseColor("#F44336"));
                }
            }

            // Clic simple pour modifier
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onTechnicianClick(technician);
                    }
                }
            });

            // Clic long pour supprimer
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        listener.onTechnicianLongClick(technician);
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return technicianList != null ? technicianList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, emailTextView, phoneTextView,
                departmentTextView, statusTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                nameTextView = itemView.findViewById(R.id.nameTextView);
                emailTextView = itemView.findViewById(R.id.emailTextView);
                phoneTextView = itemView.findViewById(R.id.phoneTextView);
                departmentTextView = itemView.findViewById(R.id.departmentTextView);
                statusTextView = itemView.findViewById(R.id.statusTextView);
            } catch (Exception e) {
                // Les vues peuvent être null si le layout n'est pas chargé correctement
            }
        }
    }
}