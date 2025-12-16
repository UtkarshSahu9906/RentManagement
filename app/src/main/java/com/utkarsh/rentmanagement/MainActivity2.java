package com.utkarsh.rentmanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.utkarsh.rentmanagement.dialog.RentCalculatorDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity2 extends AppCompatActivity
        implements RentCalculatorDialog.RentCalculatorListener {

    private Button btnOpenDialog;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        btnOpenDialog = findViewById(R.id.btnOpenDialog);
        btnOpenDialog.setOnClickListener(v -> {
            // Show the rent calculator dialog
            RentCalculatorDialog.showDialog(getSupportFragmentManager(), this);
        });
    }

    @Override
    public void onRentCalculated(String productId, Date startDate, Date endDate,
                                 double dailyPrice, double totalRent) {
        // Handle the calculated rent result
        String message = String.format(Locale.getDefault(),
                "Product %s rented from %s to %s\nDaily: $%.2f, Total: $%.2f",
                productId,
                dateFormat.format(startDate),
                dateFormat.format(endDate),
                dailyPrice,
                totalRent
        );

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // You can also save this to database, update UI, etc.
        // saveRentToDatabase(productId, startDate, endDate, dailyPrice, totalRent);
    }


}