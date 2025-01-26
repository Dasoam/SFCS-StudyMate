package com.dadash.sfcsnotes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class login_register_option extends AppCompatActivity {

    private Button loginButton;
    private Button newUserButton;
    private TextView support_link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title
        getSupportActionBar().hide(); // hide the action bar
        

        setContentView(R.layout.activity_login_register_option);

        loginButton = findViewById(R.id.login_button);
        newUserButton = findViewById(R.id.newUser_register_button);
        support_link=findViewById(R.id.login_register_click_here);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginWindow();
            }
        });

        newUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterWindow();
            }
        });
        support_link.setOnClickListener(View -> openExternalLink(getString(R.string.support_form)));
    }
    private void openRegisterWindow(){
        Intent intent = new Intent(getApplicationContext(), register.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void openLoginWindow(){
        Intent intent = new Intent(getApplicationContext(), login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void openExternalLink(String url){

        // Create the URI from the string
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        // Start the activity to open the Play Store page
        startActivity(intent);
    }
}