package com.dadash.sfcsnotes;
import android.content.res.Configuration;
import android.os.Bundle;
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
    private FirebaseAnalytics mFirebaseAnalytics;
    private GridLayout gridLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        pyqCardView = view.findViewById(R.id.pyq_cardview);
        videosCardView = view.findViewById(R.id.videos_cardview);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        gridLayout = view.findViewById(R.id.dashboard_grid);
        pyqCardView.setOnClickListener(v -> {
            openFragment(new pyq_list_fragment());
            logButtonClick("PYQ card clicked");
        });
        videosCardView.setOnClickListener(v -> {
            openFragment(new yt_playlists_list_fragment());
            logButtonClick("Videos card clicked");
        });
        adjustGridLayoutForOrientation();
        return view;
    }
    private void openFragment(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
    private void logButtonClick(String buttonName) {
        Bundle bundle = new Bundle();
        bundle.putString("button_name", buttonName);
        mFirebaseAnalytics.logEvent("user_interaction", bundle);
    }
    private void adjustGridLayoutForOrientation() {
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayout.setColumnCount(2);
            gridLayout.setRowCount(1);
        } else {
            gridLayout.setColumnCount(1);
            gridLayout.setRowCount(2);
        }
    }
}
