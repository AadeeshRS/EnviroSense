package com.example.envirosense.ui.auth;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.envirosense.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextInputEditText etName, etAge;
    private AutoCompleteTextView acvGender, acvGoal, acvEnvironment;
    private MaterialButton btnSaveProfile;
    private TextView tvError;

    private String selectedGender = "";
    private String selectedGoal = "";
    private String selectedEnvironment = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etName = view.findViewById(R.id.et_name);
        etAge = view.findViewById(R.id.et_age);
        acvGender = view.findViewById(R.id.acv_gender);
        acvGoal = view.findViewById(R.id.acv_goal);
        acvEnvironment = view.findViewById(R.id.acv_environment);
        btnSaveProfile = view.findViewById(R.id.btn_save_profile);
        tvError = view.findViewById(R.id.tv_profile_error);

        String[] genders = {"Male", "Female", "Non-binary", "Prefer not to say"};
        String[] goals = {"Exam Prep", "Coding & Development", "Research & Reading", "General Focus", "Creative Work"};
        String[] environments = {"Quiet Library", "Home Desk", "Coffee Shop", "Outdoors", "Shared Workspace"};

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, genders);
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, goals);
        ArrayAdapter<String> envAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, environments);

        acvGender.setAdapter(genderAdapter);
        acvGoal.setAdapter(goalAdapter);
        acvEnvironment.setAdapter(envAdapter);

        acvGender.setOnItemClickListener((parent, v, position, id) -> {
            selectedGender = genders[position];
            acvGender.setText(selectedGender, false);
        });

        acvGoal.setOnItemClickListener((parent, v, position, id) -> {
            selectedGoal = goals[position];
            acvGoal.setText(selectedGoal, false);
        });

        acvEnvironment.setOnItemClickListener((parent, v, position, id) -> {
            selectedEnvironment = environments[position];
            acvEnvironment.setText(selectedEnvironment, false);
        });

        loadProfileData();

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        return view;
    }

    private void loadProfileData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && getContext() != null) {
                        String name = doc.getString("name");
                        Number age = (Number) doc.get("age");
                        String gender = doc.getString("gender");
                        String goal = doc.getString("studyGoal");
                        String env = doc.getString("preferredEnvironment");

                        if (name != null) etName.setText(name);
                        if (age != null) etAge.setText(String.valueOf(age.intValue()));
                        
                        if (gender != null) {
                            selectedGender = gender;
                            acvGender.setText(gender, false);
                        }
                        if (goal != null) {
                            selectedGoal = goal;
                            acvGoal.setText(goal, false);
                        }
                        if (env != null) {
                            selectedEnvironment = env;
                            acvEnvironment.setText(env, false);
                        }
                    }
                });
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

        tvError.setVisibility(View.GONE);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("age", age);
        updates.put("gender", gender);
        updates.put("studyGoal", goal);
        updates.put("preferredEnvironment", environment);

        requireContext().getSharedPreferences("EnviroSensePrefs", Context.MODE_PRIVATE)
                .edit()
                .putString("user_name", name)
                .apply();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .set(updates, SetOptions.merge());

        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        
        if (requireActivity() instanceof com.example.envirosense.MainActivity) {
            ((com.example.envirosense.MainActivity) requireActivity()).refreshNavHeader();
            ((com.example.envirosense.MainActivity) requireActivity()).navigateToHome();
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
