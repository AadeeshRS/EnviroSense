package com.example.envirosense.data.models;

/**
 * POJO for a single leaderboard entry.
 * Backend team can later replace this with a Firestore-mapped model or API DTO.
 */
public class LeaderboardEntry {
    public int rank;
    public String username;
    public int focusScore;
    public double totalHours;
    public String initials;

    public LeaderboardEntry(int rank, String username, int focusScore, double totalHours) {
        this.rank = rank;
        this.username = username;
        this.focusScore = focusScore;
        this.totalHours = totalHours;
        // Derive initials from username
        if (username != null && !username.isEmpty()) {
            String[] parts = username.split(" ");
            if (parts.length >= 2) {
                this.initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else {
                this.initials = ("" + username.charAt(0)).toUpperCase();
            }
        } else {
            this.initials = "?";
        }
    }
}
