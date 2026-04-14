package com.example.envirosense.ui.community;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.models.StudyGroup;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    public interface OnGroupLeaveListener {
        void onLeave(StudyGroup group);
    }

    private List<StudyGroup> items;
    private final FragmentManager fragmentManager;
    private final boolean isCompactMode;
    private final OnGroupLeaveListener onLeaveListener;

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

    public GroupsAdapter(List<StudyGroup> items, FragmentManager fragmentManager, boolean isCompactMode, OnGroupLeaveListener onLeaveListener) {
        this.items = items;
        this.fragmentManager = fragmentManager;
        this.isCompactMode = isCompactMode;
        this.onLeaveListener = onLeaveListener;
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
        
        if (group.customIconBitmap != null) {
            holder.ivGroupIcon.setVisibility(View.VISIBLE);
            holder.ivGroupIcon.setImageBitmap(group.customIconBitmap);
            holder.tvGroupEmoji.setVisibility(View.INVISIBLE);
        } else {
            holder.ivGroupIcon.setVisibility(View.INVISIBLE);
            holder.tvGroupEmoji.setVisibility(View.VISIBLE);
            holder.tvGroupEmoji.setText(group.emoji);
        }

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

        if (isCompactMode) {
            holder.tvGroupDesc.setVisibility(View.GONE);
            holder.statsRow.setVisibility(View.GONE);
            holder.btnMoreOptions.setVisibility(View.VISIBLE);

            holder.btnMoreOptions.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMoreOptions);
                popup.getMenu().add("Leave Community");
                MenuItem leaveItem = popup.getMenu().getItem(0);
                SpannableString s = new SpannableString(leaveItem.getTitle());
                s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
                leaveItem.setTitle(s);

                popup.setOnMenuItemClickListener(item -> {
                    if (onLeaveListener != null) {
                        onLeaveListener.onLeave(group);
                    }
                    return true;
                });
                popup.show();
            });
        } else {
            holder.tvGroupDesc.setVisibility(View.VISIBLE);
            holder.statsRow.setVisibility(View.VISIBLE);
            holder.btnMoreOptions.setVisibility(View.GONE);
            
            holder.btnViewGroup.setOnClickListener(v -> {
                GroupDetailBottomSheet bottomSheet = new GroupDetailBottomSheet(group);
                bottomSheet.show(fragmentManager, "GroupDetail");
            });
        }
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

    /**
     * Replaces the current dataset and refreshes the list.
     * Used by SearchCommunityFragment for live filtering.
     */
    public void updateData(List<StudyGroup> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupEmoji, tvGroupName, tvGroupDesc, tvMemberCount, tvAvgScore;
        LinearLayout subjectRows;
        LinearLayout statsRow;
        MaterialButton btnViewGroup;
        ImageView btnMoreOptions;
        ImageView ivGroupIcon;

        ViewHolder(View view) {
            super(view);
            tvGroupEmoji = view.findViewById(R.id.tv_group_emoji);
            ivGroupIcon = view.findViewById(R.id.iv_group_icon);
            tvGroupName = view.findViewById(R.id.tv_group_name);
            tvGroupDesc = view.findViewById(R.id.tv_group_desc);
            tvMemberCount = view.findViewById(R.id.tv_member_count);
            tvAvgScore = view.findViewById(R.id.tv_avg_score);
            subjectRows = view.findViewById(R.id.subject_rows);
            statsRow = view.findViewById(R.id.stats_row);
            btnViewGroup = view.findViewById(R.id.btn_view_group);
            btnMoreOptions = view.findViewById(R.id.btn_more_options);
        }
    }
}
