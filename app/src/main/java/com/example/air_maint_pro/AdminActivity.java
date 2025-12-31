package com.example.air_maint_pro;



import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnCreateEmployee, btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);


        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        tvWelcome = findViewById(R.id.tvWelcome);
        btnCreateEmployee = findViewById(R.id.btnCreateEmployee);
        btnLogout = findViewById(R.id.btnLogout);

        // Vérifier utilisateur connecté
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        // Vérification simple du rôle (pas sécurisé, volontaire)
        db.collection("Users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        redirectToLogin();
                        return;
                    }

                    String role = document.getString("role");
                    String name = document.getString("name");

                    if (!"supervisor".equals(role)) {
                        Toast.makeText(this, "Accès refusé", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                        return;
                    }

                    tvWelcome.setText("Bienvenue supervisor : " + name);
                })
                .addOnFailureListener(e -> redirectToLogin());

        // Bouton créer employé
        btnCreateEmployee.setOnClickListener(v ->
                startActivity(new Intent(this, EmployeeActivity.class))
        );

        // Bouton déconnexion
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            redirectToLogin();
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
