package io.github.mehdiataei.photorush.Profile;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
//import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.List;

import io.github.mehdiataei.photorush.Main.MainActivity;
import io.github.mehdiataei.photorush.R;
import io.github.mehdiataei.photorush.Utils.BottomNavigationViewHelper;
import io.github.mehdiataei.photorush.Utils.MyRecyclerViewAdapter;

public class ProfileActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {


    private static final String TAG = "ProfileActivity";
    private Bitmap profPic;
    private ImageView profilePic;
    private TextView usernameText, bioText;
    private String userID;
    private Bundle bundle;
    private MyRecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    private List<Bitmap> mData;

    //ProgressBar progressBar;

    private static final int NUM_OF_COLUMNS = 3;

    private static final int ACTIVITY_NUM = 0;

    private Context mContext = ProfileActivity.this;


    private FirebaseFirestore db;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorageRef;

    ImageButton logoutButton;
    ImageButton uploadButton;

    static final int REQUEST_IMAGE_CAPTURE = 2;
    Bitmap lastImage;
    String lastImageAddress;
    Bitmap tempImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profilePic = findViewById(R.id.profile_picture);
        usernameText = findViewById(R.id.username_text);
        bioText = findViewById(R.id.shortBio_profile);
//
//        logoutButton = findViewById(R.id.logout_button);
//        uploadButton = findViewById(R.id.upload_button);

        //progressBar = findViewById(R.id.progressBarRecy);

        db = FirebaseFirestore.getInstance();

        setupFirebaseAuth();

        setupBottomNavigationView();


//        recyclerView = findViewById(R.id.rvNumbers);
//
//        mData = new ArrayList<>();
//
//        // set up the RecyclerView
//        recyclerView.setLayoutManager(new GridLayoutManager(ProfileActivity.this, NUM_OF_COLUMNS, GridLayoutManager.VERTICAL, false));
//        adapter = new MyRecyclerViewAdapter(ProfileActivity.this, mData);
//
//
//        recyclerView.setAdapter(adapter);
//        recyclerView.setItemAnimator(null);
//
//
//        adapter.setClickListener(ProfileActivity.this);

    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i("TAG", "You clicked number " + adapter.getItem(position) + ", which is at cell position " + position);


        Dialog builder = new Dialog(ProfileActivity.this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(ProfileActivity.this);
        imageView.setImageBitmap(adapter.getItem(position));

        final float scale = ProfileActivity.this.getResources().getDisplayMetrics().density;
        int pixels = (int) (400 * scale + 0.5f);

        RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(pixels, pixels);

        builder.addContentView(imageView, rel_btn);
        builder.show();
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
//                    setLogoutButton();
//                    setUserID(user.getUid());
//                    setUserThumbnail();
//                    profilePic.invalidate();
//                    readSingleUser();
//                    configureCaptureButton();
//                    mData.clear();
//                    initGrid();

                    Log.i(TAG, "onAuthStateChanged: I am here.");


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
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
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

                Log.d(TAG, "onSuccess: Thumbnail set.");
                profPic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profilePic.setImageBitmap(profPic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "onFailure: Thumbnail set failed.");
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
//------------------------------------ Camera and grid view init ----------------------------------
// */
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//    }
//
//    // Lunch camera
//
//    private void configureCaptureButton() {
//        uploadButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                dispatchTakePictureIntent();
//            }
//        });
//    }
//
//    // Retreive info from the camera
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//
//            Bundle extras = data.getExtras();
//            lastImage = (Bitmap) extras.get("data");
//
//            lastImage = cropToSquare(lastImage);
//
//            Log.d(TAG, "onActivityResult: Image captured");
//
//            // Get timestamp
//
//            Long tsLong = System.currentTimeMillis() / 1000;
//            String ts = tsLong.toString();
//
//            uploadImage(lastImage, ts);
//
//
//        }
//    }
//
//    private void uploadImage(Bitmap image, String ts) {
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//
//        byte[] data = baos.toByteArray();
//
//        final String adr = userID + "/" + ts + ".jpg";
//
//        final StorageReference imageRef = mStorageRef.child(adr);
//
//        UploadTask uploadTask = imageRef.putBytes(data);
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle unsuccessful uploads
//                Log.d(TAG, "onFailure: Image upload failed. ");
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
//                // ...
//                Log.d(TAG, "onSuccess: Image uploaded. Address: " + adr);
//
//                addImageAddress(adr);
//                setLastImageAddress(adr);
//                getLastImage();
//
//            }
//        });
//
//    }
//
//
//    private void initGrid() {
//
//
//        DocumentReference user = db.collection("Users").document(userID);
//        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    //progressBar.setVisibility(View.VISIBLE);
//                    Log.d(TAG, "onComplete: grid is initiating.");
//                    DocumentSnapshot doc = task.getResult();
//                    List<String> addresses = (List<String>) doc.get("images");
//
//
//                    if (addresses != null) {
//                        if (addresses.size() > 0) {
//
//                            java.util.Collections.sort(addresses);
//
//                            for (String adr : addresses) {
//
//                                Log.i(TAG, "onComplete: Link example: " + addresses.get(0));
//
//
//                                StorageReference ref = mStorageRef.child(adr);
//
//                                final long ONE_MEGABYTE = 1024 * 1024;
//                                ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                                    @Override
//                                    public void onSuccess(byte[] bytes) {
//                                        // Data for "images/island.jpg" is returns, use this as needed
//                                        tempImg = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//
//                                    }
//                                }).addOnCompleteListener(new OnCompleteListener<byte[]>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<byte[]> task) {
//                                        Log.d(TAG, "onComplete: Adding images to mData.");
//                                        addToMData(tempImg);
//                                        adapter.notifyDataSetChanged();
//
//                                    }
//                                });
//                            }
//
//
//                        }
//
//                    }
//
//
//                }
//
//                //progressBar.setVisibility(View.GONE);
//
//            }
//        })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                    }
//                });
//
//
//    }
//
//
//    private void getLastImage() {
//
//        //progressBar.setVisibility(View.VISIBLE);
//
//
//        Log.d(TAG, "getLastImage: Adding last image.");
//        StorageReference ref = mStorageRef.child(lastImageAddress);
//
//        final long ONE_MEGABYTE = 1024 * 1024;
//        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] bytes) {
//                Log.d(TAG, "onSuccess: Last image .");
//                // Data for "images/island.jpg" is returns, use this as needed
//                Bitmap img;
//                img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                mData.add(0, img);
//                adapter.notifyItemInserted(0);
//            }
//        });
//
//
//    }
//
//
//    private void addImageAddress(String address) {
//
//        Log.d(TAG, "Adding image address to the database.");
//
//
//        db.collection("Users").document(userID).update("images", FieldValue.arrayUnion(address))
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "Image address added.");
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                        Log.w(TAG, "Error adding image address.", e);
//
//
//                    }
//                });
//
//    }
//
//    public void setLastImageAddress(String lastImageAddress) {
//        this.lastImageAddress = lastImageAddress;
//    }
//
//    public static Bitmap cropToSquare(Bitmap bitmap) {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int newWidth = (height > width) ? width : height;
//        int newHeight = (height > width) ? height - (height - width) : height;
//        int cropW = (width - height) / 2;
//        cropW = (cropW < 0) ? 0 : cropW;
//        int cropH = (height - width) / 2;
//        cropH = (cropH < 0) ? 0 : cropH;
//        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
//
//        return cropImg;
//    }
//
//
//    public void addToMData(Bitmap tempImg) {
//        this.mData.add(0, tempImg);
//    }
//
//    public void setUserID(String userID) {
//        this.userID = userID;
//    }


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}

