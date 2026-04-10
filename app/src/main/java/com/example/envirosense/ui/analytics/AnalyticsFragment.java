package com.example.envirosense.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.Collections;
import com.example.envirosense.R;
import com.example.envirosense.data.AppDatabase;
import com.example.envirosense.data.FocusSession;

import java.util.List;

import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;

public class AnalyticsFragment extends Fragment {

    private RecyclerView rvSessions;
    private SessionAdapter sessionAdapter;
    private LocationAdapter locationAdapter;
    private LinearLayout emptyState;
    private LineChart lineChart;
    private FrameLayout graphCard;

    private TextView tvTabSessions;
    private TextView tvTabLocations;
    private android.widget.ImageView btnFilter;
    private boolean isShowingLocations = false;

    private long currentFilterStartTime = 0;
    private long currentFilterEndTime = Long.MAX_VALUE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        rvSessions = view.findViewById(R.id.rv_sessions);
        emptyState = view.findViewById(R.id.analytics_empty_state);
        lineChart = view.findViewById(R.id.line_chart);
        graphCard = view.findViewById(R.id.graph_card);
        tvTabSessions = view.findViewById(R.id.tv_tab_sessions);
        tvTabLocations = view.findViewById(R.id.tv_tab_locations);
        btnFilter = view.findViewById(R.id.btn_filter);
        
        setupChart();
        
        rvSessions.setLayoutManager(new LinearLayoutManager(getContext()));

        sessionAdapter = new SessionAdapter(session -> {
            SessionDetailBottomSheet detailSheet = new SessionDetailBottomSheet(session,
                    new SessionDetailBottomSheet.OnSessionActionListener() {
                        @Override
                        public void onDeleteClicked(FocusSession sessionToDelete) {
                            new Thread(() -> {
                                AppDatabase.getInstance(requireContext()).focusSessionDao().delete(sessionToDelete);
                                if (isShowingLocations) {
                                    loadLocations();
                                } else {
                                    loadSessions();
                                }
                            }).start();
                        }

                        @Override
                        public void onUpdateClicked(FocusSession updatedSession) {
                            new Thread(() -> {
                                AppDatabase.getInstance(requireContext()).focusSessionDao().update(updatedSession);
                                if (isShowingLocations) {
                                    loadLocations();
                                } else {
                                    loadSessions();
                                }
                            }).start();
                        }
                    });
            detailSheet.show(getParentFragmentManager(), "SessionDetailBottomSheet");
        });

        locationAdapter = new LocationAdapter();

        rvSessions.setAdapter(sessionAdapter);

        tvTabSessions.setOnClickListener(v -> switchTab(false));
        tvTabLocations.setOnClickListener(v -> switchTab(true));

        btnFilter.setOnClickListener(v -> {
            DateFilterBottomSheet filterSheet = new DateFilterBottomSheet((startTime, endTime, label) -> {
                currentFilterStartTime = startTime;
                currentFilterEndTime = endTime;
                if (isShowingLocations) {
                    loadLocations();
                } else {
                    loadSessions();
                }
                
            });
            filterSheet.show(getParentFragmentManager(), "DateFilterBottomSheet");
        });

        loadSessions();

        return view;
    }

    private void switchTab(boolean showLocations) {
        isShowingLocations = showLocations;

        tvTabSessions.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor(showLocations ? "#00000000" : "#2A2C2E")));
        tvTabSessions.setTextColor(Color.parseColor(showLocations ? "#A0A0A0" : "#FFFFFF"));
        tvTabSessions.setTypeface(null, showLocations ? Typeface.NORMAL : Typeface.BOLD);

        tvTabLocations.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor(showLocations ? "#2A2C2E" : "#00000000")));
        tvTabLocations.setTextColor(Color.parseColor(showLocations ? "#FFFFFF" : "#A0A0A0"));
        tvTabLocations.setTypeface(null, showLocations ? Typeface.BOLD : Typeface.NORMAL);

        if (showLocations) {
            graphCard.setVisibility(View.GONE);
            rvSessions.setAdapter(locationAdapter);
            loadLocations();
        } else {
            graphCard.setVisibility(View.VISIBLE);
            rvSessions.setAdapter(sessionAdapter);
            loadSessions();
        }
    }

    private void loadLocations() {
        new Thread(() -> {
            List<com.example.envirosense.data.LocationStat> stats;
            if (currentFilterStartTime <= 0) {
                stats = AppDatabase.getInstance(requireContext())
                        .focusSessionDao()
                        .getLocationStats();
            } else {
                stats = AppDatabase.getInstance(requireContext())
                        .focusSessionDao()
                        .getLocationStatsInRange(currentFilterStartTime, currentFilterEndTime);
            }

            requireActivity().runOnUiThread(() -> {
                if (stats.isEmpty()) {
                    rvSessions.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    rvSessions.setVisibility(View.VISIBLE);
                    locationAdapter.submitList(stats);
                }
            });
        }).start();
    }

    private void loadSessions() {
        new Thread(() -> {
            List<FocusSession> sessions;
            if (currentFilterStartTime <= 0) {
                sessions = AppDatabase.getInstance(requireContext())
                        .focusSessionDao()
                        .getAllSessions();
            } else {
                sessions = AppDatabase.getInstance(requireContext())
                        .focusSessionDao()
                        .getSessionsInRange(currentFilterStartTime, currentFilterEndTime);
            }

            requireActivity().runOnUiThread(() -> {
                if (sessions != null && !sessions.isEmpty()) {
                    emptyState.setVisibility(View.GONE);
                    rvSessions.setVisibility(View.VISIBLE);
                    sessionAdapter.submitList(sessions, () -> {
                        rvSessions.scrollToPosition(0);
                    });
                    updateChartData(sessions);
                } else {
                    emptyState.setVisibility(View.VISIBLE);
                    rvSessions.setVisibility(View.GONE);
                    lineChart.clear();
                }
            });
        }).start();

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (isShowingLocations) {
                loadLocations();
            } else {
                loadSessions();
            }
        }
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.setDrawGridBackground(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        lineChart.getAxisRight().setEnabled(false);
    }

    private void updateChartData(List<FocusSession> sessions) {
        if (sessions == null || sessions.size() < 2) {
            lineChart.clear();
            return;
        }

        List<FocusSession> chronological = new ArrayList<>(sessions);
        Collections.reverse(chronological);

        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < chronological.size(); i++) {
            entries.add(new Entry(i, chronological.get(i).finalScore));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Scores");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(2.5f);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);

        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_chart_gradient));

        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            if (isShowingLocations) {
                loadLocations();
            } else {
                loadSessions();
            }
        }
    }

}