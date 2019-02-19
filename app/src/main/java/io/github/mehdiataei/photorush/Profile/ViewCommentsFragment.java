package io.github.mehdiataei.photorush.Profile;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.Locale;

import io.github.mehdiataei.photorush.Login.LoginActivity;
import io.github.mehdiataei.photorush.Models.Comment;
import io.github.mehdiataei.photorush.Models.Photo;
import io.github.mehdiataei.photorush.R;
import io.github.mehdiataei.photorush.Utils.CommentListAdapter;

/**
 * Created by User on 8/12/2017.
 */

public class ViewCommentsFragment extends Fragment {

    public ViewCommentsFragment() {
        super();
        setArguments(new Bundle());
    }


    private static final String TAG = "ViewCommentsFragment";

    //vars
    private Photo mPhoto;
    private ArrayList<Comment> mComments;
    ImageView mBackButton, mCheckmark;
    private ListView mListView;
    TextView mComment;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseFirestore db;

    private Context mContext;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);

        mBackButton = view.findViewById(R.id.ivBackArrow);
        mCheckmark = view.findViewById(R.id.ivPostComment);
        mListView = view.findViewById(R.id.listView);
        db = FirebaseFirestore.getInstance();
        mContext = getActivity();
        mComment = view.findViewById(R.id.comment);


        try {
            mPhoto = getPhotoFromBundle();

        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }


        setupFirebaseAuth();


        return view;
    }


    private void setupWidgets() {

        CommentListAdapter adapter = new CommentListAdapter(mContext,
                R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);

        mCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mComment.getText().toString().equals("")) {
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(mComment.getText().toString());

                    mComment.setText("");
                    closeKeyboard();
                } else {
                    Toast.makeText(getActivity(), "you can't post a blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
//
//        mBackArrow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: navigating back");
//                if(getCallingActivityFromBundle().equals(getString(R.string.home_activity))){
//                    getActivity().getSupportFragmentManager().popBackStack();
//                    ((HomeActivity)getActivity()).showLayout();
//                }else{
//                    getActivity().getSupportFragmentManager().popBackStack();
//                }
//
//            }
//        });
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void addNewComment(String newComment) {
        Log.d(TAG, "addNewComment: Adding new comment: " + newComment);


        DocumentReference myRef = db.collection(mContext.getString(R.string.dbname_photos))
                .document(mPhoto.getPhoto_id())
                .collection(mContext.getString(R.string.dbname_comments))
                .document();

        String commentID = myRef.getId();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //insert into photos node


        db.collection(mContext.getString(R.string.dbname_photos))
                .document(mPhoto.getPhoto_id())
                .collection(mContext.getString(R.string.dbname_comments))
                .document(commentID)
                .set(comment);

    }


    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA);
        return sdf.format(new Date());
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


        mComments = new ArrayList<>();
        final Comment firstcomment = new Comment();
        mComments.clear();

        firstcomment.setComment(mPhoto.getCaption());
        firstcomment.setUser_id(mPhoto.getUser_id());
        firstcomment.setDate_created("20110219_032628");
        mComments.add(firstcomment);


        final Query query = db
                .collection(getString(R.string.dbname_photos)).document(mPhoto.getPhoto_id()).collection(mContext.getString(R.string.dbname_comments));

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                mComments.clear();
                mComments.add(firstcomment);


                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    mComments.add(document.toObject(Comment.class));
                }

                mComments.sort(new Comparator<Comment>() {
                    @Override
                    public int compare(Comment o1, Comment o2) {
                        try {

                            Log.d(TAG, "compare: Comments are: " + o1 + "  " + o2);

                            Date o1_date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).parse(o1.getDate_created());
                            Date o2_date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).parse(o2.getDate_created());


                            return o1_date.compareTo(o2_date);

                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }
                });


                mPhoto.setComments(mComments);

                setupWidgets();

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

    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if (user == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    }

}




