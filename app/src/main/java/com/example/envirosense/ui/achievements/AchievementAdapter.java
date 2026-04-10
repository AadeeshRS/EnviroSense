package com.example.envirosense.ui.achievements; // Adjust package as needed

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.envirosense.R;
import com.example.envirosense.data.Achievement;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {

    private final List<Achievement> achievements;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Achievement achievement);
    }

    public AchievementAdapter(List<Achievement> achievements, OnItemClickListener listener) {
        this.achievements = achievements;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);

        holder.tvTitle.setText(achievement.title);
        holder.tvSubtitle.setText(achievement.description);
        holder.ivIcon.setImageResource(achievement.iconResId);


        if (achievement.isUnlocked) {
            holder.ivLock.setVisibility(View.GONE);
            holder.cardView.setStrokeColor(Color.parseColor("#4CAF50"));
            holder.cardView.setStrokeWidth(3);

            holder.tvTitle.setTextColor(Color.WHITE);
            holder.tvSubtitle.setTextColor(Color.parseColor("#A0A0A0"));

            holder.ivIcon.setColorFilter(Color.parseColor("#4CAF50"));

        } else {
            holder.ivLock.setVisibility(View.VISIBLE);
            holder.cardView.setStrokeColor(Color.parseColor("#2A2C2E"));
            holder.cardView.setStrokeWidth(2);

            holder.tvTitle.setTextColor(Color.parseColor("#888888"));
            holder.tvSubtitle.setTextColor(Color.parseColor("#555555"));

            holder.ivIcon.setColorFilter(Color.parseColor("#888888"));
        }

        holder.cardView.setOnClickListener(v -> listener.onItemClick(achievement));
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvTitle, tvSubtitle;
        ImageView ivIcon, ivLock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_achievement);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            ivLock = itemView.findViewById(R.id.iv_lock);
        }
    }
}