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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommunityFragment extends Fragment {

    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter leaderboardAdapter;
    private final List<LeaderboardEntry> leaderboardList = new ArrayList<>();
    private ListenerRegistration userListener;
    private ListenerRegistration leaderboardListener;

    private static final List<LeaderboardEntry> DUMMY_USERS = Arrays.asList(
            new LeaderboardEntry(0, "Aisha P.", 95, 120.5),
            new LeaderboardEntry(0, "David L.", 91, 105.0),
            new LeaderboardEntry(0, "Sarah M.", 88, 89.2),
            new LeaderboardEntry(0, "Michael K.", 79, 45.1),
            new LeaderboardEntry(0, "Elena G.", 75, 30.0),
            new LeaderboardEntry(0, "Chris J.", 72, 28.5)
    );

    private android.widget.TextView tvYourRankNumber, tvYourName, tvYourStats;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        rvLeaderboard = view.findViewById(R.id.rv_leaderboard);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        leaderboardAdapter = new LeaderboardAdapter(leaderboardList);
        rvLeaderboard.setAdapter(leaderboardAdapter);

        tvYourRankNumber = view.findViewById(R.id.tv_your_rank_number);
        tvYourName = view.findViewById(R.id.tv_your_name);
        tvYourStats = view.findViewById(R.id.tv_your_stats);

        loadLeaderboard();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadLeaderboard();
            androidx.lifecycle.ViewModelProvider provider = new androidx.lifecycle.ViewModelProvider(requireActivity());
            CommunityViewModel viewModel = provider.get(CommunityViewModel.class);
            viewModel.checkUserChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
        if (leaderboardListener != null) {
            leaderboardListener.remove();
            leaderboardListener = null;
        }
    }

    private void loadLeaderboard() {
        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showDummyOnly();
            return;
        }
        String currentUid = currentUser.getUid();

        if (userListener != null) userListener.remove();
        userListener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUid)
                .addSnapshotListener((selfDoc, error) -> {
                    if (error != null || selfDoc == null || !selfDoc.exists()) {
                        showDummyOnly();
                        return;
                    }
                    String selfName = selfDoc.getString("name");
                    Number selfHoursNum = (Number) selfDoc.get("totalHours");
                    Number selfScoreNum = (Number) selfDoc.get("averageScore");
                    double selfHours = selfHoursNum != null ? selfHoursNum.doubleValue() : 0;
                    int selfScore = selfScoreNum != null ? selfScoreNum.intValue() : 0;
                    String selfDisplay = "You";

                    LeaderboardEntry selfEntry = new LeaderboardEntry(0, selfDisplay, selfScore, selfHours);

                    if (leaderboardListener != null) leaderboardListener.remove();
                    leaderboardListener = FirebaseFirestore.getInstance()
                            .collection("users")
                            .orderBy("totalHours", com.google.firebase.firestore.Query.Direction.DESCENDING)
                            .limit(20)
                            .addSnapshotListener((querySnapshot, qError) -> {
                                if (qError != null || querySnapshot == null) {
                                    showDummyOnly();
                                    return;
                                }
                                List<LeaderboardEntry> realUsers = new ArrayList<>();
                                boolean selfIncluded = false;

                                for (QueryDocumentSnapshot doc : querySnapshot) {
                                    String name = doc.getString("name");
                                    Number hoursNum = (Number) doc.get("totalHours");
                                    Number scoreNum = (Number) doc.get("averageScore");
                                    if (name == null) continue;
                                    double hours = hoursNum != null ? hoursNum.doubleValue() : 0;
                                    int score = scoreNum != null ? scoreNum.intValue() : 0;
                                    if (doc.getId().equals(currentUid)) {
                                        realUsers.add(selfEntry);
                                        selfIncluded = true;
                                    } else {
                                        realUsers.add(new LeaderboardEntry(0, name, score, hours));
                                    }
                                }

                                if (!selfIncluded) {
                                    realUsers.add(selfEntry);
                                }

                                List<LeaderboardEntry> merged = new ArrayList<>(realUsers);
                                for (LeaderboardEntry dummy : DUMMY_USERS) {
                                    if (merged.size() >= 10) break;
                                    merged.add(dummy);
                                }

                                merged.sort((a, b) -> {
                                    if (b.totalHours != a.totalHours) {
                                        return Double.compare(b.totalHours, a.totalHours);
                                    }
                                    return Integer.compare(b.focusScore, a.focusScore);
                                });

                                leaderboardList.clear();
                                int myRank = 1;
                                for (int i = 0; i < merged.size(); i++) {
                                    LeaderboardEntry e = merged.get(i);
                                    leaderboardList.add(new LeaderboardEntry(i + 1, e.username, e.focusScore, e.totalHours));
                                    if (e == selfEntry) myRank = i + 1;
                                }
                                leaderboardAdapter.notifyDataSetChanged();

                                if (tvYourRankNumber != null) tvYourRankNumber.setText("#" + myRank);
                                if (tvYourName != null) tvYourName.setText(selfName != null ? selfName : "You");
                                if (tvYourStats != null) {
                                    String hrs = String.format(java.util.Locale.getDefault(), "%.1f", selfHours);
                                    tvYourStats.setText("Score: " + selfScore + " · " + hrs + " hrs");
                                }
                            });
                });
    }

    private void showDummyOnly() {
        leaderboardList.clear();
        for (int i = 0; i < DUMMY_USERS.size(); i++) {
            LeaderboardEntry d = DUMMY_USERS.get(i);
            leaderboardList.add(new LeaderboardEntry(i + 1, d.username, d.focusScore, d.totalHours));
        }
        leaderboardAdapter.notifyDataSetChanged();
        if (tvYourRankNumber != null) tvYourRankNumber.setText("--");
        if (tvYourName != null) tvYourName.setText("You");
        if (tvYourStats != null) tvYourStats.setText("No data yet");
    }
}
