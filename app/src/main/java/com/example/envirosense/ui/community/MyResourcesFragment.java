package com.example.envirosense.ui.community;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.AppDatabase;
import com.example.envirosense.data.MyResourceDao;
import com.example.envirosense.data.models.MyResource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Displays personal resources stored locally on the device.
 * Files picked by the user are copied into the app's internal storage
 * (files/my_resources/) and their metadata is persisted in the Room database.
 */
public class MyResourcesFragment extends Fragment {

    private RecyclerView rvResources;
    private LinearLayout layoutEmpty;
    private LinearLayout layoutSaveProgress;
    private TextView tvSaveStatus;
    private FloatingActionButton fabAdd;

    private MyResourcesAdapter adapter;
    private List<MyResource> resourceList;
    private MyResourceDao resourceDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // File picker launcher
    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK
                        && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        saveFileLocally(fileUri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_resources, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvResources = view.findViewById(R.id.rv_resources);
        layoutEmpty = view.findViewById(R.id.layout_empty_resources);
        layoutSaveProgress = view.findViewById(R.id.layout_save_progress);
        tvSaveStatus = view.findViewById(R.id.tv_save_status);
        fabAdd = view.findViewById(R.id.fab_add_my_resource);

        resourceDao = AppDatabase.getInstance(requireContext()).myResourceDao();

        resourceList = new ArrayList<>();
        adapter = new MyResourcesAdapter(resourceList);

        // Tap to open resource
        adapter.setOnResourceClickListener((resource, position) -> {
            openResource(resource);
        });

        // Long-press to delete
        adapter.setOnResourceLongClickListener((resource, position) -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Resource")
                    .setMessage("Remove \"" + resource.fileName + "\" from your resources?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteResource(resource))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        rvResources.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResources.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> openFilePicker());

        loadResources();
    }

    // ── Load from local Room DB ──────────────────────────────────────────

    private void loadResources() {
        executor.execute(() -> {
            List<MyResource> resources = resourceDao.getAllResources();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    resourceList.clear();
                    resourceList.addAll(resources);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
            }
        });
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

    // ── File picker ──────────────────────────────────────────────────────

    private void openFilePicker() {
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
        filePickerLauncher.launch(intent);
    }

    // ── Save file locally ────────────────────────────────────────────────

    /**
     * Copies the selected file into app internal storage (files/my_resources/)
     * and saves its metadata to the Room database.
     */
    private void saveFileLocally(Uri fileUri) {
        String fileName = getFileName(fileUri);
        long fileSize = getFileSize(fileUri);
        String fileType = getFileExtension(fileUri);

        showSaveProgress(true, "Saving " + fileName + "…");
        fabAdd.setEnabled(false);

        executor.execute(() -> {
            try {
                // Create the directory for local resources
                File resourceDir = new File(requireContext().getFilesDir(), "my_resources");
                if (!resourceDir.exists()) {
                    resourceDir.mkdirs();
                }

                // Generate a unique file name to avoid collisions
                String uniqueId = UUID.randomUUID().toString();
                String safeFileName = uniqueId + "_" + fileName;
                File destFile = new File(resourceDir, safeFileName);

                // Copy file content from the content URI to internal storage
                try (InputStream in = requireContext().getContentResolver().openInputStream(fileUri);
                     FileOutputStream out = new FileOutputStream(destFile)) {
                    if (in == null) {
                        throw new Exception("Cannot open input stream");
                    }
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();
                }

                // Save metadata to Room database
                MyResource resource = new MyResource(
                        uniqueId,
                        fileName,
                        fileType,
                        fileSize,
                        destFile.getAbsolutePath(),
                        System.currentTimeMillis()
                );
                resourceDao.insert(resource);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showSaveProgress(false, null);
                        fabAdd.setEnabled(true);
                        Toast.makeText(requireContext(), "Saved: " + fileName, Toast.LENGTH_SHORT).show();
                        loadResources();
                    });
                }

            } catch (Exception e) {
                android.util.Log.e("MyResourcesFragment", "Failed to save file locally", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showSaveProgress(false, null);
                        fabAdd.setEnabled(true);
                        Toast.makeText(requireContext(), "Failed to save file: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void showSaveProgress(boolean show, String message) {
        layoutSaveProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        if (message != null && tvSaveStatus != null) {
            tvSaveStatus.setText(message);
        }
    }

    // ── Open locally stored file ─────────────────────────────────────────

    private void openResource(MyResource resource) {
        if (resource.localPath == null || resource.localPath.isEmpty()) {
            Toast.makeText(requireContext(), "File path not available", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(resource.localPath);
        if (!file.exists()) {
            Toast.makeText(requireContext(), "File no longer exists on device", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    file
            );

            String mimeType = getMimeTypeFromExtension(resource.fileType);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No app found to open this file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeTypeFromExtension(String ext) {
        if (ext == null) return "*/*";
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
        return mime != null ? mime : "*/*";
    }

    // ── Delete resource ──────────────────────────────────────────────────

    private void deleteResource(MyResource resource) {
        executor.execute(() -> {
            // Delete the file from internal storage
            if (resource.localPath != null) {
                File file = new File(resource.localPath);
                if (file.exists()) {
                    file.delete();
                }
            }

            // Remove metadata from database
            resourceDao.deleteById(resource.id);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Resource removed", Toast.LENGTH_SHORT).show();
                    loadResources();
                });
            }
        });
    }

    // ── File info helpers ────────────────────────────────────────────────

    private String getFileName(Uri uri) {
        String result = "Unknown file";
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = requireContext().getContentResolver()
                    .query(uri, null, null, null, null)) {
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
            try (Cursor cursor = requireContext().getContentResolver()
                    .query(uri, null, null, null, null)) {
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
        String mimeType = requireContext().getContentResolver().getType(uri);
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
}
