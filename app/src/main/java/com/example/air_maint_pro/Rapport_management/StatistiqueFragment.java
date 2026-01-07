package com.example.air_maint_pro.Rapport_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_maint_pro.R;
import com.example.air_maint_pro.gestion_vols.Vol;
import com.example.air_maint_pro.TechnicienListFragment;
import com.example.air_maint_pro.intervention_management.Intervention;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatistiqueFragment extends Fragment {

    private TextView txtVolsCount, txtVolsPercent;
    private TextView txtInterventionsCount, txtInterventionsPercent;
    private TextView txtTechniciensCount, txtTechniciensPercent;
    private RecyclerView recentActivityRecyclerView;
    private RecentActivityAdapter recentActivityAdapter;

    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat;

    // Pour le calcul des pourcentages
    private int interventionsThisWeek = 0;
    private int interventionsLastWeek = 0;
    private int volsThisWeek = 0;
    private int volsLastWeek = 0;
    private int activeTechnicians = 0;
    private int totalTechnicians = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.statistique_activity, container, false);

        // Initialiser Firebase
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH);

        // Initialiser les vues
        initViews(view);

        // Configurer le RecyclerView pour l'activité récente
        setupRecyclerView();

        // Charger les données depuis Firebase
        loadStatisticsData();
        loadRecentInterventions();

        // Setup the statistics bottom navigation
        setupBottomNavigation(view);

        return view;
    }

    private void initViews(View view) {
        txtVolsCount = view.findViewById(R.id.txtVolsCount);
        txtVolsPercent = view.findViewById(R.id.txtVolsPercent);
        txtInterventionsCount = view.findViewById(R.id.txtInterventionsCount);
        txtInterventionsPercent = view.findViewById(R.id.txtInterventionsPercent);
        txtTechniciensCount = view.findViewById(R.id.txtTechniciensCount);
        txtTechniciensPercent = view.findViewById(R.id.txtTechniciensPercent);
        recentActivityRecyclerView = view.findViewById(R.id.recentActivityRecyclerView);

        // Valeurs par défaut
        txtVolsCount.setText("0");
        txtVolsPercent.setText("0%");
        txtVolsPercent.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void setupRecyclerView() {
        recentActivityAdapter = new RecentActivityAdapter(new ArrayList<>());
        recentActivityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentActivityRecyclerView.setAdapter(recentActivityAdapter);
    }

    private void setupBottomNavigation(View view) {
        BottomNavigationView statsBottomNav = view.findViewById(R.id.bottomNavigation);
        statsBottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                // Refresh les données
                loadStatisticsData();
                loadRecentInterventions();
                Toast.makeText(getContext(), "Actualisation des données", Toast.LENGTH_SHORT).show();
                return true;

            } else if (itemId == R.id.nav_reports) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RapportsFragment())
                        .addToBackStack(null)
                        .commit();
                return true;

            } else if (itemId == R.id.nav_statistics) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ChartFragment())
                        .addToBackStack(null)
                        .commit();
                return true;

            } else if (itemId == R.id.nav_home) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TechnicienListFragment())
                        .commit();

                if (requireActivity() instanceof com.example.air_maint_pro.AdminActivity) {
                    ((com.example.air_maint_pro.AdminActivity) requireActivity())
                            .showMainBottomNav();
                }
                return true;
            }

            return false;
        });
    }

    private void loadStatisticsData() {
        // Calculer les dates pour cette semaine et la semaine dernière
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        // Début et fin de cette semaine (Lundi 00:00 → Dimanche 23:59)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfThisWeek = calendar.getTime();

        calendar.add(Calendar.DAY_OF_WEEK, 6); // Aller à dimanche
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endOfThisWeek = calendar.getTime();

        // Début et fin de la semaine dernière
        calendar.setTime(startOfThisWeek);
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        Date startOfLastWeek = calendar.getTime();

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endOfLastWeek = calendar.getTime();

        // 1. Compter les vols cette semaine
        countVolsThisWeek(startOfThisWeek, endOfThisWeek, startOfLastWeek, endOfLastWeek);

        // 2. Compter les interventions cette semaine
        db.collection("interventions")
                .whereGreaterThanOrEqualTo("dateIntervention", startOfThisWeek)
                .whereLessThanOrEqualTo("dateIntervention", endOfThisWeek)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    interventionsThisWeek = queryDocumentSnapshots.size();
                    txtInterventionsCount.setText(String.valueOf(interventionsThisWeek));

                    // Maintenant compter la semaine dernière pour le pourcentage
                    countInterventionsLastWeek(startOfLastWeek, endOfLastWeek);
                })
                .addOnFailureListener(e -> {
                    txtInterventionsCount.setText("--");
                    txtInterventionsPercent.setText("N/A");
                    Toast.makeText(getContext(), "Erreur chargement interventions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // 3. Compter les techniciens (collection "Users" avec role = "technicien")
        db.collection("Users")
                .whereEqualTo("role", "technicien")  // CORRECTION ICI : "technicien" en minuscule
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalTechnicians = queryDocumentSnapshots.size();
                    activeTechnicians = totalTechnicians; // Tous les techniciens sont actifs

                    txtTechniciensCount.setText(String.valueOf(activeTechnicians));

                    // Pourcentage fixe pour l'instant (-3%)
                    // Tu peux ajouter un historique plus tard
                    float percentChange = -3.0f;
                    txtTechniciensPercent.setText(String.format(Locale.FRENCH, "%.0f%%", percentChange));

                    if (percentChange > 0) {
                        txtTechniciensPercent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        txtTechniciensPercent.setText("+" + txtTechniciensPercent.getText());
                    } else {
                        txtTechniciensPercent.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                })
                .addOnFailureListener(e -> {
                    txtTechniciensCount.setText("--");
                    txtTechniciensPercent.setText("N/A");
                    Toast.makeText(getContext(), "Erreur chargement techniciens: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void countVolsThisWeek(Date startOfThisWeek, Date endOfThisWeek,
                                   Date startOfLastWeek, Date endOfLastWeek) {
        // Rechercher les vols dont la date de départ est dans cette semaine
        db.collection("vols")
                .whereGreaterThanOrEqualTo("date_depart", startOfThisWeek)
                .whereLessThanOrEqualTo("date_depart", endOfThisWeek)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    volsThisWeek = queryDocumentSnapshots.size();
                    txtVolsCount.setText(String.valueOf(volsThisWeek));

                    // Maintenant compter la semaine dernière pour le pourcentage
                    countVolsLastWeek(startOfLastWeek, endOfLastWeek);
                })
                .addOnFailureListener(e -> {
                    txtVolsCount.setText("--");
                    txtVolsPercent.setText("N/A");
                    Toast.makeText(getContext(), "Erreur chargement vols: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void countVolsLastWeek(Date startOfLastWeek, Date endOfLastWeek) {
        db.collection("vols")
                .whereGreaterThanOrEqualTo("date_depart", startOfLastWeek)
                .whereLessThanOrEqualTo("date_depart", endOfLastWeek)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    volsLastWeek = queryDocumentSnapshots.size();

                    // Calculer le pourcentage de changement
                    calculateVolsPercentage();
                })
                .addOnFailureListener(e -> {
                    txtVolsPercent.setText("N/A");
                    Toast.makeText(getContext(), "Erreur calcul pourcentage vols: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void calculateVolsPercentage() {
        if (volsLastWeek > 0) {
            float percentChange = ((float)(volsThisWeek - volsLastWeek) / volsLastWeek) * 100;
            txtVolsPercent.setText(String.format(Locale.FRENCH, "%.0f%%", percentChange));

            if (percentChange > 0) {
                txtVolsPercent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                txtVolsPercent.setText("+" + txtVolsPercent.getText());
            } else if (percentChange < 0) {
                txtVolsPercent.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                txtVolsPercent.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            // Pas de données la semaine dernière
            if (volsThisWeek > 0) {
                txtVolsPercent.setText("+100%");
                txtVolsPercent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                txtVolsPercent.setText("0%");
                txtVolsPercent.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        }
    }

    private void countInterventionsLastWeek(Date startOfLastWeek, Date endOfLastWeek) {
        db.collection("interventions")
                .whereGreaterThanOrEqualTo("dateIntervention", startOfLastWeek)
                .whereLessThanOrEqualTo("dateIntervention", endOfLastWeek)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    interventionsLastWeek = queryDocumentSnapshots.size();

                    // Calculer le pourcentage de changement
                    if (interventionsLastWeek > 0) {
                        float percentChange = ((float)(interventionsThisWeek - interventionsLastWeek) / interventionsLastWeek) * 100;

                        txtInterventionsPercent.setText(String.format(Locale.FRENCH, "%.0f%%", percentChange));

                        if (percentChange > 0) {
                            txtInterventionsPercent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            txtInterventionsPercent.setText("+" + txtInterventionsPercent.getText());
                        } else {
                            txtInterventionsPercent.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        }
                    } else {
                        // Pas de données la semaine dernière
                        if (interventionsThisWeek > 0) {
                            txtInterventionsPercent.setText("+100%");
                            txtInterventionsPercent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        } else {
                            txtInterventionsPercent.setText("0%");
                            txtInterventionsPercent.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    txtInterventionsPercent.setText("N/A");
                    Toast.makeText(getContext(), "Erreur calcul pourcentage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRecentInterventions() {
        // Récupérer les 3 dernières interventions
        db.collection("interventions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Intervention> recentInterventions = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Intervention intervention = document.toObject(Intervention.class);
                        intervention.setId(document.getId());
                        recentInterventions.add(intervention);
                    }

                    // Mettre à jour l'adapter
                    recentActivityAdapter.updateData(recentInterventions);

                    if (recentInterventions.isEmpty()) {
                        Toast.makeText(getContext(), "Aucune intervention récente", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur chargement interventions récentes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Adapter pour l'activité récente
    private static class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

        private List<Intervention> interventions;

        public RecentActivityAdapter(List<Intervention> interventions) {
            this.interventions = interventions;
        }

        public void updateData(List<Intervention> newInterventions) {
            this.interventions = newInterventions;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_statsrecent_activity, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Intervention intervention = interventions.get(position);
            holder.bind(intervention);
        }

        @Override
        public int getItemCount() {
            return interventions.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView txtTitle;
            private final TextView txtTime;
            private final View statusBadge;
            private final TextView txtStatus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.txtActivityTitle);
                txtTime = itemView.findViewById(R.id.txtActivityTime);
                statusBadge = itemView.findViewById(R.id.statusBadge);
                txtStatus = itemView.findViewById(R.id.txtActivityStatus);
            }

            public void bind(Intervention intervention) {
                // Titre basé sur le type d'intervention
                String title = intervention.getTypeIntervention() + " #" +
                        (intervention.getId() != null && intervention.getId().length() > 6 ?
                                intervention.getId().substring(0, 6) :
                                (intervention.getId() != null ? intervention.getId() : "N/A"));
                txtTitle.setText(title);

                // Temps écoulé depuis la création
                txtTime.setText(getTimeAgo(intervention.getCreatedAt()));

                // Déterminer le statut basé sur la date d'intervention vs maintenant
                String status = determineStatus(intervention);
                txtStatus.setText(status);

                // Couleur du badge selon le statut
                int colorRes = getStatusColor(status);
                statusBadge.setBackgroundColor(itemView.getContext().getResources().getColor(colorRes));
            }

            private String getTimeAgo(Date date) {
                if (date == null) return "Date inconnue";

                long now = System.currentTimeMillis();
                long diff = now - date.getTime();

                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                if (days > 0) {
                    return "Il y a " + days + " jour" + (days > 1 ? "s" : "");
                } else if (hours > 0) {
                    return "Il y a " + hours + " heure" + (hours > 1 ? "s" : "");
                } else if (minutes > 0) {
                    return "Il y a " + minutes + " minute" + (minutes > 1 ? "s" : "");
                } else {
                    return "À l'instant";
                }
            }

            private String determineStatus(Intervention intervention) {
                String statut = intervention.getStatut();
                Date now = new Date();
                Date interventionDate = intervention.getDateIntervention();

                if (interventionDate == null) {
                    return "Inconnu";
                }

                // Priorité au statut explicite
                if ("Terminée".equals(statut) || "Clôturée".equals(statut)) {
                    return "Complété";
                } else if ("En cours".equals(statut)) {
                    return "En cours";
                } else if ("Planifiée".equals(statut)) {
                    return "Planifié";
                }

                // Sinon, déterminer par rapport à la date
                Calendar cal = Calendar.getInstance();
                cal.setTime(interventionDate);
                cal.add(Calendar.HOUR, (int) intervention.getDureeHeures());
                Date endDate = cal.getTime();

                if (now.after(endDate)) {
                    return "Complété";
                } else if (now.before(interventionDate)) {
                    return "Planifié";
                } else {
                    return "En cours";
                }
            }

            private int getStatusColor(String status) {
                switch (status) {
                    case "Complété":
                        return android.R.color.holo_orange_dark;
                    case "En cours":
                        return android.R.color.holo_orange_light;
                    case "Planifié":
                        return android.R.color.holo_blue_light;
                    default:
                        return android.R.color.darker_gray;
                }
            }
        }
    }
}