package com.example.air_maint_pro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Afficher un indicateur de chargement (optionnel)
        btnLogin.setEnabled(false);
        btnLogin.setText("Connexion...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    db.collection("Users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (!documentSnapshot.exists()) {
                                    Toast.makeText(this, "Profil utilisateur introuvable", Toast.LENGTH_SHORT).show();
                                    resetLoginButton();
                                    return;
                                }

                                String role = documentSnapshot.getString("role");
                                String nom = documentSnapshot.getString("nom");
                                String prenom = documentSnapshot.getString("prenom");

                                // Construction du nom complet
                                String fullName = "";
                                if (nom != null && prenom != null) {
                                    fullName = nom + " " + prenom;
                                } else if (nom != null) {
                                    fullName = nom;
                                } else if (prenom != null) {
                                    fullName = prenom;
                                } else {
                                    // Fallback à l'email si pas de nom/prenom
                                    fullName = email.split("@")[0];
                                }

                                if ("supervisor".equals(role)) {
                                    Intent intent = new Intent(this, AdminActivity.class);
                                    intent.putExtra("fullName", fullName);
                                    intent.putExtra("nom", nom);
                                    intent.putExtra("prenom", prenom);
                                    startActivity(intent);
                                    finish();

                                } else if ("technicien".equals(role)) {
                                    Intent intent = new Intent(this, TechnicienHomeActivity.class);
                                    intent.putExtra("fullName", fullName);
                                    intent.putExtra("nom", nom);
                                    intent.putExtra("prenom", prenom);
                                    intent.putExtra("email", email);
                                    intent.putExtra("departement", documentSnapshot.getString("departement"));
                                    intent.putExtra("age", documentSnapshot.getLong("age"));
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Toast.makeText(this, "Rôle non reconnu", Toast.LENGTH_SHORT).show();
                                    resetLoginButton();
                                }

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erreur de lecture du profil", Toast.LENGTH_SHORT).show();
                                resetLoginButton();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                    resetLoginButton();
                });
    }

    private void resetLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText("Se connecter");
    }
}