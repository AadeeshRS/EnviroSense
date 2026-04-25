package com.example.envirosense.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.envirosense.MainActivity;
import com.example.envirosense.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    private TextInputEditText etName, etAge;
    private AutoCompleteTextView acvGender, acvGoal, acvEnvironment;
    private MaterialButton btnSaveProfile;
    private TextView tvError;

    private String selectedGender = "";
    private String selectedGoal = "";
    private String selectedEnvironment = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        etName = findViewById(R.id.et_name);
        etAge = findViewById(R.id.et_age);
        acvGender = findViewById(R.id.acv_gender);
        acvGoal = findViewById(R.id.acv_goal);
        acvEnvironment = findViewById(R.id.acv_environment);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        tvError = findViewById(R.id.tv_profile_error);

        String[] genders = {"Male", "Female", "Non-binary", "Prefer not to say"};
        String[] goals = {"Exam Prep", "Coding & Development", "Research & Reading", "General Focus", "Creative Work"};
        String[] environments = {"Quiet Library", "Home Desk", "Coffee Shop", "Outdoors", "Shared Workspace"};

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genders);
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, goals);
        ArrayAdapter<String> envAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, environments);

        acvGender.setAdapter(genderAdapter);
        acvGoal.setAdapter(goalAdapter);
        acvEnvironment.setAdapter(envAdapter);

        acvGender.setOnItemClickListener((parent, view, position, id) -> {
            selectedGender = genders[position];
            acvGender.setText(selectedGender, false);
        });

        acvGoal.setOnItemClickListener((parent, view, position, id) -> {
            selectedGoal = goals[position];
            acvGoal.setText(selectedGoal, false);
        });

        acvEnvironment.setOnItemClickListener((parent, view, position, id) -> {
            selectedEnvironment = environments[position];
            acvEnvironment.setText(selectedEnvironment, false);
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";

        String gender = selectedGender.isEmpty() ? acvGender.getText().toString().trim() : selectedGender;
        String goal = selectedGoal.isEmpty() ? acvGoal.getText().toString().trim() : selectedGoal;
        String environment = selectedEnvironment.isEmpty() ? acvEnvironment.getText().toString().trim() : selectedEnvironment;

        if (TextUtils.isEmpty(name)) {
            showError("Please enter your name.");
            return;
        }
        if (TextUtils.isEmpty(ageStr)) {
            showError("Please enter your age.");
            return;
        }
        if (TextUtils.isEmpty(gender)) {
            showError("Please select your gender.");
            return;
        }
        if (TextUtils.isEmpty(goal)) {
            showError("Please select your primary study goal.");
            return;
        }
        if (TextUtils.isEmpty(environment)) {
            showError("Please select your preferred environment.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            showError("Please enter a valid age.");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showError("Authentication error. Please log in again.");
            return;
        }

        btnSaveProfile.setEnabled(false);
        tvError.setVisibility(View.GONE);

        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("name", name);
        userDoc.put("age", age);
        userDoc.put("gender", gender);
        userDoc.put("studyGoal", goal);
        userDoc.put("preferredEnvironment", environment);
        userDoc.put("email", user.getEmail());
        userDoc.put("uid", user.getUid());
        userDoc.put("totalHours", 0.0);
        userDoc.put("averageScore", 0.0);
        userDoc.put("sessionCount", 0);

        getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE)
                .edit()
                .putString("user_name", name)
                .apply();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .set(userDoc);

        Toast.makeText(this, "Welcome, " + name + "! 🎉", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
