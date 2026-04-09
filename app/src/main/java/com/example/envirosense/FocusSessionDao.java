package com.example.envirosense;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FocusSessionDao {

    @Insert
    void insert(FocusSession session);

    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    List<FocusSession> getAllSessions();

    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC LIMIT 1")
    FocusSession getLastSession();
}