package com.example.envirosense.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.envirosense.data.models.ChatMessage;

import java.util.List;

@Dao
public interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE groupName = :groupName ORDER BY id ASC")
    List<ChatMessage> getMessagesForGroup(String groupName);

    @Insert
    void insert(ChatMessage message);
}
