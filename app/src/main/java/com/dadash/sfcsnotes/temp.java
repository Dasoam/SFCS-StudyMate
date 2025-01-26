package com.dadash.sfcsnotes;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.regex.Pattern;

public class temp {
}


//


//package com.dadash.sfcsnotes.User_Profile;
//
//        import android.content.Intent;
//        import android.os.Bundle;
//        import android.os.CountDownTimer;
//        import android.os.Handler;
//        import android.text.TextUtils;
//        import android.util.Log;
//        import android.util.Patterns;
//        import android.view.LayoutInflater;
//        import android.view.View;
//        import android.view.ViewGroup;
//        import android.widget.Button;
//        import android.widget.ProgressBar;
//        import android.widget.TextView;
//        import android.widget.Toast;
//
//        import androidx.fragment.app.Fragment;
//
//        import com.dadash.sfcsnotes.R;
//        import com.dadash.sfcsnotes.login;
//        import com.google.android.material.textfield.TextInputEditText;
//        import com.google.firebase.auth.AuthCredential;
//        import com.google.firebase.auth.EmailAuthProvider;
//        import com.google.firebase.auth.FirebaseAuth;
//        import com.google.firebase.auth.FirebaseUser;
//        import com.google.firebase.firestore.FirebaseFirestore;
//
//        import java.util.HashMap;
//        import java.util.regex.Pattern;
//
//public class profile_email_change extends Fragment {
//
//    private TextInputEditText newEmailField, passwordField;
//    private TextView countdownTextView;
//    private Button confirmButton;
//    private ProgressBar progressBar;
//    private FirebaseAuth firebaseAuth;
//    private FirebaseFirestore firestore;
//
//    private static final String TAG = "EMAILC";
//    private CountDownTimer countDownTimer;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_profile_email_change, container, false);
//
//        newEmailField = view.findViewById(R.id.new_email);
//        passwordField = view.findViewById(R.id.password);
//        confirmButton = view.findViewById(R.id.confirm_email_change_button);
//        progressBar = view.findViewById(R.id.ChangeEmail_progressBar);
//        countdownTextView = view.findViewById(R.id.countdownTextView);
//
//        firebaseAuth = FirebaseAuth.getInstance();
//        firestore = FirebaseFirestore.getInstance();
//
//        confirmButton.setOnClickListener(v -> handleChangeEmail());
//
//        logDebug("Fragment initialized successfully.");
//        return view;
//    }
//
//    private void handleChangeEmail() {
//        String newEmail = newEmailField.getText().toString().trim();
//        String password = passwordField.getText().toString().trim();
//
//        logDebug("Email change requested. New email: " + newEmail);
//
//        if (!isValidEmail(newEmail)) {
//            showToast("Please enter a valid email address.");
//            logDebug("Invalid email format entered.");
//            return;
//        }
//
//        if (TextUtils.isEmpty(password)) {
//            showToast("Please enter your password.");
//            logDebug("Password field is empty.");
//            return;
//        }
//
//        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//        if (currentUser == null) {
//            showToast("User not logged in.");
//            logError("Current user is null.");
//            return;
//        }
//
//        String oldEmail = currentUser.getEmail();
//        if (oldEmail == null) {
//            showToast("Failed to retrieve current email.");
//            logError("Failed to fetch old email.");
//            return;
//        }
//        if (oldEmail.equals(newEmail)){
//            showToast("New Email cannot not be same.");
//            logError("old==new");
//            return;
//        }
//
//        logDebug("Current email: " + oldEmail);
//
//        setFragmentInteractable(false);
//        progressBar.setVisibility(View.VISIBLE);
//
//        firebaseAuth.fetchSignInMethodsForEmail(newEmail)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().getSignInMethods().isEmpty()) {
//                        progressBar.setVisibility(View.GONE);
//                        setFragmentInteractable(true);
//                        showToast("This email is already in use. Please use a different email address.");
//                        logDebug("Email is already in use: " + newEmail);
//                    } else {
//                        checkEmailChangeAllowed(currentUser, oldEmail, newEmail, password);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    progressBar.setVisibility(View.GONE);
//                    setFragmentInteractable(true);
//                    showToast("Failed to check email availability: " + e.getMessage());
//                    logError("Failed to fetch sign-in methods: ", e);
//                });
//    }
//
//    private void checkEmailChangeAllowed(FirebaseUser currentUser, String oldEmail, String newEmail, String password) {
//        logDebug("Checking if email change is allowed for: " + oldEmail);
//
//        firestore.collection("User Email Changed")
//                .document(oldEmail)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists() && Boolean.TRUE.equals(documentSnapshot.getBoolean("Email Changed"))) {
//                        progressBar.setVisibility(View.GONE);
//                        setFragmentInteractable(true);
//                        showToast("You can only change your email address once.");
//                        logDebug("User has already changed their email.");
//                    } else {
//                        authenticateAndChangeEmail(currentUser, oldEmail, newEmail, password);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    progressBar.setVisibility(View.GONE);
//                    setFragmentInteractable(true);
//                    showToast("Error checking email change status: " + e.getMessage());
//                    logError("Failed to check email change status: ", e);
//                });
//    }
//
//    private void authenticateAndChangeEmail(FirebaseUser currentUser, String oldEmail, String newEmail, String password) {
//        logDebug("Authenticating user for email change...");
//
//        AuthCredential credential = EmailAuthProvider.getCredential(oldEmail, password);
//        currentUser.reauthenticate(credential)
//                .addOnCompleteListener(reauthTask -> {
//                    if (reauthTask.isSuccessful()) {
//                        logDebug("User reauthenticated successfully.");
//                        currentUser.verifyBeforeUpdateEmail(newEmail)
//                                .addOnCompleteListener(verificationTask -> {
//                                    if (verificationTask.isSuccessful()) {
//                                        showToast("Verification email sent to " + newEmail + ". Please verify within 2 minutes.");
//                                        logDebug("Verification email sent to: " + newEmail);
//                                        startCountdownTimer(currentUser, oldEmail, newEmail);
//                                    } else {
//                                        progressBar.setVisibility(View.GONE);
//                                        setFragmentInteractable(true);
//                                        showToast("Failed to send verification email: " + verificationTask.getException().getMessage());
//                                        logError("Failed to send verification email: ", verificationTask.getException());
//                                    }
//                                });
//                    } else {
//                        progressBar.setVisibility(View.GONE);
//                        setFragmentInteractable(true);
//                        showToast("Password authentication failed: " + reauthTask.getException().getMessage());
//                        logError("Reauthentication failed: ", reauthTask.getException());
//                    }
//                });
//    }
//
//    private void startCountdownTimer(FirebaseUser currentUser, String oldEmail, String newEmail) {
//        logDebug("Starting verification countdown timer...");
//
//        int delayDuration = 2 * 60 * 1000; // 2 minutes
//        countdownTextView.setVisibility(View.VISIBLE);
//
//        countDownTimer = new CountDownTimer(delayDuration, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                countdownTextView.setText("Time left: " + millisUntilFinished / 1000 + " seconds");
//            }
//
//            @Override
//            public void onFinish() {
//                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//                if (currentUser != null && currentUser.isEmailVerified()) {
//                    logDebug("Email verified and user is logged in.");
//                    updateEmailChangeRecords(oldEmail, newEmail);
//                } else {
//                    logError("Email not verified or user logged out.");
//                    stopAllTasks();
//                    showToast("Email verification failed or user is logged out.");
//                }
//            }
//        }.start();
//
//        new Handler().postDelayed(() -> checkAndUpdateVerificationStatus(currentUser, oldEmail, newEmail), delayDuration);
//    }
//
//    private void checkAndUpdateVerificationStatus(FirebaseUser currentUser, String oldEmail, String newEmail) {
//        currentUser.reload().addOnCompleteListener(reloadTask -> {
//            if (reloadTask.isSuccessful() && currentUser.isEmailVerified()) {
//                logDebug("Email verified successfully.");
//                updateEmailChangeRecords(oldEmail, newEmail);
//            } else {
//                progressBar.setVisibility(View.GONE);
//                countdownTextView.setVisibility(View.GONE);
//                setFragmentInteractable(true);
//                showToast("Email verification not completed. Please verify and try again.");
//                logError("Email verification failed.");
//            }
//        });
//    }
//
//    private void updateEmailChangeRecords(String oldEmail, String newEmail) {
//        logDebug("Updating email change records...");
//
//        HashMap<String, String> emailData = new HashMap<>();
//        emailData.put("Old Email", oldEmail);
//        emailData.put("New Email", newEmail);
//
//        firestore.collection("User Updated Emails").document(oldEmail)
//                .set(emailData)
//                .addOnSuccessListener(aVoid -> {
//                    firestore.collection("User Email Changed")
//                            .document(newEmail)
//                            .set(new HashMap<String, Object>() {{
//                                put("Email Changed", true);
//                            }})
//                            .addOnCompleteListener(task -> {
//                                if (task.isSuccessful()) {
//                                    logDebug("Email change records updated successfully.");
//                                    progressBar.setVisibility(View.GONE);
//                                    countdownTextView.setVisibility(View.GONE);
//                                    setFragmentInteractable(true);
//                                    logoutAndRedirectToLogin();
//                                } else {
//                                    handleFirestoreError(task.getException());
//                                }
//                            });
//                })
//                .addOnFailureListener(this::handleFirestoreError);
//    }
//
//    private void handleFirestoreError(Exception e) {
//        progressBar.setVisibility(View.GONE);
//        countdownTextView.setVisibility(View.GONE);
//        setFragmentInteractable(true);
//        showToast("Failed to update email records: " + e.getMessage());
//        logError("Firestore operation failed: ", e);
//    }
//
//    private void logoutAndRedirectToLogin() {
//        logDebug("Logging out and redirecting to login...");
//        firebaseAuth.signOut();
//        Intent intent = new Intent(requireContext(), login.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        requireActivity().finish();
//        showToast("Email updated successfully. Please log in again.");
//    }
//
//    private boolean isValidEmail(String email) {
//        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._-]+@(gmail\\.com|outlook\\.com|yahoo\\.com|hotmail\\.com|icloud\\.com|aol\\.com|protonmail\\.com|zoho\\.com|gmx\\.com|yandex\\.com)$");
//        return emailPattern.matcher(email).matches() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
//    }
//
//    private void setFragmentInteractable(boolean interactable) {
//        confirmButton.setEnabled(interactable);
//        newEmailField.setEnabled(interactable);
//        passwordField.setEnabled(interactable);
//    }
//
//    private void stopAllTasks() {
//        if (countDownTimer != null) {
//            countDownTimer.cancel();
//        }
//        progressBar.setVisibility(View.GONE);
//        countdownTextView.setVisibility(View.GONE);
//        setFragmentInteractable(true);
//        logDebug("All ongoing tasks stopped.");
//    }
//
//    private void showToast(String message) {
//        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
//    }
//
//    private void logDebug(String message) {
//        Log.d(TAG, message);
//    }
//
//    private void logError(String message, Exception e) {
//        Log.e(TAG, message, e);
//    }
//
//    private void logError(String message) {
//        Log.e(TAG, message);
//    }
//}
