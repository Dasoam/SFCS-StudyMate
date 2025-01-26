package com.dadash.sfcsnotes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_server_permission);
        firestore = FirebaseFirestore.getInstance();
        maintenanceMessage = findViewById(R.id.maintenance_message);
        estimatedTimeText = findViewById(R.id.estimated_time);
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
                        long maintenanceTimeMillis = document.getLong("Time");
                        if (maintenanceTimeMillis != 0) {

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
        new CountDownTimer(maintenanceTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                estimatedTimeText.setText("Estimated time: " + minutes + "m " + seconds + "s");
            }
            @Override
            public void onFinish() {
                estimatedTimeText.setText("Maintenance is complete. The app is now live! Please restart the app.");
            }
        }.start();
    }
}
