package com.utkarsh.rentmanagement;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.utkarsh.rentmanagement.adapter.userAdapter;
import com.utkarsh.rentmanagement.model.user;
import com.utkarsh.rentmanagement.utils.AuthUtils;
import java.util.ArrayList;
import java.util.List;

public class SearchListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvEmpty;
    private userAdapter adapter;
    private List<user> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_list);

        // Check authentication
        if (!AuthUtils.isUserLoggedIn()) {
            AuthUtils.requireAuthentication(this);
            return;
        }

        initializeViews();
        setupRecyclerView();
        loadSampleData();
        setupSearchFunctionality();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupRecyclerView() {
        userList = new ArrayList<>();
        adapter = new userAdapter(userList, new userAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(user item) {
                onItemClicked(item);
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

    private void loadSampleData() {
        // Add sample data - replace with your actual data source
        userList.clear();
        // Sample user data
        userList.add(new user("Rajesh Kumar", "USER001", "123 Main Street, Mumbai, Maharashtra", "9876543210", 123456789012L, true, "https://avatar.iran.liara.run/public/22", "2024-01-15", "2024-11-25"));
        userList.add(new user("Priya Sharma", "USER002", "456 Park Avenue, Delhi", "8765432109", 234567890123L, false, "https://avatar.iran.liara.run/public/23", "2024-02-20", "2024-11-24"));
        userList.add(new user("Amit Patel", "USER003", "789 MG Road, Bangalore, Karnataka", "7654321098", 345678901234L, true, "https://avatar.iran.liara.run/public/24", "2024-03-10", "2024-11-23"));
        userList.add(new user("Sneha Gupta", "USER004", "321 Church Street, Chennai, Tamil Nadu", "6543210987", 456789012345L, true, "https://avatar.iran.liara.run/public/25", "2024-04-05", "2024-11-22"));
        userList.add(new user("Vikram Singh", "USER005", "654 Marine Drive, Kolkata, West Bengal", "5432109876", 567890123456L, false, "https://avatar.iran.liara.run/public/26", "2024-05-12", "2024-11-21"));
        userList.add(new user("Anjali Reddy", "USER006", "987 Jubilee Hills, Hyderabad, Telangana", "4321098765", 678901234567L, true, "https://avatar.iran.liara.run/public/27", "2024-06-18", "2024-11-20"));
        userList.add(new user("Rahul Verma", "USER007", "147 Sector 17, Chandigarh", "3210987654", 789012345678L, false, "https://avatar.iran.liara.run/public/28", "2024-07-22", "2024-11-19"));
        userList.add(new user("Neha Joshi", "USER008", "258 Ashram Road, Ahmedabad, Gujarat", "2109876543", 890123456789L, true, "https://avatar.iran.liara.run/public/29", "2024-08-30", "2024-11-18"));
        userList.add(new user("Sanjay Mehta", "USER009", "369 FC Road, Pune, Maharashtra", "1098765432", 901234567890L, true, "https://avatar.iran.liara.run/public/30", "2024-09-14", "2024-11-17"));
        userList.add(new user("Pooja Desai", "USER010", "741 Jaipur Road, Jaipur, Rajasthan", "9988776655", 112233445566L, false, "https://avatar.iran.liara.run/public/31", "2024-10-08", "2024-11-16"));
        userList.add(new user("Karan Malhotra", "USER011", "852 Gandhi Nagar, Lucknow, UP", "8877665544", 223344556677L, true, "https://avatar.iran.liara.run/public/32", "2024-11-01", "2024-11-15"));
        userList.add(new user("Meera Nair", "USER012", "963 Banjara Hills, Kochi, Kerala", "7766554433", 334455667788L, false, "https://avatar.iran.liara.run/public/33", "2024-11-10", "2024-11-14"));

        adapter.updateData(userList);
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

    private void performSearch(String query) {
        if (adapter != null) {
            adapter.filter(query);
        }

        // Hide keyboard after search (optional)
        // InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    private void checkEmptyState() {
        if (adapter == null || adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);

            // Update empty text based on search
            String searchQuery = etSearch.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                tvEmpty.setText("No items found for: " + searchQuery);
            } else {
                tvEmpty.setText("No items available");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void onItemClicked(user item) {
        // Handle item click - show details, edit, etc.
        android.widget.Toast.makeText(this,
                "Clicked: " + item.getName() + "\nPrice: $" + item.getMobileNo(),
                android.widget.Toast.LENGTH_SHORT).show();

        // You can start a new activity or show a dialog here
        // Example: Open item details activity
        // Intent intent = new Intent(this, ItemDetailActivity.class);
        // intent.putExtra("item_id", item.getId());
        // startActivity(intent);
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