package com.dadash.sfcsnotes.User_Profile;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dadash.sfcsnotes.R;
public class profile_about_us extends Fragment {
    public profile_about_us() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_about_us, container, false);
    }
}