package com.utkarsh.rentmanagement;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.utkarsh.rentmanagement.utils.AuthUtils;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(this::checkAuthenticationStatus, SPLASH_DELAY);

//        mAuth = FirebaseAuth.getInstance();
//
//
//
//        String uid = mAuth.getCurrentUser().getUid();
//
//        Toast.makeText(this, ""+checkUserExistsSync(uid), Toast.LENGTH_SHORT).show();

    }

    private void checkAuthenticationStatus() {
        if (AuthUtils.isUserLoggedIn()) {
            // User is logged in, go to MainActivity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            Toast.makeText(this, "login", Toast.LENGTH_SHORT).show();
        } else {
            // User is not logged in, go to LoginActivity
            startActivity(new Intent(SplashActivity.this, PhoneLoginActivity.class));
            Toast.makeText(this, "Not_login", Toast.LENGTH_SHORT).show();

        }
        finish();
    }
    public boolean checkUserExistsSync(String uid) {


        if (uid == null || uid.isEmpty()) {
            return false;
        }

        try {
            // Use Tasks.await for synchronous operation
            Task<DocumentSnapshot> task = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get();

            DocumentSnapshot document = Tasks.await(task);
            return document != null && document.exists();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}