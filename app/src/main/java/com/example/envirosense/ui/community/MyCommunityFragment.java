package com.example.envirosense.ui.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.models.StudyGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Displays the groups the current user has joined.
 * Currently shows mock data; wire up to a real data source when ready.
 */
public class MyCommunityFragment extends Fragment {

    private RecyclerView rvMyCommunities;
    private View layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_communities, container, false);

        rvMyCommunities = view.findViewById(R.id.rv_my_communities);
        layoutEmpty = view.findViewById(R.id.layout_empty_my_communities);

        rvMyCommunities.setLayoutManager(new LinearLayoutManager(getContext()));
        GroupsAdapter adapter = new GroupsAdapter(new ArrayList<>(), getChildFragmentManager());
        rvMyCommunities.setAdapter(adapter);

        CommunityViewModel viewModel = new ViewModelProvider(requireActivity()).get(CommunityViewModel.class);
        viewModel.getJoinedGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups == null || groups.isEmpty()) {
                rvMyCommunities.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                rvMyCommunities.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
                adapter.updateData(groups);
            }
        });

        return view;
    }
}
