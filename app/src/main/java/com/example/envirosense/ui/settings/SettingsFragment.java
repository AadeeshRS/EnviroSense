package com.example.envirosense.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.example.envirosense.R;
import android.content.Intent;
import android.app.NotificationManager;

public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;
    private TextView tvNoiseLimitValue, tvLightMinValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = requireActivity().getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE);

        SwitchCompat switchDnd = view.findViewById(R.id.switch_dnd);
        SwitchCompat switchReminders = view.findViewById(R.id.switch_reminders);
        SwitchCompat switchWeekly = view.findViewById(R.id.switch_weekly_report);

        tvNoiseLimitValue = view.findViewById(R.id.tv_noise_limit_value);
        tvLightMinValue = view.findViewById(R.id.tv_light_min_value);

        LinearLayout rowNoise = view.findViewById(R.id.row_noise_limit);
        LinearLayout rowLight = view.findViewById(R.id.row_light_minimum);
        LinearLayout rowDelete = view.findViewById(R.id.row_delete_data);
        LinearLayout rowExport = view.findViewById(R.id.row_export_data);

        switchDnd.setChecked(prefs.getBoolean("dnd_enabled", false));
        switchReminders.setChecked(prefs.getBoolean("reminders_enabled", true));
        switchWeekly.setChecked(prefs.getBoolean("weekly_report_enabled", false));

        tvNoiseLimitValue.setText(prefs.getInt("noise_limit", 65) + " dB");
        tvLightMinValue.setText(prefs.getInt("light_min", 200) + " lux");

        switchDnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationManager nm = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (isChecked && !nm.isNotificationPolicyAccessGranted()) {
                buttonView.setChecked(false);

                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
                Toast.makeText(getContext(), "Please grant EnviroSense DND permission first.", Toast.LENGTH_LONG).show();
            } else {
                prefs.edit().putBoolean("dnd_enabled", isChecked).apply();
            }
        });

        NotificationManager nm = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (prefs.getBoolean("dnd_enabled", false) && !nm.isNotificationPolicyAccessGranted()) {
            prefs.edit().putBoolean("dnd_enabled", false).apply();
            switchDnd.setChecked(false);
        }
        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("reminders_enabled", isChecked).apply());
        switchWeekly.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("weekly_report_enabled", isChecked).apply());

        rowNoise.setOnClickListener(v -> {
            NoiseThresholdBottomSheet sheet = new NoiseThresholdBottomSheet(newLimit -> {

                tvNoiseLimitValue.setText(newLimit + " dB");
            });
            sheet.show(getParentFragmentManager(), "NoiseThresholdSheet");
        });
        rowLight.setOnClickListener(v -> {
            LightMinimumBottomSheet sheet = new LightMinimumBottomSheet(newLimit -> {
                tvLightMinValue.setText(newLimit + " lux");
            });
            sheet.show(getParentFragmentManager(), "LightMinimumSheet");
        });

        rowDelete.setOnClickListener(v -> {
            DeleteConfirmDialog dialog = new DeleteConfirmDialog(() -> {
            });
            dialog.show(getParentFragmentManager(), "DeleteDialog");
        });

        rowExport.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Preparing CSV export...", Toast.LENGTH_SHORT).show();

            com.example.envirosense.ui.settings.CsvExporter.exportDataToCsv(requireContext(), new com.example.envirosense.ui.settings.CsvExporter.ExportCallback() {
                @Override
                public void onSuccess(String filePath) {
                    Toast.makeText(getContext(), "Export complete\nSaved to Downloads folder.", Toast.LENGTH_LONG).show();
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                }
            });
        });

        return view;
    }
}