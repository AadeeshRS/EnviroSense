package com.example.envirosense.data.models;

import java.util.List;

/**
 * POJO for a community study group.
 * Backend team can later replace this with a Firestore-mapped model or API DTO.
 */
public class StudyGroup {
    public String groupName;
    public int memberCount;
    public int avgScore;
    public String description;
    public String emoji;
    public List<String> subjects;
    public double totalFocusHours;
    public int activeMembers;
    public String creationDate;

    public StudyGroup(String groupName, int memberCount, int avgScore, String description,
                      String emoji, List<String> subjects, double totalFocusHours,
                      int activeMembers, String creationDate) {
        this.groupName = groupName;
        this.memberCount = memberCount;
        this.avgScore = avgScore;
        this.description = description;
        this.emoji = emoji;
        this.subjects = subjects;
        this.totalFocusHours = totalFocusHours;
        this.activeMembers = activeMembers;
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudyGroup that = (StudyGroup) o;
        return groupName != null ? groupName.equals(that.groupName) : that.groupName == null;
    }

    @Override
    public int hashCode() {
        return groupName != null ? groupName.hashCode() : 0;
    }
}
