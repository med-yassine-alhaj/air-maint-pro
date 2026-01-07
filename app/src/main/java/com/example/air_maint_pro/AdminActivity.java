package com.example.air_maint_pro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.air_maint_pro.gestion_avion.AvionListFragment;
import com.example.air_maint_pro.gestion_vols.MainVolsFragment;
import com.example.air_maint_pro.intervention_management.InterventionsFragment;
import com.example.air_maint_pro.Rapport_management.StatistiqueFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {

    // DECLARE FIELDS HERE (at class level, not inside methods)
    private FirebaseAuth auth;
    private BottomNavigationView bottomNavigation; // <-- THIS IS CORRECT PLACE

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Firebase
        auth = FirebaseAuth.getInstance();

        // REMOVE THIS LINE: BottomNavigationView bottomNavigation; (WRONG - local variable)

        // Vérification connexion
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        // Setup bottom navigation - INITIALIZE the field
        bottomNavigation = findViewById(R.id.bottomNavigation); // <-- Initialize the field
        bottomNavigation.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Load default fragment (home)
        if (savedInstanceState == null) {
            loadFragment(new AdminHomeFragment()); // Changer ici
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }


    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            fragment = new AdminHomeFragment();
            showMainBottomNav();
        } else if (itemId == R.id.nav_avions) {
            fragment = new AvionListFragment();
            showMainBottomNav();
        } else if (itemId == R.id.nav_vols) {
            fragment = new MainVolsFragment();
            showMainBottomNav();
        } else if (itemId == R.id.nav_interventions) {
            fragment = new InterventionsFragment();
            showMainBottomNav();
        } else if (itemId == R.id.nav_users) {
            fragment = new TechnicienListFragment(); // For now, same as home
            showMainBottomNav();
        } else if (itemId == R.id.nav_rapport_stat) {
            fragment = new StatistiqueFragment();
            hideMainBottomNav();
        } else if (itemId == R.id.nav_settings) {
            fragment = new SettingsFragment();
            showMainBottomNav();
        }

        if (fragment != null) {
            loadFragment(fragment);
            return true;
        }

        return false;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void showMainBottomNav() {
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(View.VISIBLE);
        }
    }

    public void hideMainBottomNav() {
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}