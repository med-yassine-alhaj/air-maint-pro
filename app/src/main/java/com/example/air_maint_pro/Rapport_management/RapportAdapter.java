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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RapportAdapter extends RecyclerView.Adapter<RapportAdapter.ViewHolder> {

    public interface OnRapportActionListener {
        void onLongClick(Rapport rapport);
    }

    private List<Rapport> rapports;
    private OnRapportActionListener listener;

    public RapportAdapter(List<Rapport> rapports, OnRapportActionListener listener) {
        this.rapports = rapports;
        this.listener = listener;
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

        // Format date
        if (r.getDate_generation() != null) {
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
                    .format(r.getDate_generation().toDate());
            holder.tvDate.setText(date);
        } else {
            holder.tvDate.setText("Date non spécifiée");
        }

        // Type
        holder.tvType.setText(r.getType());

        // Calculate size from content
        if (r.getContenu() != null && !r.getContenu().isEmpty()) {
            double sizeInMB = r.getContenu().length() / (1024.0 * 1024.0);
            holder.tvSize.setText(String.format(Locale.FRENCH, "%.1f MB", sizeInMB));
        } else {
            holder.tvSize.setText("0 MB");
        }

        // Status
        holder.tvStatut.setText(r.getStatut());

        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(r);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return rapports.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDate, tvType, tvSize, tvStatut;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvRapportTitle);
            tvDate = v.findViewById(R.id.tvRapportDate);
            tvType = v.findViewById(R.id.tvRapportType);
            tvSize = v.findViewById(R.id.tvRapportSize);
            tvStatut = v.findViewById(R.id.tvRapportStatut);
        }
    }
}