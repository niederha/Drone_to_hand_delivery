/**
 TODO : retrieve sender name (from intent extra or from local storage)   (for now, hard coded in OnCreate)
 TODO : retrieve receiver name (probably from intend extra)   (for now, hard coded in OnCreate)
 TODO : improve aesthetic (layout, ...)
 TODO : stop the background process that checks for receiving
 TODO : send user location as "drone_GPS" to database?
 TODO: set properly ETA
 TODO : set properly distance
**/
package felix_loc_herman.drone_delivery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class CreateFormActivity extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_SENT_TO_DATABASE = 1;      //status code for the delivery status (firebase)

    private String sender_username;
    private String receiver_username;
    private String itemName;    //name of the item to be sent
    private Float itemQuantity; //number of items to be sent (NaN if not specified)
    private String message;     //mesage sent by the sender to the receiver
    private DroneHandler droneHandler;
    private double user_longitude=0;
    private double user_latitude=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle b=getIntent().getExtras();
        sender_username=b.getString("username");
        receiver_username=b.getString("receiver_username");
        DroneHandler droneHandler=new DroneHandler(getApplicationContext());    //TODO : remove

        setContentView(R.layout.activity_create_form);  //display the layout


        //listen for GPS
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int TAG_CODE_PERMISSION_LOCATION=42;
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    TAG_CODE_PERMISSION_LOCATION);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, this);  //updates nly if changes more than 50 meters, and no more than once every 10 seconds

    }
    //functions to receive GPS
    @Override
    public void onLocationChanged(Location location) {
        user_latitude = location.getLatitude();
        user_longitude = location.getLongitude();
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
    public void formCancelButtonClicked(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void formSendButtonClicked(View view) {
        TextView itemNameText = findViewById(R.id.itemNameText);    //get TextView
        itemName= itemNameText.getText().toString();         //extract name of item from TextView

        try {
            TextView quantityText = findViewById(R.id.quantityText);    //get TextView
            itemQuantity= Float.valueOf(quantityText.getText().toString());         //extract name of item from TextView
        } catch (NumberFormatException e) {
            itemQuantity=Float.NaN;           //using NaN (not a number) if the quantaity is invalid
        }


        TextView messageText = findViewById(R.id.messageText);    //get TextView
        message= messageText.getText().toString();         //extract name of item from TextView

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference deliveryGetRef = database.getReference("deliveries");
        DatabaseReference deliveryRef = deliveryGetRef.child(sender_username);


        deliveryRef.runTransaction(new UploadFormContentHandler(this));


    }

    private class UploadFormContentHandler implements Transaction.Handler { //This class does the actual upload and handles the launching of the next activity once finished

        private final Context context;

        UploadFormContentHandler(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public Transaction.Result doTransaction(@NonNull MutableData
                                                        mutableData) {
            //mutableData.child("sender").setValue(sender_username);    //not needed : it is already the key
            mutableData.child("reciever").setValue(receiver_username);
            mutableData.child("cancelled").setValue(false);
            mutableData.child("cancel_ack").setValue(false);
            mutableData.child("status").setValue(REQUEST_SENT_TO_DATABASE);
            mutableData.child("drone_GPS").child("north").setValue(user_latitude);    // add user's GPS cordinate as drone_GPS
            mutableData.child("drone_GPS").child("east").setValue(user_longitude);    //add user's GPS cordinate as drone_GPS
            mutableData.child("landing_allowed").setValue(false);
            mutableData.child("ETA").setValue(0);   //TODO: set properly ETA
            mutableData.child("distance").setValue(0.0);  //TODO : set properly distance
            mutableData.child("item").setValue(itemName);
            mutableData.child("quantity").setValue(itemQuantity);
            mutableData.child("message_to_receiver").setValue(message);

            return Transaction.success(mutableData);
        }

        @Override
        public void onComplete(@Nullable DatabaseError databaseError,
                               boolean b, @Nullable DataSnapshot
                                       dataSnapshot) {
            Intent intent = new Intent(context,WaitingForAcceptationByReceiverActivity.class);
            intent.putExtra("username",sender_username);
            intent.putExtra("receiver_username",receiver_username);
            intent.putExtra("droneHandler",droneHandler);
            startActivity(intent);
        }
    }



}
