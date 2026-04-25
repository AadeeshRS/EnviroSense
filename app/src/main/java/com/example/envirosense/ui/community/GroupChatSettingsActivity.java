package com.example.envirosense.ui.community;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.envirosense.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupChatSettingsActivity extends AppCompatActivity {

    private String groupName;

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
        int activeMembers = getIntent().getIntExtra("ACTIVE_MEMBERS", 0);
        int totalParticipants = getIntent().getIntExtra("TOTAL_PARTICIPANTS", 0);

        if (groupName == null) groupName = "Unknown Group";

        TextView tvEmoji = findViewById(R.id.tv_settings_emoji);
        TextView tvName = findViewById(R.id.tv_settings_group_name);
        TextView tvParticipants = findViewById(R.id.tv_settings_participants);
        TextView tvActiveStudents = findViewById(R.id.tv_settings_active_students);
        MaterialButton btnClearChat = findViewById(R.id.btn_clear_chat);

        tvName.setText(groupName);
        if (emoji != null) {
            tvEmoji.setText(emoji);
        }
        tvParticipants.setText("Members: " + totalParticipants);
        tvActiveStudents.setText("Active Now: " + activeMembers);

        btnClearChat.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Chat")
                    .setMessage("Are you sure you want to clear all messages in this group? This action cannot be undone.")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("groups").document(groupName)
                                .collection("messages")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (QueryDocumentSnapshot doc : querySnapshot) {
                                        doc.getReference().delete();
                                    }
                                    Toast.makeText(GroupChatSettingsActivity.this, "Chat cleared", Toast.LENGTH_SHORT).show();
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("CHAT_CLEARED", true);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
