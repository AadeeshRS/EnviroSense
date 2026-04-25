package com.example.envirosense.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_sessions")
public class FocusSession {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String userId;
    public long timestamp;
    public int finalScore;
    public long durationMs;
    public String location;
    
   
    public double avgNoise;
    public float avgLight;
    public int peakScore;
    public int noiseSpikes;

  
    public FocusSession(String userId, long timestamp, int finalScore, long durationMs, String location, double avgNoise, float avgLight, int peakScore, int noiseSpikes) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.finalScore = finalScore;
        this.durationMs = durationMs;
        this.location = location;
        this.avgNoise = avgNoise;
        this.avgLight = avgLight;
        this.peakScore = peakScore;
        this.noiseSpikes = noiseSpikes;
    }
}