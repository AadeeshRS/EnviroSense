package com.example.envirosense.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a personal resource stored locally on the device.
 * Files are copied to the app's internal storage directory and
 * metadata is persisted in the Room database.
 */
@Entity(tableName = "my_resources")
public class MyResource {

    @PrimaryKey
    @NonNull
    public String id;            // UUID

    public String fileName;
    public String fileType;      // e.g. "pdf", "image", "doc", "other"
    public long fileSizeBytes;
    public String localPath;     // Absolute path to the copied file in internal storage
    public long addedAt;         // System.currentTimeMillis()

    public MyResource() {
        this.id = "";
    }

    public MyResource(@NonNull String id, String fileName, String fileType,
                       long fileSizeBytes, String localPath, long addedAt) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSizeBytes = fileSizeBytes;
        this.localPath = localPath;
        this.addedAt = addedAt;
    }

    /**
     * Returns a human-readable file size string.
     */
    public String getFormattedSize() {
        if (fileSizeBytes < 1024) return fileSizeBytes + " B";
        if (fileSizeBytes < 1024 * 1024) return String.format("%.1f KB", fileSizeBytes / 1024.0);
        return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
    }

    /**
     * Returns an emoji icon based on file type.
     */
    public String getTypeEmoji() {
        if (fileType == null) return "📄";
        switch (fileType.toLowerCase()) {
            case "pdf":  return "📕";
            case "image":
            case "jpg":
            case "jpeg":
            case "png":  return "🖼️";
            case "doc":
            case "docx": return "📝";
            case "ppt":
            case "pptx": return "📊";
            case "xls":
            case "xlsx": return "📈";
            case "txt":  return "📃";
            case "zip":
            case "rar":  return "📦";
            default:     return "📄";
        }
    }
}
