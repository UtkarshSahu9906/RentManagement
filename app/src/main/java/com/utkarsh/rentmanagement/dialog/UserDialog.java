package com.utkarsh.rentmanagement.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.utkarsh.rentmanagement.R;
import com.utkarsh.rentmanagement.model.user;
import com.utkarsh.rentmanagement.utils.FirebaseHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDialog {

    private Dialog dialog;
    private Context context;
    private UserDialogListener listener;
    private user existingUser;
    private Uri selectedImageUri;
    private ImageView ivProfile;
    private Button btnSave;
    private FirebaseHelper firebaseHelper;

    // Request codes for ImagePicker
    private static final int REQUEST_IMAGE_GALLERY = 1001;
    private static final int REQUEST_IMAGE_CAMERA = 1002;

    public interface UserDialogListener {
        void onUserSaved(user user);
        void onUserUpdated(user user);
        void onUserSaveFailed(String error);
    }

    public UserDialog(Context context, UserDialogListener listener) {
        this.context = context;
        this.listener = listener;
        this.firebaseHelper = new FirebaseHelper();
        createDialog();
    }

    public UserDialog(Context context, user existingUser, UserDialogListener listener) {
        this.context = context;
        this.existingUser = existingUser;
        this.listener = listener;
        this.firebaseHelper = new FirebaseHelper();
        createDialog();
    }

    private void createDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_user);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        initializeViews();

        if (existingUser != null) {
            populateUserData();
        }
    }

    private void initializeViews() {
        TextInputEditText etUserName = dialog.findViewById(R.id.etUserName);
        TextInputEditText etUserId = dialog.findViewById(R.id.etUserId);
        TextInputEditText etAddress = dialog.findViewById(R.id.etAddress);
        TextInputEditText etMobile = dialog.findViewById(R.id.etMobile);
        TextInputEditText etAadhaar = dialog.findViewById(R.id.etAadhaar);
        RadioGroup rgPaymentStatus = dialog.findViewById(R.id.rgPaymentStatus);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        btnSave = dialog.findViewById(R.id.btnSave);

        ivProfile = dialog.findViewById(R.id.ivProfile);
        Button btnGallery = dialog.findViewById(R.id.btnGallery);
        Button btnCamera = dialog.findViewById(R.id.btnCamera);

        if (existingUser != null) {
            btnSave.setText("Update User");
        }

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSourceDialog();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    saveUserToFirebase();
                }
            }
        });
    }

    private void showImageSourceDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Choose Image Source");
        builder.setItems(new CharSequence[]{"Gallery", "Camera"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    openGallery();
                    break;
                case 1:
                    openCamera();
                    break;
            }
        });
        builder.show();
    }

    private void openGallery() {
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            ImagePicker.with(activity)
                    .galleryOnly()
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(REQUEST_IMAGE_GALLERY);
        }
    }

    private void openCamera() {
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            ImagePicker.with(activity)
                    .cameraOnly()
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(REQUEST_IMAGE_CAMERA);
        }
    }

    // Call this method from your Activity's onActivityResult
    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    handleImageSelection(uri);
                }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            String error = ImagePicker.getError(data);
            Toast.makeText(context, "Image selection failed: " + error, Toast.LENGTH_SHORT).show();
        } else {
            // User cancelled the image picker
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageSelection(Uri imageUri) {
        selectedImageUri = imageUri;
        Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile)
                .centerCrop()
                .into(ivProfile);

        Toast.makeText(context, "Image selected", Toast.LENGTH_SHORT).show();
    }

    private void populateUserData() {
        TextInputEditText etUserName = dialog.findViewById(R.id.etUserName);
        TextInputEditText etUserId = dialog.findViewById(R.id.etUserId);
        TextInputEditText etAddress = dialog.findViewById(R.id.etAddress);
        TextInputEditText etMobile = dialog.findViewById(R.id.etMobile);
        TextInputEditText etAadhaar = dialog.findViewById(R.id.etAadhaar);
        RadioGroup rgPaymentStatus = dialog.findViewById(R.id.rgPaymentStatus);
        RadioButton rbPaid = dialog.findViewById(R.id.rbPaid);
        RadioButton rbPending = dialog.findViewById(R.id.rbPending);

        etUserName.setText(existingUser.getName());
        etUserId.setText(existingUser.getUid());
        etUserId.setEnabled(false);
        etAddress.setText(existingUser.getAddress());
        etMobile.setText(""+existingUser.getMobileNo()); // Changed from String.valueOf()
        etMobile.setEnabled(false);
        etAadhaar.setText(String.valueOf(existingUser.getAadhaarNo()));

        if (!TextUtils.isEmpty(existingUser.getImgUrl()) && !existingUser.getImgUrl().equals("default")) {
            try {
                Glide.with(context)
                        .load(existingUser.getImgUrl())
                        .placeholder(R.drawable.ic_default_profile)
                        .error(R.drawable.ic_default_profile)
                        .centerCrop()
                        .into(ivProfile);
            } catch (Exception e) {
                // Keep default image
            }
        }

        if (existingUser.isPaid()) {
            rbPaid.setChecked(true);
        } else {
            rbPending.setChecked(true);
        }
    }

    private boolean validateInputs() {
        TextInputEditText etUserName = dialog.findViewById(R.id.etUserName);
        TextInputEditText etUserId = dialog.findViewById(R.id.etUserId);
        TextInputEditText etAddress = dialog.findViewById(R.id.etAddress);
        TextInputEditText etMobile = dialog.findViewById(R.id.etMobile);
        TextInputEditText etAadhaar = dialog.findViewById(R.id.etAadhaar);

        String name = etUserName.getText().toString().trim();
        String userId = etUserId.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String mobileStr = etMobile.getText().toString().trim();
        String aadhaarStr = etAadhaar.getText().toString().trim();

        // Clear previous errors
        etUserName.setError(null);
        etUserId.setError(null);
        etAddress.setError(null);
        etMobile.setError(null);
        etAadhaar.setError(null);

        if (TextUtils.isEmpty(name)) {
            etUserName.setError("Name is required");
            etUserName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(userId)) {
            etUserId.setError("User ID is required");
            etUserId.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mobileStr)) {
            etMobile.setError("Mobile number is required");
            etMobile.requestFocus();
            return false;
        }

        if (mobileStr.length() != 10) {
            etMobile.setError("Mobile number must be 10 digits");
            etMobile.requestFocus();
            return false;
        }

        if (!mobileStr.matches("[6-9][0-9]{9}")) {
            etMobile.setError("Enter a valid mobile number");
            etMobile.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(aadhaarStr)) {
            etAadhaar.setError("Aadhaar number is required");
            etAadhaar.requestFocus();
            return false;
        }

        if (aadhaarStr.length() != 12) {
            etAadhaar.setError("Aadhaar number must be 12 digits");
            etAadhaar.requestFocus();
            return false;
        }

        return true;
    }

    private void saveUserToFirebase() {
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        TextInputEditText etUserName = dialog.findViewById(R.id.etUserName);
        TextInputEditText etUserId = dialog.findViewById(R.id.etUserId);
        TextInputEditText etAddress = dialog.findViewById(R.id.etAddress);
        TextInputEditText etMobile = dialog.findViewById(R.id.etMobile);
        TextInputEditText etAadhaar = dialog.findViewById(R.id.etAadhaar);
        RadioGroup rgPaymentStatus = dialog.findViewById(R.id.rgPaymentStatus);
        RadioButton rbPaid = dialog.findViewById(R.id.rbPaid);

        String name = etUserName.getText().toString().trim();
        String userId = etUserId.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        long mobileNo = Long.parseLong(etMobile.getText().toString().trim()); // Keep as String
        long aadhaarNo;

        try {
            aadhaarNo = Long.parseLong(etAadhaar.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Invalid Aadhaar number", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            btnSave.setText(existingUser != null ? "Update User" : "Save User");
            return;
        }

        boolean isPaid = rbPaid.isChecked();

        String currentTime = getCurrentTimestamp();
        String createdAt = existingUser != null ? existingUser.getCreatedAt() : currentTime;
        String updatedAt = currentTime;

        // Create user object
        user user = new user(name, userId, address, mobileNo, aadhaarNo, isPaid, "https://avatar.iran.liara.run/public/1", createdAt, updatedAt);

        // Check if mobile number already exists (for new users)
        if (existingUser == null) {
            checkAndSaveUser(user);
        } else {
            // For existing users, proceed with update
            uploadImageAndSaveUser(user);
        }
    }

    private void checkAndSaveUser(user user) {
        // Convert mobileNo to long for checking
        long mobileLong;
        try {
            mobileLong = user.getMobileNo();
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Invalid mobile number", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            btnSave.setText("Save User");
            return;
        }

        firebaseHelper.checkMobileNumberExists(String.valueOf(mobileLong))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult()) {
                        // Mobile number already exists
                        btnSave.setEnabled(true);
                        btnSave.setText("Save User");
                        Toast.makeText(context, "Mobile number already registered", Toast.LENGTH_LONG).show();
                    } else {
                        // Mobile number doesn't exist, proceed with save
                        uploadImageAndSaveUser(user);
                    }
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save User");
                    Toast.makeText(context, "Error checking mobile number", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImageAndSaveUser(user user) {
        if (selectedImageUri != null) {
            // Upload image first
            firebaseHelper.uploadImageToFirebase(selectedImageUri, user.getUid())
                    .addOnSuccessListener(downloadUri -> {
                        // Update user with image URL
                        user.setImgUrl("https://avatar.iran.liara.run/public/1");
                        saveUserToFirestore(user);
                    })
                    .addOnFailureListener(e -> {
                        // If image upload fails, save user without image
                        user.setImgUrl("https://avatar.iran.liara.run/public/1");
                        saveUserToFirestore(user);
                    });
        } else {
            // No image selected, save user directly
            saveUserToFirestore(user);
        }
    }

    private void saveUserToFirestore(user user) {
        if (existingUser != null) {
            // Update existing user
            firebaseHelper.updateUserInFirestore(user)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) {
                            listener.onUserUpdated(user);
                        }
                        Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        handleSaveError("Failed to update user: " + e.getMessage());
                    });
        } else {
            // Save new user
            firebaseHelper.saveUserToFirestore(user)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) {
                            listener.onUserSaved(user);
                        }
                        Toast.makeText(context, "User saved successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        handleSaveError("Failed to save user: " + e.getMessage());
                    });
        }
    }

    private void handleSaveError(String error) {
        btnSave.setEnabled(true);
        btnSave.setText(existingUser != null ? "Update User" : "Save User");
        Toast.makeText(context, error, Toast.LENGTH_LONG).show();

        if (listener != null) {
            listener.onUserSaveFailed(error);
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}