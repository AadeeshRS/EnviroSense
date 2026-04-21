package com.example.envirosense.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String groupName;
    private String senderName;
    private String messageText;
    private String timestamp;
    private boolean isMe;

    public ChatMessage() {
    }

    public ChatMessage(String groupName, String senderName, String messageText, String timestamp, boolean isMe) {
        this.groupName = groupName;
        this.senderName = senderName;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.isMe = isMe;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }
}
