package com.example.air_maint_pro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class TechnicienHomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvInfo;
    private Button btnLogout, btnTasks;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technicien_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvInfo = findViewById(R.id.tvInfo);
        btnLogout = findViewById(R.id.btnLogout);
        btnTasks = findViewById(R.id.btnTasks);

        auth = FirebaseAuth.getInstance();

        // Récupérer les données du technicien
        String fullName = getIntent().getStringExtra("fullName");
        String nom = getIntent().getStringExtra("nom");
        String prenom = getIntent().getStringExtra("prenom");
        String email = getIntent().getStringExtra("email");
        String departement = getIntent().getStringExtra("departement");
        Long age = getIntent().getLongExtra("age", 0);

        // Afficher les informations
        if (fullName != null) {
            tvWelcome.setText("Bienvenue " + fullName);
        }

        StringBuilder info = new StringBuilder();
        if (departement != null) {
            info.append("Département: ").append(departement).append("\n");
        }
        if (age > 0) {
            info.append("Âge: ").append(age).append(" ans\n");
        }
        if (email != null) {
            info.append("Email: ").append(email);
        }
        tvInfo.setText(info.toString());

        // Bouton tâches (à implémenter)
        btnTasks.setOnClickListener(v -> {
            Toast.makeText(this, "Module des tâches à implémenter", Toast.LENGTH_SHORT).show();
        });

        // Déconnexion
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}