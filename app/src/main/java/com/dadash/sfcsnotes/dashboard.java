package com.dadash.sfcsnotes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class dashboard extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private long lastBackPressedTime = 0; // To track back press time
    private Toast backPressedToast; // Toast message for "press back again to exit"
    private static final int EXIT_DELAY_TIME = 2000; // Time delay for exit (in milliseconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide the title
        getSupportActionBar().hide(); // Hide the action bar
        

        setContentView(R.layout.activity_dashboard);

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // Check user authentication on activity creation
        checkUserAuthentication();
        askNotificationPermission();

        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setItemIconTintList(null);
        // Set default fragment on start
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new dashboard_fragment())
                .commit();

        // Handle bottom navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.navbar_home_button) {
                selectedFragment = new dashboard_fragment();
                clearBackStack(); // Clear the back stack when "Home" (Dashboard) is clicked
            } else if (itemId == R.id.navbar_user_account_button) {
                selectedFragment = new user_profile_fragment();
                clearBackStack();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
            }
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//
//            if (selectedFragment == null) {
//                transaction.replace(R.id.fragmentContainer, selectedFragment);
//            } else {
//                transaction.show(fragment); // Just show the fragment if it already exists
//            }
//
//            transaction.commit();

            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserAuthentication(); // Re-check authentication when activity resumes
    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in or account has been deleted
            redirectToLogin("Your session has expired or your account is no longer available.");
        } else {
            // Validate token to ensure the account exists
            currentUser.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            // Token is invalid or user account is deleted
                            redirectToLogin("Session expired. Please log in again.");
                        }
                    });
        }
    }

    private void redirectToLogin(String message) {
        // Show a toast message if needed
        if (message != null && !message.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(dashboard.this, message, Toast.LENGTH_SHORT).show());
        }

        // Redirect to login activity
        Intent intent = new Intent(dashboard.this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

//    @Override
//    public void onBackPressed() {
//        // If we are already on the dashboard (home) and the user presses back, confirm exit
//        if (System.currentTimeMillis() - lastBackPressedTime < EXIT_DELAY_TIME) {
//            super.onBackPressed();  // Exit app after 2 seconds of back press
//        } else {
//            lastBackPressedTime = System.currentTimeMillis();
//            // Show "Press back again to exit" message
//            backPressedToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
//            backPressedToast.show();
//        }
//    }

    // Clear the back stack so that pressing back will exit the app
    private void clearBackStack() {
        // Remove all fragments from the back stack
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });
    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
