package com.example.envirosense.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.envirosense.MainActivity;
import com.example.envirosense.R;
import java.io.IOException;

public class TrackingService extends Service implements SensorEventListener {
    private static final String CHANNEL_ID = "EnviroSenseTrackingChannel";
    private static final int NOTIFICATION_ID = 101;

    public static final String ACTION_TRACKING_UPDATE = "com.example.envirosense.TRACKING_UPDATE";
    public static final String EXTRA_NOISE = "extra_noise";
    public static final String EXTRA_LIGHT = "extra_light";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_LIVE_SCORE = "extra_live_score";
    
    public static final String EXTRA_AVG_NOISE = "extra_avg_noise";
    public static final String EXTRA_AVG_LIGHT = "extra_avg_light";
    public static final String EXTRA_PEAK_SCORE = "extra_peak_score";
    public static final String EXTRA_NOISE_SPIKES = "extra_noise_spikes";

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private MediaRecorder mediaRecorder;

    private boolean isTracking = false;
    private long sessionStartTime = 0;
    private float currentLux = 0f;
    private double currentLiveScoreEma = -1;
    private double cumulativeNoise = 0;
    private float cumulativeLight = 0;
    private int peakScore = 0;
    private int noiseSpikes = 0;
    private int sampleCount = 0;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("STOP".equals(intent.getAction())) {
            stopTracking();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!isTracking) {
            startTracking();
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Focus Session Active")
                .setContentText("Monitoring noise and light levels...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "EnviroSense Session Tracking",
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null)
            manager.createNotificationChannel(serviceChannel);
    }

    private void startTracking() {
        isTracking = true;
        sessionStartTime = System.currentTimeMillis();
        currentLiveScoreEma = -1;

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(getExternalCacheDir().getAbsolutePath() + "/dummy.3gp");
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException | RuntimeException e) {
            Log.e("TrackingService", "MediaRecorder failed: " + e.getMessage());
            mediaRecorder = null;
        }

        handler.post(trackingRunnable);
    }

    private void stopTracking() {
        isTracking = false;
        handler.removeCallbacks(trackingRunnable);
        if (sensorManager != null && lightSensor != null) {
            sensorManager.unregisterListener(this);
        }
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (Exception e) {
            }
            mediaRecorder = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTracking();
    }

    private final Runnable trackingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isTracking)
                return;

            long elapsed = System.currentTimeMillis() - sessionStartTime;
            double noiseDb = 0;

            if (mediaRecorder != null) {
                try {
                    int amplitude = mediaRecorder.getMaxAmplitude();
                    if (amplitude > 0) {
                        noiseDb = 20 * Math.log10(amplitude);
                    }
                } catch (Exception e) {
                }
            }

        
            double currentScore = calculateLiveScore(noiseDb, currentLux);
            if (currentLiveScoreEma == -1) {
                currentLiveScoreEma = currentScore;
            } else {
                currentLiveScoreEma = (currentScore * 0.1) + (currentLiveScoreEma * 0.9);
            }

            Intent intent = new Intent(ACTION_TRACKING_UPDATE);
            intent.putExtra(EXTRA_NOISE, noiseDb);
            intent.putExtra(EXTRA_LIGHT, currentLux);
            intent.putExtra(EXTRA_TIME, elapsed);
            intent.putExtra(EXTRA_LIVE_SCORE, currentLiveScoreEma);

            intent.setPackage(getPackageName());
            sendBroadcast(intent);

            handler.postDelayed(this, 1000);
        }
    };

    private double calculateLiveScore(double noise, float lux) {
        double score = 100;
        if (noise > 60)
            score -= (noise - 60) * 1.5;
        if (lux < 100)
            score -= (100 - lux) * 0.2;
        if (lux > 1000)
            score -= (lux - 1000) * 0.05;
        return Math.max(0, Math.min(100, score));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            currentLux = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}