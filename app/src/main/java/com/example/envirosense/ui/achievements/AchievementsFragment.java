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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class AchievementsFragment extends Fragment {

    private TextView tvTotalHours;
    private RecyclerView rvAchievements;
    private AchievementAdapter adapter;
    private List<Achievement> allBadges;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
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
        allBadges = AchievementManager.getBadges();
    }

    private void calculateTotalHours() {
        new Thread(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = user != null ? user.getUid() : "guest";
            List<FocusSession> sessions = AppDatabase.getInstance(requireContext()).focusSessionDao().getAllSessions(uid);

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

                // For the simulation, pretend we have a high score and enough sessions
                double finalAvgScore = USE_SIMULATION ? 95.0 : lifetimeAverageScore;
                int finalSessionCount = USE_SIMULATION ? 10 : sessions.size();

                android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("EnviroSenseAchieve",
                        android.content.Context.MODE_PRIVATE);

                for (Achievement badge : allBadges) {
                    if (badge.id != 5) {
                        badge.currentProgressHrs = displayHours;
                        if (displayHours >= badge.targetHours) {
                            badge.isUnlocked = true;
                        } else {
                            badge.isUnlocked = false;
                        }
                    } else {
                        badge.currentProgressHrs = (int) finalAvgScore;
                        badge.targetHours = 90;
                        badge.isUnlocked = finalAvgScore >= 90.0 && finalSessionCount >= 10;
                    }
                }

                adapter.notifyDataSetChanged();
            });

        }).start();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            calculateTotalHours();
        }
    }
}