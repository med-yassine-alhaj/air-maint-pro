package com.example.air_maint_pro.gestion_avion;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AvionAdapter extends RecyclerView.Adapter<AvionAdapter.AvionViewHolder> {

    private List<Avion> avionList;
    private OnAvionClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);

    public interface OnAvionClickListener {
        void onAvionClick(Avion avion);
        void onQrCodeClick(Avion avion);
    }

    public AvionAdapter(List<Avion> avionList, OnAvionClickListener listener) {
        this.avionList = avionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AvionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_avion, parent, false);
        return new AvionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvionViewHolder holder, int position) {
        Avion avion = avionList.get(position);
        holder.bind(avion);

        // Gestion du clic sur l'item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAvionClick(avion);
            }
        });

        // Gestion du clic sur le QR Code
        holder.ivQrCode.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQrCodeClick(avion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return avionList.size();
    }

    public void updateData(List<Avion> newAvionList) {
        this.avionList = newAvionList;
        notifyDataSetChanged();
    }

    class AvionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvImmatriculation, tvModele, tvCompagnie, tvHeuresVol, tvEtat;
        private ImageView ivQrCode;
        private CardView cardView;

        public AvionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvImmatriculation = itemView.findViewById(R.id.tvImmatriculation);
            tvModele = itemView.findViewById(R.id.tvModele);
            tvCompagnie = itemView.findViewById(R.id.tvCompagnie);
            tvHeuresVol = itemView.findViewById(R.id.tvHeuresVol);
            tvEtat = itemView.findViewById(R.id.tvEtat);
            ivQrCode = itemView.findViewById(R.id.ivQrCode);
            cardView = (CardView) itemView;
        }

        public void bind(Avion avion) {
            tvImmatriculation.setText(avion.matricule);
            tvModele.setText(avion.modele + " (" + avion.type + ")");
            tvCompagnie.setText(avion.compagnie);
            tvHeuresVol.setText(String.format(Locale.FRENCH, "%.0f h", avion.heuresVol));
            tvEtat.setText(avion.etat);

            // Changer la couleur du badge selon l'état
            switch (avion.etat) {
                case "Actif":
                    tvEtat.setBackgroundResource(R.drawable.bg_etat_actif);
                    break;
                case "En maintenance":
                    tvEtat.setBackgroundResource(R.drawable.bg_etat_maintenance);
                    break;
                case "Hors service":
                    tvEtat.setBackgroundResource(R.drawable.bg_etat_hors_service);
                    break;
            }
        }
    }
}