package com.example.air_maint_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private TextInputLayout emailLayout, passwordLayout;
    private MaterialButton loginButton;
    private TextView errorTextView;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialiser Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialiser les vues
        initViews();

        // Vérifier si l'utilisateur est déjà connecté
        checkCurrentUser();

        // Écouteur du bouton de connexion
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        loginButton = findViewById(R.id.loginButton);
        errorTextView = findViewById(R.id.errorTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Utilisateur déjà connecté, aller au dashboard superviseur
            startActivity(new Intent(LoginActivity.this, SupervisorDashboardActivity.class));
            finish();
        }
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validation des champs
        if (!validateForm(email, password)) {
            return;
        }

        // Afficher le progress bar
        showLoading(true);

        // Authentification avec Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);

                        if (task.isSuccessful()) {
                            // Connexion réussie
                            FirebaseUser user = mAuth.getCurrentUser();

                            // MODIFICATION ICI : Accepter la connexion même sans email vérifié
                            if (user != null) {
                                showMessage("Connexion réussie! Bienvenue superviseur.");

                                // Aller au dashboard superviseur
                                startActivity(new Intent(LoginActivity.this, SupervisorDashboardActivity.class));
                                finish();
                            }
                        } else {
                            // Échec de la connexion
                            handleLoginError(task.getException());
                        }
                    }
                });
    }

    private void handleLoginError(Exception exception) {
        String errorMessage;
        if (exception != null) {
            String exceptionMessage = exception.getMessage();
            if (exceptionMessage.contains("password")) {
                errorMessage = "Mot de passe incorrect";
            } else if (exceptionMessage.contains("no user record") ||
                    exceptionMessage.contains("invalid email") ||
                    exceptionMessage.contains("user not found")) {
                errorMessage = "Aucun compte superviseur trouvé avec cet email";
            } else if (exceptionMessage.contains("network")) {
                errorMessage = "Erreur de connexion réseau. Vérifiez votre connexion internet.";
            } else if (exceptionMessage.contains("too many requests")) {
                errorMessage = "Trop de tentatives. Réessayez plus tard.";
            } else {
                errorMessage = "Échec de la connexion: " + exceptionMessage;
            }
        } else {
            errorMessage = "Échec de la connexion";
        }
        showError(errorMessage);
    }

    private boolean validateForm(String email, String password) {
        boolean isValid = true;

        // Réinitialiser les erreurs
        emailLayout.setError(null);
        passwordLayout.setError(null);
        errorTextView.setVisibility(View.GONE);

        // Validation de l'email
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("L'email est requis");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Format d'email invalide");
            isValid = false;
        }

        // Validation du mot de passe
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Le mot de passe est requis");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Minimum 6 caractères");
            isValid = false;
        }

        return isValid;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setText("Connexion en cours...");
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setText("Se connecter");
        }
        loginButton.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
        passwordEditText.setEnabled(!isLoading);
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showError(String message) {
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Vérifier à nouveau au démarrage de l'activité
        checkCurrentUser();
    }
}