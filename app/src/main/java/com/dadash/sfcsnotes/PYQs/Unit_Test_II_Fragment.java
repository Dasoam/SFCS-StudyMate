package com.dadash.sfcsnotes.PYQs;

import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Unit_Test_II_Fragment extends Fragment {
    private WebView webView;
    private AdView adView;
    private String loadUrl;
    private int flagToCheckDocumentOpened = 0; // Flag to track document element status

    private ProgressBar progressBar;
    private FirebaseFirestore firestore;
    private FirebaseAnalytics firebaseAnalytics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unit_test_2, container, false);

        // Initialize ProgressBar
        progressBar = view.findViewById(R.id.unit_2_progress_bar);
        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        // Initialize and set up WebView
        initializeWebView(view);

        // Initialize and set up AdMob
        initializeAdMob(view);

        // Load the URL into WebView
        loadUrlIntoWebView();

        // Handle back navigation
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
                progressBar.setVisibility(View.VISIBLE); // Show progress bar when loading starts
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE); // Hide progress bar when loading is complete

                // Optional: JavaScript to remove UI elements
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
                progressBar.setVisibility(View.GONE); // Hide progress bar on error
            }
        });
    }


    // Load the URL into the WebView
    private void loadUrlIntoWebView() {
        loadUrl = getString(R.string.ut2_link);  // Get the URL from strings.xml
        webView.loadUrl(loadUrl);
        webView.addJavascriptInterface(new JavaScriptInterface(), "AndroidInterface");
    }

    // Initialize AdMob and set up the AdView
    private void initializeAdMob(View view) {
        // Initialize Mobile Ads SDK
        MobileAds.initialize(getContext(), initializationStatus -> {
            // Initialization complete
        });

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize the AdView
        adView = view.findViewById(R.id.unit_test_2_banner_ad);

        // Create an AdRequest
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // Set up AdListener to capture analytics
        adView.setAdListener(new AdListener() {

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad
                logAdEvent("AdClicked", "User clicked on an ad");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return to the app after tapping on an ad
                logAdEvent("AdClosed", "User closed the ad");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails
                logAdEvent("AdFailedToLoad", "Ad failed to load: " + adError.getMessage());
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded for an ad
                logAdEvent("AdImpression", "Ad impression recorded");
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading
                logAdEvent("AdLoaded", "Ad loaded successfully");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay
                logAdEvent("AdOpened", "Ad opened");
            }
        });
    }
    //    private void logAdEvent(String eventType, String description) {
//        Bundle params = new Bundle();
//        params.putString("event_type", eventType);
//        params.putString("description", description);
//        firebaseAnalytics.logEvent("ad_event", params);
//        Log.d("djbfjajas","done");
//    }
    private void logAdEvent(String eventType, String description) {
        // Prepare event parameters
        Bundle params = new Bundle();
        params.putString("event_type", eventType);
        params.putString("description", description);

        // Log the event to Firebase Analytics
        firebaseAnalytics.logEvent("ad_event", params);

        // Log a message to Logcat to indicate that the event has been logged
        Log.d("AdEvent", "Event Logged: " + eventType + " - " + description);
    }



    // Helper method to save ad events to Firestore
//    private void saveAdEventToFirestore(FirebaseFirestore firestore, String eventDescription) {
//        // Create a map to store ad event data
//        Map<String, Object> adData = new HashMap<>();
//        adData.put("event", eventDescription);
//        adData.put("timestamp", System.currentTimeMillis());
//
//        // Save to Firestore under the path "AdMob/Analytics"
//        firestore.collection("AdMob").document("Analytics")
//                .collection("Events")
//                .add(adData)
//                .addOnSuccessListener(documentReference -> {
//                    // Log success for debugging (optional)
//                })
//                .addOnFailureListener(e -> {
//                    // Log failure for debugging (optional)
//                });
//    }


    // Handle back navigation
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

//                try {
//                    Thread.sleep(100);  // Allow time for the JavaScript to evaluate
//                } catch (Exception ex) {
//                    // Handle exception if any
//                }

                if (flagToCheckDocumentOpened == 1) {
                    loadUrl = webView.getUrl();
                    webView.loadUrl(loadUrl);
                } else if (webView.isFocused() && webView.canGoBack()) {
                    // Navigate back in WebView
                    webView.goBack();
                } else {
                    // No more WebView history, exit to previous fragment
                    setEnabled(false); // Allow default back press behavior
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    // JavaScript Interface to communicate with the WebView
    class JavaScriptInterface {
        @JavascriptInterface
        public void onDocumentElementStatusChanged(boolean isOpen) {
            // Update the document element status
            if (isOpen) {
                flagToCheckDocumentOpened = 1; // Document element is open
            } else {
                flagToCheckDocumentOpened = 0; // Document element is not open
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // Prevent screenshots and screen recordings when this fragment is visible
        requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Allow screenshots and screen recordings when this fragment is no longer visible
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }
    public void onDestroy() {
        if (adView != null) {
            adView.destroy(); // Destroy the ad when activity is destroyed
        }
        super.onDestroy();
    }

}
