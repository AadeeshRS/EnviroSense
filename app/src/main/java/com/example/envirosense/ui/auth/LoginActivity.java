package com.example.envirosense.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.envirosense.MainActivity;
import com.example.envirosense.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private TextView tabLogin, tabSignup, tvError;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnEmailAction, btnGoogleSignIn;

    private boolean isLoginMode = true;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    showError("Google Sign-In failed. Please try again.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess(mAuth.getCurrentUser());
            return;
        }

        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        tabLogin = findViewById(R.id.tab_login);
        tabSignup = findViewById(R.id.tab_signup);
        tvError = findViewById(R.id.tv_error);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnEmailAction = findViewById(R.id.btn_email_action);
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);

        tabLogin.setOnClickListener(v -> setMode(true));
        tabSignup.setOnClickListener(v -> setMode(false));

        btnEmailAction.setOnClickListener(v -> handleEmailAction());
        btnGoogleSignIn.setOnClickListener(v -> launchGoogleSignIn());
    }

    private void setMode(boolean loginMode) {
        isLoginMode = loginMode;
        int greenColor = getResources().getColor(R.color.primary_green, getTheme());
        int transparentColor = android.graphics.Color.TRANSPARENT;
        int blackColor = getResources().getColor(R.color.black, getTheme());
        int secondaryColor = getResources().getColor(R.color.text_secondary, getTheme());

        if (isLoginMode) {
            tabLogin.setBackgroundResource(R.drawable.bg_card_rounded);
            tabLogin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(greenColor));
            tabLogin.setTextColor(blackColor);
            tabSignup.setBackgroundColor(transparentColor);
            tabSignup.setBackgroundTintList(null);
            tabSignup.setTextColor(secondaryColor);
            btnEmailAction.setText("Log In");
        } else {
            tabSignup.setBackgroundResource(R.drawable.bg_card_rounded);
            tabSignup.setBackgroundTintList(android.content.res.ColorStateList.valueOf(greenColor));
            tabSignup.setTextColor(blackColor);
            tabLogin.setBackgroundColor(transparentColor);
            tabLogin.setBackgroundTintList(null);
            tabLogin.setTextColor(secondaryColor);
            btnEmailAction.setText("Create Account");
        }
        tvError.setVisibility(View.GONE);
    }

    private void handleEmailAction() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("Please enter your email and password.");
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        btnEmailAction.setEnabled(false);
        tvError.setVisibility(View.GONE);

        if (isLoginMode) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnEmailAction.setEnabled(true);
                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            showError("Login failed. Check your credentials and try again.");
                        }
                    });
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnEmailAction.setEnabled(true);
                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            showError("Sign-up failed. This email may already be in use.");
                        }
                    });
        }
    }

    private void launchGoogleSignIn() {
        googleSignInClient.signOut().addOnCompleteListener(task ->
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent())
        );
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onAuthSuccess(task.getResult().getUser());
            } else {
                showError("Google authentication failed. Please try again.");
            }
        });
    }

    private void onAuthSuccess(FirebaseUser user) {
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getString("name") != null) {
                        goToMain();
                    } else {
                        goToProfileSetup();
                    }
                })
                .addOnFailureListener(e -> goToProfileSetup());
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void goToProfileSetup() {
        startActivity(new Intent(this, ProfileSetupActivity.class));
        finish();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
