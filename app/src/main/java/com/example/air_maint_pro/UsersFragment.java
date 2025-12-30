package com.example.air_maint_pro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class UsersFragment extends Fragment {

    public UsersFragment() {
        // Constructeur vide requis
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        try {
            // Boutons pour gérer les utilisateurs
            Button addUserButton = view.findViewById(R.id.add_user_button);
            Button listUsersButton = view.findViewById(R.id.list_users_button);
            Button modifyUserButton = view.findViewById(R.id.modify_user_button);
            Button deleteUserButton = view.findViewById(R.id.delete_user_button);

            addUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(new Intent(getActivity(), AddEditTechnicianActivity.class));
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            listUsersButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(new Intent(getActivity(), TechniciansListActivity.class));
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            modifyUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(),
                            "Sélectionnez un technicien depuis la liste",
                            Toast.LENGTH_LONG).show();
                    try {
                        startActivity(new Intent(getActivity(), TechniciansListActivity.class));
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            deleteUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(),
                            "Sélectionnez un technicien depuis la liste",
                            Toast.LENGTH_LONG).show();
                    try {
                        startActivity(new Intent(getActivity(), TechniciansListActivity.class));
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Erreur d'initialisation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return view;
    }
}