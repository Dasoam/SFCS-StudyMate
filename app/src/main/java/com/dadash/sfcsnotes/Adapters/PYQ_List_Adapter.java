package com.dadash.sfcsnotes.Adapters;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dadash.sfcsnotes.PYQs.Annual_Exam_Fragment;
import com.dadash.sfcsnotes.PYQs.Half_Yearly_Fragment;
import com.dadash.sfcsnotes.PYQs.Unit_Test_II_Fragment;
import com.dadash.sfcsnotes.PYQs.Unit_Test_I_Fragment;
import com.dadash.sfcsnotes.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.List;
public class PYQ_List_Adapter extends RecyclerView.Adapter<PYQ_List_Adapter.PyqViewHolder> {
    private List<String> pyqList;
    private FragmentManager fragmentManager;
    private InterstitialAd mInterstitialAd;
    private Context context;
    private FirebaseAnalytics firebaseAnalytics;
    public PYQ_List_Adapter(List<String> pyqList, FragmentManager fragmentManager, Context context) {
        this.pyqList = pyqList;
        this.fragmentManager = fragmentManager;
        this.context = context;

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        loadInterstitialAd();
    }
    @NonNull
    @Override
    public PyqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pyq_list, parent, false);
        return new PyqViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull PyqViewHolder holder, int position) {
        String pyqItem = pyqList.get(position);
        holder.textView.setText(pyqItem);
        switch (pyqItem) {
            case "Unit Test I":
                holder.imageView.setImageResource(R.drawable.exam_unit2);
                break;
            case "Half-Yearly":
                holder.imageView.setImageResource(R.drawable.half_yearly);
                break;
            case "Unit Test II":
                holder.imageView.setImageResource(R.drawable.qa);
                break;
            case "Annual Exam":
                holder.imageView.setImageResource(R.drawable.exam_icon);
                break;
            default:
                holder.imageView.setImageResource(R.drawable.test);
                break;
        }
        holder.cardView.setOnClickListener(v -> {
            trackUserClick(pyqItem);
            openFragment(pyqItem);
        });
    }
    @Override
    public int getItemCount() {
        return pyqList.size();
    }
    public static class PyqViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public CardView cardView;
        public PyqViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.pyqTextView);
            imageView = itemView.findViewById(R.id.pyqImageView);
            cardView = itemView.findViewById(R.id.pyq_list_cardview);
        }
    }
    private void openFragment(String item) {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    trackAdEvent(item, "Ad Dismissed");
                    loadInterstitialAd();
                    navigateToFragment(item);
                }
                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    trackAdEvent(item, "Ad Failed");
                    navigateToFragment(item);
                }
            });
            trackAdEvent(item, "Ad Shown");
            mInterstitialAd.show(fragmentManager.getFragments().get(0).getActivity());
        } else {
            navigateToFragment(item);
        }
    }
    private void navigateToFragment(String item) {
        Fragment fragment = null;
        if ("Unit Test I".equals(item)) {
            fragment = new Unit_Test_I_Fragment();
        } else if ("Half-Yearly".equals(item)) {
            fragment = new Half_Yearly_Fragment();
        } else if ("Unit Test II".equals(item)) {
            fragment = new Unit_Test_II_Fragment();
        } else if ("Annual Exam".equals(item)) {
            fragment = new Annual_Exam_Fragment();
        }
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
    private void loadInterstitialAd() {
        String adUnitId = context.getString(R.string.pyq_Interstitial);
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, adUnitId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        loadInterstitialAd();
                    }
                });
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
            }
        });
    }
    private void trackUserClick(String item) {
        Bundle analyticsBundle = new Bundle();
        analyticsBundle.putString("item_clicked", item);
        firebaseAnalytics.logEvent("Interstitial_item_click", analyticsBundle);
    }
    private void trackAdEvent(String item, String adStatus) {
        Bundle analyticsBundle = new Bundle();
        analyticsBundle.putString("item", item);
        analyticsBundle.putString("ad_status", adStatus);
        firebaseAnalytics.logEvent("Interstitial_ad_event", analyticsBundle);
    }
}
