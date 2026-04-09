package com.example.envirosense.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_sessions")
public class FocusSession {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp;
    public int finalScore;
    public long durationMs;
    public String location;

    // Constructor
    public FocusSession(long timestamp, int finalScore, long durationMs, String location) {
        this.timestamp = timestamp;
        this.finalScore = finalScore;
        this.durationMs = durationMs;
        this.location = location;
    }
}