package com.example.techniciens.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techniciens.R;
import com.example.techniciens.data.AssignmentWithDetails;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlanningAdapter extends RecyclerView.Adapter<PlanningAdapter.PlanningViewHolder> {
    private final List<AssignmentWithDetails> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public void submitList(List<AssignmentWithDetails> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlanningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_planning, parent, false);
        return new PlanningViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PlanningViewHolder holder, int position) {
        AssignmentWithDetails item = items.get(position);
        holder.textIntervention.setText(item.intervention != null ? item.intervention.title : "Intervention");
        holder.textTechnician.setText(item.technician != null ? item.technician.name : "Technicien");

        if (item.intervention != null) {
            String when = dateFormat.format(new Date(item.intervention.scheduledAt));
            holder.textSchedule.setText(when + " • " + item.intervention.durationHours + "h");
        } else {
            holder.textSchedule.setText("Durée inconnue");
        }

        String status = item.assignment.status == null ? "" : item.assignment.status;
        holder.chipStatus.setText(status.isEmpty() ? "Statut" : status);

        int color;
        switch (status) {
            case "done":
                color = R.color.brand_secondary_container;
                break;
            case "planned":
                color = R.color.brand_primary_container;
                break;
            case "canceled":
                color = R.color.brand_surface_variant;
                break;
            default:
                color = R.color.brand_surface_variant;
        }
        holder.chipStatus.setChipBackgroundColorResource(color);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PlanningViewHolder extends RecyclerView.ViewHolder {
        final TextView textIntervention;
        final TextView textTechnician;
        final TextView textSchedule;
        final Chip chipStatus;

        PlanningViewHolder(@NonNull View itemView) {
            super(itemView);
            textIntervention = itemView.findViewById(R.id.textIntervention);
            textTechnician = itemView.findViewById(R.id.textTechnician);
            textSchedule = itemView.findViewById(R.id.textSchedule);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
    }
}
