package com.example.envirosense.ui.settings;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import com.example.envirosense.data.AppDatabase;
import com.example.envirosense.data.FocusSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.OutputStream;
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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user != null ? user.getUid() : "guest";
                List<FocusSession> sessions = AppDatabase.getInstance(context)
                        .focusSessionDao().getAllSessions(uid);

                if (sessions.isEmpty()) {
                    post(callback::onError, "No sessions found to export.");
                    return;
                }

            
                StringBuilder sb = new StringBuilder();
                sb.append("Date,Time,Location,Final Score,Duration (mins),"
                        + "Avg Noise (dB),Max Noise Spikes,Avg Light (lux),Peak Score\n");

                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

                for (FocusSession s : sessions) {
                    Date ts = new Date(s.timestamp);
                    long durationMins = (s.durationMs / 1000) / 60;
                    String loc = s.location != null ? s.location.replace(",", " ") : "Unknown";

                    sb.append(sdfDate.format(ts)).append(",")
                            .append(sdfTime.format(ts)).append(",")
                            .append(loc).append(",")
                            .append(s.finalScore).append(",")
                            .append(durationMins).append(",")
                            .append(String.format(Locale.getDefault(), "%.1f", s.avgNoise)).append(",")
                            .append(s.noiseSpikes).append(",")
                            .append(String.format(Locale.getDefault(), "%.1f", s.avgLight)).append(",")
                            .append(s.peakScore).append("\n");
                }

            
                String fileName = "envirosense_export_"
                        + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date())
                        + ".csv";

                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                values.put(MediaStore.Downloads.IS_PENDING, 1);

                ContentResolver resolver = context.getContentResolver();
                Uri collection;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                } else {
                    
                    java.io.File dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS);
                    if (dir != null && !dir.exists()) dir.mkdirs();
                    java.io.File file = new java.io.File(dir, fileName);
                    try (java.io.FileWriter fw = new java.io.FileWriter(file)) {
                        fw.write(sb.toString());
                    }
                    final String path = file.getAbsolutePath();
                    post(() -> callback.onSuccess(path));
                    return;
                }

                Uri itemUri = resolver.insert(collection, values);
                if (itemUri == null) {
                    post(callback::onError, "Export failed: could not create file in Downloads.");
                    return;
                }

                try (OutputStream out = resolver.openOutputStream(itemUri)) {
                    if (out == null) throw new IOException("Null output stream");
                    out.write(sb.toString().getBytes());
                }

            
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(itemUri, values, null, null);

                post(() -> callback.onSuccess(fileName));

            } catch (IOException e) {
                e.printStackTrace();
                post(callback::onError, "Export failed: " + e.getMessage());
            }
        }).start();
    }


    private static void post(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    private static void post(java.util.function.Consumer<String> fn, String arg) {
        new Handler(Looper.getMainLooper()).post(() -> fn.accept(arg));
    }
}