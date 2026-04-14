package com.example.envirosense.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.envirosense.R;
import com.example.envirosense.data.FocusSession;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionDetailBottomSheet extends BottomSheetDialogFragment {

    private FocusSession session;

    public interface OnSessionActionListener {
        void onDeleteClicked(FocusSession session);

        void onUpdateClicked(FocusSession session);
    }

    private OnSessionActionListener listener;

    public SessionDetailBottomSheet(FocusSession session, OnSessionActionListener listener) {
        this.session = session;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_session_detail, container, false);

        TextView tvScore = view.findViewById(R.id.tv_detail_score_circle);
        TextView tvHeaderDateTime = view.findViewById(R.id.tv_header_date_time);
        TextView tvHeaderDesc = view.findViewById(R.id.tv_header_desc);
        EditText etLocation = view.findViewById(R.id.et_edit_location);

        tvScore.setText(String.valueOf(session.finalScore));
        
        String durationStr = formatDuration(session.durationMs);

        SimpleDateFormat sdfDate = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        tvHeaderDateTime.setText(sdfDate.format(new Date(session.timestamp)));

        String locationStr = (session.location != null && !session.location.trim().isEmpty()) ? session.location : "Local Area";
        tvHeaderDesc.setText(locationStr + " · " + durationStr);

        if (session.location != null) {
            etLocation.setText(session.location);
        }

        view.findViewById(R.id.btn_save_close).setOnClickListener(v -> {
            String newLoc = etLocation.getText().toString().trim();
            if (!newLoc.equals(session.location)) {
                session.location = newLoc;
                if (listener != null) {
                    listener.onUpdateClicked(session);
                }
            }
            dismiss();
        });

        view.findViewById(R.id.btn_delete_session).setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onDeleteClicked(session);
            }
        });

        return view;
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
}