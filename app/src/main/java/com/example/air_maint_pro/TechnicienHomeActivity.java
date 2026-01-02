package com.example.air_maint_pro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.air_maint_pro.intervention_management.TechnicianInterventionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class TechnicienHomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technicien_home);

        // Firebase
        auth = FirebaseAuth.getInstance();

        // Vérification connexion
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        // Setup bottom navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Load default fragment (home)
        if (savedInstanceState == null) {
            loadFragment(new TechnicienHomeFragment());
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            fragment = new TechnicienHomeFragment();
        } else if (itemId == R.id.nav_interventions) {
            fragment = new TechnicianInterventionsFragment();
        } else if (itemId == R.id.nav_settings) {
            fragment = new SettingsFragment(); // Utiliser le même SettingsFragment
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

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}