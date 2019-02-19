package io.github.mehdiataei.photorush.Profile;


import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import io.github.mehdiataei.photorush.Login.LoginActivity;
import io.github.mehdiataei.photorush.Models.User;
import io.github.mehdiataei.photorush.R;
import io.github.mehdiataei.photorush.Utils.BottomNavigationViewHelper;
import io.github.mehdiataei.photorush.Utils.GlideApp;
import io.github.mehdiataei.photorush.Utils.MyRecyclerViewAdapter;
import io.github.mehdiataei.photorush.Utils.SquareImageView;
import io.github.mehdiataei.photorush.Utils.GlideImageLoader;
import io.github.mehdiataei.photorush.Models.Photo;

/**
 * Created by User on 8/12/2017.
 */

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    //widgets
    private SquareImageView mPostImage;
    private BottomNavigationView bottomNavigationView;
    private TextView mBackLabel, mCaption, mUsername;
    private ImageView mBackArrow, mProfileImage;
    private ProgressBar mProgressbar;
    private Context mContext;
    private User mUser;


    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //vars
    private Photo mPhoto;
    private int mActivityNumber = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        mPostImage = view.findViewById(R.id.post_image);
        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        mBackArrow = view.findViewById(R.id.backArrow);
        mBackLabel = view.findViewById(R.id.tvBackLabel);
        mCaption = view.findViewById(R.id.image_caption);
        mUsername = view.findViewById(R.id.username);
        mProfileImage = view.findViewById(R.id.profile_photo);
        mProgressbar = view.findViewById(R.id.view_post_progressbar);

        try {
            mPhoto = getPhotoFromBundle();

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .error(R.drawable.ic_error)
                    .priority(Priority.HIGH);

            new GlideImageLoader(mPostImage,
                    mProgressbar).load(mPhoto.getImage_path(), options);

            mActivityNumber = getActivityNumFromBundle();

        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
            }
        });


        getPhotoDetails();

        setupBottomNavigationView();

        return view;
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
            }

        };

    }

    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if (user == null) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void getPhotoDetails() {
        Log.d(TAG, "getPhotoDetails: Retrieving photo details.");

        //final ArrayList<User> user = new ArrayList<>();

        mCaption.setText(mPhoto.getCaption());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query query = db
                .collection(getString(R.string.dbname_users))
                .whereEqualTo("user_id", mPhoto.getUser_id());

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            if (task.getResult() != null) {

                                for (QueryDocumentSnapshot document : task.getResult()) {


                                    mUser = document.toObject(User.class);
                                }
                                setupWidgets();

                            }
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Failed to get user info for view photo.");
            }
        });

    }


    private void setupWidgets() {

        GlideApp.with(getActivity()).load(mUser.getProfile_photo()).into(mProfileImage);
        mUsername.setText(mUser.getUsername());
    }

    /**
     * retrieve the activity number from the incoming bundle from profileActivity interface
     *
     * @return
     */
    private int getActivityNumFromBundle() {
        Log.d(TAG, "getActivityNumFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getInt(getString(R.string.activity_number));
        } else {
            return 0;
        }
    }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     *
     * @return
     */
    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity(), bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }
}