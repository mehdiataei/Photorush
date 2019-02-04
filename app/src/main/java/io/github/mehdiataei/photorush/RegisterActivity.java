package io.github.mehdiataei.photorush;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import io.github.mehdiataei.photorush.Utils.FirebaseMethods;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    static final int REQUEST_IMAGE_CAPTURE = 1;


    private EditText inputEmail;
    private EditText inputUsername;
    private EditText inputPassword;
    private EditText inputPasswordConfirm;
    private EditText inputShortBio;
    private String email, username, password, bio, userID;
    private Button signupButton;
    private ProgressBar mProgressBar;


    Bitmap imageBitmap;
    Bitmap profilePic_thumbnail;
    boolean profilePic_taken;


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorageRef;

    private static final String USERNAME_KEY = "username";
    private static final String EMAIL_KEY = "email";
    private static final String BIO_KEY = "bio";
    private static final String USERID_KEY = "user_id";


    FirebaseFirestore db;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    //"(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                    //"(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 4 characters
                    "$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initWidgets();
        validateListeners();
        configureCaptureButton();
        setupFirebaseAuth();
        init();

    }


    private void validateListeners() {

// Assign edit text listeners to check formating for each field

        inputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                validateEmail();

            }
        });

        inputUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                validateUsername();

            }
        });

        inputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                validatePassword();

            }
        });
        inputPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                validatePasswordConfirm();

            }
        });

        inputShortBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                validateShortBio();

            }
        });

    }

    private void initWidgets() {
        Log.d(TAG, "initWidgets: Initializing Widgets.");
        inputEmail = findViewById(R.id.email_register);
        inputUsername = findViewById(R.id.username_register);
        signupButton = findViewById(R.id.login_button);
        mProgressBar = findViewById(R.id.ProgressBarReg);
        inputPassword = findViewById(R.id.password_register);
        inputPasswordConfirm = findViewById(R.id.password_register_confirm);
        inputShortBio = findViewById(R.id.shortBio_register);
        mProgressBar.setVisibility(View.GONE);
        db = FirebaseFirestore.getInstance();


    }

    private boolean validateEmail() {
        String emailInput = inputEmail.getText().toString().trim();

        if (emailInput.isEmpty()) {
            inputEmail.setError("Field can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            inputEmail.setError("Please enter a valid email address");
            return false;
        } else {
            inputEmail.setError(null);
            return true;
        }
    }


    private boolean validateUsername() {
        String usernameInput = inputUsername.getText().toString().trim();

        if (usernameInput.isEmpty()) {
            inputUsername.setError("Field cannot be empty");
            return false;
        } else if (usernameInput.length() > 15) {
            inputUsername.setError("Username is too long");
            return false;
        } else {
            inputUsername.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = inputPassword.getText().toString().trim();

        if (passwordInput.isEmpty()) {
            inputPassword.setError("Field cannot be empty");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            inputPassword.setError("Password is too weak");
            return false;
        } else {
            inputPassword.setError(null);
            return true;
        }
    }

    private boolean validatePasswordConfirm() {
        String passwordInput = inputPassword.getText().toString().trim();
        String passwordInputConfirm = inputPasswordConfirm.getText().toString().trim();
        Log.d(TAG, passwordInput);


        if (passwordInput.equals(passwordInputConfirm)) {

            inputPasswordConfirm.setError(null);
            return true;

        } else {

            inputPasswordConfirm.setError("Passwords do not match");
            return false;

        }

    }

    private boolean validateShortBio() {

        String inputBio = inputShortBio.getText().toString().trim();

        if (inputBio.length() < 50) {

            inputShortBio.setError(null);
            return true;

        } else {

            inputShortBio.setError("Bio is too long");
            return false;

        }

    }

    // Confirm all inputs

    public boolean confirmAllInputs() {
        if (!validateEmail() | !validateUsername() | !validatePassword() | !validatePasswordConfirm() | !validateShortBio()) {
            return false;
        } else {

            return true;
        }
    }


    /*
    ------------------------------------ Camera ---------------------------------------------
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // Retreive info from the camera

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            CircleImageView profilePic = findViewById(R.id.profile_picture_register);
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            profilePic_taken = true;
            profilePic.setImageBitmap(imageBitmap);

        }
    }

// Check field validations and communicate with the profile activity

    private void configureSignUpButton() {


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                boolean test = confirmAllInputs();

                Log.d(TAG, String.valueOf(test));

                if (test) {

                    Intent i = new Intent(RegisterActivity.this, HomeActivity.class);
                    i.putExtra("bio", inputShortBio.getText().toString());
                    i.putExtra("username", inputUsername.getText().toString());
                    i.putExtra("profile pic taken", profilePic_taken);
                    if (profilePic_taken) {
                        i.putExtra("profile picture", profilePic_thumbnail);
                    }
                    startActivity(i);

                }

            }
        });

    }

// Lunch camera

    private void configureCaptureButton() {

        Button captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dispatchTakePictureIntent();

            }
        });

    }


    /*
    ------------------------------------ Firebase ---------------------------------------------
     */


    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

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
    }


    private void init() {
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = inputEmail.getText().toString();
                password = inputPassword.getText().toString();
                username = inputUsername.getText().toString();
                bio = inputShortBio.getText().toString();

                if (confirmAllInputs()) {
                    Log.d(TAG, "onClick: registering user");
                    mProgressBar.setVisibility(View.VISIBLE);
                    //firebaseMethods.registerNewEmail(email, password, username);
                    //userID = mAuth.getCurrentUser().getUid();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Login failed",
                                                Toast.LENGTH_SHORT).show();

                                    } else if (task.isSuccessful()) {
                                        userID = mAuth.getCurrentUser().getUid();
                                        Log.d(TAG, "onComplete: Authstate changed: " + userID);
                                        addNewUser(username, email, bio);

                                    }

                                }
                            });

                }
            }
        });
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


    /*
    ------------------------------------ Database ---------------------------------------------
     */
    private void addNewUser(String username, String email, String bio) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put(USERNAME_KEY, username);
        newUser.put(EMAIL_KEY, email);
        newUser.put(BIO_KEY, bio);
        newUser.put(USERID_KEY, userID);
        Log.d(TAG, "Adding info to the database");

        db.collection("Users").document(userID).set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot added");

                        addThumbnail();

                        Intent i = new Intent(RegisterActivity.this, HomeActivity.class);
                        startActivity(i);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.w(TAG, "Error adding document", e);


                    }
                });
    }

    private void addThumbnail() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference thumbnailImagesRef = mStorageRef.child(userID + "/thumbnail.jpg");

        UploadTask uploadTask = thumbnailImagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "onFailure: Thumbnail upload failed. ");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d(TAG, "onSuccess: Thumbnail uploaded. ");
            }
        });
    }

}
