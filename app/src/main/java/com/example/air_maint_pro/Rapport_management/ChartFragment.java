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

import java.util.ArrayList;
import java.util.List;

public class ChartFragment extends Fragment {

    private PieChart pieChart;
    private BarChart barChart;
    private BottomNavigationView bottomNavigationView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        // Initialiser les composants
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        bottomNavigationView = view.findViewById(R.id.bottomNavigation);

        // Configurer le Bottom Navigation
        setupBottomNavigation();

        // Configurer les graphiques
        setupPieChart();
        setupBarChart();

        // Charger les données
        loadChartData();

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
                    // Show dashboard statistics
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new StatistiqueFragment())
                            .addToBackStack(null)
                            .commit();
                    return true;

                } else if (itemId == R.id.nav_reports) {
                    // Naviguer vers RapportsFragment
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new RapportsFragment())
                            .addToBackStack(null)
                            .commit();
                    return true;

                } else if (itemId == R.id.nav_statistics) {
                    // Déjà sur Statistiques, ne rien faire
                    return true;

                } else if (itemId == R.id.nav_home) {
                    // Retourner à TechnicienListFragment
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new TechnicienListFragment())
                            .commit();

                    // Afficher le Bottom Navigation principal si dans AdminActivity
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

        // Configurer l'axe Y gauche
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(10f);

        // Désactiver l'axe Y droit
        barChart.getAxisRight().setEnabled(false);

        // Désactiver la légende
        barChart.getLegend().setEnabled(false);

        // Animation
        barChart.animateY(1500);

        // Toucher
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
    }

    private void loadChartData() {
        // Données pour le diagramme circulaire (comme dans l'image)
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(45f, "Maintenance"));
        pieEntries.add(new PieEntry(30f, "Réparation"));
        pieEntries.add(new PieEntry(10f, "Revision"));
        pieEntries.add(new PieEntry(15f, "Inspection"));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");

        // Couleurs exactes comme dans l'image
        int[] pieColors = {
                Color.parseColor("#FF6B6B"),  // Rouge - Maintenance
                Color.parseColor("#4ECDC4"),  // Turquoise - Réparation
                Color.parseColor("#FFD166"),  // Jaune - Revision
                Color.parseColor("#06D6A0")   // Vert - Inspection
        };
        pieDataSet.setColors(pieColors);

        // Espace entre les sections
        pieDataSet.setSliceSpace(2f);
        pieDataSet.setSelectionShift(5f);

        // Format des valeurs
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.WHITE);

        pieChart.setData(pieData);
        pieChart.invalidate();

        // Données pour le graphique en barres (comme dans l'image)
        List<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0f, 36f));  // Lundi
        barEntries.add(new BarEntry(1f, 27f));  // Mardi
        barEntries.add(new BarEntry(2f, 18f));  // Mercredi
        barEntries.add(new BarEntry(3f, 22f));  // Jeudi
        barEntries.add(new BarEntry(4f, 30f));  // Vendredi
        barEntries.add(new BarEntry(5f, 12f));  // Samedi
        barEntries.add(new BarEntry(6f, 8f));   // Dimanche

        BarDataSet barDataSet = new BarDataSet(barEntries, "Interventions");
        barDataSet.setColor(Color.parseColor("#4A90E2"));  // Bleu pour les barres

        // Remplir les barres
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(10f);
        barDataSet.setValueTextColor(Color.DKGRAY);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.6f);

        // Labels pour l'axe X
        String[] days = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));

        barChart.setData(barData);
        barChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        // S'assurer que le bon item est sélectionné
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_statistics);
        }
    }
}