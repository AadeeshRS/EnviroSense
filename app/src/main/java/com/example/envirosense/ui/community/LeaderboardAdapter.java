package com.example.envirosense.ui.community;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.models.LeaderboardEntry;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardEntry> items;

    public LeaderboardAdapter(List<LeaderboardEntry> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = items.get(position);
        
        holder.tvRank.setText(String.valueOf(entry.rank));
        holder.tvAvatar.setText(entry.initials);
        holder.tvUsername.setText(entry.username);
        holder.tvHours.setText(String.format("%.1f hrs focused", entry.totalHours));
        holder.tvScoreBadge.setText(String.valueOf(entry.focusScore));

        // Styling for top 3
        if (entry.rank == 1) {
            holder.tvRank.setTextColor(Color.parseColor("#FFD700")); // Gold
            holder.tvAvatar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700")));
        } else if (entry.rank == 2) {
            holder.tvRank.setTextColor(Color.parseColor("#C0C0C0")); // Silver
            holder.tvAvatar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#C0C0C0")));
        } else if (entry.rank == 3) {
            holder.tvRank.setTextColor(Color.parseColor("#CD7F32")); // Bronze
            holder.tvAvatar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#CD7F32")));
        } else {
            holder.tvRank.setTextColor(Color.parseColor("#8C959A")); // text_secondary
            holder.tvAvatar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#57C18B"))); // primary_green
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvAvatar, tvUsername, tvHours, tvScoreBadge;

        ViewHolder(View view) {
            super(view);
            tvRank = view.findViewById(R.id.tv_rank);
            tvAvatar = view.findViewById(R.id.tv_avatar);
            tvUsername = view.findViewById(R.id.tv_username);
            tvHours = view.findViewById(R.id.tv_hours);
            tvScoreBadge = view.findViewById(R.id.tv_score_badge);
        }
    }
}
