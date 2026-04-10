package com.example.envirosense.ui.achievements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.envirosense.R;
import com.example.envirosense.data.Achievement;
import com.example.envirosense.data.AppDatabase;
import com.example.envirosense.data.FocusSession;

import java.util.ArrayList;
import java.util.List;

public class AchievementsFragment extends Fragment {

    private TextView tvTotalHours;
    private RecyclerView rvAchievements;
    private AchievementAdapter adapter;
    private List<Achievement> allBadges;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);

        tvTotalHours = view.findViewById(R.id.tv_total_hours);
        rvAchievements = view.findViewById(R.id.rv_achievements);

        rvAchievements.setLayoutManager(new GridLayoutManager(getContext(), 2));

        initBadges();

        adapter = new AchievementAdapter(allBadges, achievement -> {
            BadgeDetailBottomSheet bottomSheet = new BadgeDetailBottomSheet(achievement);
            bottomSheet.show(getParentFragmentManager(), "BadgeDetailSheet");
        });
        rvAchievements.setAdapter(adapter);

        calculateTotalHours();

        return view;
    }

    private void initBadges() {
        allBadges = new ArrayList<>();

        allBadges.add(new Achievement(1, "First Focus", "Complete 1st session", "Complete your first ever focus session. Welcome to the club!", 1, android.R.drawable.btn_star));
        allBadges.add(new Achievement(2, "Consistent", "Reach 25 focus hours", "Consistency is key. You've hit 25 hours of deep work.", 25, android.R.drawable.ic_menu_recent_history));
        allBadges.add(new Achievement(3, "Focused Mind", "Reach 50 focus hours", "A focused mind achieves the impossible. Halfway to 100!", 50, android.R.drawable.ic_menu_view));
        allBadges.add(new Achievement(4, "Deep Worker", "Reach 100 focus hours", "100 hours of focused work in optimal environments. You're in rare company.", 100, android.R.drawable.ic_dialog_email));
        allBadges.add(new Achievement(5, "EnviroMaster", "Maintain 90+ Score", "Master of your environment. You consistently maintain optimal conditions.", 500, android.R.drawable.ic_menu_sort_by_size));
    }

    private void calculateTotalHours() {
        new Thread(() -> {
            List<FocusSession> sessions = AppDatabase.getInstance(requireContext()).focusSessionDao().getAllSessions();

            long totalDurationMs = 0;
            double totalAccumulatedScore = 0;
            for (FocusSession s : sessions) {
                totalDurationMs += s.durationMs;
                totalAccumulatedScore += s.finalScore;
            }


            int finalTotalHours = (int) ((totalDurationMs / 1000) / 60) / 60;

            final double lifetimeAverageScore;
            if (!sessions.isEmpty()) {
                lifetimeAverageScore = totalAccumulatedScore / sessions.size();
            } else {
                lifetimeAverageScore = 0;
            }
            if (!sessions.isEmpty() && finalTotalHours < 1) {
                finalTotalHours = 1;
            }


            boolean USE_SIMULATION = false;
            int SIMULATED_HOURS = 500;
            if (USE_SIMULATION) {
                finalTotalHours = SIMULATED_HOURS;
            }

            final int displayHours = finalTotalHours;

            requireActivity().runOnUiThread(() -> {
                tvTotalHours.setText(String.valueOf(displayHours));

                android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("EnviroSenseAchieve", android.content.Context.MODE_PRIVATE);

                for (Achievement badge : allBadges) {

                    if (badge.id != 5) {
                        badge.currentProgressHrs = displayHours;
                        if (displayHours >= badge.targetHours) {
                            badge.isUnlocked = true;
                        } else {
                            badge.isUnlocked = false;
                        }
                    }
                    else {
                        badge.currentProgressHrs = (int) lifetimeAverageScore;
                        badge.targetHours = 90;
                        badge.isUnlocked = lifetimeAverageScore >= 90.0 && sessions.size() >= 10;
                    }

                    if (badge.isUnlocked) {
                        boolean hasShownToastBefore = prefs.getBoolean("toast_shown_" + badge.id, false);
                        if (!hasShownToastBefore) {
                            showCustomAchievementToast(badge);
                            prefs.edit().putBoolean("toast_shown_" + badge.id, true).apply();
                        }
                    } else {
                        prefs.edit().putBoolean("toast_shown_" + badge.id, false).apply();
                    }


                }

                adapter.notifyDataSetChanged();
            });

        }).start();
    }

    private void showCustomAchievementToast(Achievement badge) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_achievement, null);

        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView title = layout.findViewById(R.id.toast_title);
        TextView desc = layout.findViewById(R.id.toast_desc);

        icon.setImageResource(badge.iconResId);
        title.setText(badge.title + " unlocked");
        desc.setText(badge.longDescription);

        android.widget.Toast toast = new android.widget.Toast(requireContext());
        toast.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.setDuration(android.widget.Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            calculateTotalHours();
        }
    }
}