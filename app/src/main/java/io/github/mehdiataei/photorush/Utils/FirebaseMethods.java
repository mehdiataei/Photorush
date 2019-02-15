package io.github.mehdiataei.photorush.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import io.github.mehdiataei.photorush.Models.User;
import io.github.mehdiataei.photorush.Profile.ProfileActivity;
import io.github.mehdiataei.photorush.R;

/**
 * Created by User on 6/26/2017.
 */

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private DocumentReference docRef;

    private static final String USERNAME_KEY = "username";
    private static final String EMAIL_KEY = "email";
    private static final String BIO_KEY = "bio";
    private static final String USERID_KEY = "user_id";

    private Context mContext;

    public FirebaseMethods(Context context) {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        docRef = db.collection("cities");
        mContext = context;


        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    /**
     * Register a new email and password to Firebase Authentication
     *
     * @param email
     * @param password
     * @param username
     */
    public void registerNewEmail(final String email, String password, final String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        } else if (task.isSuccessful()) {
                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: Authstate changed: " + userID);
                        }

                    }
                });
    }


    public void addNewUser(String email, String username, String bio, String profile_photo) {

        Map<String, Object> newUser = new HashMap<>();
        newUser.put(USERNAME_KEY, username);
        newUser.put(EMAIL_KEY, email);
        newUser.put(BIO_KEY, bio);
        newUser.put(USERID_KEY, userID);
        Log.d(TAG, "Adding info to the database");

        db.collection("Users").document(mAuth.getCurrentUser().getUid()).set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User info added to the database.");


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.w(TAG, "Error adding user info to the database.", e);

                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Intent i = new Intent(mContext, ProfileActivity.class);
                mContext.startActivity(i);

            }
        });
    }

    private User getUserSettings() {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase.");


        final User user = new User();

        Log.d(TAG, "readSingleUser: " + userID);
        DocumentReference userInfo = db.collection("Users").document(userID);
        userInfo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    user.setUsername(doc.get("username").toString());
                    user.setBio(doc.get("bio").toString());

                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

        return user;
    }

}