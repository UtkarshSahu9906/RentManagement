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
import com.utkarsh.rentmanagement.adapter.ItemAdapter;
import com.utkarsh.rentmanagement.model.Item;
import com.utkarsh.rentmanagement.utils.AuthUtils;
import java.util.ArrayList;
import java.util.List;

public class SearchListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvEmpty;
    private ItemAdapter adapter;
    private List<Item> itemList;

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
        itemList = new ArrayList<>();
        adapter = new ItemAdapter(itemList, new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Item item) {
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
        itemList.clear();
        itemList.add(new Item("1", "Apartment A", "2 BHK apartment with balcony", 1200.00, "Apartment"));
        itemList.add(new Item("2", "Studio Flat", "Compact studio near downtown", 800.00, "Studio"));
        itemList.add(new Item("3", "Luxury Villa", "3 BHK villa with pool", 2500.00, "Villa"));
        itemList.add(new Item("4", "Office Space", "Commercial space for rent", 1500.00, "Commercial"));
        itemList.add(new Item("5", "House B", "Family house with garden", 1800.00, "House"));
        itemList.add(new Item("6", "Apartment B", "1 BHK economical apartment", 600.00, "Apartment"));
        itemList.add(new Item("7", "Penthouse", "Luxury penthouse with view", 3500.00, "Penthouse"));
        itemList.add(new Item("8", "Condo", "Modern condo with amenities", 2000.00, "Condo"));
        itemList.add(new Item("9", "Townhouse", "Spacious townhouse", 1600.00, "Townhouse"));
        itemList.add(new Item("10", "Duplex", "Two-story duplex", 2200.00, "Duplex"));

        adapter.updateData(itemList);
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

    private void onItemClicked(Item item) {
        // Handle item click - show details, edit, etc.
        android.widget.Toast.makeText(this,
                "Clicked: " + item.getTitle() + "\nPrice: $" + item.getPrice(),
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