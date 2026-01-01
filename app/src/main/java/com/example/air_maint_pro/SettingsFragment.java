package com.example.air_maint_pro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private SwitchCompat switchDarkMode, switchNotifications;
    private Button btnLogout;
    private LinearLayout layoutSecurity, layoutHelp;

    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialisation
        auth = FirebaseAuth.getInstance();
        sharedPreferences = requireContext().getSharedPreferences("app_settings", 0);

        // Initialiser les vues
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        switchNotifications = view.findViewById(R.id.switchNotifications);
        btnLogout = view.findViewById(R.id.btnLogout);
        layoutSecurity = view.findViewById(R.id.layoutSecurity);
        layoutHelp = view.findViewById(R.id.layoutHelp);

        // Charger les préférences
        loadPreferences();

        // Écouteurs
        setupListeners();

        return view;
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
                // Ici vous pouvez implémenter le changement de thème
                // requireActivity().setTheme(R.style.AppTheme_Dark);
            } else {
                Toast.makeText(getContext(), "Mode clair activé", Toast.LENGTH_SHORT).show();
                // requireActivity().setTheme(R.style.AppTheme_Light);
            }

            // Redémarrer l'activité pour appliquer le thème
            // requireActivity().recreate();
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
            Toast.makeText(getContext(), "Ouverture des paramètres de sécurité", Toast.LENGTH_SHORT).show();
            // Ouvrir une activité ou fragment de sécurité
            // Intent intent = new Intent(getContext(), SecurityActivity.class);
            // startActivity(intent);
        });

        // Aide & Support
        layoutHelp.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ouverture de l'aide et support", Toast.LENGTH_SHORT).show();
            // Ouvrir une activité ou fragment d'aide
            // Intent intent = new Intent(getContext(), HelpActivity.class);
            // startActivity(intent);
        });

        // Déconnexion
        btnLogout.setOnClickListener(v -> {
            showLogoutConfirmation();
        });
    }

    private void showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
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

        Toast.makeText(getContext(), "Déconnexion réussie", Toast.LENGTH_SHORT).show();
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