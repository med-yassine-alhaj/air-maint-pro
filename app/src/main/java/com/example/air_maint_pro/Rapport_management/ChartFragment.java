package com.example.air_maint_pro.Rapport_management;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.air_maint_pro.R;
import com.example.air_maint_pro.TechnicienListFragment;
import com.example.air_maint_pro.intervention_management.Intervention;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChartFragment extends Fragment {

    private PieChart pieChart;
    private BarChart barChart;
    private BottomNavigationView bottomNavigationView;

    private FirebaseFirestore db;

    // Tous les types d'interventions
    private static final String[] ALL_INTERVENTION_TYPES = {
            "Maintenance préventive",
            "Maintenance corrective",
            "Dépannage",
            "Révision générale",
            "Contrôle qualité",
            "Inspection technique",
            "Vérification système"
    };

    // Couleurs pour chaque type
    private static final int[] PIE_COLORS = {
            Color.parseColor("#FF6B6B"),  // Rouge - Maintenance préventive
            Color.parseColor("#FF8E8E"),  // Rouge clair - Maintenance corrective
            Color.parseColor("#4ECDC4"),  // Turquoise - Dépannage
            Color.parseColor("#FFD166"),  // Jaune - Révision générale
            Color.parseColor("#FFE8A5"),  // Jaune clair - Contrôle qualité
            Color.parseColor("#06D6A0"),  // Vert - Inspection technique
            Color.parseColor("#4CD9B4")   // Vert clair - Vérification système
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        // Initialiser Firebase
        db = FirebaseFirestore.getInstance();

        // Initialiser les composants
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        bottomNavigationView = view.findViewById(R.id.bottomNavigation);

        // Configurer le Bottom Navigation
        setupBottomNavigation();

        // Configurer les graphiques
        setupPieChart();
        setupBarChart();

        // Charger les données depuis Firebase
        loadPieChartData();
        loadBarChartData();

        return view;
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView != null) {
            // Marquer l'item Statistiques comme sélectionné
            bottomNavigationView.setSelectedItemId(R.id.nav_statistics);

            // Gérer les clics sur le menu
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new StatistiqueFragment())
                            .addToBackStack(null)
                            .commit();
                    return true;

                } else if (itemId == R.id.nav_reports) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new RapportsFragment())
                            .addToBackStack(null)
                            .commit();
                    return true;

                } else if (itemId == R.id.nav_statistics) {
                    // Recharger les données
                    loadPieChartData();
                    loadBarChartData();
                    Toast.makeText(getContext(), "Graphiques actualisés", Toast.LENGTH_SHORT).show();
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
    }

    private void setupPieChart() {
        // Désactiver la description
        pieChart.getDescription().setEnabled(false);

        // Activer le trou au centre (donut chart)
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setHoleRadius(50f);

        // Configurer le centre vide
        pieChart.setDrawCenterText(false);

        // Rotation tactile
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // Animation
        pieChart.animateY(1400);

        // Légende
        pieChart.getLegend().setEnabled(false);

        // Text size
        pieChart.setEntryLabelTextSize(11f);
        pieChart.setEntryLabelColor(Color.BLACK);
    }

    private void setupBarChart() {
        // Désactiver la description
        barChart.getDescription().setEnabled(false);

        // Configurer l'axe X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setTextSize(10f);

        // Configurer l'axe Y gauche
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextSize(10f);

        // Désactiver l'axe Y droit
        barChart.getAxisRight().setEnabled(false);

        // Désactiver la légende
        barChart.getLegend().setEnabled(false);

        // Animation
        barChart.animateY(1500);

        // Configuration du touch
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);
    }

    private void loadPieChartData() {
        // Récupérer toutes les interventions pour compter par type
        db.collection("interventions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Initialiser les compteurs pour chaque type
                    Map<String, Integer> typeCounts = new HashMap<>();
                    for (String type : ALL_INTERVENTION_TYPES) {
                        typeCounts.put(type, 0);
                    }

                    // Compter les interventions par type
                    int totalInterventions = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Intervention intervention = document.toObject(Intervention.class);
                        String type = intervention.getTypeIntervention();

                        // Vérifier si le type existe dans notre liste
                        if (typeCounts.containsKey(type)) {
                            typeCounts.put(type, typeCounts.get(type) + 1);
                            totalInterventions++;
                        }
                    }

                    // Créer les entrées pour le PieChart
                    List<PieEntry> pieEntries = new ArrayList<>();

                    // Ajouter seulement les types qui ont des données
                    for (String type : ALL_INTERVENTION_TYPES) {
                        int count = typeCounts.get(type);
                        if (count > 0) {
                            // Convertir en pourcentage
                            float percentage = totalInterventions > 0 ?
                                    ((float) count / totalInterventions) * 100 : 0;
                            // Raccourcir le label pour l'affichage
                            String shortLabel = getShortLabel(type);
                            pieEntries.add(new PieEntry(percentage, shortLabel));
                        }
                    }

                    // Si aucune donnée, afficher un message
                    if (pieEntries.isEmpty()) {
                        Toast.makeText(getContext(), "Aucune donnée d'intervention", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Configurer le dataset
                    PieDataSet pieDataSet = new PieDataSet(pieEntries, "");

                    // Prendre seulement le nombre de couleurs nécessaires
                    int[] colorsToUse = new int[pieEntries.size()];
                    for (int i = 0; i < pieEntries.size() && i < PIE_COLORS.length; i++) {
                        colorsToUse[i] = PIE_COLORS[i];
                    }
                    pieDataSet.setColors(colorsToUse);

                    // Espace entre les sections
                    pieDataSet.setSliceSpace(2f);
                    pieDataSet.setSelectionShift(5f);

                    // Format des valeurs
                    PieData pieData = new PieData(pieDataSet);
                    pieData.setValueFormatter(new PercentFormatter(pieChart));
                    pieData.setValueTextSize(11f);
                    pieData.setValueTextColor(Color.WHITE);

                    pieChart.setData(pieData);
                    pieChart.invalidate();

                    // Mettre à jour les légendes
                    updateLegends(typeCounts);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur chargement données: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getShortLabel(String fullLabel) {
        // Raccourcir les labels pour qu'ils tiennent dans le graphique
        switch (fullLabel) {
            case "Maintenance préventive": return "Mnt Préventive";
            case "Maintenance corrective": return "Mnt Corrective";
            case "Révision générale": return "Révision";
            case "Inspection technique": return "Inspection";
            case "Contrôle qualité": return "Contrôle Qualité";
            case "Vérification système": return "Vérif Système";
            default: return fullLabel;
        }
    }

    private void loadBarChartData() {
        // Calculer les dates pour CETTE SEMAINE (Lundi à Dimanche)
        Calendar calendar = Calendar.getInstance();

        // Aller au début de la semaine (Lundi)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfWeek = calendar.getTime();

        // Aller à la fin de la semaine (Dimanche 23:59:59)
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endOfWeek = calendar.getTime();

        // Formatter pour le nom des jours
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.FRENCH);

        // Récupérer les interventions de CETTE SEMAINE
        db.collection("interventions")
                .whereGreaterThanOrEqualTo("dateIntervention", startOfWeek)
                .whereLessThanOrEqualTo("dateIntervention", endOfWeek)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Initialiser le compteur pour chaque jour de la semaine
                    Map<String, Integer> dailyCounts = new HashMap<>();

                    // Initialiser tous les jours de la semaine à 0
                    String[] weekDays = {"lun.", "mar.", "mer.", "jeu.", "ven.", "sam.", "dim."};
                    for (String day : weekDays) {
                        dailyCounts.put(day, 0);
                    }

                    // Compter les interventions par jour de CETTE SEMAINE
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Intervention intervention = document.toObject(Intervention.class);
                        Date interventionDate = intervention.getDateIntervention();

                        if (interventionDate != null) {
                            String dayName = dayFormat.format(interventionDate);
                            dailyCounts.put(dayName, dailyCounts.get(dayName) + 1);
                        }
                    }

                    // Créer les entrées pour le BarChart
                    List<BarEntry> barEntries = new ArrayList<>();
                    List<String> dayLabels = new ArrayList<>();

                    // Ordonner les jours (Lun, Mar, Mer, Jeu, Ven, Sam, Dim)
                    for (int i = 0; i < weekDays.length; i++) {
                        String day = weekDays[i];
                        Integer count = dailyCounts.get(day);
                        if (count != null) {
                            barEntries.add(new BarEntry(i, count));
                        } else {
                            barEntries.add(new BarEntry(i, 0));
                        }
                        dayLabels.add(day.substring(0, 3).toUpperCase()); // "lun." -> "LUN"
                    }

                    // Configurer le dataset
                    BarDataSet barDataSet = new BarDataSet(barEntries, "Interventions");
                    barDataSet.setColor(Color.parseColor("#4A90E2"));  // Bleu pour les barres
                    barDataSet.setValueTextColor(Color.DKGRAY);
                    barDataSet.setValueTextSize(10f);
                    barDataSet.setDrawValues(true);

                    // Configuration des barres
                    BarData barData = new BarData(barDataSet);
                    barData.setBarWidth(0.6f);

                    // Labels pour l'axe X
                    XAxis xAxis = barChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(dayLabels));
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f);
                    xAxis.setLabelCount(7);

                    // Configuration de l'axe Y
                    YAxis leftAxis = barChart.getAxisLeft();
                    leftAxis.setAxisMinimum(0f);

                    // Trouver la valeur maximale pour bien ajuster l'axe Y
                    float maxValue = 0;
                    for (BarEntry entry : barEntries) {
                        if (entry.getY() > maxValue) {
                            maxValue = entry.getY();
                        }
                    }
                    // Ajouter un peu d'espace au-dessus (minimum 1)
                    leftAxis.setAxisMaximum(Math.max(maxValue + 1, 1));

                    barChart.setData(barData);
                    barChart.invalidate();
                    barChart.animateY(1000);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur chargement données journalières: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLegends(Map<String, Integer> typeCounts) {
        if (getView() == null) return;

        // Récupérer toutes les vues des légendes
        Map<String, View> legendViews = new HashMap<>();
        legendViews.put("Maintenance préventive", getView().findViewById(R.id.legendMaintPreventive));
        legendViews.put("Maintenance corrective", getView().findViewById(R.id.legendMaintCorrective));
        legendViews.put("Dépannage", getView().findViewById(R.id.legendDepannage));
        legendViews.put("Révision générale", getView().findViewById(R.id.legendRevisionGenerale));
        legendViews.put("Contrôle qualité", getView().findViewById(R.id.legendControleQualite));
        legendViews.put("Inspection technique", getView().findViewById(R.id.legendInspectionTech));
        legendViews.put("Vérification système", getView().findViewById(R.id.legendVerifSysteme));

        // Masquer toutes les légendes d'abord
        for (View view : legendViews.values()) {
            if (view != null) view.setVisibility(View.GONE);
        }

        // Afficher seulement les légendes qui ont des données
        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            if (entry.getValue() > 0) {
                View legendView = legendViews.get(entry.getKey());
                if (legendView != null) {
                    legendView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // S'assurer que le bon item est sélectionné
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_statistics);
        }

        // Rafraîchir les données quand le fragment redevient visible
        loadPieChartData();
        loadBarChartData();
    }
}