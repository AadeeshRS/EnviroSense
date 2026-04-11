package com.example.envirosense.ui.achievements;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.envirosense.R;
import com.example.envirosense.data.Achievement;
import com.example.envirosense.data.AppDatabase;
import com.example.envirosense.data.FocusSession;

import java.util.ArrayList;
import java.util.List;

public class AchievementManager {

    public static List<Achievement> getBadges() {
        List<Achievement> badges = new ArrayList<>();
        badges.add(new Achievement(1, "First Focus", "Complete 1st session",
                "Complete your first ever focus session. Welcome to the club!", 1, android.R.drawable.btn_star));
        badges.add(new Achievement(2, "Consistent", "Reach 25 focus hours",
                "Consistency is key. You've hit 25 hours of deep work.", 25,
                android.R.drawable.ic_menu_recent_history));
        badges.add(new Achievement(3, "Focused Mind", "Reach 50 focus hours",
                "A focused mind achieves the impossible. Halfway to 100!", 50, android.R.drawable.ic_menu_view));
        badges.add(new Achievement(4, "Deep Worker", "Reach 100 focus hours",
                "100 hours of focused work in optimal environments. You're in rare company.", 100,
                android.R.drawable.ic_dialog_email));
        badges.add(new Achievement(5, "EnviroMaster", "Maintain 90+ Score",
                "Master of your environment. You consistently maintain optimal conditions.", 500,
                android.R.drawable.ic_menu_sort_by_size));
        return badges;
    }

    public static void checkUnlocks(Context context) {
        new Thread(() -> {
            List<FocusSession> sessions = AppDatabase.getInstance(context).focusSessionDao().getAllSessions();

            if (sessions.isEmpty()) return;

            long totalDurationMs = 0;
            double totalAccumulatedScore = 0;
            for (FocusSession s : sessions) {
                totalDurationMs += s.durationMs;
                totalAccumulatedScore += s.finalScore;
            }

            int finalTotalHours = (int) ((totalDurationMs / 1000) / 60) / 60;
            double lifetimeAverageScore = totalAccumulatedScore / sessions.size();


            if (sessions.size() >= 1 && finalTotalHours < 1) {
                finalTotalHours = 1;
            }

            SharedPreferences prefs = context.getSharedPreferences("EnviroSenseAchieve", Context.MODE_PRIVATE);
            List<Achievement> allBadges = getBadges();

            long delayMs = 0;

            for (Achievement badge : allBadges) {
                boolean isUnlocked = false;

                if (badge.id != 5) {
                    if (finalTotalHours >= badge.targetHours) {
                        isUnlocked = true;
                    }
                } else {
                    isUnlocked = lifetimeAverageScore >= 90.0 && sessions.size() >= 10;
                }

                if (isUnlocked) {
                    boolean hasShownToastBefore = prefs.getBoolean("toast_shown_" + badge.id, false);
                    if (!hasShownToastBefore) {
                        prefs.edit().putBoolean("toast_shown_" + badge.id, true).apply();

                        long currentDelay = delayMs;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            showCustomAchievementToast(context, badge);
                        }, currentDelay);

                        delayMs += 4000;
                    }else {
                        prefs.edit().putBoolean("toast_shown_" + badge.id, false).apply();
                    }
                }

            }
        }).start();
    }

    private static void showCustomAchievementToast(Context context, Achievement badge) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.toast_achievement, null);

        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView title = layout.findViewById(R.id.toast_title);
        TextView desc = layout.findViewById(R.id.toast_desc);

        icon.setImageResource(badge.iconResId);
        title.setText(badge.title + " unlocked!");
        desc.setText(badge.longDescription);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}