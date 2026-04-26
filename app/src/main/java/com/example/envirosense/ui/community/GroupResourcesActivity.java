package com.example.envirosense.ui.community;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.models.GroupResource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Displays resources shared within a group and allows users to add new resources.
 * Files are uploaded to Firebase Storage (group_resources/{groupName}/...)
 * and metadata is stored in Firestore (groups/{groupName}/resources/{docId}).
 */
public class GroupResourcesActivity extends AppCompatActivity {

    private String groupName;
    private RecyclerView rvResources;
    private LinearLayout layoutEmpty;
    private LinearLayout layoutUploadProgress;
    private ProgressBar progressUpload;
    private TextView tvUploadStatus;
    private FloatingActionButton fabAdd;
    private GroupResourcesAdapter adapter;
    private List<GroupResource> resourceList;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ListenerRegistration resourceListener;
    private String currentUserName = "User";

    // File picker launcher for device storage
    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        uploadToFirebaseStorage(fileUri, "device");
                    }
                }
            });

    // File picker launcher for "My Resources" (same file picker, tagged differently)
    private final ActivityResultLauncher<Intent> myResourcesPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        uploadToFirebaseStorage(fileUri, "my_resources");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_resources);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar_resources);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setTitle("Group Resources");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        groupName = getIntent().getStringExtra("GROUP_NAME");
        if (groupName == null) groupName = "Unknown Group";

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Fetch user name
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                if (doc.exists() && doc.getString("name") != null) {
                    currentUserName = doc.getString("name");
                }
            });
        }

        rvResources = findViewById(R.id.rv_group_resources);
        layoutEmpty = findViewById(R.id.layout_empty_resources);
        layoutUploadProgress = findViewById(R.id.layout_upload_progress);
        progressUpload = findViewById(R.id.progress_upload);
        tvUploadStatus = findViewById(R.id.tv_upload_status);
        fabAdd = findViewById(R.id.fab_add_resource);

        resourceList = new ArrayList<>();
        adapter = new GroupResourcesAdapter(resourceList);

        // Tap to download/open a resource
        adapter.setOnResourceClickListener((resource, position) -> {
            if (resource.downloadUrl != null && !resource.downloadUrl.isEmpty()) {
                openOrDownloadResource(resource);
            } else {
                Toast.makeText(this, "Resource URL not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Long-press to delete (own uploads only)
        adapter.setOnResourceLongClickListener((resource, position) -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getUid().equals(resource.uploadedByUid)) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Resource")
                        .setMessage("Remove \"" + resource.fileName + "\" from group resources?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteResource(resource))
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "You can only delete resources you uploaded", Toast.LENGTH_SHORT).show();
            }
        });

        rvResources.setLayoutManager(new LinearLayoutManager(this));
        rvResources.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddResourceBottomSheet());

        loadResources();
    }

    // ── Loading from Firestore ────────────────────────────────────────────

    private void loadResources() {
        resourceListener = db.collection("groups").document(groupName)
                .collection("resources")
                .orderBy("uploadedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Fallback: load without ordering (if index doesn't exist yet)
                        resourceListener = db.collection("groups").document(groupName)
                                .collection("resources")
                                .addSnapshotListener((val, err) -> {
                                    if (err != null || val == null) return;
                                    updateResourceList(val);
                                });
                        return;
                    }
                    if (value == null) return;
                    updateResourceList(value);
                });
    }

    private void updateResourceList(com.google.firebase.firestore.QuerySnapshot snapshot) {
        resourceList.clear();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            GroupResource resource = doc.toObject(GroupResource.class);
            if (resource != null) {
                resource.documentId = doc.getId();
                resourceList.add(resource);
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (resourceList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvResources.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvResources.setVisibility(View.VISIBLE);
        }
    }

    // ── Upload to Firebase Storage ────────────────────────────────────────

    private void showAddResourceBottomSheet() {
        AddResourceBottomSheet bottomSheet = new AddResourceBottomSheet();
        bottomSheet.setOnSourceSelectedListener(new AddResourceBottomSheet.OnSourceSelectedListener() {
            @Override
            public void onMyResourcesSelected() {
                openFilePicker(myResourcesPickerLauncher);
            }

            @Override
            public void onDeviceStorageSelected() {
                openFilePicker(filePickerLauncher);
            }
        });
        bottomSheet.show(getSupportFragmentManager(), "AddResource");
    }

    private void openFilePicker(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/pdf",
                "image/*",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/plain",
                "application/zip"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        launcher.launch(intent);
    }

    /**
     * Uploads the selected file to Firebase Storage, then saves the download URL
     * and metadata to Firestore so all group members can access it.
     */
    private void uploadToFirebaseStorage(Uri fileUri, String sourceType) {
        String fileName = getFileName(fileUri);
        long fileSize = getFileSize(fileUri);
        String fileType = getFileExtension(fileUri);

        // Show progress
        showUploadProgress(true);
        progressUpload.setProgress(0);
        tvUploadStatus.setText("Uploading… 0%");
        fabAdd.setEnabled(false);

        // Create a unique path in Storage: group_resources/{groupName}/{uuid}_{fileName}
        String storagePath = "group_resources/" + groupName + "/" + UUID.randomUUID() + "_" + fileName;
        StorageReference storageRef = storage.getReference().child(storagePath);

        UploadTask uploadTask = storageRef.putFile(fileUri);

        // Progress listener
        uploadTask.addOnProgressListener(snapshot -> {
            if (snapshot.getTotalByteCount() > 0) {
                int percent = (int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                progressUpload.setProgress(percent);
                tvUploadStatus.setText("Uploading… " + percent + "%");
            }
        });

        // On success: get the download URL, then save to Firestore
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            tvUploadStatus.setText("Finalizing…");

            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                saveResourceToFirestore(fileName, fileType, fileSize, sourceType,
                        downloadUri.toString(), storagePath);

                showUploadProgress(false);
                fabAdd.setEnabled(true);
                Toast.makeText(this, "Uploaded: " + fileName, Toast.LENGTH_SHORT).show();

            }).addOnFailureListener(e -> {
                showUploadProgress(false);
                fabAdd.setEnabled(true);
                Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
            });

        }).addOnFailureListener(e -> {
            showUploadProgress(false);
            fabAdd.setEnabled(true);
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Saves the resource metadata (including the Firebase Storage download URL)
     * to Firestore so it's visible to all group members.
     */
    private void saveResourceToFirestore(String fileName, String fileType, long fileSize,
                                          String sourceType, String downloadUrl, String storagePath) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "unknown";

        Map<String, Object> resourceData = new HashMap<>();
        resourceData.put("fileName", fileName);
        resourceData.put("fileType", fileType);
        resourceData.put("fileSizeBytes", fileSize);
        resourceData.put("uploadedBy", currentUserName);
        resourceData.put("uploadedByUid", uid);
        resourceData.put("sourceType", sourceType);
        resourceData.put("downloadUrl", downloadUrl);
        resourceData.put("storagePath", storagePath);
        resourceData.put("uploadedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("groups").document(groupName)
                .collection("resources")
                .add(resourceData);
    }

    private void showUploadProgress(boolean show) {
        layoutUploadProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    // ── Download / Open ───────────────────────────────────────────────────

    /**
     * Opens the resource in the user's browser or downloads it.
     * Since the downloadUrl is a public Firebase Storage URL, any app can open it.
     */
    private void openOrDownloadResource(GroupResource resource) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(resource.downloadUrl));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────

    /**
     * Deletes both the Firestore document and the file from Firebase Storage.
     */
    private void deleteResource(GroupResource resource) {
        // Delete from Firestore
        if (resource.documentId != null) {
            db.collection("groups").document(groupName)
                    .collection("resources").document(resource.documentId)
                    .delete()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Resource removed", Toast.LENGTH_SHORT).show());
        }

        // Delete from Firebase Storage (if storagePath is available)
        if (resource.storagePath != null && !resource.storagePath.isEmpty()) {
            storage.getReference().child(resource.storagePath).delete();
        }
    }

    // ── File info helpers ─────────────────────────────────────────────────

    private String getFileName(Uri uri) {
        String result = "Unknown file";
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if ("Unknown file".equals(result)) {
            String path = uri.getPath();
            if (path != null) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    result = path.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private long getFileSize(Uri uri) {
        long size = 0;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            }
        }
        return size;
    }

    private String getFileExtension(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType != null) {
            String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (ext != null) return ext;
            if (mimeType.startsWith("image/")) return "image";
            if (mimeType.contains("pdf")) return "pdf";
            if (mimeType.contains("word") || mimeType.contains("document")) return "doc";
            if (mimeType.contains("powerpoint") || mimeType.contains("presentation")) return "ppt";
            if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) return "xls";
        }
        String fileName = getFileName(uri);
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0 && dot < fileName.length() - 1) {
            return fileName.substring(dot + 1).toLowerCase();
        }
        return "other";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resourceListener != null) {
            resourceListener.remove();
        }
    }
}
