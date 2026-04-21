package com.example.envirosense.ui.community;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.envirosense.R;
import com.example.envirosense.data.AppDatabase;
import com.example.envirosense.data.ChatMessageDao;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupChatSettingsActivity extends AppCompatActivity {

    private String groupName;
    private ExecutorService executorService;
    private ChatMessageDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setTitle("Group Info");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        groupName = getIntent().getStringExtra("GROUP_NAME");
        String emoji = getIntent().getStringExtra("GROUP_EMOJI");

        if (groupName == null) groupName = "Unknown Group";

        TextView tvEmoji = findViewById(R.id.tv_settings_emoji);
        TextView tvName = findViewById(R.id.tv_settings_group_name);
        MaterialButton btnClearChat = findViewById(R.id.btn_clear_chat);

        tvName.setText(groupName);
        if (emoji != null) {
            tvEmoji.setText(emoji);
        }

        dao = AppDatabase.getInstance(this).chatMessageDao();
        executorService = Executors.newSingleThreadExecutor();

        btnClearChat.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Chat")
                    .setMessage("Are you sure you want to clear all messages in this group? This action cannot be undone.")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        executorService.execute(() -> {
                            dao.deleteMessagesForGroup(groupName);
                            runOnUiThread(() -> {
                                Toast.makeText(GroupChatSettingsActivity.this, "Chat cleared", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("CHAT_CLEARED", true);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
