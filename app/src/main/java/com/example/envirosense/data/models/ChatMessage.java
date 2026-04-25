package com.example.envirosense.data.models;

import com.google.firebase.firestore.Exclude;

public class ChatMessage {
    @Exclude
    public int id;
    
    @Exclude
    public String messageId;
    public String groupName;
    public String senderId;
    public String senderName;
    public String messageText;
    public String timestamp;
    public com.google.firebase.Timestamp sentAt;
    @Exclude
    public boolean isMe;

    public ChatMessage() {
    }

    public ChatMessage(String groupName, String senderId, String senderName, String messageText, String timestamp, boolean isMe) {
        this.groupName = groupName;
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.isMe = isMe;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public com.google.firebase.Timestamp getSentAt() { return sentAt; }
    public void setSentAt(com.google.firebase.Timestamp sentAt) { this.sentAt = sentAt; }

    public boolean isMe() { return isMe; }
    public void setMe(boolean me) { isMe = me; }
}
