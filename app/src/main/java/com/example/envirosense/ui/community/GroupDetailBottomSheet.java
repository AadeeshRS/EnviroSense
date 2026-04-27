package com.example.envirosense.ui.community;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.envirosense.R;
import com.example.envirosense.data.models.StudyGroup;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class GroupDetailBottomSheet extends BottomSheetDialogFragment {

    private final StudyGroup group;
    private ListenerRegistration memberCountListener;
    private ListenerRegistration activeUsersListener;

    // Same chip colors as GroupsAdapter for consistency
    private static final int[] CHIP_COLORS = {
            0xFF3A3D3F,  // dark gray
            0xFF1B5E20,  // deep green
            0xFF0D47A1,  // deep blue
            0xFF4E342E,  // deep brown
            0xFF4A148C,  // deep purple
            0xFFBF360C,  // deep orange
            0xFF006064,  // deep teal
    };

    public GroupDetailBottomSheet(StudyGroup group) {
        this.group = group;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_group_detail, container, false);

        // Header
        TextView tvEmoji = view.findViewById(R.id.tv_sheet_emoji);
        android.widget.ImageView ivIcon = view.findViewById(R.id.iv_sheet_icon);
        TextView tvName = view.findViewById(R.id.tv_sheet_group_name);
        TextView tvDesc = view.findViewById(R.id.tv_sheet_description);

        if (group.customIconBase64 != null && !group.customIconBase64.isEmpty()) {
            try {
                byte[] decodedBytes = android.util.Base64.decode(group.customIconBase64, android.util.Base64.DEFAULT);
                android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                ivIcon.setVisibility(View.VISIBLE);
                ivIcon.setImageBitmap(bmp);
                tvEmoji.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                ivIcon.setVisibility(View.INVISIBLE);
                tvEmoji.setVisibility(View.VISIBLE);
                tvEmoji.setText(group.emoji);
            }
        } else if (group.customIconBitmap != null) {
            ivIcon.setVisibility(View.VISIBLE);
            ivIcon.setImageBitmap(group.customIconBitmap);
            tvEmoji.setVisibility(View.GONE);
        } else {
            ivIcon.setVisibility(View.GONE);
            tvEmoji.setVisibility(View.VISIBLE);
            tvEmoji.setText(group.emoji);
        }
        tvName.setText(group.groupName);
        tvDesc.setText(group.description);

        // Subject chips
        LinearLayout subjectRows = view.findViewById(R.id.sheet_subject_rows);
        if (group.subjects != null) {
            for (int i = 0; i < group.subjects.size(); i++) {
                subjectRows.addView(createSubjectChip(view, group.subjects.get(i), i));
            }
        }

        // Stats
        TextView tvMembers = view.findViewById(R.id.tv_sheet_members);
        TextView tvActive = view.findViewById(R.id.tv_sheet_active);
        TextView tvHours = view.findViewById(R.id.tv_sheet_hours);
        TextView tvAvgScore = view.findViewById(R.id.tv_sheet_avg_score);
        TextView tvCreated = view.findViewById(R.id.tv_sheet_created);

        // Set initial static values
        tvMembers.setText(String.valueOf(group.memberCount));
        tvActive.setText(String.valueOf(group.activeMembers));
        tvHours.setText(String.format("%.1f hrs", group.totalFocusHours));
        tvAvgScore.setText(String.valueOf(group.avgScore));
        tvCreated.setText(group.creationDate);

        // Real-time member count from the group document
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        memberCountListener = db.collection("groups").document(group.groupName.trim())
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;
                    Long count = snapshot.getLong("memberCount");
                    tvMembers.setText(String.valueOf(count != null ? count : 0));
                });

        // Real-time active users from the active_users sub-collection
        activeUsersListener = db.collection("groups").document(group.groupName.trim())
                .collection("active_users")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    tvActive.setText(String.valueOf(value.size()));
                });

        // Check ViewModel for current state
        CommunityViewModel viewModel = new ViewModelProvider(requireActivity()).get(CommunityViewModel.class);
        boolean isJoined = viewModel.isGroupJoined(group);

        // Join/Leave button logic
        MaterialButton btnJoin = view.findViewById(R.id.btn_join_group);
        if (isJoined) {
            btnJoin.setText("Leave Group");
            // Optional styling for a leave button: red or outlined
            // For now, keep it simple by just changing the text
            btnJoin.setOnClickListener(v -> {
                viewModel.leaveGroup(group);
                Toast.makeText(requireContext(), "Left " + group.groupName, Toast.LENGTH_SHORT).show();
                dismiss();
            });
        } else {
            btnJoin.setText("Join Group");
            btnJoin.setOnClickListener(v -> {
                viewModel.joinGroup(group);
                Toast.makeText(requireContext(), "Joined " + group.groupName + "!", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }

        return view;
    }

    private TextView createSubjectChip(View parent, String subject, int index) {
        TextView chip = new TextView(parent.getContext());
        chip.setText(subject);
        chip.setTextColor(0xFFFFFFFF);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(parent, 20));
        bg.setColor(CHIP_COLORS[index % CHIP_COLORS.length]);
        chip.setBackground(bg);

        int hPad = dpToPx(parent, 10);
        int vPad = dpToPx(parent, 4);
        chip.setPadding(hPad, vPad, hPad, vPad);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dpToPx(parent, 6));
        chip.setLayoutParams(lp);

        return chip;
    }

    private int dpToPx(View view, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                view.getContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (memberCountListener != null) memberCountListener.remove();
        if (activeUsersListener != null) activeUsersListener.remove();
    }
}
