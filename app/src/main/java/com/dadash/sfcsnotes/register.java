package com.dadash.sfcsnotes;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
public class register extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private EditText nameField, emailField, phoneField, passwordField, confirmPasswordField, schoolCodeField, admissionNumberField;
    private Spinner classSpinner;
    private Button registerButton;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        nameField = findViewById(R.id.Student_Name);
        emailField = findViewById(R.id.Student_Email_ID);
        phoneField = findViewById(R.id.Student_Phone_Number);
        passwordField = findViewById(R.id.Set_password);
        confirmPasswordField = findViewById(R.id.Confirm_password);
        schoolCodeField = findViewById(R.id.School_Code);
        admissionNumberField = findViewById(R.id.Student_Admission_Number);
        classSpinner = findViewById(R.id.Spinner_Class_Option);
        registerButton = findViewById(R.id.SignUp);
        progressBar = findViewById(R.id.progressBar);
        TextView existingUserHint = findViewById(R.id.existing_userHint);
        existingUserHint.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        registerButton.setOnClickListener(v -> registerUser());
    }
    private void registerUser() {
        if (!isNetworkAvailable()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "No internet connection. Please check your network and try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim().toLowerCase();
        String phone = phoneField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        String schoolCode = schoolCodeField.getText().toString().trim();
        String admissionNumber = admissionNumberField.getText().toString().trim();
        String currentClass = classSpinner.getSelectedItem().toString();
        if (!validateInputs(name, email, phone, password, confirmPassword, schoolCode, admissionNumber, currentClass)) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please fix the errors above and try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        firestore.document("Access Codes/For SFCS Meerut").get().addOnSuccessListener(document -> {
            if (document.exists()) {
                List<String> validSchoolCodes = (List<String>) document.get("schoolCodes");
                if (validSchoolCodes != null && validSchoolCodes.contains(schoolCode)) {
                    createFirebaseUser(name, email, password, phone, currentClass, schoolCode, admissionNumber);
                } else {
                    progressBar.setVisibility(View.GONE);
                    schoolCodeField.setError("Invalid School Code");
                    Toast.makeText(this, "Registration Failed: Invalid School Code", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to validate school code. Try again later.", Toast.LENGTH_SHORT).show();
        });
    }
    private boolean validateInputs(String name, String email, String phone, String password, String confirmPassword, String schoolCode,
                                   String admissionNumber, String currentClass) {
        String emailRegex = "^[a-zA-Z0-9._-]+@(gmail\\.com|outlook\\.com|yahoo\\.com|hotmail\\.com|icloud\\.com|aol\\.com|protonmail\\.com|zoho\\.com|gmx\\.com|yandex\\.com)$";
        String nameRegex = "^[a-zA-Z\\s]+$";
        if (name.isEmpty() || !name.matches(nameRegex)) {
            nameField.setError("Enter a your name");
            return false;
        }
        if (email.isEmpty() || !email.matches(emailRegex)) {
            emailField.setError("Enter a valid email from allowed domains (e.g., gmail.com, outlook.com, etc.)");
            return false;
        }
        if (!phone.isEmpty() && phone.length() != 10) {
            phoneField.setError("Please enter a valid phone number without +91");
            return false;
        }
        if (currentClass.equals("Select Class")) {
            ((TextView) classSpinner.getSelectedView()).setError("Select valid options");
            Toast.makeText(this, "Please a valid class.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 8 || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
            passwordField.setError("Password must be at least 8 characters long, with uppercase, lowercase, digit, and special character.");
            return false;
        }
        if (confirmPassword.length() < 8 || !password.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match");
            return false;
        }
        if (schoolCode.isEmpty()) {
            schoolCodeField.setError("Please enter valid school code");
            return false;
        }
        if (admissionNumber.isEmpty()) {
            admissionNumberField.setError("Enter a valid admission number.");
            return false;
        }
        return true;
    }
    private void createFirebaseUser(String name, String email, String password, String phone,
                                    String currentClass, String schoolCode, String admissionNumber) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                                if (emailTask.isSuccessful()) {
                                    Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        saveUserDetails(user, name, email, phone, currentClass, schoolCode, admissionNumber, password);
                        collectDeviceInfo(user.getEmail());
                        saveUserName(user,name);
                        Intent intent = new Intent(getApplicationContext(), dashboard.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveUserDetails(FirebaseUser user, String name, String email, String phone, String currentClass,
                                 String schoolCode, String admissionNumber, String password) {
        HashMap<String, Object> userDetails = new HashMap<>();
        userDetails.put("Name", name);
        userDetails.put("Email", email);
        userDetails.put("Phone", phone);
        userDetails.put("Class", currentClass);
        userDetails.put("Admission Number", admissionNumber);
        userDetails.put("Password", password);
        userDetails.put("Used School Code", schoolCode);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        userDetails.put("Registration Date", currentDateTime);
        firestore.collection("User's Info").document(email).set(userDetails).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "User details saved successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to save user details", Toast.LENGTH_SHORT).show();
        });
    }
    private void collectDeviceInfo(String email) {
        String model = android.os.Build.MODEL;
        String brand = android.os.Build.BRAND;
        String manufacturer = android.os.Build.MANUFACTURER;
        String product = android.os.Build.PRODUCT;
        String device = android.os.Build.DEVICE;
        String androidVersion = android.os.Build.VERSION.RELEASE;
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float density = metrics.density;
        int densityDpi = metrics.densityDpi;
        String connectionType = "No Connection";
        String networkType = "Unknown";
        String operatorName = "N/A";
        if (isNetworkAvailable()) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    connectionType = activeNetwork.getTypeName();
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        networkType = "Wi-Fi";
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        networkType = "Mobile Data";
                        operatorName = activeNetwork.getExtraInfo();
                    }
                }
            }
        }
        HashMap<String, Object> deviceInfo = new HashMap<>();
        deviceInfo.put("Device ID (Android ID)", androidId);
        deviceInfo.put("Model", model);
        deviceInfo.put("Brand", brand);
        deviceInfo.put("Manufacturer", manufacturer);
        deviceInfo.put("Product", product);
        deviceInfo.put("Device", device);
        deviceInfo.put("Android Version", androidVersion);
        deviceInfo.put("Display Width (px)", widthPixels);
        deviceInfo.put("Display Height (px)", heightPixels);
        deviceInfo.put("Screen Density", density);
        deviceInfo.put("Screen Density DPI", densityDpi);
        deviceInfo.put("Connection Type", connectionType);
        deviceInfo.put("Network Type", networkType);
        deviceInfo.put("Network Operator Name", operatorName);
        String uniqueId = UUID.randomUUID().toString();
        firestore.collection("User Device Info").document(email + "++" + uniqueId).set(deviceInfo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Device info saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save device info", Toast.LENGTH_SHORT).show();
                });
    }
    private void saveUserName(FirebaseUser user, String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        }
                    }
                });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
}
