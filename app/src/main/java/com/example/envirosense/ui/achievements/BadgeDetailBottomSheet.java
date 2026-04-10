package com.example.envirosense.ui.achievements;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.envirosense.R;
import com.example.envirosense.data.Achievement;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BadgeDetailBottomSheet extends BottomSheetDialogFragment {

    private final Achievement achievement;

    public BadgeDetailBottomSheet(Achievement achievement) {
        this.achievement = achievement;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_badge_detail, container, false);

        FrameLayout iconContainer = view.findViewById(R.id.detail_icon_container);
        ImageView ivIcon = view.findViewById(R.id.iv_detail_icon);
        TextView tvTitle = view.findViewById(R.id.tv_detail_title);
        TextView tvStatus = view.findViewById(R.id.tv_detail_status);
        TextView tvProgressText = view.findViewById(R.id.tv_detail_progress_text);
        ProgressBar progressBar = view.findViewById(R.id.progress_badge);
        TextView tvDescription = view.findViewById(R.id.tv_detail_description);
        Button btnClose = view.findViewById(R.id.btn_close);

        tvTitle.setText(achievement.title);
        tvDescription.setText(achievement.longDescription);
        ivIcon.setImageResource(achievement.iconResId);
        progressBar.setMax(achievement.targetHours);

        int displayProgress = Math.min(achievement.currentProgressHrs, achievement.targetHours);
        progressBar.setProgress(displayProgress);

        if (achievement.id == 5) {
            tvProgressText.setText(achievement.currentProgressHrs + " / " + achievement.targetHours + " score");
        } else {
            tvProgressText.setText(achievement.currentProgressHrs + " / " + achievement.targetHours + " hrs");
        }

        if (achievement.isUnlocked) {
            tvStatus.setText("Unlocked");
            tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green text
            tvProgressText.setTextColor(Color.parseColor("#4CAF50"));

            iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1B2E24")));
            ivIcon.setColorFilter(Color.parseColor("#4CAF50")); // Green icon

        } else {
            tvStatus.setText("Locked");
            tvStatus.setTextColor(Color.parseColor("#888888")); // Gray text
            tvProgressText.setTextColor(Color.parseColor("#888888"));

            iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2A2C2E")));
            ivIcon.setColorFilter(Color.parseColor("#888888")); // Gray icon
        }

        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }
}