package com.example.envirosense.ui.community;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.envirosense.R;
import com.google.android.material.button.MaterialButton;

public class GroupSessionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_session);

        ImageView btnBack = findViewById(R.id.btn_back_session);
        btnBack.setOnClickListener(v -> finish());

        TextView tvEmoji = findViewById(R.id.tv_greeting);
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvScore = findViewById(R.id.tv_focus_score);
        TextView tvMembers = findViewById(R.id.tv_session_members_count);
        MaterialButton btnJoin = findViewById(R.id.btn_join_session);

        String groupName = getIntent().getStringExtra("GROUP_NAME");
        String groupEmoji = getIntent().getStringExtra("GROUP_EMOJI");
        int avgScore = getIntent().getIntExtra("AVG_SCORE", 72);
        int activeMembers = getIntent().getIntExtra("ACTIVE_MEMBERS", 3);
        int sessionMembers = getIntent().getIntExtra("SESSION_MEMBERS", 1);

        if (groupName != null) tvName.setText(groupName);
        if (groupEmoji != null) tvEmoji.setText(groupEmoji);
        tvScore.setText(String.valueOf(avgScore));
        tvMembers.setText(String.valueOf(sessionMembers));

        if (sessionMembers == 0) {
            btnJoin.setText("Start Session");
        } else {
            btnJoin.setText("Join Session");
        }

        btnJoin.setOnClickListener(v -> {
            String action = sessionMembers == 0 ? "Starting " : "Joining ";
            Toast.makeText(this, action + groupName + " session...", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
