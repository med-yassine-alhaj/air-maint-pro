package com.example.air_maint_pro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {

    private SwitchCompat switchDarkMode, switchNotifications;
    private Button btnLogout;
    private LinearLayout layoutSecurity, layoutHelp;
    private TextView tvUserName, tvUserRole;

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

        // Ajouter ces TextViews dans votre layout si vous voulez afficher les infos utilisateur
        // tvUserName = view.findViewById(R.id.tvUserName);
        // tvUserRole = view.findViewById(R.id.tvUserRole);
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

                        // Afficher les infos utilisateur si vous avez les TextViews
                        if (tvUserName != null) {
                            String fullName = (prenom != null ? prenom + " " : "") + (nom != null ? nom : "");
                            if (!fullName.trim().isEmpty()) {
                                tvUserName.setText(fullName);
                            }
                        }

                        if (tvUserRole != null && role != null) {
                            tvUserRole.setText(role.equals("superviseur") ? "Superviseur" : "Technicien");
                        }
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
            applyDarkMode(isChecked);
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

    private void applyDarkMode(boolean enabled) {
        // Sauvegarder le choix de l'utilisateur
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("dark_mode", enabled);
        editor.apply();

        // Appliquer le mode sombre à l'application entière
        AppCompatDelegate.setDefaultNightMode(
                enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Animation de transition
        if (getActivity() != null) {
            getActivity().getWindow().setExitTransition(new Fade());
            getActivity().getWindow().setEnterTransition(new Fade());
        }

        // Délai pour une meilleure animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity() != null) {
                getActivity().recreate();
            }
        }, 300);

        if (enabled) {
            Toast.makeText(getContext(), "Mode sombre activé", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Mode clair activé", Toast.LENGTH_SHORT).show();
        }
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
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext());

        builder.setTitle("Changer le mot de passe")
                .setItems(new String[]{"Réinitialisation par email", "Changer manuellement"},
                        (dialog, which) -> {
                            if (which == 0) {
                                sendPasswordResetEmail();
                            } else {
                                showManualPasswordChangeDialog();
                            }
                        })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void sendPasswordResetEmail() {
        // Afficher un dialogue de confirmation
        androidx.appcompat.app.AlertDialog.Builder confirmBuilder =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext());

        confirmBuilder.setTitle("Confirmation")
                .setMessage("Un email de réinitialisation sera envoyé à votre adresse email. Continuer ?")
                .setPositiveButton("Envoyer", (dialog, which) -> {
                    processPasswordResetEmail();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void processPasswordResetEmail() {
        String email = auth.getCurrentUser().getEmail();

        if (email == null || email.isEmpty()) {
            Toast.makeText(getContext(),
                    "Email non disponible",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Afficher un indicateur de chargement
        androidx.appcompat.app.AlertDialog loadingDialog =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setView(R.layout.dialog_loading)
                        .setCancelable(false)
                        .create();

        loadingDialog.show();

        // Action codes de langues disponibles
        // Pour envoyer en français :
        auth.setLanguageCode("fr");

        // Envoyer l'email de réinitialisation
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    loadingDialog.dismiss();

                    // Dialogue de succès avec instructions
                    androidx.appcompat.app.AlertDialog.Builder successBuilder =
                            new androidx.appcompat.app.AlertDialog.Builder(requireContext());

                    successBuilder.setTitle("✅ Email envoyé")
                            .setMessage("Un email de réinitialisation a été envoyé à :\n\n" + email +
                                    "\n\nVeuillez vérifier votre boîte de réception et suivre les instructions dans l'email.")
                            .setPositiveButton("J'ai compris", null)
                            .setNeutralButton("Ouvrir Gmail", (dialog, which) -> {
                                // Ouvrir l'application email
                                openEmailApp();
                            })
                            .show();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();

                    String errorMessage = e.getMessage();
                    String userMessage;

                    // Messages d'erreur personnalisés
                    if (errorMessage.contains("user-not-found")) {
                        userMessage = "Aucun compte trouvé avec cet email";
                    } else if (errorMessage.contains("invalid-email")) {
                        userMessage = "Adresse email invalide";
                    } else if (errorMessage.contains("network-request-failed")) {
                        userMessage = "Erreur de réseau. Vérifiez votre connexion internet.";
                    } else {
                        userMessage = "Erreur: " + errorMessage;
                    }

                    Toast.makeText(getContext(), userMessage, Toast.LENGTH_LONG).show();

                    // Option pour réessayer
                    androidx.appcompat.app.AlertDialog.Builder errorBuilder =
                            new androidx.appcompat.app.AlertDialog.Builder(requireContext());

                    errorBuilder.setTitle("Erreur")
                            .setMessage(userMessage)
                            .setPositiveButton("Réessayer", (dialog, which) -> {
                                processPasswordResetEmail();
                            })
                            .setNegativeButton("Annuler", null)
                            .show();
                });
    }
    private void openEmailApp() {
        try {
            // Intent pour ouvrir Gmail ou l'application email par défaut
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            // Si aucune application email n'est trouvée
            Toast.makeText(getContext(),
                    "Aucune application email trouvée",
                    Toast.LENGTH_SHORT).show();
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
        // Sauvegarder les préférences avant de se déconnecter
        sharedPreferences.edit()
                .putBoolean("dark_mode", switchDarkMode.isChecked())
                .putBoolean("notifications", switchNotifications.isChecked())
                .apply();

        auth.signOut();

        // Rediriger vers l'écran de connexion
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();

        Toast.makeText(getContext(), "✅ Déconnexion réussie", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Appliquer le mode sombre actuel
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
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
    private void showManualPasswordChangeDialog() {
        // Créer un dialogue personnalisé pour changer le mot de passe
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Changer le mot de passe")
                        .setView(dialogView)
                        .setPositiveButton("Changer", null) // Géré plus tard
                        .setNegativeButton("Annuler", null)
                        .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String currentPass = etCurrentPassword.getText().toString();
                String newPass = etNewPassword.getText().toString();
                String confirmPass = etConfirmPassword.getText().toString();

                if (validatePasswordChange(currentPass, newPass, confirmPass)) {
                    changePasswordManually(currentPass, newPass, dialog);
                }
            });
        });

        dialog.show();
    }

    private boolean validatePasswordChange(String currentPass, String newPass, String confirmPass) {
        if (currentPass.isEmpty()) {
            Toast.makeText(getContext(), "Le mot de passe actuel est requis", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPass.length() < 6) {
            Toast.makeText(getContext(), "Le nouveau mot de passe doit faire au moins 6 caractères", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(getContext(), "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void changePasswordManually(String currentPassword, String newPassword, androidx.appcompat.app.AlertDialog dialog) {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recréer l'utilisateur avec l'email et l'ancien mot de passe
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Mettre à jour le mot de passe
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                dialog.dismiss();
                                Toast.makeText(getContext(),
                                        "✅ Mot de passe changé avec succès",
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(),
                                        "Erreur: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Mot de passe actuel incorrect",
                            Toast.LENGTH_SHORT).show();
                });
}}