package com.utkarsh.rentmanagement.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.utkarsh.rentmanagement.model.Ownership;
import com.utkarsh.rentmanagement.model.Shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OwnershipDatabase {
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public OwnershipDatabase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Generate unique shop ID
    private String generateShopId() {
        return "shop_" + UUID.randomUUID().toString().substring(0, 8);
    }

    // 1. CREATE OR INITIALIZE OWNERSHIP FOR USER
    public void initializeUserOwnership(OnCompleteListener<Void> listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onComplete(false, "User not authenticated");
            return;
        }

        String uid = user.getUid();
        DocumentReference ownershipRef = db.collection("users")
                .document(uid)
                .collection("ownership")
                .document("shops");

        // Check if document exists
        ownershipRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().exists()) {
                    // Create new ownership document
                    Ownership ownership = new Ownership();
                    ownershipRef.set(ownership)
                            .addOnSuccessListener(aVoid ->
                                    listener.onComplete(true, "Ownership initialized"))
                            .addOnFailureListener(e ->
                                    listener.onComplete(false, e.getMessage()));
                } else {
                    listener.onComplete(true, "Ownership already exists");
                }
            } else {
                listener.onComplete(false, task.getException().getMessage());
            }
        });
    }

    // 2. ADD A NEW SHOP
    public void addShop(String shopName, String bgImgUrl, String logoUrl,
                        OnCompleteListener<String> listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onComplete(false, "User not authenticated");
            return;
        }

        String uid = user.getUid();
        String shopId = generateShopId();

        Shop newShop = new Shop(shopId, shopName, bgImgUrl, logoUrl);

        DocumentReference ownershipRef = db.collection("users")
                .document(uid)
                .collection("ownership")
                .document("shops");

        // Add shop to array using arrayUnion
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("shops", FieldValue.arrayUnion(newShop));

        ownershipRef.update(updateData)
                .addOnSuccessListener(aVoid ->
                        listener.onComplete(true, "Shop added successfully"))
                .addOnFailureListener(e -> {
                    // If document doesn't exist, create it with the shop
                    Ownership ownership = new Ownership();
                    ownership.getShops().add(newShop);
                    ownershipRef.set(ownership)
                            .addOnSuccessListener(aVoid2 ->
                                    listener.onComplete(true, "Shop added successfully"))
                            .addOnFailureListener(e2 ->
                                    listener.onComplete(false, e2.getMessage()));
                });
    }

    // 3. GET ALL SHOPS FOR CURRENT USER
    public void getUserShops(OnShopsFetchListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onFetchComplete(false, "User not authenticated", null);
            return;
        }

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .collection("ownership")
                .document("shops")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Ownership ownership = documentSnapshot.toObject(Ownership.class);
                        if (ownership != null && ownership.getShops() != null) {
                            listener.onFetchComplete(true, "Success", ownership.getShops());
                        } else {
                            listener.onFetchComplete(true, "No shops found", new ArrayList<>());
                        }
                    } else {
                        listener.onFetchComplete(true, "No ownership document", new ArrayList<>());
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFetchComplete(false, e.getMessage(), null));
    }

    // 4. UPDATE A SHOP
    public void updateShop(String shopId, String shopName, String bgImgUrl, String logoUrl,
                           OnCompleteListener<Void> listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onComplete(false, "User not authenticated");
            return;
        }

        String uid = user.getUid();

        // First get current shops
        getUserShops(new OnShopsFetchListener() {
            @Override
            public void onFetchComplete(boolean success, String message, List<Shop> shops) {
                if (!success) {
                    listener.onComplete(false, message);
                    return;
                }

                // Find and update the shop
                boolean found = false;
                for (Shop shop : shops) {
                    if (shop.getShopId().equals(shopId)) {
                        shop.setShopName(shopName);
                        shop.setBgImg(bgImgUrl);
                        shop.setLogo(logoUrl);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    listener.onComplete(false, "Shop not found");
                    return;
                }

                // Update the entire shops array
                DocumentReference ownershipRef = db.collection("users")
                        .document(uid)
                        .collection("ownership")
                        .document("shops");

                Map<String, Object> updateData = new HashMap<>();
                updateData.put("shops", shops);

                ownershipRef.set(updateData, SetOptions.merge())
                        .addOnSuccessListener(aVoid ->
                                listener.onComplete(true, "Shop updated successfully"))
                        .addOnFailureListener(e ->
                                listener.onComplete(false, e.getMessage()));
            }
        });
    }

    // 5. DELETE A SHOP
    public void deleteShop(String shopId, OnCompleteListener<Void> listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onComplete(false, "User not authenticated");
            return;
        }

        String uid = user.getUid();

        // Get current shops
        getUserShops(new OnShopsFetchListener() {
            @Override
            public void onFetchComplete(boolean success, String message, List<Shop> shops) {
                if (!success) {
                    listener.onComplete(false, message);
                    return;
                }

                // Remove the shop
                List<Shop> updatedShops = new ArrayList<>();
                for (Shop shop : shops) {
                    if (!shop.getShopId().equals(shopId)) {
                        updatedShops.add(shop);
                    }
                }

                // Update Firestore
                DocumentReference ownershipRef = db.collection("users")
                        .document(uid)
                        .collection("ownership")
                        .document("shops");

                Map<String, Object> updateData = new HashMap<>();
                updateData.put("shops", updatedShops);

                ownershipRef.set(updateData, SetOptions.merge())
                        .addOnSuccessListener(aVoid ->
                                listener.onComplete(true, "Shop deleted successfully"))
                        .addOnFailureListener(e ->
                                listener.onComplete(false, e.getMessage()));
            }
        });
    }

    // 6. CHECK IF USER OWNS A SHOP
    public void checkShopOwnership(String shopId, OnOwnershipCheckListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onCheckComplete(false, "User not authenticated", false);
            return;
        }

        getUserShops(new OnShopsFetchListener() {
            @Override
            public void onFetchComplete(boolean success, String message, List<Shop> shops) {
                if (!success) {
                    listener.onCheckComplete(false, message, false);
                    return;
                }

                boolean ownsShop = false;
                for (Shop shop : shops) {
                    if (shop.getShopId().equals(shopId)) {
                        ownsShop = true;
                        break;
                    }
                }

                listener.onCheckComplete(true, "Success", ownsShop);
            }
        });
    }

    // LISTENER INTERFACES
    public interface OnCompleteListener<T> {
        void onComplete(boolean success, String message);
    }

    public interface OnCompleteListenerWithId {
        void onComplete(boolean success, String message, String shopId);
    }

    public interface OnShopsFetchListener {
        void onFetchComplete(boolean success, String message, List<Shop> shops);
    }

    public interface OnOwnershipCheckListener {
        void onCheckComplete(boolean success, String message, boolean ownsShop);
    }
}
