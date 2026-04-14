package com.example.envirosense.ui.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.models.LeaderboardEntry;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private RecyclerView rvLeaderboard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        rvLeaderboard = view.findViewById(R.id.rv_leaderboard);

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
        return view;
    }
}
