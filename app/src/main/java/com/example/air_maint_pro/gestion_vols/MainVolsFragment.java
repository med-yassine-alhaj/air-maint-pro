package com.example.air_maint_pro.gestion_vols;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.air_maint_pro.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainVolsFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private VolsPagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_vols, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        setupViewPager();
        setupTabLayout();

        return view;
    }

    private void setupViewPager() {
        pagerAdapter = new VolsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3); // 3 fragments en mémoire
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Vols");
                    tab.setIcon(R.drawable.ic_flight);
                    break;
                case 1:
                    tab.setText("Équipages");
                    tab.setIcon(R.drawable.ic_group);
                    break;
                case 2:
                    tab.setText("Équipes");
                    tab.setIcon(R.drawable.ic_team);
                    break;
            }
        }).attach();
    }
}