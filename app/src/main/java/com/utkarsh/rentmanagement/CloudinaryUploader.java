package com.utkarsh.rentmanagement;


import android.content.Context;
import android.net.Uri;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import java.util.Map;

public class CloudinaryUploader {

    public interface UploadListener {
        void onSuccess(String imageUrl); // URL IS HERE!
        void onError(String errorMessage);
        void onProgress(double progress);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadListener listener) {

        if (imageUri == null) {
            listener.onError("No image selected");
            return;
        }


        String requestId = com.cloudinary.android.MediaManager.get()
                .upload(imageUri)
                .option("folder", "my_app_uploads")
                .option("public_id", "img_" + System.currentTimeMillis())
                .option("overwrite", true)
                .callback(new UploadCallback() {

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // ✅ THIS IS WHERE YOU GET THE URL!
                        String imageUrl = (String) resultData.get("secure_url");
                        listener.onSuccess(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        listener.onError(error.getDescription());
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (double) bytes / totalBytes * 100;
                        listener.onProgress(progress);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Auto-retry on failure
                    }

                    @Override
                    public void onStart(String requestId) {
                        // Upload started
                    }
                })
                .dispatch();
    }
}