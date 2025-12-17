package com.utkarsh.rentmanagement.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.utkarsh.rentmanagement.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UserInfoDialog extends DialogFragment {

    // UI Elements
    private EditText etUsername, etMobileNo, etAddress, etEmail;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;
    private TextView tvMessage;

    // Firestore
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Validation patterns
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Listener interface
    public interface UserSaveListener {
        void onUserSaved(String userId, String username);
        void onUserSaveFailed(String error);
    }

    private UserSaveListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_user_info, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews(view);
        setupButtons();

        // Auto-fill if user is logged in
        autoFillUserInfo();

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.setTitle("Save User Information");
        }
        return dialog;
    }

    private void initViews(View view) {
        etUsername = view.findViewById(R.id.etUsername);
        etMobileNo = view.findViewById(R.id.etMobileNo);
        etAddress = view.findViewById(R.id.etAddress);
        etEmail = view.findViewById(R.id.etEmail);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        progressBar = view.findViewById(R.id.progressBar);
        tvMessage = view.findViewById(R.id.tvMessage);
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> saveUserToFirestore());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void autoFillUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Auto-fill email if user is logged in
            if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                etEmail.setText(currentUser.getEmail());
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
        clearMessage();

        // Get current user ID (use Firebase Auth or generate new)
        String userId;
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Use Firebase Auth user ID
            userId = currentUser.getUid();
        } else {
            // Generate a new ID (or you can create anonymous user)
            userId = db.collection("users").document().getId();
        }

        // Create user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("mobileNo", mobileNo);
        userData.put("address", address);
        userData.put("email", email);
        userData.put("userId", userId);
        userData.put("createdAt", System.currentTimeMillis());

        // Optional: Add image URL if you have one
        // userData.put("userImg", "default_image_url");

        // Save to Firestore
        db.collection("users")
                .document(userId)
                .set(userData)
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        showSuccess("User information saved successfully!");

                        // Notify listener
                        if (listener != null) {
                            listener.onUserSaved(userId, username);
                        }

                        // Auto-close after 2 seconds
                        new android.os.Handler().postDelayed(() -> {
                            if (isAdded() && getDialog() != null && getDialog().isShowing()) {
                                dismiss();
                            }
                        }, 2000);

                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        showError("Failed to save user: " + error);

                        if (listener != null) {
                            listener.onUserSaveFailed(error);
                        }
                    }
                });
    }

    private boolean validateInputs(String username, String mobileNo, String address, String email) {
        // Username validation
        if (TextUtils.isEmpty(username)) {
            showError("Username is required");
            etUsername.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return false;
        }

        // Mobile number validation
        if (TextUtils.isEmpty(mobileNo)) {
            showError("Mobile number is required");
            etMobileNo.requestFocus();
            return false;
        }

        if (!MOBILE_PATTERN.matcher(mobileNo).matches()) {
            showError("Please enter a valid 10-digit mobile number");
            etMobileNo.requestFocus();
            return false;
        }

        // Address validation
        if (TextUtils.isEmpty(address)) {
            showError("Address is required");
            etAddress.requestFocus();
            return false;
        }

        if (address.length() < 10) {
            showError("Please enter a complete address");
            etAddress.requestFocus();
            return false;
        }

        // Email validation (optional)
        if (!TextUtils.isEmpty(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        btnCancel.setEnabled(!show);
    }

    private void showError(String message) {
        tvMessage.setText(message);
        tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        tvMessage.setVisibility(View.VISIBLE);

        // Hide after 5 seconds
        new android.os.Handler().postDelayed(() -> {
            if (isAdded() && tvMessage != null) {
                tvMessage.setVisibility(View.GONE);
            }
        }, 5000);
    }

    private void showSuccess(String message) {
        tvMessage.setText(message);
        tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        tvMessage.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
    }

    private void clearMessage() {
        tvMessage.setVisibility(View.GONE);
    }

    // Setter for listener
    public void setUserSaveListener(UserSaveListener listener) {
        this.listener = listener;
    }

    // Static method to show dialog
    public static void showDialog(androidx.fragment.app.FragmentManager fragmentManager,
                                  UserSaveListener listener) {
        UserInfoDialog dialog = new UserInfoDialog();
        dialog.setUserSaveListener(listener);
        dialog.show(fragmentManager, "UserInfoDialog");
    }

    // Alternative method with pre-filled data
    public static void showDialogWithData(androidx.fragment.app.FragmentManager fragmentManager,
                                          String username, String mobileNo,
                                          String address, String email,
                                          UserSaveListener listener) {
        UserInfoDialog dialog = new UserInfoDialog();
        dialog.setUserSaveListener(listener);

        // Pass data via arguments
        Bundle args = new Bundle();
        args.putString("username", username);
        args.putString("mobileNo", mobileNo);
        args.putString("address", address);
        args.putString("email", email);
        dialog.setArguments(args);

        dialog.show(fragmentManager, "UserInfoDialog");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pre-fill data if arguments exist
        Bundle args = getArguments();
        if (args != null) {
            // This will be used in onViewCreated
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Pre-fill data from arguments
        Bundle args = getArguments();
        if (args != null) {
            String username = args.getString("username", "");
            String mobileNo = args.getString("mobileNo", "");
            String address = args.getString("address", "");
            String email = args.getString("email", "");

            if (!username.isEmpty()) etUsername.setText(username);
            if (!mobileNo.isEmpty()) etMobileNo.setText(mobileNo);
            if (!address.isEmpty()) etAddress.setText(address);
            if (!email.isEmpty()) etEmail.setText(email);
        }
    }
}