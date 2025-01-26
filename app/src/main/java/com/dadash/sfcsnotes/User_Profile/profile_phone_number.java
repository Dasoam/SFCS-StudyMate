package com.dadash.sfcsnotes.User_Profile;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.dadash.sfcsnotes.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
public class profile_phone_number extends Fragment {
    private static final String TAG = "ProfilePhoneNumber";
    private TextInputEditText newPhoneInput, passwordInput;
    private Button confirmButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_phone_number, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        newPhoneInput = view.findViewById(R.id.new_phone);
        passwordInput = view.findViewById(R.id.password);
        confirmButton = view.findViewById(R.id.confirm_phone_change_button);
        progressBar = view.findViewById(R.id.ChangePhone_progressBar);
        confirmButton.setOnClickListener(v -> validateAndSavePhone());
        return view;
    }
    private void validateAndSavePhone() {
        String newPhone = newPhoneInput.getText().toString().trim();
        String enteredPassword = passwordInput.getText().toString().trim();
        if (TextUtils.isEmpty(newPhone)) {
            showToast("Please enter your new phone number.");
            return;
        }
        if (newPhone.length() != 10 || !newPhone.matches("\\d{10}")) {
            showToast("Phone number not valid.");
            return;
        }
        if (TextUtils.isEmpty(enteredPassword)) {
            showToast("Please enter your password.");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        confirmButton.setEnabled(false);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            showToast("User not authenticated. Please log in again.");
            progressBar.setVisibility(View.GONE);
            confirmButton.setEnabled(true);
            return;
        }
        reAuthenticateUser(user, enteredPassword, newPhone);
    }
    private void reAuthenticateUser(FirebaseUser user, String password, String newPhone) {
        if (user.getEmail() == null) {
            showToast("Cannot re-authenticate. User email not found.");
            progressBar.setVisibility(View.GONE);
            confirmButton.setEnabled(true);
            return;
        }
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        savePhoneNumberToFirestore(user, newPhone);
                    } else {
                        showToast("Incorrect password. Please try again.");
                        progressBar.setVisibility(View.GONE);
                        confirmButton.setEnabled(true);
                    }
                });
    }
    private void savePhoneNumberToFirestore(FirebaseUser user, String newPhone) {
        String email = user.getEmail();
        if (email == null) {
            showToast("Error saving phone number. User email not found.");
            progressBar.setVisibility(View.GONE);
            confirmButton.setEnabled(true);
            return;
        }
        String userName = user.getDisplayName() != null ? user.getDisplayName() : "Unknown User";
        String addedOn = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("User Name", userName);
        userData.put("Email", email);
        userData.put("Phone Number", newPhone);
        userData.put("Added on", addedOn);
        userData.put("Password Used", "Password Used");
        firestore.collection("User's Phone Number").document(email)
                .set(userData)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    confirmButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        showToast("Phone number updated successfully.");
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        showToast("Failed to save phone number in database. Try again later.");
                    }
                });
    }
    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
