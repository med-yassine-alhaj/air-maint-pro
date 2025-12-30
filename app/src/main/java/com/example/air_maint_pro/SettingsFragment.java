package com.example.air_maint_pro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private Switch darkModeSwitch;
    private Switch notificationsSwitch;

    public SettingsFragment() {
        // Constructeur vide requis
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mAuth = FirebaseAuth.getInstance();

        // Initialiser les switches
        darkModeSwitch = view.findViewById(R.id.dark_mode_switch);
        notificationsSwitch = view.findViewById(R.id.notifications_switch);

        // Bouton de sécurité
        Button securityButton = view.findViewById(R.id.security_button);
        Button helpButton = view.findViewById(R.id.help_button);
        Button logoutButton = view.findViewById(R.id.logout_button);

        // Version de l'app
        TextView versionText = view.findViewById(R.id.version_text);

        // Écouteurs pour les switches
        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getActivity(), "Mode sombre activé", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Mode sombre désactivé", Toast.LENGTH_SHORT).show();
                }
            }
        });

        notificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getActivity(), "Notifications activées", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Notifications désactivées", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Bouton Sécurité
        securityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Sécurité - À implémenter", Toast.LENGTH_SHORT).show();
            }
        });

        // Bouton Aide
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Aide & Support - À implémenter", Toast.LENGTH_SHORT).show();
            }
        });

        // Bouton Déconnexion
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        // Version de l'application
        versionText.setText("AirMaint Pro v1.0.0");

        return view;
    }
}