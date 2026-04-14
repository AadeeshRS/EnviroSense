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
import com.example.envirosense.data.FocusSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionAdapter extends ListAdapter<FocusSession, SessionAdapter.SessionViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(FocusSession session);
    }

    private OnItemClickListener listener;

    public SessionAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<FocusSession>() {
            @Override
            public boolean areItemsTheSame(@NonNull FocusSession oldItem, @NonNull FocusSession newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull FocusSession oldItem, @NonNull FocusSession newItem) {
                return oldItem.timestamp == newItem.timestamp &&
                        oldItem.finalScore == newItem.finalScore &&
                        oldItem.durationMs == newItem.durationMs &&
                        (oldItem.location != null ? oldItem.location.equals(newItem.location) : newItem.location == null);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        FocusSession session = getItem(position);

        holder.tvScore.setText(String.valueOf(session.finalScore));

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        holder.tvDatetime.setText(sdf.format(new Date(session.timestamp)));

        String durationStr = formatDuration(session.durationMs);
        holder.tvDetails.setText(durationStr + " · " + session.location);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(session);
            }
        });
    }

    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%dm %ds", minutes, seconds % 60);
        } else {
            return String.format(Locale.getDefault(), "%ds", seconds);
        }
    }

    public static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvScore, tvDatetime, tvDetails;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScore = itemView.findViewById(R.id.tv_item_score);
            tvDatetime = itemView.findViewById(R.id.tv_item_datetime);
            tvDetails = itemView.findViewById(R.id.tv_item_details);
        }
    }
}