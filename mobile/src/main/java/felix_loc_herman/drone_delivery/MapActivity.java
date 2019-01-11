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

    private DroneHandler droneHandler;
    private String sender_username;
    private String receiver_username;
    private DatabaseReference deliveryRef;
    private double distance;
    private double ETA;
    private double drone_longitude;
    private double drone_latitude;
    private boolean cancelled_by_receiver;
    private int status;

    private final int DRONE_FLYING_TO_RECEIVER=5;
    private final int DRONE_NEAR_RECEIVER=6;
    private final int DRONE_LANDING_AT_RECEIVER=7;
    private final int DRONE_LANDED_AT_RECEIVER=8;
    private final int DRONE_FLYING_BACK_TO_SENDER=9;
    private final int DRONE_LANDED_AT_SENDER=10;

    public static final String RECEIVE_LOCATION = "RECEIVE_LOCATION";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String LATITUDE = "LATITUDE";
    //public static final String RECEIVE_DATA_EXERCISE = "RECEIVE_DATA_EXERCISE";
    private final String TAG = this.getClass().getSimpleName();

    //private ArrayList<Integer> hrDataArrayList = new ArrayList<>();


    private GoogleMap mMap;
    private DatabaseReference recordingRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cancelled_by_receiver=false;
        status=DRONE_FLYING_TO_RECEIVER;

        setContentView(R.layout.activity_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.GoogleMap);
        mapFragment.getMapAsync(this);

        //get extras from intent
        Bundle b=getIntent().getExtras();
        sender_username=b.getString("username");
        receiver_username=b.getString("receiver_username");
        droneHandler=new DroneHandler(getApplicationContext());//TODO : initialize drone handler with the one received as an intent istead




        //connect to firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference deliveryGetRef = database.getReference("deliveries");
        deliveryRef = deliveryGetRef.child(sender_username);

        deliveryRef.addValueEventListener(new MapActivity.DeliveryUpdateEventListener(this));

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

    }

    public void activityMap_Cancelbutton(View view) {
        //TODO
    }

    public void updateMap()
    {
        //TODO : complete

        // Update map
        LatLng currentLocation = new LatLng(drone_latitude, drone_longitude);
        Log.e(TAG, "Current location: " + currentLocation);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentLocation)
                .title("my marker"));
    }

    private class DeliveryUpdateEventListener implements ValueEventListener {         //listener to listen to changes in firebase
        private Context context;

        DeliveryUpdateEventListener(Context context) {
            this.context=context;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            double new_distance=dataSnapshot.child("distance").getValue(Double.class).doubleValue();     //retrieve distance
            double new_ETA=dataSnapshot.child("ETA").getValue(Double.class).doubleValue();     //retrieve ETA
            double new_drone_latitude=dataSnapshot.child("drone_GPS").child("north").getValue(Double.class).doubleValue();
            double new_drone_longitude=dataSnapshot.child("drone_GPS").child("east").getValue(Double.class).doubleValue();
            boolean new_cancelled_by_receiver=dataSnapshot.child("cancelled").getValue(Boolean.class).booleanValue();     //retrieve cancelling status
            int new_status=dataSnapshot.child("status").getValue(Integer.class).intValue();

            if(new_cancelled_by_receiver && !cancelled_by_receiver) {    //the receiver cancelled the delivery
                cancelled_by_receiver=true;
                //TODO : make drone fly back to sender
            }


            if(new_ETA!=ETA)
            {
                ETA=new_ETA;
                TextView tv_ETA=(TextView) findViewById(R.id.activityMap_ETA_textView);
                tv_ETA.setText(String.valueOf(ETA));
            }

            if(new_distance!=distance)
            {
                distance=new_distance;
                TextView tv_dist=(TextView) findViewById(R.id.activityMap_distance_textView);
                tv_dist.setText(String.valueOf(distance));
            }

            if(new_drone_longitude!=drone_longitude || new_drone_latitude!=drone_latitude)
            {
                drone_latitude=new_drone_latitude;
                drone_longitude=new_drone_latitude;
                updateMap();
            }

            if(new_status!=status)
            {
                status=new_status;
                if(status==DRONE_NEAR_RECEIVER)
                {
                    //TODO
                }
                else if(status==DRONE_LANDING_AT_RECEIVER)
                {
                    //TODO
                }
                else if(status==DRONE_LANDED_AT_RECEIVER)
                {
                    //TODO
                }
                else if(status==DRONE_FLYING_BACK_TO_SENDER)
                {
                    //TODO
                }
                else if(status==DRONE_FLYING_BACK_TO_SENDER)
                {
                    //TODO
                }
            }

            //TODO : determine when and how to change status

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    }
}



//TODO : include changes in receiver position
//TODO : manage timeout of receiver GPS (ie make the drone fly back if no GPS signal received from receiver for more than a given time
