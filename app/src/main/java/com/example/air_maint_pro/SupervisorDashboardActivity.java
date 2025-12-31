package com.example.air_maint_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.air_maint_pro.intervention_management.InterventionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SupervisorDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_dashboard);

        mAuth = FirebaseAuth.getInstance();

        // Initialiser la Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Gérer la navigation
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    loadFragment(new HomeFragment());
                    return true;
                }
                else if (itemId == R.id.nav_interventions) {  // NEW
                    loadFragment(new InterventionsFragment());
                    return true;
                }

                else if (itemId == R.id.nav_users) {
                    loadFragment(new UsersFragment());
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    loadFragment(new SettingsFragment());
                    return true;
                }
                return false;
            }
        });

        // Charger le fragment Accueil par défaut
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            loadFragment(new HomeFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Vérifier si l'utilisateur est connecté
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}