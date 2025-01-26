package com.dadash.sfcsnotes;
import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.Toast;
import android.net.Network;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
public class dadash_logo_launcher extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private BroadcastReceiver networkChangeReceiver;
    private boolean wasConnected = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_dadash_logo_launcher);
        firestore = FirebaseFirestore.getInstance();
        if (isNetworkAvailable()) {
            loadUser();
        } else {
            Toast.makeText(this.getApplicationContext(), "No Internet Connection Found! Please check your network or restart the app.", Toast.LENGTH_LONG).show();
        }
        monitorInternetConnection();
    }
    private void loadUser() {
        updateUserOpenCount();
        checkServiceStatus();
    }
    private void updateUserOpenCount() {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString("event", "App Opened");
        params.putString("user_email", FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : "Anonymous");
        firebaseAnalytics.logEvent("app_open_event", params);
    }
    private void checkServiceStatus() {
        DocumentReference serviceRef = firestore.document("Maintenance/serviceStatus");
        serviceRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    if (document.getBoolean("underMaintenance")) {
                        openActivityWithDelay(Server_Permission.class, 2000);
                    } else {
                        checkUserLoginState();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Service check failed.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void checkUserLoginState() {
        if (checkIfUserLoggedIn()) {
            openActivityWithDelay(dashboard.class, 0);
        } else {
            openActivityWithDelay(login_register_option.class, 2000);
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
                    loadUser();
                } else if (!isConnected) {
                    Toast.makeText(context, "Internet disconnected! Some features may not work.", Toast.LENGTH_SHORT).show();
                }
                wasConnected = isConnected;
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
