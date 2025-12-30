package com.utkarsh.rentmanagement;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.utkarsh.rentmanagement.dialog.UserInfoDialog;
import com.utkarsh.rentmanagement.model.Shop;
import com.utkarsh.rentmanagement.utils.AuthUtils;
import com.utkarsh.rentmanagement.utils.OwnershipDatabase;

public class MainActivity extends AppCompatActivity implements UserInfoDialog.UserSaveListener {

    // Views from your layout
    private MaterialCardView headerCard;
    private FloatingActionButton fabMain, fabCreateShop, fabJoinShop;
   // private LottieAnimationView avatarAnimation;
    private TextView tvWelcomeTitle, tvWelcomeSubtitle;
    private ImageView ivNoShops, ivNoMemberships;
    private OwnershipDatabase ownershipDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        ownershipDb = new OwnershipDatabase();

        // Initialize ownership for current user
        ownershipDb.initializeUserOwnership((success, message) -> {
            if (success) {
                Log.d("Ownership", message);
            } else {
                Log.e("Ownership", message);
            }
        });

        // Check authentication first
        if (!AuthUtils.isUserLoggedIn()) {
            AuthUtils.requireAuthentication(this);
            return;
        }

        setContentView(R.layout.activity_main); // Use your updated layout

        initializeViews();
        setupAnimations();
        setupClickListeners();



        findViewById(R.id.tvShopStats).setOnClickListener(v -> {
            // Show the user info dialog
            UserInfoDialog.showDialog(getSupportFragmentManager(), this);
        });

        // Optional: Add layout transition animations
        enableLayoutTransitions();

        // Load user data after a short delay
        new Handler().postDelayed(() -> {
            loadUserData();
        }, 500);
    }

    private void initializeViews() {
        // Initialize all views
        headerCard = findViewById(R.id.headerCard);
        fabMain = findViewById(R.id.fabMain);
        fabCreateShop = findViewById(R.id.fabCreateShop);
        fabJoinShop = findViewById(R.id.fabJoinShop);
       // avatarAnimation = findViewById(R.id.avatarAnimation);
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        tvWelcomeSubtitle = findViewById(R.id.tvWelcomeSubtitle);
        ivNoShops = findViewById(R.id.ivNoShops);
        ivNoMemberships = findViewById(R.id.ivNoMemberships);
    }

    private void setupAnimations() {
        // Animate header on appear with delay
        new Handler().postDelayed(() -> {
            if (headerCard != null) {
                headerCard.setAlpha(0f);
                headerCard.setScaleX(0.9f);
                headerCard.setScaleY(0.9f);

                headerCard.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(800)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
            }
        }, 300);

        // Setup FAB animations
        setupFABAnimations();

        // Setup Lottie animation
//        if (avatarAnimation != null) {
//            avatarAnimation.setAnimation(R.raw.avatar_animation); // Make sure you have this file
//            avatarAnimation.playAnimation();
//            avatarAnimation.loop(true);
//        }
    }

    private void setupFABAnimations() {
        if (fabMain != null) {
            fabMain.setOnClickListener(v -> toggleFABs());
        }

        // Initial state - only main FAB visible
        if (fabCreateShop != null) fabCreateShop.setVisibility(View.GONE);
        if (fabJoinShop != null) fabJoinShop.setVisibility(View.GONE);

        // Set click listeners for secondary FABs
        if (fabCreateShop != null) {
            fabCreateShop.setOnClickListener(v -> {
                // Navigate to create shop activity
                startActivity(new Intent(MainActivity.this, CreateShopActivity.class));
                hideFABs();
            });
        }

        if (fabJoinShop != null) {
            fabJoinShop.setOnClickListener(v -> {
                // Navigate to join shop activity
                startActivity(new Intent(MainActivity.this, JoinShopActivity.class));
                hideFABs();
            });
        }
    }

    private void toggleFABs() {
        if (fabCreateShop != null && fabJoinShop != null) {
            if (fabCreateShop.getVisibility() == View.VISIBLE) {
                // Hide secondary FABs
                hideFABs();
            } else {
                // Show secondary FABs
                showFABs();
            }
        }
    }

    private void showFABs() {
        if (fabCreateShop != null && fabJoinShop != null && fabMain != null) {
            fabCreateShop.setVisibility(View.VISIBLE);
            fabJoinShop.setVisibility(View.VISIBLE);

            // Get the margin dimension
            float fabMargin = getResources().getDimension(R.dimen.fab_margin);

            fabCreateShop.setAlpha(0f);
            fabCreateShop.setTranslationY(0);
            fabCreateShop.animate()
                    .translationY(-fabMargin)
                    .alpha(1f)
                    .setDuration(300)
                    .start();

            fabJoinShop.setAlpha(0f);
            fabJoinShop.setTranslationY(0);
            fabJoinShop.animate()
                    .translationY(-fabMargin * 2)
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(100)
                    .start();

            fabMain.animate()
                    .rotation(45f)
                    .setDuration(300)
                    .start();
        }
    }

    private void hideFABs() {
        if (fabCreateShop != null && fabJoinShop != null && fabMain != null) {
            fabCreateShop.animate()
                    .translationY(0)
                    .alpha(0f)
                    .setDuration(300)
                    .start();

            fabJoinShop.animate()
                    .translationY(0)
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            fabCreateShop.setVisibility(View.GONE);
                            fabJoinShop.setVisibility(View.GONE);
                        }
                    })
                    .start();

            fabMain.animate()
                    .rotation(0f)
                    .setDuration(300)
                    .start();
        }
    }

    private void setupClickListeners() {
        // Setup click listeners for other views if needed
        TextView tvViewAllShops = findViewById(R.id.tvViewAllShops);
        TextView tvViewAllMemberships = findViewById(R.id.tvViewAllMemberships);

        if (tvViewAllShops != null) {
            tvViewAllShops.setOnClickListener(v -> {
                // Navigate to all shops view
                startActivity(new Intent(MainActivity.this, AllShopsActivity.class));
            });
        }

        if (tvViewAllMemberships != null) {
            tvViewAllMemberships.setOnClickListener(v -> {
                // Navigate to all memberships view
                startActivity(new Intent(MainActivity.this, AllMembershipsActivity.class));
            });
        }
    }

    private void enableLayoutTransitions() {
        // Get the content view (the entire activity layout)
        ViewGroup contentView = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        if (contentView != null) {
            LayoutTransition transition = new LayoutTransition();
            transition.enableTransitionType(LayoutTransition.CHANGING);
            transition.setDuration(300);
            contentView.setLayoutTransition(transition);
        }
    }

    private void loadUserData() {
        // Load user data and update UI
        // You can fetch from Firebase or SharedPreferences

        // Example: Update welcome text
        String userName = "User Name"; // Get from Firebase
        if (tvWelcomeTitle != null) {
            tvWelcomeTitle.setText("Welcome Back, " + userName + "!");
        }

        // Update other UI elements as needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Hide FABs when returning to activity
        hideFABs();

        // Restart animation
//        if (avatarAnimation != null) {
//            avatarAnimation.resumeAnimation();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause animation to save battery
//        if (avatarAnimation != null) {
//            avatarAnimation.pauseAnimation();
//        }
    }

    @Override
    public void onBackPressed() {
        // Hide FABs first if they're visible
        if (fabCreateShop != null && fabCreateShop.getVisibility() == View.VISIBLE) {
            hideFABs();
        } else {
            super.onBackPressed();
        }
    }


    // Get all shops
    private void loadUserShops() {
        ownershipDb.getUserShops((success, message, shops) -> {
            if (success) {
                if (shops.isEmpty()) {
                    Toast.makeText(this, "No shops yet", Toast.LENGTH_SHORT).show();
                } else {
                    for (Shop shop : shops) {
                        Log.d("Shop", shop.getShopName() + " - " + shop.getShopId());
                    }
                }
            } else {
                Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update a shop
    private void updateExistingShop(String shopId) {
        ownershipDb.updateShop(shopId,
                "Updated Shop Name",
                "https://new-bg-img.com/image.jpg",
                "https://new-logo.com/logo.png",
                (success, message) -> {
                    if (success) {
                        Toast.makeText(this, "Shop updated!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Check ownership
    private void checkIfUserOwnsShop(String shopId) {
        ownershipDb.checkShopOwnership(shopId,
                (success, message, ownsShop) -> {
                    if (success) {
                        if (ownsShop) {
                            Toast.makeText(this, "You own this shop",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "You don't own this shop",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @Override
    public void onUserSaved(String userId, String username) {
        // Handle successful save
        String message = "User '" + username + "' saved successfully!\nUser ID: " + userId;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Update UI or navigate to next screen
        // Example: updateUserStatus(userId);
    }

    @Override
    public void onUserSaveFailed(String error) {
        // Handle save failure
        Toast.makeText(this, "Failed to save user: " + error, Toast.LENGTH_LONG).show();
    }

    // Method to open dialog with pre-filled data (for editing)
    private void openEditUserDialog() {
        UserInfoDialog.showDialogWithData(
                getSupportFragmentManager(),
                "John Doe",          // Existing username
                "9876543210",        // Existing mobile
                "123 Main St, City", // Existing address
                "john@example.com",  // Existing email
                this
        );
    }
}