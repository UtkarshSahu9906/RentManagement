package com.utkarsh.rentmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.utkarsh.rentmanagement.adapter.CustomerAdapter;
import com.utkarsh.rentmanagement.dialog.CustomerDialog;
import com.utkarsh.rentmanagement.model.Customer;
import com.utkarsh.rentmanagement.utils.AuthUtils;
import com.utkarsh.rentmanagement.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class SearchListActivity extends AppCompatActivity implements CustomerDialog.UserDialogListener {

    private RecyclerView recyclerView;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvEmpty;
    private FloatingActionButton fabAddUser;
    private CustomerAdapter adapter;
    private List<Customer> customerList;
    private FirebaseHelper firebaseHelper;
    private CustomerDialog customerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_list);

        // Check authentication
        if (!AuthUtils.isUserLoggedIn()) {
            AuthUtils.requireAuthentication(this);
            return;
        }

        firebaseHelper = new FirebaseHelper();
        initializeViews();
        setupRecyclerView();
        loadUsersFromFirebase(); // Changed from loadSampleData()
        setupSearchFunctionality();
        setupFloatingActionButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle image picker results
        if (requestCode == 1001 || requestCode == 1002) { // ImagePicker request codes
            if (customerDialog != null) {
                customerDialog.handleActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAddUser = findViewById(R.id.fabAddUser); // Make sure this ID exists in your layout
    }

    private void setupRecyclerView() {
        customerList = new ArrayList<>();
        adapter = new CustomerAdapter(customerList, new CustomerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Customer item) {
                onUserClicked(item);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Show/hide empty state
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmptyState();
            }
        });
    }

    private void loadUsersFromFirebase() {
        // Show loading
        tvEmpty.setText("Loading users...");
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    customerList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Customer Customer = document.toObject(Customer.class);
                            customerList.add(Customer);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error loading Customer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    adapter.updateData(customerList);
                    checkEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    checkEmptyState();
                });
    }

    private void setupSearchFunctionality() {
        // Search button click
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = etSearch.getText().toString().trim();
                performSearch(query);
            }
        });

        // Real-time search as Customer types
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Perform real-time search
                String query = s.toString().trim();
                if (query.length() >= 2 || query.isEmpty()) {
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        // Clear search when empty and focus lost
        etSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && etSearch.getText().toString().isEmpty()) {
                    adapter.filter("");
                }
            }
        });
    }

    private void setupFloatingActionButton() {
        fabAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });
    }

    private void showAddUserDialog() {
        customerDialog = new CustomerDialog(this, this);
        customerDialog.show();
    }

    private void showEditUserDialog(Customer Customer) {
        customerDialog = new CustomerDialog(this, Customer, this);
        customerDialog.show();
    }

    private void performSearch(String query) {
        if (adapter != null) {
            adapter.filter(query);
        }
    }

    private void checkEmptyState() {
        if (adapter == null || adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);

            // Update empty text based on search
            String searchQuery = etSearch.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                tvEmpty.setText("No users found for: " + searchQuery);
            } else {
                tvEmpty.setText("No users available");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void onUserClicked(Customer Customer) {
        // Show options: Edit or View Details
        showUserOptionsDialog(Customer);
    }

    private void showUserOptionsDialog(final Customer Customer) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("User Options");
        builder.setMessage("Choose an action for " + Customer.getName());

        builder.setPositiveButton("Edit", (dialog, which) -> {
            showEditUserDialog(Customer);
        });

        builder.setNegativeButton("View Details", (dialog, which) -> {
            showUserDetails(Customer);
        });

        builder.setNeutralButton("Delete", (dialog, which) -> {
            showDeleteConfirmation(Customer);
        });

        builder.show();
    }

    private void showDeleteConfirmation(Customer Customer) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Delete User");
        builder.setMessage("Are you sure you want to delete " + Customer.getName() + "? This action cannot be undone.");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteUserFromFirebase(Customer);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteUserFromFirebase(Customer Customer) {
        // Delete image from storage first
        if (!Customer.getImgUrl().equals("default")) {
            firebaseHelper.deleteImageFromFirebase(Customer.getImgUrl());
        }

        // Delete Customer from firestore
        firebaseHelper.deleteUserFromFirestore(String.valueOf(Customer.getMobileNo()))
                .addOnSuccessListener(aVoid -> {
                    // Remove from local list
                    customerList.removeIf(u -> String.valueOf(u.getMobileNo()).equals(String.valueOf(Customer.getMobileNo())));
                    adapter.updateData(customerList);
                    Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete Customer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showUserDetails(Customer Customer) {
        // Create a detailed view dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("User Details");

        String details = "Name: " + Customer.getName() + "\n" +
                "User ID: " + Customer.getUid() + "\n" +
                "Address: " + Customer.getAddress() + "\n" +
                "Mobile: " + Customer.getMobileNo() + "\n" +
                "Aadhaar: " + Customer.getAadhaarNo() + "\n" +
                "Payment Status: " + (Customer.isPaid() ? "Paid" : "Pending") + "\n" +
                "Created: " + Customer.getCreatedAt() + "\n" +
                "Updated: " + Customer.getUpdatedAt();

        builder.setMessage(details);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    // UserDialogListener implementation
    @Override
    public void onUserSaved(Customer Customer) {
        // Add new Customer to the list
        customerList.add(0, Customer);
        adapter.updateData(customerList);
        recyclerView.smoothScrollToPosition(0);
        checkEmptyState();
        Toast.makeText(this, "User added successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserUpdated(Customer Customer) {
        // Find and update the existing Customer
        for (int i = 0; i < customerList.size(); i++) {
            if (String.valueOf(customerList.get(i).getMobileNo()).equals(String.valueOf(Customer.getMobileNo()))) {
                customerList.set(i, Customer);
                break;
            }
        }
        adapter.updateData(customerList);
        checkEmptyState();
        Toast.makeText(this, "User updated successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserSaveFailed(String error) {
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data if needed when returning to this activity
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        // Clear search and show all items when back button pressed
        if (!etSearch.getText().toString().isEmpty()) {
            etSearch.setText("");
            adapter.filter("");
        } else {
            super.onBackPressed();
        }
    }
}