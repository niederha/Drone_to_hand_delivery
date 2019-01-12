package felix_loc_herman.drone_delivery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
    }

    public void toMainActivityClicked(View view){

        Toast.makeText(this, "Remove this button :)", Toast.LENGTH_SHORT).show();

    }

    public void toLoginActivityClicked(View view){
        Log.v(TAG, "To Login Activity button clicked");

        Intent intent = new Intent(LoadingActivity.this, LoginActivity.class);
        LoadingActivity.this.startActivity(intent);

    }



}
