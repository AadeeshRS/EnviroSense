package com.example.envirosense.ui.community;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.envirosense.R;
import com.example.envirosense.data.models.StudyGroup;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CreateCommunityBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText etGroupName, etGroupSubjects, etGroupDesc;
    private FrameLayout btnSelectIcon;
    private ImageView ivSelectedIcon;
    private TextView tvPlaceholderEmoji;
    private MaterialButton btnSubmit;

    private Bitmap selectedBitmap = null;

    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
                if (result != null) {
                    selectedBitmap = result;
                    updateIconView();
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
                        updateIconView();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_create_community, container, false);

        etGroupName = view.findViewById(R.id.et_group_name);
        etGroupSubjects = view.findViewById(R.id.et_group_subjects);
        etGroupDesc = view.findViewById(R.id.et_group_desc);
        btnSelectIcon = view.findViewById(R.id.btn_select_icon);
        ivSelectedIcon = view.findViewById(R.id.iv_selected_icon);
        tvPlaceholderEmoji = view.findViewById(R.id.tv_placeholder_emoji);
        btnSubmit = view.findViewById(R.id.btn_create_submit);

        btnSelectIcon.setOnClickListener(v -> showPhotoMenu());

        btnSubmit.setOnClickListener(v -> createCommunity());

        return view;
    }

    private void showPhotoMenu() {
        PopupMenu popup = new PopupMenu(requireContext(), btnSelectIcon);
        popup.getMenu().add("Take Photo");
        popup.getMenu().add("Choose Photo");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Take Photo")) {
                cameraLauncher.launch(null);
            } else if (item.getTitle().equals("Choose Photo")) {
                galleryLauncher.launch("image/*");
            }
            return true;
        });
        popup.show();
    }

    private void updateIconView() {
        if (selectedBitmap != null) {
            tvPlaceholderEmoji.setVisibility(View.INVISIBLE);
            ivSelectedIcon.setVisibility(View.VISIBLE);
            ivSelectedIcon.setImageBitmap(selectedBitmap);
        }
    }

    private void createCommunity() {
        String name = etGroupName.getText() != null ? etGroupName.getText().toString().trim() : "";
        String subjectsRaw = etGroupSubjects.getText() != null ? etGroupSubjects.getText().toString().trim() : "";
        String desc = etGroupDesc.getText() != null ? etGroupDesc.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> subjects = Arrays.asList(subjectsRaw.split("\\s*,\\s*"));

        String[] randomEmojis = {"📚", "💻", "🔬", "🌍", "🌿", "📈", "🎨"};
        String emoji = randomEmojis[new Random().nextInt(randomEmojis.length)];

        StudyGroup newGroup = new StudyGroup(
                name,
                1, // Creator is the first member
                100, // Starting score
                desc,
                emoji,
                subjects,
                0.0,
                1,
                0, // sessionMembers
                "Just now"
        );
        newGroup.customIconBitmap = selectedBitmap;
        
        if (selectedBitmap != null) {
            try {
                android.graphics.Bitmap scaled = android.graphics.Bitmap.createScaledBitmap(selectedBitmap, 128, 128, true);
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos);
                newGroup.customIconBase64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CommunityViewModel viewModel = new ViewModelProvider(requireActivity()).get(CommunityViewModel.class);
        viewModel.createGroup(newGroup);

        Toast.makeText(getContext(), "Community created successfully!", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
