package com.example.envirosense;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.envirosense.ui.achievements.AchievementsFragment;
import com.example.envirosense.ui.analytics.AnalyticsFragment;
import com.example.envirosense.ui.community.CommunityFragment;
import com.example.envirosense.ui.home.HomeFragment;
import com.example.envirosense.ui.settings.SettingsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private final Fragment homeFragment = new HomeFragment();
    private final Fragment analyticsFragment = new AnalyticsFragment();
    private final Fragment communityFragment = new CommunityFragment();
    private final Fragment settingsFragment = new SettingsFragment();
    private final Fragment achievementsFragment = new AchievementsFragment();

    private Fragment activeFragment = homeFragment;

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private NavigationView navView;

    // Flag to prevent infinite loops when syncing bottom nav <-> drawer
    private boolean isSyncing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Apply top inset (status bar) to the parent LinearLayout so the toolbar
        // sits naturally below the status bar with its internal icon/title alignment
        // intact. Bottom inset goes to the bottom nav. Consume insets to prevent
        // propagation to child views.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            BottomNavigationView bn = findViewById(R.id.bottom_nav);
            if (bn != null) {
                bn.setPadding(0, 0, 0, systemBars.bottom);
            }
            return WindowInsetsCompat.CONSUMED;
        });

        // Setup Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Tint the hamburger icon to white
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white, getTheme()));

        // Add all fragments (hide all except home) — preserves the existing lifecycle
        // pattern
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, settingsFragment, "settings").hide(settingsFragment)
                    .add(R.id.fragment_container, communityFragment, "community").hide(communityFragment)
                    .add(R.id.fragment_container, analyticsFragment, "analytics").hide(analyticsFragment)
                    .add(R.id.fragment_container, achievementsFragment, "achievements").hide(achievementsFragment)
                    .add(R.id.fragment_container, homeFragment, "home")
                    .commit();
        }

        // Setup Bottom Navigation
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            if (isSyncing)
                return true;
            isSyncing = true;
            switchFragment(item.getItemId());
            navView.setCheckedItem(item.getItemId());
            isSyncing = false;
            return true;
        });

    
        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(item -> {
            if (isSyncing)
                return true;
            isSyncing = true;
            int id = item.getItemId();
            if (id == R.id.navigation_settings) {

                switchFragment(id);
            } else {
                switchFragment(id);
                bottomNav.setSelectedItemId(id);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            isSyncing = false;
            return true;
        });

        // Set Home as checked by default
        navView.setCheckedItem(R.id.navigation_home);
    }

    /**
     * Central method for switching fragments. Both bottom nav and drawer call this.
     */
    private void switchFragment(int itemId) {
        Fragment targetFragment = null;
        String title = "EnviroSense";

        if (itemId == R.id.navigation_home) {
            targetFragment = homeFragment;
            title = "EnviroSense";
        } else if (itemId == R.id.navigation_analytics) {
            targetFragment = analyticsFragment;
            title = "Analytics";
        } else if (itemId == R.id.navigation_community) {
            targetFragment = communityFragment;
            title = "Community";
        } else if (itemId == R.id.navigation_achieve) {
            targetFragment = achievementsFragment;
            title = "Achievements";
        } else if (itemId == R.id.navigation_settings) {
            targetFragment = settingsFragment;
            title = "Settings";
        }

        if (targetFragment != null && targetFragment != activeFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit();
            activeFragment = targetFragment;
            toolbar.setTitle(title);
        }
    }

    /**
     * Public method for fragments to programmatically navigate to the Analytics
     * tab. Used by HomeFragment after a session is saved.
     */
    public void navigateToAnalytics() {
        if (analyticsFragment != activeFragment) {
            switchFragment(R.id.navigation_analytics);
            bottomNav.setSelectedItemId(R.id.navigation_analytics);
            navView.setCheckedItem(R.id.navigation_analytics);
        }
    }

    @Override
    public void onBackPressed() {
        // Close drawer on back press if it's open, otherwise use default behavior
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}