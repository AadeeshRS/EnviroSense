package com.example.envirosense.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.envirosense.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NoiseThresholdBottomSheet extends BottomSheetDialogFragment {

    private final int MIN_DB = 30;
    private int currentDb;
    private OnThresholdSavedListener listener;

    public interface OnThresholdSavedListener {
        void onSaved(int newLimit);
    }

    public NoiseThresholdBottomSheet(OnThresholdSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_noise_threshold, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE);
        currentDb = prefs.getInt("noise_limit", 65); // Default to 65

        TextView tvCurrentValue = view.findViewById(R.id.tv_current_noise_value);
        SeekBar seekBar = view.findViewById(R.id.seekbar_noise);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        tvCurrentValue.setText(String.valueOf(currentDb));
        seekBar.setProgress(currentDb - MIN_DB);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentDb = MIN_DB + progress;
                tvCurrentValue.setText(String.valueOf(currentDb));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        btnSave.setOnClickListener(v -> {
            prefs.edit().putInt("noise_limit", currentDb).apply();
            if (listener != null) listener.onSaved(currentDb);
            dismiss();
        });


        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }
}