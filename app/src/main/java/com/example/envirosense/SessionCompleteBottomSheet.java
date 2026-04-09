package com.example.envirosense;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SessionCompleteBottomSheet extends BottomSheetDialogFragment {

    private int score;
    private String duration;
    private String location;

    public SessionCompleteBottomSheet(int score, String duration, String location) {
        this.score = score;
        this.duration = duration;
        this.location = location;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_session_complete, container, false);

        TextView tvScore = view.findViewById(R.id.tv_final_score);
        TextView tvDuration = view.findViewById(R.id.tv_final_duration);
        TextView tvLocation = view.findViewById(R.id.tv_final_location);

        tvScore.setText(String.valueOf(score));
        tvDuration.setText(duration);
        tvLocation.setText(location);

        view.findViewById(R.id.btn_done).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_view_analytics).setOnClickListener(v -> {
            dismiss();
        });

        return view;
    }
}