package com.example.envirosense.ui.community;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.envirosense.R;
import com.google.android.material.button.MaterialButton;

public class GroupSessionActivity extends AppCompatActivity {

    private TextView tvScore, tvNoise, tvLight, tvTime;
    private MaterialButton btnJoin;
    private android.widget.ProgressBar progressFocus;
    private View cardConditions;
    private boolean isTrackingActive = false;

    private int currentAvgScore;
    private String groupName;
    private long lastNoiseVibrationTime = 0;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startTracking();
                } else {
                    Toast.makeText(this, "Microphone permission required for Focus Score", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_session);

        ImageView btnBack = findViewById(R.id.btn_back_session);
        btnBack.setOnClickListener(v -> finish());

        TextView tvEmoji = findViewById(R.id.tv_greeting);
        TextView tvName = findViewById(R.id.tv_name);
        tvScore = findViewById(R.id.tv_focus_score);
        TextView tvMembers = findViewById(R.id.tv_session_members_count);
        btnJoin = findViewById(R.id.btn_join_session);
        progressFocus = findViewById(R.id.progress_focus);
        cardConditions = findViewById(R.id.card_conditions);

        tvNoise = findViewById(R.id.tv_optimal_noise);
        tvLight = findViewById(R.id.tv_optimal_light);
        tvTime = findViewById(R.id.tv_score_label);

        groupName = getIntent().getStringExtra("GROUP_NAME");
        String groupEmoji = getIntent().getStringExtra("GROUP_EMOJI");
        currentAvgScore = getIntent().getIntExtra("AVG_SCORE", 72);
        int activeMembers = getIntent().getIntExtra("ACTIVE_MEMBERS", 3);

        if (groupName != null)
            tvName.setText(groupName);
        if (groupEmoji != null)
            tvEmoji.setText(groupEmoji);
        tvScore.setText(String.valueOf(currentAvgScore));
        if (progressFocus != null)
            progressFocus.setProgress(currentAvgScore);
        tvMembers.setText(String.valueOf(activeMembers));

        int noiseLimit = getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE).getInt("noise_limit", 70);
        int lightMin = getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE).getInt("light_min", 300);
        tvNoise.setText("🎤 < " + noiseLimit + " dB");
        tvLight.setText("☀ > " + lightMin + " lux");

        btnJoin.setOnClickListener(v -> {
            if (!isTrackingActive) {
                checkPermissionsAndStart();
            } else {
                stopTracking();
            }
        });

        View.OnClickListener adjustConditionsListener = v -> {
            if (isTrackingActive)
                return;
            com.example.envirosense.ui.settings.NoiseThresholdBottomSheet noiseSheet = new com.example.envirosense.ui.settings.NoiseThresholdBottomSheet(
                    newLimit -> {
                        getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE).edit()
                                .putInt("noise_limit", newLimit).apply();
                        tvNoise.setText("🎤 < " + newLimit + " dB");
                    });
            noiseSheet.show(getSupportFragmentManager(), "NoiseThresholdSheetFromGroup");
        };

        View.OnClickListener lightListener = v -> {
            if (isTrackingActive)
                return;
            com.example.envirosense.ui.settings.LightMinimumBottomSheet lightSheet = new com.example.envirosense.ui.settings.LightMinimumBottomSheet(
                    newLightLimit -> {
                        getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE).edit()
                                .putInt("light_min", newLightLimit).apply();
                        tvLight.setText("☀ > " + newLightLimit + " lux");
                    });
            lightSheet.show(getSupportFragmentManager(), "LightMinimumSheetFromGroup");
        };

        if (cardConditions != null)
            cardConditions.setOnClickListener(null);
        if (tvNoise != null)
            tvNoise.setOnClickListener(adjustConditionsListener);
        if (tvLight != null)
            tvLight.setOnClickListener(lightListener);
    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startTracking();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void triggerNoiseAlert() {
        long currentTime = System.currentTimeMillis();
        // Cooldown for vibrations
        if (currentTime - lastNoiseVibrationTime > 5000) {
            lastNoiseVibrationTime = currentTime;

            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(500,
                            android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }

            android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    android.app.NotificationChannel channel = new android.app.NotificationChannel("envirosense_alerts",
                            "Environment Alerts", android.app.NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }

                androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(
                        this, "envirosense_alerts")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Noise Spike Detected!")
                        .setContentText("A loud noise was detected in your group session.")
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

                notificationManager.notify(1, builder.build());
            }
        }
    }

    private void startTracking() {
        isTrackingActive = true;
        btnJoin.setText("End Session");
        btnJoin.setBackgroundColor(getResources().getColor(R.color.error_red, getTheme()));

        int noiseLimit = getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE).getInt("noise_limit", 70);
        int lightMin = getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE).getInt("light_min", 300);

        tvNoise.setText("🎤 < " + noiseLimit + " dB");
        tvLight.setText("☀ > " + lightMin + " lux");

        SharedFocusTracker tracker = SharedFocusTracker.getInstance();
        tracker.setCallback((score, noiseDb, lux, elapsedTimeMillis) -> {
            long totalSeconds = elapsedTimeMillis / 1000;
            long mins = totalSeconds / 60;
            long secs = totalSeconds % 60;

            int activeM = getIntent().getIntExtra("ACTIVE_MEMBERS", 3);
            int displayedScore = score;
            if (activeM > 0) {
                int personalImpact = (score - 50) / 10;
                displayedScore = currentAvgScore + personalImpact;
                displayedScore = Math.min(100, Math.max(0, displayedScore));
            }

            tvScore.setText(String.valueOf(displayedScore));
            if (progressFocus != null)
                progressFocus.setProgress(displayedScore);

            if (noiseDb > noiseLimit) {
                triggerNoiseAlert();
            }

            tvTime.setText(String.format("Session Time: %02d:%02d", mins, secs));
            tvNoise.setText(String.format("🎤 %.0f dB (Aim: < %d)", noiseDb, noiseLimit));
            tvLight.setText(String.format("☀ %.0f lux (Aim: > %d)", lux, lightMin));
        });

        tracker.startTracking(this, noiseLimit, lightMin);
    }

    private void stopTracking() {
        isTrackingActive = false;
        SharedFocusTracker.getInstance().stopTracking();
        Toast.makeText(this, "Session Ended. Group score contributed.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTrackingActive) {
            SharedFocusTracker.getInstance().stopTracking();
        }
    }
}
