package com.example.envirosense.ui.community;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Allows users to search for and discover public study groups.
 * Currently shows all mock groups; filter logic is wired to the search bar.
 */
public class SearchCommunityFragment extends Fragment {

    private RecyclerView rvSearchResults;
    private TextInputEditText etSearch;
    private GroupsAdapter adapter;

    // Full list of available groups (replace with network/DB call later)
    private final List<StudyGroup> allGroups = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_community, container, false);

        rvSearchResults = view.findViewById(R.id.rv_search_results);
        etSearch = view.findViewById(R.id.et_search);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroupsAdapter(new ArrayList<>(), getChildFragmentManager());
        rvSearchResults.setAdapter(adapter);

        CommunityViewModel viewModel = new ViewModelProvider(requireActivity()).get(CommunityViewModel.class);
        viewModel.getAvailableGroups().observe(getViewLifecycleOwner(), groups -> {
            allGroups.clear();
            if (groups != null) {
                allGroups.addAll(groups);
            }
            filterGroups(etSearch.getText() != null ? etSearch.getText().toString() : "");
        });

        // Wire up live search filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGroups(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void filterGroups(String query) {
        if (query.isEmpty()) {
            adapter.updateData(new ArrayList<>(allGroups));
            return;
        }
        String lower = query.toLowerCase();
        List<StudyGroup> filtered = new ArrayList<>();
        for (StudyGroup group : allGroups) {
            boolean nameMatch = group.groupName.toLowerCase().contains(lower);
            boolean subjectMatch = false;
            if (group.subjects != null) {
                for (String subject : group.subjects) {
                    if (subject.toLowerCase().contains(lower)) {
                        subjectMatch = true;
                        break;
                    }
                }
            }
            if (nameMatch || subjectMatch) {
                filtered.add(group);
            }
        }
        adapter.updateData(filtered);
    }
}
