package com.example.air_maint_pro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;
    private Button logoutButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Initialiser les vues
        welcomeText = findViewById(R.id.welcomeText);
        logoutButton = findViewById(R.id.logoutButton);

        // Afficher les informations de l'utilisateur
        displayUserInfo();

        // Bouton de déconnexion
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void displayUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userInfo = "Bienvenue, " + currentUser.getEmail() + "!\n" +
                    "UID: " + currentUser.getUid() + "\n";

            if (!currentUser.isEmailVerified()) {
                userInfo += "\n⚠️ Email non vérifié";
            }

            welcomeText.setText(userInfo);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Vérifier si l'utilisateur est connecté
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Si non connecté, retourner à l'écran de login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}