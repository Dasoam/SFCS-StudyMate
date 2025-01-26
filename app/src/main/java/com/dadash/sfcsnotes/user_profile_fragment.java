package com.dadash.sfcsnotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dadash.sfcsnotes.User_Profile.profile_about_us;
import com.dadash.sfcsnotes.User_Profile.profile_change_password;
import com.dadash.sfcsnotes.User_Profile.profile_email_change;
import com.dadash.sfcsnotes.User_Profile.profile_phone_number;
import com.dadash.sfcsnotes.User_Profile.profile_pic_Selection;
import com.dadash.sfcsnotes.User_Profile.profile_upload_id;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class user_profile_fragment extends Fragment {
    //    private InterstitialAd mInterstitialAd;
    private Fragment profile_pic_Selection = new profile_pic_Selection();
    private Fragment profile_change_password = new profile_change_password();

    private Fragment profile_change_email = new profile_email_change();
    private Fragment profile_change_phone_number = new profile_phone_number();
    private Fragment profile_about_us = new profile_about_us();
    private Fragment profile_upload_id = new profile_upload_id();
    private FirebaseAuth firebaseAuth;
    private TextView userName, userEmail, versionName;
    private ImageView profilePic, verifyIcon;
    FirebaseFirestore firestore;
    private String cachedProfileId = null; // Cache to store the loaded profile ID
    private static final String PREFS_NAME = "UserProfilePrefs";
    private static final String KEY_PROFILE_ID = "ProfileID";
    private static final String KEY_USER_NAME = "UserName";
    private static final String KEY_USER_EMAIL = "UserEmail";
    private static final String LAST_VERIFICATION_TIME = "lastVerificationTime";

    public user_profile_fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setUserProfile(rootView);
//        setVersionName(rootView);

        // Initialize the interstitial ad
//        loadInterstitialAd();
        if (checkEmailVerification()) {
            rootView.findViewById(R.id.option_verify_account).setVisibility(View.GONE);
        }

        // Profile edit option
        rootView.findViewById(R.id.edit_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mInterstitialAd != null) {
//                    mInterstitialAd.show(requireActivity());
//                } else {
//                    // If the ad is not ready, navigate directly to the profile_pic_Selection fragment
////                    navigateToEditProfile();
//                }
                navigateToEditProfile();
            }
        });


        // Change Email option
        rootView.findViewById(R.id.option_change_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open change email screen
                navigateToChangeEmail();
            }
        });

        // Change Password option
        rootView.findViewById(R.id.option_change_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open change password screen
                navigateToChangePassword();
            }
        });

        // Verify Account option
        rootView.findViewById(R.id.option_verify_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open account verification screen
                navigateToVerifyAccount();
            }
        });

        // Add/Change Phone Number option
        rootView.findViewById(R.id.option_add_change_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open phone number change screen
                navigateToChangePhoneNumber();
            }
        });

        // Upload ID Card option
        rootView.findViewById(R.id.option_upload_id_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open upload ID card screen
                navigateToUploadIdCard();
            }
        });

        // Downloads option
        rootView.findViewById(R.id.option_downloads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open downloads screen
                navigateToDownloads();
            }
        });

        // Rate Us option
        rootView.findViewById(R.id.option_rate_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open rate us screen
                navigateToRateUs();
            }
        });

        // Other Apps by Us option
        rootView.findViewById(R.id.option_other_apps_by_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open other apps by us
                navigateToOtherApps();
            }
        });

        // Support option
        rootView.findViewById(R.id.option_support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open support screen
                navigateToSupport();
            }
        });

        // About Us option
        rootView.findViewById(R.id.option_about_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open about us screen
                navigateToAboutUs();
            }
        });

        // Deactivate Account option
        rootView.findViewById(R.id.option_deactivate_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Confirm account deactivation
                confirmDeactivateAccount();
            }
        });

        // Delete Account option
        rootView.findViewById(R.id.option_delete_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Confirm account deletion
                confirmDeleteAccount();
            }
        });

        // Logout button
        rootView.findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout logic
                logoutUser();
            }
        });

        return rootView;
    }

    private void setUserProfile(View rootView) {
        userName = rootView.findViewById(R.id.profile_name);
        userEmail = rootView.findViewById(R.id.profile_email);
        verifyIcon = rootView.findViewById(R.id.verify_icon);
        profilePic = rootView.findViewById(R.id.profile_image);

        // Retrieve cached username and email if available
        String cachedUserName = getCachedUserName();
        String cachedUserEmail = getCachedUserEmail();

        if (cachedUserEmail != null) {
            Log.d("CACHED", "Cached user email: " + cachedUserEmail);
        } else {
            Log.d("CACHED", "Cached user email is null");
        }
        // If cached username and email exist, use them; otherwise, get them from Firebase
        String UserName = cachedUserName != null ? cachedUserName : firebaseAuth.getCurrentUser().getDisplayName();
        String UserEmail = cachedUserEmail != null ? cachedUserEmail : firebaseAuth.getCurrentUser().getEmail();

        // Set the username and email fields
        if (TextUtils.isEmpty(UserName)) {
            userName.setText("User Name");
        } else {
            userName.setText(UserName);
        }

        if (UserEmail != null) {
            userEmail.setText(UserEmail);

            // Check if cached profile picture exists
            cachedProfileId = getCachedProfileId();
            if (cachedProfileId != null) {
                // Load avatar from cache if available
                Log.d("CACHED", "Loading profile picture from cache.");
                loadAvatarFromCache(cachedProfileId);
            } else {
                // Fetch avatar from Firestore if not cached
                Log.d("CACHED", "No cached profile picture found. Fetching from Firestore.");
                fetchProfileFromFirestore(UserEmail, UserName);
            }

            // Save the username and email to local storage only if they have changed
            if (!UserEmail.equals(getCachedUserEmail()) || !UserName.equals(getCachedUserName())) {
                saveUserToCache(UserEmail, UserName);
                Log.d("CACHED", "User details saved to SharedPreferences: " + UserName + ", " + UserEmail);
            }
        } else {
            userEmail.setText("No Email Found");
            profilePic.setImageResource(R.drawable.avatar_default2);
        }

        // Check if the user's email is verified and show verification icon
        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {
            verifyIcon.setVisibility(View.VISIBLE);
        }
    }


    private void fetchProfileFromFirestore(String userEmail, String userName) {
        DocumentReference docRef = firestore.collection("User's Profile Picture").document(userEmail);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Retrieve the "Profile ID" field
                String profileId = task.getResult().getString("Profile ID");
                if (profileId != null) {
                    Log.d("CACHED", "Profile ID found in Firestore: " + profileId);
                    cachedProfileId = profileId; // Cache the Profile ID
                    saveProfileToCache(profileId, userEmail, userName); // Save to SharedPreferences
                    loadAvatarFromCache(profileId);
                } else {
                    // If no Profile ID is found in Firestore, set the default avatar and cache it
                    Log.d("CACHED", "No Profile ID in Firestore. Using default avatar.");
                    profilePic.setImageResource(R.drawable.avatar_default2);
                    saveProfileToCache("avatar_default2", userEmail, userName); // Save default avatar in cache
                }
            } else {
                // If the user document doesn't exist in Firestore, set the default avatar and cache it
                Log.d("CACHED", "No user document in Firestore. Using default avatar.");
                profilePic.setImageResource(R.drawable.avatar_default2);
                saveProfileToCache("avatar_default2", userEmail, userName); // Save default avatar in cache
            }
        });
    }


    private void loadAvatarFromCache(String profileId) {
        int profilePictureResId = getResources().getIdentifier(profileId, "drawable", requireContext().getPackageName());
        if (profilePictureResId != 0) {
            Log.d("CACHED", "Loading profile picture from cache: " + profileId);
            profilePic.setImageResource(profilePictureResId);
        } else {
            Log.d("CACHED", "Profile picture not found in cache. Using default avatar.");
            profilePic.setImageResource(R.drawable.avatar_default2);
        }
    }

    private String getCachedProfileId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String profileId = sharedPreferences.getString(KEY_PROFILE_ID, null); // Return null if not cached
        if (profileId != null) {
            Log.d("CACHED", "Profile ID loaded from SharedPreferences: " + profileId);
        }
        return profileId;
    }

    private void saveProfileToCache(String profileId, String userEmail, String userName) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_ID, profileId); // Save Profile ID
        editor.putString(KEY_USER_NAME, userName);   // Save UserName
        editor.putString(KEY_USER_EMAIL, userEmail); // Save UserEmail
        editor.apply();
        Log.d("CACHED", "Profile saved to SharedPreferences: " + profileId);
    }

    private void saveUserToCache(String userEmail, String userName) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save user details to SharedPreferences if they are not already saved
        editor.putString(KEY_USER_NAME, userName);    // Save UserName
        editor.putString(KEY_USER_EMAIL, userEmail);  // Save UserEmail
        editor.apply();
        Log.d("CACHED", "User details saved to SharedPreferences: " + userName + ", " + userEmail);
    }

    private String getCachedUserName() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cachedUserName = sharedPreferences.getString(KEY_USER_NAME, null);
        if (cachedUserName != null) {
            Log.d("CACHED", "UserName loaded from SharedPreferences: " + cachedUserName);
        }
        return cachedUserName;
    }

    private String getCachedUserEmail() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cachedUserEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);
        if (cachedUserEmail != null) {
            Log.d("CACHED", "UserEmail loaded from SharedPreferences: " + cachedUserEmail);
        }
        return cachedUserEmail;
    }


// Define each of the methods that handle navigation or actions

    private void navigateToEditProfile() {
        // Use openFragment method to navigate to the profilePicSelectionFragment
        openFragment(profile_pic_Selection);

    }

    private void navigateToChangeEmail() {
        openFragment(profile_change_email);
        // Create an AlertDialog to get the new email from the user
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("Change Email");
//
//        // Create an EditText for the new email
//        EditText emailInput = new EditText(getContext());
//        emailInput.setHint("Enter new email");
//        emailInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
//
//        builder.setView(emailInput);
//
//        builder.setPositiveButton("Update", (dialog, which) -> {
//            String newEmail = emailInput.getText().toString().trim();
//
//            if (isValidEmail(newEmail)) {
//                updateEmail(newEmail);
//            } else {
//                Toast.makeText(getContext(), "Please enter a valid email.", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
//
//        builder.show();
    }

    // Method to check if email is valid
//    private boolean isValidEmail(String email) {
//        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
//    }
//
//    // Method to update the email
//    private void updateEmail(String newEmail) {
//        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//        if (currentUser != null) {
//            // Re-authenticate the user before updating the email
//            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), "123456@Aa"); // Replace with actual password
//            currentUser.reauthenticate(credential)
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            // Send verification email to the new email
//                            currentUser.updateEmail(newEmail)
//                                    .addOnCompleteListener(emailUpdateTask -> {
//                                        if (emailUpdateTask.isSuccessful()) {
//                                            currentUser.sendEmailVerification()
//                                                    .addOnCompleteListener(verificationTask -> {
//                                                        if (verificationTask.isSuccessful()) {
//                                                            // Email verification sent successfully
//                                                            Toast.makeText(getContext(), "Verification email sent to " + newEmail, Toast.LENGTH_SHORT).show();
//                                                        } else {
//                                                            Log.d("UPDATEEMAIL",emailUpdateTask.getException().getMessage());
//                                                            Toast.makeText(getContext(), "Failed to send verification email: " + verificationTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                                        }
//                                                    });
//                                        } else {
//                                            Toast.makeText(getContext(), "Failed to update email in Firebase Authentication: " + emailUpdateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                        } else {
//                            Toast.makeText(getContext(), "Reauthentication failed. Please log in again.", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        } else {
//            Toast.makeText(getContext(), "No user logged in.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//
//
//
//    // Method to update the email in Firestore
//    private void updateFirestoreEmail(String newEmail) {
//        // Assuming the Firestore document ID is the user email
//        DocumentReference userDocRef = firestore.collection("temp").document(firebaseAuth.getCurrentUser().getEmail());
//
//        userDocRef.update("email", newEmail)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Update local cache with the new email
//                        updateLocalEmail(newEmail);
//                    } else {
//                        Toast.makeText(getContext(), "Failed to update email in Firestore.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    // Method to update the email in local SharedPreferences
//    private void updateLocalEmail(String newEmail) {
//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(KEY_USER_EMAIL, newEmail);
//        editor.apply();
//
//        // Update the displayed email in the profile
//        userEmail.setText(newEmail);
//        Toast.makeText(getContext(), "Email updated successfully.", Toast.LENGTH_SHORT).show();
//    }


    private void navigateToChangePassword() {
        // Your navigation logic for changing password
        openFragment(profile_change_password);
    }

    private void navigateToVerifyAccount() {
        // Your navigation logic for verifying account

        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Get the last verification time from SharedPreferences
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            long lastVerificationTime = sharedPreferences.getLong(LAST_VERIFICATION_TIME, 0);

            // Get the current time
            long currentTime = System.currentTimeMillis();

            // Check if it's been less than 10 minutes (600,000 milliseconds) since the last email
            if (currentTime - lastVerificationTime < 600000) {
                long remainingTime = 600000 - (currentTime - lastVerificationTime);
                long remainingMinutes = remainingTime / 60000; // Convert milliseconds to minutes
                Toast.makeText(requireContext(), "You can send the verification email again in " + remainingMinutes + " minutes.", Toast.LENGTH_LONG).show();
            } else {
                // Send verification email
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Update the last verification time in SharedPreferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putLong(LAST_VERIFICATION_TIME, currentTime);
                                editor.apply();

                                // Verification email sent successfully
                                Toast.makeText(requireContext(), "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                            } else {
                                // Handle failure
                                Toast.makeText(requireContext(), "Failed to send verification email. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        } else {
            // If no user is logged in
            Toast.makeText(requireContext(), "No user logged in. Please sign in to verify your account.", Toast.LENGTH_LONG).show();
        }

    }

    private void navigateToChangePhoneNumber() {
        openFragment(profile_change_phone_number);
    }
//        // Create an AlertDialog to take new phone number input from the user
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("Change Phone Number");
//
//        // Set up the input field for phone number
//        final EditText phoneNumberInput = new EditText(getContext());
//        phoneNumberInput.setHint("Enter new phone number");
//        builder.setView(phoneNumberInput);
//
//        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String newPhoneNumber = phoneNumberInput.getText().toString().trim();
//
//                if (TextUtils.isEmpty(newPhoneNumber)) {
//                    Toast.makeText(getContext(), "Phone number cannot be empty", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                // Now, update the phone number in Firebase Authentication
//                updatePhoneNumber(newPhoneNumber);
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        builder.show();
//    }
//
//    private void updatePhoneNumber(final String newPhoneNumber) {
//        FirebaseUser user = firebaseAuth.getCurrentUser();
//
//        if (user == null) {
//            Toast.makeText(getContext(), "No user logged in.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Re-authenticate the user before updating the phone number
//        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), "123456@Aa"); // Replace with actual password
//        user.reauthenticate(credential)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            // Now update the phone number
//                            PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                                    newPhoneNumber,
//                                    60, // Timeout duration
//                                    TimeUnit.SECONDS,
//                                    getActivity(), // Activity context
//                                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                                        @Override
//                                        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
//                                            // Once verification is completed, sign in with the credential
//                                            user.updatePhoneNumber(phoneAuthCredential)
//                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                        @Override
//                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                            if (task.isSuccessful()) {
//                                                                Log.d("PHONE", "Phone number updated successfully.");
//                                                                Toast.makeText(getContext(), "Phone number updated", Toast.LENGTH_SHORT).show();
//                                                            } else {
//                                                                Log.e("PHONE", "Error updating phone number: " + task.getException().getMessage());
//                                                                Toast.makeText(getContext(), "Failed to update phone number: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                                            }
//                                                        }
//                                                    });
//                                        }
//
//                                        @Override
//                                        public void onVerificationFailed(FirebaseException e) {
//                                            Log.e("PHONE", "Phone number verification failed: " + e.getMessage());
//                                            Toast.makeText(getContext(), "Phone number verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//
//                                        @Override
//                                        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                                            // You can handle the verification code being sent here, if needed.
//                                            Log.d("PHONE", "Verification code sent to phone.");
//                                        }
//                                    });
//                        } else {
//                            Log.e("REAUTH", "Reauthentication failed: " + task.getException().getMessage());
//                            Toast.makeText(getContext(), "Reauthentication failed. Please log in again.", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }


    private void navigateToUploadIdCard() {
        // Your navigation logic for uploading ID card
        openFragment(profile_upload_id);
    }

    private void navigateToDownloads() {
        // Your navigation logic for opening downloads
        Toast.makeText(requireContext(), "Our team is working on it.", Toast.LENGTH_SHORT).show();
    }

    private void navigateToRateUs() {
        // Your navigation logic for rating the app
        openExternalLink(getString(R.string.rate_us));
    }

    private void navigateToOtherApps() {
        // Your navigation logic for opening other apps by you
        navigateToRateUs();
    }

    private void navigateToSupport() {
        // Your navigation logic for support
        openExternalLink(getString(R.string.support_form));
    }

    private void navigateToAboutUs() {
        // Your navigation logic for about us
        openFragment(profile_about_us);
    }

    private void confirmDeactivateAccount() {
        // Your confirmation logic for deactivating the account
        // Open the external link in a new thread to avoid blocking
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                openExternalLink(getString(R.string.deactivate_account_form));
//            }
//        }).start();
        logoutUser();
        openExternalLink(getString(R.string.deactivate_account_form));
    }

    //    private void confirmDeleteAccount() {
//        // Create an AlertDialog to confirm account deletion
//        new AlertDialog.Builder(requireContext())
//                .setTitle("Confirm Account Deletion")
//                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
//                .setPositiveButton("Yes", (dialog, which) -> {
//                    // If the user confirms, proceed with deleting the account
//                    firebaseAuth.getCurrentUser().delete()
//                            .addOnCompleteListener(task -> {
//                                if (task.isSuccessful()) {
//                                    Log.d("DELETEACCOUNT", "User account deleted.");
//                                    // Optionally, you can show a toast or navigate the user to another screen
//                                    Toast.makeText(requireContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
//                                } else {
//                                    // Handle failure, if needed
//                                    Toast.makeText(requireContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                })
//                .setNegativeButton("No", (dialog, which) -> {
//                    // If the user cancels, do nothing
//                    dialog.dismiss();
//                })
//                .show();
//    }
    private void confirmDeleteAccount() {
        // Inflate custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_confirm_delete, null);

        // Get reference to the password EditText
        EditText passwordInput = dialogView.findViewById(R.id.alert_password_input);

        // Create an AlertDialog to confirm account deletion with password input
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Account Deletion")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setView(dialogView)
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Get password entered by the user
                    String password = passwordInput.getText().toString().trim();

                    if (password.isEmpty()) {
                        Toast.makeText(requireContext(), "Password cannot be empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Re-authenticate the user before deleting the account
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseAuth.getCurrentUser().getEmail(), password);

                    firebaseAuth.getCurrentUser().reauthenticate(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Password validated, proceed with account deletion
                                    deleteAccount();
                                } else {
                                    // Password incorrect, show error
                                    Toast.makeText(requireContext(), "Incorrect password. Try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // If the user cancels, do nothing
                    dialog.dismiss();
                })
                .show();
    }

    private void deleteAccount() {
        // Delete user account from Firebase
        firebaseAuth.getCurrentUser().delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("DELETEACCOUNT", "User account deleted.");

                        // Optionally, remove all associated data from SharedPreferences or local storage
                        clearUserData();
                        Toast.makeText(requireContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();                        // Navigate the user to the login activity
                        Intent intent = new Intent(requireContext(), login_register_option.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();  // Close the current activity
                    } else {
                        // Handle failure, if needed
                        Toast.makeText(requireContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearUserData() {
        // Clear all data stored in SharedPreferences
//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//
////        // Remove the specific keys for user data
////        editor.remove(KEY_PROFILE_ID); // Remove Profile ID
////        editor.remove(KEY_USER_NAME);  // Remove User Name
////        editor.remove(KEY_USER_EMAIL); // Remove User Email
//
//        // Clear all data (remove all keys and values)
//        editor.clear();
//
//        // Apply changes
//        editor.apply();
        clearUserData.clearAllData(requireContext()); // Clear SharedPreferences
        clearUserData.clearFirestoreCache(requireContext(),firestore);                        // Clear Firestore cache
        clearUserData.deleteLocalFiles(requireContext());
    }


    private void logoutUser() {
        // Your logout logic here
        // Sign out the user from Firebase
        clearUserData();

        firebaseAuth.signOut();

        // Navigate to the login/register screen
        Intent intent = new Intent(requireContext(), login_register_option.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Start the new activity
        requireActivity().startActivity(intent);
        // Finish the current activity
        requireActivity().finish();
    }

    private void openFragment(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment) // Replace the fragment
                .addToBackStack(null) // Add to back stack to allow user to navigate back
                .commit();
    }

    private boolean checkEmailVerification() {
        return firebaseAuth.getCurrentUser().isEmailVerified();
    }
    private void openExternalLink(String url){

        // Create the URI from the string
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        // Start the activity to open the Play Store page
        requireContext().startActivity(intent);
    }
//    private void loadInterstitialAd() {
//        AdRequest adRequest = new AdRequest.Builder().build();
//
//        InterstitialAd.load(
//                requireContext(),
//                getString(R.string.sample_Interstitial), // Replace with your actual AdMob ad unit ID
//                adRequest,
//                new InterstitialAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(InterstitialAd interstitialAd) {
//                        mInterstitialAd = interstitialAd;
//
//                        // Set full-screen content callback to navigate after ad closes
//                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                            @Override
//                            public void onAdDismissedFullScreenContent() {
//                                // Navigate to the fragment when the ad is dismissed
////                                navigateToEditProfile();
//                            }
//
//                            @Override
//                            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
//                                super.onAdFailedToShowFullScreenContent(adError);
//                                // Handle failure gracefully and navigate directly
////                                navigateToEditProfile();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError loadAdError) {
//                        mInterstitialAd = null; // Set to null if the ad fails to load
//                    }
//                }
//        );
//    }
//    private void setVersionName(View rootView){
//        versionName  = rootView.findViewById(R.id.version_textview);
//        String version = BuildConfig.VERSION_NAME;
//        Log.d("version",version);// Or any other dynamic version you want to use
//        versionName.setText("Version: " + version + " ");
//    }
}