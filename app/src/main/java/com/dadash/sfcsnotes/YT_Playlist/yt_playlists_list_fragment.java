package com.dadash.sfcsnotes.YT_Playlist;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.fragment.app.Fragment;
import com.dadash.sfcsnotes.R;
import com.google.firebase.analytics.FirebaseAnalytics;
public class yt_playlists_list_fragment extends Fragment {
    private TextView ytListMessage;
    private TextView rateUsLink;
    private ProgressBar maintenanceProgressBar;
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_yt_playlists_list, container, false);
        ytListMessage = view.findViewById(R.id.yt_list_message);
        rateUsLink = view.findViewById(R.id.rate_us_link);
        maintenanceProgressBar = view.findViewById(R.id.maintenanceProgressBar);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        rateUsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logRateUsClickEvent();
                onRateUsClick();
            }
        });
        return view;
    }
    private void onRateUsClick() {
        String rateUsUrl = getString(R.string.rate_us);
        Uri uri = Uri.parse(rateUsUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
    }
    private void logRateUsClickEvent() {
        Bundle bundle = new Bundle();
        bundle.putString("action", "rate_us_click");
        bundle.putString("screen", "yt_playlists_list_fragment");
        mFirebaseAnalytics.logEvent("rate_us_button_click", bundle);
    }
}
