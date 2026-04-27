package com.example.envirosense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.Deque;

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
import com.example.envirosense.ui.auth.LoginActivity;
import com.example.envirosense.ui.community.CommunityFragment;
import com.example.envirosense.ui.community.MyCommunityFragment;
import com.example.envirosense.ui.community.MyResourcesFragment;
import com.example.envirosense.ui.community.SearchCommunityFragment;
import com.example.envirosense.ui.home.HomeFragment;
import com.example.envirosense.ui.settings.SettingsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment analyticsFragment = new AnalyticsFragment();
    private final Fragment communityFragment = new CommunityFragment();
    private final Fragment settingsFragment = new SettingsFragment();
    private final Fragment achievementsFragment = new AchievementsFragment();
    private final Fragment profileFragment = new com.example.envirosense.ui.auth.ProfileFragment();

    private final Fragment myCommunityFragment = new MyCommunityFragment();
    private final Fragment searchCommunityFragment = new SearchCommunityFragment();
    private final Fragment myResourcesFragment = new MyResourcesFragment();

    private Fragment activeFragment = homeFragment;

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private NavigationView navView;
    private boolean isCommunityNavActive = false;

    private boolean isSyncing = false;

    // ── Navigation back stack ────────────────────────────────────────────
    private static class NavState {
        final Fragment fragment;
        final boolean communityNav;
        final String title;
        NavState(Fragment fragment, boolean communityNav, String title) {
            this.fragment = fragment;
            this.communityNav = communityNav;
            this.title = title;
        }
    }
    private final Deque<NavState> backStack = new ArrayDeque<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            BottomNavigationView bn = findViewById(R.id.bottom_nav);
            if (bn != null) {
                bn.setPadding(0, 0, 0, systemBars.bottom);
            }
            return WindowInsetsCompat.CONSUMED;
        });

        
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white, getTheme()));

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == androidx.drawerlayout.widget.DrawerLayout.STATE_DRAGGING || newState == androidx.drawerlayout.widget.DrawerLayout.STATE_SETTLING) {
                    boolean personalActive = ((HomeFragment) homeFragment).isTrackingActive();
                    boolean groupActive = com.example.envirosense.ui.community.SharedFocusTracker.getInstance().isTracking();
                    android.view.MenuItem returnItem = navView.getMenu().findItem(R.id.navigation_return_session);
                    if (returnItem != null) {
                        returnItem.setVisible(personalActive || groupActive);
                    }
                }
            }
        });
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
                    .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                    .add(R.id.fragment_container, homeFragment, "home")
                    .commit();
        }


        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            if (isSyncing)
                return true;
            isSyncing = true;

            int id = item.getItemId();

            if (isCommunityNavActive) {

                if (id == R.id.navigation_home) {

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
                
                if (id == R.id.navigation_community) {
                    enterCommunityNav();
                    navView.setCheckedItem(R.id.navigation_community);
                } else {
                    switchFragment(id);
                    navView.setCheckedItem(id);
                    bottomNav.post(() -> {
                        boolean wasSyncing = isSyncing;
                        isSyncing = true;
                        bottomNav.getMenu().findItem(id).setChecked(true);
                        isSyncing = wasSyncing;
                    });
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

            if (id == R.id.navigation_return_session) {
                if (((HomeFragment) homeFragment).isTrackingActive()) {
                    navigateToHome();
                } else if (com.example.envirosense.ui.community.SharedFocusTracker.getInstance().isTracking()) {
                    android.content.SharedPreferences prefs = getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE);
                    Intent intent = new Intent(MainActivity.this, com.example.envirosense.ui.community.GroupSessionActivity.class);
                    intent.putExtra("GROUP_NAME", prefs.getString("ACTIVE_GROUP_NAME", ""));
                    intent.putExtra("GROUP_EMOJI", prefs.getString("ACTIVE_GROUP_EMOJI", ""));
                    intent.putExtra("AVG_SCORE", prefs.getInt("ACTIVE_GROUP_AVG_SCORE", 72));
                    intent.putExtra("ACTIVE_MEMBERS", prefs.getInt("ACTIVE_GROUP_MEMBERS", 0));
                    startActivity(intent);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                isSyncing = false;
                return true;
            } else if (id == R.id.navigation_sign_out) {
                FirebaseAuth.getInstance().signOut();
                getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE)
                        .edit().remove("user_name").apply();

                startActivity(new Intent(this, LoginActivity.class));
                finish();
                isSyncing = false;
                return true;
            } else if (id == R.id.navigation_community) {
                if (!isCommunityNavActive) {
                    enterCommunityNav();
                }
            } else if (id == R.id.navigation_settings || id == R.id.navigation_profile) {
                if (isCommunityNavActive) {
                    exitCommunityNav();
                }
                switchFragment(id);
                clearBottomNavSelection();
            } else {
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

        navView.setCheckedItem(R.id.navigation_home);
        loadNavHeader();
    }

   
    private void enterCommunityNav() {
        // Push current state before switching
        pushBackStack();
        isCommunityNavActive = true;
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.bottom_nav_community_menu);

        if (activeFragment != communityFragment) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .hide(activeFragment)
                    .show(communityFragment)
                    .commit();
            activeFragment = communityFragment;
        }
        toolbar.setTitle("Leaderboard");
       
        bottomNav.post(() -> {
            boolean wasSyncing = isSyncing;
            isSyncing = true;
            bottomNav.setSelectedItemId(R.id.comm_leaderboard);
            isSyncing = wasSyncing;
        });
        updateToolbarButtons(communityFragment);
    }

    
    private void exitCommunityNav() {
        isCommunityNavActive = false;
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.bottom_nav_menu);
    }

    
    private void showCommunitySubFragment(Fragment target, String title) {
        if (target != activeFragment) {
            // Push current community sub-state before switching
            pushBackStack();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .hide(activeFragment)
                    .show(target)
                    .commit();
            activeFragment = target;
        }
        toolbar.setTitle(title);
        updateToolbarButtons(target);
    }

   
    private void clearBottomNavSelection() {
        bottomNav.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setChecked(false);
        }
        bottomNav.getMenu().setGroupCheckable(0, true, true);
    }

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
        } else if (itemId == R.id.navigation_profile) {
            targetFragment = profileFragment;
            title = "Profile";
        } else if (itemId == R.id.navigation_settings) {
            targetFragment = settingsFragment;
            title = "Settings";
        }

        if (targetFragment != null && targetFragment != activeFragment) {
            // Push current state before switching
            pushBackStack();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
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

    private void loadNavHeader() {
        android.view.View headerView = navView.getHeaderView(0);
        TextView tvAvatar = headerView.findViewById(R.id.nav_header_avatar);
        TextView tvName = headerView.findViewById(R.id.nav_header_name);
        TextView tvEmail = headerView.findViewById(R.id.nav_header_email);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && tvEmail != null) {
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        }

        String cachedName = getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE)
                .getString("user_name", null);

        if (cachedName != null && tvName != null && tvAvatar != null) {
            tvName.setText(cachedName);
            tvAvatar.setText(cachedName.substring(0, 1).toUpperCase());
        } else if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String name = doc.getString("name");
                        if (name != null && tvName != null && tvAvatar != null) {
                            tvName.setText(name);
                            tvAvatar.setText(name.substring(0, 1).toUpperCase());
                            getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE)
                                    .edit().putString("user_name", name).apply();
                        }
                    });
        }
    }

    public void refreshNavHeader() {
        loadNavHeader();
    }

   
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

    public void navigateToHome() {
        if (homeFragment != activeFragment) {
            if (isCommunityNavActive) {
                exitCommunityNav();
            }
            switchFragment(R.id.navigation_home);
            clearBottomNavSelection();
            navView.setCheckedItem(R.id.navigation_home);
        }
    }

    public void navigateToSettings() {
        if (settingsFragment != activeFragment) {
            if (isCommunityNavActive) {
                exitCommunityNav();
            }
            switchFragment(R.id.navigation_settings);
            clearBottomNavSelection();
            navView.setCheckedItem(R.id.navigation_settings);
        }
    }

    // ── Back stack helpers ────────────────────────────────────────────────

    private void pushBackStack() {
        String title = toolbar.getTitle() != null ? toolbar.getTitle().toString() : "EnviroSense";
        backStack.push(new NavState(activeFragment, isCommunityNavActive, title));
    }

    @Override
    public void onBackPressed() {
        // 1. Close drawer if open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        // 2. If back stack has entries, pop and restore
        if (!backStack.isEmpty()) {
            NavState prev = backStack.pop();

            // Handle community nav mode transition
            if (prev.communityNav != isCommunityNavActive) {
                if (prev.communityNav) {
                    isCommunityNavActive = true;
                    bottomNav.getMenu().clear();
                    bottomNav.inflateMenu(R.menu.bottom_nav_community_menu);
                } else {
                    isCommunityNavActive = false;
                    bottomNav.getMenu().clear();
                    bottomNav.inflateMenu(R.menu.bottom_nav_menu);
                }
            }

            // Switch to the previous fragment
            if (prev.fragment != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .hide(activeFragment)
                        .show(prev.fragment)
                        .commit();
                activeFragment = prev.fragment;
            }

            toolbar.setTitle(prev.title);
            updateToolbarButtons(activeFragment);
            syncBottomNavSelection();
            return;
        }

        // 3. If not on home, go home first
        if (activeFragment != homeFragment) {
            if (isCommunityNavActive) {
                exitCommunityNav();
            }
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .hide(activeFragment)
                    .show(homeFragment)
                    .commit();
            activeFragment = homeFragment;
            toolbar.setTitle("EnviroSense");
            updateToolbarButtons(homeFragment);
            syncBottomNavSelection();
            backStack.clear();
            return;
        }

        // 4. Already on home with empty stack – exit app
        super.onBackPressed();
    }

    /**
     * Keeps the bottom nav checked-item in sync with the currently active fragment.
     */
    private void syncBottomNavSelection() {
        boolean wasSyncing = isSyncing;
        isSyncing = true;

        if (isCommunityNavActive) {
            if (activeFragment == communityFragment) {
                bottomNav.setSelectedItemId(R.id.comm_leaderboard);
            } else if (activeFragment == myCommunityFragment) {
                bottomNav.setSelectedItemId(R.id.comm_my_communities);
            } else if (activeFragment == searchCommunityFragment) {
                bottomNav.setSelectedItemId(R.id.comm_search);
            } else if (activeFragment == myResourcesFragment) {
                bottomNav.setSelectedItemId(R.id.comm_my_resources);
            }
        } else {
            if (activeFragment == homeFragment) {
                bottomNav.setSelectedItemId(R.id.navigation_home);
            } else if (activeFragment == analyticsFragment) {
                bottomNav.setSelectedItemId(R.id.navigation_analytics);
            } else if (activeFragment == achievementsFragment) {
                bottomNav.setSelectedItemId(R.id.navigation_achieve);
            } else if (activeFragment == communityFragment) {
                bottomNav.setSelectedItemId(R.id.navigation_community);
            } else {
                // Settings / Profile are drawer-only – clear bottom nav selection
                clearBottomNavSelection();
            }
        }
        navView.setCheckedItem(
                activeFragment == homeFragment ? R.id.navigation_home :
                activeFragment == analyticsFragment ? R.id.navigation_analytics :
                activeFragment == achievementsFragment ? R.id.navigation_achieve :
                activeFragment == settingsFragment ? R.id.navigation_settings :
                activeFragment == profileFragment ? R.id.navigation_profile :
                R.id.navigation_community
        );

        isSyncing = wasSyncing;
    }
}