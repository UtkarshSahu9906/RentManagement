package com.utkarsh.rentmanagement;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;

public class ShopRegistrationActivity extends AppCompatActivity {

    private static final int IMAGE_PICKER_REQUEST = 100;

    private Button btnSelectImage, btnUpload, btnCopyUrl;
    private ImageView imgPreview;
    private ProgressBar progressBar;
    private TextView tvStatus, tvImageUrl;
    private Uri selectedImageUri;
    private String uploadedImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_registeration);

        // Initialize views
        initViews();

        // Set click listeners
        btnSelectImage.setOnClickListener(v -> openImagePicker());
        btnUpload.setOnClickListener(v -> uploadImage());
        btnCopyUrl.setOnClickListener(v -> copyUrlToClipboard());
    }

    private void initViews() {
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);
        btnCopyUrl = findViewById(R.id.btnCopyUrl);
        imgPreview = findViewById(R.id.imgPreview);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        tvImageUrl = findViewById(R.id.tvImageUrl);

        btnUpload.setEnabled(false);
        btnCopyUrl.setEnabled(false);
    }

    private void openImagePicker() {
        ImagePicker.with(this)
                .crop()                    // Crop image
                .compress(1024)           // Max size 1MB
                .maxResultSize(1080, 1080)
                .start(IMAGE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Display selected image
                Glide.with(this)
                        .load(selectedImageUri)
                        .into(imgPreview);

                btnUpload.setEnabled(true);
                tvStatus.setText("Image selected. Ready to upload!");
                tvImageUrl.setText("");
                btnCopyUrl.setEnabled(false);
            }
        }
    }

    private void uploadImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);
        tvStatus.setText("Uploading to Cloudinary...");

        // Upload image
        CloudinaryUploader.uploadImage(this, selectedImageUri,
                new CloudinaryUploader.UploadListener() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        // ✅ URL RECEIVED HERE!
                        uploadedImageUrl = imageUrl;

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            tvStatus.setText("✅ Upload Successful!");

                            // Display the Cloudinary URL
                            tvImageUrl.setText("URL: " + imageUrl);

                            // Load uploaded image from Cloudinary URL
                            Glide.with(ShopRegistrationActivity.this)
                                    .load(imageUrl)
                                    .into(imgPreview);

                            // Enable copy button
                            btnCopyUrl.setEnabled(true);

                            Toast.makeText(ShopRegistrationActivity.this,
                                    "Image uploaded! URL is ready",
                                    Toast.LENGTH_LONG).show();

                            // Log the URL
                            System.out.println("CLOUDINARY IMAGE URL: " + imageUrl);
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnUpload.setEnabled(true);
                            tvStatus.setText("❌ Upload failed: " + errorMessage);
                            Toast.makeText(ShopRegistrationActivity.this,
                                    "Upload failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onProgress(double progress) {
                        runOnUiThread(() -> {
                            tvStatus.setText("Uploading: " + (int)progress + "%");
                        });
                    }
                });
    }

    private void copyUrlToClipboard() {
        if (uploadedImageUrl != null && !uploadedImageUrl.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Cloudinary URL", uploadedImageUrl);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "URL copied to clipboard!", Toast.LENGTH_SHORT).show();
        }
    }
}