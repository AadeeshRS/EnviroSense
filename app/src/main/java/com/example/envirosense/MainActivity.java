package com.example.envirosense;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
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
import com.example.envirosense.ui.community.MyCommunityFragment;
import com.example.envirosense.ui.community.MyResourcesFragment;
import com.example.envirosense.ui.community.SearchCommunityFragment;
import com.example.envirosense.ui.home.HomeFragment;
import com.example.envirosense.ui.settings.SettingsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    // ── Core fragments ──────────────────────────────────────────────────────────
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment analyticsFragment = new AnalyticsFragment();
    private final Fragment communityFragment = new CommunityFragment();
    private final Fragment settingsFragment = new SettingsFragment();
    private final Fragment achievementsFragment = new AchievementsFragment();

    // ── Community sub-fragments ─────────────────────────────────────────────────
    private final Fragment myCommunityFragment = new MyCommunityFragment();
    private final Fragment searchCommunityFragment = new SearchCommunityFragment();
    private final Fragment myResourcesFragment = new MyResourcesFragment();

    private Fragment activeFragment = homeFragment;

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private NavigationView navView;

    /** Whether the community-specific bottom nav is currently shown. */
    private boolean isCommunityNavActive = false;

    /** Prevents infinite loops when syncing bottom nav ↔ drawer. */
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
                    .add(R.id.fragment_container, myResourcesFragment, "myResources").hide(myResourcesFragment)
                    .add(R.id.fragment_container, searchCommunityFragment, "searchCommunity")
                    .hide(searchCommunityFragment)
                    .add(R.id.fragment_container, myCommunityFragment, "myCommunity").hide(myCommunityFragment)
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

            int id = item.getItemId();

            if (isCommunityNavActive) {
                // ── Community nav item handling ─────────────────────────────────
                if (id == R.id.navigation_home) {
                    // Exit community nav and go home
                    exitCommunityNav();
                    switchFragment(R.id.navigation_home);
                    navView.setCheckedItem(R.id.navigation_home);

                    bottomNav.post(() -> {
                        boolean wasSyncing = isSyncing;
                        isSyncing = true;
                        bottomNav.setSelectedItemId(R.id.navigation_home);
                        isSyncing = wasSyncing;
                    });
                } else if (id == R.id.comm_leaderboard) {
                    showCommunitySubFragment(communityFragment, "Leaderboard");
                } else if (id == R.id.comm_my_communities) {
                    showCommunitySubFragment(myCommunityFragment, "My Groups");
                } else if (id == R.id.comm_search) {
                    showCommunitySubFragment(searchCommunityFragment, "Find Groups");
                } else if (id == R.id.comm_my_resources) {
                    showCommunitySubFragment(myResourcesFragment, "My Resources");
                }
            } else {
                // ── Global nav item handling ────────────────────────────────────
                if (id == R.id.navigation_community) {
                    enterCommunityNav();
                    navView.setCheckedItem(R.id.navigation_community);
                } else {
                    switchFragment(id);
                    navView.setCheckedItem(id);
                }
            }

            isSyncing = false;
            return true;
        });

        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(item -> {
            if (isSyncing)
                return true;
            isSyncing = true;

            int id = item.getItemId();

            if (id == R.id.navigation_community) {
                // Always enter community nav, regardless of current nav state
                if (!isCommunityNavActive) {
                    enterCommunityNav();
                }
                // Already in community hub — nothing to do
            } else if (id == R.id.navigation_settings) {
                // Settings has no bottom nav tab — exit community nav if needed,
                // then deselect all bottom nav items
                if (isCommunityNavActive) {
                    exitCommunityNav();
                }
                switchFragment(id);
                clearBottomNavSelection();
            } else {
                // Any other destination exits community nav
                if (isCommunityNavActive) {
                    exitCommunityNav();
                }
                switchFragment(id);
                bottomNav.post(() -> {
                    boolean wasSyncing = isSyncing;
                    isSyncing = true;
                    bottomNav.setSelectedItemId(id);
                    isSyncing = wasSyncing;
                });
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            isSyncing = false;
            return true;
        });

        // Set Home as checked by default
        navView.setCheckedItem(R.id.navigation_home);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Community nav helpers
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Swaps the bottom nav to the community-specific menu and displays
     * the community hub (Leaderboard). The Leaderboard is pre-selected.
     */
    private void enterCommunityNav() {
        isCommunityNavActive = true;
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.bottom_nav_community_menu);

        // Show the leaderboard fragment
        if (activeFragment != communityFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(communityFragment)
                    .commit();
            activeFragment = communityFragment;
        }
        toolbar.setTitle("Leaderboard");
        // Defer selection so BottomNavigationView finishes its previous click handling
        // before attempting to select the newly inflated menu item.
        bottomNav.post(() -> {
            boolean wasSyncing = isSyncing;
            isSyncing = true;
            bottomNav.setSelectedItemId(R.id.comm_leaderboard);
            isSyncing = wasSyncing;
        });
        updateToolbarButtons(communityFragment);
    }

    /**
     * Restores the global bottom nav menu. Called before navigating away
     * from any community section.
     */
    private void exitCommunityNav() {
        isCommunityNavActive = false;
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.bottom_nav_menu);
    }

    /**
     * Shows one of the three community sub-fragments (My Groups, Search,
     * Resources).
     * Does not swap the nav bar — already in community nav mode.
     */
    private void showCommunitySubFragment(Fragment target, String title) {
        if (target != activeFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(target)
                    .commit();
            activeFragment = target;
        }
        toolbar.setTitle(title);
        updateToolbarButtons(target);
    }

    /**
     * Deselects all items in the current bottom nav menu.
     * Used when entering Community hub (no sub-item active) or Settings.
     */
    private void clearBottomNavSelection() {
        bottomNav.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setChecked(false);
        }
        bottomNav.getMenu().setGroupCheckable(0, true, true);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Fragment switching
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Central method for switching top-level fragments.
     * Both bottom nav and drawer call this for standard (non-community) navigation.
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
            updateToolbarButtons(targetFragment);
        }
    }

    private void updateToolbarButtons(Fragment target) {
        android.widget.ImageView btnCreate = findViewById(R.id.btn_create_community);
        if (btnCreate != null) {
            if (target instanceof MyCommunityFragment) {
                btnCreate.setVisibility(android.view.View.VISIBLE);
                btnCreate.setOnClickListener(v -> {
                    com.example.envirosense.ui.community.CreateCommunityBottomSheet bottomSheet = new com.example.envirosense.ui.community.CreateCommunityBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "CreateCommunity");
                });
            } else {
                btnCreate.setVisibility(android.view.View.GONE);
                btnCreate.setOnClickListener(null);
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Public API for fragments
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Public method for fragments to programmatically navigate to the Analytics
     * tab. Used by HomeFragment after a session is saved.
     */
    public void navigateToAnalytics() {
        if (analyticsFragment != activeFragment) {
            if (isCommunityNavActive) {
                exitCommunityNav();
            }
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