package com.example.envirosense.ui.community;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
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

public class ChatActivity extends AppCompatActivity {

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
        
        if (groupName == null) {
            groupName = "Unknown Group";
        }

        tvGroupName.setText(groupName);
        if (groupEmoji != null) {
            tvGroupEmoji.setText(groupEmoji);
        }

        rvMessages = findViewById(R.id.rv_chat_messages);
        etInput = findViewById(R.id.et_message_input);
        btnSend = findViewById(R.id.btn_send_message);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
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
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
