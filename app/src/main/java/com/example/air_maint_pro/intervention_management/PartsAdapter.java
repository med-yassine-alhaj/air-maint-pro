package com.example.air_maint_pro.intervention_management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PartsAdapter extends RecyclerView.Adapter<PartsAdapter.PartViewHolder> {

    private List<PartUsage> parts;
    private OnPartDeleteListener deleteListener;

    public interface OnPartDeleteListener {
        void onPartDelete(int position);
    }

    public PartsAdapter(List<PartUsage> parts, OnPartDeleteListener deleteListener) {
        this.parts = parts;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public PartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_part, parent, false);
        return new PartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PartViewHolder holder, int position) {
        PartUsage partUsage = parts.get(position);
        Piece piece = partUsage.getPiece();

        holder.textPartName.setText(piece.getName());
        holder.textPartDescription.setText(piece.getDescription());
        holder.textQuantity.setText(String.valueOf(partUsage.getQuantity()));
        holder.textWeight.setText(String.format(Locale.getDefault(), "%.2f kg", piece.getWeight()));
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        holder.textUnitPrice.setText(String.format(Locale.getDefault(), "%.2f", piece.getPrice()));
        holder.textPartTotal.setText(String.format(Locale.getDefault(), "Total: %s", 
                currencyFormat.format(partUsage.getTotalPrice())));

        holder.buttonDeletePart.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onPartDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return parts != null ? parts.size() : 0;
    }

    public void updateParts(List<PartUsage> newParts) {
        this.parts = newParts;
        notifyDataSetChanged();
    }

    static class PartViewHolder extends RecyclerView.ViewHolder {
        TextView textPartName;
        TextView textPartDescription;
        TextView textQuantity;
        TextView textWeight;
        TextView textUnitPrice;
        TextView textPartTotal;
        ImageButton buttonDeletePart;

        PartViewHolder(@NonNull View itemView) {
            super(itemView);
            textPartName = itemView.findViewById(R.id.textPartName);
            textPartDescription = itemView.findViewById(R.id.textPartDescription);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textWeight = itemView.findViewById(R.id.textWeight);
            textUnitPrice = itemView.findViewById(R.id.textUnitPrice);
            textPartTotal = itemView.findViewById(R.id.textPartTotal);
            buttonDeletePart = itemView.findViewById(R.id.buttonDeletePart);
        }
    }
}



