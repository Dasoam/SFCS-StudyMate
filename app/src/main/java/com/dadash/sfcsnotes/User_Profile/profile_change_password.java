package com.dadash.sfcsnotes.User_Profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dadash.sfcsnotes.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

public class profile_change_password extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private ProgressBar progressBar;

    private TextInputEditText currentPasswordInput, newPasswordInput, confirmPasswordInput;
    private Button resetPasswordButton;

    public profile_change_password() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_change_password, container, false);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Bind views
        currentPasswordInput = view.findViewById(R.id.current_password);
        newPasswordInput = view.findViewById(R.id.new_password);
        confirmPasswordInput = view.findViewById(R.id.confirm_password);
        resetPasswordButton = view.findViewById(R.id.reset_password_button);
        progressBar = view.findViewById(R.id.ChangePassword_progressBar);

        // Set click listener for reset password button
        resetPasswordButton.setOnClickListener(v -> validateAndChangePassword());

        return view;
    }

    private void validateAndChangePassword() {
        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword)) {
            showToast("Please enter your current password.");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            showToast("Please enter a new password.");
            return;
        }

        if (!isPasswordValid(newPassword)) {
            showToast("New password must contain at least 8 characters, one uppercase, one lowercase, one digit, and one special character.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showToast("New password and confirm password do not match.");
            return;
        }

        if (newPassword.equals(currentPassword)) {
            showToast("New password cannot be the same as the old password.");
            return;
        }

        reauthenticateAndChangePassword(currentPassword, newPassword);
    }

    private boolean isPasswordValid(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return Pattern.matches(passwordPattern, password);
    }

    private void reauthenticateAndChangePassword(String currentPassword, String newPassword) {
        progressBar.setVisibility(View.VISIBLE);
        if (currentUser != null && currentUser.getEmail() != null) {
            firebaseAuth.signInWithEmailAndPassword(currentUser.getEmail(), currentPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            updatePassword(newPassword);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            showToast("Current password is incorrect.");
                        }
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            showToast("User not authenticated.");
        }
    }

    private void updatePassword(String newPassword) {
        if (currentUser != null) {
            currentUser.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updatePasswordInFirestore(newPassword);
                    showToast("Password updated successfully.");
                    progressBar.setVisibility(View.GONE);
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    showToast("Failed to update password.");
                }
            });
        }
    }

    private void updatePasswordInFirestore(String newPassword) {
        if (currentUser != null && currentUser.getEmail() != null) {
            String email = currentUser.getEmail();
            String capturedOn = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());

            HashMap<String, Object> passwordData = new HashMap<>();
            passwordData.put("Captured On", capturedOn);
            passwordData.put("Email", email);
            passwordData.put("Updated Password", newPassword);

            firestore.collection("User's Updated Passwords").document(email)
                    .set(passwordData)
                    .addOnSuccessListener(aVoid -> Log.d("Password","Password updated successfully."))
                    .addOnFailureListener(e -> Log.d("Password","Failed to update password in Firestore."));
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
