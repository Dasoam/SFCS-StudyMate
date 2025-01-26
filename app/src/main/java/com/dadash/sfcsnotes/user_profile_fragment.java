package com.dadash.sfcsnotes;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
public class user_profile_fragment extends Fragment {
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
    private String cachedProfileId = null;
    private static final String PREFS_NAME = "UserProfilePrefs";
    private static final String KEY_PROFILE_ID = "ProfileID";
    private static final String KEY_USER_NAME = "UserName";
    private static final String KEY_USER_EMAIL = "UserEmail";
    private static final String LAST_VERIFICATION_TIME = "lastVerificationTime";
    public user_profile_fragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        setUserProfile(rootView);
        if (checkEmailVerification()) {
            rootView.findViewById(R.id.option_verify_account).setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.edit_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToEditProfile();
            }
        });
        rootView.findViewById(R.id.option_change_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToChangeEmail();
            }
        });
        rootView.findViewById(R.id.option_change_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToChangePassword();
            }
        });
        rootView.findViewById(R.id.option_verify_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToVerifyAccount();
            }
        });
        rootView.findViewById(R.id.option_add_change_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToChangePhoneNumber();
            }
        });
        rootView.findViewById(R.id.option_upload_id_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToUploadIdCard();
            }
        });
        rootView.findViewById(R.id.option_downloads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToDownloads();
            }
        });
        rootView.findViewById(R.id.option_rate_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRateUs();
            }
        });
        rootView.findViewById(R.id.option_other_apps_by_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToOtherApps();
            }
        });
        rootView.findViewById(R.id.option_support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSupport();
            }
        });
        rootView.findViewById(R.id.option_about_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAboutUs();
            }
        });
        rootView.findViewById(R.id.option_deactivate_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeactivateAccount();
            }
        });
        rootView.findViewById(R.id.option_delete_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteAccount();
            }
        });
        rootView.findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        String cachedUserName = getCachedUserName();
        String cachedUserEmail = getCachedUserEmail();
        if (cachedUserEmail != null) {
        } else {
        }
        String UserName = cachedUserName != null ? cachedUserName : firebaseAuth.getCurrentUser().getDisplayName();
        String UserEmail = cachedUserEmail != null ? cachedUserEmail : firebaseAuth.getCurrentUser().getEmail();
        if (TextUtils.isEmpty(UserName)) {
            userName.setText("User Name");
        } else {
            userName.setText(UserName);
        }
        if (UserEmail != null) {
            userEmail.setText(UserEmail);
            cachedProfileId = getCachedProfileId();
            if (cachedProfileId != null) {
                loadAvatarFromCache(cachedProfileId);
            } else {
                fetchProfileFromFirestore(UserEmail, UserName);
            }
            if (!UserEmail.equals(getCachedUserEmail()) || !UserName.equals(getCachedUserName())) {
                saveUserToCache(UserEmail, UserName);
            }
        } else {
            userEmail.setText("No Email Found");
            profilePic.setImageResource(R.drawable.profile_pic_default);
        }
        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {
            verifyIcon.setVisibility(View.VISIBLE);
        }
    }
    private void fetchProfileFromFirestore(String userEmail, String userName) {
        DocumentReference docRef = firestore.collection("User's Profile Picture").document(userEmail);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String profileId = task.getResult().getString("Profile ID");
                if (profileId != null) {
                    cachedProfileId = profileId;
                    saveProfileToCache(profileId, userEmail, userName);
                    loadAvatarFromCache(profileId);
                } else {
                    profilePic.setImageResource(R.drawable.profile_pic_default);
                    saveProfileToCache("avatar_default2", userEmail, userName);
                }
            } else {
                profilePic.setImageResource(R.drawable.profile_pic_default);
                saveProfileToCache("avatar_default2", userEmail, userName);
            }
        });
    }
    private void loadAvatarFromCache(String profileId) {
        int profilePictureResId = getResources().getIdentifier(profileId, "drawable", requireContext().getPackageName());
        if (profilePictureResId != 0) {
            profilePic.setImageResource(profilePictureResId);
        } else {
            profilePic.setImageResource(R.drawable.profile_pic_default);
        }
    }
    private String getCachedProfileId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String profileId = sharedPreferences.getString(KEY_PROFILE_ID, null);
        if (profileId != null) {
        }
        return profileId;
    }
    private void saveProfileToCache(String profileId, String userEmail, String userName) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_ID, profileId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.apply();
    }
    private void saveUserToCache(String userEmail, String userName) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.apply();
    }
    private String getCachedUserName() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cachedUserName = sharedPreferences.getString(KEY_USER_NAME, null);
        if (cachedUserName != null) {
        }
        return cachedUserName;
    }
    private String getCachedUserEmail() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cachedUserEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);
        if (cachedUserEmail != null) {
        }
        return cachedUserEmail;
    }
    private void navigateToEditProfile() {
        openFragment(profile_pic_Selection);
    }
    private void navigateToChangeEmail() {
        openFragment(profile_change_email);

    }
    private void navigateToChangePassword() {
        openFragment(profile_change_password);
    }
    private void navigateToVerifyAccount() {
        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            long lastVerificationTime = sharedPreferences.getLong(LAST_VERIFICATION_TIME, 0);
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastVerificationTime < 600000) {
                long remainingTime = 600000 - (currentTime - lastVerificationTime);
                long remainingMinutes = remainingTime / 60000;
                Toast.makeText(requireContext(), "You can send the verification email again in " + remainingMinutes + " minutes.", Toast.LENGTH_LONG).show();
            } else {
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putLong(LAST_VERIFICATION_TIME, currentTime);
                                editor.apply();
                                Toast.makeText(requireContext(), "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to send verification email. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        } else {
            Toast.makeText(requireContext(), "No user logged in. Please sign in to verify your account.", Toast.LENGTH_LONG).show();
        }
    }
    private void navigateToChangePhoneNumber() {
        openFragment(profile_change_phone_number);
    }
    private void navigateToUploadIdCard() {
        openFragment(profile_upload_id);
    }
    private void navigateToDownloads() {
        Toast.makeText(requireContext(), "Our team is working on it.", Toast.LENGTH_SHORT).show();
    }
    private void navigateToRateUs() {
        openExternalLink(getString(R.string.rate_us));
    }
    private void navigateToOtherApps() {
        navigateToRateUs();
    }
    private void navigateToSupport() {
        openExternalLink(getString(R.string.support_form));
    }
    private void navigateToAboutUs() {
        openFragment(profile_about_us);
    }
    private void confirmDeactivateAccount() {
        logoutUser();
        openExternalLink(getString(R.string.deactivate_account_form));
    }
    private void confirmDeleteAccount() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_confirm_delete, null);
        EditText passwordInput = dialogView.findViewById(R.id.alert_password_input);
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Account Deletion")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setView(dialogView)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String password = passwordInput.getText().toString().trim();
                    if (password.isEmpty()) {
                        Toast.makeText(requireContext(), "Password cannot be empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseAuth.getCurrentUser().getEmail(), password);
                    firebaseAuth.getCurrentUser().reauthenticate(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    deleteAccount();
                                } else {

                                    Toast.makeText(requireContext(), "Incorrect password. Try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    private void deleteAccount() {
        firebaseAuth.getCurrentUser().delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        clearUserData();
                        Toast.makeText(requireContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(requireContext(), login_register_option.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void clearUserData() {
        clearUserData.clearAllData(requireContext());
        clearUserData.clearFirestoreCache(requireContext(),firestore);
        clearUserData.deleteLocalFiles(requireContext());
    }
    private void logoutUser() {
        clearUserData();
        firebaseAuth.signOut();
        Intent intent = new Intent(requireContext(), login_register_option.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        requireActivity().startActivity(intent);
        requireActivity().finish();
    }
    private void openFragment(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
    private boolean checkEmailVerification() {
        return firebaseAuth.getCurrentUser().isEmailVerified();
    }
    private void openExternalLink(String url){
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        requireContext().startActivity(intent);
    }
}