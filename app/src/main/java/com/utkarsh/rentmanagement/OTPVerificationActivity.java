package com.utkarsh.rentmanagement;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OTPVerificationActivity extends AppCompatActivity {

    private EditText etOTP;
    private Button btnVerifyOTP;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverification);

        mAuth = FirebaseAuth.getInstance();
        initializeViews();
        getIntentData();
        setupClickListeners();
    }

    private void initializeViews() {
        etOTP = findViewById(R.id.etOTP);
        btnVerifyOTP = findViewById(R.id.btn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void getIntentData() {
        verificationId = getIntent().getStringExtra("verificationId");
        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Verify: " + phoneNumber);
        }
    }

    private void setupClickListeners() {
        btnVerifyOTP.setOnClickListener(v -> {
            String otp = etOTP.getText().toString().trim();
            if (otp.isEmpty() || otp.length() != 6) {
                etOTP.setError("Enter valid 6-digit OTP");
                etOTP.requestFocus();
                return;
            }
            verifyCode(otp);
        });
    }

    private void verifyCode(String code) {
        progressBar.setVisibility(View.VISIBLE);
        btnVerifyOTP.setEnabled(false);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnVerifyOTP.setEnabled(true);

                    if (task.isSuccessful()) {
                        // Success
                        Toast.makeText(OTPVerificationActivity.this,
                                "Login successful!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(OTPVerificationActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(OTPVerificationActivity.this,
                                "Verification failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}