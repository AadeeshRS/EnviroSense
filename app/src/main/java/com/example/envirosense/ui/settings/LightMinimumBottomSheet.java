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

public class LightMinimumBottomSheet extends BottomSheetDialogFragment {

    private final int MIN_LUX = 50;
    private int currentLux;


    private OnThresholdSavedListener listener;

    public interface OnThresholdSavedListener {
        void onSaved(int newLimit);
    }

    public LightMinimumBottomSheet(OnThresholdSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_light_minimum, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE);
        currentLux = prefs.getInt("light_min", 200);


        TextView tvCurrentValue = view.findViewById(R.id.tv_current_light_value);
        SeekBar seekBar = view.findViewById(R.id.seekbar_light);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnCancel = view.findViewById(R.id.btn_cancel);


        tvCurrentValue.setText(String.valueOf(currentLux));
        seekBar.setProgress(currentLux - MIN_LUX);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                currentLux = MIN_LUX + progress;
                tvCurrentValue.setText(String.valueOf(currentLux));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSave.setOnClickListener(v -> {
            prefs.edit().putInt("light_min", currentLux).apply();       //
            if (listener != null) listener.onSaved(currentLux);         //
            dismiss();                                                  //
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }
}