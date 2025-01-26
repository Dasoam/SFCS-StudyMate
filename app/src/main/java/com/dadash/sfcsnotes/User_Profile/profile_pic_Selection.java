package com.dadash.sfcsnotes.User_Profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dadash.sfcsnotes.Adapters.AvatarAdapter;
import com.dadash.sfcsnotes.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class profile_pic_Selection extends Fragment {

    private RecyclerView avatarRecyclerView;
    private AvatarAdapter avatarAdapter;
    private List<String> avatarList;
    private ProgressBar progressBar;
    private Button changeProfilePicButton;
    private InterstitialAd interstitialAd;
    private String selectedAvatar;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseAnalytics firebaseAnalytics;
    public static final String PREFS_NAME = "UserProfilePrefs";
    public static final String KEY_PROFILE_ID = "ProfileID";
    private String cachedProfileId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_pic_selection, container, false);

        // Initialize Firebase and UI components
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        avatarRecyclerView = view.findViewById(R.id.rvAvatars);
        progressBar = view.findViewById(R.id.profile_pic_progressBar);
        changeProfilePicButton = view.findViewById(R.id.btnChangeProfilePic);

        // Set up RecyclerView
        avatarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        avatarList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            avatarList.add("avatar_" + i);
        }

        avatarAdapter = new AvatarAdapter(requireContext(), avatarList, selected -> selectedAvatar = selected);
        avatarRecyclerView.setAdapter(avatarAdapter);

        // Check local storage for cached profile picture
        cachedProfileId = getCachedProfileId();
        if (cachedProfileId != null) {
            selectedAvatar = cachedProfileId;
        } else {
            // Fetch from Firestore if not cached
            fetchProfileFromFirestore(firebaseAuth.getCurrentUser().getEmail());
        }

        // Load interstitial ad
        loadInterstitialAd();

        // Change profile picture button listener
        changeProfilePicButton.setOnClickListener(v -> {
            if (selectedAvatar == null) {
                Toast.makeText(getContext(), "Please select an avatar first.", Toast.LENGTH_SHORT).show();
                return;
            }
            updateProfilePicture();
        });

        return view;
    }

    // Save Profile ID to SharedPreferences
    private void saveProfileIdToCache(String profileId) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_ID, profileId);
        editor.apply();
    }

    // Get Profile ID from SharedPreferences
    private String getCachedProfileId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_PROFILE_ID, null); // Return null if not cached
    }

    // Fetch Profile from Firestore
    private void fetchProfileFromFirestore(String userEmail) {
        DocumentReference docRef = firestore.collection("User's Profile Picture").document(userEmail);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Retrieve Profile ID from Firestore
                String profileId = task.getResult().getString("Profile ID");
                if (profileId != null) {
                    cachedProfileId = profileId;
                    saveProfileIdToCache(profileId); // Save to local cache
                    selectedAvatar = profileId;
                } else {
                    selectedAvatar = "avatar_default";
                }
            } else {
                selectedAvatar = "avatar_default"; // Set default if no profile is found
            }
        });
    }

//    private void loadInterstitialAd() {
//        AdRequest adRequest = new AdRequest.Builder().build();
//        InterstitialAd.load(requireContext(), getString(R.string.sample_Interstitial), adRequest, new InterstitialAdLoadCallback() {
//            @Override
//            public void onAdLoaded(@NonNull InterstitialAd ad) {
//                interstitialAd = ad;
//                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                    @Override
//                    public void onAdDismissedFullScreenContent() {
//                        // Navigate back after the ad
//                        requireActivity().onBackPressed();
//                    }
//                });
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                interstitialAd = null;
//            }
//        });
//    }
    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(requireContext(), getString(R.string.sample_Interstitial), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                interstitialAd = ad;
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        logAdEvent("ad_shown", "Interstitial ad shown");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
//                        logAdEvent("ad_dismissed", "Interstitial ad dismissed");
                        requireActivity().onBackPressed();
                    }

//                    @Override
//                    public void onAdFailedToShowFullScreenContent() {
//                        logAdEvent("ad_failed", "Interstitial ad failed to show");
//                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                interstitialAd = null;
                logAdEvent("ad_failed_to_load", loadAdError.getMessage());
            }
        });
    }

    private void updateProfilePicture() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Firestore document path
        String email = firebaseAuth.getCurrentUser().getEmail();
        DocumentReference docRef = firestore.collection("User's Profile Picture").document(email);

        // Add user details to the map
        HashMap<String, Object> userProfileID = new HashMap<>();
        userProfileID.put("Profile ID", selectedAvatar);

        // Update or create document
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Update existing document
                docRef.update("Profile ID", selectedAvatar)
                        .addOnSuccessListener(aVoid -> onProfilePictureUpdated())
                        .addOnFailureListener(e -> onProfileUpdateFailed());
            } else {
                // Create new document
                docRef.set(userProfileID)
                        .addOnSuccessListener(aVoid -> onProfilePictureUpdated())
                        .addOnFailureListener(e -> onProfileUpdateFailed());
            }
        });
    }

    private void onProfilePictureUpdated() {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(requireContext(), "Profile picture updated successfully.", Toast.LENGTH_SHORT).show();

        // Save to local cache
        saveProfileIdToCache(selectedAvatar);

        if (interstitialAd != null) {
            interstitialAd.show(requireActivity());
        } else {
            requireActivity().onBackPressed();
        }
    }

    private void onProfileUpdateFailed() {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(requireContext(), "Failed to update profile picture. Please try again.", Toast.LENGTH_SHORT).show();
    }
    private void logAdEvent(String event, String message) {
        Bundle bundle = new Bundle();
        bundle.putString("ad_event", event);
        firebaseAnalytics.logEvent("Interstitial_ad_event", bundle);

        Log.d("ADEVENT", message);
    }
}
