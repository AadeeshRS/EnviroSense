package com.example.envirosense.ui.community;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

public class SharedFocusTracker {

    private static SharedFocusTracker instance;

    private MediaRecorder mediaRecorder;
    private SensorManager sensorManager;
    private Sensor lightSensor;

    private boolean isTracking = false;
    private float currentLux = 0f;
    private double currentNoiseDb = 0.0;
    private int currentScore = 100;
    
    private int userNoiseLimit = 65;
    private int userLightMin = 200;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TrackerCallback callback;

    public interface TrackerCallback {
        void onUpdate(int score, double noiseDb, float lux, long elapsedTimeMillis);
    }

    private long startTime;
    private Runnable updateRunnable;

    private SensorEventListener lightListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                currentLux = event.values[0];
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private SharedFocusTracker() {
    }

    public static synchronized SharedFocusTracker getInstance() {
        if (instance == null) {
            instance = new SharedFocusTracker();
        }
        return instance;
    }

    public void setCallback(TrackerCallback callback) {
        this.callback = callback;
    }

    public boolean isTracking() {
        return isTracking;
    }

    public void startTracking(Context context, int noiseLimit, int lightMin) {
        if (isTracking) return;
        this.userNoiseLimit = noiseLimit;
        this.userLightMin = lightMin;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if (lightSensor != null) {
                sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(context.getCacheDir().getAbsolutePath() + "/temp_audio.3gp");
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException | IllegalStateException | SecurityException e) {
            mediaRecorder = null;
        }

        isTracking = true;
        startTime = System.currentTimeMillis();
        currentScore = 100;

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isTracking) return;

                if (mediaRecorder != null) {
                    int amplitude = mediaRecorder.getMaxAmplitude();
                    if (amplitude > 0) {
                        currentNoiseDb = 20 * Math.log10(amplitude);
                    }
                }

                if (currentNoiseDb > userNoiseLimit) {
                    currentScore = Math.max(0, currentScore - 2);
                } else if (currentLux > userLightMin) {
                    currentScore = Math.min(100, currentScore + 1);
                }

                if (callback != null) {
                    callback.onUpdate(currentScore, currentNoiseDb, currentLux, System.currentTimeMillis() - startTime);
                }

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateRunnable);
    }

    public void stopTracking() {
        if (!isTracking) return;
        isTracking = false;
        handler.removeCallbacks(updateRunnable);

        if (sensorManager != null && lightListener != null) {
            sensorManager.unregisterListener(lightListener);
        }

        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException ignored) {
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}
