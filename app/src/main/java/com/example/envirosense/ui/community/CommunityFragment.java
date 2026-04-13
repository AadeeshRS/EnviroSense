package com.example.envirosense.ui.community;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.models.LeaderboardEntry;
import com.example.envirosense.data.models.StudyGroup;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private RecyclerView rvLeaderboard;
    private RecyclerView rvGroups;
    private TextView tvTabLeaderboard;
    private TextView tvTabGroups;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        rvLeaderboard = view.findViewById(R.id.rv_leaderboard);
        rvGroups = view.findViewById(R.id.rv_groups);
        tvTabLeaderboard = view.findViewById(R.id.tv_tab_leaderboard);
        tvTabGroups = view.findViewById(R.id.tv_tab_groups);

        // Setup toggles matching the styling in AnalyticsFragment
        tvTabLeaderboard.setOnClickListener(v -> showLeaderboard());
        tvTabGroups.setOnClickListener(v -> showGroups());

        // Setup Leaderboard Mock Data
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        List<LeaderboardEntry> leaderboardMock = new ArrayList<>();
        leaderboardMock.add(new LeaderboardEntry(1, "Aisha P.", 95, 120.5));
        leaderboardMock.add(new LeaderboardEntry(2, "David L.", 91, 105.0));
        leaderboardMock.add(new LeaderboardEntry(3, "Sarah M.", 88, 89.2));
        leaderboardMock.add(new LeaderboardEntry(4, "You", 82, 24.5));
        leaderboardMock.add(new LeaderboardEntry(5, "Michael K.", 79, 45.1));
        leaderboardMock.add(new LeaderboardEntry(6, "Elena G.", 75, 30.0));
        leaderboardMock.add(new LeaderboardEntry(7, "Chris J.", 72, 28.5));
        rvLeaderboard.setAdapter(new LeaderboardAdapter(leaderboardMock));

        // Setup Groups Mock Data
        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        List<StudyGroup> groupsMock = new ArrayList<>();
        groupsMock.add(new StudyGroup("Library Legends", 12, 78, "Daily study sessions at the campus library", "📚", java.util.Arrays.asList("DCCN", "DAA", "DBMS"), 340.5, 5, "Jan 15, 2026"));
        groupsMock.add(new StudyGroup("Coding Elite", 45, 92, "Focus hours for CS students and devs", "💻", java.util.Arrays.asList("DSA", "OS", "Java", "Python", "ML"), 1280.0, 18, "Dec 01, 2025"));
        groupsMock.add(new StudyGroup("Morning Birds", 8, 85, "We focus early. 5AM - 8AM only.", "🌅", java.util.Arrays.asList("Maths", "Physics"), 96.2, 4, "Mar 10, 2026"));
        rvGroups.setAdapter(new GroupsAdapter(groupsMock, getChildFragmentManager()));

        // Default state
        showLeaderboard();

        return view;
    }

    private void showLeaderboard() {
        rvLeaderboard.setVisibility(View.VISIBLE);
        rvGroups.setVisibility(View.GONE);

        tvTabLeaderboard.setBackgroundResource(R.drawable.bg_card_rounded);
        tvTabLeaderboard.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2A2C2E")));
        tvTabLeaderboard.setTextColor(Color.WHITE);

        tvTabGroups.setBackgroundColor(Color.TRANSPARENT);
        tvTabGroups.setTextColor(Color.parseColor("#8C959A"));
    }

    private void showGroups() {
        rvLeaderboard.setVisibility(View.GONE);
        rvGroups.setVisibility(View.VISIBLE);

        tvTabGroups.setBackgroundResource(R.drawable.bg_card_rounded);
        tvTabGroups.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2A2C2E")));
        tvTabGroups.setTextColor(Color.WHITE);

        tvTabLeaderboard.setBackgroundColor(Color.TRANSPARENT);
        tvTabLeaderboard.setTextColor(Color.parseColor("#8C959A"));
    }
}
