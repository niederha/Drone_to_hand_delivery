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

    public void     toCreteFormActivityClicked (View view){
        Log.v(TAG, "To CreateForm Activity button clicked");

        Intent intent = new Intent(LoadingActivity.this, CreateFormActivity.class);
        intent.putExtra("username","fake_sender_username");
        intent.putExtra("receiver_username","fake_receiver_username");
        //DroneHandler droneHandler=new DroneHandler(getApplicationContext());  //TODO : uncomment
       // intent.putExtra("droneHandler",droneHandler);  //TODO : uncomment
        startActivity(intent);

    }

    public void     toMapActivityClicked (View view){
        Log.v(TAG, "To Map Activity button clicked");

        Intent intent = new Intent(LoadingActivity.this, MapActivity.class);
        intent.putExtra("username","fake_sender_username");
        intent.putExtra("receiver_username","fake_receiver_username");
        //DroneHandler droneHandler=new DroneHandler(getApplicationContext());  //TODO : uncomment
        //intent.putExtra("droneHandler",droneHandler);  //TODO : uncomment
        Log.e("LOADING_ACTIVITY","Warning : in order for this button to work properly, the delivery from fake_sender_username to fake_receiver_username has to be already set properly in firebase (otherways you might get errors because things are not initialized properly)");
        startActivity(intent);

    }




    //endregion

}
