package felix_loc_herman.drone_delivery;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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


public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private DroneHandler droneHandler;
    private String sender_username;
    private String receiver_username;
    private DatabaseReference deliveryRef;
    private DatabaseReference receiverGPSRef;
    private double distance;
    private double ETA;
    private double drone_longitude;
    private double drone_latitude;
    private double receiver_longitude;
    private double receiver_latitude;
    private boolean cancelled_by_receiver;
    private int status;
    private int droneStatus;

    private final int DRONE_FLYING_TO_RECEIVER=5;
    private final int DRONE_NEAR_RECEIVER=6;
    private final int DRONE_LANDING_AT_RECEIVER=7;
    private final int DRONE_LANDED_AT_RECEIVER=8;
    private final int DRONE_FLYING_BACK_TO_SENDER=9;
    private final int DRONE_NEAR_SENDER=10;
    private final int DRONE_LANDING_AT_SENDER=11;
    private final int DRONE_LANDED_AT_SENDER=12;


    private final String TAG = this.getClass().getSimpleName();


    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cancelled_by_receiver=false;
        status=DRONE_FLYING_TO_RECEIVER;
        droneStatus=DroneHandler.IDLE; //TODO : or do we start in flying as we already took off?

        setContentView(R.layout.activity_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.GoogleMap);
        mapFragment.getMapAsync(this);

        //get extras from intent
        Bundle b=getIntent().getExtras();
        sender_username=b.getString("username");
        receiver_username=b.getString("receiver_username");
        droneHandler=(DroneHandler)b.getSerializable("droneHandler");




        //connect to firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference deliveryGetRef = database.getReference("deliveries");
        deliveryRef = deliveryGetRef.child(sender_username);
        deliveryRef.addValueEventListener(new MapActivity.DeliveryUpdateEventListener(this));

        DatabaseReference userGetRef = database.getReference("user");
        receiverGPSRef = userGetRef.child(receiver_username).child("GPS");
        receiverGPSRef.addValueEventListener(new MapActivity.ReceiverGPSUpdateEventListener(this));

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //TODO : initialize with drone and receiver positions (and correct zoom?)


        // Lausanne
        double latitude = 46.5197;
        double longitude = 6.6323;

        // Add a marker in Sydney and move the camera
        LatLng currentLocation = new LatLng(latitude, longitude);
        Log.e(TAG, "Current location: " + currentLocation);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("my marker"));

    }

    public void activityMap_Cancelbutton(View view) {   //note that this button is blocked once the drone started landing at receiver
        deliveryRef.child("cancelled").setValue(true);  //inform the receiver that we cancelled the delivery  //TODO : check if it threadsafe and correct
        Toast.makeText(this,"The delivery request has been cancelled successfully : the drone is now flying back to you",Toast.LENGTH_SHORT);
        applyStatusChange(DRONE_FLYING_BACK_TO_SENDER);
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

    private void applyStatusChange(int new_status)
    {
        Log.e("MapActivity","applying change in status");
        //TODO : add notifications? What if not in foregroung?
        if(status==DRONE_NEAR_RECEIVER)
        {
            TextView tv_state=(TextView) findViewById(R.id.activtyMap_state_textView);
            tv_state.setText("Drone near receiver : waiting for receivers permission to land");
        }
        else if(status==DRONE_LANDING_AT_RECEIVER) //TODO(@receiver) : change to DRONE_LANDING_AT_RECEIVER done by receiver
        {
            TextView tv_state=(TextView) findViewById(R.id.activtyMap_state_textView);
            tv_state.setText("Drone landing at receiver");
            droneHandler.land();

            //block cancel button
            Button btn = (Button) findViewById(R.id.activityMap_Cancelbutton);
            btn.setEnabled(false);
        }
        else if(status==DRONE_LANDED_AT_RECEIVER)
        {
            TextView tv_state=(TextView) findViewById(R.id.activtyMap_state_textView);
            tv_state.setText("Drone landed at receiver");

        }
        else if(status==DRONE_FLYING_BACK_TO_SENDER) //TODO(@receiver) : change to DRONE_FLYING_BACK_TO_SENDER done by receiver
        {
            TextView tv_state=(TextView) findViewById(R.id.activtyMap_state_textView);
            tv_state.setText("Drone flying back to sender");
            droneHandler.takeOff();
            droneHandler.getBack(); //TODO : replace by goTo?
            //TODO : check if getBack take into account changes of location of sender

        }
        else if(status==DRONE_NEAR_SENDER)
        {
            //TODO : ask permission to land : if granted : switch to DRONE_LANDING and call applyStatusChange(DRONE_LANDING_AT_SENDER)
        }
        else if(status==DRONE_LANDING_AT_SENDER)
        {
            TextView tv_state=(TextView) findViewById(R.id.activtyMap_state_textView);
            tv_state.setText("Drone is landing");
            droneHandler.land();;
        }
        else if(status==DRONE_LANDED_AT_SENDER)
        {
            //TODO : clear delivery data?
            //TODO : go to main activity or to delivery summary
        }
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
            int new_droneStatus=dataSnapshot.child("droneStatus").getValue(Integer.class).intValue();


            if(new_cancelled_by_receiver && !cancelled_by_receiver) {    //the receiver cancelled the delivery
                cancelled_by_receiver=true;
                Toast.makeText(context,"The receiver cancelled the delivery : the drone is now flying back to you",Toast.LENGTH_LONG);
                applyStatusChange(DRONE_FLYING_BACK_TO_SENDER);
            }
            else if(new_status!=status)
            {
                status=new_status;
                applyStatusChange(new_status);
            }
            else if(new_droneStatus!=droneStatus)
            {
                droneStatus=new_droneStatus;
                if(droneStatus==DroneHandler.WAITING_TO_LAND && status==DRONE_FLYING_TO_RECEIVER)    //the drone arrived near the receiver
                {
                    status=DRONE_NEAR_RECEIVER;
                    deliveryRef.child("status").setValue(DRONE_NEAR_RECEIVER);  //TODO : check if it threadsafe and correct
                    applyStatusChange(DRONE_NEAR_RECEIVER);
                }
                //order of landing : no condition there because it's the receiver who modifies firebase
                else if(droneStatus==DroneHandler.LANDED && status==DRONE_LANDING_AT_RECEIVER)  //the drone just landed at receiver
                {
                    status=DRONE_LANDED_AT_RECEIVER;
                    deliveryRef.child("status").setValue(DRONE_LANDED_AT_RECEIVER);  //TODO : check if it threadsafe and correct
                    applyStatusChange(DRONE_LANDED_AT_RECEIVER);
                }
                //order of taking off again : no condition there because it's the receiver who modifies firebase
                else if(droneStatus==DroneHandler.WAITING_TO_LAND && status==DRONE_FLYING_BACK_TO_SENDER)  //the drone just took off at receiver
                {
                    status=DRONE_NEAR_SENDER;
                    deliveryRef.child("status").setValue(DRONE_NEAR_SENDER);  //TODO : check if it threadsafe and correct
                    applyStatusChange(DRONE_NEAR_SENDER);
                }
                //TODO:order of landing at senders : todo by sender?
                else if(droneStatus==DroneHandler.LANDED && status==DRONE_LANDING_AT_RECEIVER)  //the drone just landed at sender
                {
                    status=DRONE_LANDED_AT_RECEIVER;
                    deliveryRef.child("status").setValue(DRONE_LANDED_AT_RECEIVER);  //TODO : check if it threadsafe and correct
                    applyStatusChange(DRONE_LANDED_AT_RECEIVER);
                }
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
                drone_longitude=new_drone_longitude;
                updateMap();
            }


        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    }

    private class ReceiverGPSUpdateEventListener implements ValueEventListener {         //listener to listen to changes in firebase
        private Context context;

        ReceiverGPSUpdateEventListener(Context context) {
            this.context=context;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            double new_receiver_latitude=dataSnapshot.child("GPS").child("north").getValue(Double.class).doubleValue();
            double new_receiver_longitude=dataSnapshot.child("GPS").child("east").getValue(Double.class).doubleValue();

            if(new_receiver_latitude!=receiver_latitude || new_receiver_longitude!=receiver_longitude)
            {
                receiver_latitude=new_receiver_latitude;
                receiver_longitude=new_receiver_longitude;
                if(status<=DRONE_NEAR_RECEIVER)
                {
                    droneHandler.goTo(drone_latitude,drone_longitude);
                    updateMap();
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    }
}



//TODO : include changes in receiver position
//TODO : manage timeout of receiver GPS (ie make the drone fly back if no GPS signal received from receiver for more than a given time
