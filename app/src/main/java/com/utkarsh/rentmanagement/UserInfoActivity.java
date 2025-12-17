package com.utkarsh.rentmanagement;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UserInfoActivity extends AppCompatActivity {

    // UI Elements
    private TextInputEditText etUsername, etMobileNo, etAddress, etEmail;
    private TextInputLayout tilUsername, tilMobileNo, tilAddress, tilEmail;
    private MaterialButton btnSave;
    private ImageView btnBack;
    private ProgressBar progressBar;
    private TextView tvTitle, tvMessage;
    private ImageView ivStatusIcon;
    private LinearLayout llMessage;

    // Firestore
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Validation patterns
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Variables
    private String userId;
    private boolean isEditMode = false;

    // Colors
    private final int COLOR_PRIMARY = Color.parseColor("#4A6FA3");
    private final int COLOR_SUCCESS = Color.parseColor("#4CAF50");
    private final int COLOR_ERROR = Color.parseColor("#F44336");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initViews();

        // Get data from intent (if editing)
        getIntentData();

        // Setup click listeners
        setupClickListeners();

        // Setup text watchers for validation
        setupTextWatchers();

        // Auto-fill if user is logged in
        autoFillUserInfo();
    }

    private void initViews() {
        // Toolbar/Header
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);

        // TextInputLayouts
        tilUsername = findViewById(R.id.tilUsername);
        tilMobileNo = findViewById(R.id.tilMobileNo);
        tilAddress = findViewById(R.id.tilAddress);
        tilEmail = findViewById(R.id.tilEmail);

        // EditTexts
        etUsername = findViewById(R.id.etUsername);
        etMobileNo = findViewById(R.id.etMobileNo);
        etAddress = findViewById(R.id.etAddress);
        etEmail = findViewById(R.id.etEmail);

        // Buttons
        btnSave = findViewById(R.id.btnSave);

        // Status components
        progressBar = findViewById(R.id.progressBar);
        tvMessage = findViewById(R.id.tvMessage);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        llMessage = findViewById(R.id.llMessage);

        // Set default title
        tvTitle.setText("Add New User");
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("USER_ID");
            String username = intent.getStringExtra("USERNAME");
            String mobileNo = intent.getStringExtra("MOBILE_NO");
            String address = intent.getStringExtra("ADDRESS");
            String email = intent.getStringExtra("EMAIL");

            if (userId != null && !userId.isEmpty()) {
                // Edit mode
                isEditMode = true;
                tvTitle.setText("Edit User");
                btnSave.setText("UPDATE USER");

                // Pre-fill data
                if (username != null) etUsername.setText(username);
                if (mobileNo != null) etMobileNo.setText(mobileNo);
                if (address != null) etAddress.setText(address);
                if (email != null) etEmail.setText(email);
            }
        }
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Save button
        btnSave.setOnClickListener(v -> saveUserToFirestore());
    }

    private void setupTextWatchers() {
        // Clear errors when user starts typing
        etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) clearError(tilUsername);
        });

        etMobileNo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) clearError(tilMobileNo);
        });

        etAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) clearError(tilAddress);
        });

        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) clearError(tilEmail);
        });
    }

    private void autoFillUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !isEditMode) {
            // Auto-fill email if user is logged in and not in edit mode
            if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                etEmail.setText(currentUser.getEmail());
            }
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                etUsername.setText(currentUser.getDisplayName());
            }
        }
    }

    private void saveUserToFirestore() {
        // Get input values
        String username = etUsername.getText().toString().trim();
        String mobileNo = etMobileNo.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(username, mobileNo, address, email)) {
            return;
        }

        // Show loading
        showLoading(true);
        showStatus("Saving user information...", "loading");

        // Get or create user ID
        if (!isEditMode) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
            } else {
                userId = db.collection("users").document().getId();
            }
        }

        // Create user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("mobileNo", mobileNo);
        userData.put("address", address);
        userData.put("email", email);
        userData.put("userId", userId);
        userData.put("updatedAt", System.currentTimeMillis());

        if (!isEditMode) {
            userData.put("createdAt", System.currentTimeMillis());
        }

        // Save to Firestore
        db.collection("users")
                .document(userId)
                .set(userData)
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        showStatus(isEditMode ? "User updated successfully!" : "User saved successfully!", "success");
                        btnSave.setEnabled(false);

                        // Return result to calling activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("USER_ID", userId);
                        resultIntent.putExtra("USERNAME", username);
                        resultIntent.putExtra("MOBILE_NO", mobileNo);
                        resultIntent.putExtra("ADDRESS", address);
                        resultIntent.putExtra("EMAIL", email);
                        resultIntent.putExtra("IS_EDIT", isEditMode);
                        setResult(RESULT_OK, resultIntent);

                        // Auto-close after 1.5 seconds
                        new android.os.Handler().postDelayed(() -> {
                            finish();
                        }, 1500);

                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        showStatus("Failed to save: " + error, "error");
                    }
                });
    }

    private boolean validateInputs(String username, String mobileNo, String address, String email) {
        boolean isValid = true;

        // Username validation
        if (TextUtils.isEmpty(username)) {
            showError(tilUsername, "Username is required");
            isValid = false;
        } else if (username.length() < 3) {
            showError(tilUsername, "Minimum 3 characters required");
            isValid = false;
        }

        // Mobile number validation
        if (TextUtils.isEmpty(mobileNo)) {
            showError(tilMobileNo, "Mobile number is required");
            isValid = false;
        } else if (!MOBILE_PATTERN.matcher(mobileNo).matches()) {
            showError(tilMobileNo, "Enter valid 10-digit number");
            isValid = false;
        }

        // Address validation
        if (TextUtils.isEmpty(address)) {
            showError(tilAddress, "Address is required");
            isValid = false;
        } else if (address.length() < 10) {
            showError(tilAddress, "Enter complete address");
            isValid = false;
        }

        // Email validation (optional)
        if (!TextUtils.isEmpty(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            showError(tilEmail, "Enter valid email address");
            isValid = false;
        }

        return isValid;
    }

    private void showError(TextInputLayout textInputLayout, String error) {
        textInputLayout.setError(error);
        textInputLayout.setErrorEnabled(true);
    }

    private void clearError(TextInputLayout textInputLayout) {
        textInputLayout.setError(null);
        textInputLayout.setErrorEnabled(false);
    }

    private void showLoading(boolean show) {
        btnSave.setEnabled(!show);
        btnBack.setEnabled(!show);

        if (show) {
            btnSave.setText("SAVING...");
        } else {
            btnSave.setText(isEditMode ? "UPDATE USER" : "SAVE USER");
        }
    }

    private void showStatus(String message, String type) {
        llMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(message);

        switch (type) {
            case "loading":
                progressBar.setVisibility(View.VISIBLE);
                ivStatusIcon.setVisibility(View.GONE);
                llMessage.setBackgroundColor(Color.parseColor("#F8F9FA"));
                tvMessage.setTextColor(COLOR_PRIMARY);
                break;

            case "success":
                progressBar.setVisibility(View.GONE);
                ivStatusIcon.setVisibility(View.VISIBLE);
                ivStatusIcon.setImageResource(R.drawable.ic_success);
                llMessage.setBackgroundColor(Color.parseColor("#E8F5E9"));
                tvMessage.setTextColor(COLOR_SUCCESS);
                break;

            case "error":
                progressBar.setVisibility(View.GONE);
                ivStatusIcon.setVisibility(View.VISIBLE);
                ivStatusIcon.setImageResource(R.drawable.ic_error);
                llMessage.setBackgroundColor(Color.parseColor("#FFEBEE"));
                tvMessage.setTextColor(COLOR_ERROR);
                break;
        }
    }

    private void hideStatus() {
        llMessage.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        // Check if we should warn about unsaved changes
        if (hasUnsavedChanges()) {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("You have unsaved changes. Are you sure you want to exit?")
                    .setPositiveButton("Discard", (dialog, which) -> {
                        setResult(RESULT_CANCELED);
                        super.onBackPressed();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        // Check if any field has been modified
        Intent intent = getIntent();
        if (intent != null && isEditMode) {
            String originalUsername = intent.getStringExtra("USERNAME");
            String originalMobileNo = intent.getStringExtra("MOBILE_NO");
            String originalAddress = intent.getStringExtra("ADDRESS");
            String originalEmail = intent.getStringExtra("EMAIL");

            return !etUsername.getText().toString().equals(originalUsername) ||
                    !etMobileNo.getText().toString().equals(originalMobileNo) ||
                    !etAddress.getText().toString().equals(originalAddress) ||
                    !etEmail.getText().toString().equals(originalEmail);
        } else {
            return !TextUtils.isEmpty(etUsername.getText().toString().trim()) ||
                    !TextUtils.isEmpty(etMobileNo.getText().toString().trim()) ||
                    !TextUtils.isEmpty(etAddress.getText().toString().trim()) ||
                    !TextUtils.isEmpty(etEmail.getText().toString().trim());
        }
    }

    // Static method to start activity for adding new user
    public static void startForAddUser(android.content.Context context) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        context.startActivity(intent);
    }

    // Static method to start activity for editing user
    public static void startForEditUser(android.content.Context context, String userId,
                                        String username, String mobileNo,
                                        String address, String email) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USERNAME", username);
        intent.putExtra("MOBILE_NO", mobileNo);
        intent.putExtra("ADDRESS", address);
        intent.putExtra("EMAIL", email);
        context.startActivity(intent);
    }

    // Static method to start activity for result
    public static void startForResult(android.app.Activity activity, int requestCode) {
        Intent intent = new Intent(activity, UserInfoActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }
}