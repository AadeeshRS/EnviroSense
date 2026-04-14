package com.example.envirosense.ui.community;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.envirosense.data.models.StudyGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommunityViewModel extends ViewModel {

    private final MutableLiveData<List<StudyGroup>> joinedGroups = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<StudyGroup>> availableGroups = new MutableLiveData<>(new ArrayList<>());

    public CommunityViewModel() {
        // Initialize mock data
        List<StudyGroup> initialAvailable = new ArrayList<>();
        initialAvailable.add(new StudyGroup("Coding Elite", 45, 92,
                "Focus hours for CS students and devs", "💻",
                Arrays.asList("DSA", "OS", "Java", "Python", "ML"), 1280.0, 18, "Dec 01, 2025"));
        initialAvailable.add(new StudyGroup("Morning Birds", 8, 85,
                "We focus early. 5AM - 8AM only.", "🌅",
                Arrays.asList("Maths", "Physics"), 96.2, 4, "Mar 10, 2026"));

        List<StudyGroup> initialJoined = new ArrayList<>();
        initialJoined.add(new StudyGroup(
                "Library Legends", 12, 78,
                "Daily study sessions at the campus library", "📚",
                Arrays.asList("DCCN", "DAA", "DBMS"),
                340.5, 5, "Jan 15, 2026"));

        availableGroups.setValue(initialAvailable);
        joinedGroups.setValue(initialJoined);
    }

    public LiveData<List<StudyGroup>> getJoinedGroups() {
        return joinedGroups;
    }

    public LiveData<List<StudyGroup>> getAvailableGroups() {
        return availableGroups;
    }

    public boolean isGroupJoined(StudyGroup group) {
        List<StudyGroup> currentJoined = joinedGroups.getValue();
        if (currentJoined != null) {
            return currentJoined.contains(group);
        }
        return false;
    }

    public void joinGroup(StudyGroup group) {
        List<StudyGroup> available = new ArrayList<>(availableGroups.getValue() != null ? availableGroups.getValue() : new ArrayList<>());
        List<StudyGroup> joined = new ArrayList<>(joinedGroups.getValue() != null ? joinedGroups.getValue() : new ArrayList<>());

        if (available.remove(group)) {
            joined.add(group);
            availableGroups.setValue(available);
            joinedGroups.setValue(joined);
        }
    }

    public void createGroup(StudyGroup group) {
        List<StudyGroup> joined = new ArrayList<>(joinedGroups.getValue() != null ? joinedGroups.getValue() : new ArrayList<>());
        joined.add(0, group); // Add to top
        joinedGroups.setValue(joined);
    }

    public void leaveGroup(StudyGroup group) {
        List<StudyGroup> available = new ArrayList<>(availableGroups.getValue() != null ? availableGroups.getValue() : new ArrayList<>());
        List<StudyGroup> joined = new ArrayList<>(joinedGroups.getValue() != null ? joinedGroups.getValue() : new ArrayList<>());

        if (joined.remove(group)) {
            available.add(group);
            availableGroups.setValue(available);
            joinedGroups.setValue(joined);
        }
    }
}
