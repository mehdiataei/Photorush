package io.github.mehdiataei.photorush.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.github.mehdiataei.photorush.Models.Photo;
import io.github.mehdiataei.photorush.Models.User;
import io.github.mehdiataei.photorush.Profile.ProfileActivity;
import io.github.mehdiataei.photorush.R;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;

    private static final String USERNAME_KEY = "username";
    private static final String EMAIL_KEY = "email";
    private static final String BIO_KEY = "bio";
    private static final String USERID_KEY = "user_id";
    private static final String PROFILE_PHOTO_KEY = "profile_photo";

    private Context mContext;

    private double mPhotoUploadProgress = 0;


    public FirebaseMethods(Context context) {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

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


    public void addNewUser(String email, String username, String bio) {

        Map<String, Object> newUser = new HashMap<>();
        newUser.put(USERNAME_KEY, username);
        newUser.put(EMAIL_KEY, email);
        newUser.put(BIO_KEY, bio);
        newUser.put(USERID_KEY, mAuth.getCurrentUser().getUid());
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
        DocumentReference userInfo = db.collection("Users").document(mAuth.getCurrentUser().getUid());
        userInfo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    user.setUsername(doc.get("username").toString());
                    user.setBio(doc.get("bio").toString());
                    user.setEmail(doc.get("email").toString());
                    user.setUser_id(doc.get("user_id").toString());
                    user.setProfile_photo(doc.get("profile_photo").toString());

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


    public void uploadNewPhoto(String photoType, final String caption, final int count, final Uri imgUrl,
                               Bitmap bm) {
        Log.d(TAG, "uploadNewPhoto: attempting to uplaod new photo.");

        FilePaths filePaths = new FilePaths();
        //case1) new photo
        if (photoType.equals(mContext.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading NEW photo.");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageRef
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));

            //convert image url to bitmap
            if (bm == null) {
                try {
                    Log.d(TAG, "uploadNewPhoto: Downloading the image from Uri.");
                    bm = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imgUrl);
                    bm = getResizedBitmap(bm, 1024);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            byte[] bytes = ImageUtils.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            //Do what you want with the url
                            //add the new photo to 'photos' node and 'user_photos' node
                            Log.d(TAG, "onSuccess: Adding photo to the database.");
                            addPhotoToDatabase(caption, downloadUrl.toString());

                        }
                    });

                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();



                }

            }).

                    addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Photo upload failed.");
                            Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                        }
                    }).

                    addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            if (progress - 15 > mPhotoUploadProgress) {
                                Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                                mPhotoUploadProgress = progress;
                            }

                            Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                        }
                    }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    //navigate to the main feed so the user can see their photo
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.putExtra(mContext.getString(R.string.photo_added), true);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {

                    }
                    mContext.startActivity(intent);
                }
            });
        }//case new profile photo
        else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new PROFILE photo");


            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageRef
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            //convert image url to bitmap
            if (bm == null) {

                try {
                    Log.d(TAG, "uploadNewPhoto: Downloading the image from Uri.");
                    bm = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imgUrl);
                    bm = getResizedBitmap(bm, 1024);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            byte[] bytes = ImageUtils.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            //Do what you want with the url
                            //add the new photo to 'photos' node and 'user_photos' node
                            addPhotoToDatabase(caption, downloadUrl.toString());

                            //insert into 'Users' node
                            setProfilePhoto(downloadUrl.toString());


                        }
                    });

                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });
        }
    }


    private void addPhotoToDatabase(String caption, String url) {
        Log.d(TAG, "addPhotoToDatabase: Adding photo to database.");

        String newPhotoKey = db.collection(mContext.getString(R.string.dbname_photos)).document().getId();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimestamp());
        photo.setImage_path(url);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        //insert into database


        db.collection(mContext.getString(R.string.dbname_user_photos)).document(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).set(photo);
        db.collection(mContext.getString(R.string.dbname_photos)).document(newPhotoKey).set(photo);

    }

    private String getTimestamp() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(new Date());

        return timeStamp;
    }

    private void setProfilePhoto(String url) {
        Log.d(TAG, "setProfilePhoto: Setting new profile image: " + url);

        Map<String, Object> docData = new HashMap<>();

        docData.put(mContext.getString(R.string.profile_photo), url);

        db.collection(mContext.getString(R.string.dbname_users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update(docData);

    }


    public void addThumbnail(Bitmap bitmap, boolean profilePic_taken) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (profilePic_taken) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        } else {

            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.thumbnail);
        }

        byte[] data = baos.toByteArray();

        FilePaths paths = new FilePaths();

        final StorageReference thumbnailImagesRef = mStorageRef.child(paths.FIREBASE_IMAGE_STORAGE + mAuth.getCurrentUser().getUid() + "/thumbnail.jpg");

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

                thumbnailImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUrl = uri;
                        //Do what you want with the url
                        //add the new photo to 'photos' node and 'user_photos' node
                        Log.d(TAG, "onSuccess: Adding photo to the database.");
                        setProfilePhoto(downloadUrl.toString());

                    }
                });
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d(TAG, "onSuccess: Thumbnail uploaded. ");
            }
        });
    }


    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public int getImageCount(QuerySnapshot snapshot) {
        int count = 0;

        for (QueryDocumentSnapshot document : snapshot) {
            count++;
        }

        return count;
    }
}