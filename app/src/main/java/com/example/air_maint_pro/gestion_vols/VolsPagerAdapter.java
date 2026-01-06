package com.example.air_maint_pro.gestion_vols;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class VolsPagerAdapter extends FragmentStateAdapter {

    public VolsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public VolsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new VolListFragment();
            case 1:
                return new EquipageListFragment(); // Liste des membres individuels
            case 2:
                return new EquipeListFragment();   // Liste des équipes
            default:
                return new VolListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Trois onglets maintenant
    }
}