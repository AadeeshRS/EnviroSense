package com.example.envirosense.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FocusSessionDao {

    @Insert
    void insert(FocusSession session);

    @Query("SELECT * FROM focus_sessions WHERE userId = :userId ORDER BY timestamp DESC")
    List<FocusSession> getAllSessions(String userId);

    @Query("SELECT * FROM focus_sessions WHERE userId = :userId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    List<FocusSession> getSessionsInRange(String userId, long startTime, long endTime);

    @Query("SELECT * FROM focus_sessions WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    FocusSession getLastSession(String userId);

    @Query("SELECT location, COUNT(id) as sessionCount, AVG(finalScore) as avgScore FROM focus_sessions WHERE userId = :userId GROUP BY location ORDER BY sessionCount DESC")
    List<LocationStat> getLocationStats(String userId);

    @Query("SELECT location, COUNT(id) as sessionCount, AVG(finalScore) as avgScore FROM focus_sessions WHERE userId = :userId AND timestamp >= :startTime AND timestamp <= :endTime GROUP BY location ORDER BY sessionCount DESC")
    List<LocationStat> getLocationStatsInRange(String userId, long startTime, long endTime);

    @Query("DELETE FROM focus_sessions WHERE userId = :userId")
    void deleteAllSessions(String userId);

    @Delete
    void delete(FocusSession session);

    @Update
    void update(FocusSession session);
}