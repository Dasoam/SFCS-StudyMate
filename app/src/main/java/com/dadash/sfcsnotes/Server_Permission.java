package com.dadash.sfcsnotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Server_Permission extends AppCompatActivity {

    private TextView maintenanceMessage, estimatedTimeText;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide the title
        getSupportActionBar().hide(); // Hide the action bar
        
        
        setContentView(R.layout.activity_server_permission);

        // Initialize the Firebase Firestore instance
        firestore = FirebaseFirestore.getInstance();

        // Initialize the UI components
        maintenanceMessage = findViewById(R.id.maintenance_message);
        estimatedTimeText = findViewById(R.id.estimated_time);

        // Fetch the maintenance time from Firestore and start the countdown
        fetchMaintenanceTime();
    }

    private void fetchMaintenanceTime() {
        DocumentReference docRef = firestore.document("Maintenance/Maintenance Time");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Retrieve the maintenance time (in milliseconds)
                        long maintenanceTimeMillis = document.getLong("Time");

                        if (maintenanceTimeMillis != 0) {
                            // Start the countdown timer with the fetched maintenance time
                            startCountDownTimer(maintenanceTimeMillis);
                        } else {
                            estimatedTimeText.setText("Estimated time: Not available.");
                        }
                    } else {
                        estimatedTimeText.setText("Estimated time: Not available.");
                    }
                } else {
                    estimatedTimeText.setText("Failed to fetch maintenance time.");
                }
            }
        });
    }

    private void startCountDownTimer(long maintenanceTimeMillis) {
        // Set the estimated time message and start the countdown timer

        new CountDownTimer(maintenanceTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the estimated time TextView every second
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                estimatedTimeText.setText("Estimated time: " + minutes + "m " + seconds + "s");
            }

            @Override
            public void onFinish() {
                // When the countdown finishes, set the time to 0 and display a message
                estimatedTimeText.setText("Maintenance is complete. The app is now live! Please restart the app.");
            }
        }.start();
    }
}
