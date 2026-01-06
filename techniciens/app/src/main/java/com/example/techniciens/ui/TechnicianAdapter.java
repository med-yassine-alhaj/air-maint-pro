package com.example.techniciens.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techniciens.R;
import com.example.techniciens.data.Technician;
import com.example.techniciens.data.TechnicianStats;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechnicianAdapter extends RecyclerView.Adapter<TechnicianAdapter.TechnicianViewHolder> {
    public interface TechnicianActionListener {
        void onAssign(Technician technician);
        void onEdit(Technician technician);
        void onDelete(Technician technician);
    }

    private final TechnicianActionListener listener;
    private List<Technician> technicians = new ArrayList<>();
    private final Map<Long, TechnicianStats> statsByTechnician = new HashMap<>();

    public TechnicianAdapter(TechnicianActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Technician> list) {
        technicians = list == null ? new ArrayList<>() : list;
        notifyDataSetChanged();
    }

    public void setStats(List<TechnicianStats> stats) {
        statsByTechnician.clear();
        if (stats != null) {
            for (TechnicianStats stat : stats) {
                statsByTechnician.put(stat.technicianId, stat);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TechnicianViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_technician, parent, false);
        return new TechnicianViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TechnicianViewHolder holder, int position) {
        Technician technician = technicians.get(position);
        TechnicianStats stat = statsByTechnician.get(technician.id);

        holder.textName.setText(technician.name);
        holder.textSpecialty.setText("Spécialité : " + technician.specialty);
        holder.textExperience.setText("Expérience : " + technician.experienceYears + " ans");
        holder.chipAvailability.setText(technician.available ? "Disponible" : "Occupé");
        holder.chipAvailability.setChipBackgroundColorResource(
            technician.available ? R.color.brand_secondary_container : R.color.brand_surface_variant);
        holder.chipAvailability.setChipIconResource(technician.available ? android.R.drawable.checkbox_on_background : android.R.drawable.ic_delete);

        if (stat != null) {
            holder.textStats.setText("Interventions: " + stat.interventionsDone + " • Heures: " + stat.hoursWorked);
        } else {
            holder.textStats.setText("Interventions: 0 • Heures: 0");
        }

        holder.buttonAssign.setOnClickListener(v -> listener.onAssign(technician));
        holder.buttonEdit.setOnClickListener(v -> listener.onEdit(technician));
        holder.buttonDelete.setOnClickListener(v -> listener.onDelete(technician));
    }

    @Override
    public int getItemCount() {
        return technicians.size();
    }

    static class TechnicianViewHolder extends RecyclerView.ViewHolder {
        final TextView textName;
        final TextView textSpecialty;
        final TextView textExperience;
        final TextView textStats;
        final Chip chipAvailability;
        final MaterialButton buttonAssign;
        final MaterialButton buttonEdit;
        final MaterialButton buttonDelete;

        TechnicianViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textSpecialty = itemView.findViewById(R.id.textSpecialty);
            textExperience = itemView.findViewById(R.id.textExperience);
            textStats = itemView.findViewById(R.id.textStats);
            chipAvailability = itemView.findViewById(R.id.chipAvailability);
            buttonAssign = itemView.findViewById(R.id.buttonAssign);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
