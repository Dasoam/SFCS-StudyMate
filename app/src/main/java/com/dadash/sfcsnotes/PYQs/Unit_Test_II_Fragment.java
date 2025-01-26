package com.dadash.sfcsnotes.PYQs;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.dadash.sfcsnotes.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;
public class Unit_Test_II_Fragment extends Fragment {
    private WebView webView;
    private AdView adView;
    private String loadUrl;
    private int flagToCheckDocumentOpened = 0;
    private ProgressBar progressBar;
    private FirebaseFirestore firestore;
    private FirebaseAnalytics firebaseAnalytics;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unit_test_2, container, false);
        progressBar = view.findViewById(R.id.unit_2_progress_bar);
        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        initializeWebView(view);
        initializeAdMob(view);
        loadUrlIntoWebView();
        handleBackNavigation();
        return view;
    }
    private void initializeWebView(View view) {
        webView = view.findViewById(R.id.unit_test_2_webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                String javascriptCode = "var elementsToRemove = document.querySelectorAll(" +
                        "'.a-s-tb-sc-Ja.a-s-tb-Pe.a-N-Mf-Ec.a-s-tb-sc-Ja-fk, .a-N-s-uq');" +
                        "for (var i = 0; i < elementsToRemove.length; i++) {" +
                        "    elementsToRemove[i].remove();" +
                        "}";
                view.evaluateJavascript(javascriptCode, null);
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void loadUrlIntoWebView() {
        loadUrl = getString(R.string.ut2_link);
        webView.loadUrl(loadUrl);
        webView.addJavascriptInterface(new JavaScriptInterface(), "AndroidInterface");
    }
    private void initializeAdMob(View view) {
        MobileAds.initialize(getContext(), initializationStatus -> {
        });
        firestore = FirebaseFirestore.getInstance();
        adView = view.findViewById(R.id.unit_test_2_banner_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                logAdEvent("AdClicked", "User clicked on an ad");
            }
            @Override
            public void onAdClosed() {
                logAdEvent("AdClosed", "User closed the ad");
            }
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                logAdEvent("AdFailedToLoad", "Ad failed to load: " + adError.getMessage());
            }
            @Override
            public void onAdImpression() {
                logAdEvent("AdImpression", "Ad impression recorded");
            }
            @Override
            public void onAdLoaded() {
                logAdEvent("AdLoaded", "Ad loaded successfully");
            }
            @Override
            public void onAdOpened() {
                logAdEvent("AdOpened", "Ad opened");
            }
        });
    }
    private void logAdEvent(String eventType, String description) {
        Bundle params = new Bundle();
        params.putString("event_type", eventType);
        params.putString("description", description);
        firebaseAnalytics.logEvent("ad_event", params);
    }
    private void handleBackNavigation() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                String javascript = "javascript:(function() { " +
                        "var documentElement = document.querySelector('[role=\"document\"]'); " +
                        "var isOpen = (documentElement !== null && documentElement.style.display !== 'none'); " +
                        "AndroidInterface.onDocumentElementStatusChanged(isOpen); " +
                        "})();";
                webView.evaluateJavascript(javascript, null);
                if (flagToCheckDocumentOpened == 1) {
                    loadUrl = webView.getUrl();
                    webView.loadUrl(loadUrl);
                } else if (webView.isFocused() && webView.canGoBack()) {

                    webView.goBack();
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }
    class JavaScriptInterface {
        @JavascriptInterface
        public void onDocumentElementStatusChanged(boolean isOpen) {
            if (isOpen) {
                flagToCheckDocumentOpened = 1;
            } else {
                flagToCheckDocumentOpened = 0;
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }
    @Override
    public void onPause() {
        super.onPause();
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}
