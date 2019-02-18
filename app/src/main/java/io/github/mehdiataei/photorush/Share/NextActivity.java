package io.github.mehdiataei.photorush.Share;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.mehdiataei.photorush.R;
import io.github.mehdiataei.photorush.Utils.AutoHashtag;
import io.github.mehdiataei.photorush.Utils.FirebaseMethods;

import static android.support.constraint.Constraints.TAG;

/**
 * Created by User on 7/24/2017.
 */

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseMethods mFirebaseMethods;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;



    //widgets
    private EditText mCaption;

    //vars
    private String mAppend = "file:/";
    private int imageCount = 0;
    private Intent intent;
    private Bitmap bitmap;
    private Switch toggle;
    private AutoHashtag hashtagGen;
    private List<FirebaseVisionImageLabel> hashtagsInfo;
    private List<String> hashtags;

    Uri photoURI;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = findViewById(R.id.caption);
        toggle = findViewById(R.id.hashtag_toggle);
        hashtags = new ArrayList<>();

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();




        setupFirebaseAuth();

        ImageView backArrow = findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });

        TextView shareButton = findViewById(R.id.tvShare);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen.");
                //upload the image to firebase
                Toast.makeText(NextActivity.this, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();
                String caption = mCaption.getText().toString();

                mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, photoURI, null);
            }

        });

        setImage();
        setAutoHashtagToggle();

    }

    private void setAutoHashtagToggle() {


        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Log.d(TAG, "onCheckedChanged: Attempting to generate hashtags.");

                hashtagGen = new AutoHashtag(bitmap);

                hashtagGen.labeler.processImage(hashtagGen.getImage())
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                                // Task completed successfully
                                // ...

                                if (labels != null) {

                                    for (FirebaseVisionImageLabel label : labels) {
                                        hashtags.add(0, label.getText());
                                        mCaption.append("#" + label.getText().replaceAll("\\s+", "") + " ");
                                        Log.d(TAG, "onSuccess: Generated hashtag is: " + label.getText());
                                    }

                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.d(TAG, "onFailure: Failed to generate hashtags " + e.getMessage());
                                // Task failed with an exception
                                // ...
                            }
                        });
            }
        });

    }

    private void someMethod() {
        /*
            Step 1)
            Create a data model for Photos
            Step 2)
            Add properties to the Photo Objects (caption, date, imageUrl, photo_id, tags, user_id)
            Step 3)
            Count the number of photos that the user already has.
            Step 4)
            a) Upload the photo to Firebase Storage
            b) insert into 'photos' node
            c) insert into 'user_photos' node
         */

    }


    /**
     * gets the image url from the incoming intent and displays the chosen image
     */
//    private void setImage(){
//        intent = getIntent();
//        ImageView image = (ImageView) findViewById(R.id.imageShare);
//
//        if(intent.hasExtra(getString(R.string.selected_image))){
//            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
//            Log.d(TAG, "setImage: got new image url: " + imgUrl);
//            UniversalImageLoader.setImage(imgUrl, image, null, mAppend);
//        }
//        else if(intent.hasExtra(getString(R.string.selected_bitmap))){
//            bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
//            Log.d(TAG, "setImage: got new bitmap");
//            image.setImageBitmap(bitmap);
//        }
//    }

     /*
     ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
//        mFirebaseDatabase = FirebaseDatabase.getInstance();
//        myRef = mFirebaseDatabase.getReference();
        Log.d(TAG, "onDataChange: image count: " + imageCount);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
//

        db.collection(getString(R.string.dbname_user_photos))
                .whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot;
                            imageCount = mFirebaseMethods.getImageCount(task.getResult());
                        }

                    }
                });

    }

    /**
     * gets the image url from the incoming intent and displays the chosen image
     */
    private void setImage() {
        intent = getIntent();
        ImageView image = findViewById(R.id.imageShare);

        if (intent.hasExtra(getString(R.string.selected_bitmap))) {

            photoURI = intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: Got new bitmap");

            Log.d(TAG, "onActivityResult: PhotoURI is: " + photoURI);

            try {
                Log.d(TAG, "onActivityResult: PhotoURI is: " + photoURI);
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream);

            Glide.with(this)
                    .asBitmap()
                    .load(stream.toByteArray())
                    .error(R.drawable.ic_error)
                    .into(image);
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}