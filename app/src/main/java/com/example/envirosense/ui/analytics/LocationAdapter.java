package com.example.envirosense.ui.analytics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.envirosense.R;
import com.example.envirosense.data.LocationStat;

public class LocationAdapter extends ListAdapter<LocationStat, LocationAdapter.LocationViewHolder> {

    public LocationAdapter() {
        super(new DiffUtil.ItemCallback<LocationStat>() {
            @Override
            public boolean areItemsTheSame(@NonNull LocationStat oldItem, @NonNull LocationStat newItem) {
                if (oldItem.location == null && newItem.location == null)
                    return true;
                if (oldItem.location == null || newItem.location == null)
                    return false;
                return oldItem.location.equals(newItem.location);
            }

            @Override
            public boolean areContentsTheSame(@NonNull LocationStat oldItem, @NonNull LocationStat newItem) {
                return oldItem.sessionCount == newItem.sessionCount && oldItem.avgScore == newItem.avgScore;
            }
        });
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location_stat, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationStat stat = getItem(position);
        holder.bind(stat);
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocationName;
        TextView tvSessionCount;
        TextView tvAvgScore;

        LocationViewHolder(View itemView) {
            super(itemView);
            tvLocationName = itemView.findViewById(R.id.tv_location_name);
            tvSessionCount = itemView.findViewById(R.id.tv_session_count);
            tvAvgScore = itemView.findViewById(R.id.tv_avg_score);
        }

        void bind(LocationStat stat) {
            String loc = stat.location != null && !stat.location.trim().isEmpty() ? stat.location : "Unknown Location";
            tvLocationName.setText(loc);
            tvSessionCount.setText(stat.sessionCount + " Sessions");
            tvAvgScore.setText("Avg Score: " + stat.avgScore);
        }
    }
}