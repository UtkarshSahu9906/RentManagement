package com.utkarsh.rentmanagement.utils;


import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.utkarsh.rentmanagement.model.Customer;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private static final String USERS_COLLECTION = "users";
    private static final String PROFILE_IMAGES_FOLDER = "profile_images";

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    public FirebaseHelper() {
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    // Upload image to Firebase Storage and get download URL
    public Task<Uri> uploadImageToFirebase(Uri imageUri, String userId) {
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(PROFILE_IMAGES_FOLDER + "/" + userId + "_" + System.currentTimeMillis() + ".jpg");

        UploadTask uploadTask = imageRef.putFile(imageUri);

        return uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return imageRef.getDownloadUrl();
        });
    }

    // Save Customer data to Firestore
    public Task<Void> saveUserToFirestore(Customer Customer) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", Customer.getName());
        userMap.put("uid", Customer.getUid());
        userMap.put("address", Customer.getAddress());
        userMap.put("mobileNo", Customer.getMobileNo());
        userMap.put("aadhaarNo", Customer.getAadhaarNo());
        userMap.put("paid", Customer.isPaid());
        userMap.put("imgUrl", Customer.getImgUrl());
        userMap.put("createdAt", Customer.getCreatedAt());
        userMap.put("updatedAt", Customer.getUpdatedAt());

        // Use mobile number as document ID
        String documentId = String.valueOf(Customer.getMobileNo());

        return firestore.collection(USERS_COLLECTION)
                .document(documentId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User saved successfully: " + Customer.getName());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving Customer: " + e.getMessage());
                });
    }

    // Update Customer data in Firestore
    public Task<Void> updateUserInFirestore(Customer Customer) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", Customer.getName());
        userMap.put("address", Customer.getAddress());
        userMap.put("aadhaarNo", Customer.getAadhaarNo());
        userMap.put("paid", Customer.isPaid());
        userMap.put("imgUrl", Customer.getImgUrl());
        userMap.put("updatedAt", Customer.getUpdatedAt());

        String documentId = String.valueOf(Customer.getMobileNo());

        return firestore.collection(USERS_COLLECTION)
                .document(documentId)
                .update(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User updated successfully: " + Customer.getName());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating Customer: " + e.getMessage());
                });
    }

    // Delete Customer from Firestore
    public Task<Void> deleteUserFromFirestore(String mobileNo) {
        String documentId = String.valueOf(mobileNo);
        return firestore.collection(USERS_COLLECTION)
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted successfully: " + mobileNo);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting Customer: " + e.getMessage());
                });
    }

    // Delete image from Firebase Storage
    public Task<Void> deleteImageFromFirebase(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals("default")) {
            return null;
        }

        // Extract the path from the download URL
        StorageReference storageRef = storage.getReferenceFromUrl(imageUrl);
        return storageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Image deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting image: " + e.getMessage());
                });
    }

    // Check if mobile number already exists
    public Task<Boolean> checkMobileNumberExists(String mobileNo) {
        String documentId = String.valueOf(mobileNo);
        return firestore.collection(USERS_COLLECTION)
                .document(documentId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().exists();
                    }
                    return false;
                });
    }
}