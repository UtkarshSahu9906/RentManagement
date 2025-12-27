package com.utkarsh.rentmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddRentableItemActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText etItemCode, etItemName, etRentAmount, etDescription, etQuantity;
    private TextInputLayout tilItemCode, tilItemName, tilRentAmount, tilDescription, tilQuantity;
    private RadioGroup rgRentType;
    private RadioButton rbDaily, rbHourly;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel, btnGenerateCode;
    private LinearProgressIndicator progressBar;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Variables
    private String shopId;
    private String itemCode;
    private boolean isEditMode = false;

    // Constants for code generation
    private static final String PREFIX = "RENT-";
    private static final int CODE_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rentable_item);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get shopId from intent
        shopId = "sp_123";    //getIntent().getStringExtra("SHOP_ID");
        itemCode = getIntent().getStringExtra("ITEM_CODE");

        if (itemCode != null && !itemCode.isEmpty()) {
            isEditMode = true;
            setTitle("Edit Rentable Item");
        } else {
            setTitle("Add New Rentable Item");
        }

        initViews();
        setupSpinner();
        setupListeners();

        if (!isEditMode) {
            generateAndSetItemCode();
        } else {
            loadItemData();
        }
    }

    private void initViews() {
        // TextInputLayouts
        tilItemCode = findViewById(R.id.tilItemCode);
        tilItemName = findViewById(R.id.tilItemName);
        tilRentAmount = findViewById(R.id.tilRentAmount);
        tilQuantity = findViewById(R.id.tilQuantity);
        tilDescription = findViewById(R.id.tilDescription);

        // EditTexts
        etItemCode = findViewById(R.id.etItemCode);
        etItemName = findViewById(R.id.etItemName);
        etRentAmount = findViewById(R.id.etRentAmount);
        etQuantity = findViewById(R.id.etQuantity);
        etDescription = findViewById(R.id.etDescription);

        // Radio Group
        rgRentType = findViewById(R.id.rgRentType);
        rbDaily = findViewById(R.id.rbDaily);
        rbHourly = findViewById(R.id.rbHourly);

        // Spinner
        spinnerCategory = findViewById(R.id.spinnerCategory);

        // Buttons
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnGenerateCode = findViewById(R.id.btnGenerateCode);

        // Progress Bar
        progressBar = findViewById(R.id.progressBar);

        // Disable item code editing for new items
        if (!isEditMode) {
            etItemCode.setEnabled(false);
            etItemCode.setFocusable(false);
            etItemCode.setFocusableInTouchMode(false);
        }
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.rentable_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveRentableItem());
        btnCancel.setOnClickListener(v -> finish());

        btnGenerateCode.setOnClickListener(v -> generateAndSetItemCode());

        rgRentType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbDaily) {
                tilRentAmount.setHint("Daily Rent (₹) *");
            } else if (checkedId == R.id.rbHourly) {
                tilRentAmount.setHint("Hourly Rent (₹) *");
            }
        });
    }

    private void generateAndSetItemCode() {
        progressBar.setVisibility(View.VISIBLE);
        btnGenerateCode.setEnabled(false);

        generateUniqueItemCode(new ItemCodeCallback() {
            @Override
            public void onCodeGenerated(String code) {
                runOnUiThread(() -> {
                    etItemCode.setText(code);
                    progressBar.setVisibility(View.GONE);
                    btnGenerateCode.setEnabled(true);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AddRentableItemActivity.this,
                            "Error: " + error,
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnGenerateCode.setEnabled(true);
                });
            }
        });
    }

    interface ItemCodeCallback {
        void onCodeGenerated(String code);
        void onError(String error);
    }

    private void generateUniqueItemCode(ItemCodeCallback callback) {
        String generatedCode = generateRandomCode();

        db.collection("shops").document(shopId)
                .collection("rentableItems").document(generatedCode)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            generateUniqueItemCode(callback);
                        } else {
                            callback.onCodeGenerated(generatedCode);
                        }
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    private String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder(PREFIX);

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }

        return code.toString();
    }

    private boolean validateForm() {
        boolean isValid = true;

        tilItemCode.setError(null);
        tilItemName.setError(null);
        tilRentAmount.setError(null);
        tilQuantity.setError(null);

        // Item Code validation
        String code = etItemCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            tilItemCode.setError("Generate a code first");
            isValid = false;
        }

        // Rentable Item Name validation
        String name = etItemName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            tilItemName.setError("Item name is required");
            isValid = false;
        }

        // Rent Amount validation
        String rentStr = etRentAmount.getText().toString().trim();
        if (TextUtils.isEmpty(rentStr)) {
            tilRentAmount.setError("Rent amount is required");
            isValid = false;
        } else {
            try {
                double rent = Double.parseDouble(rentStr);
                if (rent <= 0) {
                    tilRentAmount.setError("Must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilRentAmount.setError("Invalid amount");
                isValid = false;
            }
        }

        // Quantity validation
        String qtyStr = etQuantity.getText().toString().trim();
        if (TextUtils.isEmpty(qtyStr)) {
            tilQuantity.setError("Quantity is required");
            isValid = false;
        } else {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty < 1) {
                    tilQuantity.setError("Minimum 1 required");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilQuantity.setError("Invalid quantity");
                isValid = false;
            }
        }

        return isValid;
    }

    private void saveRentableItem() {
        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Get values
        String itemCode = etItemCode.getText().toString().trim();
        String itemName = etItemName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        boolean rentCalculateDayWise = rbDaily.isChecked();
        int quantity = Integer.parseInt(etQuantity.getText().toString().trim());
        double rentAmount = Double.parseDouble(etRentAmount.getText().toString().trim());
        String createdBy = mAuth.getCurrentUser().getUid();

        // Create data map
        Map<String, Object> rentableItem = new HashMap<>();
        rentableItem.put("itemCode", itemCode);
        rentableItem.put("itemName", itemName);
        rentableItem.put("description", description);
        rentableItem.put("category", category);
        rentableItem.put("rentCalculateDayWise", rentCalculateDayWise);
        rentableItem.put("quantity", quantity);
        rentableItem.put("rentAmount", rentAmount);
        rentableItem.put("createdBy", createdBy);
        rentableItem.put("updatedAt", System.currentTimeMillis());

        if (!isEditMode) {
            rentableItem.put("createdAt", System.currentTimeMillis());
        }

        // Save to Firestore
        DocumentReference docRef;
        if (isEditMode) {
            docRef = db.collection("shops").document(shopId)
                    .collection("rentableItems").document(this.itemCode);
        } else {
            docRef = db.collection("shops").document(shopId)
                    .collection("rentableItems").document(itemCode);
        }

        docRef.set(rentableItem)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            isEditMode ? "Rentable item updated!" : "Rentable item added!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadItemData() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("shops").document(shopId)
                .collection("rentableItems").document(itemCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        etItemCode.setText(documentSnapshot.getString("itemCode"));
                        etItemCode.setEnabled(false);

                        etItemName.setText(documentSnapshot.getString("itemName"));
                        etDescription.setText(documentSnapshot.getString("description"));

                        Long qty = documentSnapshot.getLong("quantity");
                        if (qty != null) etQuantity.setText(String.valueOf(qty));

                        Double rent = documentSnapshot.getDouble("rentAmount");
                        if (rent != null) etRentAmount.setText(String.valueOf(rent));

                        Boolean isDaily = documentSnapshot.getBoolean("rentCalculateDayWise");
                        if (isDaily != null) {
                            if (isDaily) rbDaily.setChecked(true);
                            else rbHourly.setChecked(true);
                        }

                        String category = documentSnapshot.getString("category");
                        if (category != null) {
                            ArrayAdapter adapter = (ArrayAdapter) spinnerCategory.getAdapter();
                            int pos = adapter.getPosition(category);
                            if (pos >= 0) spinnerCategory.setSelection(pos);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}