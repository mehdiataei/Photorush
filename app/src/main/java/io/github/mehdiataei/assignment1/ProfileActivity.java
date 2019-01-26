package io.github.mehdiataei.assignment1;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Receive data from the reg activity

        Bundle bundle = getIntent().getExtras();
        String bio = bundle.getString("bio");

        TextView bioText = findViewById(R.id.shortBio_profile);
        bioText.setText(bio);

        String username = bundle.getString("username");
        TextView usernameText = findViewById(R.id.username_text);
        usernameText.setText(username);

        boolean profilePic_taken = getIntent().getExtras().getBoolean("profile pic taken");

        if (profilePic_taken) {
            Bitmap profPic = getIntent().getParcelableExtra("profile picture");
            ImageView profilePic = findViewById(R.id.profile_picture);
            profilePic.setImageBitmap(profPic);
        }


    }


}
