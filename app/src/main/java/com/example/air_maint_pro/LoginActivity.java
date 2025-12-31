package com.example.air_maint_pro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = authResult.getUser().getUid();

                    db.collection("Users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {

                                if (!documentSnapshot.exists()) {
                                    Toast.makeText(this, "Profil utilisateur introuvable", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String role = documentSnapshot.getString("role");

                                if ("supervisor".equals(role)) {
                                    startActivity(new Intent(this, AdminActivity.class));
                                } else if ("employee".equals(role)) {
                                    startActivity(new Intent(this, EmployeeActivity.class));
                                } else {
                                    Toast.makeText(this, "Rôle non reconnu", Toast.LENGTH_SHORT).show();
                                }

                                finish();

                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erreur Firestore", Toast.LENGTH_SHORT).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show()
                );
    }
}
