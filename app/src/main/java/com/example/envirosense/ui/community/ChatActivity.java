package com.example.envirosense.ui.community;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.models.ChatMessage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_SETTINGS = 1001;

    private RecyclerView rvMessages;
    private EditText etInput;
    private FloatingActionButton btnSend;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    
    private String groupName;
    private FirebaseFirestore db;
    private ListenerRegistration chatListener;
    private String currentUserName = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvGroupName = findViewById(R.id.tv_group_name_chat);
        TextView tvGroupEmoji = findViewById(R.id.tv_group_emoji_chat);

        groupName = getIntent().getStringExtra("GROUP_NAME");
        if (groupName != null) {
            groupName = groupName.trim();
        }
        String groupEmoji = getIntent().getStringExtra("GROUP_EMOJI");
        int avgScore = getIntent().getIntExtra("AVG_SCORE", 72);
        int activeMembers = getIntent().getIntExtra("ACTIVE_MEMBERS", 3);
        
        if (groupName == null) {
            groupName = "Unknown Group";
        }

        LinearLayout llGroupInfo = findViewById(R.id.ll_group_info_container);
        llGroupInfo.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, GroupChatSettingsActivity.class);
            intent.putExtra("GROUP_NAME", groupName);
            intent.putExtra("GROUP_EMOJI", groupEmoji != null ? groupEmoji : "");
            startActivityForResult(intent, REQUEST_SETTINGS);
        });

        tvGroupName.setText(groupName);
        if (groupEmoji != null) {
            tvGroupEmoji.setText(groupEmoji);
        }

        TextView btnStartSession = findViewById(R.id.btn_start_session);
        btnStartSession.setOnClickListener(v -> {
            handleSessionButtonClick(groupEmoji, avgScore, activeMembers);
        });
        updateSessionButton();

        rvMessages = findViewById(R.id.rv_chat_messages);
        etInput = findViewById(R.id.et_message_input);
        btnSend = findViewById(R.id.btn_send_message);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList, (message, position) -> {
            new AlertDialog.Builder(ChatActivity.this)
                    .setTitle("Delete Message")
                    .setMessage("Are you sure you want to delete this message?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (message.getMessageId() != null) {
                            db.collection("groups").document(groupName)
                                    .collection("messages").document(message.getMessageId())
                                    .delete();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
        
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    currentUserName = doc.getString("name");
                }
            });
        }

        loadMessages();

        btnSend.setOnClickListener(v -> {
            String text = etInput.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                String timestamp = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String senderId = currentUser != null ? currentUser.getUid() : "guest";

                java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                msgMap.put("groupName", groupName);
                msgMap.put("senderId", senderId);
                msgMap.put("senderName", currentUserName);
                msgMap.put("messageText", text);
                msgMap.put("timestamp", timestamp);
                msgMap.put("sentAt", FieldValue.serverTimestamp());

                etInput.setText("");
                
                db.collection("groups").document(groupName)
                        .collection("messages").add(msgMap);
            }
        });
    }

    private void loadMessages() {
        chatListener = db.collection("groups").document(groupName)
                .collection("messages")
                .orderBy("sentAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (chatListener != null) chatListener.remove();
                        chatListener = db.collection("groups").document(groupName)
                                .collection("messages")
                                .orderBy("timestamp", Query.Direction.ASCENDING)
                                .addSnapshotListener((val, err) -> {
                                    if (err != null) {
                                        if (chatListener != null) chatListener.remove();
                                        chatListener = db.collection("groups").document(groupName)
                                                .collection("messages")
                                                .addSnapshotListener((v, e) -> {
                                                    if (e != null || v == null) return;
                                                    updateMessageList(v);
                                                });
                                        return;
                                    }
                                    if (val == null) return;
                                    updateMessageList(val);
                                });
                        return;
                    }
                    if (value == null) return;
                    updateMessageList(value);
                });
    }

    private void updateMessageList(com.google.firebase.firestore.QuerySnapshot value) {
        messageList.clear();
        String currentUid = FirebaseAuth.getInstance().getUid();

        for (QueryDocumentSnapshot doc : value) {
            ChatMessage msg = doc.toObject(ChatMessage.class);
            if (msg != null) {
                msg.setMessageId(doc.getId());
                msg.setMe(msg.getSenderId() != null && msg.getSenderId().equals(currentUid));
                messageList.add(msg);
            }
        }
        adapter.notifyDataSetChanged();
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            rvMessages.scrollToPosition(messageList.size() - 1);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateSessionButton();
        setPresence(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setPresence(false);
    }

    /**
     * Adds or removes the current user from the real-time active_users sub-collection
     * under the group document. Other clients listening to this sub-collection will
     * see the count change in real time.
     */
    private void setPresence(boolean active) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || groupName == null) return;

        if (active) {
            Map<String, Object> data = new HashMap<>();
            data.put("joinedAt", FieldValue.serverTimestamp());
            db.collection("groups").document(groupName)
                    .collection("active_users").document(user.getUid())
                    .set(data);
            // Also increment activeMembers on the main document for the list views
            db.collection("groups").document(groupName)
                    .update("activeMembers", FieldValue.increment(1));
        } else {
            db.collection("groups").document(groupName)
                    .collection("active_users").document(user.getUid())
                    .delete();
            // Decrement activeMembers on the main document
            db.collection("groups").document(groupName)
                    .update("activeMembers", FieldValue.increment(-1));
        }
    }

    // ── Session button state ─────────────────────────────────────────────

    /**
     * Updates the session button appearance based on the current session state:
     *   • No session active        → green "+" icon (start a new session)
     *   • This group's session     → green "Return" text
     *   • Another group's session  → grey "Session" text (blocked)
     */
    private void updateSessionButton() {
        TextView btn = findViewById(R.id.btn_start_session);
        if (btn == null) return;

        boolean sessionActive = SharedFocusTracker.getInstance().isTracking();
        String activeGroup = getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE)
                .getString("ACTIVE_GROUP_NAME", null);

        if (sessionActive && groupName.equals(activeGroup)) {
            // This group has an active session → show "Return"
            btn.setText("Return");
            btn.setTextColor(getResources().getColor(R.color.primary_green, getTheme()));
            btn.setBackgroundResource(R.drawable.bg_session_button);
        } else if (sessionActive) {
            // A different group has an active session → show greyed-out
            btn.setText("Session");
            btn.setTextColor(0xFF6B6B6B);
            btn.setBackgroundResource(R.drawable.bg_session_button_grey);
        } else {
            // No session active → show default "+"
            btn.setText("＋");
            btn.setTextColor(getResources().getColor(R.color.primary_green, getTheme()));
            btn.setBackgroundResource(R.drawable.bg_session_button);
        }
    }

    private void handleSessionButtonClick(String groupEmoji, int avgScore, int activeMembers) {
        boolean sessionActive = SharedFocusTracker.getInstance().isTracking();
        String activeGroup = getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE)
                .getString("ACTIVE_GROUP_NAME", null);

        if (sessionActive && groupName.equals(activeGroup)) {
            // Return to the active session
            Intent intent = new Intent(this, GroupSessionActivity.class);
            intent.putExtra("GROUP_NAME", groupName);
            intent.putExtra("GROUP_EMOJI", groupEmoji);
            intent.putExtra("AVG_SCORE", avgScore);
            intent.putExtra("ACTIVE_MEMBERS", activeMembers);
            startActivity(intent);
        } else if (sessionActive) {
            // Blocked – another group's session is running
            Toast.makeText(this, "Please end the current session first", Toast.LENGTH_SHORT).show();
        } else {
            // Start a new session
            Intent intent = new Intent(this, GroupSessionActivity.class);
            intent.putExtra("GROUP_NAME", groupName);
            intent.putExtra("GROUP_EMOJI", groupEmoji);
            intent.putExtra("AVG_SCORE", avgScore);
            intent.putExtra("ACTIVE_MEMBERS", activeMembers);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("CHAT_CLEARED", false)) {
                messageList.clear();
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) {
            chatListener.remove();
        }
    }
}
