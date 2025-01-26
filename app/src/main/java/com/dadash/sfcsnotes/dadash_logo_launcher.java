package com.dadash.sfcsnotes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.net.Network;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.Manifest;

public class dadash_logo_launcher extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private BroadcastReceiver networkChangeReceiver;
    private boolean wasConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide the title
        getSupportActionBar().hide(); // Hide the action bar

        setContentView(R.layout.activity_dadash_logo_launcher);

        firestore = FirebaseFirestore.getInstance();

//        // Check if the app was opened via notification
//        if (getIntent().getBooleanExtra("notification_clicked", false)) {
//            Log.d("Notification", "Notification was clicked");
//            // Log this event to your analytics platform (e.g., Firebase Analytics)
//        }
//        Bundle bundle = new Bundle();
//        bundle.putString("notification_action", "clicked");
//        FirebaseAnalytics.getInstance(this).logEvent("notification_event", bundle);


        // Check if network is available
        if (isNetworkAvailable()) {
            loadUser(); // Proceed to load user data or continue with the intended operation
        } else {
            Toast.makeText(this.getApplicationContext(), "No Internet Connection Found! Please check your network or restart the app.", Toast.LENGTH_LONG).show();
        }

        // Monitor network changes
        monitorInternetConnection();
    }

    private void loadUser() {
        updateUserOpenCount(); // Step 1: Update app open count
        checkServiceStatus();  // Step 2: Check maintenance and login state
    }

    private void updateUserOpenCount() {
//        DocumentReference analyticsRef = firestore.document("Analytics/userCountOpen");
//        analyticsRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                if (document.exists()) {
//                    Long appOpen = document.getLong("app_opens");
//                    int appOpenCount = appOpen != null ? appOpen.intValue() : 0;
//                    appOpenCount++;
//                    analyticsRef.update("app_opens", appOpenCount)
//                            .addOnSuccessListener(aVoid -> Log.e("Firestore", "App open count updated"))
//                            .addOnFailureListener(e -> Log.e("Firestore", "Failed to update app open count: " + e.getMessage()));
//                } else {
//                    Log.e("Firestore", "Analytics document does not exist.");
//                }
//            } else {
//                Log.e("Firestore", "Failed to fetch analytics data: " + task.getException().getMessage());
//            }
//        });
        // Get instance of FirebaseAnalytics
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Create a Bundle to hold event parameters (optional)
        Bundle params = new Bundle();
        params.putString("event", "App Opened");
        params.putString("user_email", FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : "Anonymous");

        // Log the event
        firebaseAnalytics.logEvent("app_open_event", params);

        // Log to console for verification
        Log.d("FirebaseAnalytics", "App open event logged successfully");
    }

    private void checkServiceStatus() {
        DocumentReference serviceRef = firestore.document("Maintenance/serviceStatus");
        serviceRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    if (document.getBoolean("underMaintenance")) {
                        openActivityWithDelay(Server_Permission.class, 2000); // Add a delay
                    } else {
                        checkUserLoginState();
                    }
                } else {
                    Log.e("Firestore", "Service document does not exist.");
                    Toast.makeText(getApplicationContext(), "Service check failed.", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e("Firestore", "Failed to fetch service data: " + task.getException().getMessage());
                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkUserLoginState() {
        if (checkIfUserLoggedIn()) {
            openActivityWithDelay(dashboard.class, 0); // Add a delay
        } else {
            openActivityWithDelay(login_register_option.class, 2000); // Add a delay
        }
    }

    private void openActivityWithDelay(Class<?> activityClass, int delayMillis) {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), activityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, delayMillis);
    }

    private boolean checkIfUserLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network activeNetwork = cm.getActiveNetwork();
            return activeNetwork != null;
        } else {
            NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    private void monitorInternetConnection() {
        networkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isConnected = isNetworkAvailable();
                if (isConnected && !wasConnected) {
                    Toast.makeText(context, "Internet reconnected! Resuming operations...", Toast.LENGTH_SHORT).show();
                    loadUser(); // Re-trigger Firestore logic
                } else if (!isConnected) {
                    Toast.makeText(context, "Internet disconnected! Some features may not work.", Toast.LENGTH_SHORT).show();
                }
                wasConnected = isConnected; // Update connection state
            }
        };
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}
