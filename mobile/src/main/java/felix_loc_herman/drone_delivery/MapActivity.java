package felix_loc_herman.drone_delivery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.graphics.Color.RED;
import static android.graphics.Color.TRANSPARENT;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    public static final String RECEIVE_LOCATION = "RECEIVE_LOCATION";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String LATITUDE = "LATITUDE";
    public static final String RECEIVE_DATA_EXERCISE =
            "RECEIVE_DATA_EXERCISE";
    private final String TAG = this.getClass().getSimpleName();

    private ArrayList<Integer> hrDataArrayList = new ArrayList<>();

    private LocationBroadcastReceiver locationBroadcastReceiver;
    private HeartRateBroadcastReceiver heartRateBroadcastReceiver;

    private GoogleMap mMap;
    private DatabaseReference recordingRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Obtain the SupportMapFragment and get notified when the map is
        // ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.GoogleMap);
        mapFragment.getMapAsync(this);

        Intent intentFromRec = getIntent();
        //String userID = intentFromRec.getStringExtra(MyProfileFragment.USER_ID);
        //String recID = intentFromRec.getStringExtra(NewRecordingFragment             .RECORDIND_ID);

    /*

        // Get recording information from Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference profileGetRef = database.getReference("profiles");
        recordingRef = profileGetRef.child(userID).child("recordings").child
                (recID);

        recordingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView exerciseType = findViewById(R.id.exerciseTypeLive);
                exerciseType.setText(dataSnapshot.child("exercise_type")
                        .getValue().toString());
                TextView exerciseDatetime = findViewById(R.id
                        .exerciseDateTimeLive);
                Long datetime = Long.parseLong(dataSnapshot.child("datetime")
                        .getValue().toString
                                ());
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy" +
                        " hh:mm:ss", Locale
                        .getDefault());
                exerciseDatetime.setText(formatter.format(new Date(datetime)));
                String switchWatch = dataSnapshot.child("switch_watch")
                        .getValue().toString();
                String switchBelt = dataSnapshot.child("switch_hr_belt")
                        .getValue().toString();
                TextView hrWatch = findViewById(R.id.exerciseHRwatchLive);
                hrWatch.setText(switchWatch);
                TextView hrBelt = findViewById(R.id.exerciseHRbeltLive);
                hrBelt.setText(switchBelt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get the HR data back from the watch
        heartRateBroadcastReceiver = new HeartRateBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver
                (heartRateBroadcastReceiver, new
                        IntentFilter("RECEIVE_HEART_RATE"));

        // Get the location data back from the watch
        locationBroadcastReceiver = new LocationBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver
                (locationBroadcastReceiver, new
                        IntentFilter(RECEIVE_LOCATION));

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(heartRateBroadcastReceiver);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationBroadcastReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Lausanne
        double latitude = 46.5197;
        double longitude = 6.6323;

        // Add a marker in Sydney and move the camera
        LatLng currentLocation = new LatLng(latitude, longitude);
        Log.e(TAG, "Current location: " + currentLocation);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("my marker"));

        TextView longitudeTextView = findViewById(R.id.longitude);
        longitudeTextView.setText(String.valueOf(longitude));
        TextView latitudeTextView = findViewById(R.id.latitude);
        latitudeTextView.setText(String.valueOf(latitude));
    }



    private class HeartRateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Show HR in a TextView
            int heartRateWatch = intent.getIntExtra("HEART_RATE", -1);
            TextView hrTextView = findViewById(R.id.exerciseHRwatchLive);
            hrTextView.setText(String.valueOf(heartRateWatch));

//            // Add HR value to HR ArrayList
//            hrDataArrayList.add(heartRateWatch);


        }
    }

    private class LocationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Update TextViews
            double longitude = intent.getDoubleExtra(LONGITUDE, -1);
            double latitude = intent.getDoubleExtra(LATITUDE, -1);

            TextView longitudeTextView = findViewById(R.id.longitude);
            longitudeTextView.setText(String.valueOf(longitude));

            TextView latitudeTextView = findViewById(R.id.latitude);
            latitudeTextView.setText(String.valueOf(latitude));

            // Update map
            LatLng currentLocation = new LatLng(latitude, longitude);
            Log.e(TAG, "Current location: " + currentLocation);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(currentLocation)
                    .title("my marker"));
        }
    }
}


































/*import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}*/
