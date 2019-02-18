package io.github.mehdiataei.photorush.Profile;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.mehdiataei.photorush.Login.LoginActivity;
import io.github.mehdiataei.photorush.Models.User;
import io.github.mehdiataei.photorush.R;
import io.github.mehdiataei.photorush.Share.NextActivity;
import io.github.mehdiataei.photorush.Utils.BottomNavigationViewHelper;
import io.github.mehdiataei.photorush.Utils.MyRecyclerViewAdapter;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by User on 6/29/2017.
 */

public class ProfileFragment extends Fragment implements MyRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = "ProfileFragment";

    private Bitmap profPic;
    private ImageView profilePic;
    private TextView usernameText, bioText;
    private String userID;
    private Bundle bundle;
    private MyRecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    private List<Bitmap> mData;
    private String mCurrentPhotoPath;
    Uri photoURI;


    private static final int NUM_OF_COLUMNS = 3;

    private static final int ACTIVITY_NUM = 0;

    private Context mContext;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    private FirebaseFirestore db;

    private BottomNavigationView bottomNavigationView;

    private StorageReference mStorageRef;

    private FloatingActionButton myFab;

    static final int REQUEST_IMAGE_CAPTURE = 2;
    Bitmap lastImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profilePic = view.findViewById(R.id.profile_picture);
        usernameText = view.findViewById(R.id.username_text);
        bioText = view.findViewById(R.id.shortBio_profile);
        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        myFab = view.findViewById(R.id.fab);

        mContext = getActivity();

        setupFirebaseAuth();
        setupBottomNavigationView();
        configureCaptureButton();


        recyclerView = view.findViewById(R.id.rvNumbers);

        mData = new ArrayList<>();
        Bitmap bitmap = null;

        for (int i = 0; i < 100; i++) {

            mData.add(bitmap);
        }

        // set up the RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, NUM_OF_COLUMNS, GridLayoutManager.VERTICAL, false));
        adapter = new MyRecyclerViewAdapter(mContext, mData);

        recyclerView.setAdapter(adapter);

        adapter.setClickListener(this);

        return view;
    }


    @Override
    public void onItemClick(View view, int position) {
        Log.i("TAG", "You clicked number " + adapter.getItem(position) + ", which is at cell position " + position);

    }

    private void setProfileWidgets(User user) {


    }


    /**
     * Firebase Auth setup
     */

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Log.d(TAG, "onAuthStateChanged: State changed.");

                checkCurrentUser(user);

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

//        if (mAuth.getUid() != null) {
//
//            Log.d(TAG, "setupFirebaseAuth: Getting document snapshot for user " + mAuth.getUid());
//
//            final DocumentReference docRef = db.collection("Users").document(mAuth.getUid());
//            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
//
//                    if (e != null) {
//                        Log.w(TAG, "Listen failed.", e);
//                        return;
//                    }
//
//                    if (documentSnapshot != null && documentSnapshot.exists()) {
//                        Log.d(TAG, "Current data: " + documentSnapshot.getData());
//                    } else {
//                        Log.d(TAG, "Current data: null");
//                    }
//
//                }
//            });

//        }

    }

    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if (user == null) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
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

    /**
     * Camera
     */

//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(mContext,
                        "io.github.mehdiataei.photorush",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "createImageFile: Absolute path is: " + mCurrentPhotoPath);
        return image;
    }

    // Lunch camera

    private void configureCaptureButton() {
        myFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: fab clicked.");

                dispatchTakePictureIntent();
            }
        });
    }

    // Retreive info from the camera

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && resultCode != RESULT_CANCELED) {

            Log.d(TAG, "onActivityResult: Done taking a photo.");
            Log.d(TAG, "onActivityResult: Attempting to navigate to final share screen.");

            try {

                Intent intent = new Intent(mContext, NextActivity.class);

                intent.putExtra(getString(R.string.selected_bitmap), photoURI);
                mContext.startActivity((intent));
            } catch (NullPointerException e) {
                Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
            }

        }

    }
}