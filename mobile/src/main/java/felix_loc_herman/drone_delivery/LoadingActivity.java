package felix_loc_herman.drone_delivery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class LoadingActivity extends AppCompatActivity {

    /*  This class is now a placeholder for the background process that will control
        what happends when the user has already signed in and relaunches the app. For
        now it is a visible activity.
     */

    private final String TAG = this.getClass().getSimpleName();

    private String userID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
    }

    //region Button XML callbacks

    public void toMainActivityClicked(View view){

        //region Get user profile

        // Create a pseudo-random user profile for testing purposes and launch activity main
        // TODO: Get profile from local storage in future, or background-process magic

        userID = "DonaldTrump";

        //endregion

        //region Start Main activity with said user profile

        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        // TODO: Fix when main fragments are implemented
        intent.putExtra(MainReceiverFragment.USER_ID, userID);
        startActivity(intent);

        //endregion

    }

    public void toLoginActivityClicked(View view){
        Log.v(TAG, "To Login Activity button clicked");

        Intent intent = new Intent(LoadingActivity.this, LoginActivity.class);
        LoadingActivity.this.startActivity(intent);

    }


    //endregion

}
