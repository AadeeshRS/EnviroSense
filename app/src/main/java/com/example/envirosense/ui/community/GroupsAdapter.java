package com.example.envirosense.ui.community;

import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.models.StudyGroup;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    private final List<StudyGroup> items;
    private final FragmentManager fragmentManager;

    // Curated chip colors — muted tones that match the dark UI
    private static final int[] CHIP_COLORS = {
            0xFF3A3D3F,  // dark gray
            0xFF1B5E20,  // deep green
            0xFF0D47A1,  // deep blue
            0xFF4E342E,  // deep brown
            0xFF4A148C,  // deep purple
            0xFFBF360C,  // deep orange
            0xFF006064,  // deep teal
    };

    public GroupsAdapter(List<StudyGroup> items, FragmentManager fragmentManager) {
        this.items = items;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudyGroup group = items.get(position);
        
        holder.tvGroupEmoji.setText(group.emoji);
        holder.tvGroupName.setText(group.groupName);
        holder.tvGroupDesc.setText(group.description);
        holder.tvMemberCount.setText(String.valueOf(group.memberCount));
        holder.tvAvgScore.setText(String.valueOf(group.avgScore));

        // Populate subject chips
        holder.subjectRows.removeAllViews();
        if (group.subjects != null) {
            for (int i = 0; i < group.subjects.size(); i++) {
                String subject = group.subjects.get(i);
                TextView chip = createSubjectChip(holder.itemView, subject, i);
                holder.subjectRows.addView(chip);
            }
        }

        holder.btnViewGroup.setOnClickListener(v -> {
            GroupDetailBottomSheet bottomSheet = new GroupDetailBottomSheet(group);
            bottomSheet.show(fragmentManager, "GroupDetail");
        });
    }

    /**
     * Creates a pill-shaped subject chip TextView styled to match the app's dark UI.
     * Each chip gets a deterministic color based on its index within the group.
     */
    private TextView createSubjectChip(View parent, String subject, int index) {
        TextView chip = new TextView(parent.getContext());
        chip.setText(subject);
        chip.setTextColor(0xFFFFFFFF);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        // Pill background with a unique color per chip
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(parent, 20));
        bg.setColor(CHIP_COLORS[index % CHIP_COLORS.length]);
        chip.setBackground(bg);

        // Padding & margins
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
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupEmoji, tvGroupName, tvGroupDesc, tvMemberCount, tvAvgScore;
        LinearLayout subjectRows;
        MaterialButton btnViewGroup;

        ViewHolder(View view) {
            super(view);
            tvGroupEmoji = view.findViewById(R.id.tv_group_emoji);
            tvGroupName = view.findViewById(R.id.tv_group_name);
            tvGroupDesc = view.findViewById(R.id.tv_group_desc);
            tvMemberCount = view.findViewById(R.id.tv_member_count);
            tvAvgScore = view.findViewById(R.id.tv_avg_score);
            subjectRows = view.findViewById(R.id.subject_rows);
            btnViewGroup = view.findViewById(R.id.btn_view_group);
        }
    }
}
