package com.example.air_maint_pro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {

    private SwitchCompat switchDarkMode, switchNotifications;
    private Button btnLogout;
    private LinearLayout layoutSecurity, layoutHelp;


    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialisation
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = requireContext().getSharedPreferences("app_settings", 0);

        // Initialiser les vues
        initViews(view);

        // Charger les informations de l'utilisateur
        loadUserInfo();

        // Charger les préférences
        loadPreferences();

        // Écouteurs
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        switchNotifications = view.findViewById(R.id.switchNotifications);
        btnLogout = view.findViewById(R.id.btnLogout);
        layoutSecurity = view.findViewById(R.id.layoutSecurity);
        layoutHelp = view.findViewById(R.id.layoutHelp);

        // Optionnel: Ajouter un TextView pour afficher le nom/rôle
        // Vous pouvez ajouter ces TextView dans votre layout si vous voulez
        // tvUserRole = view.findViewById(R.id.tvUserRole);
        // tvUserName = view.findViewById(R.id.tvUserName);
    }

    private void loadUserInfo() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        String nom = documentSnapshot.getString("nom");
                        String prenom = documentSnapshot.getString("prenom");

                        // Personnaliser l'interface selon le rôle si nécessaire
                        customizeForRole(role, nom, prenom);
                    }
                })
                .addOnFailureListener(e -> {
                    // Ne rien faire en cas d'erreur
                });
    }

    private void customizeForRole(String role, String nom, String prenom) {
        // Vous pouvez personnaliser l'interface selon le rôle
        // Par exemple, cacher certaines options pour les techniciens
        if ("technicien".equals(role)) {
            // Optionnel: Cacher certaines sections pour les techniciens
            // layoutSecurity.setVisibility(View.GONE);

            // Afficher le nom du technicien
            String userName = "";
            if (nom != null && prenom != null) {
                userName = prenom + " " + nom;
            } else if (nom != null) {
                userName = nom;
            } else if (prenom != null) {
                userName = prenom;
            }

            // Mettre à jour le titre si vous voulez
            // ((TextView) getView().findViewById(R.id.tvTitle)).setText("Paramètres - " + userName);
        }
    }

    private void loadPreferences() {
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications", true);

        switchDarkMode.setChecked(darkModeEnabled);
        switchNotifications.setChecked(notificationsEnabled);
    }

    private void setupListeners() {
        // Mode sombre
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();

            if (isChecked) {
                Toast.makeText(getContext(), "Mode sombre activé", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Mode clair activé", Toast.LENGTH_SHORT).show();
            }
        });

        // Notifications
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notifications", isChecked).apply();

            if (isChecked) {
                Toast.makeText(getContext(), "Notifications activées", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Notifications désactivées", Toast.LENGTH_SHORT).show();
            }
        });

        // Sécurité
        layoutSecurity.setOnClickListener(v -> {
            showSecurityOptions();
        });

        // Aide & Support
        layoutHelp.setOnClickListener(v -> {
            showHelpAndSupport();
        });

        // Déconnexion
        btnLogout.setOnClickListener(v -> {
            showLogoutConfirmation();
        });
    }

    private void showSecurityOptions() {
        // Récupérer le rôle de l'utilisateur
        String userId = auth.getCurrentUser().getUid();

        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        androidx.appcompat.app.AlertDialog.Builder builder =
                                new androidx.appcompat.app.AlertDialog.Builder(requireContext());

                        if ("superviseur".equals(role)) {
                            builder.setTitle("Options de sécurité")
                                    .setItems(new String[]{"Changer mot de passe", "Authentification à deux facteurs"},
                                            (dialog, which) -> {
                                                if (which == 0) {
                                                    changePassword();
                                                } else if (which == 1) {
                                                    setupTwoFactorAuth();
                                                }
                                            });
                        } else {
                            // Options réduites pour les techniciens
                            builder.setTitle("Sécurité")
                                    .setMessage("Changer votre mot de passe")
                                    .setPositiveButton("Changer", (dialog, which) -> {
                                        changePassword();
                                    })
                                    .setNegativeButton("Annuler", null);
                        }

                        builder.show();
                    }
                });
    }

    private void changePassword() {
        Toast.makeText(getContext(),
                "Un email de réinitialisation va être envoyé",
                Toast.LENGTH_SHORT).show();

        // Envoyer un email de réinitialisation
        String email = auth.getCurrentUser().getEmail();
        if (email != null) {
            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(),
                                "Email de réinitialisation envoyé à " + email,
                                Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(),
                                "Erreur: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setupTwoFactorAuth() {
        Toast.makeText(getContext(),
                "Authentification à deux facteurs - Fonctionnalité à venir",
                Toast.LENGTH_SHORT).show();
    }

    private void showHelpAndSupport() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext());

        builder.setTitle("Aide & Support")
                .setItems(new String[]{"Documentation", "Contact support", "FAQ"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    Toast.makeText(getContext(), "Ouvrir la documentation", Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    Toast.makeText(getContext(), "Contacter le support", Toast.LENGTH_SHORT).show();
                                    break;
                                case 2:
                                    Toast.makeText(getContext(), "Foire aux questions", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        })
                .show();
    }

    private void showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext());

        builder.setTitle("Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                .setPositiveButton("Déconnexion", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void performLogout() {
        auth.signOut();

        // Rediriger vers l'écran de connexion
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();

        Toast.makeText(getContext(), "✅ Déconnexion réussie", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Sauvegarder les préférences
        sharedPreferences.edit()
                .putBoolean("dark_mode", switchDarkMode.isChecked())
                .putBoolean("notifications", switchNotifications.isChecked())
                .apply();
    }
}