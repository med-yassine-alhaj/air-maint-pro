package com.example.air_maint_pro.Rapport_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.air_maint_pro.R;
import com.example.air_maint_pro.TechnicienListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class StatistiqueFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout
        View view = inflater.inflate(R.layout.statistique_activity, container, false);

        // Setup the statistics bottom navigation
        BottomNavigationView statsBottomNav = view.findViewById(R.id.bottomNavigation);
        statsBottomNav.setOnNavigationItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {

                // Show dashboard statistics
                Toast.makeText(getContext(), "Tableau de bord", Toast.LENGTH_SHORT).show();
                return true;

            } else if (itemId == R.id.nav_reports) {

                // 👉 GO TO RAPPORT PAGE
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

                // Go back to main home (TechnicienListFragment)
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TechnicienListFragment())
                        .commit();

                // Show main bottom navigation again
                if (requireActivity() instanceof com.example.air_maint_pro.AdminActivity) {
                    ((com.example.air_maint_pro.AdminActivity) requireActivity())
                            .showMainBottomNav();
                }
                return true;
            }

            return false;
        });

        return view;
    }
}
