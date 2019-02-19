package io.github.mehdiataei.photorush.Feed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import io.github.mehdiataei.photorush.Login.LoginActivity;
import io.github.mehdiataei.photorush.Models.Photo;
import io.github.mehdiataei.photorush.Profile.ViewPostFragment;
import io.github.mehdiataei.photorush.R;
import io.github.mehdiataei.photorush.Utils.BottomNavigationViewHelper;
import io.github.mehdiataei.photorush.Utils.MyRecyclerViewAdapter;

public class FeedFragment extends Fragment implements MyRecyclerViewAdapter.ItemClickListener {


    private static final String TAG = "ProfileFragment";

    private MyRecyclerViewAdapter adapter;
    RecyclerView recyclerView;

    private static final int NUM_OF_COLUMNS = 1;

    private static final int ACTIVITY_NUM = 1;

    private Context mContext;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseFirestore db;

    private BottomNavigationView bottomNavigationView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        db = FirebaseFirestore.getInstance();

        mContext = getActivity();

        setupFirebaseAuth();
        setupBottomNavigationView();
        setupGridView(this);
        recyclerView = view.findViewById(R.id.rvNumbers);

        return view;
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i("TAG", "You clicked number " + adapter.getItem(position) + ", which is at cell position " + position);

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), adapter.getItem(position));
        args.putInt(getString(R.string.activity_number), ACTIVITY_NUM);
        fragment.setArguments(args);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();

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


    private void setupGridView(final FeedFragment fragment) {
        Log.d(TAG, "setupGridView: Setting up image grid in feed fragment.");

        final ArrayList<Photo> photos = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query query = db
                .collection(getString(R.string.dbname_photos));

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    photos.add(document.toObject(Photo.class));
                }

                // Sort photos chronologically
                photos.sort(new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {

                        try {

                            Date o1_date = new SimpleDateFormat("yyyyMMdd_HHmmss").parse(o1.getDate_created());
                            Date o2_date = new SimpleDateFormat("yyyyMMdd_HHmmss").parse(o2.getDate_created());

                            int compare = o2_date.compareTo(o1_date);

                            return compare;

                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }

                });
                // set up the RecyclerView
                recyclerView.setLayoutManager(new GridLayoutManager(mContext, NUM_OF_COLUMNS, GridLayoutManager.VERTICAL, false));
                adapter = new MyRecyclerViewAdapter(mContext, photos);

                recyclerView.setAdapter(adapter);

                adapter.setClickListener(fragment);
            }
        });

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
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), bottomNavigationView);
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
}