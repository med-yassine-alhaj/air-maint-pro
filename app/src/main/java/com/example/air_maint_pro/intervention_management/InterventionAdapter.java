package com.example.air_maint_pro.intervention_management;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.gestion_avion.Avion;
import com.example.air_maint_pro.R;
import com.example.air_maint_pro.Technicien;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InterventionAdapter extends RecyclerView.Adapter<InterventionAdapter.InterventionViewHolder> {

    private List<Intervention> interventionList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private FirebaseFirestore db;
    private Map<String, String> avionMatriculeCache = new HashMap<>();
    private Map<String, String> technicianNameCache = new HashMap<>();
    private OnInterventionClickListener onItemClickListener;
    private OnPDFExportClickListener onPDFExportClickListener;

    public interface OnInterventionClickListener {
        void onInterventionClick(Intervention intervention);
    }

    public interface OnPDFExportClickListener {
        void onPDFExportClick(Intervention intervention);
    }

    public InterventionAdapter(List<Intervention> interventionList) {
        this.interventionList = interventionList;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setOnItemClickListener(OnInterventionClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnPDFExportClickListener(OnPDFExportClickListener listener) {
        this.onPDFExportClickListener = listener;
    }

    @NonNull
    @Override
    public InterventionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_intervention, parent, false);
        return new InterventionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InterventionViewHolder holder, int position) {
        Intervention intervention = interventionList.get(position);

        holder.textType.setText(intervention.getTypeIntervention());
        holder.textStatus.setText(intervention.getStatut());
        holder.textDuration.setText(String.format(Locale.getDefault(), "Durée: %.1fh", intervention.getDureeHeures()));

        // Format date
        if (intervention.getDateIntervention() != null) {
            holder.textDate.setText(dateFormat.format(intervention.getDateIntervention()));
        }

        // Load and display avion matricule
        String avionId = intervention.getAvionId();
        if (!avionId.isEmpty()) {
            if (avionMatriculeCache.containsKey(avionId)) {
                holder.textPlaneId.setText("Avion: " + avionMatriculeCache.get(avionId));
            } else {
                holder.textPlaneId.setText("Avion: Chargement...");
                db.collection("Avions").document(avionId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Avion avion = documentSnapshot.toObject(Avion.class);
                                if (avion != null) {
                                    String matricule = avion.getMatricule();
                                    avionMatriculeCache.put(avionId, matricule);
                                    // Update only if this holder still shows the same intervention
                                    int currentPosition = holder.getAdapterPosition();
                                    if (currentPosition != RecyclerView.NO_POSITION &&
                                            currentPosition < interventionList.size() &&
                                            interventionList.get(currentPosition).getId().equals(intervention.getId())) {
                                        holder.textPlaneId.setText("Avion: " + matricule);
                                    }
                                }
                            } else {
                                holder.textPlaneId.setText("Avion: N/A");
                            }
                        })
                        .addOnFailureListener(e -> {
                            holder.textPlaneId.setText("Avion: Erreur");
                        });
            }
        } else {
            holder.textPlaneId.setText("Avion: N/A");
        }

        // Load and display technician name
        String technicianId = intervention.getTechnicienId();
        if (!technicianId.isEmpty()) {
            if (technicianNameCache.containsKey(technicianId)) {
                holder.textTechnicianId.setText("Technicien: " + technicianNameCache.get(technicianId));
            } else {
                holder.textTechnicianId.setText("Technicien: Chargement...");
                db.collection("Users").document(technicianId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Technicien technician = documentSnapshot.toObject(Technicien.class);
                                if (technician != null) {
                                    String fullName = technician.getFullName();
                                    technicianNameCache.put(technicianId, fullName);
                                    // Update only if this holder still shows the same intervention
                                    int currentPosition = holder.getAdapterPosition();
                                    if (currentPosition != RecyclerView.NO_POSITION &&
                                            currentPosition < interventionList.size() &&
                                            interventionList.get(currentPosition).getId().equals(intervention.getId())) {
                                        holder.textTechnicianId.setText("Technicien: " + fullName);
                                    }
                                }
                            } else {
                                holder.textTechnicianId.setText("Technicien: N/A");
                            }
                        })
                        .addOnFailureListener(e -> {
                            holder.textTechnicianId.setText("Technicien: Erreur");
                        });
            }
        } else {
            holder.textTechnicianId.setText("Technicien: N/A");
        }

        // Set status color using color palette with rounded background
        int statusBgColor;
        switch (intervention.getStatut()) {
            case "Planifiée":
                statusBgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_waiting);
                break;
            case "En cours":
                statusBgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_in_progress);
                break;
            case "Terminée":
            case "Clôturée":
                statusBgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_done);
                break;
            default:
                statusBgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.muted);
                break;
        }
        // Create a drawable with rounded corners and border
        android.graphics.drawable.GradientDrawable statusBg = new android.graphics.drawable.GradientDrawable();
        statusBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        statusBg.setCornerRadius(16f);
        statusBg.setColor(statusBgColor);
        statusBg.setStroke(1, ContextCompat.getColor(holder.itemView.getContext(), R.color.border));
        holder.textStatus.setBackground(statusBg);
        holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));

        // Show PDF export button only for "Terminée" status
        boolean isTerminee = "Terminée".equals(intervention.getStatut()) || "Clôturée".equals(intervention.getStatut());
        if (holder.buttonExportPDF != null) {
            holder.buttonExportPDF.setVisibility(isTerminee ? View.VISIBLE : View.GONE);
            holder.buttonExportPDF.setOnClickListener(v -> {
                if (onPDFExportClickListener != null) {
                    onPDFExportClickListener.onPDFExportClick(intervention);
                }
            });
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onInterventionClick(intervention);
            }
        });
    }

    @Override
    public int getItemCount() {
        return interventionList.size();
    }

    static class InterventionViewHolder extends RecyclerView.ViewHolder {
        TextView textType, textStatus, textPlaneId, textTechnicianId, textDuration, textDate;
        Button buttonExportPDF;

        public InterventionViewHolder(@NonNull View itemView) {
            super(itemView);
            textType = itemView.findViewById(R.id.textInterventionType);
            textStatus = itemView.findViewById(R.id.textInterventionStatus);
            textPlaneId = itemView.findViewById(R.id.textPlaneId);
            textTechnicianId = itemView.findViewById(R.id.textTechnicianId);
            textDuration = itemView.findViewById(R.id.textDuration);
            textDate = itemView.findViewById(R.id.textDate);
            buttonExportPDF = itemView.findViewById(R.id.buttonExportPDF);
        }
    }
}