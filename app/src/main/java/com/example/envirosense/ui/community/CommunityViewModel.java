package com.example.envirosense.ui.community;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.envirosense.data.models.StudyGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityViewModel extends ViewModel {

    private final MutableLiveData<List<StudyGroup>> joinedGroups = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<StudyGroup>> availableGroups = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration groupsListener;
    private ListenerRegistration joinedListener;
    private String currentUid;

    private List<StudyGroup> allGroupsCache = new ArrayList<>();
    private List<String> joinedNamesCache = new ArrayList<>();

    public CommunityViewModel() {
        loadGroups();
    }

    private void loadGroups() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        currentUid = uid;

        if (groupsListener != null) groupsListener.remove();
        if (joinedListener != null) joinedListener.remove();

        db.collection("groups").get().addOnSuccessListener(querySnapshot -> {
            if (querySnapshot != null && querySnapshot.isEmpty()) {
                List<StudyGroup> initial = new ArrayList<>();
                initial.add(new StudyGroup("Coding Elite", 45, 92,
                        "Focus hours for CS students and devs", "💻",
                        java.util.Arrays.asList("DSA", "OS", "Java", "Python", "ML"), 1280.0, 18, "Dec 01, 2025"));
                initial.add(new StudyGroup("Morning Birds", 8, 85,
                        "We focus early. 5AM - 8AM only.", "🌅",
                        java.util.Arrays.asList("Maths", "Physics"), 96.2, 4, "Mar 10, 2026"));
                initial.add(new StudyGroup("Library Legends", 12, 78,
                        "Daily study sessions at the campus library", "📚",
                        java.util.Arrays.asList("DCCN", "DAA", "DBMS"), 340.5, 5, "Jan 15, 2026"));

                for (StudyGroup g : initial) {
                    db.collection("groups").document(g.groupName.trim()).set(g);
                  
                    if (g.groupName.equals("Library Legends")) {
                        joinGroup(g);
                    }
                }
            }
        });

        groupsListener = db.collection("groups").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            allGroupsCache.clear();
            for (DocumentSnapshot doc : value.getDocuments()) {
                StudyGroup group = doc.toObject(StudyGroup.class);
                if (group != null) {
                    allGroupsCache.add(group);
                }
            }
            updateLiveData();
        });

        joinedListener = db.collection("users").document(uid).collection("joined_communities")
                .addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            joinedNamesCache.clear();
            for (DocumentSnapshot doc : value.getDocuments()) {
                joinedNamesCache.add(doc.getId().trim());
            }
            updateLiveData();
        });
    }

    private void updateLiveData() {
        List<StudyGroup> joined = new ArrayList<>();
        List<StudyGroup> available = new ArrayList<>();

        for (StudyGroup g : allGroupsCache) {
            if (g.groupName != null && joinedNamesCache.contains(g.groupName.trim())) {
                joined.add(g);
            } else {
                available.add(g);
            }
        }

        joinedGroups.setValue(joined);
        availableGroups.setValue(available);
    }

    public void checkUserChanged() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null && !uid.equals(currentUid)) {
            loadGroups();
        }
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
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        String groupId = group.groupName.trim();

        Map<String, Object> data = new HashMap<>();
        data.put("joinedAt", FieldValue.serverTimestamp());

        db.collection("users").document(uid)
                .collection("joined_communities").document(groupId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    db.collection("groups").document(groupId)
                            .update("memberCount", FieldValue.increment(1));
                });
    }

    public void createGroup(StudyGroup group) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        String groupId = group.groupName.trim();

        com.google.firebase.firestore.WriteBatch batch = db.batch();

        batch.set(db.collection("groups").document(groupId), group);

        Map<String, Object> data = new HashMap<>();
        data.put("joinedAt", FieldValue.serverTimestamp());
        batch.set(db.collection("users").document(uid)
                .collection("joined_communities").document(groupId), data);

        batch.commit()
            .addOnFailureListener(e -> {
                android.util.Log.e("CommunityViewModel", "Failed to create group. Check Firestore Rules!", e);
            });
    }

    public void leaveGroup(StudyGroup group) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        String groupId = group.groupName.trim();

        db.collection("users").document(uid)
                .collection("joined_communities").document(groupId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("groups").document(groupId)
                            .update("memberCount", FieldValue.increment(-1));
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (groupsListener != null) {
            groupsListener.remove();
        }
        if (joinedListener != null) {
            joinedListener.remove();
        }
    }
}
