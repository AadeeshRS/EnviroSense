package com.example.envirosense.ui.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.envirosense.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateFilterBottomSheet extends BottomSheetDialogFragment {

    public interface OnFilterAppliedListener {
        void onFilterApplied(long startTime, long endTime, String label);
    }

    private OnFilterAppliedListener listener;
    private long startTime;
    private long endTime;
    private String label = "All time";

    public DateFilterBottomSheet(OnFilterAppliedListener listener) {
        this.listener = listener;
        this.endTime = System.currentTimeMillis();
        this.startTime = 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_date_filter, container, false);

        TextView chip7 = view.findViewById(R.id.chip_7_days);
        TextView chip30 = view.findViewById(R.id.chip_30_days);
        TextView chipMonth = view.findViewById(R.id.chip_this_month);
        TextView chipAll = view.findViewById(R.id.chip_all_time);
        
        TextView tvFromDate = view.findViewById(R.id.tv_from_date);
        TextView tvToDate = view.findViewById(R.id.tv_to_date);
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());

        View.OnClickListener clickListener = v -> {
            chip7.setBackgroundTintList(null);
            chip7.setTextColor(Color.parseColor("#8C959A"));
            chip30.setBackgroundTintList(null);
            chip30.setTextColor(Color.parseColor("#8C959A"));
            chipMonth.setBackgroundTintList(null);
            chipMonth.setTextColor(Color.parseColor("#8C959A"));
            chipAll.setBackgroundTintList(null);
            chipAll.setTextColor(Color.parseColor("#8C959A"));

            ((TextView) v)
                    .setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#57C18B")));
            ((TextView) v).setTextColor(Color.WHITE);

            Calendar cal = Calendar.getInstance();
            endTime = System.currentTimeMillis();

            if (v == chip7) {
                cal.add(Calendar.DAY_OF_YEAR, -7);
                startTime = cal.getTimeInMillis();
                label = "Last 7 days";
            } else if (v == chip30) {
                cal.add(Calendar.DAY_OF_YEAR, -30);
                startTime = cal.getTimeInMillis();
                label = "Last 30 days";
            } else if (v == chipMonth) {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startTime = cal.getTimeInMillis();
                label = "This month";
            } else if (v == chipAll) {
                startTime = 0;
                label = "All time";
            }
            
            tvToDate.setText(sdf.format(endTime));
            if (startTime == 0) {
                tvFromDate.setText("Begin");
            } else {
                tvFromDate.setText(sdf.format(startTime));
            }
        };

        chip7.setOnClickListener(clickListener);
        chip30.setOnClickListener(clickListener);
        chipMonth.setOnClickListener(clickListener);
        chipAll.setOnClickListener(clickListener);

        view.findViewById(R.id.btn_apply_filter).setOnClickListener(v -> {
            if (listener != null) {
                long finalEndTime = (startTime == 0) ? Long.MAX_VALUE : endTime;
                listener.onFilterApplied(startTime, finalEndTime, label);
            }
            dismiss();
        });

    
        chipAll.performClick();

        return view;
    }
}