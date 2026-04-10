package com.example.envirosense.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.envirosense.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SessionCompleteBottomSheet extends BottomSheetDialogFragment {

    private int score;
    private String duration;
    private String location;

    public interface OnAnalyticsButtonClickListener {
        void onAnalyticsClicked(String finalLocation);
        void onDoneClicked(String finalLocation);
    }

    private OnAnalyticsButtonClickListener listener;

    public SessionCompleteBottomSheet(int score, String duration, String location, OnAnalyticsButtonClickListener listener) {
        this.score = score;
        this.duration = duration;
        this.location = location;
        this.listener = listener;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_session_complete, container, false);


        TextView tvScore = view.findViewById(R.id.tv_final_score);
        TextView tvDuration = view.findViewById(R.id.tv_final_duration);
        EditText etLocation = view.findViewById(R.id.et_final_location);

        tvScore.setText(String.valueOf(score));
        tvDuration.setText(duration);
        etLocation.setText(location);

        view.findViewById(R.id.btn_done).setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onDoneClicked(etLocation.getText().toString());
            }
        });
        view.findViewById(R.id.btn_view_analytics).setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onAnalyticsClicked(etLocation.getText().toString());
            }
        });

        return view;
    }
}