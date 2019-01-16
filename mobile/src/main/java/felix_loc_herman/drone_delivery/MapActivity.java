package felix_loc_herman.drone_delivery;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private DroneHandler droneHandler;
    private String sender_username;
    private String receiver_username;
    private DatabaseReference deliveryRef;
    private DatabaseReference receiverGPSRef;
    private ValueEventListener myReceiverGPSEventListener;
    private ValueEventListener valueEventListenerDelivery;
    private double distance;
    private double ETA;
    private double drone_longitude;
    private double drone_latitude;
    private double receiver_longitude;
    private double receiver_latitude;
    private double sender_longitude;
    private double sender_latitude;
    private boolean cancelled_by_receiver;
    private int status;
    private int droneStatus;

    private final int DRONE_FLYING_TO_RECEIVER = 5;
    private final int DRONE_NEAR_RECEIVER = 6;
    private final int DRONE_LANDING_AT_RECEIVER = 7;
    private final int DRONE_LANDED_AT_RECEIVER = 8;
    private final int DRONE_FLYING_BACK_TO_SENDER = 9;
    private final int DRONE_NEAR_SENDER = 10;
    private final int DRONE_LANDING_AT_SENDER = 11;
    private final int DRONE_LANDED_AT_SENDER = 12;


    private final String TAG = this.getClass().getSimpleName();


    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cancelled_by_receiver = false;
        status = DRONE_FLYING_TO_RECEIVER;
        droneStatus = DroneHandler.FLYING; //TODO : or do we start in IDLE?

        setContentView(R.layout.activity_map);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.GoogleMap);
        mapFragment.getMapAsync(this);

        //get extras from intent
        Bundle b = getIntent().getExtras();
        sender_username = b.getString("username");
        receiver_username = b.getString("receiver_username");
        droneHandler = (DroneHandler) b.getSerializable("droneHandler");


        //connect to firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference deliveryGetRef = database.getReference("deliveries");
        deliveryRef = deliveryGetRef.child(sender_username);
        valueEventListenerDelivery = deliveryRef.addValueEventListener(new DeliveryUpdateEventListener(this));

        DatabaseReference userGetRef = database.getReference("receiver");
        receiverGPSRef = userGetRef.child(receiver_username).child("GPS");
        myReceiverGPSEventListener = receiverGPSRef.addValueEventListener(new ReceiverGPSUpdateEventListener(this));

        //listen for GPS

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int TAG_CODE_PERMISSION_LOCATION=42;
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    TAG_CODE_PERMISSION_LOCATION);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double new_sender_latitude = location.getLatitude();
        double new_sender_longitude = location.getLongitude();

        if(sender_latitude!=new_sender_latitude || sender_longitude!=new_sender_longitude)
        {
            sender_latitude=new_sender_latitude;
            sender_longitude=new_sender_longitude;
            if(status==DRONE_FLYING_BACK_TO_SENDER || status==DRONE_NEAR_SENDER)    //ie the drone is flying towards sender
            {
                droneHandler.goTo(sender_latitude,sender_longitude);
            }
        }
        updateMap();
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
/*
        //TODO : initialize with drone and receiver positions (and correct zoom?)

        LatLng droneLocation = new LatLng(drone_latitude,drone_longitude);
        LatLng receiverLocation = new LatLng(receiver_latitude,receiver_longitude);
        LatLng senderLocation = new LatLng(sender_latitude,sender_longitude);

        double margin=0.01;
        double min_lat=Math.min(Math.min(drone_latitude,sender_latitude),receiver_longitude) - margin;
        double max_lat=Math.max(Math.max(drone_latitude,sender_latitude),receiver_longitude) + margin;
        double min_long=Math.min(Math.min(drone_longitude,sender_longitude),receiver_longitude) - margin;
        double max_long=Math.max(Math.max(drone_longitude,sender_longitude),receiver_longitude) + margin;
        LatLngBounds boundaries=new LatLngBounds(new LatLng(min_lat,min_long), new LatLng(max_lat,max_long));

        //set position and zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundaries, 0));

        //set markers
        mMap.addMarker(new MarkerOptions().position(droneLocation).title("drone"));
        mMap.addMarker(new MarkerOptions().position(receiverLocation).title("receiver"));
        mMap.addMarker(new MarkerOptions().position(senderLocation).title("sender"));*/

    }

    public void activityMap_Cancelbutton(View view) {   //note that this button is blocked once the drone started landing at receiver
        deliveryRef.child("cancelled").setValue(true);  //inform the receiver that we cancelled the delivery  //TODO : check if it threadsafe and correct
        Toast.makeText(this,"The delivery request has been cancelled successfully : the drone is now flying back to you",Toast.LENGTH_SHORT).show();
        applyStatusChange(DRONE_FLYING_BACK_TO_SENDER);
    }

    public void updateMap()
    {
        LatLng droneLocation = new LatLng(drone_latitude,drone_longitude);
        LatLng receiverLocation = new LatLng(receiver_latitude,receiver_longitude);
        LatLng senderLocation = new LatLng(sender_latitude,sender_longitude);

        double min_lat=Math.min(Math.min(drone_latitude,sender_latitude),receiver_latitude);
        double max_lat=Math.max(Math.max(drone_latitude,sender_latitude),receiver_latitude);
        double min_long=Math.min(Math.min(drone_longitude,sender_longitude),receiver_longitude);
        double max_long=Math.max(Math.max(drone_longitude,sender_longitude),receiver_longitude);
        double lat_margin = 0.1*Math.abs(max_lat-min_lat);
        double long_margin = 0.1*Math.abs(max_long-min_long);
        min_lat-=lat_margin;
        max_lat+=lat_margin;
        min_long-=long_margin;
        max_lat+=long_margin;
        LatLngBounds boundaries=new LatLngBounds(new LatLng(min_lat,min_long), new LatLng(max_lat,max_long));

        mMap.clear();

        //set position and zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundaries, 0));

        //set markers
        mMap.addMarker(new MarkerOptions().position(droneLocation).title("drone"));
        mMap.addMarker(new MarkerOptions().position(receiverLocation).title("receiver"));
        mMap.addMarker(new MarkerOptions().position(senderLocation).title("sender"));
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
            receiverGPSRef.removeEventListener(myReceiverGPSEventListener);


            TextView tv_state=(TextView) findViewById(R.id.activtyMap_state_textView);
            tv_state.setText("Drone landed at receiver");

        }
        else if(status==DRONE_FLYING_BACK_TO_SENDER) //TODO(@receiver) : change to DRONE_FLYING_BACK_TO_SENDER done by receiver
        {
            TextView tv_state=(TextView) findViewById(R.id.activtyMap_state_textView);
            tv_state.setText("Drone flying back to sender");
            droneHandler.takeOff();
            droneHandler.goTo(sender_latitude,sender_longitude);

        }
        else if(status==DRONE_NEAR_SENDER)
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapActivity.this);
            alertDialog.setTitle("Drone ready to land");
            alertDialog.setMessage("Click on LAND to allow the drone to land (if the drone is above a location it can't land, then move to an open area : the drone will follow you)");
            alertDialog.setPositiveButton("LAND", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.i("MapActivity","drone is allowed to land at sender's");
                    status=DRONE_LANDING_AT_SENDER;
                    deliveryRef.child("status").setValue(DRONE_LANDING_AT_SENDER);  //TODO : check if it threadsafe and correct
                    applyStatusChange(DRONE_LANDING_AT_SENDER);
                }
            });
            alertDialog.show();
        }
        else if(status==DRONE_LANDING_AT_SENDER)
        {
            TextView tv_state=(TextView) findViewById(R.id.activtyMap_state_textView);
            tv_state.setText("Drone is landing");
            droneHandler.land();;
        }
        else if(status==DRONE_LANDED_AT_SENDER)
        {
            //TODO : modify if we want a summary or a history of delivery(ies)
            deliveryRef.setValue(null); //delete the delivery structure
            Intent intent = new Intent(this,MainActivity.class);
            intent.putExtra("username",sender_username);
            intent.putExtra("receiver_username",receiver_username);
            intent.putExtra("droneHandler",droneHandler);
            startActivity(intent);

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
                Toast.makeText(context,"The receiver cancelled the delivery : the drone is now flying back to you",Toast.LENGTH_LONG).show();
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
                else if(droneStatus==DroneHandler.LANDED && status==DRONE_LANDING_AT_SENDER)  //the drone just landed at sender
                {
                    status=DRONE_LANDED_AT_SENDER;
                    deliveryRef.child("status").setValue(DRONE_LANDED_AT_SENDER);  //TODO : check if it threadsafe and correct
                    applyStatusChange(DRONE_LANDED_AT_SENDER);
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
            double new_receiver_latitude=dataSnapshot.child("north").getValue(Double.class).doubleValue();
            double new_receiver_longitude=dataSnapshot.child("east").getValue(Double.class).doubleValue();

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

    public void onDestroy()
    {
        Log.i("MapActivity","OnDestroy : stopping listeners");
        if(deliveryRef!=null && valueEventListenerDelivery!=null)
            deliveryRef.removeEventListener(valueEventListenerDelivery);
        if(receiverGPSRef!=null && myReceiverGPSEventListener!=null)
            receiverGPSRef.removeEventListener(myReceiverGPSEventListener);
        super.onDestroy();
    }
}



//TODO : manage timeout of receiver GPS (ie make the drone fly back if no GPS signal received from receiver for more than a given time
