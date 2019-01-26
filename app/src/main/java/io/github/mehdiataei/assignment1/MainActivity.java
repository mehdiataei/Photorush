package io.github.mehdiataei.assignment1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginButton = findViewById(R.id.signup_button);
        Button gotoSignupButton = findViewById(R.id.goto_signup_button);

// Assign buttons listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                Log.d(TAG, "Login button clicked.");

                Intent signupIntent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(signupIntent);

            }
        });

        gotoSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                Log.d(TAG, "go to signup button clicked.");

                Intent signupIntent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(signupIntent);

            }
        });

    }


}
