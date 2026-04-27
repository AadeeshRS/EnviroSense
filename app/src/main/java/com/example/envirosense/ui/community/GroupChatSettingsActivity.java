package com.example.envirosense.ui.community;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.envirosense.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class GroupChatSettingsActivity extends AppCompatActivity {

    private String groupName;
    private FirebaseFirestore db;
    private ListenerRegistration memberListener;
    private ListenerRegistration activeListener;

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

        db = FirebaseFirestore.getInstance();

        TextView tvEmoji = findViewById(R.id.tv_settings_emoji);
        TextView tvName = findViewById(R.id.tv_settings_group_name);
        TextView tvParticipants = findViewById(R.id.tv_settings_participants);
        TextView tvActiveStudents = findViewById(R.id.tv_settings_active_students);
        MaterialButton btnClearChat = findViewById(R.id.btn_clear_chat);

        tvName.setText(groupName);
        if (emoji != null) {
            tvEmoji.setText(emoji);
        }

        // ── Real-time member count from the group document ───────────────
        memberListener = db.collection("groups").document(groupName)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;
                    Long count = snapshot.getLong("memberCount");
                    tvParticipants.setText("Members: " + (count != null ? count : 0));
                });

        // ── Real-time active users from the active_users sub-collection ──
        activeListener = db.collection("groups").document(groupName)
                .collection("active_users")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    int activeCount = value.size();
                    tvActiveStudents.setText("Active Now: " + activeCount);
                });

        // Group Resources option
        LinearLayout optionResources = findViewById(R.id.option_group_resources);
        optionResources.setOnClickListener(v -> {
            Intent intent = new Intent(GroupChatSettingsActivity.this, GroupResourcesActivity.class);
            intent.putExtra("GROUP_NAME", groupName);
            startActivity(intent);
        });

        btnClearChat.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Chat")
                    .setMessage("Are you sure you want to clear all messages in this group? This action cannot be undone.")
                    .setPositiveButton("Clear", (dialog, which) -> {
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
        if (memberListener != null) memberListener.remove();
        if (activeListener != null) activeListener.remove();
    }
}
