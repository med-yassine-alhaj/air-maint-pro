package com.example.air_maint_pro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Charger les préférences avant de setContentView
        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);

        // Appliquer le mode sombre
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_login);

        // Initialisation Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Vérifier si l'utilisateur est déjà connecté
        checkExistingSession();

        // Initialisation des vues
        initViews();

        // Configuration des écouteurs
        setupListeners();
    }

    private void checkExistingSession() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // L'utilisateur est déjà connecté, vérifier son rôle
            checkUserRoleAndRedirect(currentUser.getUid());
        }
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupListeners() {
        // Validation en temps réel de l'email
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Validation en temps réel du mot de passe
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bouton de connexion
        btnLogin.setOnClickListener(v -> loginUser());

        // Mot de passe oublié
        tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            tilEmail.setError("L'email est requis");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Format d'email invalide");
            return false;
        } else {
            tilEmail.setError(null);
            tilEmail.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            tilPassword.setError("Le mot de passe est requis");
            return false;
        } else if (password.length() < 6) {
            tilPassword.setError("Minimum 6 caractères");
            return false;
        } else {
            tilPassword.setError(null);
            tilPassword.setErrorEnabled(false);
            return true;
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        boolean isEmailValid = validateEmail(email);
        boolean isPasswordValid = validatePassword(password);

        if (!isEmailValid || !isPasswordValid) {
            return;
        }

        // Désactiver le bouton et montrer l'état de chargement
        setLoginButtonState(false, "Connexion en cours...");

        // Connexion Firebase
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    checkUserRoleAndRedirect(uid);
                })
                .addOnFailureListener(e -> {
                    setLoginButtonState(true, "Se connecter");

                    // Messages d'erreur spécifiques
                    String errorMessage = e.getMessage();
                    if (errorMessage != null) {
                        if (errorMessage.contains("invalid credential") ||
                                errorMessage.contains("wrong password")) {
                            tilPassword.setError("Mot de passe incorrect");
                            tilPassword.requestFocus();
                        } else if (errorMessage.contains("user not found")) {
                            tilEmail.setError("Aucun compte avec cet email");
                            tilEmail.requestFocus();
                        } else if (errorMessage.contains("network error")) {
                            Toast.makeText(this,
                                    "Erreur réseau. Vérifiez votre connexion internet.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,
                                    "Échec de la connexion: " + errorMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkUserRoleAndRedirect(String uid) {
        db.collection("Users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Profil utilisateur introuvable", Toast.LENGTH_SHORT).show();
                        setLoginButtonState(true, "Se connecter");
                        auth.signOut(); // Déconnecter car pas de profil
                        return;
                    }

                    String role = documentSnapshot.getString("role");
                    String nom = documentSnapshot.getString("nom");
                    String prenom = documentSnapshot.getString("prenom");
                    String email = documentSnapshot.getString("email");

                    // Construction du nom complet
                    String fullName = buildFullName(email, nom, prenom);

                    // Redirection selon le rôle
                    if ("superviseur".equals(role)) {
                        redirectToAdmin(fullName, nom, prenom);
                    } else if ("technicien".equals(role)) {
                        redirectToTechnician(fullName, nom, prenom, documentSnapshot);
                    } else {
                        Toast.makeText(this, "Rôle non reconnu", Toast.LENGTH_SHORT).show();
                        setLoginButtonState(true, "Se connecter");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de lecture du profil", Toast.LENGTH_SHORT).show();
                    setLoginButtonState(true, "Se connecter");
                });
    }

    private String buildFullName(String email, String nom, String prenom) {
        if (nom != null && prenom != null) {
            return nom + " " + prenom;
        } else if (nom != null) {
            return nom;
        } else if (prenom != null) {
            return prenom;
        } else {
            return email.split("@")[0];
        }
    }

    private void redirectToAdmin(String fullName, String nom, String prenom) {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra("fullName", fullName);
        intent.putExtra("nom", nom);
        intent.putExtra("prenom", prenom);
        startActivity(intent);
        finish();

        // Toast de confirmation
        Toast.makeText(this, "Connexion réussie - Superviseur", Toast.LENGTH_SHORT).show();
    }

    private void redirectToTechnician(String fullName, String nom, String prenom,
                                      com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
        Intent intent = new Intent(this, TechnicienHomeActivity.class);
        intent.putExtra("fullName", fullName);
        intent.putExtra("nom", nom);
        intent.putExtra("prenom", prenom);
        intent.putExtra("email", etEmail.getText().toString().trim());
        intent.putExtra("departement", documentSnapshot.getString("departement"));
        Long age = documentSnapshot.getLong("age");
        if (age != null) {
            intent.putExtra("age", age);
        }
        startActivity(intent);
        finish();

        // Toast de confirmation
        Toast.makeText(this, "Connexion réussie - Technicien", Toast.LENGTH_SHORT).show();
    }

    private void setLoginButtonState(boolean enabled, String text) {
        btnLogin.setEnabled(enabled);
        btnLogin.setText(text);

        if (enabled) {
            btnLogin.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.primary_color)
            );
        } else {
            btnLogin.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.disabled_color)
            );
        }
    }

    private void showForgotPasswordDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Mot de passe oublié")
                .setMessage("Voulez-vous recevoir un email de réinitialisation ?")
                .setPositiveButton("Envoyer", (dialog, which) -> {
                    String email = etEmail.getText().toString().trim();
                    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this,
                                "Veuillez entrer un email valide",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sendPasswordResetEmail(email);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Email de réinitialisation envoyé à " + email,
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Erreur lors de l'envoi de l'email: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Réactiver le bouton si nécessaire
        setLoginButtonState(true, "Se connecter");
    }
}