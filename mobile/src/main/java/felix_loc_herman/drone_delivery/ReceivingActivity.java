/*package felix_loc_herman.drone_delivery;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ReceivingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving);
    }
}*/
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


public class ReceivingActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

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
    private boolean cancelled_by_sender;
    private int status;

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
        cancelled_by_sender = false;
        status = DRONE_FLYING_TO_RECEIVER;

        setContentView(R.layout.activity_receiving);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.GoogleMap);
        mapFragment.getMapAsync(this);

        //get extras from intent
        Bundle b = getIntent().getExtras();
        sender_username = b.getString("username");
        receiver_username = b.getString("receiver_username");
       // droneHandler = (DroneHandler) b.getSerializable("droneHandler");


        //connect to firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference deliveryGetRef = database.getReference("deliveries");
        deliveryRef = deliveryGetRef.child(sender_username);
        deliveryRef.addValueEventListener(new ReceivingActivity.DeliveryUpdateEventListener(this));

        DatabaseReference userGetRef = database.getReference("receiver");
        receiverGPSRef = userGetRef.child(receiver_username).child("GPS");


        //listen for GPS
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO : handle case when no GPS permission/connection


            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double new_receiver_latitude = location.getLatitude();
        double new_receiver_longitude = location.getLongitude();

        if(receiver_latitude!=new_receiver_latitude || receiver_longitude!=new_receiver_longitude)
        {
            receiver_latitude=new_receiver_latitude;
            receiver_longitude=new_receiver_longitude;
            if(status<=DRONE_NEAR_RECEIVER)    //ie the drone is flying towards the receiver
            {
                receiverGPSRef.child("north").setValue(receiver_latitude);
                receiverGPSRef.child("east").setValue(receiver_longitude);
              //  receiverGPSRef.child("gpstime").setValue((int) (System.currentTimeMillis() / 1000L));
            }
            updateMap();
        }
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

        LatLng droneLocation = new LatLng(drone_latitude,drone_longitude);
        LatLng receiverLocation = new LatLng(receiver_latitude,receiver_longitude);

        double margin=0.01;
        double min_lat=Math.min(drone_latitude,receiver_longitude) - margin;
        double max_lat=Math.max(drone_latitude,receiver_longitude) + margin;
        double min_long=Math.min(drone_longitude,receiver_longitude) - margin;
        double max_long=Math.max(drone_longitude,receiver_longitude) + margin;
        LatLngBounds boundaries=new LatLngBounds(new LatLng(min_lat,min_long), new LatLng(max_lat,max_long));

        //set position and zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundaries, 0));

        //set markers
        mMap.addMarker(new MarkerOptions().position(droneLocation).title("drone"));
        mMap.addMarker(new MarkerOptions().position(receiverLocation).title("you"));

    }

    public void activityReceiverMap_Cancelbutton(View view) {
        deliveryRef.child("cancelled").setValue(true);  //inform the sender that we cancelled the delivery  //TODO : check if it threadsafe and correct
        Toast.makeText(this,"The delivery request has been cancelled successfully : the drone is now flying back to the sender",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ReceivingActivity.this, MainActivity.class);
        intent.putExtra("username",receiver_username);
        intent.putExtra("sender_username",sender_username);
        startActivity(intent);
        //TODO : clean the receiver field?
        //TODO : block the button once drone is landing at receivers : needed?
    }

    public void updateMap() {
        LatLng droneLocation = new LatLng(drone_latitude, drone_longitude);
        LatLng receiverLocation = new LatLng(receiver_latitude, receiver_longitude);

        double margin = 0.01;
        double min_lat = Math.min(drone_latitude, receiver_longitude) - margin;
        double max_lat = Math.max(drone_latitude, receiver_longitude) + margin;
        double min_long = Math.min(drone_longitude, receiver_longitude) - margin;
        double max_long = Math.max(drone_longitude, receiver_longitude) + margin;
        LatLngBounds boundaries = new LatLngBounds(new LatLng(min_lat, min_long), new LatLng(max_lat, max_long));

        mMap.clear();

        //set position and zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundaries, 0));

        //set markers
        mMap.addMarker(new MarkerOptions().position(droneLocation).title("drone"));
        mMap.addMarker(new MarkerOptions().position(receiverLocation).title("you"));
    }

    private void applyStatusChange(int new_status) {
        TextView tv_state = (TextView) findViewById(R.id.activtyReceiving_state_textView);

        switch (status) {
            case DRONE_FLYING_TO_RECEIVER:
                tv_state.setText("Drone took off and is flying to you");
                break;
            case DRONE_NEAR_RECEIVER:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReceivingActivity.this);
                alertDialog.setTitle("Drone ready to land");
                alertDialog.setMessage("Click on LAND to allow the drone to land (if the drone is above a location it can't land, then move to an open area : the drone will follow you)");
                alertDialog.setPositiveButton("LAND", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("ReceivingActivity", "drone is allowed to land at receiver's");
                        deliveryRef.child("status").setValue(DRONE_LANDING_AT_RECEIVER);
                        applyStatusChange(DRONE_LANDING_AT_SENDER);
                    }
                });
                alertDialog.setNegativeButton("CANCEL Delivery", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activityReceiverMap_Cancelbutton(null); //TODO : check if it works or if I have to copy the content
                    }
                });
                alertDialog.show();
                break;
            case DRONE_LANDING_AT_RECEIVER:
                tv_state.setText("Drone is landing");
                break;
            case DRONE_LANDED_AT_RECEIVER:
                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(ReceivingActivity.this);
                alertDialog2.setTitle("Drone landed");
                alertDialog2.setMessage("Click on TAKE-OFF to allow the drone to take off again once you finished unloading it");
                alertDialog2.setPositiveButton("TAKE-OFF", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("ReceivingActivity", "drone unloaded and allowed to take off again");
                        deliveryRef.child("status").setValue(DRONE_FLYING_BACK_TO_SENDER);
                        applyStatusChange(DRONE_FLYING_BACK_TO_SENDER);
                    }
                });
                alertDialog2.show();
                break;
            case DRONE_FLYING_BACK_TO_SENDER:
                Intent intent = new Intent(ReceivingActivity.this, MainActivity.class);
                intent.putExtra("username", receiver_username);
                intent.putExtra("sender_username", sender_username);
                startActivity(intent);
                //TODO : delete delivery structure?
                break;
            case DRONE_NEAR_SENDER:
            case DRONE_LANDING_AT_SENDER:
            case DRONE_LANDED_AT_SENDER:
                Log.wtf("ReceivingActivity", "The status should never have reached one of the default cases");
                break;
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
            boolean new_cancelled_by_sender=dataSnapshot.child("cancelled").getValue(Boolean.class).booleanValue();     //retrieve cancelling status
            int new_status=dataSnapshot.child("status").getValue(Integer.class).intValue();


            if(new_cancelled_by_sender && !cancelled_by_sender) {    //the sender cancelled the delivery
                cancelled_by_sender=true;
                Toast.makeText(context,"The sender cancelled the delivery : the drone is now flying back to sender",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ReceivingActivity.this, MainActivity.class);
                intent.putExtra("username",receiver_username);
                intent.putExtra("sender_username",sender_username);
                startActivity(intent);
                //TODO : clean delivery structure from database?
            }
            else if(new_status!=status)
            {
                status=new_status;
                applyStatusChange(new_status);
            }


            if(new_ETA!=ETA)
            {
                ETA=new_ETA;
                TextView tv_ETA=(TextView) findViewById(R.id.activityReceiving_ETA_textView);
                tv_ETA.setText(String.valueOf(ETA));
            }

            if(new_distance!=distance)
            {
                distance=new_distance;
                TextView tv_dist=(TextView) findViewById(R.id.activityReceiving_distance_textView);
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

}