package com.utkarsh.rentmanagement.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.utkarsh.rentmanagement.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RentCalculatorDialog extends DialogFragment {

    private RentCalculatorListener listener;
    private Spinner spinnerProducts;
    private Button btnStartDate, btnEndDate, btnCalculate, btnCancel;
    private EditText etDailyPrice;
    private TextView tvResult;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String selectedProduct = "";

    // Product class to hold product details
    public static class Product {
        private String id;
        private String name;
        private double defaultPrice;

        public Product(String id, String name, double defaultPrice) {
            this.id = id;
            this.name = name;
            this.defaultPrice = defaultPrice;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public double getDefaultPrice() { return defaultPrice; }

        @Override
        public String toString() { return name; }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_rent_calculator, null);

        initViews(view);
        setupSpinner();
        setupDatePickers();
        setupButtons(view); // Pass the view here

        builder.setView(view)
                .setTitle("Calculate Product Rent");

        return builder.create();
    }

    private void initViews(View view) {
        spinnerProducts = view.findViewById(R.id.spinnerProducts);
        btnStartDate = view.findViewById(R.id.btnStartDate);
        btnEndDate = view.findViewById(R.id.btnEndDate);
        etDailyPrice = view.findViewById(R.id.etDailyPrice);
        tvResult = view.findViewById(R.id.tvResult);
        btnCalculate = view.findViewById(R.id.btnCalculate); // Initialize from view
        btnCancel = view.findViewById(R.id.btnCancel); // Initialize from view

        // Set current dates
        updateDateButtons();
    }

    private void setupSpinner() {
        // Create product list
        List<Product> products = new ArrayList<>();
        products.add(new Product("A", "Product A", 50.0));
        products.add(new Product("B", "Product B", 75.0));
        products.add(new Product("C", "Product C", 100.0));
        products.add(new Product("D", "Product D", 125.0));
        products.add(new Product("E", "Product E", 150.0));

        // Create adapter
        ArrayAdapter<Product> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                products
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducts.setAdapter(adapter);

        // Set spinner listener
        spinnerProducts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Product selected = (Product) parent.getItemAtPosition(position);
                selectedProduct = selected.getId();
                // Set default price for selected product
                etDailyPrice.setText(String.valueOf(selected.getDefaultPrice()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProduct = "";
            }
        });
    }

    private void setupDatePickers() {
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startCalendar : endCalendar;
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    updateDateButtons();
                    clearResult(); // Clear previous result when dates change
                },
                year, month, day
        );

        // Optional: Set minimum date for end date picker
        if (!isStartDate) {
            datePickerDialog.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    private void updateDateButtons() {
        btnStartDate.setText("Start: " + dateFormat.format(startCalendar.getTime()));
        btnEndDate.setText("End: " + dateFormat.format(endCalendar.getTime()));
    }

    private void setupButtons(View view) {
        // Get buttons from the inflated view, NOT from getDialog()
        btnCalculate = view.findViewById(R.id.btnCalculate);
        btnCancel = view.findViewById(R.id.btnCancel);

        btnCalculate.setOnClickListener(v -> calculateRent());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void calculateRent() {
        // Validate inputs
        if (selectedProduct.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a product", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startCalendar.after(endCalendar)) {
            Toast.makeText(requireContext(), "End date cannot be before start date", Toast.LENGTH_SHORT).show();
            return;
        }

        String priceStr = etDailyPrice.getText().toString().trim();
        if (priceStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter daily price", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double dailyPrice = Double.parseDouble(priceStr);
            if (dailyPrice <= 0) {
                Toast.makeText(requireContext(), "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculate days between dates
            long startTime = startCalendar.getTimeInMillis();
            long endTime = endCalendar.getTimeInMillis();
            long days = (endTime - startTime) / (1000 * 60 * 60 * 24) + 1;

            // Calculate total rent
            double totalRent = days * dailyPrice;

            // Display result
            tvResult.setText(String.format(Locale.getDefault(),
                    "Product: %s\nTotal Rent: $%.2f\nDuration: %d days",
                    selectedProduct, totalRent, days));
            tvResult.setVisibility(View.VISIBLE);

            // Notify listener if needed
            if (listener != null) {
                listener.onRentCalculated(selectedProduct, startCalendar.getTime(),
                        endCalendar.getTime(), dailyPrice, totalRent);
            }

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearResult() {
        tvResult.setVisibility(View.GONE);
    }

    // Listener interface for callback
    public interface RentCalculatorListener {
        void onRentCalculated(String productId, Date startDate, Date endDate,
                              double dailyPrice, double totalRent);
    }

    public void setRentCalculatorListener(RentCalculatorListener listener) {
        this.listener = listener;
    }

    // Static method to show dialog
    public static void showDialog(androidx.fragment.app.FragmentManager fragmentManager,
                                  RentCalculatorListener listener) {
        RentCalculatorDialog dialog = new RentCalculatorDialog();
        dialog.setRentCalculatorListener(listener);
        dialog.show(fragmentManager, "RentCalculatorDialog");
    }
}