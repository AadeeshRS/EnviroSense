package com.example.envirosense.data.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Represents a resource (file) shared within a study group.
 * Stored under Firestore: groups/{groupName}/resources/{docId}
 */
public class GroupResource {

    @Exclude
    public String documentId;

    public String fileName;
    public String fileType;       // e.g. "pdf", "image", "doc", "other"
    public long fileSizeBytes;
    public String uploadedBy;     // user display name
    public String uploadedByUid;
    public String sourceType;     // "my_resources" or "device"
    public String downloadUrl;    // Firebase Storage download URL
    public String storagePath;    // Firebase Storage path for deletion
    public String base64Content;  // For small files stored inline (optional)

    @ServerTimestamp
    public Timestamp uploadedAt;

    public GroupResource() {
    }

    public GroupResource(String fileName, String fileType, long fileSizeBytes,
                         String uploadedBy, String uploadedByUid, String sourceType) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSizeBytes = fileSizeBytes;
        this.uploadedBy = uploadedBy;
        this.uploadedByUid = uploadedByUid;
        this.sourceType = sourceType;
    }

    /**
     * Returns a human-readable file size string.
     */
    @Exclude
    public String getFormattedSize() {
        if (fileSizeBytes < 1024) return fileSizeBytes + " B";
        if (fileSizeBytes < 1024 * 1024) return String.format("%.1f KB", fileSizeBytes / 1024.0);
        return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
    }

    /**
     * Returns an emoji icon based on file type.
     */
    @Exclude
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
