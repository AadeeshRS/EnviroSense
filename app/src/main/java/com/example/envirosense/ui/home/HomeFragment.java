package com.example.envirosense.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.envirosense.data.AppDatabase;
import com.example.envirosense.data.FocusSession;
import com.example.envirosense.R;
import com.example.envirosense.ui.achievements.AchievementManager;
import com.google.android.material.button.MaterialButton;
import java.io.IOException;
import java.util.List;

import android.widget.ProgressBar;
import android.app.NotificationManager;
import android.content.SharedPreferences;

public class HomeFragment extends Fragment implements SensorEventListener {

    public enum HomeState {
        DEFAULT, FIRST_LAUNCH, ACTIVE_SESSION, NOISE_ALERT, SESSION_ENDED, POOR_ENVIRONMENT
    }

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private MediaRecorder mediaRecorder;

    private long sessionStartTime = 0;
    private double cumulativeScore = 0;
    private int sampleCount = 0;
    private boolean wasDndEnabledByUs = false;

    private ViewGroup rootLayout;
    private View cardOptimalConditions, cardFirstLaunch, cardCurrentEnv, bannerNoiseAlert, bannerPoorEnv;
    private MaterialButton btnStart;
    private TextView tvActiveNoise, tvActiveLight, tvActiveTime;

    // Tracking state
    private boolean isTracking = false;
    private final Handler handler = new Handler();
    private float currentLux = 0f;
    private TextView tvFocusScore;
    private ProgressBar progressFocus;

    private View focusRingContainer, viewActiveRing, viewNoData;
    private TextView tvScoreLabel;
    private TextView tvGreeting, tvName;
    private TextView tvNoiseAlertDetails;
    private TextView tvOptimalNoise, tvOptimalLight, tvOptimalTime;

    private double currentLiveScoreEma = -1;
    private long lastNoiseVibrationTime = 0;

    private int currentPeakScore = 0;
    private int currentNoiseSpikes = 0;
    private double cumulativeNoise = 0;
    private float cumulativeLight = 0;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startTrackingSensors();
                } else {
                    Toast.makeText(getContext(), "Microphone permission required for Focus Score", Toast.LENGTH_SHORT)
                            .show();
                    updateUi(HomeState.DEFAULT); // rollback
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        rootLayout = (ViewGroup) view;

        // Base UI
        cardOptimalConditions = view.findViewById(R.id.card_conditions);
        cardFirstLaunch = view.findViewById(R.id.card_first_launch);
        cardCurrentEnv = view.findViewById(R.id.card_current_env);
        bannerNoiseAlert = view.findViewById(R.id.banner_noise_alert);
        bannerPoorEnv = view.findViewById(R.id.banner_poor_env);
        btnStart = view.findViewById(R.id.btn_start);

        tvActiveNoise = view.findViewById(R.id.tv_active_noise);
        tvActiveLight = view.findViewById(R.id.tv_active_light);
        tvActiveTime = view.findViewById(R.id.tv_active_time);
        tvFocusScore = view.findViewById(R.id.tv_focus_score);
        progressFocus = view.findViewById(R.id.progress_focus);
        focusRingContainer = view.findViewById(R.id.focus_ring_container);
        viewActiveRing = view.findViewById(R.id.view_active_ring);
        viewNoData = view.findViewById(R.id.view_no_data);
        tvScoreLabel = view.findViewById(R.id.tv_score_label);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvName = view.findViewById(R.id.tv_name);
        tvNoiseAlertDetails = view.findViewById(R.id.tv_noise_alert_details);
        tvOptimalNoise = view.findViewById(R.id.tv_optimal_noise);
        tvOptimalLight = view.findViewById(R.id.tv_optimal_light);
        tvOptimalTime = view.findViewById(R.id.tv_optimal_time);

        focusRingContainer.setVisibility(View.GONE);
        tvScoreLabel.setVisibility(View.GONE);
        cardOptimalConditions.setVisibility(View.GONE);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        btnStart.setOnClickListener(v -> {
            if (cardCurrentEnv.getVisibility() == View.VISIBLE) {

                updateUi(HomeState.SESSION_ENDED);
            } else {
                checkPermissionsAndStart();
            }
        });

        view.findViewById(R.id.btn_dismiss_alert).setOnClickListener(v -> bannerNoiseAlert.setVisibility(View.GONE));

        new Thread(() -> {
            FocusSession lastSession = AppDatabase.getInstance(requireContext())
                    .focusSessionDao()
                    .getLastSession();

            requireActivity().runOnUiThread(() -> {
                if (lastSession == null) {
                    updateUi(HomeState.FIRST_LAUNCH);
                } else {
                    tvFocusScore.setText(String.valueOf(lastSession.finalScore));
                    progressFocus.setProgress(lastSession.finalScore);
                    updateUi(HomeState.DEFAULT);
                }
            });
        }).start();

        return view;
    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startTrackingSensors();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startTrackingSensors() {
        updateUi(HomeState.ACTIVE_SESSION);
        isTracking = true;
        sessionStartTime = System.currentTimeMillis();
        cumulativeScore = 0;
        sampleCount = 0;
        currentPeakScore = 0;
        currentNoiseSpikes = 0;
        cumulativeNoise = 0;
        cumulativeLight = 0;

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        startAudioRecording();

        handler.post(updateSensorsRunnable);

        SharedPreferences prefs = requireActivity().getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean("dnd_enabled", false)) {
            NotificationManager nm = (NotificationManager) requireContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm.isNotificationPolicyAccessGranted()) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                wasDndEnabledByUs = true;
            }
        }
    }

    private void stopTrackingSensors() {
        isTracking = false;
        handler.removeCallbacks(updateSensorsRunnable);
        sensorManager.unregisterListener(this);
        stopAudioRecording();
        if (wasDndEnabledByUs) {
            NotificationManager nm = (NotificationManager) requireContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && nm.isNotificationPolicyAccessGranted()) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
            wasDndEnabledByUs = false;
        }
    }

    private void startAudioRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(requireContext().getCacheDir().getAbsolutePath() + "/dummy.3gp");
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopAudioRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaRecorder = null;
        }
    }

    private final Runnable updateSensorsRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTracking && mediaRecorder != null) {

                String currentTime = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                        .format(new java.util.Date());
                if (tvActiveTime != null) {
                    tvActiveTime.setText("✓ " + currentTime);
                }

                int amplitude = mediaRecorder.getMaxAmplitude();
                double db = 0;
                if (amplitude > 0) {
                    db = 20 * Math.log10(amplitude);
                }

                int displayDb = (int) Math.max(30, db);

                if (tvActiveNoise != null)
                    tvActiveNoise.setText("✓ " + displayDb + " dB");

                double noiseScore = 100;
                if (displayDb > 60) {
                    noiseScore = Math.max(0, 100 - ((displayDb - 60) * 2.5));
                }

                double lightScore = 100;
                if (currentLux < 200) {
                    lightScore = Math.max(0, (currentLux / 200f) * 100);
                } else if (currentLux > 2000) {
                    lightScore = Math.max(0, 100 - ((currentLux - 2000) * 0.05));
                }

                double instantaneousScore = (noiseScore + lightScore) / 2.0;

                cumulativeScore += instantaneousScore;
                sampleCount++;

                currentPeakScore = Math.max(currentPeakScore, (int) instantaneousScore);
                cumulativeNoise += displayDb;
                cumulativeLight += currentLux;
                if (displayDb > 60) {
                    currentNoiseSpikes++;
                }

                if (currentLiveScoreEma < 0) {
                    currentLiveScoreEma = instantaneousScore;
                } else {
                    currentLiveScoreEma = (currentLiveScoreEma * 0.9) + (instantaneousScore * 0.1);
                }

                int displayScore = (int) Math.round(currentLiveScoreEma);

                if (tvFocusScore != null)
                    tvFocusScore.setText(String.valueOf(displayScore));
                if (progressFocus != null)
                    progressFocus.setProgress(displayScore);

                int noiseLimit = 70;
                if (displayDb > noiseLimit + 10) {
                    if (bannerNoiseAlert.getVisibility() == View.GONE) {
                        bannerNoiseAlert.setVisibility(View.VISIBLE);
                        if (System.currentTimeMillis() - lastNoiseVibrationTime > 10000) {
                            android.os.Vibrator vibrator = (android.os.Vibrator) requireContext()
                                    .getSystemService(Context.VIBRATOR_SERVICE);
                            if (vibrator != null && vibrator.hasVibrator()) {

                                vibrator.vibrate(android.os.VibrationEffect.createOneShot(300,
                                        android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                            }
                            lastNoiseVibrationTime = System.currentTimeMillis();
                        }
                    }
                    if (tvNoiseAlertDetails != null) {
                        tvNoiseAlertDetails.setText("Current: " + displayDb + " dB\nLimit: " + noiseLimit + " dB");
                    }
                } else {
                    if (bannerNoiseAlert.getVisibility() == View.VISIBLE) {
                        bannerNoiseAlert.setVisibility(View.GONE);
                    }
                }

                handler.postDelayed(this, 500);
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            currentLux = event.values[0];
            if (tvActiveLight != null)
                tvActiveLight.setText("✓ " + (int) currentLux + " lux");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTrackingSensors();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            new Thread(() -> {
                List<FocusSession> sessions = AppDatabase.getInstance(requireContext())
                        .focusSessionDao().getAllSessions();

                requireActivity().runOnUiThread(() -> {

                    if (sessions.isEmpty() && !isTracking) {

                        updateUi(HomeState.FIRST_LAUNCH);

                        cumulativeScore = 0;
                        sampleCount = 0;
                        currentPeakScore = 0;
                        currentNoiseSpikes = 0;
                        cumulativeNoise = 0;
                        cumulativeLight = 0;
                    }
                });
            }).start();
        }
    }

    public void updateUi(HomeState state) {
        TransitionManager.beginDelayedTransition(rootLayout);

        if (state == HomeState.DEFAULT) {
            tvGreeting.setText("Good morning");
            tvName.setText("Arjun");
            focusRingContainer.setVisibility(View.VISIBLE);
            viewActiveRing.setVisibility(View.VISIBLE);
            viewNoData.setVisibility(View.GONE);
            tvScoreLabel.setVisibility(View.VISIBLE);
            tvScoreLabel.setVisibility(View.VISIBLE);
            cardOptimalConditions.setVisibility(View.VISIBLE);
            cardFirstLaunch.setVisibility(View.GONE);
            cardCurrentEnv.setVisibility(View.GONE);
            bannerNoiseAlert.setVisibility(View.GONE);
            bannerPoorEnv.setVisibility(View.GONE);
            btnStart.setText("Start Session");
            stopTrackingSensors();
        } else if (state == HomeState.FIRST_LAUNCH) {
            tvGreeting.setText("Welcome to");
            tvName.setText("EnviroSense");
            tvFocusScore.setText("--");
            progressFocus.setProgress(0);
            focusRingContainer.setVisibility(View.VISIBLE);
            viewActiveRing.setVisibility(View.GONE);
            viewNoData.setVisibility(View.VISIBLE);
            tvScoreLabel.setVisibility(View.GONE);
            tvScoreLabel.setVisibility(View.GONE);
            cardOptimalConditions.setVisibility(View.GONE);
            cardFirstLaunch.setVisibility(View.VISIBLE);
            cardCurrentEnv.setVisibility(View.GONE);
            bannerNoiseAlert.setVisibility(View.GONE);
            bannerPoorEnv.setVisibility(View.GONE);
            btnStart.setText("Start My First Session");
            stopTrackingSensors();
        } else if (state == HomeState.ACTIVE_SESSION) {
            currentLiveScoreEma = -1;
            tvGreeting.setText("Tracking");
            tvName.setText("Environment");
            focusRingContainer.setVisibility(View.VISIBLE);
            viewActiveRing.setVisibility(View.VISIBLE);
            viewNoData.setVisibility(View.GONE);
            tvScoreLabel.setVisibility(View.VISIBLE);
            tvScoreLabel.setVisibility(View.VISIBLE);
            cardOptimalConditions.setVisibility(View.GONE);
            cardFirstLaunch.setVisibility(View.GONE);
            cardCurrentEnv.setVisibility(View.VISIBLE);
            bannerPoorEnv.setVisibility(View.GONE);
            btnStart.setText("End Session");
        } else if (state == HomeState.SESSION_ENDED) {
            tvGreeting.setText("Good morning");
            tvName.setText("Arjun");
            stopTrackingSensors();
            cardOptimalConditions.setVisibility(View.VISIBLE);
            cardCurrentEnv.setVisibility(View.GONE);
            bannerNoiseAlert.setVisibility(View.GONE);
            btnStart.setText("Start Session");

            int finalScore = sampleCount > 0 ? (int) Math.round(cumulativeScore / sampleCount) : 0;

            tvFocusScore.setText(String.valueOf(finalScore));
            progressFocus.setProgress(finalScore);

            double avgNoise = sampleCount > 0 ? (cumulativeNoise / sampleCount) : 0;
            float avgLight = sampleCount > 0 ? (float) (cumulativeLight / sampleCount) : 0;

            long elapsedMillis = System.currentTimeMillis() - sessionStartTime;
            FocusSession newSession = new FocusSession(
                    System.currentTimeMillis(),
                    finalScore,
                    elapsedMillis,
                    "Local Area",
                    avgNoise,
                    avgLight,
                    currentPeakScore,
                    currentNoiseSpikes);

            long minutes = (elapsedMillis / 1000) / 60;
            long seconds = (elapsedMillis / 1000) % 60;
            String realDuration = String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds);

            SessionCompleteBottomSheet bottomSheet = new SessionCompleteBottomSheet(
                    finalScore,
                    realDuration,
                    "Home",
                    new SessionCompleteBottomSheet.OnAnalyticsButtonClickListener() {
                        @Override
                        public void onAnalyticsClicked(String finalLocation) {
                            saveSession(finalScore, elapsedMillis, finalLocation, () -> {
                                if (requireActivity() instanceof com.example.envirosense.MainActivity) {
                                    ((com.example.envirosense.MainActivity) requireActivity()).navigateToAnalytics();
                                }
                            });
                        }

                        @Override
                        public void onDoneClicked(String finalLocation) {
                            saveSession(finalScore, elapsedMillis, finalLocation, null);
                        }
                    });
            bottomSheet.show(getParentFragmentManager(), "SessionCompleteBottomSheet");
        }
    }

    private void saveSession(int score, long durationMs, String location, Runnable onSaved) {
        double avgNoise = sampleCount > 0 ? (cumulativeNoise / sampleCount) : 0;
        float avgLight = sampleCount > 0 ? (float) (cumulativeLight / sampleCount) : 0;

        FocusSession newSession = new FocusSession(System.currentTimeMillis(), score, durationMs, location, avgNoise,
                avgLight, currentPeakScore, currentNoiseSpikes);
        new Thread(() -> {
            AppDatabase.getInstance(requireContext()).focusSessionDao().insert(newSession);

            AchievementManager.checkUnlocks(requireContext());

            // Wait for DB insertion to finish before triggering navigation
            if (onSaved != null) {
                requireActivity().runOnUiThread(onSaved);
            }
        }).start();
    }
}