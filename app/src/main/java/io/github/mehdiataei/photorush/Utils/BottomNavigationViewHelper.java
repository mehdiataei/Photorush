package io.github.mehdiataei.photorush.Utils;

import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

//import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import io.github.mehdiataei.photorush.Profile.ProfileActivity;
import io.github.mehdiataei.photorush.R;
import io.github.mehdiataei.photorush.Feed.FeedActivity;


/**
 * Created by User on 5/28/2017.
 */

public class BottomNavigationViewHelper {

    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationView bottomNavigationView) {
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");

    }

    public static void enableNavigation(final Context context, BottomNavigationView view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.ic_profile:
                        Intent intent1 = new Intent(context, ProfileActivity.class);//ACTIVITY_NUM = 0
                        context.startActivity(intent1);
                        break;

                    case R.id.ic_feed:
                        Intent intent2 = new Intent(context, FeedActivity.class);//ACTIVITY_NUM = 1
                        context.startActivity(intent2);
                        break;

                }


                return false;
            }
        });
    }
}