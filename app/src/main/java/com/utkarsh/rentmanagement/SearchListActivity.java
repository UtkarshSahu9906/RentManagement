package com.utkarsh.rentmanagement;

import android.content.Intent;
import android.net.Uri;
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
import com.utkarsh.rentmanagement.adapter.userAdapter;
import com.utkarsh.rentmanagement.dialog.UserDialog;
import com.utkarsh.rentmanagement.model.user;
import com.utkarsh.rentmanagement.utils.AuthUtils;
import com.utkarsh.rentmanagement.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class SearchListActivity extends AppCompatActivity implements UserDialog.UserDialogListener {

    private RecyclerView recyclerView;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvEmpty;
    private FloatingActionButton fabAddUser;
    private userAdapter adapter;
    private List<user> userList;
    private FirebaseHelper firebaseHelper;
    private UserDialog userDialog;

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
            if (userDialog != null) {
                userDialog.handleActivityResult(requestCode, resultCode, data);
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
        userList = new ArrayList<>();
        adapter = new userAdapter(userList, new userAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(user item) {
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
                    userList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            user user = document.toObject(user.class);
                            userList.add(user);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error loading user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    adapter.updateData(userList);
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

        // Real-time search as user types
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
        userDialog = new UserDialog(this, this);
        userDialog.show();
    }

    private void showEditUserDialog(user user) {
        userDialog = new UserDialog(this, user, this);
        userDialog.show();
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

    private void onUserClicked(user user) {
        // Show options: Edit or View Details
        showUserOptionsDialog(user);
    }

    private void showUserOptionsDialog(final user user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("User Options");
        builder.setMessage("Choose an action for " + user.getName());

        builder.setPositiveButton("Edit", (dialog, which) -> {
            showEditUserDialog(user);
        });

        builder.setNegativeButton("View Details", (dialog, which) -> {
            showUserDetails(user);
        });

        builder.setNeutralButton("Delete", (dialog, which) -> {
            showDeleteConfirmation(user);
        });

        builder.show();
    }

    private void showDeleteConfirmation(user user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Delete User");
        builder.setMessage("Are you sure you want to delete " + user.getName() + "? This action cannot be undone.");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteUserFromFirebase(user);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteUserFromFirebase(user user) {
        // Delete image from storage first
        if (!user.getImgUrl().equals("default")) {
            firebaseHelper.deleteImageFromFirebase(user.getImgUrl());
        }

        // Delete user from firestore
        firebaseHelper.deleteUserFromFirestore(String.valueOf(user.getMobileNo()))
                .addOnSuccessListener(aVoid -> {
                    // Remove from local list
                    userList.removeIf(u -> String.valueOf(u.getMobileNo()).equals(String.valueOf(user.getMobileNo())));
                    adapter.updateData(userList);
                    Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showUserDetails(user user) {
        // Create a detailed view dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("User Details");

        String details = "Name: " + user.getName() + "\n" +
                "User ID: " + user.getUid() + "\n" +
                "Address: " + user.getAddress() + "\n" +
                "Mobile: " + user.getMobileNo() + "\n" +
                "Aadhaar: " + user.getAadhaarNo() + "\n" +
                "Payment Status: " + (user.isPaid() ? "Paid" : "Pending") + "\n" +
                "Created: " + user.getCreatedAt() + "\n" +
                "Updated: " + user.getUpdatedAt();

        builder.setMessage(details);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    // UserDialogListener implementation
    @Override
    public void onUserSaved(user user) {
        // Add new user to the list
        userList.add(0, user);
        adapter.updateData(userList);
        recyclerView.smoothScrollToPosition(0);
        checkEmptyState();
        Toast.makeText(this, "User added successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserUpdated(user user) {
        // Find and update the existing user
        for (int i = 0; i < userList.size(); i++) {
            if (String.valueOf(userList.get(i).getMobileNo()).equals(String.valueOf(user.getMobileNo()))) {
                userList.set(i, user);
                break;
            }
        }
        adapter.updateData(userList);
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