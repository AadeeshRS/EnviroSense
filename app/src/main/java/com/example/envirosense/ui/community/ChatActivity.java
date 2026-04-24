package com.example.envirosense.ui.community;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.envirosense.R;
import com.example.envirosense.data.AppDatabase;
import com.example.envirosense.data.ChatMessageDao;
import com.example.envirosense.data.models.ChatMessage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_SETTINGS = 1001;

    private RecyclerView rvMessages;
    private EditText etInput;
    private FloatingActionButton btnSend;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    
    private String groupName;
    private AppDatabase db;
    private ChatMessageDao chatMessageDao;
    private ExecutorService executorService;

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
        String groupEmoji = getIntent().getStringExtra("GROUP_EMOJI");
        int avgScore = getIntent().getIntExtra("AVG_SCORE", 72);
        int activeMembers = getIntent().getIntExtra("ACTIVE_MEMBERS", 3);
        int sessionMembers = getIntent().getIntExtra("SESSION_MEMBERS", 1);
        int totalParticipants = getIntent().getIntExtra("TOTAL_PARTICIPANTS", 12);
        
        if (groupName == null) {
            groupName = "Unknown Group";
        }

        LinearLayout llGroupInfo = findViewById(R.id.ll_group_info_container);
        llGroupInfo.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, GroupChatSettingsActivity.class);
            intent.putExtra("GROUP_NAME", groupName);
            intent.putExtra("GROUP_EMOJI", groupEmoji);
            intent.putExtra("ACTIVE_MEMBERS", activeMembers);
            intent.putExtra("TOTAL_PARTICIPANTS", totalParticipants);
            startActivityForResult(intent, REQUEST_SETTINGS);
        });

        tvGroupName.setText(groupName);
        if (groupEmoji != null) {
            tvGroupEmoji.setText(groupEmoji);
        }

        ImageButton btnStartSession = findViewById(R.id.btn_start_session);
        btnStartSession.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, GroupSessionActivity.class);
            intent.putExtra("GROUP_NAME", groupName);
            intent.putExtra("GROUP_EMOJI", groupEmoji);
            intent.putExtra("AVG_SCORE", avgScore);
            intent.putExtra("ACTIVE_MEMBERS", activeMembers); // We'll pass sessionMembers as active session members in GroupSessionActivity
            intent.putExtra("SESSION_MEMBERS", sessionMembers);
            startActivity(intent);
        });

        rvMessages = findViewById(R.id.rv_chat_messages);
        etInput = findViewById(R.id.et_message_input);
        btnSend = findViewById(R.id.btn_send_message);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList, (message, position) -> {
            new AlertDialog.Builder(ChatActivity.this)
                    .setTitle("Delete Message")
                    .setMessage("Are you sure you want to delete this message?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        executorService.execute(() -> {
                            chatMessageDao.delete(message);
                            runOnUiThread(() -> {
                                messageList.remove(position);
                                adapter.notifyItemRemoved(position);
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
        
        db = AppDatabase.getInstance(this);
        chatMessageDao = db.chatMessageDao();
        executorService = Executors.newSingleThreadExecutor();

        loadMessages();

        btnSend.setOnClickListener(v -> {
            String text = etInput.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                String timestamp = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                ChatMessage newMessage = new ChatMessage(groupName, "Me", text, timestamp, true);
                
                etInput.setText("");
                
                executorService.execute(() -> {
                    chatMessageDao.insert(newMessage);
                    runOnUiThread(() -> {
                        messageList.add(newMessage);
                        adapter.notifyItemInserted(messageList.size() - 1);
                        scrollToBottom();
                    });
                });
            }
        });

        // Sync/Fetch from Firestore
        fetchAndSyncFromFirestore(groupName, totalParticipants, activeMembers, sessionMembers);
    }

    private void fetchAndSyncFromFirestore(String groupName, int defaultTotal, int defaultActive, int defaultSession) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("communities").document(groupName);
        
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long fTotal = documentSnapshot.getLong("totalMembers");
                Long fActive = documentSnapshot.getLong("activeMembers");
                Long fSession = documentSnapshot.getLong("sessionMembers");
                
                int total = fTotal != null ? fTotal.intValue() : defaultTotal;
                int active = fActive != null ? fActive.intValue() : defaultActive;
                int session = fSession != null ? fSession.intValue() : defaultSession;
                
                // Update local intents so they are passed to other activities correctly
                getIntent().putExtra("TOTAL_PARTICIPANTS", total);
                getIntent().putExtra("ACTIVE_MEMBERS", active);
                getIntent().putExtra("SESSION_MEMBERS", session);
            } else {
                // If it doesn't exist, we sync our default local data (which guarantees session < active <= total)
                Map<String, Object> data = new HashMap<>();
                data.put("totalMembers", defaultTotal);
                data.put("activeMembers", defaultActive);
                data.put("sessionMembers", defaultSession);
                docRef.set(data, SetOptions.merge());
            }
        });
    }

    private void loadMessages() {
        executorService.execute(() -> {
            List<ChatMessage> dbMessages = chatMessageDao.getMessagesForGroup(groupName);
            
            // If the database is empty for this group, let's load initial mock data
            if (dbMessages.isEmpty()) {
                dbMessages.add(new ChatMessage(groupName, "Alice Walker", "Hey everyone! Who is joining the study session tonight?", "10:00", false));
                dbMessages.add(new ChatMessage(groupName, "David Attenborough", "I will be there. We need to review chapter 4.", "10:05", false));
                dbMessages.add(new ChatMessage(groupName, "Jane Goodall", "Count me in. The library is usually quiet at 8 PM.", "10:15", false));
                dbMessages.add(new ChatMessage(groupName, "Me", "Sounds great! See you guys then.", "10:20", true));
                
                // Insert these mock messages into DB
                for (ChatMessage msg : dbMessages) {
                    chatMessageDao.insert(msg);
                }
            }
            
            final List<ChatMessage> finalDbMessages = dbMessages;
            runOnUiThread(() -> {
                messageList.clear();
                messageList.addAll(finalDbMessages);
                adapter.notifyDataSetChanged();
                scrollToBottom();
            });
        });
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            rvMessages.scrollToPosition(messageList.size() - 1);
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
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
