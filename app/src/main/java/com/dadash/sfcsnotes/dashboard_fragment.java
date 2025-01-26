package com.dadash.sfcsnotes;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.dadash.sfcsnotes.PYQs.pyq_list_fragment;
import com.dadash.sfcsnotes.YT_Playlist.yt_playlists_list_fragment;
import com.google.firebase.analytics.FirebaseAnalytics;

public class dashboard_fragment extends Fragment {

    private CardView pyqCardView, videosCardView;
    private FirebaseAnalytics mFirebaseAnalytics; // Firebase Analytics instance
    private GridLayout gridLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);  // Correct layout name here

        // Initialize the CardViews
        pyqCardView = view.findViewById(R.id.pyq_cardview);
        videosCardView = view.findViewById(R.id.videos_cardview);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        gridLayout = view.findViewById(R.id.dashboard_grid);
        // Set click listeners on the CardViews
        pyqCardView.setOnClickListener(v -> {
            openFragment(new pyq_list_fragment());
            logButtonClick("PYQ card clicked");
        });

        videosCardView.setOnClickListener(v -> {
            openFragment(new yt_playlists_list_fragment());
            logButtonClick("Videos card clicked");
        });


        // Adjust GridLayout for orientation
        adjustGridLayoutForOrientation();
        
        return view;
    }

    // Method to replace current fragment with a new one
    private void openFragment(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment) // Replace the fragment
                .addToBackStack(null) // Add to back stack to allow user to navigate back
                .commit();
    }
    // Method to log button clicks to Firebase Analytics
    private void logButtonClick(String buttonName) {
        // Log event to Firebase Analytics
        Bundle bundle = new Bundle();
        bundle.putString("button_name", buttonName);
        mFirebaseAnalytics.logEvent("user_interaction", bundle);

        // Log to Logcat for debugging purposes
        Log.d("DashboardFragment", "Button clicked: " + buttonName);
    }
    private void adjustGridLayoutForOrientation() {
        // Get the current screen orientation
        Configuration configuration = getResources().getConfiguration();

        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayout.setColumnCount(2);  // 2 columns in landscape mode
            gridLayout.setRowCount(1);     // 1 row in landscape mode
        } else {
            gridLayout.setColumnCount(1);  // 1 column in portrait mode
            gridLayout.setRowCount(2);     // 2 rows in portrait mode
        }
    }
}
