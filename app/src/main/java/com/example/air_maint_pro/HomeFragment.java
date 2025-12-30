package com.example.air_maint_pro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Constructeur vide requis
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflater le layout pour ce fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ici vous pouvez initialiser les éléments de l'interface
        TextView welcomeText = view.findViewById(R.id.welcome_text);

        // Exemple: Afficher un message de bienvenue
        welcomeText.setText("Tableau de Bord Superviseur\n\nStatistiques et activités à venir...");

        return view;
    }
}