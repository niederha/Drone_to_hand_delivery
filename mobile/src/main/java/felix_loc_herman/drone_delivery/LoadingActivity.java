package felix_loc_herman.drone_delivery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class LoadingActivity extends AppCompatActivity {

    /*  This class is now a placeholder for the background process that will control
        what happends when the user has already signed in and relaunches the app. For
        now it is a visible activity.
     */

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    public void toMainActivityClicked(View view){

        Toast.makeText(this, "Remove this button :)", Toast.LENGTH_SHORT).show();

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
        DroneHandler droneHandler=new DroneHandler(getApplicationContext());
        intent.putExtra("droneHandler",droneHandler);
        startActivity(intent);

    }

    public void     toRecevingActivityClicked (View view){
        Log.v(TAG, "To RecevingActivity Activity button clicked");

        Intent intent = new Intent(LoadingActivity.this, ReceivingActivity.class);
        intent.putExtra("sender_name","Shrek");
        intent.putExtra("receiver_username","felix");
        DroneHandler droneHandler=new DroneHandler(getApplicationContext());
        intent.putExtra("droneHandler",droneHandler);
        startActivity(intent);

    }

    public void     toMapActivityClicked (View view){
        Log.v(TAG, "To Map Activity button clicked");

        Intent intent = new Intent(LoadingActivity.this, MapActivity.class);
        intent.putExtra("username","Shrek");
        intent.putExtra("receiver_username","felix");
        DroneHandler droneHandler=new DroneHandler(getApplicationContext());
        intent.putExtra("droneHandler",droneHandler);
        Log.e("LOADING_ACTIVITY","Warning : in order for this button to work properly, the delivery from fake_sender_username to fake_receiver_username has to be already set properly in firebase (otherways you might get errors because things are not initialized properly)");
        startActivity(intent);

    }


}
