package io.github.mehdiataei.photorush;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.mehdiataei.photorush.Utils.MyRecyclerViewAdapter;

import static io.github.mehdiataei.photorush.RegisterActivity.REQUEST_IMAGE_CAPTURE;

public class HomeActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {


    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;
    private Bitmap profPic;
    private ImageView profilePic;
    private TextView usernameText, bioText;
    private String userID;
    private Bundle bundle;
    private MyRecyclerViewAdapter adapter;

    private static final int NUM_OF_COLUMNS = 3;


    private FirebaseFirestore db;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorageRef;

    ImageButton logoutButton;
    Button uploadButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        profilePic = findViewById(R.id.profile_picture);
        usernameText = findViewById(R.id.username_text);
        bioText = findViewById(R.id.shortBio_profile);

        logoutButton = findViewById(R.id.logout_button);

        db = FirebaseFirestore.getInstance();

        setupFirebaseAuth();

    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i("TAG", "You clicked number " + adapter.getItem(position) + ", which is at cell position " + position);
    }



    /**
     * Firebase Auth setup
     */

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    setLogoutButton();
                    userID = user.getUid();
                    setUserThumbnail();
                    readSingleUser();
                    // data to populate the RecyclerView with

                    //Bitmap[] data = new Bitmap[]{profPic, profPic};

                    Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.thumbnail);//assign your bitmap;
                    Bitmap[] data = {bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1, bitmap1};

                    // set up the RecyclerView
                    RecyclerView recyclerView = findViewById(R.id.rvNumbers);
                    recyclerView.setLayoutManager(new GridLayoutManager(HomeActivity.this, NUM_OF_COLUMNS));
                    adapter = new MyRecyclerViewAdapter(HomeActivity.this, data);
                    adapter.setClickListener(HomeActivity.this);
                    recyclerView.setAdapter(adapter);

                } else {

                    finish();
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
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


    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if (user == null) {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }


    /*
    ------------------------------------ Firebase ---------------------------------------------
    */

    private void readSingleUser() {

        Log.d(TAG, "readSingleUser: " + userID);
        DocumentReference user = db.collection("Users").document(userID);
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    usernameText.setText(doc.get("username").toString());
                    bioText.setText(doc.get("bio").toString());

                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void setUserThumbnail() {


        StorageReference islandRef = mStorageRef.child(userID + "/thumbnail.jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                profPic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profilePic.setImageBitmap(profPic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void setLogoutButton() {


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth.getInstance().signOut();
                //finish();

            }
        });

    }


//    /*
//------------------------------------ Camera ---------------------------------------------
// */
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//    }
//
//    // Retreive info from the camera
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//
//            CircleImageView profilePic = findViewById(R.id.profile_picture_register);
//            Bundle extras = data.getExtras();
//            imageBitmap = (Bitmap) extras.get("data");
//            profilePic.setImageBitmap(imageBitmap);
//
//        }
//    }
//




}

