package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends CloudActivity implements TabLayout.OnTabSelectedListener {
    private TabLayout tabLayout;
    RestaurantFragment frgRestaurant;
    OrderFragment frgOrder;
    ProfileFragment frgProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        createTabs();
        tabLayout.addOnTabSelectedListener(this);

        frgRestaurant = new RestaurantFragment();
        frgOrder = new OrderFragment();
        frgProfile = new ProfileFragment();

        displayFragment(frgRestaurant);
    }

    private void createTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Nhà hàng"));
        tabLayout.addTab(tabLayout.newTab().setText("Đơn hàng"));
        tabLayout.addTab(tabLayout.newTab().setText("Tài khoản"));
    }

    protected void displayFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.layoutContainer, fragment);
        ft.commit();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()) {
            case 0:
                displayFragment(frgRestaurant);
                break;
            case 1:
                displayFragment(frgOrder);
                break;
            case 2:
                displayFragment(frgProfile);
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {}
}
