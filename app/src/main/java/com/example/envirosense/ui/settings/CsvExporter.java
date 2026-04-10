package com.example.envirosense.ui.settings;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.example.envirosense.data.AppDatabase;
import com.example.envirosense.data.FocusSession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CsvExporter {

    public interface ExportCallback {
        void onSuccess(String filePath);
        void onError(String error);
    }

    public static void exportDataToCsv(Context context, ExportCallback callback) {
        new Thread(() -> {
            try {
                List<FocusSession> sessions = AppDatabase.getInstance(context).focusSessionDao().getAllSessions();

                if (sessions.isEmpty()) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError("No active tracking data found to export."));
                    return;
                }

                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File exportFile = new File(downloadsDir, "envirosense_data.csv");

                FileWriter writer = new FileWriter(exportFile);

                writer.append("Date,Time,Location,Final Score,Duration (mins),Avg Noise (dB),Max Noise Spikes,Avg Light (lux),Peak Score\n");

                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

                for (FocusSession s : sessions) {
                    Date timestamp = new Date(s.timestamp);
                    long durationMins = (s.durationMs / 1000) / 60;

                    String cleanLocation = s.location != null ? s.location.replace(",", " ") : "Unknown";

                    writer.append(sdfDate.format(timestamp)).append(",")
                            .append(sdfTime.format(timestamp)).append(",")
                            .append(cleanLocation).append(",")
                            .append(String.valueOf(s.finalScore)).append(",")
                            .append(String.valueOf(durationMins)).append(",")
                            .append(String.format(Locale.getDefault(), "%.1f", s.avgNoise)).append(",")
                            .append(String.valueOf(s.noiseSpikes)).append(",")
                            .append(String.format(Locale.getDefault(), "%.1f", s.avgLight)).append(",")
                            .append(String.valueOf(s.peakScore)).append("\n");
                }

                writer.flush();
                writer.close();

                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(exportFile.getAbsolutePath()));

            } catch (IOException e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Export failed: " + e.getMessage()));
            }
        }).start();
    }
}